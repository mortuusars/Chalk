package io.github.mortuusars.chalk.client.gui;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.config.Config;
import io.github.mortuusars.chalk.core.IDrawingTool;
import io.github.mortuusars.chalk.core.Mark;
import io.github.mortuusars.chalk.core.MarkSymbol;
import io.github.mortuusars.chalk.network.Packets;
import io.github.mortuusars.chalk.network.packet.ServerboundDrawMarkPacket;
import io.github.mortuusars.chalk.render.ChalkColors;
import io.github.mortuusars.chalk.utils.MarkDrawingContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class SymbolSelectScreen extends Screen {
    private static final int SYMBOL_SIZE = 48;
    private static final int SYMBOL_SPACING = 10;
    private static final float SYMBOL_BORDER_THICKNESS = 2;
    private static final int DEFAULT_SYMBOL_BORDER_COLOR = 0xFF252525;
    private static final Map<MarkSymbol, ResourceLocation> SYMBOL_TEXTURES;

    private final Player player;
    private final Level level;
    private final long openTimestamp;

    private final List<MarkSymbol> unlockedSymbols;
    private final MarkDrawingContext drawingContext;
    private final InteractionHand drawingHand;

    private final int color;
    private final float r;
    private final float g;
    private final float b;
    private final int hoverBorderColor;
    private final Direction markFacing;
    private final BlockState surfaceState;

    private int centerX;
    private int centerY;
    private int buttonsWidth;
    private int buttonsStartX;

    @Nullable
    private MarkSymbol hoveredSymbol;
    private boolean mouseWasReleased;

    static {
        SYMBOL_TEXTURES = new HashMap<>();
        for (MarkSymbol symbol : MarkSymbol.getSpecialSymbols()) {
            SYMBOL_TEXTURES.put(symbol, Chalk.resource("textures/block/mark/" + symbol.getSerializedName() + ".png"));
        }
    }

    public SymbolSelectScreen(List<MarkSymbol> unlockedSymbols, MarkDrawingContext context) {
        super(Component.empty());
        this.unlockedSymbols = unlockedSymbols;
        this.drawingContext = context;
        this.drawingHand = context.getDrawingHand();

        this.minecraft = Minecraft.getInstance();
        this.player = minecraft.player;
        Preconditions.checkArgument(player != null, "Player cannot be null.");
        this.level = player.level();
        this.openTimestamp = level.getGameTime();

        ItemStack itemInHand = minecraft.player.getItemInHand(drawingHand);

        this.color = itemInHand.getItem() instanceof IDrawingTool drawingTool ? drawingTool.getMarkColorValue(itemInHand) : ChalkColors.fromDyeColor(DyeColor.WHITE);
        r = (float)(this.color >> 16 & 255) / 255.0F;
        g = (float)(this.color >> 8 & 255) / 255.0F;
        b = (float)(this.color & 255) / 255.0F;
        hoverBorderColor = this.color + (0xFF << 24);
        markFacing = drawingContext.getMarkFacing();
        BlockPos surfacePos = drawingContext.getMarkBlockPos().relative(markFacing.getOpposite());
        surfaceState = minecraft.level != null ? minecraft.level.getBlockState(surfacePos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void init() {
        if (!drawingContext.canDraw()) {
            this.close();
            return;
        }

        centerX = width / 2;
        centerY = height / 2;

        buttonsWidth = unlockedSymbols.size() > 0 ?
                SYMBOL_SIZE * unlockedSymbols.size() + SYMBOL_SPACING * (unlockedSymbols.size() - 1)
                : SYMBOL_SIZE;
        buttonsStartX = centerX - buttonsWidth / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float pPartialTick) {
        // Keep sneaking while choosing a mark to not jerk player's camera:
        player.setPose(Pose.CROUCHING);
        if (player.getForcedPose() != Pose.CROUCHING)
            player.setForcedPose(Pose.CROUCHING);

        // BG
        graphics.fillGradient(0, 0, width, height, 0x15000000, 0x35000000);

        super.render(graphics, mouseX, mouseY, pPartialTick);

        hoveredSymbol = null;

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 200);

        for (int i = 0; i < unlockedSymbols.size(); i++) {
            MarkSymbol symbol = unlockedSymbols.get(i);

            int x = buttonsStartX + SYMBOL_SIZE * i + SYMBOL_SPACING * i - 1;
            int y = centerY - SYMBOL_SIZE / 2;
            boolean isHovering = (mouseX >= x && mouseX <= x + SYMBOL_SIZE) && (mouseY >= y && mouseY <= y + SYMBOL_SIZE);

            if (isHovering) {
                this.hoveredSymbol = symbol;
                player.displayClientMessage(Component.translatable(symbol.getTranslationKey()), true);
            }

            drawSymbolButton(graphics, mouseX, mouseY, symbol, x, y, isHovering);
        }

        graphics.pose().popPose();
    }

    private void drawSymbolButton(GuiGraphics graphics, int mouseX, int mouseY, MarkSymbol symbol, int x, int y, boolean isHovering) {
        renderBlockSurface(graphics, mouseX, mouseY, x, y);

        int borderColor = isHovering ? hoverBorderColor : DEFAULT_SYMBOL_BORDER_COLOR;

        graphics.fill((int)(x - SYMBOL_BORDER_THICKNESS), y, x, y + SYMBOL_SIZE, borderColor);
        graphics.fill(x, (int)(y - SYMBOL_BORDER_THICKNESS), x + SYMBOL_SIZE, y, borderColor);
        graphics.fill(x + SYMBOL_SIZE, y, (int)(x + SYMBOL_SIZE + SYMBOL_BORDER_THICKNESS), y + SYMBOL_SIZE, borderColor);
        graphics.fill(x, y + SYMBOL_SIZE, x + SYMBOL_SIZE, (int)(y + SYMBOL_SIZE + SYMBOL_BORDER_THICKNESS), borderColor);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(r, g, b, 1f);
        RenderSystem.enableBlend();

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        poseStack.translate(x + SYMBOL_SIZE / 2f, y + SYMBOL_SIZE / 2f, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(symbol.getDefaultOrientation().getRotation() + Config.SYMBOL_ROTATION_OFFSETS.get(symbol).get()));
        poseStack.translate(-x - SYMBOL_SIZE / 2f , -y - SYMBOL_SIZE / 2f, 0);
        poseStack.translate(0, 0, 100);
        graphics.blit(SYMBOL_TEXTURES.get(symbol), x, y, SYMBOL_SIZE, SYMBOL_SIZE, 0, 0, 16, 16, 16, 16);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        poseStack.popPose();

        // Shadow overlay
        graphics.fillGradient(x, y, x + SYMBOL_SIZE, y + SYMBOL_SIZE, 0x08FFFFFF, 0x25000000);
    }

    @SuppressWarnings("DataFlowIssue")
    private void renderBlockSurface(GuiGraphics graphics, int mouseX, int mouseY, int x, int y) {
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.applyModelViewMatrix();
        Lighting.setupForFlatItems();

        PoseStack posestack1 = new PoseStack();
        posestack1.translate(x, y, 0);

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
        posestack1.mulPose(Axis.XP.rotationDegrees(xRot - 0.1f));
        posestack1.mulPose(Axis.YP.rotationDegrees(yRot - 0.1f));
        posestack1.translate(-SYMBOL_SIZE / 2f, -SYMBOL_SIZE / 2f, -SYMBOL_SIZE / 2f);

        posestack1.translate(0, 48, 0);
        posestack1.scale(1.0F, -1.0F, 1.0F);
        posestack1.scale(SYMBOL_SIZE, SYMBOL_SIZE, SYMBOL_SIZE);

        MultiBufferSource.BufferSource bufferSource = graphics.bufferSource();

        minecraft.getBlockRenderer().renderSingleBlock(surfaceState, posestack1, bufferSource,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (Minecraft.getInstance().options.keyInventory.matches(pKeyCode, pScanCode)) {
            this.close();
            return true;
        }

        int key = pKeyCode - InputConstants.KEY_0; // Offset
        if (key >= 1 && key <= Math.min(unlockedSymbols.size(), 9)) {
            tryDrawSymbol(unlockedSymbols.get(key - 1));
            this.close();
            return true;
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!mouseWasReleased && level.getGameTime() - openTimestamp < 4) {
            mouseWasReleased = true;
            return true;
        }

        if (button == 0 || !mouseWasReleased)
            tryDrawSymbol(hoveredSymbol);

        this.close();
        return true;
    }

    private boolean tryDrawSymbol(MarkSymbol symbol) {
        if (symbol == null)
            return false;

        return drawSymbol(symbol);
    }

    private boolean drawSymbol(@NotNull MarkSymbol symbol) {
        Mark mark = createMark(symbol, player.getItemInHand(drawingHand));

        if (drawingContext.canDraw() && (!drawingContext.hasExistingMark() || drawingContext.shouldMarkReplaceAnother(mark))) {
            Packets.sendToServer(new ServerboundDrawMarkPacket(mark.color,
                    NbtUtils.writeBlockState(mark.createBlockState(player.getItemInHand(drawingHand))), drawingContext.getMarkBlockPos(), drawingHand));
            player.swing(drawingHand);
            return true;
        }

        return false;
    }

    private Mark createMark(MarkSymbol symbol, ItemStack itemInHand) {
        if (!(itemInHand.getItem() instanceof IDrawingTool drawingTool))
            throw new IllegalStateException("Item in hand is not IDrawingTool. [%s]".formatted(itemInHand));

        return drawingTool.getMark(itemInHand, drawingContext, symbol);
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
