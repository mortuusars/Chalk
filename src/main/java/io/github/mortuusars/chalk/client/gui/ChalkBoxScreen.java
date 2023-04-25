package io.github.mortuusars.chalk.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.config.CommonConfig;
import io.github.mortuusars.chalk.menus.ChalkBoxMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class ChalkBoxScreen extends AbstractContainerScreen<ChalkBoxMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Chalk.ID, "textures/gui/chalk_box.png");

    private static final int GLOWING_BAR_WIDTH = 72;

    private final boolean glowingEnabled;

    private final int maxGlowingUses;

    public ChalkBoxScreen(ChalkBoxMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        glowingEnabled = CommonConfig.CHALK_BOX_GLOWING.get();
        maxGlowingUses = CommonConfig.CHALK_BOX_GLOWING_USES.get();

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
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        if (menu.chalkBoxCoords != null) {
            itemRenderer.renderGuiItem(menu.chalkBoxStack, getGuiLeft() + menu.chalkBoxCoords.getFirst(),
                    getGuiTop() + menu.chalkBoxCoords.getSecond());
        }

        poseStack.pushPose();
        poseStack.translate(0, 0, 200);
        fill(poseStack, getGuiLeft() + menu.chalkBoxCoords.getFirst() - 1, getGuiTop() + menu.chalkBoxCoords.getSecond() - 1,
                getGuiLeft() + menu.chalkBoxCoords.getFirst() + 17, getGuiTop() + menu.chalkBoxCoords.getSecond() + 17,
                0x20c8c8c8);
        poseStack.popPose();

        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1,1, 1);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, getGuiLeft(), getGuiTop(), 0,0, imageWidth, imageHeight);

        if (glowingEnabled){
            // Chalk Slots
            blit(poseStack, getGuiLeft() + 52, getGuiTop() + 17, 0, 180, 72, 36);

            // Bar + Slot
            blit(poseStack, getGuiLeft() + 52, getGuiTop() + 57, 0, 217, 72, 28);

            int barSize = (int)Math.ceil((Math.min(menu.getGlowingUses(), maxGlowingUses) / (float) maxGlowingUses) * GLOWING_BAR_WIDTH);
            int glowingBarFillLevel = Math.min(GLOWING_BAR_WIDTH, barSize);

            // Fill
            blit(poseStack, getGuiLeft() + 52, getGuiTop() + 57, 72, 217, glowingBarFillLevel, 5);

        }
        else {
            // Chalk slots
            blit(poseStack, getGuiLeft() + 52, getGuiTop() + 32, 0, 180, 72, 36);
        }

        fill(poseStack, getGuiLeft() + menu.chalkBoxCoords.getFirst() - 1, getGuiTop() + menu.chalkBoxCoords.getSecond() - 1,
                getGuiLeft() + menu.chalkBoxCoords.getFirst() + 17, getGuiTop() + menu.chalkBoxCoords.getSecond() + 17,
                0xAAc8c8c8);
    }
}
