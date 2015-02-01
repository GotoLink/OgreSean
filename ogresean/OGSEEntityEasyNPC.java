package ogresean;

import net.minecraft.command.IEntitySelector;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.List;

public class OGSEEntityEasyNPC extends EntityCreature implements IMob {

    public final IEntitySelector NPC_SENSOR = new IEntitySelector()
    {
        @Override
        public boolean isEntityApplicable(Entity entity)
        {
            return entity.isEntityAlive() && entity instanceof OGSEEntityEasyNPC;
        }
    };
    private int ID = -1;
    private ResourceLocation texture;
    /**
     * ^bit 1: jump height doubled
     * ^bit 2: movement speed increased by 50%
     * ^bit 3-6: determines held item
     * ^bit 7-10: determines drop item
     * ^bit 11: takes only half damage from attacks
     * ^bit 12: can fall twice as far
     * ^bit 13-16: determines favored walking locations
     */
    private long flags; //more flags to be added later
    public OGSEEntityEasyNPC(World world) {
        super(world);
        flags = rand.nextLong();
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(12, ID);
    }

    @Override
    public boolean getCanSpawnHere() {
        boolean flag = false;
        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(boundingBox.minY);
        int k = MathHelper.floor_double(posZ);
        if (EasyNPCs.canSpawnUnderground || worldObj.canBlockSeeTheSky(i, j, k)) //prevent underground spawning if enabled
            flag = !worldObj.checkBlockCollision(boundingBox) && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty() && !worldObj.isAnyLiquid(boundingBox) && worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(16.0D, 5.0D, 16.0D), NPC_SENSOR).isEmpty() && EasyNPCs.canSpawn(worldObj); //prevent more spawning than allowed

        if (flag) {
            ID = EasyNPCs.onNPCSpawn(worldObj);
            if(ID<0)
                return false;
            setupTexture();
        }

        return flag;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (ID < 0 || ID >= EasyNPCs.maxNPCs)
            setDead();
        /*if(((flags >> 12) & 15) < 7 && !hasPath())TODO
            func_31026_E();*/
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setInteger("EasyIDNum", ID);
        nbttagcompound.setLong("Flags", flags);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        super.readEntityFromNBT(nbttagcompound);
        ID = nbttagcompound.getInteger("EasyIDNum");
        flags = nbttagcompound.getLong("Flags");
        setupTexture();
    }

    private void setupTexture(){
        this.dataWatcher.updateObject(12, ID);
    }

    @Override
    public void setDead() {
        super.setDead();
        EasyNPCs.onNPCDeath(worldObj);
    }

    @Override
    protected boolean canDespawn() {
        return EasyNPCs.canNaturallyDespawn;
    }

    @Override
    protected void jump() {
        if ((flags & 1) == 1)
            motionY = 0.83D;
        else
            motionY = 0.42D;

        //field_35118_ao = true;TODO
    }

    @Override
    public void moveFlying(float f, float f1, float f2) {
        if (((flags >> 1) & 1) == 1)
            f2 *= 1.5F;
        super.moveFlying(f, f1, f2);
    }

    @Override
    public ItemStack getHeldItem() {
        if (((flags >> 2) & 15) > 0)
            return heldObjs[(int) ((flags >> 2) & 15) - 1];
        else
            return null;
    }

    @Override
    protected Item getDropItem() {
        if (((flags >> 6) & 15) > 0)
            return dropIDs[(int) ((flags >> 6) & 15) - 1];
        else
            return null;
    }

    @Override
    public boolean attackEntityFrom(DamageSource d, float i) {
        if (((flags >> 10) & 1) == 1 && i > 1)
            i /= 2;
        return
                super.attackEntityFrom(d, i);
    }

    @Override
    protected void fall(float f) {
        if (((flags >> 11) & 1) == 1)
            f /= 2.0F;
        super.fall(f);
    }

    @Override
    public float getBlockPathWeight(int i, int j, int k) {
        int movementType = (int) ((flags >> 12) & 15);
        float xDif = (float) (posX - i);
        float zDif = (float) (posZ - k);

        switch (movementType) {
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
                if (worldObj.getBlock(i, j, k) == Blocks.grass) return 10F;
                else return 0.0F;
            case 6:
                if (worldObj.getBlock(i, j, k) == Blocks.sand) return 10F;
                else return 0.0F;
            case 7:
                if (worldObj.getBlock(i, j, k) == Blocks.stone) return 10F;
                else return 0.0F;
            case 8:
                if (worldObj.getBlock(i, j, k) == Blocks.gravel) return 10F;
                else return 0.0F;
            case 9:
                if (worldObj.getBlock(i, j, k) == Blocks.dirt) return 10F;
                else return 0.0F;
            case 10:
                if (worldObj.getBlock(i, j, k) == Blocks.log) return 10F;
                else return 0.0F;
            case 11:
                if (worldObj.getBlock(i, j, k) == Blocks.snow) return 10F;
                else return 0.0F;
            case 12:
                if (worldObj.getBlock(i, j, k) == Blocks.ice) return 10F;
                else return 0.0F;
            case 13:
                if (worldObj.getBlock(i, j, k) == Blocks.tallgrass) return 10F;
                else return 0.0F;
            case 14:
                return (float) (j - posY);
            case 15:
                return (float) (posY - j);
            default:
                return super.getBlockPathWeight(i, j, k);
        }
    }

    public ResourceLocation getTexture(){
        if(texture == null){
            ID = this.dataWatcher.getWatchableObjectInt(12);
            texture = new ResourceLocation("ogresean", new StringBuilder("textures/mob/EasyNPC").append(ID + 1).append(".png").toString());
        }
        return texture;
    }

    public int getID(){
        return ID;
    }

    public static final ItemStack heldObjs[] = new ItemStack[]{
            new ItemStack(Items.compass), new ItemStack(Items.stone_sword), new ItemStack(Blocks.cactus),
            new ItemStack(Items.apple), new ItemStack(Items.iron_pickaxe), new ItemStack(Blocks.tnt),
            new ItemStack(Items.map), new ItemStack(Items.golden_shovel), new ItemStack(Blocks.planks),
            new ItemStack(Items.fishing_rod), new ItemStack(Items.wooden_axe), new ItemStack(Blocks.sapling),
            new ItemStack(Items.diamond_hoe), new ItemStack(Items.book), new ItemStack(Blocks.torch)
    };

    public static final Item dropIDs[] = new Item[]{
            Items.stick, Items.bowl, Items.leather_boots, Item.getItemFromBlock(Blocks.ladder),
            Items.arrow, Items.wheat_seeds, Items.paper, Item.getItemFromBlock(Blocks.torch),
            Items.bone, Items.flint, Items.golden_leggings, Item.getItemFromBlock(Blocks.reeds),
            Items.string, Items.stone_hoe, Items.painting
    };
}
