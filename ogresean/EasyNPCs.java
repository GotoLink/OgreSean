package ogresean;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.WorldEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Coding Plan for gameplay:
 * 0. count number of EasyNPC textures when game loaded.
 * 1. When new world started, load count of NPCs from world's own special file
 * 2. NPCs will or will not spawn based on current NPC count.
 * 3. When an NPC spawns, its texture will be based on current NPC count
 * 4. After spawning, NPC count increments
 * 5. When main menu loaded, NPC count is saved.
 * 
 * For user to add new textures, he must simply name the texture EasyNPC1.png, EasyNPC2.png, etc.
 * If max number of NPCs lower than current NPC count, when NPC update occurs, check if id number above max, and if so, kill
 */
public final class EasyNPCs {
    public static final String NAME = "EasyNPC";
    public static Map<Integer, Integer> numNPCs = new HashMap<Integer, Integer>();

    //settings
    public static boolean canNaturallyDespawn, canSpawnUnderground;
    public static int maxNPCs;

    public void load(boolean isClient, Object mod) {
        maxNPCs = getNumNPCsInMobFolder();
        EntityRegistry.registerModEntity(OGSEEntityEasyNPC.class, "Easy NPC", 11, mod, 80, 3, true);
        EntityRegistry.addSpawn(OGSEEntityEasyNPC.class, 1, 1, 1, EnumCreatureType.monster, OgreSeanMods.getSpawn());
        if (isClient)
            addRenderer();
    }

    @SideOnly(Side.CLIENT)
    public void addRenderer() {
        RenderingRegistry.registerEntityRenderingHandler(OGSEEntityEasyNPC.class, new RenderNPC());
    }

    public void preLoad(Configuration config) {
        canNaturallyDespawn = config.getBoolean("canNaturallyDespawn", NAME, false, "If the easy NPCs can despawn");
        canSpawnUnderground = config.getBoolean("canSpawnUnderground", NAME, false, "If the easy NPCs can spawn below ground");
        MinecraftForge.EVENT_BUS.register(this);
    }

    private File saveDir(World world) {
        ISaveHandler worldSaver = world.getSaveHandler();
        if (worldSaver.getChunkLoader(world.provider) instanceof AnvilChunkLoader) {
            return ((AnvilChunkLoader) worldSaver.getChunkLoader(world.provider)).chunkSaveLocation;
        }
        return null;
    }

    //refresh npc count
    private void npcCountLoad(World world) {
        numNPCs.put(world.provider.dimensionId, 0);
        try {
            //open saved sign file; if does not exist, end method
            File file3 = new File(saveDir(world), "easyNPCs.dat"); //signs file
            if(file3.exists()) {
                NBTTagCompound nbttagcompound = CompressedStreamTools.read(file3);
                numNPCs.put(world.provider.dimensionId, nbttagcompound.getInteger("NumberNPCs"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void npcCountSave(World world) {
        try {
            //create or replace sign file
            File file3 = new File(saveDir(world), "easyNPCs.dat"); //signs file
            //copy data from each sign on the list into file, until end of list
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setInteger("NumberNPCs", numNPCs.get(world.provider.dimensionId));
            CompressedStreamTools.write(nbttagcompound, file3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Save event) {
        if (!event.world.isRemote)
            npcCountSave(event.world);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.world.isRemote)
            npcCountLoad(event.world);
    }

    private int getNumNPCsInMobFolder() {
        String name;
        int i = 0;
        for (; ; i++) {
            name = new StringBuilder("/assets/ogresean/textures/mob/EasyNPC").append(i + 1).append(".png").toString();
            if (EasyNPCs.class.getResource(name) == null)
                break;
        }
        return i;
    }

    public static void onNPCDeath(World world) {
        int id = world.provider.dimensionId;
        if(numNPCs.get(id) <= 0){
            return;
        }
        numNPCs.put(id, numNPCs.get(id) - 1);
    }

    public static boolean canSpawn(World world){
        return numNPCs.get(world.provider.dimensionId) < EasyNPCs.maxNPCs;
    }

    public static int onNPCSpawn(World world){
        int npc = getNextAvailableID(world);
        if(npc>=0) {
            int id = world.provider.dimensionId;
            numNPCs.put(id, numNPCs.get(id) + 1);
        }
        return npc;
    }

    //Big Oh = O(n + m)
    private static int getNextAvailableID(World world) {
        boolean acceptedIDs[] = new boolean[maxNPCs];
        //get all easy NPC ids onto loaded list
        for (Object e : world.loadedEntityList) {
            if (e instanceof OGSEEntityEasyNPC) {
                int id = ((OGSEEntityEasyNPC) e).getID();
                if (id < acceptedIDs.length)
                    acceptedIDs[id] = true;
            }
        }
        //sort this list based on id numbers, and find first available id number start off with random id num
        int x = world.rand.nextInt(acceptedIDs.length);
        for (int j = x; j < acceptedIDs.length; j++) {
            if (!acceptedIDs[j]) {
                return j;
            } else if ((x > 0 && j == x - 1) || (x == 0 && j == acceptedIDs.length - 1)) {
                break;
            } else if (j == acceptedIDs.length - 1) {
                j = -1;
            }
        }
        return -1;
    }
}
