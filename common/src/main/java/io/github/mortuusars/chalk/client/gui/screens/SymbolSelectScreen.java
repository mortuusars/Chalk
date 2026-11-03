package io.github.mortuusars.chalk.client.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.network.packet.serverbound.DrawMarkServerboundPacket;
import io.github.mortuusars.chalk.world.chalk.Mark;
import io.github.mortuusars.chalk.world.chalk.MarkDrawingContext;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import io.github.mortuusars.chalk.world.item.MarkDrawable;
import io.github.mortuusars.mortaar.client.Minecrft;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public class SymbolSelectScreen extends Screen {
    protected static final Identifier BORDER_REGULAR_SPRITE = Chalk.resource("symbol_selection/border_regular");
    protected static final Identifier BORDER_GOLD_SPRITE = Chalk.resource("symbol_selection/border_gold");
    protected static final Identifier BORDER_HOVERED_SPRITE = Chalk.resource("symbol_selection/border_hovered");

    protected int SYMBOL_SIZE = 32;
    protected int SYMBOL_SPACING = 8;
    protected int SYMBOL_BORDER_THICKNESS = SYMBOL_SIZE / 16;
    protected int GROUP_LABEL_BOTTOM_PADDING = 4;
    protected int DEFAULT_BORDER_COLOR = 0xFF252525;

    protected final LocalPlayer player;
    protected final ClientLevel level;
    protected final long openTimestamp;
    protected final long openTimestampMs = System.currentTimeMillis();
    protected float openTime = 0f;

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

    protected float changePerScroll;
    protected float maxScroll = 999999.0f;
    protected float scroll = 0;
    protected float currentScroll = 0;
    protected int scrollBarWidth;
    protected int scrollBarX;
    protected int scrollBarYStart;
    protected int scrollBarYEnd;
    protected float scrollThumbY;
    protected int scrollThumbSize;
    protected boolean draggingScrollThumb;
    protected boolean isHoveringOverScrollBar;
    protected boolean isHoveringOverScrollThumb;
    protected float lastScrollingMouseY;

    @Nullable
    protected Mark hoveredMark;
    protected boolean mouseWasReleased;
    protected float edgeMargin = 0.15f;

    public SymbolSelectScreen(List<Holder<MarkSymbol>> symbols, MarkDrawingContext context) {
        super(Component.empty());
        this.symbols = symbols;
        this.context = context;

        this.player = Minecrft.player();
        this.level = Minecrft.level();
        this.openTimestamp = level.getGameTime();

        itemStack = player.getItemInHand(context.hand());
        // Screen shouldn't be opened with other items
        drawable = ((MarkDrawable) itemStack.getItem());

        color = ARGB.opaque(drawable.getMarkDrawingColor(itemStack));
        r = (float) (this.color >> 16 & 255) / 255.0F;
        g = (float) (this.color >> 8 & 255) / 255.0F;
        b = (float) (this.color & 255) / 255.0F;
        hoverBorderColor = ARGB.srgbLerp(0.2f, ARGB.opaque(this.color), 0xFFFFFFFF);

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
    public boolean isInGameUi() {
        return true;
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

        changePerScroll = height / 8.0f;
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
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        openAnimation = Mth.clamp((System.currentTimeMillis() - openTimestampMs) / 500f, 0f, 1f);
        openAnimation = 1f - openAnimation;
        openAnimation *= openAnimation * openAnimation;
        openAnimation = 1f - openAnimation;

        openTime += partialTick;

        handleScreenEdgeScrolling(mouseY, partialTick);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        try {
            extractMarks(graphics, mouseX, mouseY, partialTick);
        } catch (Exception e) {
            Chalk.LOGGER.error(e);
        }

        // Top and bottom gradients
        graphics.fillGradient(0, 0, width, (int) (height * 0.08f),
              ARGB.srgbLerp(openAnimation, 0x007F7F7F, 0x44000000), 0x007F7F7F);
        graphics.fillGradient(0, (int) (height * 0.92f), width, height, 0x007F7F7F,
              ARGB.srgbLerp(openAnimation, 0x00000000, 0x44000000));

        if (maxScroll > 0) {
            extractScrollBar(graphics, mouseX, mouseY, partialTick);
        }
    }

    public void extractScrollBar(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        scrollBarWidth = 3;
        scrollBarX = (int) (width - height * 0.2f);
        scrollBarYStart = (int) (height * 0.2f);
        scrollBarYEnd = (int) (height * 0.8f);

        graphics.fill(scrollBarX - 1, scrollBarYStart - 1, scrollBarX + scrollBarWidth + 1, scrollBarYEnd + 1, 0x55111111);
        graphics.fill(scrollBarX, scrollBarYStart, scrollBarX + scrollBarWidth, scrollBarYEnd, 0x55AAAAAA);

        float progress = currentScroll / maxScroll;
        float thumbRatio = height * 0.5f / contentHeight;
        int yArea = scrollBarYEnd - scrollBarYStart;
        scrollThumbSize = (int) Math.max(height * 0.075f, yArea * thumbRatio);
        scrollThumbY = scrollBarYStart + ((yArea - scrollThumbSize) * progress);

        graphics.pose().pushMatrix();
        graphics.pose().translate(scrollBarX, scrollThumbY);
        isHoveringOverScrollBar = mouseX >= scrollBarX && mouseX < scrollBarX + scrollBarWidth && mouseY >= scrollBarYStart && mouseY < scrollBarYEnd;
        isHoveringOverScrollThumb = isHoveringOverScrollBar && mouseY >= scrollThumbY && mouseY < scrollThumbY + scrollThumbSize;
        int thumbColor = draggingScrollThumb || isHoveringOverScrollThumb ? 0xFFFFFFFF : 0xBBFFFFFF;
        graphics.fill(0, 0, scrollBarWidth, scrollThumbSize, thumbColor);
        graphics.pose().popMatrix();
    }

    protected void handleScreenEdgeScrolling(int mouseY, float partialTick) {
        if (draggingScrollThumb) {
            return;
        }

        float screenScrollArea = Math.max(20, height * 0.1f);

        if (mouseY < screenScrollArea) {
            float strength = Mth.clamp((screenScrollArea - mouseY) / screenScrollArea, 0, 1);
            scroll(-height * (0.15f * strength * partialTick));
        }

        if (mouseY > height - screenScrollArea) {
            float strength = Mth.clamp((mouseY - (height - screenScrollArea)) / screenScrollArea, 0, 1);
            scroll(height * (0.15f * strength * partialTick));
        }
    }

    protected void extractMarks(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        hoveredMark = null;

        // Animates scroll smoothly
        if (Math.abs(currentScroll - scroll) > 0.001f) {
            currentScroll = Mth.lerp(Math.min(1, partialTick), currentScroll, scroll);
        } else {
            currentScroll = scroll;
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(0.0f, -Mth.frac(currentScroll));

        int y = contentY - (int) currentScroll;

        for (Map.Entry<Component, List<List<Mark>>> group : groupsAndRows.entrySet()) {
            int labelX = width / 2 - font.width(group.getKey()) / 2;
            int textOutlineColor = ARGB.srgbLerp(openAnimation, 0x22000000, DEFAULT_BORDER_COLOR);
            int textColor = ARGB.srgbLerp(openAnimation, 0x22FFFFFF, 0xFFFFFFFF);
            graphics.text(font, group.getKey(), labelX - 1, y, textOutlineColor, false);
            graphics.text(font, group.getKey(), labelX - 1, y + 1, textOutlineColor, false);
            graphics.text(font, group.getKey(), labelX - 1, y - 1, textOutlineColor, false);
            graphics.text(font, group.getKey(), labelX + 1, y, textOutlineColor, false);
            graphics.text(font, group.getKey(), labelX + 1, y - 1, textOutlineColor, false);
            graphics.text(font, group.getKey(), labelX + 1, y + 1, textOutlineColor, false);
            graphics.text(font, group.getKey(), labelX, y + 1, textOutlineColor, false);
            graphics.text(font, group.getKey(), labelX, y - 1, textOutlineColor, false);
            graphics.text(font, group.getKey(), labelX, y, textColor, false);

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

        graphics.pose().popMatrix();

        if (hoveredMark != null) {
            extractMarkTooltip(graphics, mouseX, mouseY + SYMBOL_SIZE, partialTick, hoveredMark);
        }
    }

    public void extractMarkTooltip(GuiGraphicsExtractor graphics, int x, int y, float partialTick, Mark mark) {
        mark.symbol().unwrapKey()
              .map(ResourceKey::identifier)
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

                  graphics.setTooltipForNextFrame(font, lines, x, y);
              });
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, width, height,
              ARGB.srgbLerp(openAnimation, 0x00000000, 0x40000000),
              ARGB.srgbLerp(openAnimation, 0x00000000, 0x40000000));
    }

    protected void renderMarkButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY, Mark mark, int x, int y, boolean isHovering) {
        Identifier borderSprite = isHovering
              ? BORDER_HOVERED_SPRITE
              : mark.symbol().value().supporterOnly()
              ? BORDER_GOLD_SPRITE
              : BORDER_REGULAR_SPRITE;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, borderSprite, x - SYMBOL_BORDER_THICKNESS, y - SYMBOL_BORDER_THICKNESS,
              SYMBOL_SIZE + SYMBOL_BORDER_THICKNESS * 2, SYMBOL_SIZE + SYMBOL_BORDER_THICKNESS * 2);

        renderBlockSurface(graphics, x, y);

        graphics.pose().pushMatrix();
        float rotation = calculateMarkRoration(mark);
        graphics.pose().rotateAbout((float)Math.toRadians(rotation), x + SYMBOL_SIZE / 2f, y + SYMBOL_SIZE / 2f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, mark.symbol().value().texture().withPrefix("textures/").withSuffix(".png"),
              x, y, 0, 0, SYMBOL_SIZE, SYMBOL_SIZE, 16, 16, 16, 16, ARGB.multiplyAlpha(color, openAnimation));
        graphics.pose().popMatrix();

        // Shadow overlay
        graphics.fillGradient(x, y, x + SYMBOL_SIZE, y + SYMBOL_SIZE,
              ARGB.srgbLerp(openAnimation, 0x33000000, 0x00AAAAAA),
              ARGB.srgbLerp(openAnimation, 0x33000000, 0x2F000000));
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

    protected void renderBlockSurface(GuiGraphicsExtractor graphics, int x, int y) {
        if (surfaceState.isAir()) {
            return;
        }

        List<BlockStateModelPart> modelParts = Lists.newArrayList();
        minecraft.getModelManager().getBlockStateModelSet().get(surfaceState)
              .collectParts(net.minecraft.util.RandomSource.create(surfaceState.getSeed(context.markPos())), modelParts);

        // The face pointing from the surface block toward the player is the face touched by the mark.
        for (BlockStateModelPart modelPart : modelParts) {
            List<BakedQuad> faceQuads = modelPart.getQuads(markFacing);
            for (BakedQuad quad : faceQuads) {
                BakedQuad.MaterialInfo material = quad.materialInfo();
                int tint = ARGB.opaque(0xFFFFFF);
                if (material.isTinted()) {
                    BlockPos surfacePos = context.markPos().relative(markFacing.getOpposite());
                    var tintSource = minecraft.getBlockColors().getTintSource(surfaceState, material.tintIndex());
                    if (tintSource != null) {
                        tint = tintSource.colorInWorld(surfaceState, level, surfacePos);
                    }
                }

                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, material.sprite(), x, y, SYMBOL_SIZE, SYMBOL_SIZE, tint);
            }
        }
    }

    // -- Input

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        if (Minecraft.getInstance().options.keyInventory.matches(event)) {
            this.onClose();
            return true;
        }

        if (event.key() == InputConstants.KEY_ADD || event.key() == InputConstants.KEY_EQUALS) {
            scroll(changePerScroll);
            return true;
        }

        if (event.key() == 86 || event.key() == InputConstants.KEY_MINUS) {
            scroll(-changePerScroll);
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        if (isHoveringOverScrollThumb) {
            draggingScrollThumb = true;
            lastScrollingMouseY = 0;
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        if (draggingScrollThumb) {
            draggingScrollThumb = false;
            return true;
        }

        if (isHoveringOverScrollBar) {
            if (event.y() < scrollThumbY) {
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

        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT || !mouseWasReleased) {
            if (hoveredMark != null) {
                tryDrawSymbol(hoveredMark.symbol());
            }
        }

        this.onClose();
        return true;
    }

    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent event, double dx, double dy) {
        if (draggingScrollThumb && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            int scrollBarSize = scrollBarYEnd - scrollBarYStart;
            double value = maxScroll / (scrollBarSize - scrollThumbSize);
            scroll(dy * value);
            lastScrollingMouseY = (float) event.y();
            return true;
        }

        return super.mouseDragged(event, dx, dy);
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
        player.swing(context.hand(), SwingAnimation.DEFAULT, true);
    }

    public void scroll(double change) {
        scrollTo(scroll + change);
    }

    public void scrollTo(double position) {
        scroll = (float)Mth.clamp(position, 0, maxScroll);
    }
}
