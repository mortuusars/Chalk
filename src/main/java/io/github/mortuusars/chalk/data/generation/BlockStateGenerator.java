package io.github.mortuusars.chalk.data.generation;


import io.github.mortuusars.chalk.Chalk;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockStateGenerator extends BlockStateProvider {
    public BlockStateGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen.getPackOutput(), Chalk.ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        Chalk.Blocks.MARKS.forEach((color, block) -> {
            simpleBlock(block.get(), models().getExistingFile(new ResourceLocation("chalk:block/chalk_mark")));
        });
    }
}
