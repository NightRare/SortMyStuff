package nz.ac.aut.comp705.sortmystuff.data.models;

import android.graphics.Bitmap;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.util.AppConstraints;

/**
 * Created by Yuan on 2017/5/27.
 */

public class Category {

    //region DATA FIELDS

    private String name;

    private List<Detail> details;

    //endregion

    //region STATIC FACTORY

    static Category create(String name) {
        checkIllegalName(name);
        List<Detail> details = new ArrayList<>();

        return new Category(name, details);
    }

    //region ACCESSORS

    public String getName() {
        return name;
    }

    @Deprecated
    public List<Detail> getDetails() {
        return new ArrayList<>(details);
    }

    //endregion

    //region MODIFIERS

    void setName(String name) {
        checkIllegalName(name);
        this.name = name;
    }

    boolean addDetail(Detail detail) {
        return hasLabel(detail.getLabel()) ?
                false : details.add(duplicateDetail(DUMMY_ASSET_ID, detail));
    }

    boolean removeDetail(String label) {
        for (Detail d : details) {
            if (d.getLabel().equals(label))
                return details.remove(d);
        }
        return false;
    }

    //endregion

    public List<Detail> generateDetails(String assetId) {
        Preconditions.checkNotNull(assetId);

        List<Detail> clone = new ArrayList<>();
        for (Detail d : details) {
            clone.add(duplicateDetail(assetId, d));
        }
        return clone;
    }

    //region PRIVATE STUFF

    private static final String DUMMY_ASSET_ID = "DUMMY_ASSET_ID";

    private Category(String name, List<Detail> details) {
        this.name = name;
        this.details = details;
    }

    private boolean hasLabel(String label) {
        for (Detail d : details) {
            if(d.getLabel().equals(label))
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
