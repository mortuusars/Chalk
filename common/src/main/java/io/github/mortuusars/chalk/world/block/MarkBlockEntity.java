package io.github.mortuusars.chalk.world.block;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.chalk.MarkSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

public class MarkBlockEntity extends BlockEntity {
    public static final Logger LOGGER = LogUtils.getLogger();

    protected MarkSet marks = new MarkSet();

    protected MarkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public MarkBlockEntity(BlockPos pos, BlockState blockState) {
        super(Chalk.BlockEntityTypes.MARK.get(), pos, blockState);
    }

    public MarkSet getMarks() {
        return marks;
    }

    public void marksChanged() {
        if (getBlockState().getBlock() instanceof MarkBlock markBlock) {
            assert getLevel() != null;
            markBlock.recheckAndUpdate(getLevel(), getBlockPos());
        }
        setChanged();
        if (level != null) {
            // Forces client to update
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            return output.buildResult();
        }
    }

    // --

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        output.store("Marks", MarkSet.CODEC, marks);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        marks = input.read("Marks", MarkSet.CODEC).orElseGet(MarkSet::new);
        if (level != null && level.isClientSide()) {
            // Updates model on the client
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
