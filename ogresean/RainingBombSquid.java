package ogresean;

import java.util.ArrayList;
import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityFallingSand;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class RainingBombSquid implements ITickHandler {
	public static ArrayList<EntitySquid> bombSquids = new ArrayList<EntitySquid>();

	@Override
	public String getLabel() {
		return "BombSquidTick";
	}

	public void onTickInGame(World world) {
		//spawn bomb squids every X ticks, where X = 100(Peaceful), 69(Easy), 37(Normal), 6(Hard)
		if (world.getWorldTime() % (100 - 31 * world.difficultySetting) == 0) {
			performSquidSpawning(world);
		}
		checkSquids(world);
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

	private static void checkSquids(World world) {
		for (int i = 0; i < bombSquids.size(); i++) {
			EntitySquid sq = bombSquids.get(i);
			if (sq == null || sq.isDead) { //remove dead squids
				bombSquids.remove(i);
				i--;
				continue;
			} else if (sq.isCollided || sq.isInWater() || sq.onGround) { //squids that are no longer in air explode
				if (!sq.getEntityData().getBoolean("RainingSquidFlag")) {
					//explode squid
					int explosionSize = world.rand.nextInt(1 + world.difficultySetting) + 1;
					world.createExplosion(sq, sq.posX, sq.posY, sq.posZ, explosionSize, true);
					//sq.dropFewItems(false);
				} else if (sq.entityId % 2 == 0) { //block shuffler squid
					int shuffleSize = world.rand.nextInt(1 + world.difficultySetting) + 1;
					for (int x = -shuffleSize; x <= shuffleSize; x++)
						for (int y = -shuffleSize; y <= shuffleSize; y++)
							for (int z = -shuffleSize; z <= shuffleSize; z++) {
								int m = MathHelper.floor_double(sq.posX) + x;
								int n = MathHelper.floor_double(sq.boundingBox.minY) + y;
								int o = MathHelper.floor_double(sq.posZ) + z;
								//get all common blocks near squid
								int bid = sq.worldObj.getBlockId(m, n, o);
								//convert these common solid blocks to fallingsand
								if (bid == Block.dirt.blockID || bid == Block.grass.blockID || bid == Block.sand.blockID || bid == Block.gravel.blockID || bid == Block.stone.blockID) {
									EntityFallingSand efs = new EntityFallingSand(sq.worldObj, m, n, o, bid);
									//assign random velocities
									//efs.setVelocity(world.rand.nextDouble() / 2D - 0.25D, world.rand.nextDouble() + 0.5D, world.rand.nextDouble() / 2D - 0.25D);
									for (int j = 0; j < 2 + world.rand.nextInt(3); j++) {
										double d = world.rand.nextGaussian() * 0.02D;
										double d1 = world.rand.nextGaussian() * 0.02D;
										double d2 = world.rand.nextGaussian() * 0.02D;
										world.spawnParticle("portal", (sq.posX + world.rand.nextFloat() * sq.width * 2.0F) - sq.width, sq.posY + world.rand.nextFloat() * sq.height,
												(sq.posZ + world.rand.nextFloat() * sq.width * 2.0F) - sq.width, d, d1, d2);
									}
									efs.worldObj.spawnEntityInWorld(efs);
									efs.worldObj.setBlockToAir(m, n, o);
								}
							}
				} else { // Lava/Water Squid
					int floodSize = world.rand.nextInt(1 + world.difficultySetting) + 1;
					int floodBlock = sq.entityId % 5 == 0 ? Block.lavaMoving.blockID : Block.waterMoving.blockID;
					for (int x = -floodSize; x <= floodSize; x++)
						for (int y = -floodSize; y <= floodSize; y++)
							for (int z = -floodSize; z <= floodSize; z++) {
								int m = MathHelper.floor_double(sq.posX) + x;
								int n = MathHelper.floor_double(sq.boundingBox.minY) + y;
								int o = MathHelper.floor_double(sq.posZ) + z;
								if (sq.worldObj.getBlockId(m, n, o) == 0)
									sq.worldObj.setBlock(m, n, o, floodBlock, 0, 3);
							}
				}
				sq.setDead();
				bombSquids.remove(i);
				i--;
				continue;
			} else {
				if (!sq.getEntityData().getBoolean("RainingSquidFlag")) {
					sq.setHealth(200);
					sq.setFire(400);
					sq.motionY -= sq.entityId % 4 == 0 ? 0.009D : 0.002D;
					sq.motionX += sq.entityId % 3 == 0 ? 0.009D : -0.009D;
					sq.motionZ += sq.entityId % 2 == 0 ? 0.009D : -0.009D;
					sq.motionY *= sq.entityId % 5 == 0 ? 0.96D : 1.032D;
					sq.motionX *= sq.entityId % 6 == 0 ? 0.98D : 1.025D;
					sq.motionZ *= sq.entityId % 7 == 0 ? 0.98D : 1.025D;
					for (int j = 0; j < 2 + world.rand.nextInt(4); j++) {
						double d = world.rand.nextGaussian() * 0.02D;
						double d1 = world.rand.nextGaussian() * 0.02D;
						double d2 = world.rand.nextGaussian() * 0.02D;
						world.spawnParticle("explode", (sq.posX + world.rand.nextFloat() * sq.width * 2.0F) - sq.width, sq.posY + world.rand.nextFloat() * sq.height, (sq.posZ + world.rand.nextFloat()
								* sq.width * 2.0F)
								- sq.width, d, d1, d2);
					}
				} else if (sq.entityId % 2 == 0) { //block shuffler squids fall slower
					sq.setHealth(200);
					sq.motionY *= 0.5D + (sq.ticksExisted % 100) * 0.003;
					sq.motionX += -0.005D + (sq.ticksExisted % 100) * 0.0001;
					sq.motionZ += -0.005D + (sq.ticksExisted % 100) * 0.0001;
					for (int j = 0; j < 2 + world.rand.nextInt(4); j++) {
						double d = world.rand.nextGaussian() * 0.02D;
						double d1 = world.rand.nextGaussian() * 0.02D;
						double d2 = world.rand.nextGaussian() * 0.02D;
						world.spawnParticle("portal", (sq.posX + world.rand.nextFloat() * sq.width * 2.0F) - sq.width, sq.posY + world.rand.nextFloat() * sq.height, (sq.posZ + world.rand.nextFloat()
								* sq.width * 2.0F)
								- sq.width, d, d1, d2);
					}
				} else { // Lava/Water Squid
					sq.setHealth(200);
					sq.motionY *= 0.55D + (sq.ticksExisted % 200) * 0.0018;
					sq.motionX += -0.008D + (sq.ticksExisted % 100) * 0.00016;
					sq.motionZ += -0.008D + (sq.ticksExisted % 100) * 0.00016;
					for (int j = 0; j < 2 + world.rand.nextInt(6); j++) {
						double d = world.rand.nextGaussian() * 0.02D;
						double d1 = world.rand.nextGaussian() * 0.02D;
						double d2 = world.rand.nextGaussian() * 0.02D;
						world.spawnParticle(sq.entityId % 5 == 0 ? "lava" : "bubble", (sq.posX + world.rand.nextFloat() * sq.width * 2.0F) - sq.width, sq.posY + world.rand.nextFloat() * sq.height,
								(sq.posZ + world.rand.nextFloat() * sq.width * 2.0F) - sq.width, d, d1, d2);
					}
				}
			}
		}
	}

	private static void performSquidSpawning(World world) {
		if (bombSquids.size() > 6 * world.difficultySetting || world.playerEntities.isEmpty())
			return;
		EntityPlayer player = (EntityPlayer) world.playerEntities.get(world.rand.nextInt(world.playerEntities.size()));
		int chunkX = MathHelper.floor_double(player.posX / 16.0D);
		int chunkZ = MathHelper.floor_double(player.posZ / 16.0D);
		//choose random chunks to spawn bomb squids at; squids spawn closer on tougher difficulties
		chunkX += world.rand.nextInt(9 - world.difficultySetting * 2) - (4 - world.difficultySetting);
		chunkZ += world.rand.nextInt(9 - world.difficultySetting * 2) - (4 - world.difficultySetting);
		if (world.getChunkProvider().chunkExists(chunkX, chunkZ)) {
			//choose random coordinates in chunk to spawn bats at
			int x = (chunkX * 16) + world.rand.nextInt(16);
			int y = 127;
			int z = (chunkZ * 16) + world.rand.nextInt(16);
			EntitySquid squid;
			for (int i = 0; i < world.difficultySetting + 1; i++) {
				squid = new EntitySquid(world);
				squid.setLocationAndAngles(x + world.rand.nextInt(15) - 7, y, z + world.rand.nextInt(15) - 7, world.rand.nextFloat() * 360.0F, 0.0F);
				if (squid.getCanSpawnHere()) {
					squid.getEntityData().setBoolean("RainingSquidFlag", world.rand.nextFloat() < 0.1F + (0.1F * world.difficultySetting));
					bombSquids.add(squid);
					world.spawnEntityInWorld(squid);
				}
			}
		}
	}
}
