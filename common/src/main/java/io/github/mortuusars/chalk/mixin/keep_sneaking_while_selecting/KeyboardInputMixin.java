package io.github.mortuusars.chalk.mixin.keep_sneaking_while_selecting;

import io.github.mortuusars.chalk.client.gui.screens.SymbolSelectScreen;
import io.github.mortuusars.mortaar.client.Minecrft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends ClientInput {
    /**
     * Purpose of this is to keep player crouched while they are choosing a symbol.
     * Removes jerking the view up/down, and overall makes it look smooth.
     */
    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickEnd(CallbackInfo ci) {
        if (Minecrft.get().gui.screen() instanceof SymbolSelectScreen)
            keyPresses = new Input(
                  keyPresses.forward(),
                  keyPresses.backward(),
                  keyPresses.left(),
                  keyPresses.right(),
                  keyPresses.jump(),
                  true,
                  keyPresses.sprint()
            );
    }
}
