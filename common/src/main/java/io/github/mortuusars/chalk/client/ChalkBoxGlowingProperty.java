package io.github.mortuusars.chalk.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.mortuusars.chalk.world.item.ChalkBoxItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public record ChalkBoxGlowingProperty() implements SelectItemModelProperty<Boolean> {
    public static final MapCodec<ChalkBoxGlowingProperty> CODEC = MapCodec.unit(ChalkBoxGlowingProperty::new);
    public static final Type<ChalkBoxGlowingProperty, Boolean> TYPE = Type.create(CODEC, Codec.BOOL);

    @Override
    public Boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, @NonNull ItemDisplayContext context) {
        return stack.getItem() instanceof ChalkBoxItem chalkBox && chalkBox.shouldDrawGlowingMark(stack);
    }

    @Override
    public @NonNull Codec<Boolean> valueCodec() {
        return Codec.BOOL;
    }

    @Override
    public @NonNull Type<? extends SelectItemModelProperty<Boolean>, Boolean> type() {
        return TYPE;
    }
}
