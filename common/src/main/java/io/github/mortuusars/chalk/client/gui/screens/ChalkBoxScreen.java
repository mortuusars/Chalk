package io.github.mortuusars.chalk.client.gui.screens;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.inventory.ChalkBoxMenu;
import io.github.mortuusars.chalk.world.item.component.ChalkBoxContents;
import io.github.mortuusars.mortaar.client.Minecrft;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ChalkBoxScreen extends AbstractInHandContainerScreen<ChalkBoxMenu> {
    public static final Identifier TEXTURE = Chalk.resource("textures/gui/container/chalk_box.png");
    public static final Identifier CHALK_SLOT_PLACEHOLDER_SPRITE = Chalk.resource("chalk_box/chalk_slot_placeholder");
    public static final Identifier GLOWING_SLOT_PLACEHOLDER_SPRITE = Chalk.resource("chalk_box/glowing_slot_placeholder");
    public static final Identifier GLOW_BAR_SPRITE = Chalk.resource("chalk_box/glow_bar");
    public static final Identifier SELECTED_CHALK_SLOT_OVERLAY_SPRITE = Chalk.resource("chalk_box/selected_chalk_slot_overlay");

    public static final int GLOW_BAR_HEIGHT = 18;

    protected final Player player;
    protected int selectedSlot;

    public ChalkBoxScreen(ChalkBoxMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE, 176, 166);
        this.player = playerInventory.player;
    }

    @Override
    protected void init() {
        this.inventoryLabelY = this.imageHeight - 94;
        super.init();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        selectedSlot = getMenu().getSelectedSlot();
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractBackgroundBase(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackgroundBase(graphics, mouseX, mouseY, partialTick);
        if (getMenu().isGlowEnabled()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 133, topPos + 34, imageWidth, 0, 25, 18, 256, 256);
            extractGlowingBar(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void extractSlotBackground(@NonNull GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, float partialTick) {
        super.extractSlotBackground(graphics, slot, mouseX, mouseY, partialTick);

        if (slot.index == selectedSlot) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SELECTED_CHALK_SLOT_OVERLAY_SPRITE, slot.x - 2, slot.y - 2, 20, 20);
        } else if (!slot.hasItem() && slot.index < ChalkBoxContents.CHALK_SLOTS) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CHALK_SLOT_PLACEHOLDER_SPRITE, slot.x - 1, slot.y - 1, 18, 18);
        } else if (getMenu().isGlowEnabled() && slot.index == ChalkBoxContents.GLOWINGS_SLOT && !slot.hasItem()) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, GLOWING_SLOT_PLACEHOLDER_SPRITE, slot.x - 1, slot.y - 1, 18, 18);
        }
    }

    protected void extractGlowingBar(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int currentGlow = getMenu().getGlow();
        int maxGlow = getMenu().getMaxGlow();
        int barSize = currentGlow == 0 ? 0
              : currentGlow == maxGlow ? 18
              : 2 + Math.round((currentGlow - 1) * 14f / (maxGlow - 2));
        int glowingBarFillLevel = Math.min(GLOW_BAR_HEIGHT, barSize);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, GLOW_BAR_SPRITE, 5, 18, 0, 18 - glowingBarFillLevel,
              leftPos + 153, topPos + 34 + 18 - glowingBarFillLevel, 5, glowingBarFillLevel);
    }

    @Override
    protected void extractTooltip(@NonNull GuiGraphicsExtractor graphics, int x, int y) {
        if (getMenu().isGlowEnabled()
              && x >= leftPos + 151 && x <= leftPos + 160
              && y >= topPos + 34 && y <= topPos + 51) {
            int glow = getMenu().getGlow();
            ChatFormatting glowAmountColor = glow > 0
                  ? ChatFormatting.GOLD
                  : ChatFormatting.GRAY;
            graphics.setTooltipForNextFrame(font, Component.translatable("item.chalk.chalk_box.tooltip.glow",
                  Component.literal(Integer.toString(glow)).withStyle(glowAmountColor)), x, y);
            return;
        }

        super.extractTooltip(graphics, x, y);
    }

    @Override
    protected @NotNull List<Component> getTooltipFromContainerItem(@NonNull ItemStack stack) {
        List<Component> lines = super.getTooltipFromContainerItem(stack);
        if (hoveredSlot != null && hoveredSlot.index < ChalkBoxContents.CHALK_SLOTS && hoveredSlot.getItem().equals(stack)) {
            lines = new ArrayList<>(lines);
            lines.add(Component.translatable("item.chalk.chalk_box.tooltip.alt_select"));
        }

        return lines;
    }

    @Override
    protected void slotClicked(@NonNull Slot slot, int slotId, int buttonNum, @NonNull ContainerInput containerInput) {
        if (Minecrft.get().hasAltDown() && slotId >= 0 && slotId < ChalkBoxContents.CHALK_SLOTS && slot.hasItem()) {
            getMenu().setSelectedSlot(slotId);
            slotId += 100;
            assert minecraft.gameMode != null;
            minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, slotId);
            return;
        }
        super.slotClicked(slot, slotId, buttonNum, containerInput);
    }
}
