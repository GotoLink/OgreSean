package ogresean.bats;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public abstract class BBEntityBat extends EntityLiving {
    public static final ResourceLocation bloodEater = new ResourceLocation("ogresean", "textures/bat/bloodEaterBat.png");
    public static final ResourceLocation insect = new ResourceLocation("ogresean", "textures/bat/insectBat.png");
    public static final ResourceLocation fruit = new ResourceLocation("ogresean", "textures/bat/fruitBat.png");
    public static final ResourceLocation meatEater = new ResourceLocation("ogresean", "textures/bat/meatEaterBat.png");
    public static final ResourceLocation nectar = new ResourceLocation("ogresean", "textures/bat/nectarBat.png");
    public static final ResourceLocation cave = new ResourceLocation("ogresean", "textures/bat/caveBat.png");
	public float wingb;
	public float wingc;
	public float wingd;
	public float winge;
	public float wingh;
	public int wayPoints[]; //determines wayPoint coordinate [x, y, z]
	protected int batTimer;
	public Entity attackTarget; //target that bat will attack
	protected byte attackDelay; //delay before bat's next attack.
	protected byte wakeDelay; //initial delay before tamed bats can possibly wake up
	protected byte tempClip; //No Clip related; handles the transition between leaves and air

	public BBEntityBat(World world) {
		super(world);
		setSize(getScale() - 0.2F, getScale() - 0.1F);
		wingb = 0.0F;
		wingc = 0.0F;
		wingh = 1.0F;
		wayPoints = null;
		renderDistanceWeight = 5D;
		batTimer = 0;
		attackDelay = 0;
		wakeDelay = 0;
		attackTarget = null;
		tempClip = -1;
	}

    @Override
    public void entityInit(){
        super.entityInit();
        this.dataWatcher.addObject(20, Byte.valueOf((byte) 0));
        this.dataWatcher.addObject(21, Byte.valueOf((byte) 0));
    }

    /**
     * batActions: 0: Sleeping 1: Awake - Panicking 2: Awake - Wandering 3:
     * Tamed - Sleeping 4: Tamed - Following 5: Tamed - Looking for spot to
     * sleep at
     */
    public byte getBatAction(){
        return this.dataWatcher.getWatchableObjectByte(20);
    }

    public void setBatAction(byte action){
        this.dataWatcher.updateObject(20, action);
    }

    public byte getBatDirection(){
        return this.dataWatcher.getWatchableObjectByte(21);
    }

    public void setFlightDirection(byte direction){
        this.dataWatcher.updateObject(21, direction);
    }

	//on collide with other entity, pushes entity far
	@Override
	public void applyEntityCollision(Entity entity) {
		if (entity.riddenByEntity == this || entity.ridingEntity == this) {
			return;
		}
		if (entity instanceof BBEntityBat) {
			BBEntityBat bat = (BBEntityBat) entity;
			if (bat.getBatAction() == 0 || bat.getBatAction() == 3)
				bat.wakeUp();
			else if (getBatAction() == 0 || getBatAction() == 3)
				wakeUp();
		}
		double d = entity.posX - posX;
		double d1 = entity.posZ - posZ;
		double d2 = MathHelper.abs_max(d, d1);
		if (d2 >= 0.01D) {
			d2 = MathHelper.sqrt_double(d2);
			d /= d2;
			d1 /= d2;
			double d3 = 1.0D / d2;
			if (d3 > 1.0D) {
				d3 = 1.0D;
			}
			d *= d3;
			d1 *= d3;
			d *= 0.05000000074505806D;
			d1 *= 0.05000000074505806D;
			d *= 1.0F - entityCollisionReduction;
			d1 *= 1.0F - entityCollisionReduction;
			double bonusVelocity = getBatAction() == 0 || getBatAction() == 3 ? 1.0D : getBonusVelocity();
			addVelocity(-d, 0.0D, -d1);
			entity.addVelocity(d * bonusVelocity, getBonusVelocity2(), d1 * bonusVelocity);
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource d, float i) {
		Entity entity = d.getEntity();
		if (getBatAction() == 0 || getBatAction() == 3)
			wakeUp();
		if (attackTarget == null && willBecomeAggressive(entity))
			attackTarget = entity;
		return super.attackEntityFrom(d, i);
	}

	//tamed bats don't despawn
	@Override
	public boolean canDespawn() {
		if (getBatAction() > 2)
			return false;
		else
			return super.canDespawn();
	}

	public void faceLocation(int i, int j, int k, float f) {
		double d = i - posX;
		double d1 = k - posZ;
		double d2 = j - posY;
		double d3 = MathHelper.sqrt_double(d * d + d1 * d1);
		float f1 = (float) ((Math.atan2(d1, d) * 180D) / Math.PI) - 90F;
		float f2 = (float) ((Math.atan2(d2, d3) * 180D) / Math.PI);
		rotationPitch = -rotationUpdate(rotationPitch, f2, f);
		rotationYaw = rotationUpdate(rotationYaw, f1, f);
	}

	//spawn related
	public int getBiomeMaxY(BiomeGenBase biome) {
		return 127;
	}

	//spawn related
	public int getBiomeMinY(BiomeGenBase biome) {
		return 10;
	}

	@Override
	public boolean getCanSpawnHere() {
		boolean flag;
		flag = super.getCanSpawnHere();
		if (!flag)
			return false;
		//spawn onto ceiling; more common at day
		flag = findCeilingBlock();
		if (flag)
			return true;
		//if location is dark, spawn as wandering; more common at night
		flag = spawnAsWanderingBat();
		return flag;
	}

	@Override
	public float getEyeHeight() {
		if (getBatAction() == 0 || getBatAction() == 3)
			return height * -0.65F * (getScale() / 0.4F);
		else
			return height * 0.55F * (getScale() / 0.4F);
	}

	@Override
	public int getMaxSpawnedInChunk() {
		return 4;
	}

	/**
	 * @return returns the bat's scale, which affects the size of the bat.
	 */
	public float getScale() {
		return 0.4F;
	}

	@Override
	public int getTalkInterval() {
		if (getBatAction() == 0 || getBatAction() == 3)
			return 240;
		else
			return 160;
	}

	public ResourceLocation getTexture() {
		return cave;
	}

	public void handleAttacking() {
		if (attackDelay > 0)
			attackDelay--;
		if (attackTarget == null && willAttack())
			attackTarget = findTarget(); //set mob to target
		else if (attackTarget != null && attackDelay == 0) {
			if (attackTarget.isDead || (!canEntityBeSeen(attackTarget) && getDistanceToEntity(attackTarget) > 12F))
				attackTarget = null;
			else {
				wayPoints = new int[3];
				wayPoints[0] = MathHelper.floor_double(attackTarget.posX);
				wayPoints[1] = MathHelper.floor_double(attackTarget.boundingBox.minY) + 1;
				wayPoints[2] = MathHelper.floor_double(attackTarget.posZ);
				if (attackEnemy()) {
					worldObj.playSoundAtEntity(this, getEchoSound(), getSoundVolume() * 1.2F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
					chooseRandomFlightDirection();
					setNewWayPoint();
					attackDelay = getAttackDelay();
				}
			}
		}
	}

	@Override
	public void heal(float i) {
		if (getHealth() <= 0) {
			return;
		}
		setHealth(getHealth() + i);
		if (getHealth() > getMaxHealth()) {
			setHealth(getMaxHealth());
		}
	}

	@Override
	public boolean interact(EntityPlayer entityplayer) {
		if (getBatAction() == 0 || getBatAction() == 3) {
			wakeUp();
			return true;
		} else if (entityplayer.getCurrentEquippedItem() != null) {
			ItemStack item = entityplayer.getCurrentEquippedItem();
			if (item != null && item.getItem()==Items.feather && getBatAction() == 4) {
				setBatAction((byte)5);
				wayPoints = null;
			}
		}
		return false;
	}

	@Override
	public void knockBack(Entity entity, float i, double d, double d1) {
		float f = MathHelper.sqrt_double(d * d + d1 * d1);
		float f1 = 1.4F;
		motionX /= 2D;
		motionY /= 2D;
		motionZ /= 2D;
		motionX -= (d / f) * f1;
		motionY += 0.40000000596046448D;
		motionZ -= (d1 / f) * f1;
		if (motionY > 0.40000000596046448D * 2) {
			motionY = 0.40000000596046448D * 2;
		}
	}

	//player wakes up bat by touching it
	@Override
	public void onCollideWithPlayer(EntityPlayer entityplayer) {
		if (getBatAction() == 0 || getBatAction() == 3)
			wakeUp();
		super.onCollideWithPlayer(entityplayer);
	}

	@Override
	public void onLivingUpdate() {
		onSuperLivingUpdate();
		if (getBatAction() < 3)
			Bats.batCount--; //used for determining how many untamed bats are in world
		if (getHealth() <= 0) {
			setDyingVelocity();
		} else
			actionUpdate();
		handleNoClip();
	}

	////////////////New Methods////////////////
	public void onSuperLivingUpdate() {
		if (newPosRotationIncrements > 0) {
			double d = posX + (newPosX - posX) / newPosRotationIncrements;
			double d1 = posY + (newPosY - posY) / newPosRotationIncrements;
			double d2 = posZ + (newPosZ - posZ) / newPosRotationIncrements;
			double d3;
			for (d3 = newRotationYaw - rotationYaw; d3 < -180D; d3 += 360D) {
			}
			for (; d3 >= 180D; d3 -= 360D) {
			}
			rotationYaw += d3 / newPosRotationIncrements;
			rotationPitch += (newRotationPitch - rotationPitch) / newPosRotationIncrements;
			newPosRotationIncrements--;
			setPosition(d, d1, d2);
			setRotation(rotationYaw, rotationPitch);
			List<?> list1 = worldObj.getCollidingBoundingBoxes(this, boundingBox.contract(0.03125D, 0.0D, 0.03125D));
			if (list1.size() > 0) {
				double d4 = 0.0D;
				for (int j = 0; j < list1.size(); j++) {
					AxisAlignedBB axisalignedbb = (AxisAlignedBB) list1.get(j);
					if (axisalignedbb.maxY > d4) {
						d4 = axisalignedbb.maxY;
					}
				}
				d1 += d4 - boundingBox.minY;
				setPosition(d, d1, d2);
			}
		}
		if (isMovementBlocked()) {
			isJumping = false;
			moveStrafing = 0.0F;
			moveForward = 0.0F;
			randomYawVelocity = 0.0F;
		} else if (this.isClientWorld()) {
			updateEntityActionState();
		}
		boolean flag = isInWater();
		boolean flag1 = handleLavaMovement();
		if (isJumping) {
			if (flag) {
				motionY += 0.039999999105930328D;
			} else if (flag1) {
				motionY += 0.039999999105930328D;
			} else if (onGround) {
				jump();
			}
		}
		moveStrafing *= 0.98F;
		moveForward *= 0.98F;
		randomYawVelocity *= 0.9F;
		moveEntityWithHeading(moveStrafing, moveForward);
		if (!canBePushed())
			return; //this is only line added
		List<?> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(0.20000000298023224D, 0.0D, 0.20000000298023224D));
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				Entity entity = (Entity) list.get(i);
				if (entity.canBePushed()) {
					entity.applyEntityCollision(this);
				}
			}
		}
	}

	public void playerEntityAttack(Entity e) {
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		setBatAction(nbttagcompound.getByte("BatAction"));
		if (getBatAction() != 0 && getBatAction() != 3) {
			chooseRandomFlightDirection();
		}
        setFlightDirection(nbttagcompound.getByte("BatDirection"));
	}

	@Override
	public void setDead() {
		isDead = true;
		Bats.batsList.remove(this);
		if (getBatAction() > 2)
			for (ArrayList<BBEntityBat> list : Bats.assistants.values()) {
				list.remove(this);
			}
	}

	public void wakeUp() {
        int i = getBatAction();
		setBatAction((byte) (i == 0 || i == 3 ? i + 1 : i));
		chooseRandomFlightDirection();
		if (i == 1)
			wakeUpNearbyBats(16D);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setByte("BatAction", getBatAction());
        nbttagcompound.setByte("BatDirection", getBatDirection());
	}

	/**
	 * Determines what actions the bat will take based on the bat's current
	 * batAction
	 */
	protected void actionUpdate() {
		if (getBatAction() == 0 || getBatAction() == 3)
			sleepingUpdate();
		else
			awakeUpdate();
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(5D);
	}

	protected boolean attackEnemy() {
		if (getDistanceSqToEntity(attackTarget) < getAttackDistance()) {
			if (attackTarget instanceof EntityItem) {
				EntityItem item = (EntityItem) attackTarget;
				if (isTamingItemID(item.getEntityItem())) {
					devourTamingItem();
					item.setDead();
					attackTarget = null;
					return true;
				}
			} else if (attackTarget.attackEntityFrom(DamageSource.causeMobDamage(this), getBatDamage())) {
				flingTarget();
				return true;
			}
		}
		return false;
	}

	/**
	 * This method determines the actions taken by active and awake bats.
	 */
	protected void awakeUpdate() {
		handleWings();
		if (getBatAction() != 5)
			handleAttacking();
		if (!inWater) {
			handleYMotion();
			handleEchos();
		}
		if (inWater) {
			drown();
		} else if (isCollided) {
			collisionUpdate();
		} else if (wayPoints == null || this.getDistance(wayPoints[0], wayPoints[1], wayPoints[2]) < getWaypointDistance())
			setNewWayPoint();
		else {
			travelToWaypoint();
		}
		handlePlayerInteraction();
	}

	protected void blockBump() {
		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(boundingBox.minY);
		int k = MathHelper.floor_double(posZ);
		Block bid = worldObj.getBlock(i, j, k);
		if (isLooseBlock(bid)) {
			bid.dropBlockAsItem(worldObj, i, j, k, worldObj.getBlockMetadata(i, j, k), 0);
			worldObj.setBlockToAir(i, j, k);
			this.attackEntityFrom(DamageSource.inWall, 1);
		}
	}

	protected void checkForTamingItem() {
		double d1 = 9999D;
		EntityItem entityitem = null;
		List<?> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(6D, 6D, 6D));
		for (int i = 0; i < list.size(); i++) {
			Entity entity1 = (Entity) list.get(i);
			if (!(entity1 instanceof EntityItem) || entity1 == ridingEntity || entity1 == riddenByEntity) {
				continue;
			}
			double d2 = entity1.getDistanceSq(posX, posY, posZ);
			if ((d2 < d1) && canEntityBeSeen(entity1) && isTamingItemID(((EntityItem) entity1).getEntityItem())) {
				d1 = d2;
				entityitem = (EntityItem) entity1;
			}
		}
		attackTarget = entityitem;
	}

	protected void chooseRandomFlightDirection() {
		setFlightDirection((byte) rand.nextInt(8));
	}

	protected void collisionUpdate() {
		updateFlightDirectionOnCollision();
		setNewWayPoint();
		//destroy some items bumped into
		blockBump();
	}

	protected void devourTamingItem() {
		if (getBatAction() < 3) {
			setBatAction((byte)4);
			Bats.batsList.remove(this);
			for (int i = 0; i < 7; i++) {
				double d = rand.nextGaussian() * 0.02D;
				double d1 = rand.nextGaussian() * 0.02D;
				double d2 = rand.nextGaussian() * 0.02D;
				worldObj.spawnParticle("heart", (posX + rand.nextFloat() * width * 2.0F) - width, posY + 0.5D + rand.nextFloat() * height, (posZ + rand.nextFloat() * width * 2.0F) - width, d, d1, d2);
			}
			setHealth(getMaxHealth());
			EntityPlayer ep = worldObj.getClosestPlayerToEntity(this, 20D);
			if (ep != null) {
				ArrayList<BBEntityBat> list = new ArrayList<BBEntityBat>();
				if (Bats.assistants.containsKey(ep.getCommandSenderName())) {
					list = Bats.assistants.get(ep.getCommandSenderName());
				}
				list.add(this);
				Bats.assistants.put(ep.getCommandSenderName(), list);
			}
		}
	}

	@Override
	protected void dropFewItems(boolean par1, int par2) {
		int j = rand.nextInt(32) - 29;
		for (int k = 0; k < j; k++) {
			this.entityDropItem(new ItemStack(Items.dye, 1, 15), -0.05F);
		}
	}

	//if bat in water, it will drown fast
	protected void drown() {
		if (getAir() > 20) {
			setAir(5);
			motionX = 0D;
			motionY = -0.63D;
			motionZ = 0D;
		}
		motionX += rand.nextFloat() / 25F - 0.02F;
		motionY += rand.nextFloat() / 50F - 0.01F;
		motionZ += rand.nextFloat() / 25F - 0.02F;
	}

	@Override
	protected void fall(float f) {
	}

	protected boolean findCeilingBlock() {
		if (!worldObj.isDaytime() && rand.nextInt(getSleepNightSpawnRate()) > 0)
			return false;
		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(boundingBox.minY);
		int k = MathHelper.floor_double(posZ);
		for (int w = 0; w < 10; w++) {
			if (isCeilingBlock(worldObj.getBlock(i, j + w, k)) && !isInvalidTravelBlock(i, j + w - 1, k) && worldObj.getBlockLightValue(i, j + w - 1, k) <= maxCeilingLight()) {
				setPosition(i + 0.5D, j + w - getScale(), k + 0.5D);
				if (!worldObj.checkNoEntityCollision(boundingBox)) {
					setPosition(posX, posY - w + getScale(), posZ);
					return false;
				}
				return true;
			}
		}
		return false;
	}

	protected Entity findTarget() {
		double d1 = 9999D;
		EntityLiving entityliving = null;
		List<?> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(10, 10, 10));
		for (int i = 0; i < list.size(); i++) {
			Entity entity1 = (Entity) list.get(i);
			if (!(entity1 instanceof EntityLiving) || entity1 == ridingEntity || entity1 == riddenByEntity) {
				continue;
			}
			double d2 = entity1.getDistanceSq(posX, posY, posZ);
			if ((d2 < d1) && ((EntityLiving) entity1).canEntityBeSeen(this) && isValidTarget((EntityLiving) entity1)) {
				d1 = d2;
				entityliving = (EntityLiving) entity1;
			}
		}
		return entityliving;
	}

	protected void flingTarget() {
		attackTarget.motionY += 0.17D;
		attackTarget.motionX *= 1.18D;
		attackTarget.motionZ *= 1.18D;
	}

	protected byte getAttackDelay() {
		return getBatAction() > 2 ? 50 : (byte) (120 - worldObj.difficultySetting.ordinal() * 20);
	}

	protected float getAttackDistance() {
		return 2.2F;
	}

	protected float getBaseWakeChance() {
		return getBatAction() == 3 ? 0.00001F : 0.0001F;
	}

	protected int getBatDamage() {
		return getBatAction() < 3 ? worldObj.difficultySetting.ordinal() : 2;
	}

	/**
	 * @return the additional x and z velocity that is added to entities that
	 *         collide with the bat.
	 */
	protected double getBonusVelocity() {
		return (rand.nextInt(8 + worldObj.difficultySetting.ordinal() * 3) + 8 + worldObj.difficultySetting.ordinal());
	}

	/**
	 * @return the additional y velocity that is added to entities that collide
	 *         with the bat.
	 */
	protected double getBonusVelocity2() {
		return 0.016D + worldObj.difficultySetting.ordinal() * 0.04D;
	}

	/**
	 * @return float above 1.0F that partially determines the chance of waking
	 *         up
	 */
	protected float getBrightnessWakeChance() {
		return getBrightness(1.0F) <= maxCeilingLight() ? 1.0F : getBrightness(1.0F) * 36F;
	}

	@Override
	protected String getDeathSound() {
		return "ogresean:bat.death";
	}

	/**
	 * @return a value that determines how often the bat drops manure. higher =
	 *         less often
	 */
	protected int getDropFreq() {
		return 10000;
	}

	/**
	 * @return the string representing the echo sound, used for active bats.
	 */
	protected String getEchoSound() {
		return "ogresean:bat.echo";
	}

	//path weight for flight
	protected float getFlightPathWeight(int i, int j, int k) {
		//in rain bats fly without direction
		if (isWet())
			return rand.nextFloat();
		//tamed bats fly close to player
		if (getBatAction() == 4) {
			EntityPlayer player = worldObj.getClosestPlayerToEntity(this, 20);
			if (player != null) {
				double dist = getDistanceToEntity(player);
				boolean flag = player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == Items.feather; //bats fly closer when holding feather
				if (!flag && dist > 12D || flag && dist > 3.0D)
					return (float) (!isInvalidTravelBlock(i, j, k) ? -player.getDistance(i, j, k) : -100F);
			} else {
				return rand.nextFloat();
			}
		}
		//if looking for spot to roost
		else if (getBatAction() == 5 && isCeilingBlock(worldObj.getBlock(i, j + 1, k)) && !isInvalidTravelBlock(i, j, k) && worldObj.getBlockLightValue(i, j, k) <= maxCeilingLight())
			return 200F;
		//wild bats have chance of aiming for loose blocks
		else if (getBatAction() < 3 && isLooseBlock(worldObj.getBlock(i, j, k))) //wild bats have chance of aiming for loose blocks
			return -4F + worldObj.difficultySetting.ordinal() * 3F;
		else if (isInvalidTravelBlock(i, j, k)) //other non-air blocks ignored
			return -100F;
		else if (isFavoredTravelBlock(i, j, k))
			return 100F;
		float xDif = (float) (posX - i); // north/south
		float zDif = (float) (posZ - k); // east/west
		switch (getBatDirection()) {
		case 0: //NorthEast
			return ((xDif + zDif) / 2F);
		case 1: //East
			return (zDif);
		case 2: //SouthEast
			return -((xDif - zDif) / 2F);
		case 3: //South
			return -(xDif);
		case 4: //SouthWest
			return -((xDif + zDif) / 2F);
		case 5: //West
			return -(zDif);
		case 6: //NorthWest
			return ((xDif - zDif) / 2F);
		case 7: //North
			return (xDif);
		default:
			return 0.0F;
		}
	}

	@Override
	protected String getHurtSound() {
		return "ogresean:bat.hurt";
	}

	@Override
	protected String getLivingSound() {
		return "ogresean:bat.living";
	}

	protected float getPlayerProximityWakeChance(float dist) {
		return dist > 10F ? 1.0F : (12F - dist) * 2;
	}

	/**
	 * @return number above 0; determines chance of sleeping bats spawning in
	 *         night time; lower = more common
	 */
	protected int getSleepNightSpawnRate() {
		return 32;
	}

	/**
	 * @return number above 0; determines chance of active bats spawning in day
	 *         time; lower = more common
	 */
	protected int getWanderDaySpawnRate() {
		return 32;
	}

	protected double getWaypointDistance() {
		return getBatAction() == 5 ? 0.4D : 1.4D;
	}

	protected double getXZFlight() {
		return 0.028D - (getMaxHealth() - getHealth()) * 0.003D;
	}

	protected double getXZFlightAttackBoost() {
		return 0.018D;
	}

	protected double getXZMaxFlight() {
		return 1.25D - (getMaxHealth() - getHealth()) * 0.15D;
	}

	protected double getXZMaxFlightAttackBoost() {
		return 1.5D;
	}

	protected double getYFlight() {
		return 0.12D - (getMaxHealth() - getHealth()) * 0.01D;
	}

	protected double getYFlightAttackBoost() {
		return 0.06D;
	}

	protected double getYMaxFlight() {
		return 1.5D - (getMaxHealth() - getHealth()) * 0.15D;
	}

	protected double getYMaxFlightAttackBoost() {
		return 1.0D;
	}

	protected void handleEchos() {
		if (rand.nextFloat() < (isWet() ? 0.02F : 0.005F)) {
			worldObj.playSoundAtEntity(this, getEchoSound(), getSoundVolume() * 1.2F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
		}
	}

	/**
	 * This method makes a bat occasionally drop bonemeal, representing bat
	 * manure.
	 */
	protected void handleManure() {
		batTimer++;
		if (batTimer == getDropFreq()) {
			this.entityDropItem(new ItemStack(Items.dye, 1, 15), -0.05F);
			batTimer = 0;
		}
	}

	/**
	 * This method makes a sleeping bat wake up if the block it is perched from
	 * is missing.
	 */
	protected void handleMissingCeiling() {
		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(boundingBox.minY);
		int k = MathHelper.floor_double(posZ);
		if ((getBatAction() == 0 || getBatAction() == 3) && !isCeilingBlock(worldObj.getBlock(i, j + 1, k)))
			wakeUp();
	}

	protected void handleNoClip() {
		if (tempClip > 0)
			tempClip--;
		else if (tempClip == 0) {
			tempClip--;
			noClip = false;
		}
		//simulate motion movement in advance
		AxisAlignedBB ab = boundingBox.addCoord(motionX, motionY, motionZ);
		//check to see if bat will be within solid block or leaf block.
		boolean solidBlock = false;
		boolean leafBlock = false;
		boolean reverse = false;
		for (double d = ab.minX; d <= ab.maxX; d = ab.maxX - d < 1 && ab.maxX != d ? ab.maxX : d + 1.0D)
			for (double d1 = ab.minY; d1 <= ab.maxY; d1 = ab.maxY - d1 < 1 && ab.maxY != d1 ? ab.maxY : d1 + 1.0D)
				for (double d2 = ab.minZ; d2 <= ab.maxZ; d2 = ab.maxZ - d2 < 1 && ab.maxZ != d2 ? ab.maxZ : d2 + 1.0D) {
					Block bid = worldObj.getBlock(MathHelper.floor_double(d), MathHelper.floor_double(d1), MathHelper.floor_double(d2));
					if (bid == Blocks.leaves)
						leafBlock = true;
					else if (bid.getCollisionBoundingBoxFromPool(worldObj, MathHelper.floor_double(d), MathHelper.floor_double(d1), MathHelper.floor_double(d2)) != null)
						solidBlock = true;
				}
		//set or unset noclip based on what bat will collide into
		if (!noClip && leafBlock && !solidBlock)
			noClip = true;
		else if (noClip) {
			if (solidBlock)
				reverse = true;
			else if (!leafBlock && tempClip < 0)
				tempClip = 3;
			else if (leafBlock && tempClip > -1)
				tempClip = -1;
		}
		//if going to collide into solid block while in leaves reverse motion
		if (reverse) {
			reverseMotion();
			setNewWayPoint();
		}
	}

	protected void handlePerching() {
		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(boundingBox.minY);
		int k = MathHelper.floor_double(posZ);
		if (isCeilingBlock(worldObj.getBlock(i, j + 1, k)) && !isInvalidTravelBlock(i, j, k) && worldObj.getBlockLightValue(i, j, k) <= maxCeilingLight()) {
			setPosition(i + 0.5D, j + 1 - getScale(), k + 0.5D);
			boolean flag = true;
			List<?> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox);
			for (int c = 0; c < list.size(); c++) {
				Entity entity = (Entity) list.get(c);
				if (!entity.isDead && entity.preventEntitySpawning) {
					flag = false;
				}
			}
			if (!flag) {
				setPosition(posX, posY - 1 + getScale(), posZ);
			} else {
				setBatAction((byte)3);
				wakeDelay = 120;
				setSleepMotion();
			}
		}
	}

	protected void handlePlayerInteraction() {
		//handle player interactions
		EntityPlayer ep = worldObj.getClosestPlayerToEntity(this, 20);
		if (ep == null)
			return;
		if (getBatAction() < 3) { //frightened or wandering bat
			handleTaming(ep);
		}
		if (getBatAction() == 5) { //find perch
			handlePerching();
		}
		if (getBatAction() == 4) //teleport far away bats
			handleTeleport(ep);
	}

	protected void handleTaming(EntityPlayer ep) {
		float distance = ep.getDistanceToEntity(this);
		if (getBatAction() == 1 && distance > 20F && !this.canEntityBeSeen(ep))
			setDead();
		else if (distance < 12F && attackTarget == null) //bats not eating an item willing to be tamed
			checkForTamingItem();
	}

	protected void handleTeleport(EntityPlayer ep) {
		float distance = ep.getDistanceToEntity(this);
		if (distance > 20F && !this.canEntityBeSeen(ep)) {
			int i = MathHelper.floor_double(ep.posX) - 2;
			int j = MathHelper.floor_double(ep.posZ) - 2;
			int k = MathHelper.floor_double(ep.boundingBox.minY) + rand.nextInt(3);
			for (int l = 0; l <= 4; l++) {
				for (int i1 = 0; i1 <= 4; i1++) {
					if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && worldObj.isAirBlock(i + l, k, j + i1)) {
						setLocationAndAngles(i + l + 0.5F, k, j + i1 + 0.5F, rotationYaw, rotationPitch);
						return;
					}
				}
			}
		}
	}

	/**
	 * This method has a chance of making a sleeping bat wake up when the player
	 * is near by. The chance of waking up is based on how close the player is
	 * to the bat and how much light is shining on the bat.
	 */
	protected void handleWakeUp() {
		EntityPlayer ep = worldObj.getClosestPlayerToEntity(this, 20);
		if (wakeDelay == 0 && ep != null && canEntityBeSeen(ep)) {
			float wakeUpChance1 = getBrightnessWakeChance();
			float wakeUpChance2 = getPlayerProximityWakeChance(ep.getDistanceToEntity(this));
			if (rand.nextFloat() < (getBaseWakeChance() * wakeUpChance1 * wakeUpChance2))
				wakeUp();
		}
		handleMissingCeiling();
	}

	protected void handleWings() {
		winge = wingb;
		wingd = wingc;
		wingc = (float) (wingc + (onGround ? -1 : 0.5F) * 0.3D);
		if (wingc < 0.0F) {
			wingc = 0.0F;
		} else if (wingc > 1.0F) {
			wingc = 1.0F;
		}
		if (!onGround && wingh < 1.0F) {
			wingh = 1.0F;
		}
		wingh = (float) (wingh * 0.4D * (1.0F - (getMaxHealth() - getHealth() * 0.1F)));
		wingb += wingh * 2.0F;
	}

	protected void handleYMotion() {
		if (onGround)
			motionY += 0.4D;
		if (motionY < 0.0D) {
			motionY *= 0.7D;
		}
	}

	protected boolean isCeilingBlock(Block bid) {
		return bid.isOpaqueCube();
	}

	protected boolean isFavoredTravelBlock(int i, int j, int k) {
		return false;
	}

	protected boolean isInvalidTravelBlock(int i, int j, int k) {
		return !(worldObj.getBlock(i, j, k) == Blocks.air || worldObj.getBlock(i, j, k) == Blocks.leaves);
	}

	protected boolean isLooseBlock(Block bid) {
		return bid == Blocks.torch || bid == Blocks.wheat || bid == Blocks.redstone_torch || bid == Blocks.lit_redstone_lamp || bid == Blocks.sapling
				|| bid == Blocks.reeds || bid == Blocks.red_flower || bid == Blocks.yellow_flower || bid == Blocks.brown_mushroom || bid == Blocks.red_mushroom;
	}

	protected boolean isTamingItemID(ItemStack stack) {
		return stack!=null && stack.getItem() == Items.slime_ball;
	}

	protected boolean isValidTarget(EntityLivingBase el) {
		return (getBatAction() < 3 && el instanceof EntityPlayer)
				|| (getBatAction() > 2 && (el instanceof BBEntityBat && ((BBEntityBat) el).attackTarget instanceof EntityPlayer) || (el instanceof EntityCreature && ((EntityCreature) el)
						.getEntityToAttack() instanceof EntityPlayer));
	}

	protected float maxCeilingLight() {
		return getBatAction() > 2 ? 6F : 4F;
	}

	protected float maxWanderLight() {
		return 4F;
	}

	protected void reverseMotion() {
		motionX = -motionX * 0.90D + rand.nextFloat() / 20F - 0.025F;
		motionY = -motionY * 0.90D + rand.nextFloat() / 40F - 0.0125F;
		motionZ = -motionZ * 0.90D + rand.nextFloat() / 20F - 0.025F;
	}

	protected float rotationUpdate(float f, float f1, float f2) {
		float f3 = f1;
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

	/**
	 * Makes a bat fall straight down to the ground when it is killed.
	 */
	protected void setDyingVelocity() {
        motionY = 0D;
        motionX = -0.4D;
        motionZ = 0D;
	}

	protected void setNewWayPoint() {
		boolean flag = false;
		wayPoints = new int[3];
		float f2 = -100F;
		for (int i1 = 0; i1 < 10; i1++) {
			int j1 = MathHelper.floor_double((posX + rand.nextInt(13)) - 6D);
			int k1 = MathHelper.floor_double((posY + rand.nextInt(7)) - 3D);
			int l1 = MathHelper.floor_double((posZ + rand.nextInt(13)) - 6D);
			float f3 = getFlightPathWeight(j1, k1, l1);
			if (f3 > f2) {
				f2 = f3;
				wayPoints[0] = j1;
				wayPoints[1] = k1;
				wayPoints[2] = l1;
				flag = true;
			}
		}
		if (!flag) {
			wayPoints = null;
			updateFlightDirectionOnCollision();
		}
	}

	/**
	 * makes a sleeping bat entirely still.
	 */
	protected void setSleepMotion() {
		motionY = 0D;
		motionX = 0D;
		motionZ = 0D;
		rotationYaw = 0F;
		prevRotationYaw = 0F;
		rotationPitch = 0F;
		prevRotationPitch = 0F;
	}

	/**
	 * This method makes the sleeping bat recover 1 hp every 30 seconds, until
	 * at maximum health.
	 */
	protected void sleepHealthUpdate() {
		if (ticksExisted % 600 == 0) {
			if (getHealth() < getMaxHealth()) {
				setHealth(getHealth() + 1);
			}
		}
	}

	/**
	 * actions taken by sleeping upside down bats
	 */
	protected void sleepingUpdate() {
		setSleepMotion();
		sleepHealthUpdate();
		handleManure();
		if (wakeDelay > 0)
			wakeDelay--;
		handleWakeUp();
	}

	protected boolean spawnAsWanderingBat() {
		if (worldObj.isDaytime() && rand.nextInt(getWanderDaySpawnRate()) > 0)
			return false;
		int i = MathHelper.floor_double(posX);
		int j = MathHelper.floor_double(boundingBox.minY);
		int k = MathHelper.floor_double(posZ);
		if (worldObj.getBlockLightValue(i, j, k) <= maxWanderLight()) {
			setBatAction((byte)2);
			return true;
		}
		return false;
	}

	//makes bat fly toward waypoint coordinates
	protected void travelToWaypoint() {
		faceLocation(wayPoints[0], wayPoints[1], wayPoints[2], 30F - (getMaxHealth() - getHealth()) * 3F);
		double yFlight = getYFlight();
		double xzFlight = getXZFlight();
		double yMaxSpeed = getYMaxFlight();
		double xzMaxSpeed = getXZMaxFlight();
		if (attackTarget != null && attackDelay == 0) { //speed up when attacking
			yFlight += getYFlightAttackBoost();
			xzFlight += getXZFlightAttackBoost();
			yMaxSpeed += getYMaxFlightAttackBoost();
			xzMaxSpeed += getXZMaxFlightAttackBoost();
		}
		//slow down bat if near player and player holding feather
		if (getBatAction() > 2) {
			EntityPlayer player = worldObj.getClosestPlayerToEntity(this, 6);
			if (player != null && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == Items.feather) {
				yMaxSpeed = 0.3D;
				xzMaxSpeed = 0.1D;
			}
		}
		if (MathHelper.floor_double(posY) < wayPoints[1]) {
			motionY += yFlight;
		}
		double d = wayPoints[0] - posX;
		//if(motionX < 0.0D && d > 0.0D || motionX > 0.0D && d < 0.0D) motionX = 0.0D; //sharper turns
		motionX += d > 0.0D ? xzFlight : -xzFlight;
		double d1 = wayPoints[2] - posZ;
		//if(motionZ < 0.0D && d > 0.0D || motionZ > 0.0D && d < 0.0D) motionZ = 0.0D; //sharper turns
		motionZ += d1 > 0.0D ? xzFlight : -xzFlight;
		//maximum speeds
		if (motionX > xzMaxSpeed)
			motionX = xzMaxSpeed;
		else if (motionX < -xzMaxSpeed)
			motionX = -xzMaxSpeed;
		if (motionZ > xzMaxSpeed)
			motionZ = xzMaxSpeed;
		else if (motionZ < -xzMaxSpeed)
			motionZ = -xzMaxSpeed;
		if (motionY > yMaxSpeed)
			motionY = yMaxSpeed;
		else if (motionY < -yMaxSpeed)
			motionY = -yMaxSpeed;
	}

	//changes flight direction slightly on collision
	protected void updateFlightDirectionOnCollision() {
		setFlightDirection((byte) (rand.nextBoolean() ? getBatDirection() - 1 : getBatDirection() + 1));
		if (getBatDirection() < 0)
			setFlightDirection((byte)7);
		else if (getBatDirection() > 7)
			setFlightDirection((byte)0);
	}

	protected void wakeUpNearbyBats(double range) {
		BBEntityBat bat = null;
		int batCount = 0;
		List<?> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(range, range, range));
		for (int i = 0; i < list.size(); i++) {
			Entity entity1 = (Entity) list.get(i);
			if (!(entity1 instanceof BBEntityBat) || entity1 == this.ridingEntity || entity1 == this.riddenByEntity) {
				continue;
			}
			bat = ((BBEntityBat) entity1);
			if (bat.getBatAction() == 0 || bat.getBatAction() == 3) {
				bat.wakeUp();
				batCount++;
			}
		}
		if (batCount > 4)
			worldObj.playSoundAtEntity(this, "ogresean:bat.wakeMany", getSoundVolume() * 1.4F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
	}

	protected boolean willAttack() {
		return getBatAction() > 2 ? rand.nextInt(20) == 0 : (worldObj.difficultySetting.ordinal() * 3 * worldObj.difficultySetting.ordinal()) + rand.nextInt(100) > 99;
	}

	/**
	 * @return true if the bat has become aggressive after an attack
	 */
	protected boolean willBecomeAggressive(Entity entity) {
		return worldObj.difficultySetting.ordinal() * 4 + rand.nextInt(16) > 16;
	}
}
