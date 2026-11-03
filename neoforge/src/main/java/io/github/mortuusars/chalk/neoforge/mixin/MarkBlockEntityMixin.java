package io.github.mortuusars.chalk.neoforge.mixin;

import io.github.mortuusars.chalk.neoforge.client.NeoForgeMarkModelData;
import io.github.mortuusars.chalk.world.block.MarkBlockEntity;
import io.github.mortuusars.chalk.world.chalk.MarkSet;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import net.neoforged.neoforge.model.data.ModelData;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MarkBlockEntity.class)
public abstract class MarkBlockEntityMixin implements IBlockEntityExtension {
    @Shadow
    protected MarkSet marks;

    @Override
    public @NonNull ModelData getModelData() {
        return marks.isEmpty()
              ? ModelData.EMPTY
              : ModelData.of(NeoForgeMarkModelData.MARKS, marks.copyArray());
    }

    @Inject(method = "marksChanged", at = @At("TAIL"))
    private void chalk$requestModelDataUpdateAfterMarksChanged(CallbackInfo callbackInfo) {
        requestModelDataUpdate();
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void chalk$requestModelDataUpdateAfterLoad(ValueInput input, CallbackInfo ci) {
        requestModelDataUpdate();
    }
}
