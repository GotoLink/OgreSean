package ogresean;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.monster.IMob;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class OGSEEntityEasyNPC extends EntityCreature implements IMob {
	public OGSEEntityEasyNPC(World world){
		super(world);
		ID = 0;
		flags = rand.nextLong();
	}

    @Override
	public boolean getCanSpawnHere()
    {
		boolean flag;
    	int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(boundingBox.minY);
        int k = MathHelper.floor_double(posZ);
        if(!mod_EasyNPCs.canSpawnUnderground && !worldObj.canBlockSeeTheSky(i, j, k)) //prevent underground spawning if enabled
        	flag = false;
        else
        	flag = !worldObj.checkBlockCollision(boundingBox) && worldObj.getCollidingBoundingBoxes(this, boundingBox).size() == 0 && !worldObj.isAnyLiquid(boundingBox) && mod_EasyNPCs.numNPCs.get(worldObj.provider.dimensionId) < mod_EasyNPCs.maxNPCs; //prevent more spawning than allowed
        
        if(flag){
        	mod_EasyNPCs.incrementNPCCount(worldObj.provider.dimensionId, 1);
        	ID = getNextAvailableID();
        }
        
        return flag;
    }

    @Override
	public void onLivingUpdate()
    {
        super.onLivingUpdate();
        if(ID >= mod_EasyNPCs.maxNPCs)
        	setDead();
        /*if(((flags >> 12) & 15) < 7 && !hasPath())TODO
            func_31026_E();*/
    }

    @Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setInteger("EasyIDNum", ID);
        nbttagcompound.setLong("Flags", flags);
    }

    @Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
        ID = nbttagcompound.getInteger("EasyIDNum");
        flags = nbttagcompound.getLong("Flags");
    }

    @Override
	public void setDead()
    {
        super.setDead();
        mod_EasyNPCs.incrementNPCCount(worldObj.provider.dimensionId,-1);
    }
	
	public String getEntityTexture()
    {
        return new StringBuilder("ogresean:textures/mob/EasyNPC").append(ID + 1).append(".png").toString();
    }

    @Override
	protected boolean canDespawn()
    {
    	return mod_EasyNPCs.canNaturallyDespawn;
    }
	
	//Big Oh = O(n + m)
	private int getNextAvailableID(){
		List l = worldObj.getLoadedEntityList();
		int num = 0;
		Entity e;
		OGSEEntityEasyNPC npc;
		boolean acceptedIDs[] = new boolean[mod_EasyNPCs.maxNPCs];
		for(int i = 0; i < acceptedIDs.length ; i++)
			acceptedIDs[i] = false;
		//get all easy NPC ids onto above list
		for(int k = 0; k < l.size(); k++){
			e = (Entity) l.get(k);
			if(e instanceof OGSEEntityEasyNPC){
				npc = (OGSEEntityEasyNPC) e;
				if(npc.ID < acceptedIDs.length)
					acceptedIDs[npc.ID] = true;
			}
		}
		//sort this list based on id numbers, and find first available id number
		//start off with random id num
		int x = rand.nextInt(acceptedIDs.length);
		for(int j = x; j < acceptedIDs.length ; j++){
			if(!acceptedIDs[j]){
				num = j;
				break;
			}
			else if((x > 0 && j == x - 1) || (x == 0 && j == acceptedIDs.length - 1)){
				break;
			}
			else if(j == acceptedIDs.length - 1){
				j = -1;
			}
		}
		return num;
	}

    @Override
	protected void jump()
    {
		if((flags & 1) == 1)
            motionY = 0.83D;
		else
            motionY = 0.42D;
		
		//field_35118_ao = true;TODO
    }

    @Override
	public void moveFlying(float f, float f1, float f2)
    {
		if(((flags >> 1) & 1) == 1)
            f2 *= 1.5F;
        super.moveFlying(f, f1, f2);
    }

    @Override
	public ItemStack getHeldItem()
    {
		if(((flags >> 2) & 15) > 0)
            return heldObjs[(int) ((flags >> 2) & 15) - 1];
		else
            return null;
    }

    @Override
	protected Item getDropItem()
    {
		if(((flags >> 6) & 15) > 0)
            return dropIDs[(int) ((flags >> 6) & 15) - 1];
		else
            return null;
    }

    @Override
	public boolean attackEntityFrom(DamageSource d, float i)
    {
		if(((flags >> 10) & 1) == 1 && i > 1)
            i /= 2;
		return
                super.attackEntityFrom(d, i);
    }

    @Override
	protected void fall(float f)
    {
		if(((flags >> 11) & 1) == 1)
            f /= 2.0F;
        super.fall(f);
    }

    @Override
	public float getBlockPathWeight(int i, int j, int k)
    {
		int movementType = (int) ((flags >> 12) & 15);
    	float xDif = (float) (posX - i);
    	float zDif = (float) (posZ - k);
    	
    	switch (movementType){
    	case 0:
    		return 0.0F;
    	case 1:
    		return ((xDif + zDif) / 2F);
    	case 2:
    		return -((xDif + zDif) / 2F);
    	case 3:
    		return ((xDif - zDif) / 2F);
    	case 4:
    		return -((xDif - zDif) / 2F);
    	case 5:
    		if(worldObj.getBlock(i, j, k) == Blocks.grass)return 10F;
    		else return 0.0F;
    	case 6:
    		if(worldObj.getBlock(i, j, k) == Blocks.sand)return 10F;
    		else return 0.0F;
    	case 7:
    		if(worldObj.getBlock(i, j, k) == Blocks.stone)return 10F;
    		else return 0.0F;
    	case 8:
    		if(worldObj.getBlock(i, j, k) == Blocks.gravel)return 10F;
    		else return 0.0F;
    	case 9:
    		if(worldObj.getBlock(i, j, k) == Blocks.dirt)return 10F;
    		else return 0.0F;
    	case 10:
    		if(worldObj.getBlock(i, j, k) == Blocks.log)return 10F;
    		else return 0.0F;
    	case 11:
    		if(worldObj.getBlock(i, j, k) == Blocks.snow)return 10F;
    		else return 0.0F;
    	case 12:
    		if(worldObj.getBlock(i, j, k) == Blocks.ice)return 10F;
    		else return 0.0F;
    	case 13:
    		if(worldObj.getBlock(i, j, k) == Blocks.tallgrass)return 10F;
    		else return 0.0F;
    	case 14:
    		return (float) (j - posY);
    	case 15:
    		return (float) (posY - j);
    	default:
    		return super.getBlockPathWeight(i, j, k);
    	}
    }

	public int ID;
	/**
	 * ^bit 1: jump height doubled
	 * ^bit 2: movement speed increased by 50%
	 * ^bit 3-6: determines held item
	 * ^bit 7-10: determines drop item
	 * ^bit 11: takes only half damage from attacks
	 * ^bit 12: can fall twice as far
	 * ^bit 13-16: determines favored walking locations
	 */
	public long flags; //more flags to be added later
	
	static final public ItemStack heldObjs[] = new ItemStack[]{
		new ItemStack(Items.compass, 1), new ItemStack(Items.stone_sword, 1), new ItemStack(Blocks.cactus, 1),
		new ItemStack(Items.apple, 1), new ItemStack(Items.iron_pickaxe, 1), new ItemStack(Blocks.tnt, 1),
		new ItemStack(Items.map, 1), new ItemStack(Items.golden_shovel, 1), new ItemStack(Blocks.planks, 1),
		new ItemStack(Items.fishing_rod, 1), new ItemStack(Items.wooden_axe, 1), new ItemStack(Blocks.sapling, 1),
		new ItemStack(Items.diamond_hoe, 1), new ItemStack(Items.book, 1), new ItemStack(Blocks.torch, 1)
	};
	
	static final public Item dropIDs[] = new Item[]{
		Items.stick, Items.bowl, Items.leather_boots, Item.getItemFromBlock(Blocks.ladder),
		Items.arrow, Items.wheat_seeds, Items.paper, Item.getItemFromBlock(Blocks.torch),
		Items.bone, Items.flint, Items.golden_leggings, Item.getItemFromBlock(Blocks.reeds),
		Items.string, Items.stone_hoe, Items.painting
	};
}
