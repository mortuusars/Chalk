package io.github.mortuusars.chalk.world.item.component;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.world.item.ChalkItem;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ChalkBoxContents implements TooltipComponent {
    public static final int SLOTS = 10;
    public static final int CHALK_SLOTS = 9;
    public static final int GLOWINGS_SLOT = SLOTS - 1;

    private static final Codec<Optional<ItemStackTemplate>> OPTIONAL_TEMPLATE_CODEC = Codec.of(
          new Encoder<>() {
              @Override
              public <T> DataResult<T> encode(Optional<ItemStackTemplate> value, DynamicOps<T> ops, T prefix) {
                  return value.isPresent()
                        ? ItemStackTemplate.CODEC.encode(value.get(), ops, prefix)
                        : ops.mapBuilder().build(prefix);
              }
          },
          new Decoder<>() {
              @Override
              public <T> DataResult<Pair<Optional<ItemStackTemplate>, T>> decode(DynamicOps<T> ops, T input) {
                  return ops.getMap(input).flatMap(map -> map.entries().findAny().isEmpty()
                        ? DataResult.success(Pair.of(Optional.empty(), input))
                        : ItemStackTemplate.CODEC.decode(ops, input).map(pair -> pair.mapFirst(Optional::of)));
              }
          }
    );

    public static final Codec<ChalkBoxContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                OPTIONAL_TEMPLATE_CODEC.listOf(0, SLOTS)
                      .optionalFieldOf("items", List.of())
                      .forGetter(ChalkBoxContents::getTemplatesTrimmed),
                Codec.INT.optionalFieldOf("glow", 0).forGetter(ChalkBoxContents::glow),
                Codec.INT.optionalFieldOf("selected", -1).forGetter(ChalkBoxContents::selected))
          .apply(instance, ChalkBoxContents::fromTemplates));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChalkBoxContents> STREAM_CODEC = StreamCodec.composite(
          ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), ChalkBoxContents::getItemsTrimmed,
          ByteBufCodecs.INT, ChalkBoxContents::glow,
          ByteBufCodecs.INT, ChalkBoxContents::selected,
          ChalkBoxContents::new
    );

    public static final ChalkBoxContents EMPTY = fromTemplates(List.of(), 0, -1);

    private final List<Optional<ItemStackTemplate>> templates;
    private final int glow;
    private final int selected;
    private List<ItemStack> materializedItems;

    public ChalkBoxContents(List<ItemStack> items, int glow, int selected) {
        this(items.stream()
                    .map(stack -> stack.isEmpty() ? Optional.<ItemStackTemplate>empty() : Optional.of(ItemStackTemplate.fromNonEmptyStack(stack)))
                    .toList(),
              glow, selected, true);
    }

    public ChalkBoxContents(List<ItemStack> items, int glow) {
        this(items, glow, 0);
    }

    private ChalkBoxContents(List<Optional<ItemStackTemplate>> templates, int glow, int selected, boolean templateValues) {
        NonNullList<Optional<ItemStackTemplate>> normalized = NonNullList.withSize(SLOTS, Optional.empty());
        for (int i = 0; i < Math.min(templates.size(), SLOTS); i++) {
            normalized.set(i, templates.get(i));
        }
        this.templates = Collections.unmodifiableList(normalized);
        this.glow = Math.max(glow, 0);
        this.selected = correctSelected(selected);
    }

    public static ChalkBoxContents fromTemplates(List<Optional<ItemStackTemplate>> templates, int glow) {
        return fromTemplates(templates, glow, 0);
    }

    private static ChalkBoxContents fromTemplates(List<Optional<ItemStackTemplate>> templates, int glow, int selected) {
        return new ChalkBoxContents(templates, glow, selected, true);
    }

    private int correctSelected(int selected) {
        if (!isTemplateEmpty(selected) && selected < CHALK_SLOTS) {
            return selected;
        }

        if (selected <= -1) {
            for (int i = 0; i < CHALK_SLOTS; i++) {
                if (!isTemplateEmpty(i)) {
                    return i;
                }
            }
            return -1;
        }

        if (selected > CHALK_SLOTS - 1) {
            selected = CHALK_SLOTS - 1;
        }

        for (int index = selected; index < CHALK_SLOTS; index++) {
            if (!isTemplateEmpty(index)) {
                return index;
            }
        }

        for (int index = 0; index < selected; index++) {
            if (!isTemplateEmpty(index)) {
                return index;
            }
        }

        return -1;
    }

    public List<ItemStack> items() {
        if (materializedItems == null) {
            NonNullList<ItemStack> materialized = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
            for (int i = 0; i < SLOTS; i++) {
                if (!isTemplateEmpty(i)) {
                    materialized.set(i, templates.get(i).orElseThrow().create());
                }
            }
            materializedItems = Collections.unmodifiableList(materialized);
        }
        return materializedItems;
    }

    public int glow() {
        return glow;
    }

    public int selected() {
        return selected;
    }

    private List<Optional<ItemStackTemplate>> templates() {
        return templates;
    }

    private List<Optional<ItemStackTemplate>> getTemplatesTrimmed() {
        int lastNonEmpty = templates.size() - 1;
        while (lastNonEmpty >= 0 && isTemplateEmpty(lastNonEmpty)) {
            lastNonEmpty--;
        }
        return templates.subList(0, lastNonEmpty + 1);
    }

    private boolean isTemplateEmpty(int index) {
        if (index < 0 || index >= templates.size()) {
            return true;
        }
        return templates.get(index).isEmpty();
    }

    public boolean isEmpty() {
        return this == EMPTY || templates.stream().allMatch(Optional::isEmpty);
    }

    public int size() {
        return SLOTS;
    }

    public boolean hasChalks() {
        return templates.stream().flatMap(Optional::stream).anyMatch(template -> template.item().value() instanceof ChalkItem);
    }

    public int getChalkCount() {
        int count = 0;
        for (int i = 0; i < CHALK_SLOTS; i++) {
            if (!isTemplateEmpty(i)) count++;
        }
        return count;
    }

    /** Items should not be modified. Use {@link ChalkBoxContents#copyItems()} if modification is needed. */
    public List<ItemStack> getItems() {
        return items();
    }

    /** Items should not be modified. Use {@link ChalkBoxContents#copyItems()} if modification is needed. */
    public List<ItemStack> getItemsTrimmed() {
        int lastNonEmpty = templates.size() - 1;
        while (lastNonEmpty >= 0 && isTemplateEmpty(lastNonEmpty)) {
            lastNonEmpty--;
        }
        return items().subList(0, lastNonEmpty + 1);
    }

    public @NotNull ItemStack getItem(int index) {
        return index >= 0 && index < size() ? items().get(index) : ItemStack.EMPTY;
    }

    public @NotNull ItemStack copyItem(int index) {
        return getItem(index).copy();
    }

    public List<ItemStack> copyItems() {
        return Lists.transform(this.items(), ItemStack::copy);
    }

    public ItemStack getSelectedChalk() {
        return getItem(selected);
    }

    public static ChalkBoxContents of(ItemStack stack) {
        return stack.getOrDefault(Chalk.DataComponents.CHALK_BOX_CONTENTS, EMPTY);
    }

    public Mutable mutable() {
        return new Mutable(this);
    }

    public ChalkBoxContents withGlow(int glow) {
        return fromTemplates(templates, glow, selected);
    }

    public ChalkBoxContents withSelected(int selected) {
        return fromTemplates(templates, glow, selected);
    }

    @Override
    public boolean equals(Object another) {
        return this == another || another instanceof ChalkBoxContents other
              && templates.equals(other.templates)
              && glow == other.glow;
    }

    @Override
    public int hashCode() {
        return templates.hashCode() + glow;
    }

    @Override
    public String toString() {
        return "ChalkBoxContents{" +
              "items=" + templates +
              ", glow=" + glow +
              ", selected=" + selected +
              '}';
    }

    public static boolean canHold(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOTS) {
            return false;
        } else if (!stack.getItem().canFitInsideContainerItems()) {
            return false;
        } else if (slot == GLOWINGS_SLOT) {
            return Config.Server.GLOWING_ENABLED.get() && Config.Server.CHALK_BOX_GLOWING_ENABLED.get() && stack.is(Chalk.Tags.Items.GLOWINGS);
        } else {
            return stack.getItem() instanceof ChalkItem;
        }
    }

    public static boolean canHold(ItemStack stack) {
        if (!stack.getItem().canFitInsideContainerItems()) {
            return false;
        }
        return stack.getItem() instanceof ChalkItem
              || (Config.Server.GLOWING_ENABLED.get()
                  && Config.Server.CHALK_BOX_GLOWING_ENABLED.get()
                  && stack.is(Chalk.Tags.Items.GLOWINGS));
    }

    public static class Mutable {
        private List<ItemStack> items;
        private int glow;
        private int selected;

        public Mutable(ChalkBoxContents contents) {
            this.items = new ArrayList<>(contents.copyItems());
            this.glow = contents.glow;
            this.selected = contents.selected;
        }

        public ChalkBoxContents toImmutable() {
            return new ChalkBoxContents(items, glow, selected);
        }

        public ItemStack toImmutable(ItemStack stack) {
            stack.set(Chalk.DataComponents.CHALK_BOX_CONTENTS, new ChalkBoxContents(items, glow, selected));
            return stack;
        }

        public Mutable setItems(Container container) {
            for (int i = 0; i < items.size(); i++) {
                items.set(i, container.getItem(i));
            }
            return this;
        }

        public Mutable setItems(List<ItemStack> items) {
            this.items = items;
            return this;
        }

        public Mutable setItem(int index, ItemStack item) {
            items.set(index, item);
            return this;
        }

        public Mutable consumeGlow() {
            glow -= 1;
            return updateGlow();
        }

        public Mutable updateGlow() {
            if (glow <= 0 && !items.get(GLOWINGS_SLOT).isEmpty()) {
                items.get(GLOWINGS_SLOT).shrink(1);
                glow = Config.Server.CHALK_BOX_GLOWING_AMOUNT_PER_ITEM.get();
            }
            return this;
        }

        public Mutable setSelected(int slot) {
            selected = slot;
            return this;
        }
    }
}
