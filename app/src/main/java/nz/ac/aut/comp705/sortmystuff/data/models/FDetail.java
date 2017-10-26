package nz.ac.aut.comp705.sortmystuff.data.models;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import nz.ac.aut.comp705.sortmystuff.utils.AppConstraints;
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
public class FDetail<T> implements IDetail<T>, Comparable {

    //region FIELD NAMES

    public static final String DETAILS_LIST = "details";
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

    private T field;

    private long createTimestamp;

    private long modifyTimestamp;

    private int position;

    private boolean defaultFieldValue;

    //endregion

    //region STATIC FACTORIES

    public FDetail() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    @Exclude
    public static FDetail<String> createTextDetail(String assetId, String label, String field) {
        checkIllegalAssetId(assetId);
        checkIllegalLabel(label);
        checkNotNull(field);

        String id = UUID.randomUUID().toString();
        DetailType type = DetailType.Text;
        long createTimestamp = System.currentTimeMillis();

        return new FDetail<String>(id, assetId, type, label, field, createTimestamp, createTimestamp, -1, true);
    }

    @Exclude
    public static FDetail<String> createDateDetail(String assetId, String label, String field) {
        checkIllegalAssetId(assetId);
        checkIllegalLabel(label);
        checkNotNull(field);

        String id = UUID.randomUUID().toString();
        DetailType type = DetailType.Date;
        long createTimestamp = System.currentTimeMillis();

        return new FDetail<String>(id, assetId, type, label, field, createTimestamp, createTimestamp, -1, true);
    }

    @Exclude
    public static FDetail<Bitmap> createImageDetail(String assetId, String label, Bitmap field) {
        checkIllegalAssetId(assetId);
        checkIllegalLabel(label);
        checkNotNull(field);

        String id = UUID.randomUUID().toString();
        DetailType type = DetailType.Image;
        long createTimestamp = System.currentTimeMillis();

        return new FDetail<Bitmap>(id, assetId, type, label, field, createTimestamp, createTimestamp, -1, true);
    }

    //endregion

    //region TRANSFORMERS

    @Exclude
    public static FDetail fromMap(Map<String, Object> members) {
        String id = members.get(DETAIL_ID).toString();
        String assetId = members.get(DETAIL_ASSETID).toString();
        DetailType type = DetailType.valueOf(members.get(DETAIL_TYPE).toString());
        String label = members.get(DETAIL_LABEL).toString();
        Long createTimestamp = (Long) members.get(DETAIL_CREATETIMESTAMP);
        Long modifyTimestamp = (Long) members.get(DETAIL_MODIFYTIMESTAMP);
        // Integer is retrieved as Long from Firebase
        int position = Integer.valueOf((members.get(DETAIL_POSITION).toString()));
        boolean defaultFieldValue = (boolean) members.get(DETAIL_DEFAULTFIELDVALUE);

        if (type.equals(DetailType.Date) || type.equals(DetailType.Text)) {
            String field = members.get(DETAIL_FIELD).toString();
            return new FDetail<String>(id, assetId, type, label, field, createTimestamp, modifyTimestamp, position, defaultFieldValue);
        } else if (type.equals(DetailType.Image)) {
            Bitmap field = null;
            if (!defaultFieldValue) {
                String byteArrayString = (String) members.get(DETAIL_FIELD);
                field = byteArrayString == null ? null : BitmapHelper.toBitmap(byteArrayString);
            }
            return new FDetail<Bitmap>(id, assetId, type, label, field, createTimestamp, modifyTimestamp, position, defaultFieldValue);
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

        if (type.equals(DetailType.Date) || type.equals(DetailType.Text)) {
            map.put(DETAIL_FIELD, field.toString());
        } else if (type.equals(DetailType.Image)) {
            String fieldValue = null;
            if (!defaultFieldValue) {
                fieldValue = field == null ? null : BitmapHelper.toString((Bitmap) field);
            }
            map.put(DETAIL_FIELD, fieldValue);
        }
        return map;
    }

    @Exclude
    public static FDetail fromDetail(Detail source) {
        checkNotNull(source, "The source detail cannot be null");

        String id = source.getId();
        String assetId = source.getAssetId();
        DetailType type = source.getType();
        String label = source.getLabel();
        Object field = source.getField();
        long createTimestamp = System.currentTimeMillis();
        int position = source.getPosition();
        boolean defaultFieldValue = source.isDefaultFieldValue();

        return new FDetail(id, assetId, type, label, field, createTimestamp, createTimestamp, position, defaultFieldValue);
    }

    @Exclude
    public void overwrittenBy(FDetail source) {
        if (!field.getClass().equals(checkNotNull(source).getField().getClass()))
            throw new IllegalStateException("Cannot be overwritten by a different type of FDetail");

        id = source.getId();
        assetId = source.getAssetId();
        type = source.getType();
        label = source.getLabel();
        field = (T) source.getField();
        createTimestamp = source.getCreateTimestamp();
        modifyTimestamp = source.getModifyTimestamp();
        position = source.getPosition();
        boolean defaultFieldValue = source.isDefaultFieldValue();
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
        return field;
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

    //endregion

    //region MODIFIERS

    @Exclude
    public void setLabel(String label) {
        checkIllegalLabel(label);
        this.label = label;
    }

    /**
     * Sets the field to the given value.
     *
     * @param field the field to be set.
     * @throws NullPointerException if field is {@code null}
     */
    @Exclude
    public void setField(T field, boolean defaultFieldValue) {
        this.field = checkNotNull(field);
        this.defaultFieldValue = defaultFieldValue;
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
        if (o instanceof FDetail) {
            FDetail d = (FDetail) o;
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
        if (o instanceof FDetail) {
            FDetail d = (FDetail) o;
            return Integer.compare(position, d.getPosition());
        }
        // always bigger than something not a FDetail
        return 1;
    }

    //endregion

    //region PRIVATE STUFF

    private FDetail(String id, String assetId, DetailType type, String label, T field,
                    long createTimestamp, long modifyTimestamp, int position, boolean defaultFieldValue) {
        this.id = id;
        this.assetId = assetId;
        this.type = type;
        this.label = label;
        this.field = field;
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
        if (label.length() > AppConstraints.DETAIL_LABEL_CAP)
            throw new IllegalArgumentException("string length exceeds cap");
    }

    @Exclude
    private static void checkIllegalAssetId(String assetId) {
        checkNotNull(assetId);
        if (assetId.isEmpty())
            throw new IllegalArgumentException("cannot be empty");
    }

    @Exclude
    private void updateTimeStamp() {
        modifyTimestamp = System.currentTimeMillis();
    }

    //endregion
}
