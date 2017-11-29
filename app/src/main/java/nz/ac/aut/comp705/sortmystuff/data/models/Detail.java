package nz.ac.aut.comp705.sortmystuff.data.models;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.Exclude;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import nz.ac.aut.comp705.sortmystuff.utils.AppConfigs;
import nz.ac.aut.comp705.sortmystuff.utils.BitmapHelper;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A detail of an asset is a particular record/information of as asset. Each detail has a label and
 * a field.
 * <p>
 * The label of a detail is like the name, identifying the detail whereas the field is an object
 * storing the value of the detail. For example, for a Book asset, there could be a detail whose
 * label is "Author" and field as "J.R.R. Tolkien".
 * <p>
 *
 * @param <T> the type of the field
 */
public class Detail<T> implements IDetail<T>, Comparable {

    //region FIELD NAMES

    public static final String DETAIL_ID = "id";
    public static final String DETAIL_ASSETID = "assetId";
    public static final String DETAIL_TYPE = "type";
    public static final String DETAIL_LABEL = "label";
    public static final String DETAIL_FIELD = "field";
    public static final String DETAIL_CREATETIMESTAMP = "createTimestamp";
    public static final String DETAIL_MODIFYTIMESTAMP = "modifyTimestamp";
    public static final String DETAIL_POSITION = "position";
    public static final String DETAIL_DEFAULTFIELDVALUE = "defaultFieldValue";

    //endregion

    //region DATA FIELDS

    private String id;

    private String assetId;

    private DetailType type;

    private String label;

    private String fieldData;

    private WeakReference<T> fieldObject;

    private long createTimestamp;

    private long modifyTimestamp;

    private int position;

    private boolean defaultFieldValue;

    //endregion

    //region STATIC FACTORIES

    @Exclude
    public static Detail createDetail(String assetId, DetailType type, String label, String fieldData) {
        checkIllegalAssetId(assetId);
        checkIllegalLabel(label);
        checkNotNull(fieldData);
        checkNotNull(type);

        String id = UUID.randomUUID().toString();
        long createTimestamp = System.currentTimeMillis();
        return new Detail(id, assetId, type, label, fieldData, createTimestamp, createTimestamp, -1, true);
    }

    @Exclude
    public static Detail<String> createTextDetail(String assetId, String label, String fieldData) {
        checkIllegalAssetId(assetId);
        checkIllegalLabel(label);
        checkNotNull(fieldData);

        String id = UUID.randomUUID().toString();
        DetailType type = DetailType.Text;
        long createTimestamp = System.currentTimeMillis();

        return new Detail<String>(id, assetId, type, label, fieldData, createTimestamp, createTimestamp, -1, true);
    }

    @Exclude
    public static Detail<String> createDateDetail(String assetId, String label, String fieldData) {
        checkIllegalAssetId(assetId);
        checkIllegalLabel(label);
        checkNotNull(fieldData);

        String id = UUID.randomUUID().toString();
        DetailType type = DetailType.Date;
        long createTimestamp = System.currentTimeMillis();

        return new Detail<String>(id, assetId, type, label, fieldData, createTimestamp, createTimestamp, -1, true);
    }

    @Exclude
    public static Detail<Bitmap> createImageDetail(String assetId, String label, String fieldData) {
        checkIllegalAssetId(assetId);
        checkIllegalLabel(label);
        checkNotNull(fieldData);

        String id = UUID.randomUUID().toString();
        DetailType type = DetailType.Image;
        long createTimestamp = System.currentTimeMillis();

        return new Detail<Bitmap>(id, assetId, type, label, fieldData, createTimestamp, createTimestamp, -1, true);
    }

    //endregion

    //region TRANSFORMERS

    @Exclude
    public static Detail fromMap(Map<String, Object> members) {
        String id = members.get(DETAIL_ID).toString();
        String assetId = members.get(DETAIL_ASSETID).toString();
        DetailType type = DetailType.valueOf(members.get(DETAIL_TYPE).toString());
        String label = members.get(DETAIL_LABEL).toString();
        Long createTimestamp = (Long) members.get(DETAIL_CREATETIMESTAMP);
        Long modifyTimestamp = (Long) members.get(DETAIL_MODIFYTIMESTAMP);
        // Integer is retrieved as Long from Firebase
        int position = Integer.valueOf((members.get(DETAIL_POSITION).toString()));
        boolean defaultFieldValue = (boolean) members.get(DETAIL_DEFAULTFIELDVALUE);

        String fieldData = (String) members.get(DETAIL_FIELD);
        if (type.equals(DetailType.Date) || type.equals(DetailType.Text)) {
            if (fieldData == null) fieldData = "";
            return new Detail<String>(id, assetId, type, label, fieldData, createTimestamp, modifyTimestamp, position, defaultFieldValue);
        } else if (type.equals(DetailType.Image)) {
            return new Detail<Bitmap>(id, assetId, type, label, fieldData, createTimestamp, modifyTimestamp, position, defaultFieldValue);
        } else return null;
    }


    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(DETAIL_ID, id);
        map.put(DETAIL_ASSETID, assetId);
        map.put(DETAIL_TYPE, type.toString());
        map.put(DETAIL_LABEL, label);
        map.put(DETAIL_CREATETIMESTAMP, createTimestamp);
        map.put(DETAIL_MODIFYTIMESTAMP, modifyTimestamp);
        map.put(DETAIL_POSITION, position);
        map.put(DETAIL_DEFAULTFIELDVALUE, defaultFieldValue);

        String field = fieldData;
        if (type.equals(DetailType.Image)) {
            // do not convert default photo data
            field = null;
        }
        map.put(DETAIL_FIELD, field);

        return map;
    }

    @Exclude
    public void overwrittenBy(Detail source) {
        if (!type.equals(source.getType()))
            throw new IllegalStateException("Cannot be overwritten by a different type of Detail");

        id = source.getId();
        assetId = source.getAssetId();
        label = source.getLabel();
        fieldData = source.getFieldData();
        createTimestamp = source.getCreateTimestamp();
        modifyTimestamp = source.getModifyTimestamp();
        position = source.getPosition();
        defaultFieldValue = source.isDefaultFieldValue();
    }

    //endregion

    //region ACCESSORS

    /**
     * Gets the unique id.
     *
     * @return the id
     */

    @Override
    public String getId() {
        return id;
    }

    /**
     * Gets the id of the owner asset.
     *
     * @return the id of the owner asset
     */

    @Override
    public String getAssetId() {
        return assetId;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */

    @Override
    public String getLabel() {
        return label;
    }

    /**
     * Gets the type of the detail.
     *
     * @return the DetailType
     */
    @Override
    public DetailType getType() {
        return type;
    }

    /**
     * Gets the field of the detail.
     *
     * @return the field
     */
    @Override
    @Exclude
    public T getField() {
        if (type.equals(DetailType.Image)) {
            if (fieldObject == null || fieldObject.get() == null) {
                fieldObject = new WeakReference<T>((T) BitmapHelper.toBitmap(fieldData));
            }

            return fieldObject.get();
        } else {
            return (T) fieldData;
        }
    }

    @Override
    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    @Override
    public Long getModifyTimestamp() {
        return modifyTimestamp;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public boolean isDefaultFieldValue() {
        return defaultFieldValue;
    }

    public String getFieldData() {
        return fieldData;
    }

    //endregion

    //region MODIFIERS

    @Exclude
    public void setLabel(String label) {
        checkIllegalLabel(label);
        this.label = label;
    }

    /**
     * Sets the modify timestamp to the given long. If the given timestamp is smaller than
     * the original one, then the change won't be made.
     *
     * @param modifyTimestamp
     */
    @Exclude
    public void setModifyTimestamp(long modifyTimestamp) {
        if (modifyTimestamp < this.modifyTimestamp) return;
        this.modifyTimestamp = modifyTimestamp;
    }

    @Exclude
    public void setPosition(int position) {
        if (position < 0) return;
        this.position = position;
    }

    public void setFieldData(@Nullable String fieldData, boolean defaultFieldValue) {
        this.defaultFieldValue = defaultFieldValue;
        this.fieldData = fieldData;

        // remove cached fieldObject
        fieldObject = null;
    }

    //endregion

    //region OBJECT/COMPARABLE METHODS OVERRIDING

    /**
     * Compares by the id.
     *
     * @param o the object to be compared
     * @return true if the ids are equal
     */
    @Override
    @Exclude
    public boolean equals(Object o) {
        if (o instanceof Detail) {
            Detail d = (Detail) o;
            if (d.id.equals(id))
                return true;
        }
        return false;
    }

    /**
     * @return the hashcode of its id
     */
    @Override
    @Exclude
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * @return the label of the detail
     */
    @Override
    @Exclude
    public String toString() {
        return label;
    }


    @Override
    public int compareTo(@NonNull Object o) {
        if (o instanceof Detail) {
            Detail d = (Detail) o;
            return Integer.compare(position, d.getPosition());
        }
        // always bigger than something not a Detail
        return 1;
    }

    //endregion

    //region PRIVATE STUFF

    private Detail(String id, String assetId, DetailType type, String label, String fieldData,
                   long createTimestamp, long modifyTimestamp, int position, boolean defaultFieldValue) {
        this.id = id;
        this.assetId = assetId;
        this.type = type;
        this.label = label;
        this.fieldData = fieldData;
        this.createTimestamp = createTimestamp;
        this.modifyTimestamp = modifyTimestamp;
        this.position = position;
        this.defaultFieldValue = defaultFieldValue;
    }

    @Exclude
    private static void checkIllegalLabel(String label) {
        checkNotNull(label);
        if (label.isEmpty())
            throw new IllegalArgumentException("cannot be empty");
        if (label.length() > AppConfigs.DETAIL_LABEL_CAP)
            throw new IllegalArgumentException("string length exceeds cap");
    }

    @Exclude
    private static void checkIllegalAssetId(String assetId) {
        checkNotNull(assetId);
        if (assetId.isEmpty())
            throw new IllegalArgumentException("cannot be empty");
    }

    //endregion
}
