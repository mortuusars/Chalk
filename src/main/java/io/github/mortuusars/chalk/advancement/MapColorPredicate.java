package io.github.mortuusars.chalk.advancement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapColorPredicate {
    public static final String JSON_PROPERTY = "materialColorPredicate";
    public static final MapColorPredicate ANY = new MapColorPredicate(Collections.emptyList());
    private final List<MapColor> colors;

    public MapColorPredicate(List<MapColor> colors) {
        this.colors = colors;
    }

    public boolean matches(MapColor color) {
        if (colors.size() == 0)
            return true;

        for (MapColor dyeColor : colors) {
            if (color.equals(dyeColor))
                return true;
        }

        return false;
    }

    public static MapColorPredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull())
            return ANY;

        JsonObject jsonobject = GsonHelper.convertToJsonObject(json, JSON_PROPERTY);
        JsonArray colorsList = jsonobject.get("colors").getAsJsonArray();

        List<MapColor> colors = new ArrayList<>();

        for (JsonElement element : colorsList) {
            colors.add(MapColor.byId(element.getAsInt()));
        }

        return new MapColorPredicate(colors);
    }

    public JsonElement serializeToJson() {
        if (this == ANY)
            return JsonNull.INSTANCE;

        JsonArray colorsList = new JsonArray();
        for (MapColor color : this.colors) {
            colorsList.add(color.id);
        }

        JsonObject jsonobject = new JsonObject();
        jsonobject.add("colors", colorsList);
        return jsonobject;
    }
}
