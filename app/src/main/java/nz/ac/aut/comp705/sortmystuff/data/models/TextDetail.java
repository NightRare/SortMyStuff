package nz.ac.aut.comp705.sortmystuff.data.models;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import nz.ac.aut.comp705.sortmystuff.util.AppConstraints;

/**
 * TextDetail has a field as String.
 * <p>
 * Created by Yuan on 2017/4/24.
 */

public final class TextDetail extends Detail<String> {

    //region DATA FIELDS

    private String field;

    //endregion

    //region STATIC FACTORIES

    /**
     * Static factory to instantiates a TextDetail whose type is {@link DetailType#Text}.
     *
     * @param assetId the id of the owner asset
     * @param label   the label of the detail
     * @param field   the field of the detail
     * @return the ImageDetail instance
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if assetId is empty; or if label is empty or longer than
     *                                  {@link AppConstraints#DETAIL_LABEL_CAP}; or if field exceeds
     *                                  {@link AppConstraints#TEXTDETAIL_FIELD_CAP}
     */
    public static TextDetail createTextDetail(String assetId, String label, String field) {
        checkIllegalField(field);
        return new TextDetail(assetId, DetailType.Text, label, field);
    }

    /**
     * Static factory to instantiates a TextDetail whose type is {@link DetailType#Date}.
     *
     * @param assetId the id of the owner asset
     * @param label   the label of the detail
     * @param field   the field of the detail
     * @return the ImageDetail instance
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if assetId is empty; or if label is empty or longer than
     *                                  {@link AppConstraints#DETAIL_LABEL_CAP}; or if field exceeds
     *                                  {@link AppConstraints#TEXTDETAIL_FIELD_CAP}
     */
    public static TextDetail createDateDetail(String assetId, String label, String field) {
        checkIllegalField(field);
        return new TextDetail(assetId, DetailType.Date, label, field);
    }

    //endregion

    //region ACCESSORS

    /**
     * {@inheritDoc}
     */
    @Override
    public String getField() {
        return field;
    }

    //endregion

    //region MUTATORS

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public void setField(@NonNull String field) {
        checkIllegalField(field);
        this.field = field;
    }

    //endregion

    //region PRIVATE

    private TextDetail(String assetId, DetailType type, String label, String field) {
        super(assetId, type, label);
        this.field = field;
    }

    private static void checkIllegalField(String field) {
        Preconditions.checkNotNull(field);
        if (field.length() > AppConstraints.TEXTDETAIL_FIELD_CAP)
            throw new IllegalArgumentException("string length exceeds cap");
    }

    //endregion
}
