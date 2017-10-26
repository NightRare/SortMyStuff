package nz.ac.aut.comp705.sortmystuff.data.models;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nz.ac.aut.comp705.sortmystuff.utils.AppConstraints;
import nz.ac.aut.comp705.sortmystuff.utils.BitmapHelper;

import static com.google.common.base.Preconditions.checkNotNull;
import static nz.ac.aut.comp705.sortmystuff.utils.AppConstraints.ROOT_ASSET_ID;

/**
 * Represents an asset instance in the app.
 * Any object that the user wish to record with the application is identified as an asset.
 * An asset can be contained by another asset or contains may other assets (e.g. Spaces, furnitures,
 * books, appliances, food etc.). An asset could also have detail information
 * (e.g., exp date, price, etc.).
 *
 * @author Yuan
 */
@IgnoreExtraProperties
public final class FAsset implements IAsset {

    //region FIELD NAMES

    public static final String ASSET_ID = "id";
    public static final String ASSET_NAME = "name";
    public static final String ASSET_CONTAINERID = "containerId";
    public static final String ASSET_RECYCLED = "recycled";
    public static final String ASSET_CONTENTIDS = "contentIds";
    public static final String ASSET_CATEGORYTYPE = "categoryType";
    public static final String ASSET_CREATETIMESTAMP = "createTimestamp";
    public static final String ASSET_MODIFYTIMESTAMP = "modifyTimestamp";
    public static final String ASSET_DETAILIDS = "detailIds";
    public static final String ASSET_THUMBNAIL = "thumbnail";

    //endregion

    //region DATA FIELDS

    private String id;

    private String name;

    private String containerId;

    private List<String> contentIds;

    private CategoryType categoryType;

    /**
     * Value of System.currentTimeMillis() when the asset is created.
     */
    private long createTimestamp;

    /**
     * Value of System.currentTimeMillis() when the asset is modified
     * changing the container and adding contents does not count as modifications
     * to the Asset.
     */
    private long modifyTimestamp;

    private boolean recycled;

    private List<String> detailIds;

    private Bitmap thumbnail;

    //endregion

    //region STATIC FACTORIES

    public FAsset() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    /**
     * Static factory to create an asset with the given name and has a categoryType type as given.
     *
     * @param name        the name of the Asset
     * @param containerId the id of the asset's container
     * @param category    the CategoryType of the asset (name of the Category)
     * @return an Asset instance
     * @throws NullPointerException     if name or container is {@code null}
     * @throws IllegalArgumentException if name is empty or exceeds the length limit
     */
    @Exclude
    public static FAsset create(String name, String containerId, CategoryType category) {
        checkIllegalName(name);
        checkNotNull(containerId);

        String id = UUID.randomUUID().toString();
        Long createTimestamp = System.currentTimeMillis();
        Long modifyTimestamp = System.currentTimeMillis();

        FAsset asset = new FAsset(id, name, containerId, new ArrayList<>(), category,
                createTimestamp, modifyTimestamp, false, new ArrayList<>(), null, true);

        return asset;
    }

    /**
     * Static factory to create an asset with the given name and has a categoryType type as "Miscellaneous".
     *
     * @param name        the name of the asset
     * @param containerId the id of the asset's container
     * @return an Asset instance
     * @throws NullPointerException     if name or container is {@code null}
     * @throws IllegalArgumentException if name is empty or exceeds the length limit
     */
    @Exclude
    public static FAsset createAsMisc(String name, String containerId) {
        return create(name, containerId, CategoryType.Miscellaneous);
    }

    /**
     * Static factory to create a Root Asset.
     *
     * @return the Root Asset instance
     */
    @Exclude
    public static FAsset createRoot() {
        String id = ROOT_ASSET_ID;

        return new FAsset(id, "Assets", "", new ArrayList<>(), CategoryType.None,
                System.currentTimeMillis(), System.currentTimeMillis(), false, new ArrayList<>(), null, true);
    }

    //endregion

    //region TRANSFORMERS

    @Exclude
    public static FAsset fromMap(Map<String, Object> members) {
        String id = members.get(ASSET_ID).toString();
        String name = members.get(ASSET_NAME).toString();
        String containerId = members.get(ASSET_CONTAINERID).toString();
        List<String> contentIds = (List) members.get(ASSET_CONTENTIDS);
        if (contentIds == null) contentIds = new ArrayList<>();
        CategoryType categoryType = CategoryType.valueOf(members.get(ASSET_CATEGORYTYPE).toString());
        Long createTimestamp = (Long) members.get(ASSET_CREATETIMESTAMP);
        Long modifyTimestamp = (Long) members.get(ASSET_MODIFYTIMESTAMP);
        Boolean recycled = (Boolean) members.get(ASSET_RECYCLED);
        List<String> detailIds = (List) members.get(ASSET_DETAILIDS);
        if (detailIds == null) detailIds = new ArrayList<>();

        String encodedThumbnail = (String) members.get(ASSET_THUMBNAIL);
        if (encodedThumbnail != null && !encodedThumbnail.isEmpty()) {
            Bitmap thumbnail = BitmapHelper.toBitmap(encodedThumbnail);
            return new FAsset(id, name, containerId, contentIds, categoryType, createTimestamp,
                    modifyTimestamp, recycled, detailIds, thumbnail, false);
        } else {
            return new FAsset(id, name, containerId, contentIds, categoryType, createTimestamp,
                    modifyTimestamp, recycled, detailIds, null, true);
        }
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(ASSET_ID, id);
        map.put(ASSET_NAME, name);
        map.put(ASSET_CONTAINERID, containerId);
        map.put(ASSET_CONTENTIDS, contentIds);
        map.put(ASSET_CATEGORYTYPE, categoryType.toString());
        map.put(ASSET_CREATETIMESTAMP, createTimestamp);
        map.put(ASSET_MODIFYTIMESTAMP, modifyTimestamp);
        map.put(ASSET_RECYCLED, recycled);
        map.put(ASSET_DETAILIDS, detailIds);

        // if using default thumbnail then don't put it into the map
        String thumbnailEncodedString = usingDefaultThumbnail ? null : BitmapHelper.toString(thumbnail);
        map.put(ASSET_THUMBNAIL, thumbnailEncodedString);

        return map;
    }

    @Exclude
    public void overwrittenBy(FAsset source) {
        checkNotNull(source, "The source asset cannot be null.");
        id = source.getId();
        name = source.getName();
        containerId = source.getContainerId();
        contentIds = source.getContentIds();
        categoryType = source.getCategoryType();
        createTimestamp = source.getCreateTimestamp();
        modifyTimestamp = source.getModifyTimestamp();
        recycled = source.isRecycled();
        detailIds = source.getDetailIds();
        thumbnail = source.getThumbnail();
    }

    //endregion

    //region ACCESSORS

    /**
     * Gets the unique id.
     *
     * @return the id.
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Gets the name.
     *
     * @return the name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the unique id of the asset's container.
     *
     * @return the container's id
     */
    @Override
    public String getContainerId() {
        return containerId;
    }

    @Override
    public List<String> getContentIds() {
        return new ArrayList<>(contentIds);
    }

    /**
     * Gets the name of the category of the asset as a CategoryType instance.
     *
     * @return the name of the category this asset was classified as
     */
    @Override
    public CategoryType getCategoryType() {
        return categoryType;
    }

    /**
     * Gets the timestamp of creating as a Long object storing the milliseconds
     *
     * @return the creating timestamp
     */
    @Override
    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    /**
     * Gets the timestamp of latest modifying as a Long object storing the milliseconds
     *
     * @return the latest modifying timestamp
     */
    @Override
    public Long getModifyTimestamp() {
        return modifyTimestamp;
    }

    /**
     * Gets the status whether the asset has been recycled.
     *
     * @return true if is recycled
     */
    @Override
    public boolean isRecycled() {
        return recycled;
    }

    /**
     * Checks whether the asset is a root asset.
     *
     * @return true if the asset is a root asset
     */
    @Override
    @Exclude
    public boolean isRoot() {
        return id.equals(ROOT_ASSET_ID);
    }

    @Override
    @Exclude
    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public List<String> getDetailIds() {
        return new ArrayList<>(detailIds);
    }

    //endregion

    //region MODIFIERS

    @Exclude
    public void setId(String id) {
        if (this.id == null || this.id.isEmpty())
            this.id = checkNotNull(id);
    }

    /**
     * Sets the name of the asset.
     *
     * @param name the new name.
     * @throws NullPointerException     if name is {@code null}
     * @throws IllegalArgumentException if name is empty or exceeds the length limit
     */
    @Exclude
    public void setName(String name) {
        checkIllegalName(name);

        if (!isRoot()) {
            this.name = name;
            updateTimeStamp();
        }
    }

    @Exclude
    public void setRecycled(boolean recycled) {
        if (!isRoot())
            this.recycled = recycled;
    }

    /**
     * Sets the modify timestamp to the given long. If the given timestamp is smaller than
     * the original one or this asset is the root asset, then the change won't be made.
     *
     * @param modifyTimestamp
     */
    @Exclude
    public void setModifyTimestamp(long modifyTimestamp) {
        if(isRoot() || modifyTimestamp < this.modifyTimestamp) return;
        this.modifyTimestamp = modifyTimestamp;
    }

    @Exclude
    public void setThumbnail(Bitmap photo, boolean usingDefaultThumbnail) {
        this.usingDefaultThumbnail = usingDefaultThumbnail;
        this.thumbnail = checkNotNull(photo);
    }

    @Exclude
    public void addContentId(String id) {
        contentIds.add(checkNotNull(id));
    }

    @Exclude
    public void removeContentId(String id) {
        contentIds.remove(checkNotNull(id));
    }

    @Exclude
    public void addDetailId(String id) {
        detailIds.add(checkNotNull(id));
    }

    @Exclude
    public void removeDetailId(String id) {
        detailIds.remove(checkNotNull(id));
    }

    @Exclude
    public boolean move(FAsset from, FAsset to) {
        checkNotNull(from);
        checkNotNull(to);

        if (isRoot()) return false;

        if (from.contentIds.remove(this.id)) {
            containerId = to.id;
            // if the new container already contains this, then no need for adding
            return to.contentIds.contains(this.id) || to.contentIds.add(this.id);
        }
        return false;
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
    @Exclude
    public boolean equals(Object o) {
        if (o instanceof FAsset) {
            FAsset a = (FAsset) o;
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
    @Exclude
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Returns the name of the asset.
     *
     * @return the name of the asset
     */
    @Override
    @Exclude
    public String toString() {
        return name;
    }

    //endregion

    //region PRIVATE STUFF

    private FAsset(String id, String name, String containerId, List<String> contentIds,
                   CategoryType categoryType, Long createdTimestamp,
                   Long modifiedTimestamp, boolean recycled, List<String> detailIds, Bitmap thumbnail,
                   boolean usingDefaultThumbnail) {
        this.id = id;
        this.name = name;
        this.containerId = containerId;
        this.contentIds = contentIds;
        this.categoryType = categoryType;
        this.createTimestamp = createdTimestamp;
        this.modifyTimestamp = modifiedTimestamp;
        this.recycled = recycled;
        this.detailIds = detailIds;
        this.thumbnail = thumbnail;
        this.usingDefaultThumbnail = usingDefaultThumbnail;
    }

    /**
     * Checks if the given name is illegal.
     *
     * @param name
     */
    @Exclude
    private static void checkIllegalName(String name) {
        checkNotNull(name);
        if (name.isEmpty())
            throw new IllegalArgumentException("The name cannot be empty");
        if (name.length() > AppConstraints.ASSET_NAME_CAP)
            throw new IllegalArgumentException("The length of the name should be shorter than "
                    + AppConstraints.ASSET_NAME_CAP);
    }


    /**
     * Updates the {@link #modifyTimestamp } to {@link System#currentTimeMillis()}.
     */
    @Exclude
    private void updateTimeStamp() {
        if (isRoot()) return;
        modifyTimestamp = System.currentTimeMillis();
    }

    @Exclude
    private boolean usingDefaultThumbnail;

    //endregion
}
