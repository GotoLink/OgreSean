package ogresean;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.config.Configuration;
import ogresean.bats.Bats;
import ogresean.talkingpig.TalkingPig;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "ogreseanmods", name = "OgreSean Mods", version = "0.2")
public class OgreSeanMods {
	@Instance("ogreseanmods")
	public static OgreSeanMods instance;
	private static boolean creeper;
	private static boolean bats;
	private static boolean pig;

	@EventHandler
	public void load(FMLInitializationEvent event) {
		if (creeper) {
			CreeperSwarm.load(event.getSide().isClient(), this);
		}
		if (bats) {
			new Bats().load(event.getSide().isClient(), this);
		}
		if (pig) {
			new TalkingPig().load(event.getSide().isClient(), this);
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
			FMLCommonHandler.instance().bus().register(new RandomItemSpawn());
		}
		if (config.get("General", "Enable RainingBombSquid", true).getBoolean(true)) {
            FMLCommonHandler.instance().bus().register(new RainingBombSquid());
		}
		bats = config.get("General", "Enable Bats", true).getBoolean(true);
		if (bats) {
			bats = Bats.preLoad(config);
		}
		pig = config.get("General", "Enable TalkingPig", false).getBoolean(false);

        if(config.get("General", "Enable Deadly caves", true).getBoolean(true)){
            new DeadlyCaves().setConfigurationSettings(config);
        }
        config.save();
	}
}
