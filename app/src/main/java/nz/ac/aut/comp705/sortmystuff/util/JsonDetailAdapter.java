package nz.ac.aut.comp705.sortmystuff.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import nz.ac.aut.comp705.sortmystuff.data.Detail;
import nz.ac.aut.comp705.sortmystuff.data.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.TextDetail;

/**
 * Created by Yuan on 2017/4/25.
 */


public class JsonDetailAdapter implements JsonSerializer<Detail>, JsonDeserializer<Detail> {

    private static final String TYPE = "type";
    private static final Map<String, Class<?>> classes = mapClasses();

    private static Map<String, Class<?>> mapClasses() {
        Map<String, Class<?>> map = new HashMap<>();

        map.put(DetailType.Text.toString(), TextDetail.class);

        return map;
    }

    @Override
    public Detail deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        JsonPrimitive prim = (JsonPrimitive) jsonObject.get(TYPE);
        Class<?> klass = classes.get(prim.getAsString());
        return context.deserialize(jsonObject, klass);
    }

    @Override
    public JsonElement serialize(Detail src, Type typeOfSrc, JsonSerializationContext context) {
        switch(src.getType()) {
            case Text:
                TextDetail td = (TextDetail) src;
                return context.serialize(td, TextDetail.class);
            default:
                return context.serialize(src, Detail.class);
        }
    }
}