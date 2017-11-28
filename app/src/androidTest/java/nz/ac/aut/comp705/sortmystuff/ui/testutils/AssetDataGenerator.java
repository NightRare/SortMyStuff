package nz.ac.aut.comp705.sortmystuff.ui.testutils;

import java.util.LinkedHashMap;
import java.util.Map;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import rx.Observable;
import rx.Subscription;

import static nz.ac.aut.comp705.sortmystuff.utils.AppStrings.ROOT_ASSET_ID;


/**
 * Some experimental class.
 */

public class AssetDataGenerator {

    public AssetDataGenerator(IDataManager dataManager) {
        mDataManager = dataManager;
        mCurrentContainerId = ROOT_ASSET_ID;
        mData = new LinkedHashMap<>();
    }

    public AssetDataGenerator toRoot() {
        mCurrentContainerId = ROOT_ASSET_ID;
        return this;
    }

    public AssetDataGenerator toParent() {
        if(mCurrentContainerId.equals(ROOT_ASSET_ID)) return this;
        Subscription subscription = mDataManager.getParentAssets(mCurrentContainerId, false)
                .subscribe(assets -> mCurrentContainerId = assets.get(1).getId());
        return waitFor(subscription::isUnsubscribed, 2000, () -> this);
    }

    public AssetDataGenerator toChildren(String assetName) {
        Subscription subscription = mDataManager
                .getContentAssets(mCurrentContainerId)
                .flatMap(Observable::from)
                .filter(asset -> asset.getName().equals(assetName))
                .toList()
                .subscribe(
                        assets -> {
                            if(assets.isEmpty()) return;
                            if(assets.size() > 1) throw new IllegalStateException("Duplicate names found");
                            mCurrentContainerId = assets.get(0).getId();
                        }
                );
        return waitFor(subscription::isUnsubscribed, 2000, () -> this);
    }

    public AssetDataGenerator addAsset(String name, CategoryType category) {
        if(mData.containsKey(name))
            throw new IllegalArgumentException("Cannot have duplicate names");
        mData.put(name, category);
        mDataManager.createAsset(name, mCurrentContainerId, category);
        return this;
    }

    public AssetDataGenerator addAssets(Map<String, CategoryType> assets) {
        for(String name : assets.keySet()) {
            if(mData.containsKey(name))
                throw new IllegalArgumentException("Cannot have duplicate names");
        }
        mData.putAll(assets);
        for(Map.Entry<String, CategoryType> a : assets.entrySet()) {
            mDataManager.createAsset(a.getKey(), mCurrentContainerId, a.getValue());
        }
        return this;
    }

    private <T> T waitFor(ConditionToStop conditionToStop, long timeoutMillis, AfterWaiting<T> afterWaiting) {
        long timeout = System.currentTimeMillis() + timeoutMillis;
        while(!conditionToStop.check()) {
            if(System.currentTimeMillis() > timeout)
                throw new IllegalStateException("Time out");

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return afterWaiting.call();
    }

    private interface AfterWaiting<T> {
        T call();
    }

    private interface ConditionToStop {
        boolean check();
    }

    private IDataManager mDataManager;
    private String mCurrentContainerId;

    private Map<String, CategoryType> mData;

}
