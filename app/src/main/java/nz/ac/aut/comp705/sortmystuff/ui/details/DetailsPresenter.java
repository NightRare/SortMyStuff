package nz.ac.aut.comp705.sortmystuff.ui.details;

import android.graphics.Bitmap;

import java.util.AbstractMap;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;
import nz.ac.aut.comp705.sortmystuff.util.schedulers.ISchedulerProvider;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class DetailsPresenter implements IDetailsPresenter {

    public DetailsPresenter(IDataManager dataManager, IDetailsView view,
                            ISchedulerProvider schedulerProvider, String currentAssetId) {
        mDataManager = checkNotNull(dataManager, "The dataManager cannot be null.");
        mView = checkNotNull(view, "The view cannot be null.");
        mSchedulerProvider = checkNotNull(schedulerProvider, "The schedulerProvider cannot be null.");
        checkNotNull(currentAssetId, "The currentAssetId cannot be null.");
        checkArgument(!currentAssetId.replaceAll(" ", "").isEmpty(), currentAssetId);
        mCurrentAssetId = currentAssetId;

        mSubscriptions = new CompositeSubscription();
        mView.setPresenter(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe() {
        loadDetails();
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    /**
     * {@inheritDoc}
     *
     * @return detailList
     */
    @Override
    public void loadDetails() {
        if (mCurrentAssetId.equals(mDataManager.getRootAsset().getId())) {
            mView.showRootAssetDetailPage();
            return;
        }

        mView.setLoadingIndicator(true);
        mSubscriptions.clear();
        Subscription subscription = mDataManager
                .getDetails(mCurrentAssetId)
                .zipWith(mDataManager.getAsset(mCurrentAssetId),
                        (details, assets) -> new AbstractMap.SimpleEntry<>(assets, details))
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(
                        //onNext
                        args -> mView.showDetails(args.getKey(), args.getValue()),
                        //onError
                        throwable -> mView.showLoadingDetailsError(throwable),
                        //onCompleted
                        () -> mView.setLoadingIndicator(false)
                );
        mSubscriptions.add(subscription);
    }

    @Override
    public void updateTextDetail(final IDetail<String> detail, String newText) {
        boolean edited = false;
        try {
            mDataManager.updateTextDetail(mCurrentAssetId, detail.getId(), detail.getLabel(), newText);
            edited = true;
        } catch (IllegalArgumentException e) {
            mView.showMessage(e.getMessage());
        }
        loadDetails();
        if(edited)
            mView.showMessage("Edited " + detail.getLabel());
    }

    /**
     * {@inheritDoc}
     *
     * @param newImage
     */
    @Override
    public void updateAssetPhoto(final Bitmap newImage) {
        checkNotNull(newImage, "The new image cannot be null");

        mSubscriptions.clear();
        Subscription subscription = mDataManager
                .getDetails(mCurrentAssetId)
                .flatMap(Observable::from)
                .doOnNext(detail -> {
                    if(detail.getLabel().equals(CategoryType.BasicDetail.PHOTO))
                        mDataManager.updateImageDetail((IDetail<Bitmap>) detail, detail.getLabel(), newImage);
                })
                .toList()
                .zipWith(mDataManager.getAsset(mCurrentAssetId),
                        (details, assets) -> new AbstractMap.SimpleEntry<>(assets, details))
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(
                        //onNext
                        args -> mView.showDetails(args.getKey(), args.getValue()),
                        //onError
                        throwable -> mView.showLoadingDetailsError(throwable),
                        //onCompleted
                        () -> mView.setLoadingIndicator(false)
                );
        mSubscriptions.add(subscription);
    }

    /**
     * {@inheritDoc}
     *
     * @param imageDetail
     */
    @Override
    public void resetImage(IDetail<Bitmap> imageDetail) {
        checkNotNull(imageDetail, "The image detail cannot be null");
        mDataManager.resetImageDetail(imageDetail);
        loadDetails();
    }

    @Override
    public void setCurrentAsset(String currentAssetId) {
        checkNotNull(currentAssetId, "The currentAssetId cannot be null.");
        checkArgument(!currentAssetId.replaceAll(" ", "").isEmpty(), currentAssetId);
        mCurrentAssetId = currentAssetId;
    }

    // region PRIVATE STUFF

    private IDataManager mDataManager;
    private IDetailsView mView;
    private String mCurrentAssetId;
    private ISchedulerProvider mSchedulerProvider;
    private CompositeSubscription mSubscriptions;

    //endregion
}
