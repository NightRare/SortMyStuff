package nz.ac.aut.comp705.sortmystuff.util.schedulers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * Created by YuanY on 2017/9/23.
 */

public class ImmediateSchedularProvider implements ISchedulerProvider {

    @NonNull
    @Override
    public Scheduler computation() {
        return Schedulers.immediate();
    }

    @NonNull
    @Override
    public Scheduler io() {
        return Schedulers.immediate();
    }

    @NonNull
    @Override
    public Scheduler ui() {
        return Schedulers.immediate();
    }
}
