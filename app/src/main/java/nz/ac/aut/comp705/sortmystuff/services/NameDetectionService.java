package nz.ac.aut.comp705.sortmystuff.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;
import nz.ac.aut.comp705.sortmystuff.utils.AppStrings;
import nz.ac.aut.comp705.sortmystuff.utils.RetryWithDelay;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import static nz.ac.aut.comp705.sortmystuff.utils.AppStrings.ASSET_DEFAULT_NAME;

public class NameDetectionService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();

        IFactory factory = ((SortMyStuffApp) getApplication()).getFactory();
        mDataManager = factory.getDataManager();
        mSchedulerProvider = factory.getSchedulerProvider();
        mSubscriptions = new CompositeSubscription();

        mAssetIds = new LinkedList<>();
        mServing = false;
    }

    @Override
    public void onDestroy() {
        cancelAll();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    public void cancelAll() {
        mSubscriptions.clear();
    }

    private void handleIntent(@Nullable Intent intent) {
        if (intent == null) return;
        String assetId = intent.getStringExtra(AppStrings.INTENT_NAME_DETECTION_ASSET_ID);

        if (assetId == null) return;
        mAssetIds.add(assetId);
        serveRequests();
    }

    private synchronized void serveRequests() {
        if (mServing || mAssetIds.isEmpty()) return;

        mServing = true;
        String assetId = mAssetIds.get(0);
        Subscription subscription = mDataManager.getAsset(assetId)
                .subscribeOn(mSchedulerProvider.newThread())
                .doOnNext(asset -> {
                    if (asset == null)
                        throw new AssetNotFoundException();
                    if (!conformsToDefaultNamingScheme(asset.getName()))
                        throw new IllegalStateException();
                })
                // retry if the asset is not yet created in DataManager
                .retryWhen(new RetryWithDelay(MAX_RETRY, 3000, AssetNotFoundException.class))
                .flatMap(asset -> mDataManager.getPhotoDetail(assetId))
                .flatMap(imageDetail -> {
                    if (imageDetail.isDefaultFieldValue())
                        throw new IllegalStateException();
                    return mDataManager.getNewAssetName(imageDetail.getField());
                })
                .subscribe(
                        //onNext
                        name -> {
                            if (name != null)
                                mDataManager.updateAssetName(assetId, name);
                            serveNext();
                        },
                        // onError
                        throwable -> {
                            serveNext();
                        }
                );
        mSubscriptions.add(subscription);
    }

    private synchronized void serveNext() {
        mAssetIds.remove(0);
        mServing = false;
        serveRequests();
    }

    //region PRIVATE STUFF

    private static boolean conformsToDefaultNamingScheme(String name) {
        if (name == null) return false;
        String subject = name.trim();

        return subject.contains(ASSET_DEFAULT_NAME) &&
                subject.startsWith(ASSET_DEFAULT_NAME) &&
                isNonNegativeInteger(subject.substring(ASSET_DEFAULT_NAME.length()).trim());
    }

    private static boolean isNonNegativeInteger(String input) {
        try {
            int value = Integer.parseInt(input);
            return value >= 0;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private static class AssetNotFoundException extends RuntimeException {

    }

    private final static int MAX_RETRY = 5;

    private IDataManager mDataManager;
    private ISchedulerProvider mSchedulerProvider;
    private CompositeSubscription mSubscriptions;
    private List<String> mAssetIds;

    private boolean mServing;

    //endregion
}
