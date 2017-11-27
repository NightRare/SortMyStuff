package nz.ac.aut.comp705.sortmystuff.services;

import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;
import nz.ac.aut.comp705.sortmystuff.utils.AppConfigs;
import rx.Observable;
import rx.Subscription;

import static nz.ac.aut.comp705.sortmystuff.utils.AssetNamingHelper.conformsToDefaultNamingScheme;

public class PhotoRecognitionTask {

    public PhotoRecognitionTask(
            @NonNull IDataManager dataManager,
            @NonNull PhotoRecognitionListener photoRecognitionListener) {
        mDataManager = dataManager;
        mPhotoRecognitionListener = photoRecognitionListener;
        mProcessed = 0;
        mTotalTasks = 0;
        mRunning = false;
        mHandler = new Handler();
        mTerminated = false;
        mResultList = new ArrayList<>();
    }

    public void start(long delayed) {
        checkTaskAvailability();
        stopPendingTask();
        if (mRunning) return;
        mHandler = new Handler();
        mHandler.postDelayed(this::execute, delayed >= 0 ? delayed : 0);
    }

    public void start() {
        checkTaskAvailability();
        stopPendingTask();
        if (mRunning) return;
        mHandler = new Handler();
        mHandler.post(this::execute);
    }

    public void stopPendingTask() {
        checkTaskAvailability();
        if (mRunning) return;
        mHandler.removeCallbacksAndMessages(null);
    }

    public void terminate() {
        if (mTerminated) return;
        synchronized (PhotoRecognitionTask.class) {
            if (mRunning || mSubscription != null) {
                mTerminated = true;
                mSubscription.unsubscribe();
                mRunning = false;
                mProcessed = 0;
                mTotalTasks = 0;
                mHandler.removeCallbacksAndMessages(null);
            }
        }
    }

    public boolean isRunning() {
        return mRunning;
    }

    public boolean isTerminated() {
        return mTerminated;
    }

    // region PRIVATE STUFF

    private void execute() {
        synchronized (PhotoRecognitionTask.class) {
            mRunning = true;

            mSubscription = mDataManager.getAssets()
                    .flatMap(Observable::from)
                    .serialize()
                    .onBackpressureBuffer()
                    .filter(asset -> conformsToDefaultNamingScheme(asset.getName()))
                    .map(TaskUnit::new)
                    .flatMap(this::zipPhotoDetail)
                    .filter(taskUnit -> !taskUnit.failed && !taskUnit.completed)
                    .toList()
                    .flatMap(taskList -> {
                        synchronized (PhotoRecognitionTask.class) {
                            if(taskList.isEmpty())
                                throw new IllegalStateException();
                            mTotalTasks = taskList.size();
                        }
                        return Observable.from(taskList);
                    })
                    .serialize()
                    .onBackpressureBuffer()
                    .zipWith(Observable.interval(AppConfigs.PHOTO_RECOGNITION_INTERVAL, TimeUnit.MILLISECONDS),
                            (taskUnit, delay) -> taskUnit)
                    .flatMap(this::updateName)
                    .onErrorResumeNext(throwable -> Observable.just(null))
                    .subscribe(
                            //onNext
                            this::enqueueResults,
                            // onError
                            throwable -> {
                                mPhotoRecognitionListener.onFailed(throwable);
                                terminate();
                            }
                    );
        }
    }

    private void enqueueResults(TaskUnit taskUnit) {
        synchronized (PhotoRecognitionTask.class) {
            if(mTotalTasks == 0) {
                mPhotoRecognitionListener.onSucceeded(mResultList);
                terminate();
                return;
            }

            int progress = ((int) ++mProcessed / mTotalTasks) * 100;
            mPhotoRecognitionListener.onProgress(progress);

            if(taskUnit != null) {
                mResultList.add(taskUnit);
            }

            if(mProcessed >= mTotalTasks) {
                mPhotoRecognitionListener.onSucceeded(mResultList);
                terminate();
            }
        }
    }

    private void checkTaskAvailability() {
        if (mTerminated) {
            throw new IllegalStateException("Cannot operate a terminated task.");
        }
    }

    private Observable<TaskUnit> zipPhotoDetail(TaskUnit taskUnit) {
        synchronized (PhotoRecognitionTask.class) {
            if (taskUnit.completed)
                return Observable.just(taskUnit);

            return mDataManager.getPhotoDetail(taskUnit.asset.getId())
                    .map(detail -> {
                        synchronized (PhotoRecognitionTask.class) {
                            if (detail == null ||
                                    detail.getField() == null) {
                                taskUnit.completed = true;
                                taskUnit.failed = true;
                                taskUnit.error = ErrorType.PhotoError;
                                return taskUnit;
                            }

                            if (detail.isDefaultFieldValue()) {
                                taskUnit.completed = true;
                                taskUnit.failed = true;
                                taskUnit.error = ErrorType.DefaultPhoto;
                                return taskUnit;
                            }

                            taskUnit.photoDetail = detail;
                            return taskUnit;
                        }
                    })
                    .onErrorReturn(throwable -> {
                        synchronized (PhotoRecognitionTask.class) {
                            taskUnit.completed = true;
                            taskUnit.failed = true;
                            taskUnit.error = ErrorType.Unexpected;
                            taskUnit.throwable = throwable;
                            return taskUnit;
                        }
                    });
        }
    }

    private Observable<TaskUnit> updateName(TaskUnit taskUnit) {
        synchronized (PhotoRecognitionTask.class) {
            if (taskUnit == null || taskUnit.completed)
                return Observable.just(taskUnit);

            if (taskUnit.photoDetail == null || taskUnit.photoDetail.isDefaultFieldValue()) {
                taskUnit.completed = true;
                taskUnit.failed = true;
                taskUnit.error = taskUnit.photoDetail == null ?
                        ErrorType.PhotoError : ErrorType.DefaultPhoto;
                return Observable.just(taskUnit);
            }

            return mDataManager.getNewAssetName(taskUnit.photoDetail.getField())
                    .map(newName -> {
                        synchronized (PhotoRecognitionTask.class) {
                            if (newName == null) {
                                taskUnit.completed = true;
                                taskUnit.failed = true;
                                taskUnit.error = ErrorType.ServiceError;
                                return taskUnit;
                            }

                            // if name is changed on other threads
                            if (!taskUnit.asset.getName().equals(taskUnit.originalName)) {
                                taskUnit.completed = true;
                                taskUnit.failed = true;
                                taskUnit.error = ErrorType.OriginalNameChanged;
                                return taskUnit;
                            }

                            mDataManager.updateAssetName(taskUnit.asset.getId(), newName);
                            taskUnit.completed = true;
                            return taskUnit;
                        }
                    })
                    .onErrorReturn(throwable -> {
                        synchronized (PhotoRecognitionTask.class) {
                            taskUnit.completed = true;
                            taskUnit.failed = true;
                            taskUnit.error = ErrorType.Unexpected;
                            taskUnit.throwable = throwable;
                            return taskUnit;
                        }
                    });
        }
    }

    private enum ErrorType {
        NoError,

        ServiceError,
        DefaultPhoto,
        PhotoError,
        OriginalNameChanged,

        Unexpected
    }

    private static class TaskUnit implements IPhotoRecognitionResult {

        @NonNull
        private final IAsset asset;

        @NonNull
        private final String originalName;

        private IDetail<Bitmap> photoDetail;

        private boolean completed;

        private boolean failed;

        private ErrorType error;

        private Throwable throwable;

        private TaskUnit(@NonNull IAsset asset) {
            this.asset = asset;
            originalName = asset.getName();
            completed = false;
            failed = false;
            error = ErrorType.NoError;
        }

        @Override
        public IAsset asset() {
            return asset;
        }

        @Override
        public String errorMessage() {
            switch (error) {
                case PhotoError:
                    return "Unable to process asset photo.";
                case Unexpected:
                    return "Unexpected error occurred.";
                case DefaultPhoto:
                    return "The photo is reset to default.";
                case ServiceError:
                    return "Server error.";
                case OriginalNameChanged:
                    return "Name is changed manually.";
                default:
                    return "Photo recognition succeeded.";
            }
        }

        @Override
        public boolean failed() {
            return failed;
        }
    }

    private IDataManager mDataManager;
    private PhotoRecognitionListener mPhotoRecognitionListener;

    private Subscription mSubscription;
    private Handler mHandler;

    private boolean mTerminated;
    private boolean mRunning;
    private int mProcessed;
    private int mTotalTasks;
    private List<IPhotoRecognitionResult> mResultList;

    //endregion
}
