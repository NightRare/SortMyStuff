package nz.ac.aut.comp705.sortmystuff.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import rx.Observable;

import static com.google.common.base.Preconditions.checkNotNull;
import static nz.ac.aut.comp705.sortmystuff.utils.AppConfigs.ASSET_THUMBNAIL_LENGTH;
import static nz.ac.aut.comp705.sortmystuff.utils.AppConfigs.ASSET_THUMBNAIL_WIDTH;
import static nz.ac.aut.comp705.sortmystuff.utils.AppConfigs.DETAIL_IMAGE_LENGTH;
import static nz.ac.aut.comp705.sortmystuff.utils.AppConfigs.DETAIL_IMAGE_WIDTH;

public class BitmapHelper {

    public static final Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
    public static final int compressQuality = 100;
    public static final String IMAGE_FORMAT = "jpeg";
    public static final String IMAGE_EXTENSION = ".jpg";
    public static int ENCODE_FLAG = Base64.DEFAULT;

    public static byte[] toByteArray(Bitmap bitmap) {
        checkNotNull(bitmap);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(compressFormat, compressQuality, baos);
        return baos.toByteArray();
    }

    public static String toCloudSightString(Bitmap bitmap) {
        Bitmap image = Bitmap.createScaledBitmap(checkNotNull(bitmap),
                300, 300, false);
        return Base64.encodeToString(toByteArray(image), Base64.NO_WRAP);
    }

    public static byte[] toByteArray(String string) {
        return Base64.decode(checkNotNull(string), ENCODE_FLAG);
    }

    public static String toString(Bitmap bitmap) {
        return Base64.encodeToString(toByteArray(bitmap), ENCODE_FLAG);
    }

    public static String toString(byte[] bytes) {
        return Base64.encodeToString(bytes, ENCODE_FLAG);
    }

    public static Bitmap toBitmap(String string) {
        byte[] encodeByte = Base64.decode(checkNotNull(string), ENCODE_FLAG);
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

    public static Observable<Bitmap> fromURI(URL url) {
        return Observable.defer(() -> {
            LoadingBitmapAsync loadingBitmapAsync = new LoadingBitmapAsync();
            try {
                return Observable.just(loadingBitmapAsync.execute(url).get());
            } catch (InterruptedException | ExecutionException e) {
                return Observable.just(null);
            }
        });
    }

    private static class LoadingBitmapAsync extends AsyncTask<URL, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(URL... urls) {
            for (URL url : urls) {
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap output = BitmapFactory.decodeStream(input);
                    return output;
                } catch (IOException e) {
                    // Log exception
                    return null;
                }
            }
            return null;
        }
    }
}
