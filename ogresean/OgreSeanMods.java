package ogresean;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.Configuration;
import ogresean.bats.Bats;
import ogresean.talkingpig.TalkingPig;

import java.util.ArrayList;

@Mod(modid = "ogreseanmods", name = "OgreSean Mods", version = "$version")
public class OgreSeanMods {
    @Instance("ogreseanmods")
    public static OgreSeanMods instance;
    private static CreeperSwarm creeper;
    private static Bats bats;
    private static boolean pig;
    private static EasyNPCs easyNPCs;

    @EventHandler
    public void load(FMLInitializationEvent event) {
        if (creeper != null) {
            creeper.load(event.getSide().isClient(), this);
        }
        if (bats != null) {
            bats.load(event.getSide().isClient(), this);
        }
        if (pig) {
            new TalkingPig().load(event.getSide().isClient(), this);
        }
        if (easyNPCs != null) {
            easyNPCs.load(event.getSide().isClient(), this);
        }
    }

    @EventHandler
    public void preLoad(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile(), true);
        if (config.get("General", "Enable CreeperSwarm", true).getBoolean()) {
            creeper = new CreeperSwarm();
            creeper.preLoad(config);
        }
        if (config.get("General", "Enable RandomItemSpawn", true).getBoolean()) {
            FMLCommonHandler.instance().bus().register(new RandomItemSpawn());
        }
        if (config.get("General", "Enable RainingBombSquid", true).getBoolean()) {
            FMLCommonHandler.instance().bus().register(new RainingBombSquid());
        }
        if (config.get("General", "Enable Bats", true).getBoolean()) {
            bats = new Bats();
            bats.preLoad(config);
        }
        pig = config.get("General", "Enable TalkingPig", false).getBoolean();

        if (config.get("General", "Enable Deadly caves", true).getBoolean()) {
            new DeadlyCaves().setConfigurationSettings(config);
        }
        if (config.get("General", "Enable EasyNPC", true).getBoolean()) {
            easyNPCs = new EasyNPCs();
            easyNPCs.preLoad(config);
        }
        if (config.hasChanged())
            config.save();
        if (event.getSourceFile().getName().endsWith(".jar") && event.getSide().isClient()) {
            try {
                Class.forName("mods.mud.ModUpdateDetector").getDeclaredMethod("registerMod", ModContainer.class, String.class, String.class).invoke(null,
                        FMLCommonHandler.instance().findContainerFor(this),
                        "https://raw.github.com/GotoLink/OgreSean/master/update.xml",
                        "https://raw.github.com/GotoLink/OgreSean/master/changelog.md"
                );
            } catch (Throwable ignored) {
            }
        }
    }

    public static BiomeGenBase[] getSpawn() {
        ArrayList<BiomeGenBase> result = new ArrayList<BiomeGenBase>();
        for (BiomeGenBase itr : BiomeGenBase.getBiomeGenArray()) {
            if (itr != null) {
                result.add(itr);
            }
        }
        return result.toArray(new BiomeGenBase[result.size()]);
    }
}
