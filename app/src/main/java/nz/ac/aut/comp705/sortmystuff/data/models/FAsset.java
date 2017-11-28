package nz.ac.aut.comp705.sortmystuff.data.models;

import android.graphics.Bitmap;
import android.util.ArrayMap;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import nz.ac.aut.comp705.sortmystuff.utils.AppConfigs;
import nz.ac.aut.comp705.sortmystuff.utils.BitmapHelper;

import static com.google.common.base.Preconditions.checkNotNull;
import static nz.ac.aut.comp705.sortmystuff.utils.AppStrings.ROOT_ASSET_ID;

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

    @Exclude
    private static final Map<String, Class> memberClasses;

    static {
        Map<String, Class> aMap = new HashMap<>();
        aMap.put(ASSET_ID, String.class);
        aMap.put(ASSET_NAME, String.class);
        aMap.put(ASSET_CONTAINERID, String.class);
        aMap.put(ASSET_RECYCLED, Boolean.class);
        aMap.put(ASSET_CONTENTIDS, List.class);
        aMap.put(ASSET_CATEGORYTYPE, CategoryType.class);
        aMap.put(ASSET_CREATETIMESTAMP, Long.class);
        aMap.put(ASSET_MODIFYTIMESTAMP, Long.class);
        aMap.put(ASSET_DETAILIDS, List.class);
        // Bitmap is transferred into a String to store in the Database
        aMap.put(ASSET_THUMBNAIL, String.class);
        memberClasses = Collections.unmodifiableMap(aMap);
    }

    //endregion

    //region DATA FIELDS

    private final String id;

    private volatile String name;

    private volatile String containerId;

    private final List<String> contentIds;

    private volatile CategoryType categoryType;

    /**
     * Value of System.currentTimeMillis() when the asset is created.
     */
    private final AtomicLong createTimestamp = new AtomicLong();

    /**
     * Value of System.currentTimeMillis() when the asset is modified
     * changing the container and adding contents does not count as modifications
     * to the Asset.
     */
    private final AtomicLong modifyTimestamp = new AtomicLong();

    private final AtomicBoolean recycled = new AtomicBoolean();

    private final List<String> detailIds;

    private volatile String thumbnailEncodedData;

    private volatile WeakReference<Bitmap> thumbnail = new WeakReference<>(null);

    //endregion

    //region STATIC FACTORIES

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

        List<String> contentIds = Collections.synchronizedList(new ArrayList<>());
        List<String> detailIds = Collections.synchronizedList(new ArrayList<>());
        FAsset asset = new FAsset(id, name, containerId, contentIds, category,
                createTimestamp, modifyTimestamp, false, detailIds, null, true);

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

    //region TRANSFORMERS AND UTILS

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
        return new FAsset(id, name, containerId, contentIds, categoryType, createTimestamp,
                modifyTimestamp, recycled, detailIds, encodedThumbnail, encodedThumbnail == null);
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = Collections.synchronizedMap(new ArrayMap<>());
        map.put(ASSET_ID, id);
        map.put(ASSET_NAME, name);
        map.put(ASSET_CONTAINERID, containerId);
        map.put(ASSET_CONTENTIDS, contentIds);
        map.put(ASSET_CATEGORYTYPE, categoryType.toString());
        map.put(ASSET_CREATETIMESTAMP, createTimestamp.get());
        map.put(ASSET_MODIFYTIMESTAMP, modifyTimestamp.get());
        map.put(ASSET_RECYCLED, recycled.get());
        map.put(ASSET_DETAILIDS, detailIds);

        // if using default thumbnail then don't put it into the map
        String thumbnailEncodedString = usingDefaultThumbnail.get() ? null : thumbnailEncodedData;
        map.put(ASSET_THUMBNAIL, thumbnailEncodedString);

        return map;
    }

    @Exclude
    public void overwrittenBy(FAsset source) {
        checkNotNull(source, "The source asset cannot be null.");
        if (!id.equals(source.getId()))
            throw new IllegalStateException("The source asset must have the same id.");
        name = source.getName();
        containerId = source.getContainerId();
        contentIds.clear();
        contentIds.addAll(source.getContentIds());
        categoryType = source.getCategoryType();
        createTimestamp.set(source.getCreateTimestamp());
        modifyTimestamp.set(source.getModifyTimestamp());
        recycled.set(source.isRecycled());
        detailIds.clear();
        detailIds.addAll(source.getDetailIds());
        thumbnailEncodedData = source.thumbnailEncodedData;
        usingDefaultThumbnail.set(source.usingDefaultThumbnail.get());
    }

    public static Class getMemberClassForDatabase(String key) {
        return memberClasses.get(key);
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
        return createTimestamp.get();
    }

    /**
     * Gets the timestamp of latest modifying as a Long object storing the milliseconds
     *
     * @return the latest modifying timestamp
     */
    @Override
    public Long getModifyTimestamp() {
        return modifyTimestamp.get();
    }

    /**
     * Gets the status whether the asset has been recycled.
     *
     * @return true if is recycled
     */
    @Override
    public Boolean isRecycled() {
        return recycled.get();
    }

    /**
     * Checks whether the asset is a root asset.
     *
     * @return true if the asset is a root asset
     */
    @Override
    @Exclude
    public Boolean isRoot() {
        return id.equals(ROOT_ASSET_ID);
    }

    @Override
    @Exclude
    public Bitmap getThumbnail() {
        if(thumbnailEncodedData == null)
            return null;

        if(thumbnail.get() == null) {
            thumbnail = new WeakReference<>(BitmapHelper.toBitmap(thumbnailEncodedData));
        }

        return thumbnail.get();
    }

    public List<String> getDetailIds() {
        return new ArrayList<>(detailIds);
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
    @Exclude
    public void setName(String name) {
        checkIllegalName(name);

        if (!isRoot()) {
            this.name = name;
        }
    }

    @Exclude
    public void setRecycled(boolean recycled) {
        if (!isRoot()) {
            this.recycled.set(recycled);
        }
    }

    /**
     * Sets the modify timestamp to the given long. If the given timestamp is smaller than
     * the original one or this asset is the root asset, then the change won't be made.
     *
     * @param modifyTimestamp
     */
    @Exclude
    public void setModifyTimestamp(long modifyTimestamp) {
        if (isRoot() || modifyTimestamp < this.modifyTimestamp.get()) return;
        this.modifyTimestamp.set(modifyTimestamp);
    }

    @Exclude
    public void setThumbnail(Bitmap photo, boolean usingDefaultThumbnail) {

//        if (encodedThumbnail != null && !encodedThumbnail.isEmpty()) {
//            Bitmap thumbnail = BitmapHelper.toBitmap(encodedThumbnail);
//            return new FAsset(id, name, containerId, contentIds, categoryType, createTimestamp,
//                    modifyTimestamp, recycled, detailIds, thumbnail, false);
//        } else {
//            return new FAsset(id, name, containerId, contentIds, categoryType, createTimestamp,
//                    modifyTimestamp, recycled, detailIds, null, true);


        this.usingDefaultThumbnail.set(usingDefaultThumbnail);
        this.thumbnailEncodedData = BitmapHelper.toString(checkNotNull(photo));
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

    private FAsset(
            String id,
            String name,
            String containerId,
            List<String> contentIds,
            CategoryType categoryType,
            Long createdTimestamp,
            Long modifiedTimestamp,
            boolean recycled,
            List<String> detailIds,
            String thumbnailEncodedData,
            boolean usingDefaultThumbnail) {

        this.id = id;
        this.name = name;
        this.containerId = containerId;
        this.contentIds = contentIds;
        this.categoryType = categoryType;
        this.createTimestamp.set(createdTimestamp);
        this.modifyTimestamp.set(modifiedTimestamp);
        this.recycled.set(recycled);
        this.detailIds = detailIds;
        this.thumbnailEncodedData = thumbnailEncodedData;
        this.usingDefaultThumbnail.set(usingDefaultThumbnail);
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
        if (name.length() > AppConfigs.ASSET_NAME_CAP)
            throw new IllegalArgumentException("The length of the name should be shorter than "
                    + AppConfigs.ASSET_NAME_CAP);
    }

    @Exclude
    private final AtomicBoolean usingDefaultThumbnail = new AtomicBoolean();

    //endregion
}