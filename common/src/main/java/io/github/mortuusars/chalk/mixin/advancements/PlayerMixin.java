package io.github.mortuusars.chalk.mixin.advancements;

import io.github.mortuusars.chalk.event.CommonEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {
    // Using mixin instead of events to have the same code for neoforge and fabric
    @Inject(method = "stopSleepInBed", at = @At("HEAD"))
    private void onStopSleeping(boolean forcefulWakeUp, boolean updateLevelList, CallbackInfo ci) {
        if (((Player)(Object)this) instanceof ServerPlayer serverPlayer) {
            CommonEvents.onStoppedSleeping(serverPlayer);
        }
    }
}
