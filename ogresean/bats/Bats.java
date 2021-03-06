package ogresean.bats;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class Bats {
    //configuration variables
    public static int dayBatSpawnRate = 75, nightBatSpawnRate = 10; //implemented
    public static final Class<? extends EntityLiving>[] bats = new Class[]{BBEntityInsectBat.class, BBEntityNectarBat.class, BBEntityFruitBat.class, BBEntityMeatEaterBat.class, BBEntityBloodEaterBat.class};
    public static final String[] batsName = {"Insect ", "Nectar ", "Fruit ", "Meat Eater ", "Blood Eater "};
    public static ArrayList<Integer> batSpawnList = new ArrayList<Integer>(); //implemented
    public static int batSpawnNum = 0; //implemented
    public static ArrayList<BBEntityBat> batsList = new ArrayList<BBEntityBat>(); //used for bat spawning
    public static int batCount = 0; //used for bat spawning
    public static HashMap<String, ArrayList<BBEntityBat>> assistants = new HashMap<String, ArrayList<BBEntityBat>>();

    public void load(boolean client, Object mod) {
        for (int i = 0; i < batsName.length; i++) {
            EntityRegistry.registerModEntity(bats[i], batsName[i] + "Bat", i + 1, mod, 80, 3, false);
            if (i != 2 && i != 4) {
                addSpawn(bats[i], batSpawnList.get(i), BiomeGenBase.getBiomeGenArray());
            }
        }
        addSpawn(bats[2], batSpawnList.get(2), BiomeDictionary.getBiomesForType(BiomeDictionary.Type.FOREST));
        addSpawn(bats[4], batSpawnList.get(4), BiomeDictionary.getBiomesForType(BiomeDictionary.Type.JUNGLE));
        removeSpawn(bats[1], BiomeDictionary.getBiomesForType(BiomeDictionary.Type.DESERT));
        removeSpawn(bats[1], BiomeDictionary.getBiomesForType(BiomeDictionary.Type.FROZEN));
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
        if (client) {
            addRenderers();
        }
    }

    public static void addSpawn(Class<? extends EntityLiving> entityClass, int weightedProb, BiomeGenBase... biomes) {
        for (BiomeGenBase biome : biomes) {
            if (biome != null) {
                List<BiomeGenBase.SpawnListEntry> spawns = biome.getSpawnableList(EnumCreatureType.creature);
                spawns.add(new BiomeGenBase.SpawnListEntry(entityClass, weightedProb, 1, 1));
            }
        }
    }

    public static void removeSpawn(Class<? extends EntityLiving> entityClass, BiomeGenBase... biomes) {
        for (BiomeGenBase biome : biomes) {
            if (biome != null) {
                List<BiomeGenBase.SpawnListEntry> spawns = biome.getSpawnableList(EnumCreatureType.creature);
                for (BiomeGenBase.SpawnListEntry entry : spawns) {
                    if (entry.entityClass == entityClass) {
                        spawns.remove(entry);
                        break;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.WorldTickEvent event) {
        if (!event.world.isRemote && event.phase == TickEvent.Phase.END) {
            onTickInGame(event.world);
        }
    }

    //returns base bat spawn rate depending on whether it is day or night in world
    public static int getBatSpawnRate(World world) {
        if (world.isDaytime())
            return dayBatSpawnRate;
        else
            return nightBatSpawnRate;
    }

    public boolean preLoad(Configuration config) {
        dayBatSpawnRate = config.get("Bats", "Spawn rate by day", dayBatSpawnRate).getInt(dayBatSpawnRate);
        if (dayBatSpawnRate > 400)
            dayBatSpawnRate = 400;
        nightBatSpawnRate = config.get("Bats", "Spawn rate by night", nightBatSpawnRate).getInt(nightBatSpawnRate);
        if (nightBatSpawnRate > 400)
            nightBatSpawnRate = 400;
        int num = config.get("Bats", "Relative spawn rate for insect type", 20).getInt(20);
        if (num > 0) {
            batSpawnList.add(num);
            batSpawnNum += num;
        }
        num = config.get("Bats", "Relative spawn rate for nectar type", 5).getInt(5);
        if (num > 0) {
            batSpawnList.add(num);
            batSpawnNum += num;
        }
        num = config.get("Bats", "Relative spawn rate for fruit type", 5).getInt(5);
        if (num > 0) {
            batSpawnList.add(num);
            batSpawnNum += num;
        }
        num = config.get("Bats", "Relative spawn rate for meat type", 5).getInt(5);
        if (num > 0) {
            batSpawnList.add(num);
            batSpawnNum += num;
        }
        num = config.get("Bats", "Relative spawn rate for blood type", 5).getInt(5);
        if (num > 0) {
            batSpawnList.add(num);
            batSpawnNum += num;
        }
        return batSpawnNum > 0;
    }

    @SubscribeEvent
    public void onTrySpawn(LivingSpawnEvent.CheckSpawn event) {
        if (event.entityLiving instanceof BBEntityBat) {
            if (getBatSpawnRate(event.world) <= 0 || event.world.getWorldTime() % MathHelper.floor_double(4D * (100D / getBatSpawnRate(event.world))) != 0 || !validSpawnArea((BBEntityBat) event.entityLiving, (int) event.x, (int) event.y, (int) event.z)) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    protected static boolean validSpawnArea(BBEntityBat bat, int x, int y, int z) {
        if (!bat.worldObj.isAirBlock(x, y, z)) {
            return false;
        }
        BiomeGenBase mobspawnerbase = bat.worldObj.getWorldChunkManager().getBiomeGenAt(x, z);
        return bat.getBiomeMaxY(mobspawnerbase) > y && bat.getBiomeMinY(mobspawnerbase) < y;
    }

    @SideOnly(Side.CLIENT)
    //each bat is a slightly different size
    private static void addRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(BBEntityInsectBat.class, new BBRenderBat("insect", 0.4F));
        RenderingRegistry.registerEntityRenderingHandler(BBEntityNectarBat.class, new BBRenderBat("nectar", 0.44F));
        RenderingRegistry.registerEntityRenderingHandler(BBEntityFruitBat.class, new BBRenderBat("fruit", 0.48F));
        RenderingRegistry.registerEntityRenderingHandler(BBEntityMeatEaterBat.class, new BBRenderBat("meatEater", 0.5F));
        RenderingRegistry.registerEntityRenderingHandler(BBEntityBloodEaterBat.class, new BBRenderBat("bloodEater", 0.32F));
    }

    private static void onTickInGame(World world) {
        //if newly untamed bats spawned, they will be added to the bats list
        if (batCount != 0)
            refreshBatList(world);
        batCount = batsList.size();
        //TELL FOLLOWing bats TO AID
        if (world == null || world.playerEntities.isEmpty())
            return;
        for (Object ent : world.playerEntities) {
            EntityPlayer ep = (EntityPlayer) ent;
            if (!assistants.containsKey(ep.getCommandSenderName())) {
                assistants.put(ep.getCommandSenderName(), new ArrayList<BBEntityBat>());
            }
            boolean leftClicking = ep.isSwingInProgress; //true if player holding down left mouse button
            if (leftClicking && !assistants.get(ep.getCommandSenderName()).isEmpty()) {
                //checks to see if player's cursor is over an entity or a block
                Vec3 vec3 = Vec3.createVectorHelper(ep.posX, ep.posY, ep.posZ);
                Vec3 vec31 = Vec3.createVectorHelper(ep.posX + ep.motionX, ep.posY + ep.motionY, ep.posZ + ep.motionZ);
                MovingObjectPosition movingobjectposition = world.func_147447_a(vec3, vec31, true, true, false);
                if (movingobjectposition != null) {
                    vec31 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
                }
                Entity entityTarget = null;
                @SuppressWarnings("unchecked") List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(ep, ep.boundingBox.addCoord(ep.motionX, ep.motionY, ep.motionZ).expand(1.0D, 1.0D, 1.0D));
                double d0 = 0.0D;
                for (Entity entity1 : list) {
                    if (entity1.canBeCollidedWith() && !assistants.get(ep.getCommandSenderName()).contains(entity1)) {
                        float f = 0.3F;
                        AxisAlignedBB axisalignedbb = entity1.boundingBox.expand(f, f, f);
                        MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec3, vec31);
                        if (movingobjectposition1 != null) {
                            double d1 = vec3.distanceTo(movingobjectposition1.hitVec);
                            if (d1 < d0 || d0 == 0.0D) {
                                entityTarget = entity1;
                                d0 = d1;
                            }
                        }
                    }
                }
                if (entityTarget != null)
                    for (int i = 0; i < assistants.size(); i++)
                        assistants.get(ep.getCommandSenderName()).get(i).playerEntityAttack(entityTarget);
            }
        }
    }

    //refreshes the bat list
    private static void refreshBatList(World world) {
        batsList.clear();
        Entity ent;
        @SuppressWarnings("unchecked") Iterator<Entity> itr = world.loadedEntityList.iterator();
        while (itr.hasNext()) {
            ent = itr.next();
            if (ent instanceof BBEntityBat && !ent.isDead && ((BBEntityBat) ent).getBatAction() < 3) {
                batsList.add((BBEntityBat) ent);
            }
        }
    }
}
