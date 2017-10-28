package nz.ac.aut.comp705.sortmystuff.data.models;

import android.graphics.Bitmap;

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

public class FCategory implements ICategory {

    //region FIELD NAMES

    public static final String CATEGORY_NAME = "name";
    public static final String CATEGORY_DETAILS = "details";

    //endregion

    //region DATA FIELDS

    /**
     * Unique identifier of the category
     */
    private String name;

    private List<FDetail> details;

    //endregion

    //region CONSTRUCTORS

    public FCategory() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    //endregion

    //region TRANSFORMERS

    @Exclude
    public static FCategory fromMap(Map<String, Object> members) {
        String name = (String) members.get(CATEGORY_NAME);
        List<Map> detailObjects = (List<Map>) members.get(CATEGORY_DETAILS);
        if(detailObjects == null) detailObjects = new ArrayList<>();

        List<FDetail> details = new ArrayList<>();
        for (Map<String, Object> dMember : detailObjects) {
            FDetail detail = FDetail.fromMap(dMember);
            if (detail != null)
                details.add(detail);
        }
        return new FCategory(name, details);
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

    public void setDefaultFieldValue(String defaultTextValue, Bitmap defaultImageValue) {
        for (FDetail detail : details) {
            if (detail.getType().equals(DetailType.Text) || detail.getType().equals(DetailType.Date)) {
                detail.setField(defaultTextValue, true);

            } else if (detail.getType().equals(DetailType.Image)) {
                detail.setField(defaultImageValue, true);
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
    public List<FDetail> generateDetails(String assetId) {
        checkNotNull(assetId);

        List<FDetail> clone = new ArrayList<>();
        for (FDetail d : details) {
            FDetail fDetail = duplicateDetail(assetId, d);
            if (fDetail != null)
                clone.add(fDetail);
        }
        return clone;
    }

    //endregion

    //region PRIVATE STUFF

    @Exclude
    private static final String DUMMY_ASSET_ID = "DUMMY_ASSET_ID";

    private FCategory(String name, List<FDetail> details) {
        this.name = name;
        this.details = details;
    }

    @Exclude
    private FDetail duplicateDetail(String assetId, FDetail detail) {
        FDetail output = null;
        DetailType type = detail.getType();
        if (type.equals(DetailType.Image)) {
            output = FDetail.createImageDetail(assetId, detail.getLabel(), (Bitmap) detail.getField());
            output.setPosition(detail.getPosition());

        } else if (type.equals(DetailType.Date)) {
            output = FDetail.createDateDetail(assetId, detail.getLabel(), (String) detail.getField());
            output.setPosition(detail.getPosition());

        } else if (type.equals(DetailType.Text)) {
            output = FDetail.createTextDetail(assetId, detail.getLabel(), (String) detail.getField());
            output.setPosition(detail.getPosition());
        }
        return output;
    }

    //endregion
}
