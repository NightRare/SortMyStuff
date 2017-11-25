package nz.ac.aut.comp705.sortmystuff.ui.adding;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;
import nz.ac.aut.comp705.sortmystuff.ui.IPresenter;

public interface IAddingAssetPresenter extends IPresenter{

    void addingAsset(@Nullable Bitmap photo);

    void updateAssetName(@Nullable Bitmap photo);

    void selectCategory(CategoryType category);

    void createAsset(
            String name,
            CategoryType category,
            Bitmap photo,
            List<IDetail> details);

    void resetPhoto();
}
