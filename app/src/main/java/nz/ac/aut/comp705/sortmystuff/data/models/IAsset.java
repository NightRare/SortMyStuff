package nz.ac.aut.comp705.sortmystuff.data.models;

import android.graphics.Bitmap;

public interface IAsset {

    String getId();

    String getName();

    String getContainerId();

    boolean isRecycled();

    boolean isRoot();

    Long getCreateTimestamp();

    Long getModifyTimestamp();

    Bitmap getThumbnail();

    CategoryType getCategoryType();
}
