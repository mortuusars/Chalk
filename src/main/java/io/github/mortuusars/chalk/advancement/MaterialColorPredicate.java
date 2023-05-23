package io.github.mortuusars.chalk.advancement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.MaterialColor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MaterialColorPredicate {
    public static final String JSON_PROPERTY = "materialColorPredicate";
    public static final MaterialColorPredicate ANY = new MaterialColorPredicate(Collections.emptyList());
    private final List<MaterialColor> colors;

    public MaterialColorPredicate(List<MaterialColor> colors) {
        this.colors = colors;
    }

    public boolean matches(MaterialColor color) {
        if (colors.size() == 0)
            return true;

        for (MaterialColor dyeColor : colors) {
            if (color.equals(dyeColor))
                return true;
        }

        return false;
    }

    public static MaterialColorPredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull())
            return ANY;

        JsonObject jsonobject = GsonHelper.convertToJsonObject(json, JSON_PROPERTY);
        JsonArray colorsList = jsonobject.get("colors").getAsJsonArray();

        List<MaterialColor> colors = new ArrayList<>();

        for (JsonElement element : colorsList) {
            colors.add(MaterialColor.byId(element.getAsInt()));
        }

        return new MaterialColorPredicate(colors);
    }

    public JsonElement serializeToJson() {
        if (this == ANY)
            return JsonNull.INSTANCE;

        JsonArray colorsList = new JsonArray();
        for (MaterialColor color : this.colors) {
            colorsList.add(color.id);
        }

        JsonObject jsonobject = new JsonObject();
        jsonobject.add("colors", colorsList);
        return jsonobject;
    }
}
