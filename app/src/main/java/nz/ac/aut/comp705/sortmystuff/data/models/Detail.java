package nz.ac.aut.comp705.sortmystuff.data.models;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.UUID;

import nz.ac.aut.comp705.sortmystuff.utils.AppConstraints;

/**
 * A detail of an asset is a particular record/information of as asset. Each detail has a label and
 * a field.
 * <p>
 * The label of a detail is like the name, identifying the detail whereas the field is an object
 * storing the value of the detail. For example, for a Book asset, there could be a detail whose
 * label is "Author" and field as "J.R.R. Tolkien".
 * <p>
 * This abstract class is a base of all types of details.
 *
 * @param <T> the type of the field
 */
public abstract class Detail<T> implements IDetail<T>{

    //region DATA FIELDS

    @NonNull
    private final String id;

    @NonNull
    private final String assetId;

    private final DetailType type;

    @NonNull
    private String label;

    //endregion

    //region STATIC FACTORIES

    /**
     * Initialises the detail according to the given arguments.
     *
     * @param assetId the id of the owner asset
     * @param type the {@link DetailType} of the detail
     * @param label the label of the detail
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if assetId is empty; or if label is empty or longer than
     *                                  {@link AppConstraints#DETAIL_LABEL_CAP}
     */
    protected Detail(String assetId, DetailType type, String label) {
        checkIllegalAssetId(assetId);
        Preconditions.checkNotNull(type);
        checkIllegalLabel(label);

        id = UUID.randomUUID().toString();
        this.assetId = assetId;
        this.type = type;
        this.label = label;
    }

    //endregion

    //region ACCESSORS

    /**
     * Gets the unique id.
     *
     * @return the id
     */
    @NonNull
    @Override
    public String getId() {
        return id;
    }

    /**
     * Gets the id of the owner asset.
     *
     * @return the id of the owner asset
     */
    @NonNull
    @Override
    public String getAssetId() {
        return assetId;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    @NonNull
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
    public abstract T getField();

    @Override
    public Long getCreateTimestamp() {
        //TODO: to be implemented
        return 0L;
    }

    @Override
    public Long getModifyTimestamp() {
        //TODO: to be implemented
        return 0L;
    }

    //endregion

    //region MODIFIERS

    /**
     * IMPORTANT: FOR DATA LAYER COMPONENTS USE ONLY.
     * <p>
     * DO NOT CALL OUTSIDE {@link nz.ac.aut.comp705.sortmystuff.data} PACKAGE
     */
    @Deprecated
    public void setLabel(@NonNull String label) {
        checkIllegalLabel(label);
        this.label = label;
    }

    /**
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
     * Sets the field to the given value.
     *
     * @param field the field to be set.
     * @throws NullPointerException if field is {@code null}
     */
    @Deprecated
    public abstract void setField(@NonNull T field);

    //endregion

    //region OBJECT METHODS OVERRIDING

    /**
     * Compares by the id.
     *
     * @param o the object to be compared
     * @return true if the ids are equal
     */
    @Override
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
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * @return the label of the detail
     */
    @Override
    public String toString() {
        return label;
    }

    //endregion

    //region PRIVATE STUFF

    private static void checkIllegalLabel(String label) {
        Preconditions.checkNotNull(label);
        if (label.isEmpty())
            throw new IllegalArgumentException("cannot be empty");
        if (label.length() > AppConstraints.DETAIL_LABEL_CAP)
            throw new IllegalArgumentException("string length exceeds cap");
    }

    private static void checkIllegalAssetId(String assetId) {
        Preconditions.checkNotNull(assetId);
        if (assetId.isEmpty())
            throw new IllegalArgumentException("cannot be empty");
    }

    //endregion
}
