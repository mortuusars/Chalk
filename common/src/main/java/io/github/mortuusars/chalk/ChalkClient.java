package io.github.mortuusars.chalk;

import io.github.mortuusars.chalk.client.gui.tooltip.ClientChalkBoxTooltip;
import io.github.mortuusars.chalk.world.item.component.ChalkBoxContents;
import io.github.mortuusars.mortaar.client.gui.tooltip.Tooltips;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ChalkClient {
    public static void init() {
        ItemModelOverrides.register();
        Tooltips.register(ChalkBoxContents.class, ClientChalkBoxTooltip::new);
    }

    // --

    public static class ItemModelOverrides {
        public static final Identifier GLOWING_PROPERTY = Chalk.resource("glowing");

        public static void register() {
//            ItemProperties.register(Chalk.Items.CHALK_BOX.get(), GLOWING_PROPERTY, ItemModelOverrides::isChalkBoxGlowing);
        }

        public static float isChalkBoxGlowing(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            return Chalk.Items.CHALK_BOX.get().shouldDrawGlowingMark(stack) ? 1F : 0F;
        }
    }
}
