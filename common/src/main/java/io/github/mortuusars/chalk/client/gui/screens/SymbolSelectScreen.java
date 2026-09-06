package io.github.mortuusars.chalk.client.gui.screens;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.network.packet.serverbound.DrawMarkServerboundPacket;
import io.github.mortuusars.chalk.world.chalk.Mark;
import io.github.mortuusars.chalk.world.chalk.MarkDrawingContext;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import io.github.mortuusars.chalk.world.item.MarkDrawable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class SymbolSelectScreen extends Screen {
    protected static final ResourceLocation BORDER_REGULAR_SPRITE = Chalk.resource("symbol_selection/border_regular");
    protected static final ResourceLocation BORDER_GOLD_SPRITE = Chalk.resource("symbol_selection/border_gold");
    protected static final ResourceLocation BORDER_HOVERED_SPRITE = Chalk.resource("symbol_selection/border_hovered");

    protected int SYMBOL_SIZE = 32;
    protected int SYMBOL_SPACING = 8;
    protected int SYMBOL_BORDER_THICKNESS = SYMBOL_SIZE / 16;
    protected int GROUP_LABEL_BOTTOM_PADDING = 4;
    protected int DEFAULT_BORDER_COLOR = 0xFF252525;

    protected final LocalPlayer player;
    protected final Level level;
    protected final long openTimestamp;
    protected final long openTimestampMs = System.currentTimeMillis();

    protected final List<Holder<MarkSymbol>> symbols;
    protected final MarkDrawingContext context;

    protected final int color;
    protected final float r;
    protected final float g;
    protected final float b;
    protected final int hoverBorderColor;
    protected final Direction markFacing;
    protected final BlockState surfaceState;

    protected final List<Mark> marks;
    protected final MarkDrawable drawable;
    protected final ItemStack itemStack;

    protected float openAnimation;
    protected int centerX;
    protected int centerY;

    protected LinkedHashMap<Component, List<List<Mark>>> groupsAndRows = new LinkedHashMap<>();
    protected int contentHeight;
    protected int contentY;

    protected double changePerScroll;
    protected double maxScroll = 999999.0;
    protected double scroll = 0;
    protected double currentScroll = 0;
    protected int scrollBarWidth;
    protected int scrollBarX;
    protected int scrollBarYStart;
    protected int scrollBarYEnd;
    protected double scrollThumbY;
    protected int scrollThumbSize;
    protected boolean draggingScrollThumb;
    protected boolean isHoveringOverScrollBar;
    protected boolean isHoveringOverScrollThumb;
    protected double lastScrollingMouseY;

    @Nullable
    protected Mark hoveredMark;
    protected boolean mouseWasReleased;
    protected float edgeMargin = 0.15f;

    public SymbolSelectScreen(List<Holder<MarkSymbol>> symbols, MarkDrawingContext context) {
        super(Component.empty());
        this.symbols = symbols;
        this.context = context;

        this.minecraft = Minecraft.getInstance();
        this.player = minecraft.player;
        Preconditions.checkArgument(player != null, "Player cannot be null.");
        this.level = player.level();
        this.openTimestamp = level.getGameTime();

        itemStack = player.getItemInHand(context.hand());
        // Screen shouldn't be opened with other items
        drawable = ((MarkDrawable) itemStack.getItem());

        color = drawable.getMarkDrawingColor(itemStack);
        r = (float) (this.color >> 16 & 255) / 255.0F;
        g = (float) (this.color >> 8 & 255) / 255.0F;
        b = (float) (this.color & 255) / 255.0F;
        hoverBorderColor = FastColor.ARGB32.lerp(0.2f, FastColor.ARGB32.opaque(this.color), 0xFFFFFFFF);

        marks = symbols.stream()
              .map(symbol -> drawable.createMark(player, context, itemStack, symbol))
              .toList();
        markFacing = context.markFacing();

        BlockPos surfacePos = this.context.markPos().relative(markFacing.getOpposite());
        surfaceState = minecraft.level != null ? minecraft.level.getBlockState(surfacePos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        if (!drawable.canDrawMark(player, context)) {
            this.onClose();
        }
    }

    @Override
    protected void init() {
        centerX = width / 2;
        centerY = height / 2;
        createGroupsAndRows();

        contentHeight = 0;

        for (Map.Entry<Component, List<List<Mark>>> group : groupsAndRows.entrySet()) {
            contentHeight += font.lineHeight + GROUP_LABEL_BOTTOM_PADDING;
            contentHeight += (SYMBOL_SIZE + SYMBOL_SPACING) * group.getValue().size();
        }

        if (contentHeight < height * (1 - edgeMargin)) {
            maxScroll = 0;
            contentY = height / 2 - contentHeight / 2;
        } else {
            maxScroll = Math.max(0, height * edgeMargin + contentHeight - height * (1 - edgeMargin));
            contentY = (int) (height * edgeMargin);
        }

        changePerScroll = height / 8.0;
    }

    protected void createGroupsAndRows() {
        groupsAndRows.clear();

        Map<String, List<Holder<MarkSymbol>>> groupsAndSymbols = symbols.stream()
              .collect(Collectors.groupingBy(symbol -> symbol.value().group()));

        List<? extends String> groupSorting = Config.Client.SYMBOL_SELECTION_GROUP_SORTING.get();

        List<String> groups = new ArrayList<>();
        groups.addAll(groupSorting.stream()
              .filter(groupsAndSymbols::containsKey)
              .toList());

        groups.addAll(groupsAndSymbols.keySet().stream()
              .filter(group -> !groups.contains(group))
              .sorted()
              .toList());

        for (String group : groups) {
            groupsAndRows.put(Component.translatable("mark_symbol_group.chalk." + group), createRows(groupsAndSymbols.get(group)));
        }
    }

    protected List<List<Mark>> createRows(List<Holder<MarkSymbol>> symbols) {
        int maxSymbolsInRow = Math.max(1, (int) ((width * (1f - edgeMargin * 2)) / (SYMBOL_SIZE + SYMBOL_SPACING)));

        List<Mark> marks = symbols.stream()
              .sorted(Comparator.comparingInt(symbol -> symbol.value().groupPriority()))
              .map(symbol -> drawable.createMark(player, context, itemStack, symbol))
              .toList();

        return Lists.partition(marks, maxSymbolsInRow);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        openAnimation = Mth.clamp((System.currentTimeMillis() - openTimestampMs) / 500f, 0f, 1f);
        openAnimation = 1f - openAnimation;
        openAnimation *= openAnimation * openAnimation;
        openAnimation = 1f - openAnimation;

        handleScreenEdgeScrolling(mouseY, partialTicks);

        super.render(graphics, mouseX, mouseY, partialTicks);

        try {
            renderMarks(graphics, mouseX, mouseY, partialTicks);
        } catch (Exception e) {
            Chalk.LOGGER.error(e);
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 200);
        graphics.fillGradient(0, 0, width, (int) (height * 0.08f),
              FastColor.ARGB32.lerp(openAnimation, 0x007F7F7F, 0x44000000), 0x007F7F7F);
        graphics.fillGradient(0, (int) (height * 0.92f), width, height, 0x007F7F7F,
              FastColor.ARGB32.lerp(openAnimation, 0x00000000, 0x44000000));

        if (maxScroll > 0) {
            renderScrollBar(graphics, mouseX, mouseY, partialTicks);
        }

        graphics.pose().popPose();
    }

    public void renderScrollBar(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        scrollBarWidth = 3;
        scrollBarX = (int) (width - height * 0.2f);
        scrollBarYStart = (int) (height * 0.2f);
        scrollBarYEnd = (int) (height * 0.8f);

        graphics.fill(scrollBarX - 1, scrollBarYStart - 1, scrollBarX + scrollBarWidth + 1, scrollBarYEnd + 1, 0x55111111);
        graphics.fill(scrollBarX, scrollBarYStart, scrollBarX + scrollBarWidth, scrollBarYEnd, 0x55AAAAAA);

        double progress = currentScroll / maxScroll;
        double thumbRatio = (double) height * 0.5 / contentHeight;
        int yArea = scrollBarYEnd - scrollBarYStart;
        scrollThumbSize = (int) Math.max(height * 0.075f, yArea * thumbRatio);
        scrollThumbY = scrollBarYStart + ((yArea - scrollThumbSize) * progress);

        graphics.pose().pushPose();
        graphics.pose().translate(scrollBarX, scrollThumbY, 0);
        isHoveringOverScrollBar = mouseX >= scrollBarX && mouseX < scrollBarX + scrollBarWidth && mouseY >= scrollBarYStart && mouseY < scrollBarYEnd;
        isHoveringOverScrollThumb = isHoveringOverScrollBar && mouseY >= scrollThumbY && mouseY < scrollThumbY + scrollThumbSize;
        int thumbColor = draggingScrollThumb || isHoveringOverScrollThumb ? 0xFFFFFFFF : 0xBBFFFFFF;
        graphics.fill(0, 0, scrollBarWidth, scrollThumbSize, thumbColor);
        graphics.pose().popPose();
    }

    protected void handleScreenEdgeScrolling(int mouseY, float partialTicks) {
        if (draggingScrollThumb) {
            return;
        }

        float screenScrollArea = Math.max(20, height * 0.1f);

        if (mouseY < screenScrollArea) {
            float strength = Mth.clamp((screenScrollArea - mouseY) / screenScrollArea, 0, 1);
            scroll(-height * (0.15f * strength * partialTicks));
        }

        if (mouseY > height - screenScrollArea) {
            float strength = Mth.clamp((mouseY - (height - screenScrollArea)) / screenScrollArea, 0, 1);
            scroll(height * (0.15f * strength * partialTicks));
        }
    }

    protected void renderMarks(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        hoveredMark = null;

        // Animates scroll smoothly
        if (Math.abs(currentScroll - scroll) > 0.001f) {
            currentScroll = Mth.lerp(Math.min(1, partialTicks), currentScroll, scroll);
        } else {
            currentScroll = scroll;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0, -Mth.frac(currentScroll), 0);

        int y = contentY - (int) currentScroll;

        for (Map.Entry<Component, List<List<Mark>>> group : groupsAndRows.entrySet()) {
            int labelX = width / 2 - font.width(group.getKey()) / 2;
            int textOutlineColor = FastColor.ARGB32.lerp(openAnimation, 0x22000000, DEFAULT_BORDER_COLOR);
            int textColor = FastColor.ARGB32.lerp(openAnimation, 0x22FFFFFF, 0xFFFFFFFF);
            graphics.drawString(font, group.getKey(), labelX - 1, y, textOutlineColor, false);
            graphics.drawString(font, group.getKey(), labelX - 1, y + 1, textOutlineColor, false);
            graphics.drawString(font, group.getKey(), labelX - 1, y - 1, textOutlineColor, false);
            graphics.drawString(font, group.getKey(), labelX + 1, y, textOutlineColor, false);
            graphics.drawString(font, group.getKey(), labelX + 1, y - 1, textOutlineColor, false);
            graphics.drawString(font, group.getKey(), labelX + 1, y + 1, textOutlineColor, false);
            graphics.drawString(font, group.getKey(), labelX, y + 1, textOutlineColor, false);
            graphics.drawString(font, group.getKey(), labelX, y - 1, textOutlineColor, false);
            graphics.drawString(font, group.getKey(), labelX, y, textColor, false);

            y += font.lineHeight + GROUP_LABEL_BOTTOM_PADDING;

            List<List<Mark>> rows = group.getValue();

            for (List<Mark> marks : rows) {
                int count = marks.size();
                int rowWidth = (SYMBOL_SIZE * count) + (SYMBOL_SPACING * (count - 1));
                int rowX = (width / 2) - rowWidth / 2;

                for (int markIndex = 0; markIndex < marks.size(); markIndex++) {
                    Mark mark = marks.get(markIndex);
                    int markX = rowX + markIndex * (SYMBOL_SIZE + SYMBOL_SPACING);

                    boolean isHovering = (mouseX >= markX - SYMBOL_BORDER_THICKNESS && mouseX <= markX + SYMBOL_SIZE + SYMBOL_BORDER_THICKNESS)
                          && (mouseY >= y - SYMBOL_BORDER_THICKNESS - Mth.frac(scroll) && mouseY <= y + SYMBOL_SIZE + SYMBOL_BORDER_THICKNESS - Mth.frac(scroll));

                    if (isHovering) {
                        hoveredMark = mark;
                    }

                    renderMarkButton(graphics, mouseX, mouseY, mark, markX, y, isHovering);
                }

                y += SYMBOL_SIZE + SYMBOL_SPACING;
            }
        }

        graphics.pose().popPose();

        if (hoveredMark != null) {
            renderMarkTooltip(graphics, mouseX, mouseY + SYMBOL_SIZE, partialTicks, hoveredMark);
        }
    }

    public void renderMarkTooltip(GuiGraphics graphics, int x, int y, float partialTicks, Mark mark) {
        mark.symbol().unwrapKey()
              .map(ResourceKey::location)
              .ifPresent(location -> {
                  List<FormattedCharSequence> lines = new ArrayList<>();
                  lines.add(Component.translatable(location.toLanguageKey("mark_symbol")).getVisualOrderText());

                  if (mark.symbol().value().supporterOnly()) {
                      lines.add(Component.translatable("gui.chalk.mark_symbol.supporter_exclusive").withStyle(ChatFormatting.GOLD)
                            .getVisualOrderText());
                  }

                  if (player.isCreative() && Minecraft.getInstance().options.advancedItemTooltips) {
                      mark.symbol().value().requiredAdvancement().ifPresent(advancementId -> lines.add(
                            Component.translatable("gui.chalk.mark_symbol.unlocked_by",
                                        Component.literal(advancementId.toString()).withStyle(ChatFormatting.DARK_GRAY))
                                  .withStyle(ChatFormatting.GRAY).getVisualOrderText()));
                  }

                  @SuppressWarnings("deprecation")
                  String namespace = WordUtils.capitalizeFully(location.getNamespace().replace("_", " "));
                  lines.add(Component.literal(namespace).withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withItalic(true))
                        .getVisualOrderText());

                  graphics.renderTooltip(font, lines, x, y);
              });
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.fillGradient(0, 0, width, height,
              FastColor.ARGB32.lerp(openAnimation, 0x00000000, 0x40000000),
              FastColor.ARGB32.lerp(openAnimation, 0x00000000, 0x40000000));
    }

    protected void renderMarkButton(GuiGraphics graphics, int mouseX, int mouseY, Mark mark, int x, int y, boolean isHovering) {
        ResourceLocation borderSprite = isHovering
              ? BORDER_HOVERED_SPRITE
              : mark.symbol().value().supporterOnly()
              ? BORDER_GOLD_SPRITE
              : BORDER_REGULAR_SPRITE;
        graphics.blitSprite(borderSprite, x - SYMBOL_BORDER_THICKNESS, y - SYMBOL_BORDER_THICKNESS,
              SYMBOL_SIZE + SYMBOL_BORDER_THICKNESS * 2, SYMBOL_SIZE + SYMBOL_BORDER_THICKNESS * 2);

        renderBlockSurface(graphics, mouseX, mouseY, x, y);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(r, g, b, openAnimation);
        RenderSystem.enableBlend();

        graphics.pose().pushPose();
        graphics.pose().translate(x + SYMBOL_SIZE / 2f, y + SYMBOL_SIZE / 2f, 0);

        float rotation = calculateMarkRoration(mark);
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(rotation));
        graphics.pose().translate(-x - SYMBOL_SIZE / 2f, -y - SYMBOL_SIZE / 2f, 100);
        graphics.blit(mark.symbol().value().texture().withPrefix("textures/").withSuffix(".png"),
              x, y, SYMBOL_SIZE, SYMBOL_SIZE, 0, 0, 16, 16, 16, 16);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        graphics.pose().popPose();

        // Shadow overlay
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 100);
        graphics.fillGradient(x, y, x + SYMBOL_SIZE, y + SYMBOL_SIZE,
              FastColor.ARGB32.lerp(openAnimation, 0x33000000, 0x00AAAAAA),
              FastColor.ARGB32.lerp(openAnimation, 0x33000000, 0x2F000000));
        graphics.pose().popPose();
    }

    protected float calculateMarkRoration(Mark mark) {
        if (context.markFacing().getAxis().isHorizontal()) {
            return mark.orientation().getRotation() + mark.symbol().value().rotationOffset();
        } else if (context.markFacing() == Direction.UP) {
            int viewRot = (player.getDirection().get2DDataValue() * 90 + 180) % 360;
            return mark.orientation().getRotation() - viewRot + mark.symbol().value().rotationOffset();
        } else {
            // Down orientation for some symbols is still wrong in some cases.
            // Maybe I'll fix it in the future.
            int viewRot = (player.getDirection().get2DDataValue() * 90) % 360;
            int orientationRot = mark.orientation().getRotation();
            if (mark.symbol().value().orientationBehavior() == MarkSymbol.OrientationBehavior.FULL
                  || mark.symbol().value().orientationBehavior() == MarkSymbol.OrientationBehavior.CARDINAL) {
                orientationRot = (orientationRot + 180) % 360;
            }
            return orientationRot - viewRot + mark.symbol().value().rotationOffset();
        }
    }

    @SuppressWarnings("DataFlowIssue")
    protected void renderBlockSurface(GuiGraphics graphics, int mouseX, int mouseY, int x, int y) {
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.applyModelViewMatrix();
        Lighting.setupForFlatItems();

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);

        int xRot = 0;
        int yRot = 0;

        if (markFacing == Direction.UP) {
            xRot = -90;
        } else if (markFacing == Direction.DOWN) {
            xRot = 90;
        } else if (markFacing == Direction.EAST) {
            yRot = 270;
        } else if (markFacing == Direction.NORTH) {
            yRot = 180;
        } else if (markFacing == Direction.WEST) {
            yRot = 90;
        }

        if (markFacing == Direction.UP || markFacing == Direction.DOWN) {
            yRot = player.getDirection().get2DDataValue() * 90 + 180;
        }

        graphics.pose().translate(SYMBOL_SIZE / 2f, SYMBOL_SIZE / 2f, SYMBOL_SIZE / 2f);
        graphics.pose().mulPose(Axis.XP.rotationDegrees(xRot - 0.1f));
        graphics.pose().mulPose(Axis.YP.rotationDegrees(yRot - 0.1f));
        graphics.pose().translate(-SYMBOL_SIZE / 2f, -SYMBOL_SIZE / 2f, -SYMBOL_SIZE / 2f);

        graphics.pose().translate(0, SYMBOL_SIZE, 0);
        graphics.pose().scale(1.0F, -1.0F, 1.0F);
        graphics.pose().scale(SYMBOL_SIZE, SYMBOL_SIZE, SYMBOL_SIZE);

        MultiBufferSource.BufferSource bufferSource = graphics.bufferSource();

        minecraft.getBlockRenderer().renderSingleBlock(surfaceState, graphics.pose(), bufferSource,
              LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
        RenderSystem.applyModelViewMatrix();

        graphics.pose().popPose();
    }

    // -- Input

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }

        if (keyCode == InputConstants.KEY_ADD || keyCode == InputConstants.KEY_EQUALS) {
            scroll(changePerScroll);
            return true;
        }

        if (keyCode == 333 /*KEY_SUBTRACT*/ || keyCode == InputConstants.KEY_MINUS) {
            scroll(-changePerScroll);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHoveringOverScrollThumb) {
            draggingScrollThumb = true;
            lastScrollingMouseY = 0;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingScrollThumb) {
            draggingScrollThumb = false;
            return true;
        }

        if (isHoveringOverScrollBar) {
            if (mouseY < scrollThumbY) {
                scroll(-changePerScroll * 3);
            } else {
                scroll(changePerScroll * 3);
            }
            return true;
        }

        if (!mouseWasReleased && level.getGameTime() - openTimestamp < 4) {
            mouseWasReleased = true;
            return true;
        }

        if (button == 0 || !mouseWasReleased) {
            if (hoveredMark != null) {
                tryDrawSymbol(hoveredMark.symbol());
            }
        }

        this.onClose();
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollThumb && button == InputConstants.MOUSE_BUTTON_LEFT) {
            int scrollBarSize = scrollBarYEnd - scrollBarYStart;
            double value = maxScroll / (scrollBarSize - scrollThumbSize);
            scroll(dragY * value);
            lastScrollingMouseY = mouseY;
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0) {
            scroll(-(scrollY * changePerScroll));
            return true;
        }
        return false;
    }

    protected void tryDrawSymbol(Holder<MarkSymbol> symbol) {
        new DrawMarkServerboundPacket(symbol, context).sendToServer();
        player.swing(context.hand());
    }

    public void scroll(double change) {
        scrollTo(scroll + change);
    }

    public void scrollTo(double position) {
        scroll = Mth.clamp(position, 0, maxScroll);
    }
}
