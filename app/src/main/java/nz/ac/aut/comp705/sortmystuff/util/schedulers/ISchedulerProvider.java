package nz.ac.aut.comp705.sortmystuff.util.schedulers;

import android.support.annotation.NonNull;

import rx.Scheduler;

/**
 * Created by YuanY on 2017/9/23.
 */

public interface ISchedulerProvider {

    @NonNull
    Scheduler computation();

    @NonNull
    Scheduler io();

    @NonNull
    Scheduler ui();
}
