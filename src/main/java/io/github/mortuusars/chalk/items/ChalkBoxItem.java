package io.github.mortuusars.chalk.items;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.menus.ChalkBoxItemStackHandler;
import io.github.mortuusars.chalk.menus.ChalkBoxMenu;
import io.github.mortuusars.chalk.setup.ModItems;
import io.github.mortuusars.chalk.setup.ModTags;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.apache.logging.log4j.core.jmx.Server;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ChalkBoxItem extends Item {

    public ChalkBoxItem(Properties properties) {
        super(properties);
    }


    @SuppressWarnings("ConstantConditions")
    public float getSelectedChalkColor(ItemStack stack){
        return stack.hasTag() ? stack.getTag().getFloat(ChalkBox.SELECTED_CHALK_TAG_KEY) : 0f;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {


        context.getLevel().playSound(context.getPlayer(), context.getClickedPos(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1f, 1f);
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {

        ItemStack usedStack = player.getItemInHand(usedHand);

        if (!usedStack.is(this) || !(player instanceof ServerPlayer))
            return InteractionResultHolder.pass(usedStack);

        if (player.isSecondaryUseActive()){

            List<ItemStack> contents = ChalkBox.getContents(usedStack);
            if (contents.size() == 0){
                NonNullList<ItemStack> items = NonNullList.withSize(ChalkBox.SLOTS, ItemStack.EMPTY);
                items.set(3, new ItemStack(ModItems.LIGHT_BLUE_CHALK.get()));
                ChalkBox.setContents(usedStack, items);
            }
            else {
                contents.forEach(s -> {
                    if (!s.isEmpty())
                        player.displayClientMessage(s.getHoverName(), true);
                });
            }
        }
        else {
            if (!level.isClientSide){
                NetworkHooks.openGui((ServerPlayer) player,
                        new SimpleMenuProvider( (containerID, playerInventory, playerEntity) ->
                                new ChalkBoxMenu(containerID, playerInventory, usedStack, new ChalkBoxItemStackHandler(usedStack)),
                                new TranslatableComponent("chalk.container.chalk_box")), buffer -> buffer.writeItem(usedStack.copy()));
            }
        }

        return InteractionResultHolder.sidedSuccess(usedStack, level.isClientSide);
    }



    @SuppressWarnings("ConstantConditions")
    private @Nullable DyeColor getSelectedColor(ItemStack stack){
        return stack.hasTag() ? decodeColor(stack.getTag().getFloat(ChalkBox.SELECTED_CHALK_TAG_KEY)) : null;
    }

    private @Nullable DyeColor decodeColor(float value){
        if (value < 1)
            return null;

        return DyeColor.byId(((int) (value - 1)));
    }

    private float encodeColor(DyeColor color){
        return color.getId() + 1;
    }
}
