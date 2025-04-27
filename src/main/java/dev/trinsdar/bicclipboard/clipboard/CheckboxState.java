package dev.trinsdar.bicclipboard.clipboard;

import net.minecraft.util.StringRepresentable;

public enum CheckboxState implements StringRepresentable {
    EMPTY,
    CHECK,
    X;

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }

    public CheckboxState cycle(){
        return switch (this) {
            case EMPTY -> CHECK;
            case CHECK -> X;
            case X -> EMPTY;
        };
    }
}
