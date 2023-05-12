package io.github.mortuusars.chalk.client.gui;

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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;
import java.util.Optional;

public class SymbolSelectScreen extends Screen {
    private final MarkDrawingContext drawingContext;
    private final InteractionHand drawingHand;

    private final List<MarkSymbol> symbols = List.of(
            MarkSymbol.HOUSE,
            MarkSymbol.CHECKMARK,
            MarkSymbol.CROSS,
            MarkSymbol.HEART,
            MarkSymbol.SKULL
    );

    private final DyeColor color;

    public SymbolSelectScreen(MarkDrawingContext context, InteractionHand drawingHand) {
        super(Component.empty());
        this.drawingContext = context;
        this.drawingHand = drawingHand;

        this.minecraft = Minecraft.getInstance();

        ItemStack itemInHand = minecraft.player.getItemInHand(drawingHand);

        color = itemInHand.getItem() instanceof IDrawingTool drawingTool ?
                drawingTool.getMarkColor(itemInHand).orElse(DyeColor.WHITE) : DyeColor.WHITE;
    }

    @Override
    protected void init() {
//        addRenderableWidget(new Button(width / 2 + 30,  height / 2 + 20, 50, 20,
//                Component.literal("Skull"), bt -> buttonPressed(bt, MarkSymbol.SKULL)));
//        addRenderableWidget(new Button(width / 2 - 30,  height / 2 + 20, 50, 20,
//                Component.literal("Cross"), bt -> buttonPressed(bt, MarkSymbol.CROSS)));
    }

    @Override
    public void render(PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (!drawingContext.canDraw()) {
            this.onClose();
            return;
        }

        // Keep sneaking while choosing a mark to not jerk the player's camera:
        if (minecraft != null && minecraft.player != null)
            minecraft.player.setPose(Pose.CROUCHING);

        fillGradient(poseStack, 0, 0, width, height, 0x20000000, 0x40000000);

        super.render(poseStack, pMouseX, pMouseY, pPartialTick);






        int size = 48;
        int spacing = 8;
        int xCenter = width / 2;
        int y = height / 2 - size / 2;

        for (int i = 0; i < symbols.size(); i++) {
            int x = xCenter - spacing * 3 - (symbols.size() / 2 * (size + spacing)) + (size * i) + (spacing * i);
//            x -= size / 2;
//            fill(poseStack, x, y, x + size, y + size, 0x20888888);


            boolean isHovering = (pMouseX >= x && pMouseX <= x + size) && (pMouseY >= y && pMouseY <= y + size);

            int color = ChalkColors.fromDyeColor(this.color);

            float r =  (float)(color >> 16 & 255) / 255.0F;
            float g =  (float)(color >> 8 & 255) / 255.0F;
            float b =  (float)(color & 255) / 255.0F;

//            float r = (color & 255) / 255f;
//            float g = ((color >> 8) & 255) / 255f;
//            float b = ((color >> 16) & 255) / 255f;






//            RenderSystem.setShaderTexture(0, new ResourceLocation("minecraft:textures/block/gold_block.png"));



            {
                BlockPos surfacePos = drawingContext.getMarkBlockPos().relative(Direction.DOWN);
                BlockState surfaceState = minecraft.level.getBlockState(surfacePos);
                BakedModel surfaceModel = minecraft.getBlockRenderer().getBlockModel(surfaceState);

                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, /*isHovering ? 1f : 0.65f*/1f);
//            PoseStack posestack = RenderSystem.getModelViewStack();
//            posestack.pushPose();
//            posestack.translate((double)width / 2, (double)height / 2, (double)(100.0f));
//            posestack.translate(8.0D, 8.0D, 0.0D);
//            posestack.scale(1.0F, -1.0F, 1.0F);
//            posestack.scale(16.0F, 16.0F, 16.0F);


//            posestack.scale();
                RenderSystem.applyModelViewMatrix();
                PoseStack posestack1 = new PoseStack();
//                posestack1.translate((double)width / 2, (double)height / 2, -50);
                posestack1.translate(x, y, -50);
//            posestack1.translate(s / 2, s / 2, s / 2);
//            posestack1.mulPose(Vector3f.YP.rotationDegrees(minecraft.level.getGameTime() % 360));
                posestack1.mulPose(Vector3f.XP.rotationDegrees(-90));
//                posestack1.translate(-24, -24 ,-24);
                posestack1.translate(0, 48, 0);

                posestack1.scale(1.0F, -1.0F, 1.0F);
                posestack1.scale(size, size, size);
                MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
//            boolean flag = !surfaceModel.usesBlockLight();
//            if (flag) {
                Lighting.setupForFlatItems();
//            }

//                for (int is = 0; is < symbols.size(); is++) {
//                    posestack1.pushPose();
//                    posestack1.translate(is * 1.15f - 2.3, 0, 0);

                    minecraft.getBlockRenderer().renderSingleBlock(surfaceState, posestack1, minecraft.renderBuffers().bufferSource(),
                            LightTexture.pack(0, isHovering ? 15 : 10), OverlayTexture.NO_OVERLAY);
//                    posestack1.popPose();
//                }
                multibuffersource$buffersource.endBatch();
                RenderSystem.enableDepthTest();
//            if (flag) {
                Lighting.setupFor3DItems();
//            }

                RenderSystem.applyModelViewMatrix();
            }

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(r, g, b, isHovering ? 1f : 0.65f);
            RenderSystem.enableBlend();
            MarkSymbol symbol = symbols.get(i);
            RenderSystem.setShaderTexture(0, Chalk.resource("textures/block/mark/" + symbol.getSerializedName() + ".png"));

            if (isHovering)
                minecraft.player.displayClientMessage(Component.literal(symbol.getSerializedName()), true);

            poseStack.pushPose();

            poseStack.translate(x + size / 2, y + size / 2, 0);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(symbol.getDefaultOrientation().getRotation()));
//            poseStack.mulPose(Vector3f.ZP.rotationDegrees((minecraft.level.getGameTime() % 360) * 4));

            poseStack.pushPose();
            poseStack.translate(-x - size / 2 , -y - size / 2, 0);

            blit(poseStack, x, y, size, size, 0, 0, 16, 16, 16, 16);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            fillGradient(new PoseStack(), x, y, x + size, y + size, 0x00000000, 0x30000000);
            poseStack.popPose();
            poseStack.popPose();

        }

    }

    public void buttonPressed(Button button, MarkSymbol symbol) {
        drawSymbol(symbol);
        this.onClose();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int pButton) {
//        Optional<GuiEventListener> child = this.getChildAt(mouseX, mouseY);
//        if (child.isPresent()) {
//            GuiEventListener guiEventListener = child.get();
//
//            if (guiEventListener instanceof Button button) {
//                button.onPress();
//                return true;
//            }

            int size = 48;
            int spacing = 8;
            int xCenter = width / 2;
            int y = height / 2 - size / 2;

            for (int i = 0; i < symbols.size(); i++) {
                int x = xCenter - spacing - (symbols.size() / 2 * (size + spacing)) + (size * i) + (spacing * i);
                boolean isHovering = (mouseX >= x && mouseX <= x + size) && (mouseY >= y && mouseY <= y + size);
                if (isHovering) {
                    buttonPressed(null, symbols.get(i));
                    return true;
                }

            }
//        }

//        this.onClose();

        return super.mouseReleased(mouseX, mouseY, pButton);
    }

    private boolean drawSymbol(MarkSymbol symbol) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return false;

        Mark mark = createMark(symbol, player.getItemInHand(drawingHand));

        if (drawingContext.canDraw() && (!drawingContext.hasExistingMark() || drawingContext.shouldMarkReplaceAnother(mark))) {
            Packets.sendToServer(new ServerboundDrawMarkPacket(mark, drawingContext.getMarkBlockPos(), drawingHand));

            //TODO: particles will not be visible to others
            ParticleUtils.spawnColorDustParticles(mark.color(), player.level, drawingContext.getMarkBlockPos(), mark.facing());
            return true;
        }

        return false;
    }

    private Mark createMark(MarkSymbol symbol, ItemStack itemInHand) {
        if (!(itemInHand.getItem() instanceof IDrawingTool drawingTool))
            throw new IllegalStateException("Item in hand is not IDrawingTool. [%s]".formatted(itemInHand));

//        Optional<DyeColor> color = drawingTool.getMarkColor(itemInHand);

//        if (color.isEmpty())
//            throw new IllegalStateException("Cannot get color from drawing tool. [%s]".formatted(itemInHand));

        return drawingContext.createMark(color, symbol, drawingTool.getGlowing(itemInHand));
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
