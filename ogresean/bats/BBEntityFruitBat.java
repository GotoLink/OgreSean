package ogresean.bats;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BBEntityFruitBat extends BBEntityBat {
    public EntityItem heldItem;

    public BBEntityFruitBat(World world) {
        super(world);
    }

    @Override
    public boolean attackEntityFrom(DamageSource d, float i) {
        if (heldItem != null) {
            heldItem.age = 5600;
            heldItem.delayBeforeCanPickup = 20;
            heldItem.motionX = rand.nextFloat() / 10D - 0.05D;
            heldItem.motionY = rand.nextFloat() / 20D;
            heldItem.motionZ = rand.nextFloat() / 10D - 0.05D;
            heldItem = null;
        }
        return super.attackEntityFrom(d, i);
    }

    //spawn related; -1 if invalid biome
    @Override
    public int getBiomeMaxY(BiomeGenBase biome) {
        return biome == BiomeGenBase.forest || biome == BiomeGenBase.swampland ? 127 : -1;
    }

    //spawn related; 200 if invalid biome
    @Override
    public int getBiomeMinY(BiomeGenBase biome) {
        return biome == BiomeGenBase.forest || biome == BiomeGenBase.swampland ? 56 : 200;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return worldObj.isDaytime() ? 5 : 3;
    }

    @Override
    public float getScale() {
        return 0.48F;
    }

    @Override
    public boolean interact(EntityPlayer entityplayer) {
        if (heldItem != null) {
            heldItem.age = 5600;
            heldItem.delayBeforeCanPickup = 20;
            heldItem.motionX = rand.nextFloat() / 10D - 0.05D;
            heldItem.motionY = rand.nextFloat() / 20D;
            heldItem.motionZ = rand.nextFloat() / 10D - 0.05D;
            heldItem = null;
        }
        return super.interact(entityplayer);
    }

    @Override
    protected void awakeUpdate() {
        super.awakeUpdate();
        if (heldItem == null && noClip && rand.nextInt(80) == 0)
            getItemFromLeaves(rand.nextFloat());
        if (heldItem != null)
            handleHeldItem();
    }

    @Override
    protected byte getAttackDelay() {
        return 120;
    }

    @Override
    protected int getBatDamage() {
        return 1;
    }

    /**
     * @return the additional x and z velocity that is added to entities that
     * collide with the bat.
     */
    @Override
    protected double getBonusVelocity() {
        return (rand.nextInt(1 + worldObj.difficultySetting.ordinal() * 3) + 4 + worldObj.difficultySetting.ordinal());
    }

    /**
     * @return the additional y velocity that is added to entities that collide
     * with the bat.
     */
    @Override
    protected double getBonusVelocity2() {
        return 0.031D + worldObj.difficultySetting.ordinal() * 0.01D;
    }

    /**
     * @return a value that determines how often the bat drops manure. higher =
     * less often
     */
    @Override
    protected int getDropFreq() {
        return 7600;
    }

    protected void getItemFromLeaves(float f) {
        heldItem = new EntityItem(worldObj);
        if (f < 0.4F)
            heldItem.setEntityItemStack(new ItemStack(Items.stick));
        else if (f < 0.6F)
            heldItem.setEntityItemStack(new ItemStack(Blocks.leaves, 1, worldObj.getBlockMetadata(MathHelper.floor_double(posX), MathHelper.floor_double(boundingBox.minY),
                    MathHelper.floor_double(posZ))));
        else if (f < 0.8F)
            heldItem.setEntityItemStack(new ItemStack(Blocks.sapling, 1, worldObj.getBlockMetadata(MathHelper.floor_double(posX), MathHelper.floor_double(boundingBox.minY),
                    MathHelper.floor_double(posZ)) & 3));
        else if (f < 0.9F)
            heldItem.setEntityItemStack(new ItemStack(Blocks.web));
        else if (f < 0.93F)
            heldItem.setEntityItemStack(new ItemStack(Items.egg));
        else if (f < 0.96F)
            heldItem.setEntityItemStack(new ItemStack(Items.feather));
        else if (f < 0.98F)
            heldItem.setEntityItemStack(new ItemStack(Items.golden_apple));
        else if (f < 0.99F)
            heldItem.setEntityItemStack(new ItemStack(Items.record_13));
        else if (f < 1F)
            heldItem.setEntityItemStack(new ItemStack(Items.record_cat));
        worldObj.spawnEntityInWorld(heldItem);
    }

    @Override
    protected double getXZFlight() {
        return 0.028D - (getMaxHealth() - getHealth()) * 0.0043D;
    }

    @Override
    protected double getXZFlightAttackBoost() {
        return 0.0D;
    }

    @Override
    protected double getYFlight() {
        return 0.1134D - (getMaxHealth() - getHealth()) * 0.022D;
    }

    @Override
    protected double getYFlightAttackBoost() {
        return 0.0D;
    }

    @Override
    protected void handleEchos() {
        if (rand.nextFloat() < (isWet() ? 0.015F : 0.003F)) { //pitch changed from 1.0 to 1.2
            worldObj.playSoundAtEntity(this, getEchoSound(), getSoundVolume() * 0.8F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.2F);
        }
    }

    protected void handleHeldItem() {
        heldItem.motionX = 0D;
        heldItem.motionY = 0D;
        heldItem.motionZ = 0D;
        heldItem.age = 5900;
        heldItem.delayBeforeCanPickup = 200;
        heldItem.setPosition(posX, boundingBox.minY - 0.22, posZ);
    }

    @Override
    protected void handleYMotion() {
        if (onGround)
            motionY += 0.3698D;
        if (motionY < 0.0D) {
            motionY *= 0.77500000000840004D;
        }
    }

    @Override
    protected boolean isTamingItemID(ItemStack id) {
        return id != null && id.getItem() == Items.apple;
    }

    @Override
    protected boolean isValidTarget(EntityLivingBase el) {
        return false;
    }

    @Override
    protected float maxCeilingLight() {
        return getBatAction() > 2 ? 8F : 7F;
    }

    @Override
    protected float maxWanderLight() {
        return 6F;
    }

    @Override
    protected void sleepingUpdate() {
        super.sleepingUpdate();
        if (heldItem != null) {
            heldItem.age = 5600;
            heldItem.delayBeforeCanPickup = 20;
            heldItem = null;
        }
    }

    @Override
    protected boolean willAttack() {
        return false;
    }

    /**
     * @return true if the bat has become aggressive
     */
    @Override
    protected boolean willBecomeAggressive(Entity entity) {
        return false;
    }
}
