package nz.ac.aut.comp705.sortmystuff.data.models;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.UUID;

import nz.ac.aut.comp705.sortmystuff.util.AppConstraints;

/**
 *
 * @author Yuan
 */

public abstract class Detail<T> {

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

    protected Detail(String assetId, DetailType type, String label) {
        checkIllegalAssetId(assetId);
        checkIllegalLabel(label);

        id = UUID.randomUUID().toString();
        this.assetId = assetId;
        this.type = type;
        this.label = label;
    }

    //endregion

    //region ACCESSORS

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getAssetId() {
        return assetId;
    }

    @NonNull
    public String getLabel() {
        return label;
    }

    public DetailType getType() {
        return type;
    }

    public abstract T getField();

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
     * IMPORTANT: FOR DATA LAYER COMPONENTS USE ONLY.
     * <p>
     * DO NOT CALL OUTSIDE {@link nz.ac.aut.comp705.sortmystuff.data} PACKAGE
     */
    @Deprecated
    public abstract void setField(@NonNull T field);

    //endregion

    //region OBJECT METHODS OVERRIDING

    @Override
    public boolean equals(Object o) {
        if(o instanceof Detail) {
            Detail d = (Detail) o;
            if(d.id.equals(id))
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
        return label;
    }

    //endregion

    //region PRIVATE STUFF

    private static void checkIllegalLabel(String label) {
        Preconditions.checkNotNull(label);
        if(label.isEmpty())
            throw new IllegalArgumentException("cannot be empty");
        if(label.length() > AppConstraints.DETAIL_LABEL_CAP)
            throw new IllegalArgumentException("string length exceeds cap");
    }

    private static void checkIllegalAssetId(String assetId) {
        Preconditions.checkNotNull(assetId);
        if(assetId.isEmpty())
            throw new IllegalArgumentException("cannot be empty");
    }

    //endregion
}
