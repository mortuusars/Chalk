package io.github.mortuusars.chalk.advancement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.DyeColor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DyeColorPredicate {
    public static final String JSON_PROPERTY = "colorPredicate";
    public static final DyeColorPredicate ANY = new DyeColorPredicate(Collections.emptyList());
    private final List<DyeColor> colors;

    public DyeColorPredicate(List<DyeColor> colors) {
        this.colors = colors;
    }

    public boolean matches(DyeColor color) {
        if (colors.size() == 0)
            return true;

        for (DyeColor dyeColor : colors) {
            if (color.equals(dyeColor))
                return true;
        }

        return false;
    }

    public static DyeColorPredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull())
            return ANY;

        JsonObject jsonobject = GsonHelper.convertToJsonObject(json, JSON_PROPERTY);
        JsonArray colorsList = jsonobject.get("colors").getAsJsonArray();

        List<DyeColor> colors = new ArrayList<>();

        for (JsonElement element : colorsList) {
            colors.add(DyeColor.byName(element.getAsString(), null));
        }

        return new DyeColorPredicate(colors);
    }

    public JsonElement serializeToJson() {
        if (this == ANY)
            return JsonNull.INSTANCE;

        JsonArray colorsList = new JsonArray();
        for (DyeColor color : this.colors) {
            colorsList.add(color.getSerializedName());
        }

        JsonObject jsonobject = new JsonObject();
        jsonobject.add("colors", colorsList);
        return jsonobject;
    }
}
