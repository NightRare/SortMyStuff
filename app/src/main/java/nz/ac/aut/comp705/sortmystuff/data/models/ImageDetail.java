package nz.ac.aut.comp705.sortmystuff.data.models;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import nz.ac.aut.comp705.sortmystuff.util.AppConstraints;

/**
 * ImageDetail has a field as Bitmap instance.
 * <p>
 * Created by Yuan on 2017/5/16.
 */

public class ImageDetail extends Detail<Bitmap> {

    //region DATA FIELDS


    //endregion

    //region STATIC FACTORIES

    /**
     * Static factory to instantiates an ImageDetail.
     *
     * @param assetId the id of the owner asset
     * @param label   the label of the detail
     * @param field   the field of the detail
     * @return the ImageDetail instance
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if assetId is empty; or if label is empty or longer than
     *                                  {@link AppConstraints#DETAIL_LABEL_CAP}
     */
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

    //endregion

    //region MUTATORS

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public void setField(@NonNull Bitmap field) {
        Preconditions.checkNotNull(field);

        this.field = field;
    }

    //endregion

    //region PRIVATE

    private transient Bitmap field;

    private ImageDetail(String assetId, DetailType type, String label, Bitmap field) {
        super(assetId, type, label);
        this.field = field;
    }

    //endregion
}
