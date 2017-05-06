package nz.ac.aut.comp705.sortmystuff.util;

/**
 * Created by Yuan on 2017/4/30.
 */

public class Log {

    //region TAGS

    public static final String LOCAL_FILE_CORRUPT = "LOCAL_FILE_CORRUPT";

    //endregion

    private static final boolean DEBUG = false;

    public static void i(String tag, String msg) {
        if (DEBUG)
            android.util.Log.i(tag, msg);
    }

    public static void i(String tag, String msg, Throwable ex) {
        if (DEBUG)
            android.util.Log.i(tag, msg, ex);
    }

    public static void e(String tag, String msg) {
        if (DEBUG)
            android.util.Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable ex) {
        if (DEBUG)
            android.util.Log.e(tag, msg, ex);
    }

    public static void d(String tag, String msg) {
        if (DEBUG)
            android.util.Log.d(tag, msg);
    }

    public static void d(String tag, String msg, Throwable ex) {
        if (DEBUG)
            android.util.Log.d(tag, msg, ex);
    }

    public static void v(String tag, String msg) {
        if (DEBUG)
            android.util.Log.v(tag, msg);
    }

    public static void v(String tag, String msg, Throwable ex) {
        if (DEBUG)
            android.util.Log.v(tag, msg, ex);
    }

    public static void w(String tag, String msg) {
        if (DEBUG)
            android.util.Log.w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable ex) {
        if (DEBUG)
            android.util.Log.w(tag, msg, ex);
    }
}