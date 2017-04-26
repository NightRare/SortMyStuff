package nz.ac.aut.comp705.sortmystuff.data.local;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.data.Detail;

/**
 * Created by Vince on 2017/4/25.
 */

public interface IJsonHelper {

    Asset deserialiseAsset(final String assetId);

    Asset deserialiseRootAsset();

    List<Asset> deserialiseAllAssets();

    List<Detail> deserialiseDetails(final String assetId);

    boolean serialiseAsset(final Asset asset);

    boolean serialiseDetails(final List<Detail> details);

    boolean rootExists();

}
