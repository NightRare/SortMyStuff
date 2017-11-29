package nz.ac.aut.comp705.sortmystuff.data.models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public class Category implements ICategory {

    //region FIELD NAMES

    public static final String CATEGORY_NAME = "name";
    public static final String CATEGORY_DETAILS = "details";

    //endregion

    //region DATA FIELDS

    /**
     * Unique identifier of the category
     */
    private String name;

    private List<Detail> details;

    //endregion

    //region CONSTRUCTORS

    public Category() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    //endregion

    //region TRANSFORMERS

    @Exclude
    public static Category fromMap(Map<String, Object> members) {
        String name = (String) members.get(CATEGORY_NAME);
        List<Map> detailObjects = (List<Map>) members.get(CATEGORY_DETAILS);
        if(detailObjects == null) detailObjects = new ArrayList<>();

        List<Detail> details = new ArrayList<>();
        for (Map<String, Object> dMember : detailObjects) {
            Detail detail = Detail.fromMap(dMember);
            if (detail != null)
                details.add(detail);
        }
        return new Category(name, details);
    }

    //endregion

    //region ACCESSORS

    /**
     * Gets the name of the category.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    //endregion

    //region OTHER METHODS

    public void setDefaultFieldValue(String defaultTextValue, String defaultImageValue) {
        for (Detail detail : details) {
            if (detail.getType().equals(DetailType.Text) || detail.getType().equals(DetailType.Date)) {
                detail.setFieldData(defaultTextValue, true);

            } else if (detail.getType().equals(DetailType.Image)) {
                detail.setFieldData(defaultImageValue, true);
            }
        }
    }

    /**
     * Generates a list of details for the Asset whose id is as the given argument. The details
     * are cloned from the template list only with a different assetid.
     *
     * @param assetId the id of the owner asset
     * @return the lit of details
     * @throws NullPointerException if assetId is {@code null}
     */
    @Exclude
    public List<Detail> generateDetails(String assetId) {
        checkNotNull(assetId);

        List<Detail> clone = new ArrayList<>();
        for (Detail templateDetail : details) {
            Detail detail = Detail.createDetail(assetId, templateDetail.getType(), templateDetail.getLabel(), templateDetail.getFieldData());
            detail.setPosition(templateDetail.getPosition());
            if (detail != null)
                clone.add(detail);
        }
        return clone;
    }

    //endregion

    //region PRIVATE STUFF

    @Exclude
    private static final String DUMMY_ASSET_ID = "DUMMY_ASSET_ID";

    private Category(String name, List<Detail> details) {
        this.name = name;
        this.details = details;
    }

    //endregion
}
