package nz.ac.aut.comp705.sortmystuff.utils;

import static nz.ac.aut.comp705.sortmystuff.utils.AppStrings.ASSET_DEFAULT_NAME;

public class AssetNamingHelper {

    public static boolean conformsToDefaultNamingScheme(String name) {
        if (name == null) return false;
        String subject = name.trim();

        return subject.contains(ASSET_DEFAULT_NAME) &&
                subject.startsWith(ASSET_DEFAULT_NAME) &&
                (subject.equals(ASSET_DEFAULT_NAME) ||
                        isNonNegativeInteger(subject.substring(ASSET_DEFAULT_NAME.length()).trim()));
    }

    private static boolean isNonNegativeInteger(String input) {
        try {
            int value = Integer.parseInt(input);
            return value >= 0;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}
