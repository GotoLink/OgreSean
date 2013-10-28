package ogresean;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;

public class RandomItemSpawn implements IScheduledTickHandler {
	public static ArrayList<EntityItem> items = new ArrayList<EntityItem>();
	public static final ItemStack itemChoices[] = new ItemStack[] { new ItemStack(Block.dirt.blockID, 3, 0), new ItemStack(Block.blockClay), new ItemStack(Block.cobblestone),
			new ItemStack(Block.cobblestoneMossy), new ItemStack(Item.dyePowder.itemID, 1, 3), new ItemStack(Block.cobblestoneMossy.blockID, 2, 0), new ItemStack(Block.gravel),
			new ItemStack(Block.sand.blockID, 2, 0), new ItemStack(Block.pumpkin), new ItemStack(Block.sandStone), new ItemStack(Block.mushroomBrown), new ItemStack(Block.stone),
			new ItemStack(Item.bone), new ItemStack(Item.stick.itemID, 2, 0), new ItemStack(Item.slimeBall.itemID, 3, 0), new ItemStack(Item.silk), new ItemStack(Item.seeds),
			new ItemStack(Item.reed), new ItemStack(Item.leather), new ItemStack(Item.clay.itemID, 3, 0), new ItemStack(Item.egg), new ItemStack(Item.feather), new ItemStack(Item.porkRaw),
			new ItemStack(Item.fishRaw), new ItemStack(Item.appleRed), new ItemStack(Item.appleRed), new ItemStack(Item.flint.itemID, 2, 0), new ItemStack(Item.gunpowder.itemID, 2, 0),
			new ItemStack(Item.gunpowder), new ItemStack(Item.bone.itemID, 2, 0), new ItemStack(Item.silk.itemID, 2, 0), new ItemStack(Item.slimeBall.itemID, 2, 0),
			new ItemStack(Item.feather.itemID, 2, 0), new ItemStack(Item.coal.itemID, 1, 1), new ItemStack(Item.seeds.itemID, 3, 0), new ItemStack(Item.stick), new ItemStack(Item.bone.itemID, 2, 0),
			new ItemStack(Item.stick.itemID, 3, 0), new ItemStack(Item.bone.itemID, 4, 0), new ItemStack(Item.stick.itemID, 4, 0), };
	public static final int MAX_ITEMS = 32, MAX_ENTITIES_PER_CHUNK = 100;

	@Override
	public String getLabel() {
		return "RandomItemTick";
	}

	@Override
	public int nextTickSpacing() {
		return 4;//every 4 ticks
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		//attempt to spawn items 
		performItemSpawning(((WorldServer) tickData[0]));
		//every 62 ticks refresh item list, and remove dead or null items
		if (((World) tickData[0]).getWorldTime() % 62 == 0)
			refreshItemList(((World) tickData[0]).rand);
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
	}

	//spawns items into world
	private static void performItemSpawning(WorldServer world) {
		if (items.size() > MAX_ITEMS || world.provider.dimensionId != 0 || world.playerEntities.isEmpty())
			return;
		EntityPlayer player = (EntityPlayer) world.playerEntities.get(world.rand.nextInt(world.playerEntities.size()));
		int chunkX = MathHelper.floor_double(player.posX / 16.0D);
		int chunkZ = MathHelper.floor_double(player.posZ / 16.0D);
		boolean spawnNearPlayer = world.rand.nextFloat() < 0.01F;
		//choose random chunk to spawn items at
		chunkX += spawnNearPlayer ? world.rand.nextInt(3) - 1 : world.rand.nextInt(11) - 5;
		chunkZ += spawnNearPlayer ? world.rand.nextInt(3) - 1 : world.rand.nextInt(11) - 5;
		if (world.getChunkProvider().chunkExists(chunkX, chunkZ)) {
			//choose random coordinates in chunk to spawn bats at
			int x = (chunkX * 16) + world.rand.nextInt(16);
			int y = spawnNearPlayer ? MathHelper.floor_double(player.posY) : world.rand.nextInt(123) + 5;
			int z = (chunkZ * 16) + world.rand.nextInt(16);
			while (player.getDistance(x, y, z) < (spawnNearPlayer ? 4D : 10D))
				//make items spawn somewhat far from player
				y = world.rand.nextInt(123) + 5;
			//spawn items near that location
			EntityItem item;
			for (int i = 0; i < 3; i++) {
				int x0 = x + world.rand.nextInt(7) - 3;
				int y0 = y + world.rand.nextInt(7) - 3;
				int z0 = z + world.rand.nextInt(7) - 3;
				if (world.getBlockId(x0, y0, z0) != 0)
					continue;
				item = new EntityItem(world, x0, y0, z0, itemChoices[world.rand.nextInt(itemChoices.length)].copy());
				item.age = 1000 * world.rand.nextInt(6);
				items.add(item);
				world.spawnEntityInWorld(item);
			}
		}
	}

	private static void refreshItemList(Random rand) {
		Iterator<EntityItem> itr = items.iterator();
		while (itr.hasNext()) {
			EntityItem item = itr.next();
			if (item == null || !item.isEntityAlive()) {
				itr.remove();
			} else {
				//make items simultaneously jump randomly
				item.moveEntity(rand.nextDouble() / 4D - 0.125D, rand.nextDouble() / 4D, rand.nextDouble() / 4D - 0.125D);
			}
		}
	}
}
