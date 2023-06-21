package io.github.mortuusars.chalk.advancement;

import com.google.gson.JsonObject;
import io.github.mortuusars.chalk.Chalk;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public class ChalkDrawTrigger extends SimpleCriterionTrigger<ChalkDrawTrigger.TriggerInstance> {
    private static final ResourceLocation ID = Chalk.resource("mark_drawn");

    @Override
    protected @NotNull TriggerInstance createInstance(JsonObject json, @NotNull ContextAwarePredicate predicate, @NotNull DeserializationContext context) {
        return new TriggerInstance(predicate, LocationPredicate.fromJson(json.get("location")),
                MapColorPredicate.fromJson(json.get(MapColorPredicate.JSON_PROPERTY)),
                DyeColorPredicate.fromJson(json.get(DyeColorPredicate.JSON_PROPERTY)));
    }

    public void trigger(ServerPlayer player, MapColor surfaceColor, DyeColor chalkColor) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player, surfaceColor, chalkColor));
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return ID;
    }


    @SuppressWarnings("unused")
    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final LocationPredicate location;
        private final MapColorPredicate surfaceColor;
        private final DyeColorPredicate chalkColor;

        public TriggerInstance(ContextAwarePredicate predicate, LocationPredicate location,
                               MapColorPredicate surfaceColor, DyeColorPredicate markColor) {
            super(ChalkDrawTrigger.ID, predicate);
            this.location = location;
            this.surfaceColor = surfaceColor;
            this.chalkColor = markColor;
        }

        public static TriggerInstance structure(ResourceKey<Structure> structureKey) {
            return new TriggerInstance(ContextAwarePredicate.ANY, LocationPredicate.inStructure(structureKey),
                    MapColorPredicate.ANY, DyeColorPredicate.ANY);
        }

        public static TriggerInstance structureAndLight(ResourceKey<Structure> structureKey, MinMaxBounds.Ints lightLevel) {
            return new TriggerInstance(ContextAwarePredicate.ANY, LocationPredicate.Builder.location()
                    .setStructure(structureKey)
                    .setLight(new LightPredicate.Builder().setComposite(lightLevel).build())
                    .build(),
                MapColorPredicate.ANY, DyeColorPredicate.ANY);
        }

        @Override
        public @NotNull JsonObject serializeToJson(@NotNull SerializationContext conditions) {
            JsonObject jsonObject = super.serializeToJson(conditions);
            jsonObject.add("location", location.serializeToJson());
            jsonObject.add(MapColorPredicate.JSON_PROPERTY, surfaceColor.serializeToJson());
            jsonObject.add(DyeColorPredicate.JSON_PROPERTY, chalkColor.serializeToJson());
            return jsonObject;
        }

        public boolean matches(ServerPlayer player, MapColor surfaceColor, DyeColor chalkColor) {
            return this.location.matches((ServerLevel) player.level(), player.position().x, player.position().y, player.position().z) &&
                this.surfaceColor.matches(surfaceColor) && this.chalkColor.matches(chalkColor);
        }
    }
}

