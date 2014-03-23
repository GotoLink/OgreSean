package ogresean;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.Configuration;
import ogresean.bats.Bats;
import ogresean.talkingpig.TalkingPig;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import java.util.Iterator;

@Mod(modid = "ogreseanmods", name = "OgreSean Mods", version = "0.2")
public class OgreSeanMods {
	@Instance("ogreseanmods")
	public static OgreSeanMods instance;
	private static CreeperSwarm creeper;
	private static Bats bats;
	private static boolean pig;
    private static mod_EasyNPCs easyNPCs;

	@EventHandler
	public void load(FMLInitializationEvent event) {
		if (creeper!=null) {
			creeper.load(event.getSide().isClient(), this);
		}
		if (bats!=null) {
			bats.load(event.getSide().isClient(), this);
		}
		if (pig) {
			new TalkingPig().load(event.getSide().isClient(), this);
		}
        if(easyNPCs!=null){
            easyNPCs.load(event.getSide().isClient(), this);
        }
	}

	@EventHandler
	public void preLoad(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile(), true);
		if (config.get("General", "Enable CreeperSwarm", true).getBoolean(true)) {
            creeper = new CreeperSwarm();
			creeper.preLoad(config);
		}
		if (config.get("General", "Enable RandomItemSpawn", true).getBoolean(true)) {
			FMLCommonHandler.instance().bus().register(new RandomItemSpawn());
		}
		if (config.get("General", "Enable RainingBombSquid", true).getBoolean(true)) {
            FMLCommonHandler.instance().bus().register(new RainingBombSquid());
		}
		if (config.get("General", "Enable Bats", true).getBoolean(true)) {
            bats = new Bats();
			bats.preLoad(config);
		}
		pig = config.get("General", "Enable TalkingPig", false).getBoolean(false);

        if(config.get("General", "Enable Deadly caves", true).getBoolean(true)){
            new DeadlyCaves().setConfigurationSettings(config);
        }
        if(config.get("General", "Enable EasyNPC", true).getBoolean(true)){
            easyNPCs = new mod_EasyNPCs();
            easyNPCs.loadConfig(event);
        }
        config.save();
	}

    public static BiomeGenBase[] getSpawn(){
        BiomeGenBase[] result = new BiomeGenBase[BiomeGenBase.explorationBiomesList.size()];
        int i = 0;
        for(Iterator<BiomeGenBase> itr = BiomeGenBase.explorationBiomesList.iterator(); itr.hasNext(); ++i){
            result[i] = itr.next();
        }
        return result;
    }
}
