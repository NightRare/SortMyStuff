package nz.ac.aut.comp705.sortmystuff.data;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.UUID;

import nz.ac.aut.comp705.sortmystuff.util.AppConstraints;

/**
 * @author Yuan
 *
 * An Extended Detail class used to format an asset's details
 */

public abstract class Detail {

    //********************************************
    // DATA FIELDS
    //********************************************

    @NonNull
    private final String id;

    @NonNull
    private final String assetId;

    private final DetailType type;

    @NonNull
    private String label;


    //********************************************
    // CONSTRUCTOR
    //********************************************
    protected Detail(String assetId, DetailType type, String label) {
        checkIllegalAssetId(assetId);
        checkIllegalLabel(label);

        id = UUID.randomUUID().toString();
        this.assetId = assetId;
        this.type = type;
        this.label = label;
    }


    //********************************************
    // ACCESSORS
    //********************************************

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

    public abstract Object getField();


    //********************************************
    // MUTATORS
    //********************************************

    protected void setLabel(@NonNull String label) {
        checkIllegalLabel(label);
        this.label = label;
    }

    //********************************************
    // OBJECT METHODS OVERRIDING
    //********************************************

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

    //********************************************
    // PRIVATE
    //********************************************

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
}
