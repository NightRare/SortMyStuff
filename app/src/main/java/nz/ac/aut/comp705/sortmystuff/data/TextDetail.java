package nz.ac.aut.comp705.sortmystuff.data;

import com.google.common.base.Preconditions;

import nz.ac.aut.comp705.sortmystuff.util.AppConstraints;

/**
 * Created by Yuan on 2017/4/24.
 */

public final class TextDetail extends Detail {

    //region DATA FIELDS

    private String field;

    //endregion

    //region STATIC FACTORIES

    public static TextDetail create(String assetId, String label, String field) {
        checkIllegalField(field);
        return new TextDetail(assetId, DetailType.Text, label, field);
    }

    //endregion

    //region ACCESSORS

    public String getTextField() {
        return field;
    }

    @Override
    public Object getField() {
        return getTextField();
    }

    //endregion

    //region MUTATORS

    void setField(String field) {
        checkIllegalField(field);
        this.field = field;
    }

    //endregion

    //region OBJECT METHODS OVERRIDING

    @Override
    public boolean equals(Object o) {
        if(o instanceof TextDetail) {
            return super.equals(o);
        }
        return false;
    }

    //endregion

    //region PRIVATE

    private TextDetail(String assetId, DetailType type, String label, String field) {
        super(assetId, type, label);
        this.field = field;
    }

    private static void checkIllegalField(String field) {
        Preconditions.checkNotNull(field);
        if(field.isEmpty())
            throw new IllegalArgumentException("cannot be empty");
        if(field.length() > AppConstraints.TEXTDETAIL_FIELD_CAP)
            throw new IllegalArgumentException("string length exceeds cap");
    }

    //endregion
}
