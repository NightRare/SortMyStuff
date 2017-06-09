package nz.ac.aut.comp705.sortmystuff.data.models;

/**
 * This is an enum for all built-in category names.
 * <p>
 * Created by Yuan on 2017/5/28.
 */

public enum CategoryType {
    None,

    Appliances,
    Books,
    Collectibles,
    Food,
    Miscellaneous,
    Places;

    /**
     * A class defining the labels of the basic details.
     * <p>
     * This should remain consistent with what has been defined in 'assets/categories.json'.
     */
    public class BasicDetail {
        public final static String PHOTO = "Photo";
        public final static String NOTES = "Notes";
    }
}
