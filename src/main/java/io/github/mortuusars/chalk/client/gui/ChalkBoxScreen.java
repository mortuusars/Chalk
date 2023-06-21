package io.github.mortuusars.chalk.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.config.Config;
import io.github.mortuusars.chalk.menus.ChalkBoxMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class ChalkBoxScreen extends AbstractContainerScreen<ChalkBoxMenu> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(Chalk.ID, "textures/gui/chalk_box.png");
    private static final int GLOWING_BAR_WIDTH = 72;
    private final int maxGlowingUses;

    public ChalkBoxScreen(ChalkBoxMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        maxGlowingUses = Config.CHALK_BOX_GLOWING_USES.get();

        this.minecraft = Minecraft.getInstance();
    }

    @Override
    protected void init() {
        this.imageWidth = 176;
        this.imageHeight = 180;
        this.inventoryLabelY = this.imageHeight - 94;
        super.init();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (menu.chalkBoxCoords != null) {
            graphics.renderItem(menu.chalkBoxStack,getGuiLeft() + menu.chalkBoxCoords.getFirst(),getGuiTop() + menu.chalkBoxCoords.getSecond());
        }

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, 0, 200);
        graphics.fill(getGuiLeft() + menu.chalkBoxCoords.getFirst() - 1, getGuiTop() + menu.chalkBoxCoords.getSecond() - 1,
                getGuiLeft() + menu.chalkBoxCoords.getFirst() + 17, getGuiTop() + menu.chalkBoxCoords.getSecond() + 17,
                0x20c8c8c8);
        poseStack.popPose();

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, getGuiLeft(), getGuiTop(), 0,0, imageWidth, imageHeight);

        if (Config.CHALK_BOX_GLOWING.get()){
            // Chalk Slots
            graphics.blit(TEXTURE, getGuiLeft() + 52, getGuiTop() + 17, 0, 180, 72, 36);

            // Bar + Slot
            graphics.blit(TEXTURE, getGuiLeft() + 52, getGuiTop() + 57, 0, 217, 72, 28);

            int barSize = (int)Math.ceil((Math.min(menu.getGlowingUses(), maxGlowingUses) / (float) maxGlowingUses) * GLOWING_BAR_WIDTH);
            int glowingBarFillLevel = Math.min(GLOWING_BAR_WIDTH, barSize);

            // Fill
            graphics.blit(TEXTURE, getGuiLeft() + 52, getGuiTop() + 57, 72, 217, glowingBarFillLevel, 5);

        }
        else {
            // Chalk slots
            graphics.blit(TEXTURE, getGuiLeft() + 52, getGuiTop() + 32, 0, 180, 72, 36);
        }

        graphics.fill(getGuiLeft() + menu.chalkBoxCoords.getFirst() - 1, getGuiTop() + menu.chalkBoxCoords.getSecond() - 1,
                getGuiLeft() + menu.chalkBoxCoords.getFirst() + 17, getGuiTop() + menu.chalkBoxCoords.getSecond() + 17,
                0xAAc8c8c8);
    }
}
