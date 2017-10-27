package nz.ac.aut.comp705.sortmystuff.ui.details;

import android.graphics.Bitmap;

import java.util.AbstractMap;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static nz.ac.aut.comp705.sortmystuff.utils.AppConstraints.ROOT_ASSET_ID;

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
        if(!mView.isReady()) return;

        if (mCurrentAssetId.equals(ROOT_ASSET_ID)) {
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
            mDataManager.updateDetail(mCurrentAssetId, detail.getId(), detail.getType(), null, newText);
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
    public void updateAssetPhoto(IDetail<Bitmap> photo, final Bitmap newImage) {
        checkNotNull(newImage, "The new image cannot be null");

        mDataManager.updateDetail(photo.getAssetId(), photo.getId(), photo.getType(), null, newImage);
        loadDetails();
    }

    /**
     * {@inheritDoc}
     *
     * @param imageDetail
     */
    @Override
    public void resetImage(IDetail<Bitmap> imageDetail) {
        checkNotNull(imageDetail, "The image detail cannot be null");
        mDataManager.resetImageDetail(imageDetail.getAssetId(), imageDetail.getId());
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
