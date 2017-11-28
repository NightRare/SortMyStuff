package nz.ac.aut.comp705.sortmystuff.services;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;
import nz.ac.aut.comp705.sortmystuff.utils.AppConfigs;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;

import static nz.ac.aut.comp705.sortmystuff.utils.AssetNamingHelper.conformsToDefaultNamingScheme;

public class PhotoRecognitionTask {

    public PhotoRecognitionTask(
            @NonNull IDataManager dataManager,
            @NonNull PhotoRecognitionListener photoRecognitionListener,
            @NonNull Scheduler scheduler) {
        mDataManager = dataManager;
        mPhotoRecognitionListener = photoRecognitionListener;
        mProcessed = 0;
        mTotalTasks = 0;
        mStatus = Status.Ready;
        mResultList = Collections.synchronizedList(new ArrayList<>());
        mScheduler = scheduler;
    }

    /**
     * Each task can only be started once.
     *
     * @param delayMillis
     */
    public void start(long delayMillis) {
        synchronized (this) {
            if (mStatus != Status.Ready) return;
            mStatus = Status.Pending;
        }
        execute(delayMillis >= 0 ? delayMillis : 0);
    }

    public void terminate() {
        if (mStatus == Status.Terminated) return;
        synchronized (this) {
            mStatus = Status.Terminated;
            mProcessed = 0;
            mTotalTasks = 0;
        }
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
    }

    public boolean isRunning() {
        return mStatus.equals(Status.Running);
    }

    public boolean isPending() {
        return mStatus.equals(Status.Pending);
    }

    public boolean isTerminated() {
        return mStatus.equals(Status.Terminated);
    }

    // region PRIVATE STUFF

    private void execute(long delayMillis) {
        mStatus = Status.Running;

        mSubscription = mDataManager.getAssets()
                .delay(delayMillis, TimeUnit.MILLISECONDS)
                .subscribeOn(mScheduler)
                .flatMap(Observable::from)
                // important to make sure that only one thread is accessing/mutating taskUnit
                // as TaskUnit is not thread safe
                .serialize()
                .onBackpressureBuffer()
                .filter(asset -> conformsToDefaultNamingScheme(asset.getName()))
                .map(TaskUnit::new)
                .flatMap(this::zipPhotoDetail)
                .filter(taskUnit -> !taskUnit.failed && !taskUnit.completed)
                .toList()
                .flatMap(taskList -> {
                    if (taskList.isEmpty())
                        throw new IllegalStateException();
                    mTotalTasks = taskList.size();
                    return Observable.from(taskList);
                })
                .serialize()
                .onBackpressureBuffer()
                .zipWith(Observable.interval(AppConfigs.PHOTO_RECOGNITION_INTERVAL, TimeUnit.MILLISECONDS),
                        (taskUnit, delay) -> taskUnit)
                .flatMap(this::updateName)
                .onErrorResumeNext(throwable -> Observable.just(null))
                .subscribe(this::enqueueResults);
    }

    private void enqueueResults(TaskUnit taskUnit) {
        if (mTotalTasks == 0) {
            mPhotoRecognitionListener.onSucceeded(mResultList);
            terminate();
            return;
        }

        int progress = ((int) ++mProcessed / mTotalTasks) * 100;
        mPhotoRecognitionListener.onProgress(progress);

        if (taskUnit != null) {
            mResultList.add(taskUnit.asResult());
        }

        if (mProcessed == mTotalTasks) {
            mPhotoRecognitionListener.onSucceeded(mResultList);
            terminate();
        }
    }

    private Observable<TaskUnit> zipPhotoDetail(TaskUnit taskUnit) {
        if (taskUnit.completed)
            return Observable.just(taskUnit);

        return mDataManager.getPhotoDetail(taskUnit.asset.getId())
                .map(detail -> {
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
                })
                .onErrorReturn(throwable -> {
                    taskUnit.completed = true;
                    taskUnit.failed = true;
                    taskUnit.error = ErrorType.Unexpected;
                    taskUnit.throwable = throwable;
                    return taskUnit;
                });
    }

    private Observable<TaskUnit> updateName(TaskUnit taskUnit) {
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
                })
                .onErrorReturn(throwable -> {
                    taskUnit.completed = true;
                    taskUnit.failed = true;
                    taskUnit.error = ErrorType.Unexpected;
                    taskUnit.throwable = throwable;
                    return taskUnit;
                });
    }

    private enum ErrorType {
        NoError,

        ServiceError,
        DefaultPhoto,
        PhotoError,
        OriginalNameChanged,

        Unexpected
    }

    private static class TaskResult implements IPhotoRecognitionResult {

        private final IAsset asset;
        private final String originalName;
        private final String errorMessage;
        private final boolean failed;

        public TaskResult(
                IAsset asset,
                String errorMessage,
                String originalName,
                boolean failed) {

            this.asset = asset;
            this.errorMessage = errorMessage;
            this.failed = failed;
            this.originalName = originalName;
        }

        @Override
        public IAsset asset() {
            return asset;
        }

        @Override
        public String originalName() {
            return originalName;
        }

        @Override
        public String errorMessage() {
            return errorMessage;
        }

        @Override
        public boolean failed() {
            return failed;
        }
    }

    private static class TaskUnit {

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

        private IPhotoRecognitionResult asResult() {
            return new TaskResult(asset, getErrorMessage(), originalName, failed);
        }

        private String getErrorMessage() {
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
    }

    private enum Status {
        Ready,
        Pending,
        Running,
        Terminated
    }

    private final IDataManager mDataManager;
    private final PhotoRecognitionListener mPhotoRecognitionListener;
    private final Scheduler mScheduler;

    private volatile Subscription mSubscription;
    private volatile Status mStatus;

    private volatile int mProcessed;
    private volatile int mTotalTasks;
    private final List<IPhotoRecognitionResult> mResultList;

    //endregion
}
