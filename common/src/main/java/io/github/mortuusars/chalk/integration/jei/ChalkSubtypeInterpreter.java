package io.github.mortuusars.chalk.integration.jei;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public enum ChalkSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
    INSTANCE;

    @Override
    public @Nullable Object getSubtypeData(ItemStack ingredient, @NonNull UidContext context) {
        return ingredient.get(DataComponents.DYED_COLOR);
    }
}
