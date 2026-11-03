package io.github.mortuusars.chalk.client;

import com.mojang.serialization.MapCodec;
import io.github.mortuusars.chalk.world.item.ChalkBoxItem;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record ChalkBoxTintSource() implements ItemTintSource {
    public static final MapCodec<ChalkBoxTintSource> CODEC = MapCodec.unit(new ChalkBoxTintSource());

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        return stack.getItem() instanceof ChalkBoxItem chalkBox ? chalkBox.getTintColor(stack, 1) : -1;
    }

    @Override
    public @NonNull MapCodec<ChalkBoxTintSource> type() {
        return CODEC;
    }
}
