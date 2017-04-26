package nz.ac.aut.comp705.sortmystuff.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import nz.ac.aut.comp705.sortmystuff.util.AppConstraints;

/**
 * An Asset class which stores formatted information
 * relevant to an asset
 *
 * @author Yuan
 */

public final class Asset {

    //********************************************
    // DATA FIELDS
    // *******************************************

    // the identifier of the asset
    @NonNull
    private final String id;

    @NonNull
    private String name;

    @NonNull
    private String containerId;

    // value of System.currentTimeMillis() when the asset is created
    @NonNull
    private Long createTimestamp;

    // value of System.currentTimeMillis() when the asset is modified
    // changing the container and adding contents does not count as modifications
    // to the Asset
    @NonNull
    private Long modifyTimestamp;

    private boolean isRoot;

    private boolean isRecycled;

    //********************************************
    // STATIC FACTORIES
    //********************************************

    /**
     * @param name
     * @param container
     */
    public static Asset create(String name, Asset container) {
        checkIllegalName(name);

        String id = UUID.randomUUID().toString();
        List<Detail> details = new LinkedList<>();
        Long ct = System.currentTimeMillis();
        Long mt = System.currentTimeMillis();
        List<Asset> content = new LinkedList<>();

        Asset asset = new Asset(id, name, container.id, container, false, content, details, ct, mt, false);
        if (container != null) {
            if(container.contents == null) {
                container.contents = new LinkedList<>();
            }
            container.contents.add(asset);
        }

        return asset;
    }


    public static Asset createRoot() {
        return new Asset(UUID.randomUUID().toString(),
                "Root", "", null, true, new LinkedList<Asset>(),
                new LinkedList<Detail>(), System.currentTimeMillis(),
                System.currentTimeMillis(), false);
    }


    //********************************************
    // ACCESSORS
    //********************************************

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Asset getContainer() {
        return container;
    }

    public String getContainerId() {
        return containerId;
    }

    public boolean isRecycled() {
        return isRecycled;
    }

    public boolean isRoot() { return isRoot; }

    List<Asset> getContents() {
        if(contents != null)
            return new LinkedList<>(contents);
        return null;
    }

    @NonNull
    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    @NonNull
    public Long getModifyTimestamp() {
        return modifyTimestamp;
    }

    //********************************************
    // MUTATORS
    //********************************************

    void setName(@NonNull String name) {
        checkIllegalName(name);
        this.name = name;
        updateTimeStamp();
    }

    boolean moveTo(@NonNull Asset containerObj) {
        Preconditions.checkNotNull(containerObj);
        // cannot move Root asset
        if (isRoot)
            return false;

        // cannot move to its children asset
        if  (isParentOf(containerObj)) {
            return false;
        }

        if (container.contents.remove(this)) {
            container = containerObj;
            containerId = containerObj.id;
            if(container.contents.contains(this))
                return true;
            else
                return container.contents.add(this);
        }
        return false;
    }

    boolean attachToTree(@NonNull Asset containerObj) {
        Preconditions.checkNotNull(containerObj);

        if(contents == null) {
            contents = new LinkedList<>();
        }
        if(isRoot()) {
            return true;
        }
        if(!containerObj.getId().equals(containerId)) {
            return false;
        }
        container = containerObj;
        if(container.contents == null)
            container.contents = new LinkedList<>();
        if(container.contents.contains(this))
            return true;
        return container.contents.add(this);
    }

    // TODO delete removeDetail
    @Deprecated
    boolean addDetail(Detail detail) {
        Preconditions.checkNotNull(detail);
        if (!detail.getAssetId().equals(id))
            return false;

        if (details == null)
            details = new LinkedList<>();

        if (details.contains(detail))
            return false;

        if (details.add(detail)) {
            updateTimeStamp();
            return true;
        }
        return false;
    }

    // TODO delete removeDetail
    @Deprecated
    boolean removeDetail(Detail detail) {
        Preconditions.checkNotNull(detail);
        if (details.remove(detail)) {
            updateTimeStamp();
            return true;
        }
        return false;
    }

    void updateTimeStamp() {
        modifyTimestamp = System.currentTimeMillis();
    }

    void recycle() {
        isRecycled = true;
    }

    void restore() {
        isRecycled = false;
    }

    //********************************************
    // OBJECT METHODS OVERRIDING
    //********************************************

    @Override
    public boolean equals(Object o) {
        if (o instanceof Asset) {
            Asset a = (Asset) o;
            if (a.id.equals(id))
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }


    //********************************************
    // PRIVATE
    //********************************************

    @Nullable
    // the container asset of the asset
    private transient Asset container;

    @NonNull
    // list of assets this certain asset contains
    // just for convenience
    private transient List<Asset> contents;

    @NonNull
    // list of this asset's extended details
    // for convenience
    private transient List<Detail> details;

    private boolean isParentOf(Asset asset) {
        if(asset.container == null || asset.id.equals(id)) {
            return false;
        }
        if(asset.containerId.equals(id)) {
            return true;
        }
        return isParentOf(asset.container);
    }

    private Asset(@NonNull String id, @NonNull String name, @NonNull String containerId, Asset container,
                  boolean isRoot, @NonNull List<Asset> contents, @NonNull List<Detail> details,
                  @NonNull Long createdTimestamp, @NonNull Long modifiedTimestamp,
                  boolean isRecycled) {
        this.id = id;
        this.name = name;
        this.containerId = containerId;
        this.container = container;
        this.isRoot = isRoot;
        this.contents = contents;
        this.details = details;
        this.createTimestamp = createdTimestamp;
        this.modifyTimestamp = modifiedTimestamp;
        this.isRecycled = isRecycled;
    }


    private static void checkIllegalName(String name) {
        Preconditions.checkNotNull(name);
        if (name.isEmpty())
            throw new IllegalArgumentException("cannot be empty");
        if (name.length() > AppConstraints.ASSET_NAME_CAP)
            throw new IllegalArgumentException("string length exceeds cap");
    }
}
