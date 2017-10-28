package nz.ac.aut.comp705.sortmystuff.data.models;

import android.graphics.Bitmap;

/**
 * All the types of the details.
 * <p>
 * Created by Yuan on 2017/4/24.
 */

public enum DetailType {

    Text(String.class),
    Date(String.class),
    Image(Bitmap.class);

    private transient Class fieldClass;

    DetailType(Class fieldClass) {
        this.fieldClass = fieldClass;
    }

    public Class getFieldClass() {
        return fieldClass;
    }
}
