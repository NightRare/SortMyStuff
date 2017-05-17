package nz.ac.aut.comp705.sortmystuff.data.local;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.data.models.Detail;

/**
 * IFileHelper is responsible for serialising/deserialising objects to/from files stored in
 * local storage.
 *
 * @author Yuan
 */

public interface IFileHelper {

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
     * Serialised an asset and write the data to the corresponding json file. If Root asset does
     * not exist, the asset won't be serialised.
     *
     * @param asset the asset to be serialised
     * @return true if asset is serialised successfully
     * @throws NullPointerException if asset is {@code null}
     */
    boolean serialiseAsset(final Asset asset);

    /**
     * Serialised a list of details and write the data to the corresponding json file
     * and image files.
     * <p>
     * If imageUpdated is set to false, then only the details json file will be updated while
     * the linked image files will not be updated.
     * <p>
     * Details inside this list must belong to the same asset.
     * <p>
     * It returns false in the case that empty details list is given.
     *
     * @param details      the list of details to be serialised
     * @param imageUpdated true if the field of any ImageDetail has been updated
     * @return true if asset is serialised successfully
     * @throws NullPointerException if asset is {@code null}
     */
    boolean serialiseDetails(final List<Detail> details, boolean imageUpdated);

    /**
     * Checks if Root asset exists.
     *
     * @return true if Root asset exists.
     */
    boolean rootExists();
}
