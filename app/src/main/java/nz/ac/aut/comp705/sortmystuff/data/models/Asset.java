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

    @NonNull
    private final String id;

    @NonNull
    private String name;

    @NonNull
    private String containerId;

    private CategoryType categoryType;

    /**
     * Value of System.currentTimeMillis() when the asset is created.
     */
    @NonNull
    private long createTimestamp;

    /**
     * Value of System.currentTimeMillis() when the asset is modified
     * changing the container and adding contents does not count as modifications
     * to the Asset.
     */
    @NonNull
    private long modifyTimestamp;

    private boolean isRoot;

    private boolean isRecycled;

    //endregion

    //region STATIC FACTORIES

    /**
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
     * Static factory to create an asset with the given name and has a categoryType type as given.
     *
     * @param name      the name of the Asset
     * @param container the container of the asset
     * @param category  the CategoryType of the asset (name of the Category)
     * @return an Asset instance
     * @throws NullPointerException     if name or container is {@code null}
     * @throws IllegalArgumentException if name is empty or exceeds the length limit
     */
    @Deprecated
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
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
     * Static factory to create an asset with the given name and has a categoryType type as "Miscellaneous".
     *
     * @param name      the name of the asset.
     * @param container the container of the asset.
     * @return an Asset instance
     * @throws NullPointerException     if name or container is {@code null}
     * @throws IllegalArgumentException if name is empty or exceeds the length limit
     */
    @Deprecated
    public static Asset createAsMisc(String name, Asset container) {
        return create(name, container, CategoryType.Miscellaneous);
    }

    /**
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
     * Static factory to create a Root Asset.
     *
     * @return the Root Asset instance
     */
    @Deprecated
    public static Asset createRoot() {
        String id = AppConstraints.ROOT_ASSET_ID;

        return new Asset(id, "Root", "", CategoryType.None, null, true, new ArrayList<Asset>(),
                System.currentTimeMillis(),
                System.currentTimeMillis(), false, null);
    }

    //endregion

    //region ACCESSORS

    /**
     * Gets the unique id.
     *
     * @return the id.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the name.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the unique id of the asset's container.
     *
     * @return the container's id
     */
    public String getContainerId() {
        return containerId;
    }

    /**
     * Gets the status whether the asset has been recycled.
     *
     * @return true if is recycled
     */
    public boolean isRecycled() {
        return isRecycled;
    }

    /**
     * Checks whether the asset is a root asset.
     *
     * @return true if the asset is a root asset
     */
    public boolean isRoot() {
        return isRoot;
    }

    /**
     * Gets the timestamp of creating as a Long object storing the milliseconds
     *
     * @return the creating timestamp
     */
    @NonNull
    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    /**
     * Gets the timestamp of latest modifying as a Long object storing the milliseconds
     *
     * @return the latest modifying timestamp
     */
    @NonNull
    public Long getModifyTimestamp() {
        return modifyTimestamp;
    }

    /**
     * Gets the photo of the asset.
     *
     * @return the photo
     */
    public Bitmap getPhoto() {
        return photo;
    }

    /**
     * Gets the name of the category of the asset as a CategoryType instance.
     *
     * @return the name of the category this asset was classified as
     */
    public CategoryType getCategoryType() {
        return categoryType;
    }

    /**
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
     * Gets the container asset.
     *
     * @return the container asset
     */
    @Deprecated
    @Nullable
    public Asset getContainer() {
        return container;
    }

    /**
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
     * Gets the contents assets of the assets.
     *
     * @return the list of contents assets
     */
    @Deprecated
    public List<Asset> getContents() {
        return new ArrayList<>(contents);
    }


    //endregion

    //region MODIFIERS

    /**
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
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
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
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
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
     * Attaches the asset to its container to form a tree structure of assets. The tree structure
     * is not recorded in data storage, therefore in order to be used as an Asset instance,
     * they should be "attached" to its containers and contents after the assets have been retrieved
     *
     * @param containerObj the container asset
     * @true if attach successfully
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
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
     * Updates the {@link #modifyTimestamp } to {@link System#currentTimeMillis()}.
     */
    @Deprecated
    public void updateTimeStamp() {
        if (isRoot()) return;
        modifyTimestamp = System.currentTimeMillis();
    }

    /**
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
     * Marks the Asset as being recycled.
     */
    @Deprecated
    public void recycle() {
        if (!isRoot()) {
            isRecycled = true;
        }
    }

    /**
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
     * Marks the asset as not being recycled.
     */
    @Deprecated
    public void restore() {
        if (!isRoot()) {
            isRecycled = false;
        }
    }

    /**
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
     * Sets the photo.
     */
    @Deprecated
    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    //endregion

    //region OBJECT METHODS OVERRIDING

    /**
     * Compares the id of the asset.
     *
     * @param o the object to be compared with
     * @return true if the ids are equal
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Asset) {
            Asset a = (Asset) o;
            if (a.id.equals(id))
                return true;
        }
        return false;
    }

    /**
     * Hashes by the asset's id.
     *
     * @return the hashcode of the id.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Returns the name of the asset.
     *
     * @return the name of the asset
     */
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

    private Asset(@NonNull String id, @NonNull String name, @NonNull String containerId,
                  CategoryType categoryType, @Nullable Asset container, boolean isRoot, @NonNull List<Asset> contents,
                  @NonNull Long createdTimestamp, @NonNull Long modifiedTimestamp,
                  boolean isRecycled, @Nullable Bitmap photo) {
        this.id = id;
        this.name = name;
        this.containerId = containerId;
        this.categoryType = categoryType;
        this.container = container;
        this.isRoot = isRoot;
        this.contents = contents;
        this.createTimestamp = createdTimestamp;
        this.modifyTimestamp = modifiedTimestamp;
        this.isRecycled = isRecycled;
        this.photo = photo;
    }

    /**
     * Checks if this asset is the parent of the given asset.
     *
     * @param asset the asset to be checked
     * @return true if this asset is the parent of the given asset
     */
    private boolean isParentOf(Asset asset) {
        if (asset.container == null || asset.id.equals(id)) {
            return false;
        }
        if (asset.containerId.equals(id)) {
            return true;
        }
        return isParentOf(asset.container);
    }

    /**
     * Checks if the given name is illegal.
     *
     * @param name
     */
    private static void checkIllegalName(String name) {
        Preconditions.checkNotNull(name);
        if (name.isEmpty())
            throw new IllegalArgumentException("cannot be empty");
        if (name.length() > AppConstraints.ASSET_NAME_CAP)
            throw new IllegalArgumentException("string length exceeds cap");
    }

    //endregion
}
