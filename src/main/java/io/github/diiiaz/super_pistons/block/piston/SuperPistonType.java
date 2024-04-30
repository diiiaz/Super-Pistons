package io.github.diiiaz.super_pistons.block.piston;

import net.minecraft.util.StringIdentifiable;

public enum SuperPistonType implements StringIdentifiable
{
    DEFAULT("normal"),
    STICKY("sticky");

    private final String name;

    private SuperPistonType(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}