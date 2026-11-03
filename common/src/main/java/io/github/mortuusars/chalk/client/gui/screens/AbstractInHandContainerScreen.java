package io.github.mortuusars.chalk.client.gui.screens;

import io.github.mortuusars.chalk.world.inventory.AbstractInHandContainerMenu;
import io.github.mortuusars.chalk.world.inventory.slot.DisabledSlot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.NonNull;

public class AbstractInHandContainerScreen<T extends AbstractInHandContainerMenu> extends AbstractContainerScreen<T> {
    private final Identifier texture;

    public AbstractInHandContainerScreen(T menu, Inventory playerInventory, Component title, Identifier texture, int imageWidth, int imageHeight) {
        super(menu, playerInventory, title, imageWidth, imageHeight);
        this.texture = texture;
    }

    public Identifier getTexture() {
        return texture;
    }

    @Override
    protected void init() {
        super.init();
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        updateButtons();
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos, topPos);
        for (Slot slot : getMenu().slots) {
            extractSlotOverlay(graphics, slot, mouseX, mouseY, partialTick);
        }
        graphics.pose().popMatrix();
        extractTooltip(graphics, mouseX, mouseY);
    }

    protected void updateButtons() {

    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        extractBackgroundBase(graphics, mouseX, mouseY, partialTick);

        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos, topPos);
        for (Slot slot : getMenu().slots) {
            extractSlotBackground(graphics, slot, mouseX, mouseY, partialTick);
        }
        graphics.pose().popMatrix();
    }

    protected void extractBackgroundBase(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    protected void extractSlotBackground(@NonNull GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, float partialTick) {

    }

    protected void extractSlotOverlay(@NonNull GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, float partialTick) {
        if (shouldShowDisabledOverlayOver(slot)) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, DisabledSlot.OVERLAY_SPRITE, slot.x - 1, slot.y - 1, 18, 18);
        }
    }

    protected boolean shouldShowDisabledOverlayOver(Slot slot) {
        return slot instanceof DisabledSlot;
    }
}
