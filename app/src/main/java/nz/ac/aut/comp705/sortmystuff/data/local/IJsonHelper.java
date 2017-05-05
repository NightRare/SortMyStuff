package nz.ac.aut.comp705.sortmystuff.data.local;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.data.Detail;

/**
 * IJsonHelper is responsible for serialising/deserialising objects to/from json files stored in
 * local storage.
 *
 * @author Yuan
 */

public interface IJsonHelper {

    /**
     * Deserialise an asset from the json file. Root asset cannot be deserilised
     * via this method, use {@link #deserialiseRootAsset()} instead.
     *
     * @param assetId the id of the asset
     * @return the asset object; or null if the json file does not exist, or root asset
     * does not exist
     * @throws NullPointerException if assetId is {@code null}
     */
    Asset deserialiseAsset(final String assetId);

    /**
     * Deserialise the Root asset from the json file.
     *
     * @return the Root asset; or null if root asset does not exist
     */
    Asset deserialiseRootAsset();

    /**
     * Deserialise all the assets, including the recycled ones from json files.
     *
     * @return the list of all assets; or null if root asset does not exist
     */
    List<Asset> deserialiseAllAssets();

    /**
     * Deserialise the list of details of an asset.
     *
     * @param assetId the id of the owner asset
     * @return the list of details; or null if no such asset, or if json file does not exist
     * @throws NullPointerException if assetId is {@code null}
     */
    List<Detail> deserialiseDetails(final String assetId);

    /**
     * Serialised an asset and write the data to the corresponding json file
     *
     * @param asset the asset to be serialised
     * @return true if asset is serialised successfully
     * @throws NullPointerException if asset is {@code null}
     */
    boolean serialiseAsset(final Asset asset);

    /**
     * Serialised a list of details and write the data to the corresponding json file.
     * Details inside this list must belong to the same asset.
     *
     * @param details the list of details to be serialised
     * @return true if asset is serialised successfully
     * @throws NullPointerException if asset is {@code null}
     */
    boolean serialiseDetails(final List<Detail> details);

    /**
     * Checks if Root asset exists.
     *
     * @return true if Root asset exists.
     */
    boolean rootExists();

}
