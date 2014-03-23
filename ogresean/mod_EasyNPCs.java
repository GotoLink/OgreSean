package ogresean;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.WorldEvent;

//uses EntityEasyNPC
/*
 * Coding Plan for gameplay:
 * 0. count number of EasyNPC textures when game loaded.
 * 1. When new world started, load count of NPCs from world's own special file
 * 2. NPCs will or will not spawn based on current NPC count.
 * 3. When an NPC spawns, its texture will be based on current NPC count
 * 4. After spawning, NPC count increments
 * 5. When main menu loaded, NPC count is saved.
 * 
 * Coding Plan for config file
 * 1. User may include config file for determing following:
 *    a. Can NPCs spawn underground? (default: NO)
 *    b. Can NPCs naturally despawn? (default: NO)
 * 2. Defaults loaded if no config file found
 * 
 * For user to add new textures, he must simply name the texture EasyNPC1.png, EasyNPC2.png, etc.
 * If max number of NPCs lower than current NPC count, when NPC update occurs, check if id number above max, and if so, kill
 */
public class mod_EasyNPCs{
    public static final String NAME = "EasyNPC";
    public static Map<Integer, Integer> numNPCs = new HashMap<Integer, Integer>();

    //settings
    public static boolean canNaturallyDespawn;
    public static boolean canSpawnUnderground;
    public static int maxNPCs;
    public void load(boolean isClient, Object mod){
    	maxNPCs = getNumNPCsInMobFolder();
        EntityRegistry.registerModEntity(OGSEEntityEasyNPC.class, "Easy NPC", 10, mod, 20, 3, false);
        EntityRegistry.addSpawn(OGSEEntityEasyNPC.class, 10, 1, 6, EnumCreatureType.monster, OgreSeanMods.getSpawn());
    	if(isClient)
            addRenderer();
	}

    @SideOnly(Side.CLIENT)
	public void addRenderer(){
        RenderingRegistry.registerEntityRenderingHandler(OGSEEntityEasyNPC.class, new RenderBiped(new ModelBiped(), 0.5F));
    }
	
	public void loadConfig(FMLPreInitializationEvent event){
        try{
        	Configuration config = new Configuration(event.getSuggestedConfigurationFile());

            canNaturallyDespawn = config.get(NAME, "canNaturallyDespawn", false).getBoolean(false);
            canSpawnUnderground = config.get(NAME, "canSpawnUnderground", false).getBoolean(false);
        	
        }catch(Exception e){
            canNaturallyDespawn = false;
            canSpawnUnderground = false;
        }
        MinecraftForge.EVENT_BUS.register(this);
	}
	
	private void npcCountLoad(World world){
		   //refresh npc count
	    numNPCs.put(world.provider.dimensionId, 0);
		try{
			   //open saved sign file; if does not exist, end method
			   File file3 = new File(world.getSaveHandler().getWorldDirectory(), "easyNPCs.dat"); //signs file
			   if(file3 == null || !file3.exists())
				   return;

	           NBTTagCompound nbttagcompound = CompressedStreamTools.read(file3);
                numNPCs.put(world.provider.dimensionId, nbttagcompound.getInteger("NumberNPCs"));
       }catch(Exception e){
           e.printStackTrace();
       }
   }

   private void npcCountSave(World world){
       try{
           //create or replace sign file
           File file3 = new File(world.getSaveHandler().getWorldDirectory(), "easyNPCs.dat"); //signs file
           if(file3.exists())
               file3.delete();

           //copy data from each sign on the list into file, until end of list
           ////save coordinates, type, range, num1, and num2 to file
           NBTTagCompound nbttagcompound = new NBTTagCompound();
           nbttagcompound.setInteger("NumberNPCs", numNPCs.get(world.provider.dimensionId));
           CompressedStreamTools.write(nbttagcompound, file3);
       }catch(Exception e){
           e.printStackTrace();
       }
   }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event){
        npcCountSave(event.world);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event){
        npcCountLoad(event.world);
    }

   private int getNumNPCsInMobFolder(){
       String name;
       int i;
       for(i = 0;;i++){
           name = new StringBuilder("ogresean:textures/mob/EasyNPC").append(i + 1).append(".png").toString();
           if(mod_EasyNPCs.class.getResource(name) == null)
               break;
       }
       return i;
   }

    public static void incrementNPCCount(int id, int count){
        numNPCs.put(id, numNPCs.get(id)+count);
    }
}
