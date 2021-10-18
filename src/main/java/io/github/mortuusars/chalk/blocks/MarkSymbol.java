package io.github.mortuusars.chalk.blocks;


import net.minecraft.util.StringRepresentable;

public enum MarkSymbol implements StringRepresentable {
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
