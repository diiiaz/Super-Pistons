package io.github.diiiaz.super_pistons;

import io.github.diiiaz.super_pistons.entity.ModBlockEntities;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mod implements ModInitializer {
	public static final String ID = "super-pistons";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	@Override
	public void onInitialize() {
		ModItemGroup.register();
		ModBlockEntities.registerAllBlockEntities();
		ModSounds.register();
	}
}