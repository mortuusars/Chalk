package io.github.mortuusars.chalk.setup;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public class ModSoundEvents {
    public static final RegistryObject<SoundEvent> CHALK_BROKEN = Registry.SOUNDS.register("item.chalk_broken",
            () -> new SoundEvent(Chalk.resource("item.chalk_broken")));
    public static final RegistryObject<SoundEvent> CHALK_BOX_CHANGE = Registry.SOUNDS.register("item.chalk_box_change",
            () -> new SoundEvent(Chalk.resource("item.chalk_box_change")));
    public static final RegistryObject<SoundEvent> CHALK_BOX_OPEN = Registry.SOUNDS.register("item.chalk_box_open",
            () -> new SoundEvent(Chalk.resource("item.chalk_box_open")));
    public static final RegistryObject<SoundEvent> MARK_DRAW = Registry.SOUNDS.register("item.chalk_draw",
            () -> new SoundEvent(Chalk.resource("item.chalk_draw")));
    public static final RegistryObject<SoundEvent> MARK_GLOW_APPLIED = Registry.SOUNDS.register("block.mark_glow_applied",
            () -> new SoundEvent(Chalk.resource("block.mark_glow_applied")));
    public static final RegistryObject<SoundEvent> MARK_REMOVED = Registry.SOUNDS.register("block.mark_removed",
            () -> new SoundEvent(Chalk.resource("block.mark_removed")));

    public static void register() {}
}
