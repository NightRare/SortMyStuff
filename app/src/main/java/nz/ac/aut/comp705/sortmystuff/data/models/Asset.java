package nz.ac.aut.comp705.sortmystuff.data.models;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import nz.ac.aut.comp705.sortmystuff.util.AppConstraints;

/**
 * Represents an asset instance in the app.
 * Any object that the user wish to record with the application is identified as an asset.
 * An asset can be contained by another asset or contains may other assets (e.g. Spaces, furnitures,
 * books, appliances, food etc.). An asset could also have detail information
 * (e.g., exp date, price, etc.).
 *
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

    private CategoryType category;

    // value of System.currentTimeMillis() when the asset is created
    @NonNull
    private long createTimestamp;

    // value of System.currentTimeMillis() when the asset is modified
    // changing the container and adding contents does not count as modifications
    // to the Asset
    @NonNull
    private long modifyTimestamp;

    private boolean isRoot;

    private boolean isRecycled;

    //endregion

    //region STATIC FACTORIES

    /**
     * Static factory to create an asset with the given name and has a category type as given.
     *
     * @param name
     * @param container
     * @param category
     * @return an Asset instance
     * @throws NullPointerException     if name or container is {@code null}
     * @throws IllegalArgumentException if name is empty or exceeds the length limit
     */
    public static Asset create(String name, Asset container, CategoryType category) {
        checkIllegalName(name);
        Preconditions.checkNotNull(container);

        String id = UUID.randomUUID().toString();
        Long ct = System.currentTimeMillis();
        Long mt = System.currentTimeMillis();
        List<Asset> contents = new ArrayList<>();

        Asset asset = new Asset(id, name, container.id, category, container, false, contents, ct, mt, false, null);
        if (container.contents == null) {
            container.contents = new ArrayList<>();
        }
        container.contents.add(asset);

        return asset;
    }

    /**
     * Static factory to create an asset with the given name and has a category type as "Miscellaneous".
     *
     * @param name      the name of the asset.
     * @param container the container of the asset.
     * @return an Asset instance
     * @throws NullPointerException     if name or container is {@code null}
     * @throws IllegalArgumentException if name is empty or exceeds the length limit
     */
    public static Asset createAsMisc(String name, Asset container) {
        return create(name, container, CategoryType.Miscellaneous);
    }

    /**
     * Static factory to create a Root Asset.
     *
     * @return the Root Asset instance
     */
    public static Asset createRoot() {
        String id = AppConstraints.ROOT_ASSET_ID;

        return new Asset(id, "Root", "", CategoryType.None, null, true, new ArrayList<Asset>(),
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
     * Sets the name of the asset.
     *
     * @param name the new name.
     * @throws NullPointerException     if name is {@code null}
     * @throws IllegalArgumentException if name is empty or exceeds the length limit
     */
    @Deprecated
    public void setName(@NonNull String name) {
        checkIllegalName(name);

        if (!isRoot()) {
            this.name = name;
            updateTimeStamp();
        }
    }

    /**
     * Moves this asset to a new container.
     *
     * @param containerObj the new container asset.
     * @return true if the asset is moved to the new container successfully.
     * @throws NullPointerException if containerObj is {@code null}
     */
    @Deprecated
    public boolean moveTo(@NonNull Asset containerObj) {
        Preconditions.checkNotNull(containerObj);

        // cannot move Root asset and cannot move to its children asset
        if (isRoot() || isParentOf(containerObj))
            return false;

        if (container.contents.remove(this)) {
            container = containerObj;
            containerId = containerObj.id;
            return container.contents.contains(this) || container.contents.add(this);
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

        if (isRoot())
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
                  CategoryType category, @Nullable Asset container, boolean isRoot, @NonNull List<Asset> contents,
                  @NonNull Long createdTimestamp, @NonNull Long modifiedTimestamp,
                  boolean isRecycled, @Nullable Bitmap photo) {
        this.id = id;
        this.name = name;
        this.containerId = containerId;
        this.category = category;
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
