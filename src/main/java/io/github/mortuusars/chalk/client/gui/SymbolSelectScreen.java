package io.github.mortuusars.chalk.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.chalk.core.MarkSymbol;
import io.github.mortuusars.chalk.core.ChalkMark;
import io.github.mortuusars.chalk.items.ChalkItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class SymbolSelectScreen extends Screen {
    private final UseOnContext context;

    public SymbolSelectScreen(UseOnContext context) {
        super(Component.empty());
        this.context = context;
        this.minecraft = Minecraft.getInstance();

//        this.minecraft.player.isCrouching()
    }

    @Override
    protected void init() {

        addRenderableWidget(new Button(width / 2 + 20,  height / 2 + 20, 50, 20, Component.literal("Skull"), pButton -> {}));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (!Minecraft.getInstance().mouseHandler.isRightPressed())
            this.onClose();

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int pButton) {

        Optional<GuiEventListener> child = this.getChildAt(mouseX, mouseY);
        if (child.isPresent()) {
            GuiEventListener guiEventListener = child.get();
            LocalPlayer player = Minecraft.getInstance().player;
            player.playSound(SoundEvents.NOTE_BLOCK_GUITAR);
            this.onClose();

            Level level = context.getLevel();
            Vec3 clickLocation = context.getClickLocation();

            ItemStack itemInHand = player.getItemInHand(context.getHand());
            if (itemInHand.getItem() instanceof ChalkItem chalkItem) {
                ChalkMark.tryDraw(MarkSymbol.SKULL, chalkItem.getColor(), false, context.getClickedPos(),
                        context.getClickedFace(), clickLocation, level);
            }

//            boolean a = true;
        }

        return super.mouseReleased(mouseX, mouseY, pButton);
    }

    @Override
    public void onClose() {
        super.onClose();

    }
}
