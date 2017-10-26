package nz.ac.aut.comp705.sortmystuff.utils;

/**
 * Created by Vince on 2017/4/24.
 */

public class AppConstraints {

    //********************************************
    // ASSET
    //********************************************

    public final static int ASSET_NAME_CAP = 40;

    public final static String ROOT_ASSET_ID = "RootAssetId";

    public final static int ASSET_THUMBNAIL_WIDTH = 128;

    public final static int ASSET_THUMBNAIL_LENGTH = 128;

    //********************************************
    // DETAIL
    //********************************************

    public final static int CATEGORY_NAME_CAP = 40;

    public final static int DETAIL_LABEL_CAP = 40;

    public final static int TEXTDETAIL_FIELD_CAP = 400;

    public final static int DETAIL_IMAGE_WIDTH = 1024;

    public final static int DETAIL_IMAGE_LENGTH = 1024;

    //********************************************
    // IDataManager
    //********************************************

    public final static int CACHED_DETAILS_LIST_NUM = 10;

    //********************************************
    // IDataRepostiory
    //********************************************
    public static final long MAX_DOWNLOAD_BYTES = 20 * 1024 * 1024; //50MB
}
