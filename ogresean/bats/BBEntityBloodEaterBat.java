package ogresean.bats;

import java.util.List;

import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class BBEntityBloodEaterBat extends BBEntityBat {
	public EntityAnimal latchTarget;

	public BBEntityBloodEaterBat(World world) {
		super(world);
		latchTarget = null;
	}

	@Override
	public void applyEntityCollision(Entity entity) {
		if (latchTarget == null)
			super.applyEntityCollision(entity);
	}

	@Override
	public boolean attackEntityFrom(DamageSource d, float i) {
		if (latchTarget != null)
			latchTarget = null;
		return super.attackEntityFrom(d, i);
	}

	@Override
	public boolean canBeCollidedWith() {
		return !isDead && latchTarget == null;
	}

	@Override
	public boolean canBePushed() {
		return latchTarget == null;
	}

	//spawn related; -1 if invalid biome
	@Override
	public int getBiomeMaxY(BiomeGenBase biome) {
		return biome == BiomeGenBase.swampland ? 127 : -1;
	}

	//spawn related; 200 if invalid biome
	@Override
	public int getBiomeMinY(BiomeGenBase biome) {
		return biome == BiomeGenBase.swampland ? 40 : 200;
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return worldObj.isDaytime() ? 6 : 6;
	}

	@Override
	public float getScale() {
		return 0.32F;
	}

	@Override
	public ResourceLocation getTexture() {
		return bloodEater;
	}

	@Override
	public void playerEntityAttack(Entity e) {
		if (getBatAction() == 4 && e != this) {
			attackTarget = e;
			attackDelay = 0;
		}
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(7D);
	}

	@Override
	protected boolean attackEnemy() {
		boolean flag = super.attackEnemy();
		if (flag)
			heal(getBatDamage());
		return flag;
	}

	@Override
	protected void awakeUpdate() {
		super.awakeUpdate();
		if (latchTarget == null && rand.nextInt(40) == 0) {
			latchTarget = getLatchTarget();
			if (latchTarget == null)
				return;
			attackTarget = null;
			attackDelay = 0;
		}
		if (latchTarget != null)
			handleLatching();
	}

	@Override
	protected byte getAttackDelay() {
		return (byte) (getBatAction() > 2 ? 60 : 120);
	}

	@Override
	protected float getAttackDistance() {
		return 1.4F;
	}

	@Override
	protected int getBatDamage() {
		return latchTarget == null || !(attackTarget instanceof EntityPig) ? 1 + rand.nextInt(getBatAction() > 2 ? 3 : 2) : 0;
	}

	/**
	 * @return the additional x and z velocity that is added to entities that
	 *         collide with the bat.
	 */
	@Override
	protected double getBonusVelocity() {
		return (rand.nextInt(2 + worldObj.difficultySetting.ordinal()) + 2 + worldObj.difficultySetting.ordinal());
	}

	/**
	 * @return the additional y velocity that is added to entities that collide
	 *         with the bat.
	 */
	@Override
	protected double getBonusVelocity2() {
		return 0.04D + worldObj.difficultySetting.ordinal() * 0.003D;
	}

	/**
	 * @return a value that determines how often the bat drops manure. higher =
	 *         less often
	 */
	@Override
	protected int getDropFreq() {
		return 9600;
	}

	protected EntityAnimal getLatchTarget() {
		double d1 = 9999D;
		EntityAnimal ea = null;
		List<?> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(1.2, 1.2, 1.2));
		for (int i = 0; i < list.size(); i++) {
			Entity entity1 = (Entity) list.get(i);
			if (!(entity1 instanceof EntityAnimal) || entity1 instanceof EntityChicken || entity1 == ridingEntity || entity1 == riddenByEntity) {
				continue;
			}
			double d2 = entity1.getDistanceSq(posX, posY, posZ);
			if ((d2 < d1) && ((EntityAnimal) entity1).canEntityBeSeen(this)) {
				d1 = d2;
				ea = (EntityAnimal) entity1;
			}
		}
		return ea;
	}

	@Override
	protected double getXZFlight() {
		return 0.051D - (getMaxHealth() - getHealth()) * 0.003D;
	}

	@Override
	protected double getXZFlightAttackBoost() {
		return 0.009D;
	}

	@Override
	protected double getXZMaxFlight() {
		return 1.7D - (getMaxHealth() - getHealth()) * 0.2D;
	}

	@Override
	protected double getXZMaxFlightAttackBoost() {
		return 0.3D;
	}

	@Override
	protected double getYFlight() {
		return 0.22D - (getMaxHealth() - getHealth()) * 0.01D;
	}

	@Override
	protected double getYFlightAttackBoost() {
		return 0.06D;
	}

	@Override
	protected double getYMaxFlight() {
		return 2.0D - (getMaxHealth() - getHealth()) * 0.25D;
	}

	@Override
	protected double getYMaxFlightAttackBoost() {
		return 0.5D;
	}

	@Override
	protected void handleEchos() {
		if (rand.nextFloat() < (isWet() ? 0.025F : 0.01F)) {
			worldObj.playSoundAtEntity(this, getEchoSound(), getSoundVolume() * 0.8F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 0.75F);
		}
	}

	protected void handleLatching() {
		motionX = 0D;
		motionY = 0D;
		motionZ = 0D;
		faceEntity(latchTarget, 180F, 180F);
		float rotationY = latchTarget.rotationYaw - 135F;
		if (rotationY < 0)
			rotationY += 360F;
		double d = latchTarget.posX - (latchTarget.width * MathHelper.cos((rotationY / 180F) * 3.141593F));
		double d1 = (latchTarget.boundingBox.minY + latchTarget.boundingBox.maxY) / 2D;
		double d2 = latchTarget.posZ - (latchTarget.width * 1.25 * MathHelper.sin((rotationY / 180F) * 3.141593F));
		setPosition(d, d1, d2);
		if (latchTarget.hurtTime == 0 && ticksExisted % 100 == 0) {
			latchTarget.attackEntityFrom(null, 0);
			heal(1);
		}
		if (ticksExisted % 40 == 0 && rand.nextInt(60) == 0 || latchTarget.isDead)
			latchTarget = null;
	}

	@Override
	protected boolean isTamingItemID(ItemStack id) {
		return id!=null && id.getItem() == Items.porkchop;
	}

    @Override
	protected boolean isValidTarget(EntityLivingBase el) {
		return getBatAction() > 2 ? ((el instanceof BBEntityBat && ((BBEntityBat) el).attackTarget instanceof EntityPlayer) || (el instanceof EntityCreature && ((EntityCreature) el).getEntityToAttack() instanceof EntityPlayer))
				: (el instanceof EntityAnimal && !(el instanceof EntityChicken));
	}

	@Override
	protected float maxCeilingLight() {
		return getBatAction() > 2 ? 6F : 4F;
	}

	@Override
	protected float maxWanderLight() {
		return 4F;
	}

	@Override
	protected boolean willAttack() {
		return latchTarget == null && getBatAction() > 2 ? rand.nextInt(80) == 0 : rand.nextInt(160) == 0;
	}
}
