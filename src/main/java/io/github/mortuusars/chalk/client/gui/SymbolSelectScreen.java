package io.github.mortuusars.chalk.client.gui;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.core.IDrawingTool;
import io.github.mortuusars.chalk.core.Mark;
import io.github.mortuusars.chalk.core.MarkSymbol;
import io.github.mortuusars.chalk.network.Packets;
import io.github.mortuusars.chalk.network.packet.ServerboundDrawMarkPacket;
import io.github.mortuusars.chalk.render.ChalkColors;
import io.github.mortuusars.chalk.utils.MarkDrawingContext;
import io.github.mortuusars.chalk.utils.ParticleUtils;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SymbolSelectScreen extends Screen {
    private static final List<MarkSymbol> SYMBOLS = List.of(
            MarkSymbol.HOUSE,
            MarkSymbol.CHECKMARK,
            MarkSymbol.CROSS,
            MarkSymbol.HEART,
            MarkSymbol.SKULL
    );
    private static final int SYMBOL_SIZE = 48;
    private static final int SYMBOL_SPACING = 10;

    private final Player player;
    private final Level level;

    private final MarkDrawingContext drawingContext;
    private final InteractionHand drawingHand;

    private final DyeColor color;
    private final Direction markFacing;
    private final BlockPos surfacePos;
    private final BlockState surfaceState;

    @Nullable
    private MarkSymbol hoveredSymbol;
    private boolean mouseWasReleased;
    private long timestamp;

    public SymbolSelectScreen(MarkDrawingContext context, InteractionHand drawingHand) {
        super(Component.empty());
        this.drawingContext = context;
        this.drawingHand = drawingHand;

        this.minecraft = Minecraft.getInstance();
        this.player = minecraft.player;
        Preconditions.checkArgument(player != null, "Player cannot be null.");
        this.level = player.level;
        this.timestamp = level.getGameTime();

        ItemStack itemInHand = minecraft.player.getItemInHand(drawingHand);

        color = itemInHand.getItem() instanceof IDrawingTool drawingTool ?
                drawingTool.getMarkColor(itemInHand).orElse(DyeColor.WHITE) : DyeColor.WHITE;
        markFacing = drawingContext.getMarkFacing();
        surfacePos = drawingContext.getMarkBlockPos().relative(markFacing.getOpposite());
        surfaceState = minecraft.level != null ? minecraft.level.getBlockState(surfacePos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void init() {

    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float pPartialTick) {
        if (!drawingContext.canDraw()) {
            this.close();
            return;
        }

        // Keep sneaking while choosing a mark to not jerk the player's camera:
        player.setPose(Pose.CROUCHING);
        if (player.getForcedPose() != Pose.CROUCHING)
            player.setForcedPose(Pose.CROUCHING);


        // BG
//        fillGradient(poseStack, 0, 0, width, height, 0x20000000, 0x40000000);

        super.render(poseStack, mouseX, mouseY, pPartialTick);

        int xCenter = width / 2;
        int y = height / 2 - SYMBOL_SIZE / 2;

        boolean isHoveringOverSymbol = false;

        for (int i = 0; i < SYMBOLS.size(); i++) {
            int x = xCenter - SYMBOL_SPACING * 3 - (SYMBOLS.size() / 2 * (SYMBOL_SIZE + SYMBOL_SPACING))
                    + (SYMBOL_SIZE * i) + (SYMBOL_SPACING * i);

            boolean isHovering = (mouseX >= x && mouseX <= x + SYMBOL_SIZE) && (mouseY >= y && mouseY <= y + SYMBOL_SIZE);

            int color = ChalkColors.fromDyeColor(this.color);

            float r =  (float)(color >> 16 & 255) / 255.0F;
            float g =  (float)(color >> 8 & 255) / 255.0F;
            float b =  (float)(color & 255) / 255.0F;

            renderBlockSurface(poseStack, mouseX, mouseY, x, y);


            int regularBorderColor = 0xFF252525;
            int hoverBorderColor = (int) ((b * 0.97f + 0.03f)  * 255) + ((int) ((g * 0.97f + 0.03f) * 255) << 8)
                    + ((int) ((r * 0.97f + 0.03f) * 255) << 16) + (0xFF << 24);

            int borderColor = isHovering ? hoverBorderColor : regularBorderColor;
            float thickness = 2;

            fill(poseStack, (int)(x - thickness), y, x, y + SYMBOL_SIZE, borderColor);
            fill(poseStack, x, (int)(y - thickness), x + SYMBOL_SIZE, y, borderColor);
            fill(poseStack, x + SYMBOL_SIZE, y, (int)(x + SYMBOL_SIZE + thickness), y + SYMBOL_SIZE, borderColor);
            fill(poseStack, x, y + SYMBOL_SIZE, x + SYMBOL_SIZE, (int)(y + SYMBOL_SIZE + thickness), borderColor);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(r, g, b, /*isHovering ? 1f : 0.65f*/1f);
            RenderSystem.enableBlend();
            MarkSymbol symbol = SYMBOLS.get(i);
            RenderSystem.setShaderTexture(0, Chalk.resource("textures/block/mark/" + symbol.getSerializedName() + ".png"));

            if (isHovering) {
                this.hoveredSymbol = symbol;
                isHoveringOverSymbol = true;
                player.displayClientMessage(Component.translatable(symbol.getTranslationKey()), true);
            }

            poseStack.pushPose();

            poseStack.translate(x + SYMBOL_SIZE / 2f, y + SYMBOL_SIZE / 2f, 0);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(symbol.getDefaultOrientation().getRotation()));

//            poseStack.pushPose();
            poseStack.translate(-x - SYMBOL_SIZE / 2f , -y - SYMBOL_SIZE / 2f, 0);

            blit(poseStack, x, y, SYMBOL_SIZE, SYMBOL_SIZE, 0, 0, 16, 16, 16, 16);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            fillGradient(new PoseStack(), x, y, x + SYMBOL_SIZE, y + SYMBOL_SIZE, 0x00000000, 0x25000000);
//            poseStack.popPose();
            poseStack.popPose();

        }

        if (!isHoveringOverSymbol)
            hoveredSymbol = null;
    }

    private void renderBlockSurface(PoseStack poseStack, int mouseX, int mouseY, int x, int y) {
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1f);
        RenderSystem.applyModelViewMatrix();

        PoseStack posestack1 = new PoseStack();
        posestack1.translate(x, y, -100);

        int xRot = 0;
        int yRot = 0;

        if (markFacing == Direction.UP)
            xRot = -90;
        else if (markFacing == Direction.DOWN)
            xRot = 90;

        if (markFacing == Direction.EAST)
            yRot = 270;
        else if (markFacing == Direction.NORTH)
            yRot = 180;
        else if (markFacing == Direction.WEST)
            yRot = 90;

        posestack1.translate(SYMBOL_SIZE / 2f, SYMBOL_SIZE / 2f, SYMBOL_SIZE / 2f);
        posestack1.mulPose(Vector3f.XP.rotationDegrees(xRot - 0.1f));
        posestack1.mulPose(Vector3f.YP.rotationDegrees(yRot - 0.1f));
        posestack1.translate(-SYMBOL_SIZE / 2f, -SYMBOL_SIZE / 2f, -SYMBOL_SIZE / 2f);

        posestack1.translate(0, 48, 0);
        posestack1.scale(1.0F, -1.0F, 1.0F);
        posestack1.scale(SYMBOL_SIZE, SYMBOL_SIZE, SYMBOL_SIZE);

        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        Lighting.setupForFlatItems();

        minecraft.getBlockRenderer().renderSingleBlock(surfaceState, posestack1, minecraft.renderBuffers().bufferSource(),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        multibuffersource$buffersource.endBatch();
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == GLFW.GLFW_KEY_E) {
            this.close();
            return true;
        }

        int key = pKeyCode - 48;
        if (key >= 1 && key <= Math.min(SYMBOLS.size(), 9)) {
            drawSymbol(SYMBOLS.get(key - 1));
            this.close();
            return true;
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
//        if (hoveredSymbol == null) {
//            this.close();
//            return true;
//        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int pButton) {
        if (!mouseWasReleased && level.getGameTime() - timestamp < 4) {
            mouseWasReleased = true;
            return true;
        }

        if (hoveredSymbol != null) {
            drawSymbol(hoveredSymbol);
        }

        this.close();
        return true;
//        return super.mouseReleased(mouseX, mouseY, pButton);
    }

    private boolean drawSymbol(@NotNull MarkSymbol symbol) {
        Mark mark = createMark(symbol, player.getItemInHand(drawingHand));

        if (drawingContext.canDraw() && (!drawingContext.hasExistingMark() || drawingContext.shouldMarkReplaceAnother(mark))) {
            Packets.sendToServer(new ServerboundDrawMarkPacket(mark, drawingContext.getMarkBlockPos(), drawingHand));
            player.swing(drawingHand);

            //TODO: particles will not be visible to others
            ParticleUtils.spawnColorDustParticles(mark.color(), level, drawingContext.getMarkBlockPos(), mark.facing());
            return true;
        }

        return false;
    }

    private Mark createMark(MarkSymbol symbol, ItemStack itemInHand) {
        if (!(itemInHand.getItem() instanceof IDrawingTool drawingTool))
            throw new IllegalStateException("Item in hand is not IDrawingTool. [%s]".formatted(itemInHand));

        return drawingContext.createMark(color, symbol, drawingTool.getGlowing(itemInHand));
    }

    public void close() {
        this.onClose();
    }

    @Override
    public void onClose() {
        player.setForcedPose(null);
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
