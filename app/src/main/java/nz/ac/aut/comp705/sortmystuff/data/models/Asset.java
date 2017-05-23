package nz.ac.aut.comp705.sortmystuff.data.models;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import nz.ac.aut.comp705.sortmystuff.util.AppConstraints;

/**
 * @author Yuan
 */

public final class Asset {

    //region DATA FIELDS

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

    //endregion

    //region STATIC FACTORIES

    /**
     * @param name
     * @param container
     */
    public static Asset create(String name, Asset container) {
        checkIllegalName(name);

        String id = UUID.randomUUID().toString();
        Long ct = System.currentTimeMillis();
        Long mt = System.currentTimeMillis();
        List<Asset> contents = new ArrayList<>();

        Asset asset = new Asset(id, name, container.id, container, false, contents, ct, mt, false, null);
        if (container != null) {
            if (container.contents == null) {
                container.contents = new ArrayList<>();
            }
            container.contents.add(asset);
        }

        return asset;
    }


    public static Asset createRoot() {
        String id = AppConstraints.ROOT_ASSET_ID;

        return new Asset(id, "Root", "", null, true, new ArrayList<Asset>(),
                System.currentTimeMillis(),
                System.currentTimeMillis(), false, null);
    }

    //endregion

    //region ACCESSORS

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContainerId() {
        return containerId;
    }

    public boolean isRecycled() {
        return isRecycled;
    }

    public boolean isRoot() {
        return isRoot;
    }

    /**
     * IMPORTANT: FOR DATA LAYER COMPONENTS USE ONLY.
     * <p>
     * DO NOT CALL OUTSIDE {@link nz.ac.aut.comp705.sortmystuff.data} PACKAGE
     */
    @Deprecated
    @Nullable
    public Asset getContainer() {
        return container;
    }

    /**
     * IMPORTANT: FOR DATA LAYER COMPONENTS USE ONLY.
     * <p>
     * DO NOT CALL OUTSIDE {@link nz.ac.aut.comp705.sortmystuff.data} PACKAGE
     */
    @Deprecated
    public List<Asset> getContents() {
        return new ArrayList<>(contents);
    }

    @NonNull
    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    @NonNull
    public Long getModifyTimestamp() {
        return modifyTimestamp;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    //endregion

    //region MODIFIERS

    /**
     * IMPORTANT: FOR DATA LAYER COMPONENTS USE ONLY.
     * <p>
     * DO NOT CALL OUTSIDE {@link nz.ac.aut.comp705.sortmystuff.data} PACKAGE
     */
    @Deprecated
    public void setName(@NonNull String name) {
        checkIllegalName(name);
        if (isRoot())
            return;

        this.name = name;
        updateTimeStamp();
    }

    /**
     * IMPORTANT: FOR DATA LAYER COMPONENTS USE ONLY.
     * <p>
     * DO NOT CALL OUTSIDE {@link nz.ac.aut.comp705.sortmystuff.data} PACKAGE
     */
    @Deprecated
    public boolean moveTo(@NonNull Asset containerObj) {
        Preconditions.checkNotNull(containerObj);
        // cannot move Root asset
        if (isRoot())
            return false;

        // cannot move to its children asset
        if (isParentOf(containerObj)) {
            return false;
        }

        if (container.contents.remove(this)) {
            container = containerObj;
            containerId = containerObj.id;
            if (container.contents.contains(this))
                return true;
            else
                return container.contents.add(this);
        }
        return false;
    }

    /**
     * IMPORTANT: FOR DATA LAYER COMPONENTS USE ONLY.
     * <p>
     * DO NOT CALL OUTSIDE {@link nz.ac.aut.comp705.sortmystuff.data} PACKAGE
     */
    @Deprecated
    public boolean attachToTree(@Nullable Asset containerObj) {
        if (contents == null)
            contents = new ArrayList<>();

        if(isRoot())
            return true;

        if (!containerObj.getId().equals(containerId))
            return false;

        container = containerObj;
        if (container.contents == null)
            container.contents = new ArrayList<>();
        if (container.contents.contains(this))
            return true;
        return container.contents.add(this);
    }

    /**
     * IMPORTANT: FOR DATA LAYER COMPONENTS USE ONLY.
     * <p>
     * DO NOT CALL OUTSIDE {@link nz.ac.aut.comp705.sortmystuff.data} PACKAGE
     */
    @Deprecated
    public void updateTimeStamp() {
        if (isRoot()) return;
        modifyTimestamp = System.currentTimeMillis();
    }

    /**
     * IMPORTANT: FOR DATA LAYER COMPONENTS USE ONLY.
     * <p>
     * DO NOT CALL OUTSIDE {@link nz.ac.aut.comp705.sortmystuff.data} PACKAGE
     */
    @Deprecated
    public void recycle() {
        if (isRoot()) return;
        isRecycled = true;
    }

    /**
     * IMPORTANT: FOR DATA LAYER COMPONENTS USE ONLY.
     * <p>
     * DO NOT CALL OUTSIDE {@link nz.ac.aut.comp705.sortmystuff.data} PACKAGE
     */
    @Deprecated
    public void restore() {
        if (isRoot()) return;
        isRecycled = false;
    }

    /**
     * IMPORTANT: FOR DATA LAYER COMPONENTS USE ONLY.
     * <p>
     * DO NOT CALL OUTSIDE {@link nz.ac.aut.comp705.sortmystuff.data} PACKAGE
     */
    @Deprecated
    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    //endregion

    //region OBJECT METHODS OVERRIDING

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

    //endregion

    //region PRIVATE STUFF

    @Nullable
    // the container asset of the asset
    private transient Asset container;

    @NonNull
    // list of assets this certain asset contains
    // just for convenience
    private transient List<Asset> contents;

    @Nullable
    private transient Bitmap photo;

    private boolean isParentOf(Asset asset) {
        if (asset.container == null || asset.id.equals(id)) {
            return false;
        }
        if (asset.containerId.equals(id)) {
            return true;
        }
        return isParentOf(asset.container);
    }

    private Asset(@NonNull String id, @NonNull String name, @NonNull String containerId,
                  @Nullable Asset container, boolean isRoot, @NonNull List<Asset> contents,
                  @NonNull Long createdTimestamp, @NonNull Long modifiedTimestamp,
                  boolean isRecycled, @Nullable Bitmap photo) {
        this.id = id;
        this.name = name;
        this.containerId = containerId;
        this.container = container;
        this.isRoot = isRoot;
        this.contents = contents;
        this.createTimestamp = createdTimestamp;
        this.modifyTimestamp = modifiedTimestamp;
        this.isRecycled = isRecycled;
        this.photo = photo;
    }


    private static void checkIllegalName(String name) {
        Preconditions.checkNotNull(name);
        if (name.isEmpty())
            throw new IllegalArgumentException("cannot be empty");
        if (name.length() > AppConstraints.ASSET_NAME_CAP)
            throw new IllegalArgumentException("string length exceeds cap");
    }

    //endregion
}
