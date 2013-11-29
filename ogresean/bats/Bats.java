package ogresean.bats;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.SpawnListEntry;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Bats implements ITickHandler {
	//configuration variables
	public static int dayBatSpawnRate = 75, nightBatSpawnRate = 10; //implemented
	public static ArrayList<SpawnListEntry> batSpawnList = new ArrayList<SpawnListEntry>(); //implemented
	public static int batSpawnNum = 0; //implemented
	public static ArrayList<BBEntityBat> batsList = new ArrayList<BBEntityBat>(); //used for bat spawning
	public static int batCount = 0; //used for bat spawning
	public static HashMap<String, ArrayList<BBEntityBat>> assistants = new HashMap<String, ArrayList<BBEntityBat>>();

	@Override
	public String getLabel() {
		return "Bats Tick";
	}

	public void load(boolean client, Object mod) {
		MinecraftForge.EVENT_BUS.register(new SoundHandler());
		EntityRegistry.registerModEntity(BBEntityInsectBat.class, "Insect Bat", 1, mod, 80, 3, false);
		EntityRegistry.registerModEntity(BBEntityNectarBat.class, "Nectar Bat", 2, mod, 80, 3, false);
		EntityRegistry.registerModEntity(BBEntityFruitBat.class, "Fruit Bat", 3, mod, 80, 3, false);
		EntityRegistry.registerModEntity(BBEntityMeatEaterBat.class, "Meat Eater Bat", 4, mod, 80, 3, false);
		EntityRegistry.registerModEntity(BBEntityBloodEaterBat.class, "Blood Eater Bat", 5, mod, 80, 3, false);
		TickRegistry.registerTickHandler(this, Side.SERVER);
		if (client) {
			addRenderers();
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		onTickInGame((World) tickData[0]);
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
	}

	//returns base bat spawn rate depending on whether it is day or night in world
	public static int getBatSpawnRate(World world) {
		if (world.isDaytime())
			return dayBatSpawnRate;
		else
			return nightBatSpawnRate;
	}

	public static boolean preLoad(Configuration config) {
		dayBatSpawnRate = config.get("Bats", "Spawn rate by day", dayBatSpawnRate).getInt(dayBatSpawnRate);
		if (dayBatSpawnRate > 400)
			dayBatSpawnRate = 400;
		nightBatSpawnRate = config.get("Bats", "Spawn rate by night", nightBatSpawnRate).getInt(nightBatSpawnRate);
		if (nightBatSpawnRate > 400)
			nightBatSpawnRate = 400;
		int num = config.get("Bats", "Relative spawn rate for insect type", 20).getInt(20);
		if (num > 0) {
			batSpawnList.add(new SpawnListEntry(BBEntityInsectBat.class, num, 0, 0));
			batSpawnNum += num;
		}
		num = config.get("Bats", "Relative spawn rate for nectar type", 5).getInt(5);
		if (num > 0) {
			batSpawnList.add(new SpawnListEntry(BBEntityNectarBat.class, num, 0, 0));
			batSpawnNum += num;
		}
		num = config.get("Bats", "Relative spawn rate for fruit type", 5).getInt(5);
		if (num > 0) {
			batSpawnList.add(new SpawnListEntry(BBEntityFruitBat.class, num, 0, 0));
			batSpawnNum += num;
		}
		num = config.get("Bats", "Relative spawn rate for meat type", 5).getInt(5);
		if (num > 0) {
			batSpawnList.add(new SpawnListEntry(BBEntityMeatEaterBat.class, num, 0, 0));
			batSpawnNum += num;
		}
		num = config.get("Bats", "Relative spawn rate for blood type", 5).getInt(5);
		if (num > 0) {
			batSpawnList.add(new SpawnListEntry(BBEntityBloodEaterBat.class, num, 0, 0));
			batSpawnNum += num;
		}
		return batSpawnNum > 0;
	}

	protected static BBEntityBat pickRandomBat(World world) {
		int b = world.rand.nextInt(batSpawnNum);
		for (int i = 0; i < batSpawnList.size(); i++) {
			int x = batSpawnList.get(i).itemWeight;
			if (b >= x)
				b -= x;
			else
				try {
					return (BBEntityBat) batSpawnList.get(i).entityClass.getConstructor(World.class).newInstance(world);
				} catch (Exception e) {
					return new BBEntityInsectBat(world);
				}
		}
		return new BBEntityInsectBat(world);
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
		RenderingRegistry.registerEntityRenderingHandler(BBEntityInsectBat.class, new BBRenderBat(new BBModelBat(), 0.4F));
		RenderingRegistry.registerEntityRenderingHandler(BBEntityNectarBat.class, new BBRenderBat(new BBModelBat(), 0.44F));
		RenderingRegistry.registerEntityRenderingHandler(BBEntityFruitBat.class, new BBRenderBat(new BBModelBat(), 0.48F));
		RenderingRegistry.registerEntityRenderingHandler(BBEntityMeatEaterBat.class, new BBRenderBat(new BBModelBat(), 0.5F));
		RenderingRegistry.registerEntityRenderingHandler(BBEntityBloodEaterBat.class, new BBRenderBat(new BBModelBat(), 0.32F));
	}

	private static void onTickInGame(World world) {
		//spawn bats every 4 ticks (increased by spawning frequency)
		if (getBatSpawnRate(world) > 0 && world.getWorldTime() % MathHelper.floor_double(4D * (100D / getBatSpawnRate(world))) == 0) {
			performBatSpawning(world);
		}
		//if newly untamed bats spawned, they will be added to the bats list
		if (batCount != 0)
			refreshBatList(world);
		batCount = batsList.size();
		//TELL FOLLOWing bats TO AID
		if (!world.isRemote || world.playerEntities.isEmpty())
			return;
		for (Object ent : world.playerEntities) {
			EntityPlayer ep = (EntityPlayer) ent;
			if (!assistants.containsKey(ep.getCommandSenderName())) {
				assistants.put(ep.getCommandSenderName(), new ArrayList<BBEntityBat>());
			}
			boolean leftClicking = ep.isSwingInProgress; //true if player holding down left mouse button
			if (leftClicking && !assistants.get(ep.getCommandSenderName()).isEmpty()) {
				//checks to see if player's cursor is over an entity or a block
				Vec3 vec3 = world.getWorldVec3Pool().getVecFromPool(ep.posX, ep.posY, ep.posZ);
				Vec3 vec31 = world.getWorldVec3Pool().getVecFromPool(ep.posX + ep.motionX, ep.posY + ep.motionY, ep.posZ + ep.motionZ);
				MovingObjectPosition movingobjectposition = world.rayTraceBlocks_do_do(vec3, vec31, true, true);
				vec3 = world.getWorldVec3Pool().getVecFromPool(ep.posX, ep.posY, ep.posZ);
				vec31 = world.getWorldVec3Pool().getVecFromPool(ep.posX + ep.motionX, ep.posY + ep.motionY, ep.posZ + ep.motionZ);
				if (movingobjectposition != null) {
					vec31 = world.getWorldVec3Pool().getVecFromPool(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
				}
				Entity entityTarget = null;
				List<?> list = world.getEntitiesWithinAABBExcludingEntity(ep, ep.boundingBox.addCoord(ep.motionX, ep.motionY, ep.motionZ).expand(1.0D, 1.0D, 1.0D));
				double d0 = 0.0D;
				for (int j = 0; j < list.size(); ++j) {
					Entity entity1 = (Entity) list.get(j);
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

	//spawns bats into world
	private static void performBatSpawning(World world) {
		if (batsList.size() > 32 || world.playerEntities.isEmpty())
			return;
		EntityPlayer player = (EntityPlayer) world.playerEntities.get(world.rand.nextInt(world.playerEntities.size()));
		//get current player chunk
		int chunkX = MathHelper.floor_double(player.posX / 16.0D);
		int chunkZ = MathHelper.floor_double(player.posZ / 16.0D);
		//choose random chunk to spawn bats at
		chunkX += world.rand.nextInt(13) - 6;
		chunkZ += world.rand.nextInt(13) - 6;
		if (world.getChunkProvider().chunkExists(chunkX, chunkZ)) {
			//choose random coordinates in chunk to spawn bats at
			int x = (chunkX * 16) + world.rand.nextInt(16);
			int y = world.rand.nextInt(120) + 6;
			int z = (chunkZ * 16) + world.rand.nextInt(16);
			while (player.getDistance(x, y, z) < 20D)
				//make bats spawn somewhat far from player
				z -= world.rand.nextInt(6) + 4;
			//spawn multiple bats near that location
			BBEntityBat bat = pickRandomBat(world); //get a random type of bat
			if (!validSpawnArea(bat, x, y, z))
				return; //check if bat can spawn in this biome and depth
			for (int i = 0; i < bat.getMaxSpawnedInChunk(); i++) {
				bat.setLocationAndAngles(x, y, z, world.rand.nextFloat() * 360.0F, 0.0F);
				if (bat.getCanSpawnHere()) {
					batsList.add(bat);
					world.spawnEntityInWorld(bat);
				}
			}
		}
	}

	//refreshes the bat list
	private static void refreshBatList(World world) {
		batsList.clear();
		Entity ent;
		Iterator<Entity> itr = world.loadedEntityList.iterator();
		while (itr.hasNext()) {
			ent = itr.next();
			if (ent instanceof BBEntityBat && !ent.isDead && ((BBEntityBat) ent).batAction < 3) {
				batsList.add((BBEntityBat) ent);
			}
		}
	}
}
