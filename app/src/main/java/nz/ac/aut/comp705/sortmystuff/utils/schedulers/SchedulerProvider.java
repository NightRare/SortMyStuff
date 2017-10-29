package nz.ac.aut.comp705.sortmystuff.utils.schedulers;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Singleton
public class SchedulerProvider implements ISchedulerProvider {

    @Inject
    public SchedulerProvider() {

    }

    @Override
    public Scheduler computation() {
        return Schedulers.computation();
    }

    @Override
    public Scheduler io() {
        return Schedulers.io();
    }

    @Override
    public Scheduler ui() {
        return AndroidSchedulers.mainThread();
    }

    @Override
    public Scheduler newThread() {
        return Schedulers.newThread();
    }

}
