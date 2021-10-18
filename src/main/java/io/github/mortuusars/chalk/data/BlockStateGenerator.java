package io.github.mortuusars.chalk.data;


import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.setup.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockStateGenerator extends BlockStateProvider {
    public BlockStateGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, Chalk.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModBlocks.MARKS.forEach( (name, block) -> {
            simpleBlock(block.get(), models().getExistingFile(new ResourceLocation("chalk:block/chalk_mark")));
        });
    }
}
