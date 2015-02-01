package ogresean;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public final class RandomItemSpawn {
    public static ArrayList<EntityItem> items = new ArrayList<EntityItem>();
    public static final ItemStack itemChoices[] = new ItemStack[]{new ItemStack(Blocks.dirt, 3, 0), new ItemStack(Blocks.clay), new ItemStack(Blocks.cobblestone),
            new ItemStack(Blocks.mossy_cobblestone), new ItemStack(Items.dye, 1, 3), new ItemStack(Blocks.mossy_cobblestone, 2, 0), new ItemStack(Blocks.gravel),
            new ItemStack(Blocks.sand, 2, 0), new ItemStack(Blocks.pumpkin), new ItemStack(Blocks.sandstone), new ItemStack(Blocks.brown_mushroom), new ItemStack(Blocks.stone),
            new ItemStack(Items.bone), new ItemStack(Items.stick, 2, 0), new ItemStack(Items.slime_ball, 3, 0), new ItemStack(Items.string), new ItemStack(Items.wheat_seeds),
            new ItemStack(Items.reeds), new ItemStack(Items.leather), new ItemStack(Items.clay_ball, 3, 0), new ItemStack(Items.egg), new ItemStack(Items.feather), new ItemStack(Items.porkchop),
            new ItemStack(Items.fish), new ItemStack(Items.apple), new ItemStack(Items.apple), new ItemStack(Items.flint, 2, 0), new ItemStack(Items.gunpowder, 2, 0),
            new ItemStack(Items.gunpowder), new ItemStack(Items.bone, 2, 0), new ItemStack(Items.string, 2, 0), new ItemStack(Items.slime_ball, 2, 0),
            new ItemStack(Items.feather, 2, 0), new ItemStack(Items.coal, 1, 1), new ItemStack(Items.wheat_seeds, 3, 0), new ItemStack(Items.stick), new ItemStack(Items.bone, 2, 0),
            new ItemStack(Items.stick, 3, 0), new ItemStack(Items.bone, 4, 0), new ItemStack(Items.stick, 4, 0),};
    public static final int MAX_ITEMS = 32, MAX_ENTITIES_PER_CHUNK = 100;

    @SubscribeEvent
    public void tickEnd(TickEvent.WorldTickEvent event) {
        if (event.world instanceof WorldServer && event.phase == TickEvent.Phase.END && event.world.getWorldTime() % 4 == 0) {
            //attempt to spawn items
            performItemSpawning(((WorldServer) event.world));
            //every 62 ticks refresh item list, and remove dead or null items
            if (event.world.getWorldTime() % 62 == 0)
                refreshItemList(event.world.rand);

        }
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
                if (!world.isAirBlock(x0, y0, z0))
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
