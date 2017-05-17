package nz.ac.aut.comp705.sortmystuff.data.models;

import android.graphics.Bitmap;

/**
 * Created by Yuan on 2017/4/24.
 */

public enum DetailType {
    Text (TextDetail.class, String.class),
    Date (TextDetail.class, String.class),
    Image (ImageDetail.class, Bitmap.class);

    private transient Class detailClass;
    private transient Class fieldClass;

    DetailType(Class detailClass, Class fieldClass) {
        this.detailClass = detailClass;
        this.fieldClass = fieldClass;
    }

    public Class getFieldClass() {
        return fieldClass;
    }

    public Class getDetailClass() {
        return detailClass;
    }
}
