package io.github.mortuusars.chalk.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.core.Mark;
import io.github.mortuusars.chalk.core.MarkSymbol;
import io.github.mortuusars.chalk.items.ChalkItem;
import io.github.mortuusars.chalk.network.Packets;
import io.github.mortuusars.chalk.network.packet.DrawMarkPacket;
import io.github.mortuusars.chalk.utils.MarkDrawingContext;
import io.github.mortuusars.chalk.utils.ParticleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;

public class SymbolSelectScreen extends Screen {
    private final MarkDrawingContext drawingContext;
    private final InteractionHand drawingHand;

    public SymbolSelectScreen(MarkDrawingContext context, InteractionHand drawingHand) {
        super(Component.empty());
        this.drawingContext = context;
        this.drawingHand = drawingHand;

        this.minecraft = Minecraft.getInstance();

        // Keep sneaking while choosing a mark to not jerk the player's camera:
        if (minecraft.player != null)
            minecraft.player.setPose(Pose.CROUCHING);
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
        super.render(poseStack, pMouseX, pMouseY, pPartialTick);

        // Keep sneaking while choosing a mark to not jerk the player's camera:
        if (minecraft != null && minecraft.player != null)
            minecraft.player.setPose(Pose.CROUCHING);

        int size = 48;
        int spacing = 8;
        int xCenter = width / 2;
        int y = height / 2 - size / 2;

        for (int i = 0; i < MarkSymbol.values().length; i++) {
            int x = xCenter - spacing - (MarkSymbol.values().length / 2 * (size + spacing)) + (size * i) + (spacing * i);
//            fill(poseStack, x, y, x + size, y + size, 0x20888888);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);

            boolean isHovering = (pMouseX >= x && pMouseX <= x + size) && (pMouseY >= y && pMouseY <= y + size);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, isHovering ? 1f : 0.65f);
//            RenderSystem.enableBlend();
            MarkSymbol symbol = MarkSymbol.values()[i];
            RenderSystem.setShaderTexture(0, Chalk.resource("textures/block/mark/" + symbol.getSerializedName() + ".png"));
//            RenderSystem.setShaderTexture(0, new ResourceLocation("minecraft:textures/block/gold_block.png"));

            poseStack.pushPose();

            poseStack.translate(x + size / 2, y + size / 2, 0);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(symbol.getDefaultOrientation().getRotation()));
//            poseStack.mulPose(Vector3f.ZP.rotationDegrees((minecraft.level.getGameTime() % 360) * 4));

            poseStack.pushPose();
            poseStack.translate(-x - size / 2 , -y - size / 2, 0);

            blit(poseStack, x, y, size, size, 0, 0, 16, 16, 16, 16);
            poseStack.popPose();
            poseStack.popPose();
        }

    }

    public void buttonPressed(Button button, MarkSymbol symbol) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;

        ItemStack itemInHand = player.getItemInHand(drawingHand);

        if (!(itemInHand.getItem() instanceof ChalkItem chalkItem))
            throw new IllegalStateException("Should be ChalkItem");

        Mark mark = drawingContext.createMark(chalkItem.getColor(), symbol, false);

        if (drawingContext.canDraw() && (!drawingContext.hasExistingMark() || drawingContext.shouldMarkReplaceAnother(mark))) {
            Packets.sendToServer(new DrawMarkPacket(mark, drawingContext.getMarkBlockPos(), drawingHand));

            ParticleUtils.spawnColorDustParticles(mark.color(), player.level, drawingContext.getMarkBlockPos(), mark.facing());
        }

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

            for (int i = 0; i < MarkSymbol.values().length; i++) {
                int x = xCenter - spacing - (MarkSymbol.values().length / 2 * (size + spacing)) + (size * i) + (spacing * i);
                boolean isHovering = (mouseX >= x && mouseX <= x + size) && (mouseY >= y && mouseY <= y + size);
                if (isHovering) {
                    buttonPressed(null, MarkSymbol.values()[i]);
                    return true;
                }

            }
//        }

        return super.mouseReleased(mouseX, mouseY, pButton);
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
