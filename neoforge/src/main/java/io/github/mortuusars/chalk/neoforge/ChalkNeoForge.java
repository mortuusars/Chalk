package io.github.mortuusars.chalk.neoforge;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.neoforge.loot.ConfigurableAddTableLootModifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

@Mod(Chalk.ID)
public class ChalkNeoForge {
    public ChalkNeoForge(ModContainer container) {
        Chalk.init();

        container.registerConfig(ModConfig.Type.SERVER, Config.Server.SPEC);
        container.registerConfig(ModConfig.Type.COMMON, Config.Common.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, Config.Client.SPEC);

        @Nullable IEventBus modEventBus = container.getEventBus();
        Preconditions.checkNotNull(modEventBus);
        LootModifiers.LOOT_MODIFIERS.register(modEventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ChalkNeoForgeClient.init(container);
        }
    }

    public static class LootModifiers {
        private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
              DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Chalk.ID);

        public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<ConfigurableAddTableLootModifier>> ADD_TABLE =
              LOOT_MODIFIERS.register("add_table", () -> ConfigurableAddTableLootModifier.CODEC);
    }
}