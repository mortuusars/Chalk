package io.github.mortuusars.chalk.blocks;

import net.minecraft.util.IStringSerializable;

public enum MarkSymbol implements IStringSerializable {
    NONE("none"),
    CROSS("cross");

    private String _name;

    private MarkSymbol(String name){
        _name = name;
    }

    @Override
    public String getSerializedName() {
        return _name;
    }
}
