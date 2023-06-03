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
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.NotNull;

public class ChalkDrawTrigger extends SimpleCriterionTrigger<ChalkDrawTrigger.TriggerInstance> {
    private static final ResourceLocation ID = Chalk.resource("mark_drawn");

    @Override
    protected @NotNull TriggerInstance createInstance(JsonObject json, EntityPredicate.@NotNull Composite player,
                                                      @NotNull DeserializationContext conditionsParser) {
        return new TriggerInstance(player, LocationPredicate.fromJson(json.get("location")),
                MaterialColorPredicate.fromJson(json.get(MaterialColorPredicate.JSON_PROPERTY)),
                DyeColorPredicate.fromJson(json.get(DyeColorPredicate.JSON_PROPERTY)));
    }

    public void trigger(ServerPlayer player, MaterialColor surfaceColor, DyeColor chalkColor) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player, surfaceColor, chalkColor));
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return ID;
    }

    @SuppressWarnings("unused")
    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final LocationPredicate location;
        private final MaterialColorPredicate surfaceColor;
        private final DyeColorPredicate chalkColor;

        public TriggerInstance(EntityPredicate.Composite player, LocationPredicate location,
                               MaterialColorPredicate surfaceColor, DyeColorPredicate markColor) {
            super(ChalkDrawTrigger.ID, player);
            this.location = location;
            this.surfaceColor = surfaceColor;
            this.chalkColor = markColor;
        }

        public static TriggerInstance structure(ResourceKey<Structure> structureKey) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, LocationPredicate.inStructure(structureKey),
                    MaterialColorPredicate.ANY, DyeColorPredicate.ANY);
        }

        public static TriggerInstance structureAndLight(ResourceKey<Structure> structureKey, MinMaxBounds.Ints lightLevel) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, LocationPredicate.Builder.location()
                    .setStructure(structureKey)
                    .setLight(new LightPredicate.Builder().setComposite(lightLevel).build())
                    .build(),
                MaterialColorPredicate.ANY, DyeColorPredicate.ANY);
        }

        @Override
        public @NotNull JsonObject serializeToJson(@NotNull SerializationContext conditions) {
            JsonObject jsonObject = super.serializeToJson(conditions);
            jsonObject.add("location", location.serializeToJson());
            jsonObject.add(MaterialColorPredicate.JSON_PROPERTY, surfaceColor.serializeToJson());
            jsonObject.add(DyeColorPredicate.JSON_PROPERTY, chalkColor.serializeToJson());
            return jsonObject;
        }

        public boolean matches(ServerPlayer player, MaterialColor surfaceColor, DyeColor chalkColor) {
            return this.location.matches((ServerLevel) player.level, player.position().x, player.position().y, player.position().z) &&
                this.surfaceColor.matches(surfaceColor) && this.chalkColor.matches(chalkColor);
        }
    }
}

