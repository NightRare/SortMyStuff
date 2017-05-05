package nz.ac.aut.comp705.sortmystuff.testutils;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.data.Detail;
import nz.ac.aut.comp705.sortmystuff.data.DetailType;

/**
 * Created by Yuan on 2017/5/6.
 */

public class TestUtil {

    public static boolean areIdenticalAssets(Asset asset1, Asset asset2) {
        return areIdenticalAssets(asset1, asset2.getId(), asset2.getName(), asset2.getContainerId(),
                asset2.getCreateTimestamp(), asset2.getModifyTimestamp());
    }

    public static boolean areIdenticalAssets(Asset asset, String id, String name,
                                       String containerId, Long createTS, Long modifyTS) {
        if (id != null) {
            if (!asset.getId().equals(id))
                return false;
        }

        if (name != null) {
            if (!asset.getName().equals(name))
                return false;
        }

        if (containerId != null) {
            if (!asset.getContainerId().equals(containerId))
                return false;
        }

        if (createTS != null) {
            if (!asset.getCreateTimestamp().equals(createTS))
                return false;
        }

        if (modifyTS != null) {
            if (!asset.getModifyTimestamp().equals(modifyTS))
                return false;
        }

        return true;
    }

    public static boolean areIdenticalAssets(List<Asset> list1, List<Asset> list2) {
        if (list1.size() != list2.size())
            return false;

        for (Asset a1 : list1) {
            boolean included = false;
            for (Asset a2 : list2) {
                if (areIdenticalAssets(a1, a2))
                    included = true;
            }
            // if a1 is not included in a2
            if (!included)
                return false;
        }
        return true;
    }

    public static boolean areIdenticalDetails(Detail detail1, Detail detail2) {
        return areIdenticalDetails(detail1, detail2.getId(), detail2.getAssetId(),
                detail2.getType(), detail2.getLabel(), detail2.getField());
    }

    public static boolean areIdenticalDetails(Detail detail, String id, String assetId,
                                        DetailType type, String label, Object field) {
        if (id != null) {
            if (!detail.getId().equals(id))
                return false;
        }

        if (assetId != null) {
            if (!detail.getAssetId().equals(assetId))
                return false;
        }

        if (type != null) {
            if (!detail.getType().equals(type))
                return false;
        }

        if (label != null) {
            if (!detail.getLabel().equals(label))
                return false;
        }

        if (field != null) {
            if (!detail.getField().equals(field))
                return false;
        }

        return true;
    }

    public static boolean areIdenticalDetails(List<Detail> list1, List<Detail> list2) {
        if (list1.size() != list2.size())
            return false;

        for (Detail d1 : list1) {
            boolean included = false;
            for (Detail d2 : list2) {
                if (areIdenticalDetails(d1, d2))
                    included = true;
            }
            // if a1 is not included in a2
            if (!included)
                return false;
        }
        return true;
    }

}