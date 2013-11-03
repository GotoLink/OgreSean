package ogresean;

import net.minecraftforge.common.Configuration;
import ogresean.bats.Bats;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "ogreseanmods", name = "OgreSean Mods", version = "0.1")
@NetworkMod(clientSideRequired = true)
public class OgreSeanMods {
	private static boolean creeper;
	private static boolean bats;

	@EventHandler
	public void load(FMLInitializationEvent event) {
		if (creeper) {
			CreeperSwarm.load(event.getSide().isClient(), this);
		}
		if (bats) {
			new Bats().load(event.getSide().isClient(), this);
		}
	}

	@EventHandler
	public void preLoad(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile(), true);
		creeper = config.get("General", "Enable CreeperSwarm", true).getBoolean(true);
		if (creeper) {
			CreeperSwarm.preLoad(config);
		}
		if (config.get("General", "Enable RandomItemSpawn", true).getBoolean(true)) {
			TickRegistry.registerScheduledTickHandler(new RandomItemSpawn(), Side.SERVER);
		}
		if (config.get("General", "Enable RainingBombSquid", true).getBoolean(true)) {
			TickRegistry.registerTickHandler(new RainingBombSquid(), Side.SERVER);
		}
		bats = config.get("General", "Enable Bats", true).getBoolean(true);
		if (bats) {
			bats = Bats.preLoad(config);
		}
		config.save();
	}
}
