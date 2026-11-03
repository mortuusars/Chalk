package io.github.mortuusars.chalk.neoforge.client;

import io.github.mortuusars.chalk.world.chalk.Mark;
import net.neoforged.neoforge.model.data.ModelProperty;

public final class NeoForgeMarkModelData {
    public static final ModelProperty<Mark[]> MARKS = new ModelProperty<>();

    private NeoForgeMarkModelData() {
    }
}
