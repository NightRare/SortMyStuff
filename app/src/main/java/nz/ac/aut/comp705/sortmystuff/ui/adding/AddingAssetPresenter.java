package nz.ac.aut.comp705.sortmystuff.ui.adding;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.local.LocalResourceLoader;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import static dagger.internal.Preconditions.checkNotNull;

public class AddingAssetPresenter implements IAddingAssetPresenter {

    public AddingAssetPresenter(
            IDataManager dataManager,
            IAddingAssetView view,
            ISchedulerProvider schedulerProvider,
            LocalResourceLoader localResourceLoader,
            String containerId) {
        mDataManager = checkNotNull(dataManager);
        mView = checkNotNull(view);
        mSchedulerProvider = checkNotNull(schedulerProvider);
        mResLoader = checkNotNull(localResourceLoader);
        mContainerId = checkNotNull(containerId);

        mSubscriptions = new CompositeSubscription();
        mView.setPresenter(this);
    }

    @Override
    public void addingAsset(@Nullable Bitmap photo) {
        boolean defaultPhoto = photo == null;
        Bitmap image = defaultPhoto ? mResLoader.getDefaultPhoto() : photo;
        mView.showAssetPhoto(image);
        mView.showAssetName("");
        mView.showSpinner();

        mSubscriptions.clear();
        mSubscriptions.add(
                mDataManager.getDetailsFromCategory(CategoryType.Miscellaneous)
                        .subscribeOn(mSchedulerProvider.io())
                        .observeOn(mSchedulerProvider.ui())
                        .subscribe(details -> mView.showDetails(details)));

        updateAssetName(photo);
    }

    @Override
    public void updateAssetName(@Nullable Bitmap photo) {
        Subscription subscription;

        if (photo != null) {
            subscription = mDataManager.getNewAssetName(photo)
                    .subscribeOn(mSchedulerProvider.newThread())
                    .observeOn(mSchedulerProvider.ui())
                    .subscribe(
                            //onNext
                            newName -> {
                                if (newName == null) {
                                    mView.showMessage("Name detection failed.");
                                } else {
                                    mView.showAssetName(newName);
                                }
                            },
                            //onError
                            throwable -> {
                            } //do nothing
                    );
        } else {
            subscription = mDataManager.getNewAssetName()
                    .subscribeOn(mSchedulerProvider.newThread())
                    .observeOn(mSchedulerProvider.ui())
                    .subscribe(
                            //onNext
                            newName -> mView.showAssetName(newName),
                            //onError
                            throwable -> {
                            } //do nothing
                    );
        }

        if (subscription != null)
            mSubscriptions.add(subscription);
    }

    @Override
    public void selectCategory(CategoryType category) {
        mSubscriptions.add(
                mDataManager.getDetailsFromCategory(category)
                        .subscribeOn(mSchedulerProvider.io())
                        .observeOn(mSchedulerProvider.ui())
                        .subscribe(details -> mView.showDetails(details)));
    }

    @Override
    public void createAsset(
            String name,
            CategoryType category,
            Bitmap photo,
            List<IDetail> details) {
        mDataManager.createAsset(name, mContainerId, category, photo, details);
        mView.goBack();
    }

    @Override
    public void resetPhoto() {
        mView.showAssetPhoto(mResLoader.getDefaultPhoto());
    }

    @Override
    public void start() {
        mView.turnToCamera();
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    //region PRIVATE STUFF

    private IAddingAssetView mView;
    private IDataManager mDataManager;
    private LocalResourceLoader mResLoader;
    private ISchedulerProvider mSchedulerProvider;
    private CompositeSubscription mSubscriptions;
    private String mContainerId;

    //endregion
}
