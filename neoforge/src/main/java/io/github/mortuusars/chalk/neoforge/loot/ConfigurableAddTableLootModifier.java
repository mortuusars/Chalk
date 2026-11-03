package io.github.mortuusars.chalk.neoforge.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.neoforge.ChalkNeoForge;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Basically a copy of AddTableLootModifier just to allow disabling it through config.
 */
public class ConfigurableAddTableLootModifier extends AddTableLootModifier {
    public static final MapCodec<ConfigurableAddTableLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance)
            .and(ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("table").forGetter(ConfigurableAddTableLootModifier::table))
            .apply(instance, ConfigurableAddTableLootModifier::new));

    public ConfigurableAddTableLootModifier(Optional<Holder<LootItemCondition>> condition, int priority, ResourceKey<LootTable> table) {
        super(condition, priority, table);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context) {
        if (!Config.Common.LOOT.get()) {
            return generatedLoot;
        }

        return super.doApply(generatedLoot, context);
    }

    @Override
    public @NotNull MapCodec<? extends IGlobalLootModifier> codec() {
        return ChalkNeoForge.LootModifiers.ADD_TABLE.get();
    }
}
