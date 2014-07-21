package ogresean.bats;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BBEntityMeatEaterBat extends BBEntityBat {
	boolean isRabid = false;

	public BBEntityMeatEaterBat(World world) {
		super(world);
	}

	@Override
	public int getBiomeMaxY(BiomeGenBase biome) {
		return 127;
	}

	@Override
	public int getBiomeMinY(BiomeGenBase biome) {
		return 6;
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return worldObj.isDaytime() ? 3 : 4;
	}

	@Override
	public float getScale() {
		return 0.5F;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(6D);
	}

	@Override
	protected void awakeUpdate() {
		super.awakeUpdate();
		if (!isRabid && rand.nextInt(400) == 0 && worldObj.difficultySetting.ordinal() > 0)
			isRabid = true;
	}

	@Override
	protected byte getAttackDelay() {
		return (byte) (getBatAction() > 2 ? 40 : 120);
	}

	@Override
	protected int getBatDamage() {
		return 2 + rand.nextInt(getBatAction() > 2 ? 5 : 3);
	}

	/**
	 * @return the additional x and z velocity that is added to entities that
	 *         collide with the bat.
	 */
	@Override
	protected double getBonusVelocity() {
		return (rand.nextInt(2 + worldObj.difficultySetting.ordinal() * 3) + 2 + worldObj.difficultySetting.ordinal());
	}

	/**
	 * @return the additional y velocity that is added to entities that collide
	 *         with the bat.
	 */
	@Override
	protected double getBonusVelocity2() {
		return 0.34D + worldObj.difficultySetting.ordinal() * 0.011D;
	}

	/**
	 * @return a value that determines how often the bat drops manure. higher =
	 *         less often
	 */
	@Override
	protected int getDropFreq() {
		return 5600;
	}

	@Override
	protected double getXZFlight() {
		return 0.045000000000000003D - (getMaxHealth() - getHealth()) * 0.003D;
	}

	@Override
	protected double getXZFlightAttackBoost() {
		return 0.035D;
	}

	@Override
	protected double getXZMaxFlight() {
		return 1.5D - (getMaxHealth() - getHealth()) * 0.12D;
	}

	@Override
	protected double getXZMaxFlightAttackBoost() {
		return 2.1D;
	}

	@Override
	protected double getYFlight() {
		return 0.19D - (getMaxHealth() - getHealth()) * 0.01D;
	}

	@Override
	protected double getYFlightAttackBoost() {
		return 0.16D;
	}

	@Override
	protected double getYMaxFlight() {
		return 1.8D - (getMaxHealth() - getHealth()) * 0.12D;
	}

	@Override
	protected double getYMaxFlightAttackBoost() {
		return 1.5D;
	}

	@Override
	protected void handleEchos() {
		if (rand.nextFloat() < (isWet() ? 0.03F : 0.01F)) {
			worldObj.playSoundAtEntity(this, getEchoSound(), getSoundVolume() * 1.1F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.25F);
		}
	}

	@Override
	protected boolean isTamingItemID(ItemStack id) {
		return !isRabid && (id!=null && id.getItem() == Items.fish);
	}

	@Override
	protected boolean isValidTarget(EntityLivingBase el) {
		return isRabid ? super.isValidTarget(el)
				: (getBatAction() > 2 ? ((el instanceof BBEntityBat && ((BBEntityBat) el).attackTarget instanceof EntityPlayer) || (el instanceof EntityCreature && ((EntityCreature) el)
						.getEntityToAttack() instanceof EntityPlayer)) : ((el instanceof BBEntityBat && rand.nextFloat() < 0.2F) || el instanceof EntityChicken));
	}

	@Override
	protected float maxCeilingLight() {
		return getBatAction() > 2 ? 6F : 6F;
	}

	@Override
	protected float maxWanderLight() {
		return 5F;
	}

	@Override
	protected boolean willAttack() {
		return isRabid ? super.willAttack() : (getBatAction() > 2 ? rand.nextInt(40) == 0 : rand.nextInt(50) == 0);
	}
}
