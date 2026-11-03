package io.github.mortuusars.chalk.neoforge.datagen.generation;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.references.ItemIds;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Chalk.ID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(Chalk.Tags.Items.GLOWINGS)
                .add(ItemIds.GLOW_INK_SAC)
                .add(ItemIds.GLOWSTONE_DUST);
    }
}
