package nz.ac.aut.comp705.sortmystuff.data.models;

import android.graphics.Bitmap;

import com.google.common.base.Preconditions;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.utils.AppConstraints;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An asset with a particular category has a certain (default) types of details.
 * Each category has a name and a list of template details.
 * <p>
 * A list of Details can be generated once the owner Asset is given to the Category by calling
 * {@link #generateDetails(String)}.
 * <p>
 * Created by Yuan on 2017/5/27.
 */

public class Category {

    //region DATA FIELDS

    private String name;

    private List<Detail> details;

    //endregion

    //region STATIC FACTORY

    /**
     * Static factory to create a Category whose name is as given.
     *
     * @param name the name of the category
     * @return the Category instance
     * @throws NullPointerException     if name is {@code null}
     * @throws IllegalArgumentException if name if whitespaces, empty, or exceeds the length
     *                                  {@link AppConstraints#CATEGORY_NAME_CAP}
     */
    public static Category create(String name) {
        checkIllegalName(name);
        List<Detail> details = new ArrayList<>();

        return new Category(name, details);
    }

    //endregion

    //region ACCESSORS

    /**
     * Gets the name of the category.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
     * Gets the shallow clone list of template details.
     *
     * @return the list of template details
     */
    @Deprecated
    public List<Detail> getDetails() {
        return new ArrayList<>(details);
    }

    //endregion

    //region MODIFIERS

    /**
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
     * Sets the name of the category.
     *
     * @param name the name of the category
     * @throws NullPointerException     if name is {@code null}
     * @throws IllegalArgumentException if name if whitespaces, empty, or exceeds the length
     *                                  {@link AppConstraints#CATEGORY_NAME_CAP}
     */
    @Deprecated
    public void setName(String name) {
        checkIllegalName(name);
        this.name = name;
    }

    /**
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
     * Adds the detail to the list of template details.
     *
     * @param detail the detail to be added
     * @return true if add successfully
     */
    @Deprecated
    public boolean addDetail(Detail detail) {
        return hasLabel(detail.getLabel()) ?
                false : details.add(duplicateDetail(DUMMY_ASSET_ID, detail));
    }

    /**
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
     * Removes the detail whose label is as given from the list of template details.
     *
     * @param label the label of the detail which needs to be removed
     * @return true if remove successfully
     */
    @Deprecated
    public boolean removeDetail(String label) {
        for (Detail d : details) {
            if (d.getLabel().equals(label))
                return details.remove(d);
        }
        return false;
    }

    //endregion

    /**
     * <p><em>
     * Annotated with Deprecated to prevent invocation outside {@link nz.ac.aut.comp705.sortmystuff.data} package.
     * </em></p>
     * <p>
     * <p>
     * Generates a list of details for the Asset whose id is as the given argument. The details
     * are cloned from the template list only with a different assetid.
     *
     * @param assetId the id of the owner asset
     * @return the lit of details
     * @throws NullPointerException if asssetId is {@code null}
     */
    @Deprecated
    public List<Detail> generateDetails(String assetId) {
        Preconditions.checkNotNull(assetId);

        List<Detail> clone = new ArrayList<>();
        for (Detail d : details) {
            clone.add(duplicateDetail(assetId, d));
        }
        return clone;
    }

    @Exclude
    public List<FDetail> generateFDetails(String assetId) {
        checkNotNull(assetId);

        List<FDetail> clone = new ArrayList<>();

        int index = 0;
        for (Detail d : details) {
            FDetail fDetail = FDetail.fromDetail(duplicateDetail(assetId, d));
            fDetail.setPosition(index);
            clone.add(fDetail);
            index++;
        }
        return clone;
    }

    //region PRIVATE STUFF

    private static final String DUMMY_ASSET_ID = "DUMMY_ASSET_ID";

    private Category(String name, List<Detail> details) {
        this.name = name;
        this.details = details;
    }

    /**
     * Returns true if there is already a detail with the same label in template list.
     *
     * @param label
     * @return
     */
    private boolean hasLabel(String label) {
        for (Detail d : details) {
            if (d.getLabel().equals(label))
                return true;
        }
        return false;
    }

    private static void checkIllegalName(String name) {
        Preconditions.checkNotNull(name);
        if (name.isEmpty())
            throw new IllegalArgumentException("cannot be empty");
        if (name.length() > AppConstraints.CATEGORY_NAME_CAP)
            throw new IllegalArgumentException("string length exceeds cap");
    }

    private static Detail duplicateDetail(String assetId, Detail detail) {
        if (detail instanceof ImageDetail) {
            return ImageDetail.create(assetId, detail.getLabel(), (Bitmap) detail.getField());
        }

        if (detail instanceof TextDetail) {
            if (detail.getType().equals(DetailType.Date))
                return TextDetail.createDateDetail(assetId, detail.getLabel(), (String) detail.getField());

            if (detail.getType().equals(DetailType.Text))
                return TextDetail.createTextDetail(assetId, detail.getLabel(), (String) detail.getField());
        }
        return null;
    }

    //endregion
}
