package ogresean.bats;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BBEntityInsectBat extends BBEntityBat {
	public BBEntityInsectBat(World world) {
		super(world);
	}

	@Override
	public int getBiomeMaxY(BiomeGenBase biome) {
		return biome == BiomeGenBase.forest || biome == BiomeGenBase.swampland ? 127 : 64;
	}

	@Override
	public int getBiomeMinY(BiomeGenBase biome) {
		return 6;
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return worldObj.isDaytime() ? 14 : 8;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(3D);
	}

	@Override
	protected byte getAttackDelay() {
		return (byte) (getBatAction() > 2 ? 40 : 80);
	}

	@Override
	protected int getBatDamage() {
		return getBatAction() < 3 ? 2 : 3;
	}

	/**
	 * @return the additional x and z velocity that is added to entities that
	 *         collide with the bat.
	 */
	@Override
	protected double getBonusVelocity() {
		return (rand.nextInt(3 + worldObj.difficultySetting.ordinal() * 2) + 4 + worldObj.difficultySetting.ordinal());
	}

	/**
	 * @return the additional y velocity that is added to entities that collide
	 *         with the bat.
	 */
	@Override
	protected double getBonusVelocity2() {
		return 0.01D + worldObj.difficultySetting.ordinal() * 0.01D;
	}

	/**
	 * @return a value that determines how often the bat drops manure. higher =
	 *         less often
	 */
	@Override
	protected int getDropFreq() {
		return 3200;
	}

	@Override
	protected double getXZFlight() {
		return 0.038D - (getMaxHealth() - getHealth()) * 0.0038D;
	}

	@Override
	protected double getXZFlightAttackBoost() {
		return 0.028D;
	}

	@Override
	protected double getYFlight() {
		return 0.16D - (getMaxHealth() - getHealth()) * 0.016D;
	}

	@Override
	protected double getYFlightAttackBoost() {
		return 0.08D;
	}

	@Override
	protected void handleEchos() {
		if (rand.nextFloat() < (isWet() ? 0.024F : 0.008F)) {
			worldObj.playSoundAtEntity(this, getEchoSound(), getSoundVolume() * 1.1F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
		}
	}

    @Override
	protected boolean isValidTarget(EntityLivingBase el) {
		return el instanceof EntitySpider;
	}

	@Override
	protected boolean willAttack() {
		return getBatAction() > 2 ? rand.nextInt(20) == 0 : rand.nextInt(10) == 0;
	}

	/**
	 * @return true if the bat has become aggressive
	 */
	@Override
	protected boolean willBecomeAggressive(Entity entity) {
		return entity instanceof EntitySpider;
	}
}
