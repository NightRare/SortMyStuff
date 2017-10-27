package nz.ac.aut.comp705.sortmystuff.data.models;

import android.graphics.Bitmap;

import java.util.List;

public interface IAsset {

    String getId();

    String getName();

    String getContainerId();

    List<String> getContentIds();

    Boolean isRecycled();

    Boolean isRoot();

    Long getCreateTimestamp();

    Long getModifyTimestamp();

    Bitmap getThumbnail();

    CategoryType getCategoryType();
}
