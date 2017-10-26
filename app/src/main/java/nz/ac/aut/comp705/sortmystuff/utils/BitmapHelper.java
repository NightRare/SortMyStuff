package nz.ac.aut.comp705.sortmystuff.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static nz.ac.aut.comp705.sortmystuff.utils.AppConstraints.ASSET_THUMBNAIL_LENGTH;
import static nz.ac.aut.comp705.sortmystuff.utils.AppConstraints.ASSET_THUMBNAIL_WIDTH;
import static nz.ac.aut.comp705.sortmystuff.utils.AppConstraints.DETAIL_IMAGE_LENGTH;
import static nz.ac.aut.comp705.sortmystuff.utils.AppConstraints.DETAIL_IMAGE_WIDTH;

public class BitmapHelper {

    public static final Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
    public static final int compressQuality = 100;
    public static final String IMAGE_EXTENSION = ".jpg";

    public static byte[] toByteArray(Bitmap bitmap) {
        checkNotNull(bitmap);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(compressFormat, compressQuality, baos);
        return baos.toByteArray();
    }

    public static String toString(Bitmap bitmap) {
        return Base64.encodeToString(toByteArray(bitmap), Base64.DEFAULT);
    }

    public static Bitmap toBitmap(String string) {
        byte[] encodeByte = Base64.decode(checkNotNull(string), Base64.DEFAULT);
        return toBitmap(encodeByte);
    }

    public static Bitmap toBitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    public static Bitmap rescaleToImageDetailSize(Bitmap original) {
        return Bitmap.createScaledBitmap(checkNotNull(original),
                DETAIL_IMAGE_WIDTH, DETAIL_IMAGE_LENGTH, false);
    }

    public static Bitmap toThumbnail(Bitmap original) {
        return Bitmap.createScaledBitmap(checkNotNull(original),
                ASSET_THUMBNAIL_WIDTH, ASSET_THUMBNAIL_LENGTH, false);
    }
}
