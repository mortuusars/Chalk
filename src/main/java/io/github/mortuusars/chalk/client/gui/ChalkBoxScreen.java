package io.github.mortuusars.chalk.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.config.CommonConfig;
import io.github.mortuusars.chalk.menus.ChalkBoxMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class ChalkBoxScreen extends AbstractContainerScreen<ChalkBoxMenu> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Chalk.MOD_ID, "textures/gui/chalk_box.png");

    private static final int GLOWING_BAR_HEIGHT = 16;

    private final int maxGlowingUses;
    private final int glowingUses;

    public ChalkBoxScreen(ChalkBoxMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        maxGlowingUses = CommonConfig.CHALK_BOX_GLOWING_USES.get();
        glowingUses = menu.getGlowingUses();
    }

    @Override
    protected void init() {
        this.imageWidth = 176;
        this.imageHeight = 166;
        super.init();
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);

        int textY = 5;
        drawString(poseStack, font, "Glowing Uses: " + glowingUses, 5, textY, 0xFFAA5555);
        drawString(poseStack, font, "Max Uses: " + maxGlowingUses, 5, textY += 10, 0xFFAA5555);
    }

    int glowBarTest = 0;

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1,1, 1);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, getGuiLeft(), getGuiTop(), 0,0, imageWidth, imageHeight);

        if (CommonConfig.CHALK_BOX_GLOWING.get()){
            blit(poseStack, getGuiLeft() + 23, getGuiTop() + 33, 177, 1, 31, 22);

            // TODO: Glowing bar
            int glowingBarFillLevel = calculateGlowingBarHeight(glowingUses, maxGlowingUses, GLOWING_BAR_HEIGHT);
//            if (glowBarTest > 16)
//                glowBarTest = 0;
//            int glowingBarFillLevel = glowBarTest++;
            blit(poseStack, getGuiLeft() + 45, getGuiTop() + 52 - glowingBarFillLevel, 177, 25, 4, glowingBarFillLevel);
        }
    }

    private int calculateGlowingBarHeight(int uses, int maxUses, int textureBarHeight){
        int height = (int)Math.ceil((Math.min(uses, maxUses) / (float) maxUses) * textureBarHeight);
        return Math.min(textureBarHeight, height);
    }
}
