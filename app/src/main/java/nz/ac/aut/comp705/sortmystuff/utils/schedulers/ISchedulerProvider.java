package nz.ac.aut.comp705.sortmystuff.utils.schedulers;

import android.support.annotation.NonNull;

import rx.Scheduler;

public interface ISchedulerProvider {

    @NonNull
    Scheduler computation();

    @NonNull
    Scheduler io();

    @NonNull
    Scheduler ui();

    @NonNull
    Scheduler newThread();
}
