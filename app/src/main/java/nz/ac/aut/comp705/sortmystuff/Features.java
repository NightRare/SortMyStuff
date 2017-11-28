package nz.ac.aut.comp705.sortmystuff;

import android.util.ArrayMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Features {

    public final boolean DevelopmentMode;
    public final boolean PhotoDetection;
    public final boolean DelayPhotoDetection;

    public Features() {
        PhotoDetection = false;
        DelayPhotoDetection = false;
        DevelopmentMode = false;
    }

    private Features(Map<String, Boolean> fields) {
        PhotoDetection = fields.get("PhotoDetection");
        DelayPhotoDetection = fields.get("DelayPhotoDetection");
        DevelopmentMode = fields.get("DevelopmentMode");
    }

    public static Features make(String... enabledFeatures) {
        List<String> enbaledFeatures = Arrays.asList(enabledFeatures);
        Map<String, Boolean> fields = new ArrayMap<>();

        for (Field field : Features.class.getFields()) {
            if (!field.getType().equals(boolean.class))
                continue;

            fields.put(field.getName(), enbaledFeatures.contains(field.getName()));
        }
        return new Features(fields);
    }

    public List<String> getEnabledFeatures() {
        List<String> enabledFeats = new ArrayList<>();
        for (Field field : getClass().getFields()) {
            if (!field.getType().equals(boolean.class))
                continue;

            boolean enabled = false;
            try {
                enabled = (boolean) field.get(this);
            } catch (IllegalAccessException ignored) {
            }
            if (enabled) {
                enabledFeats.add(field.getName());
            }
        }
        return enabledFeats;
    }

}
