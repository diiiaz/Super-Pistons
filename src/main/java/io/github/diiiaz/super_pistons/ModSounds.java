package io.github.diiiaz.super_pistons;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {

    public static final SoundEvent BLOCK_SUPER_PISTON_MOVE = registerSound("super_piston_move");

    private static SoundEvent registerSound(String name) {
        Identifier id = new Identifier(Mod.ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void register() {}


}
