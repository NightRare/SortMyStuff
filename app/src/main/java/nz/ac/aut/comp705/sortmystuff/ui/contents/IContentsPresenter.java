package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;

import java.util.ArrayList;

import nz.ac.aut.comp705.sortmystuff.ui.IPresenter;

/**
 * Created by Yuan on 2017/4/24.
 */

public interface IContentsPresenter extends IPresenter {

    void start();

    void loadAssetList(String assetID);

    ArrayList getRootContents();

    ArrayList getContentsOf(String assetID);

    void addAsset(String assetName, String containerID);

    String getRoot();
}
