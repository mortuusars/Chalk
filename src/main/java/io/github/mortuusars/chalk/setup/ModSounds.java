package io.github.mortuusars.chalk.setup;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final RegistryObject<SoundEvent> CHALK_BROKEN = Registry.SOUNDS.register("item.chalk.chalk_broken",
            () -> new SoundEvent(Chalk.resource("item.chalk.chalk_broken")));

    public static final RegistryObject<SoundEvent> MARK_DRAW = Registry.SOUNDS.register("item.chalk.draw",
            () -> new SoundEvent(Chalk.resource("item.chalk.draw")));

    public static final RegistryObject<SoundEvent> MARK_GLOW_APPLIED = Registry.SOUNDS.register("item.chalk.glow_applied",
            () -> new SoundEvent(Chalk.resource("item.chalk.glow_applied")));

    public static final RegistryObject<SoundEvent> MARK_REMOVED = Registry.SOUNDS.register("block.chalk.removed",
            () -> new SoundEvent(Chalk.resource("block.chalk.removed")));

    public static void register() {}
}
