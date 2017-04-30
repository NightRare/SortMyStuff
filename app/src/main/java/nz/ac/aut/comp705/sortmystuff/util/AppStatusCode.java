package nz.ac.aut.comp705.sortmystuff.util;

/**
 * Created by Yuan on 2017/4/26.
 */

public class AppStatusCode {

    public static final int OK = 100;

    public static final int ASSET_NOT_EXISTS = 102;

    public static final int NO_ROOT_ASSET = 103;

    /**
     * Any request involving modification to Root asset should get this code.
     */
    public static final int ROOT_ASSET_IMMUTABLE = 105;

    public static final int UNEXPECTED_ERROR = 111;

    public static final int LOCAL_DATA_CORRUPT = 201;

}
