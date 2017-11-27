package nz.ac.aut.comp705.sortmystuff.utils;

public class AppConfigs {

    //********************************************
    // App
    //********************************************

    public static final long DELAYED_PHOTO_RECOGNITION_MILLIS = 12000;

    public static final long PHOTO_RECOGNITION_INTERVAL = 10000;

    public static final long PHOTO_RECOGNITION_GET_RESULT_INITIAL_WAIT = 6000;

    public static final long PHOTO_RECOGNITION_GET_RESULT_INTERVAL = 4000;

    public static final int PHOTO_RECOGNITION_GET_RESULT_MAX_TRY = 5;

    //********************************************
    // ASSET
    //********************************************

    public final static int ASSET_NAME_CAP = 100;

    public final static int ASSET_THUMBNAIL_WIDTH = 150;

    public final static int ASSET_THUMBNAIL_LENGTH = 150;

    //********************************************
    // DETAIL
    //********************************************

    public final static int CATEGORY_NAME_CAP = 100;

    public final static int DETAIL_LABEL_CAP = 100;

    public final static int TEXTDETAIL_FIELD_CAP = 400;

    public final static int DETAIL_IMAGE_WIDTH = 1200;

    public final static int DETAIL_IMAGE_LENGTH = 1200;

    //********************************************
    // IDataManager
    //********************************************

    public final static int CACHED_DETAILS_LIST_NUM = 5;

}
