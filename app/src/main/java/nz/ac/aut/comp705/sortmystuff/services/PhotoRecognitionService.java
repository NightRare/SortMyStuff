package nz.ac.aut.comp705.sortmystuff.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;
import rx.Emitter;
import rx.Observable;

public class PhotoRecognitionService extends Service {

    //region Service METHODS

    @Override
    public void onCreate() {
        super.onCreate();

        IFactory factory = ((SortMyStuffApp) getApplication()).getFactory();
        mDataManager = factory.getDataManager();
        mSchedulerProvider = factory.getSchedulerProvider();
    }

    @Override
    public void onDestroy() {
        killCurrentTask();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //endregion

    public class ServiceBinder extends Binder {

        public boolean isRunning() {
            return mTask != null && mTask.isRunning();
        }

        public boolean isPending() {
            return mTask != null && !mTask.isTerminated() && !mTask.isRunning();
        }

        public void resetPendingTimer(long delayedMillis) {
            if (isPending()) {
                mTask.start(delayedMillis);
            }
        }

        public synchronized Observable<List<IPhotoRecognitionResult>> startTask(long delayedMillis) {
            if (isRunning() || isPending())
                return Observable.error(new IllegalStateException("A task is already started"));

            return Observable.create(emitter -> {
                PhotoRecognitionListener listener = new PhotoRecognitionListener() {
                    @Override
                    public void onProgress(int progress) {

                    }

                    @Override
                    public void onSucceeded(List<IPhotoRecognitionResult> results) {
                        emitter.onNext(results);
                        killCurrentTask();
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        emitter.onError(throwable);
                        killCurrentTask();
                    }
                };


                mTask = new PhotoRecognitionTask(mDataManager, listener);
                mTask.start(delayedMillis > 0 ? delayedMillis : 0);
            }, Emitter.BackpressureMode.BUFFER);
        }

        public synchronized void terminateTask() {
            killCurrentTask();
        }
    }

    //region PRIVATE STUFF

    private synchronized void killCurrentTask() {
        if (mTask != null) {
            mTask.terminate();
            mTask = null;
        }
    }

    private final ServiceBinder mBinder = new ServiceBinder();

    private IDataManager mDataManager;
    private ISchedulerProvider mSchedulerProvider;

    private PhotoRecognitionTask mTask;


    //endregion
}
