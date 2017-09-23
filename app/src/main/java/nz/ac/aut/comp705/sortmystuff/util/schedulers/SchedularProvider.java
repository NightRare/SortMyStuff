package nz.ac.aut.comp705.sortmystuff.util.schedulers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by YuanY on 2017/9/23.
 */

public class SchedularProvider implements ISchedulerProvider {

    public static synchronized SchedularProvider getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SchedularProvider();
        }
        return INSTANCE;
    }

    @NonNull
    @Override
    public Scheduler computation() {
        return Schedulers.computation();
    }

    @NonNull
    @Override
    public Scheduler io() {
        return Schedulers.io();
    }

    @NonNull
    @Override
    public Scheduler ui() {
        return AndroidSchedulers.mainThread();
    }

    @Nullable
    private static SchedularProvider INSTANCE;

    private SchedularProvider() {};
}
