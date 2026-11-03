package io.github.mortuusars.chalk.client.gui.tooltip;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.client.gui.screens.ChalkBoxScreen;
import io.github.mortuusars.chalk.world.item.component.ChalkBoxContents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class ClientChalkBoxTooltip implements ClientTooltipComponent {
    public static final Identifier SLOTS_SPRITE = Chalk.resource("chalk_box/tooltip_slots");
    public static final Identifier GLOWING_SPRITE = Chalk.resource("chalk_box/tooltip_glowing");

    public static final int ROWS = 3;
    public static final int COLUMNS = 3;

    protected final ChalkBoxContents contents;

    public ClientChalkBoxTooltip(ChalkBoxContents contents) {
        this.contents = contents;
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return backgroundWidth();
    }

    @Override
    public int getHeight(@NotNull Font font) {
        return backgroundHeight() + 3;
    }

    protected int backgroundWidth() {
        return shouldShowGlow() ? 86 : 58;
    }

    protected int backgroundHeight() {
        return 60;
    }

    protected boolean shouldShowGlow() {
        return Config.Server.CHALK_BOX_GLOWING_ENABLED.get()
              && (contents.glow() > 0 || !contents.items().get(ChalkBoxContents.GLOWINGS_SLOT).isEmpty());
    }

    @Override
    public void extractText(@NonNull GuiGraphicsExtractor graphics, @NonNull Font font, int x, int y) {
        ClientTooltipComponent.super.extractText(graphics, font, x, y);
    }

    @Override
    public void extractImage(@NonNull Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOTS_SPRITE, x, y, 58, 60);

        int selectedIndex = contents.selected();
        int index = 0;
        for (int column = 0; column < COLUMNS; column++) {
            for (int row = 0; row < ROWS; row++) {
                int slotX = x + row * 18 + 2;
                int slotY = y + column * 18 + 3;

                if (index == selectedIndex) {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ChalkBoxScreen.SELECTED_CHALK_SLOT_OVERLAY_SPRITE, slotX - 1, slotY - 1, 20, 20);
                }

                if (contents.getItem(index).isEmpty()) {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ChalkBoxScreen.CHALK_SLOT_PLACEHOLDER_SPRITE, slotX, slotY, 18, 18);
                }

                extractSlotItem(font, graphics, index, slotX, slotY);

                index++;
            }
        }

        if (shouldShowGlow()) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, GLOWING_SPRITE, x + 58, y + 18, 28, 24);

            if (contents.getItem(index).isEmpty()) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ChalkBoxScreen.GLOWING_SLOT_PLACEHOLDER_SPRITE, x + 59, y + 21, 18, 18);
            }

            extractSlotItem(font, graphics, index, x + 59, y + 21);

            if (contents.glow() > 0) {
                int currentGlow = contents.glow();
                int maxGlow = Config.Server.CHALK_BOX_GLOWING_AMOUNT_PER_ITEM.get();
                int barSize = currentGlow == maxGlow ? 18 : 2 + Math.round((currentGlow - 1) * 14f / (maxGlow - 2));
                int glowingBarFillLevel = Math.min(ChalkBoxScreen.GLOW_BAR_HEIGHT, barSize);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ChalkBoxScreen.GLOW_BAR_SPRITE, 5, 18, 0, 18 - glowingBarFillLevel,
                      x + 79, y + 21 + 18 - glowingBarFillLevel, 5, glowingBarFillLevel);
            }
        }
    }

    protected void extractSlotItem(Font font, GuiGraphicsExtractor graphics, int index, int x, int y) {
        ItemStack itemstack = this.contents.items().get(index);
        graphics.item(itemstack, x + 1, y + 1, index);
        graphics.itemDecorations(font, itemstack, x + 1, y + 1);
    }
}
