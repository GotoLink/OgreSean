package ogresean.bats;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class BBEntityNectarBat extends BBEntityBat {
	public boolean flowerHover;
	public short flowerTimer;

	public BBEntityNectarBat(World world) {
		super(world);
		flowerHover = false;
		flowerTimer = 0;
	}

	@Override
	public void applyEntityCollision(Entity entity) {
		if (flowerHover) {
			flowerHover = false;
			flowerTimer = -350;
			setNewWayPoint();
		}
		super.applyEntityCollision(entity);
	}

	@Override
	public boolean attackEntityFrom(DamageSource d, float i) {
		if (flowerHover) {
			flowerHover = false;
			flowerTimer = -350;
			setNewWayPoint();
		}
		return super.attackEntityFrom(d, i);
	}

	public void faceBlock(double x, double y, double z, float f, float f1) {
		double d = x - posX;
		double d2 = z - posZ;
		double d1 = y - posY + getEyeHeight();
		double d3 = MathHelper.sqrt_double(d * d + d2 * d2);
		float f2 = (float) ((Math.atan2(d2, d) * 180D) / Math.PI) - 90F;
		float f3 = (float) (-((Math.atan2(d1, d3) * 180D) / Math.PI));
		rotationPitch = -updateRotation(rotationPitch, f3, f1);
		rotationYaw = updateRotation(rotationYaw, f2, f);
	}

	//spawn related; -1 if invalid biome
	@Override
	public int getBiomeMaxY(BiomeGenBase biome) {
		return biome == BiomeGenBase.desert || Arrays.asList(BiomeDictionary.getTypesForBiome(biome)).contains(Type.FROZEN) ? -1 : 127;//ocean
	}

	//spawn related; 200 if invalid biome
	@Override
	public int getBiomeMinY(BiomeGenBase biome) {
		return biome == BiomeGenBase.desert || Arrays.asList(BiomeDictionary.getTypesForBiome(biome)).contains(Type.FROZEN) ? 200 : 50;
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return worldObj.isDaytime() ? 9 : 9;
	}

	@Override
	public float getScale() {
		return 0.44F;
	}

	@Override
	public ResourceLocation getTexture() {
		return new ResourceLocation("ogresean", "textures/bat/nectarBat.png");
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(4D);
	}

	@Override
	protected void awakeUpdate() {
		super.awakeUpdate();
		//insert flower hovering code here
		if (!flowerHover)
			flowerCheck();
		else
			flowerFloat();
	}

	protected void flowerCheck() {
		if (flowerTimer < 0) {
			flowerTimer++;
			return;
		}
		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(boundingBox.minY);
		int k = MathHelper.floor_double(posZ);
		if (isFavoredTravelBlock(i, j, k))
			flowerHover = true;
	}

	protected void flowerFloat() {
		motionX = 0.0D;
		motionY = 0.0D;
		motionZ = 0.0D;
		faceBlock(MathHelper.floor_double(posX) + 0.5D, MathHelper.floor_double(boundingBox.minY) + 0.5D, MathHelper.floor_double(posZ) + 0.5D, 180F, 180F);
		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(boundingBox.minY);
		int k = MathHelper.floor_double(posZ);
		if (flowerTimer++ > 200 || !isFavoredTravelBlock(i, j, k)) {
			flowerTimer = -400;
			flowerHover = false;
			setNewWayPoint();
		}
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
	 *         collide with the bat.
	 */
	@Override
	protected double getBonusVelocity() {
		return (rand.nextInt(1 + worldObj.difficultySetting * 2) + 2 + worldObj.difficultySetting);
	}

	/**
	 * @return the additional y velocity that is added to entities that collide
	 *         with the bat.
	 */
	@Override
	protected double getBonusVelocity2() {
		return 0.007D + worldObj.difficultySetting * 0.007D;
	}

	/**
	 * @return a value that determines how often the bat drops manure. higher =
	 *         less often
	 */
	@Override
	protected int getDropFreq() {
		return 8200;
	}

	@Override
	protected double getWaypointDistance() {
		return isFavoredTravelBlock(wayPoints[0], wayPoints[1], wayPoints[2]) ? 0.03D : super.getWaypointDistance();
	}

	@Override
	protected double getXZFlight() {
		return 0.03D - (getMaxHealth() - getHealth()) * 0.0050D;
	}

	@Override
	protected double getXZFlightAttackBoost() {
		return 0.0D;
	}

	@Override
	protected double getYFlight() {
		return 0.13D - (getMaxHealth() - getHealth()) * 0.025D;
	}

	@Override
	protected double getYFlightAttackBoost() {
		return 0.0D;
	}

	@Override
	protected void handleEchos() {
		if (rand.nextFloat() < (isWet() ? 0.013F : 0.003F)) { //pitch changed from 1.0 to 1.1
			worldObj.playSoundAtEntity(this, getEchoSound(), getSoundVolume() * 0.8F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.1F);
		}
	}

	@Override
	protected void handleYMotion() {
		if (onGround)
			motionY += 0.473D;
		if (motionY < 0.0D) {
			motionY *= 0.65700000000000004D;
		}
	}

	@Override
	protected boolean isFavoredTravelBlock(int i, int j, int k) {
		return flowerTimer < 0 ? false : worldObj.getBlockId(i, j, k) == Block.plantRed.blockID || worldObj.getBlockId(i, j, k) == Block.plantYellow.blockID;
	}

	@Override
	protected boolean isInvalidTravelBlock(int i, int j, int k) {
		return worldObj.getBlockId(i, j, k) != 0 && !isFavoredTravelBlock(i, j, k);
	}

	@Override
	protected boolean isLooseBlock(int bid) {
		return bid == Block.torchWood.blockID || bid == Block.crops.blockID || bid == Block.torchRedstoneActive.blockID || bid == Block.torchRedstoneIdle.blockID || bid == Block.sapling.blockID
				|| bid == Block.reed.blockID || bid == Block.signPost.blockID || bid == Block.redstoneWire.blockID || bid == Block.mushroomBrown.blockID || bid == Block.mushroomRed.blockID;
	}

	@Override
	protected boolean isTamingItemID(int id) {
		return id == Block.plantYellow.blockID || id == Block.plantRed.blockID;
	}

	protected boolean isValidTarget(EntityLiving el) {
		return false;
	}

	@Override
	protected float maxCeilingLight() {
		return batAction > 2 ? 7F : 5F;
	}

	@Override
	protected float maxWanderLight() {
		return 5F;
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

	private float updateRotation(float f, float f1, float f2) {
		float f3;
		for (f3 = f1 - f; f3 < -180F; f3 += 360F) {
		}
		for (; f3 >= 180F; f3 -= 360F) {
		}
		if (f3 > f2) {
			f3 = f2;
		}
		if (f3 < -f2) {
			f3 = -f2;
		}
		return f + f3;
	}
}
