package nz.ac.aut.comp705.sortmystuff.utils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

import static com.google.common.base.Preconditions.checkNotNull;

public class RetryWithDelay implements
        Func1<Observable<? extends Throwable>, Observable<?>> {

    public RetryWithDelay(
            final int maxRetries,
            final int retryDelayMillis) {
        this(maxRetries,
                retryDelayMillis,
                Throwable.class);
    }

    public RetryWithDelay(
            final int maxRetries,
            final int retryDelayMillis,
            Class<? extends Throwable> retryOnThrowable) {
        this.maxRetries = maxRetries;
        this.retryDelayMillis = retryDelayMillis;
        this.retryOnThrowable = checkNotNull(retryOnThrowable);
        this.retryCount = 0;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> attempts) {
        return attempts
                .flatMap(new Func1<Throwable, Observable<?>>() {
                    @Override
                    public Observable<?> call(Throwable throwable) {
                        if (throwable.getClass().equals(retryOnThrowable) &&
                                ++retryCount < maxRetries) {
                            // When this Observable calls onNext, the original
                            // Observable will be retried (i.e. re-subscribed).
                            return Observable.timer(retryDelayMillis,
                                    TimeUnit.MILLISECONDS);
                        }

                        // Max retries hit, or not retryable throwable, pass the error along.
                        return Observable.error(throwable);
                    }
                });
    }

    private final int maxRetries;
    private final int retryDelayMillis;
    private int retryCount;
    private Class<? extends Throwable> retryOnThrowable;

}
