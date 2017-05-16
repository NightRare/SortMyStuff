package nz.ac.aut.comp705.sortmystuff.data;

import android.graphics.Bitmap;

import com.google.common.base.Preconditions;

import static nz.ac.aut.comp705.sortmystuff.util.AppConstraints.IMAGE_DETAIL_FORMAT;

/**
 * Created by Yuan on 2017/5/16.
 */

public class ImageDetail extends Detail<Bitmap> {

    //region DATA FIELDS

    private String imageFileName;

    //endregion

    //region STATIC FACTORIES

    public static ImageDetail create(String assetId, String label, Bitmap field) {
        Preconditions.checkNotNull(field);
        return new ImageDetail(assetId, DetailType.Image, label, field);
    }

    //endregion

    //region ACCESSORS

    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap getField() {
        return field;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    //endregion

    //region MUTATORS

    void setField(Bitmap field) {
        Preconditions.checkNotNull(field);
        this.field = field;
    }

    //endregion

    //region OBJECT METHODS OVERRIDING

    @Override
    public boolean equals(Object o) {
        if(o instanceof ImageDetail) {
            return super.equals(o);
        }
        return false;
    }

    //endregion

    //region PRIVATE

    private transient Bitmap field;

    private ImageDetail(String assetId, DetailType type, String label, Bitmap field) {
        super(assetId, type, label);
        this.field = field;
        this.imageFileName = getId() + IMAGE_DETAIL_FORMAT;
    }

    //endregion
}
