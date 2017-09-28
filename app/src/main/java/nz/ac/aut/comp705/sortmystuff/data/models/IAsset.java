package nz.ac.aut.comp705.sortmystuff.data.models;

import android.graphics.Bitmap;

/**
 * Created by YuanY on 2017/9/23.
 */

public interface IAsset {
    String getId();

    String getName();

    String getContainerId();

    boolean isRecycled();

    boolean isRoot();

    Long getCreateTimestamp();

    Long getModifyTimestamp();

    Byte[] getThumbData();

    Bitmap getPhoto();

    CategoryType getCategoryType();
}
