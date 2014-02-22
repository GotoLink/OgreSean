package ogresean.talkingpig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ogresean.OgreSeanMods;
//To DO: Add Gui functionality
//TO DO: Add pig gathering functionality;
//TO DO: Implement experience
//TO DO: Fix the bugs relating to status

public class EntityTalkingPig extends EntityPig {
	private int levelMessageTimer;
	private String levelMessage;
	public int phraseTimer;
	public int recoverTimer;
	public int hunger;
	public byte status;
	/*
	 * Command flags: bit 1: followMode bit 2: aggressiveMode bit 3: gatherMode
	 */
	public byte commands;
	//stats
	public byte skillLevel; //influences mushroom finding rate
	public byte skillPoints; //distributed based on pig's actions
	public int experiencePoints; //gained via your talking pig fighting
	public byte strength, speed, endurance, followers;
	protected ArrayList<EntityPig> pals;
	private EntityPlayer owner;
	public float moveSpeed;
	static final HashMap<Item, Integer> edibleFoods = new HashMap<Item, Integer>();
	static final public String names[] = new String[] { "Porky", "Gordo", "Oinkers", "Cuddles", "Pinky", "Snuggles", "Rex", "Snipper", "Molly", "Bubbles", "Nanders", "Pugsy", "Pickles", "Thunder",
			"Piggy", "Blossom", "Sneers", "LePig", "Stompers", "Nameless", "Doug", "Earl", "Leena", "Guppers", "Martheon the Third", "Slushy", "Puckers" };
	static final public String hurtPhrases[] = new String[] { "Ouch! Cut that out, you ugly $T$!", "Hey! Stop that $T$!", "Ugh! Leave me alone $T$!", "Get away from me, $T$!",
			"You are so annoying, $T$!", "Stop hurting me, $T$!", "Grr! You're asking for it, $T$!", "Don't make me hurt you, $T$!", "$T$s can be so frustrating!",
			"$T$, calm down and leave me alone!" };
	static final public String idlePhrases[] = new String[] { "I feel great today! How about you, $P$?", "Hey $P$, let's explore!", "Do you think there are any mushrooms nearby?",
			"So $P$, where are we?", "Kind of boring right now.", "I hope we see some action soon!", "Oink Oink!", "Whatcha doing, $P$?", "I love jumping around!", "We need more excitement!",
			"I love eating mushrooms!", "What do you want to do today, $P$?", "Nature is lovely!", "I love being able to talk!", "What do you like to eat, $P$", "I wonder why the sky is blue....",
			"Yawn...", "Do you like puns, $P$?  I sure do!", "Do I annoy you, $P$? Go ahead, be honest.", "How high can you jump, $P$?", "You sure can hold a lot of stuff, $P$!",
			"I sure would like to see a forest fire!", "Cows are better than Sheep.  There I said it!", "I wonder what chickens taste like?", "Do you know the muffin man, $P$?",
			"I wish I had a top hat. Sigh...", "Have you ever killed a pig, $P$?  I sure hope not." };
	static final public String attackPhrases[] = new String[] { "Prepare to feel the pain, $T$!", "Take this, $T$!", "Fear my pig power! Give up, $T$!", "You can't beat me, $T$!",
			"Ha! I'm invincible, $T$!", "You're doomed, $T$!", "My pal and I can win this fight easily, $T$!", "Heh, nice try $T$!", "Time to lose, $T$!", "Hide while you can, $T$!",
			"Learn your place, $T$!", "Silly $T$! You have no chance!", "Foolish $T$! I can't lose!", "Pig versus $T$! Let's see who wins!", "Another $T$ is about to bite the dust!" };

	public EntityTalkingPig(World world) {
		super(world);
		setHealth(6);
		setCustomNameTag(names[rand.nextInt(names.length)]);
		phraseTimer = 200;
		recoverTimer = -1;
		skillLevel = 1;
		skillPoints = 0;
		experiencePoints = 35;
		status = 0;
		commands = 1;
		strength = (byte) (1 + rand.nextInt(3));
		speed = (byte) (1 + rand.nextInt(3));
		endurance = (byte) (1 + rand.nextInt(3));
		followers = 1;
		//moveSpeed determined by speed stat
		moveSpeed = 0.7F + (speed) / 10F;
		//hunger starts slightly lower based on endurance
		hunger = -3000 - (600 * endurance);
		pals = new ArrayList<EntityPig>();
		levelMessageTimer = 0;
	}

	static {
		edibleFoods.put(Items.feather, 260);
		edibleFoods.put(Items.wheat_seeds, 380);
		edibleFoods.put(Item.getItemFromBlock(Blocks.red_flower), 420);
		edibleFoods.put(Item.getItemFromBlock(Blocks.yellow_flower), 420);
		edibleFoods.put(Item.getItemFromBlock(Blocks.pumpkin), 460);
		edibleFoods.put(Items.bone, 480);
		edibleFoods.put(Items.reeds, 560);
		edibleFoods.put(Items.sugar, 700);
		edibleFoods.put(Items.slime_ball, 740);
		edibleFoods.put(Items.wheat, 1000);
		edibleFoods.put(Items.egg, 1200);
		edibleFoods.put(Items.cookie, 1550);
		edibleFoods.put(Items.fish, 1800);
		edibleFoods.put(Items.cooked_fished, 2600);
		edibleFoods.put(Items.bread, 3600);
		edibleFoods.put(Items.apple, 3900);
		edibleFoods.put(Item.getItemFromBlock(Blocks.brown_mushroom), 4200);
		edibleFoods.put(Item.getItemFromBlock(Blocks.red_mushroom), 4200);
		edibleFoods.put(Items.cake, 7200);
		edibleFoods.put(Items.golden_apple, 8800);
		edibleFoods.put(Items.bowl, 9600);
	}

	@Override
	//spouts a hurt phrase when hit; unable to take damage except from player
	public boolean attackEntityFrom(DamageSource d, float i) {
		Entity entity = d.getSourceOfDamage();
		phraseTimer = 300;
		float dist = distToPlayer();
		if (hurtTime == 0 && getHealth() > 0 && entity != null && dist > -1 && dist < 18) {
			if (rand.nextFloat() < 0.2F || entity instanceof EntityPlayer)
				sayHurt(entity);
			if (entityToAttack == null && !(entity instanceof EntityPlayer))
				entityToAttack = entity;
		}
		if (!(entity instanceof EntityPlayer)) {
			return super.attackEntityFrom(d, 0);
		} else {
			return super.attackEntityFrom(d, 1);
		}
	}

	public String getLevelMessage() {
		return levelMessage;
	}

	public int getLevelTimer() {
		return levelMessageTimer;
	}

	public String getOwnerName() {
		return this.dataWatcher.getWatchableObjectString(18);
	}

	@Override
	public boolean interact(EntityPlayer entityplayer) {
		ItemStack itemstack = entityplayer.inventory.getCurrentItem();
		if (itemstack != null && (itemstack.getItem() != Items.saddle) && (itemstack.getItem() != Items.stick)) {
			phraseTimer = 250;
			String str = "";
			if (itemstack.getItem() == Items.golden_apple) {
				experiencePoints = (skillLevel * (75 + skillLevel * 25)) + 1;
				skillLevel++;
				skillPoints++;
				levelUP("Level Up!");
				//unlock gatherMode when skillLevel at 8
				if ((commands & 4) != 4 && skillLevel > 7) {
					commands |= 4;
					levelUP("Level Up! Pig allies unlocked!");
				}
			}
			if (hunger > -6000) {
				boolean flag = edibleFoods.containsKey(itemstack.getItem());
				//
				if (flag == true) {
					hunger -= edibleFoods.get(itemstack.getItem());
					entityplayer.inventory.decrStackSize(entityplayer.inventory.currentItem, 1);
					if (hunger > 3600)
						str = "Thanks. But, I'm still hungry...";
					else
						str = "Yum! That was satisfying.";
					if (hunger >= 12000)
						status = 3;
					else if (hunger >= 7200)
						status = 2;
					else if (hunger >= 3600)
						status = 1;
					else
						status = 0;
				} else
					str = "I can't eat that!";
			} else
				str = "I couldn't eat another bite. I'm stuffed.";
			entityplayer.addChatComponentMessage(new ChatComponentText("�a<".concat(getCustomNameTag()).concat("> �e").concat(str)));
			return true;
		}//else if stick, show stat screen
		else if (itemstack != null && itemstack.getItem() == Items.stick && entityplayer.getCommandSenderName().equals(getOwnerName())) {
			entityplayer.openGui(OgreSeanMods.instance, 0, worldObj, this.getEntityId(), 0, 0);
			return true;
		} else
			return super.interact(entityplayer);
	}

	public void levelUP(String s) {
		levelMessage = s;
		levelMessageTimer = 60;
	}

	@Override
	//overriden to allow moveSpeeds above 1.0F
	public void moveFlying(float f, float f1, float f2) {
		if (moveSpeed <= 1.0F)
			super.moveFlying(f, f1, f2);
		else
			super.moveFlying(f, f1, f2 * moveSpeed);
	}

	@Override
	//only dies by death from player
	public void onDeath(DamageSource d) {
		Entity entity = d.getSourceOfDamage();
		if (entity instanceof EntityPlayer) {
			((EntityPlayer) entity).addChatComponentMessage(new ChatComponentText("�3You killed ".concat(getCustomNameTag()).concat(". Enjoy your pork, you heartless monster.")));
			this.dropItem(Items.porkchop, 4);
			this.dropItem(Items.bone, 3);
		}
		if (owner != null) {
			owner.getEntityData().setBoolean("TalkingPigDead", true);
		}
		super.onDeath(d);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		levelingEffect();
		float dist = distToPlayer();
		if (dist == -55)
			return;
		if (entityToAttack != null && entityToAttack != owner && getDistanceToEntity(entityToAttack) > 17D)
			entityToAttack = null;
		hunger++;
		if ((commands & 4) == 4)
			handlePals(); //handle pig pals
		//occasionally subtract hunger equal to endurance
		if (rand.nextFloat() < 0.01)
			hunger -= endurance * 5;
		if (hunger >= 3600 && hunger < 7200 && dist < 24 && status < 1) { //mildly hungry after 3 minutes
			owner.addChatComponentMessage(new ChatComponentText("�a<".concat(getCustomNameTag()).concat("> �e").concat("I'm hungry. Feed me some food!")));
			phraseTimer = 300;
			status = 1;
		} else if (hunger >= 7200 && hunger < 12000 && dist < 24 && status < 2) { //very hungry after 6 minutes
			owner.addChatComponentMessage(new ChatComponentText("�a<".concat(getCustomNameTag()).concat("> �e").concat("I'm starving! Feed me some food already!")));
			phraseTimer = 300;
			status = 2;
		} else if (hunger >= 12000 && hunger < 13200 && dist < 24 && status < 3) { //extremely hungry after 10 minutes
			owner.addChatComponentMessage(new ChatComponentText("�a<".concat(getCustomNameTag()).concat("> �e").concat("...cough....so....hungry...need....food....")));
			moveSpeed = 0.1F;
			phraseTimer = 300;
			status = 3;
		} else if (hunger >= 13200 && dist < 24) { //dead after 11 minutes
			owner.addChatComponentMessage(new ChatComponentText("�3You let ".concat(getCustomNameTag()).concat(" starve to death. Such horrible neglect...")));
			setHealth(0);
		}
		//if had been hurt by player, gradually recover if healthy
		if (recoverTimer > 0)
			recoverTimer--;
		else if (recoverTimer == 0) {
			setHealth(6);
			recoverTimer = -1;
			if (moveSpeed == 0.2F || moveSpeed == 0.5F)
				moveSpeed = 0.7F + (speed) / 10F;
		} else if (hunger < 13200 && moveSpeed != 0.7F + (speed) / 10F)
			moveSpeed = 0.7F + (speed) / 10F;
		//every 10 - 30 seconds, pig spouts a random phrase if not weak
		if (phraseTimer > 0 && getHealth() > 2 && dist < 16)
			phraseTimer--;
		else if (phraseTimer == 0) {
			phraseTimer = 200 + rand.nextInt(401);
			if (hunger > 12000)
				owner.addChatComponentMessage(new ChatComponentText("�a<".concat(getCustomNameTag()).concat("> �e").concat("So hungry....")));
			else if (rand.nextFloat() < (0.15F + (skillLevel) / 50F)) //skillLevel influences mushroom finding rater
				findMushroom();
			else
				sayIdlePhrase();
			//attract enemies after talking
			attractEnemies();
		}
		//if weak, pig cries occasionally
		if (getHealth() <= 2 && rand.nextFloat() < 0.3F) {
			float f1 = 0.01645278F;
			double d1 = (float) posX - Math.sin(rotationYaw * f1) / 3D;
			double d3 = (float) posY + 0.4F;
			double d5 = (float) posZ + Math.cos(rotationYaw * f1) / 3D;
			worldObj.spawnParticle("splash", d1, d3 + 0.49D, d5, 0.0D, 0.0D, 0.0D);
		}
		//pig follows player if not attacking an enemy (followMode)
		if (entityToAttack == null && dist > 5 && (commands & 1) == 1)
			entityToAttack = owner;
		else if (entityToAttack instanceof EntityPlayer && dist <= 4)
			entityToAttack = null;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		setOwner(nbttagcompound.getString("Owner"));
		phraseTimer = nbttagcompound.getInteger("PhraseTimer");
		recoverTimer = nbttagcompound.getInteger("RecoverTimer");
		hunger = nbttagcompound.getInteger("Hunger");
		//get command modes
		commands = nbttagcompound.getByte("Commands");
		//get stats
		status = nbttagcompound.getByte("Status");
		experiencePoints = nbttagcompound.getInteger("ExperiencePoints");
		skillLevel = nbttagcompound.getByte("SkillLevel");
		skillPoints = nbttagcompound.getByte("SkillPoints");
		strength = nbttagcompound.getByte("Strength");
		speed = nbttagcompound.getByte("Speed");
		moveSpeed = nbttagcompound.getFloat("MoveSpeed");
		endurance = nbttagcompound.getByte("Endurance");
		followers = nbttagcompound.getByte("Followers");
	}

	public void setOwner(String user) {
		this.dataWatcher.updateObject(18, user);
		owner = this.worldObj.getPlayerEntityByName(user);
	}

	//pig is ridable
	@Override
	public void updateEntityActionState() {
		if (!worldObj.isRemote) {
			if (riddenByEntity instanceof EntityLivingBase) {
				moveForward = 0.0F;
				moveStrafing = 0.0F;
				riddenByEntity.fallDistance = 0.0F;
				isJumping = false;
				fallDistance = 0.0F;
				prevRotationYaw = rotationYaw = riddenByEntity.rotationYaw;
				prevRotationPitch = rotationPitch = riddenByEntity.rotationPitch;
				EntityLiving entityliving = (EntityLiving) riddenByEntity;
				float f = 3.141593F;
				float f1 = f / 180F;
				float ms = !onGround ? 0.04F : 0.2F + speed * 0.01F;
				float ms2 = 0.3F + speed * 0.025F;
				ms = inWater ? 0.06F : ms;
				ms2 = inWater ? ms2 / 2F : ms2;
				if (entityliving.moveForward > 0.1F) {
					float f2 = entityliving.rotationYaw * f1;
					motionX += entityliving.moveForward * -Math.sin(f2) * ms;
					motionZ += entityliving.moveForward * Math.cos(f2) * ms;
				} else if (entityliving.moveForward < -0.1F) {
					float f3 = entityliving.rotationYaw * f1;
					motionX += entityliving.moveForward * -Math.sin(f3) * ms;
					motionZ += entityliving.moveForward * Math.cos(f3) * ms;
				}
				if (entityliving.moveStrafing > 0.1F) {
					float f4 = entityliving.rotationYaw * f1;
					motionX += entityliving.moveStrafing * Math.cos(f4) * ms;
					motionZ += entityliving.moveStrafing * Math.sin(f4) * ms;
				} else if (entityliving.moveStrafing < -0.1F) {
					float f5 = entityliving.rotationYaw * f1;
					motionX += entityliving.moveStrafing * Math.cos(f5) * ms;
					motionZ += entityliving.moveStrafing * Math.sin(f5) * ms;
				}
				Field jump = EntityLivingBase.class.getDeclaredFields()[41];
				try {
					if (Boolean.class.cast(jump.get(entityliving)).booleanValue()) {
						if (onGround) {
							onGround = false;
							jump();
						} else if (handleWaterMovement()) {
							motionY = 0.4D;
						}
					}
				} catch (ReflectiveOperationException e) {
				}
				double d = Math.abs(Math.sqrt(motionX * motionX + motionZ * motionZ));
				if (d > ms2) {
					double d1 = ms2 / d;
					motionX = motionX * d1;
					motionZ = motionZ * d1;
				}
				return;
			} else {
				super.updateEntityActionState();
				return;
			}
		} else {
			return;
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setString("Owner", getOwnerName());
		nbttagcompound.setInteger("PhraseTimer", phraseTimer);
		nbttagcompound.setInteger("RecoverTimer", recoverTimer);
		nbttagcompound.setInteger("Hunger", hunger);
		//save command modes
		nbttagcompound.setByte("Commands", commands);
		//save stats
		nbttagcompound.setByte("Status", status);
		nbttagcompound.setInteger("ExperiencePoints", experiencePoints);
		nbttagcompound.setByte("SkillLevel", skillLevel);
		nbttagcompound.setByte("SkillPoints", skillPoints);
		nbttagcompound.setByte("Strength", strength);
		nbttagcompound.setByte("Speed", speed);
		nbttagcompound.setFloat("MoveSpeed", moveSpeed);
		nbttagcompound.setByte("Endurance", endurance);
		nbttagcompound.setByte("Followers", followers);
	}

	@Override
	//pig fights enemies
	protected void attackEntity(Entity entity, float f) {
		if (f < 1.8D && hunger < 12000 && entity.boundingBox.maxY > boundingBox.minY && entity.boundingBox.minY < boundingBox.maxY && attackTime == 0) {
			float dist = distToPlayer();
			if (dist > -1 && dist < 18 && rand.nextFloat() < 0.2F)
				sayAttackPhrase();
			attackTime = 20;
			entity.attackEntityFrom(DamageSource.causeMobDamage(this), 1 + rand.nextInt(strength)); //damage influenced by strength
			//strength affects knockback
			entity.motionX *= 0.9 + ((strength) / 10F);
			entity.motionZ *= 0.9 + ((strength) / 10F);
			entity.motionY *= 0.8 + ((strength) / 10F);
			//endurance affects hunger gained
			hunger += 30 - endurance;
			//gain experience
			gainExp(entity);
		}
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(18, "buddy");
	}

	@Override
	//if pig is aggressive, it seeks out enemies
	protected Entity findPlayerToAttack() {
		if ((commands & 2) != 2)
			return null;
		List<?> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(14D, 14D, 14D));
		double dist = 9999;
		Entity target = null;
		for (int i = 0; i < list.size(); i++) {
			Entity entity = (Entity) list.get(i);
			if (!(entity instanceof EntityMob)) {
				continue;
			}
			EntityMob entitymob = (EntityMob) entity;
			double dist2 = entitymob.getDistanceToEntity(this);
			if (canEntityBeSeen(entitymob) && dist2 < dist) {
				dist = dist2;
				target = entity;
			}
		}
		return target;
	}

	protected void gainExp(Entity entity) {
		if (entity instanceof EntityLiving) {
			EntityLiving el = (EntityLiving) entity;
			experiencePoints += el.getHealth();
			//gain additional experience for victories
			if (el.getHealth() < 1)
				try {
					experiencePoints += 10 + 2 * ((EntityLiving) (entity.getClass().getConstructor(new Class[] { World.class }).newInstance(new Object[] { worldObj }))).getMaxHealth();
				} catch (Exception e) {
					experiencePoints += 50;
				}
			//if experience high enough, level up
			if (experiencePoints > (skillLevel * (75 + skillLevel * 25))) {
				skillLevel++;
				skillPoints++;
				levelUP("Level Up!");
				//unlock gatherMode when skillLevel at 8
				if ((commands & 4) != 4 && skillLevel > 7) {
					commands |= 4;
					levelUP("Level Up! Pig allies unlocked!");
				}
			}
		}
	}

	protected void handlePals() {
		//gather nearby pigs
		if (pals.size() < followers) {
			double d = 16D;
			List<?> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(d, d, d));
			for (int i = 0; i < list.size(); i++) {
				Entity entity1 = (Entity) list.get(i);
				if (!(entity1 instanceof EntityPig) || entity1 == ridingEntity || entity1 == riddenByEntity) {
					continue;
				}
				if (this.canEntityBeSeen(entity1) && !pals.contains(entity1)) {
					pals.add((EntityPig) entity1);
				}
			}
		}
		//order pig pals
		for (int i = 0; i < pals.size(); i++) {
			EntityPig es = pals.get(i);
			//remove dead animals from herd
			if (es == null || es.isDead) {
				pals.remove(i);
				i--;
				continue;
			}
			//manage animal movement
			float f = es.getDistanceToEntity(this);
			//follow talking pig if talking pig not attacking anything
			if (entityToAttack == null || entityToAttack instanceof EntityPlayer) {
				if (f < 6F && es.getEntityToAttack() == this) //wander if close
					es.setTarget(null);
				else if (f > 6F && es.getEntityToAttack() != this) //follow if far
					es.setTarget(this);
				else if (f > 16F) { //remove from pal list if too far
					es.setTarget(null);
					pals.remove(i);
					i--;
					continue;
				}
			}
			//else make pigs attack target
			else {
				es.setTarget(entityToAttack);
				if (es.getDistanceToEntity(entityToAttack) < 1.8D && es.boundingBox.maxY > entityToAttack.boundingBox.minY && es.boundingBox.minY < entityToAttack.boundingBox.maxY
						&& es.attackTime == 0) {
					es.attackTime = 20;
					entityToAttack.attackEntityFrom(DamageSource.causeMobDamage(es), 1); //make pal damage enemy
				}
			}
		}
	}

	@Override
	//sometimes jumps higher, based on speed
	protected void jump() {
		motionY = 0.42D;
		if (rand.nextFloat() < 0.3F || riddenByEntity instanceof EntityLiving)
			motionY += (speed) / 25F;
		isAirBorne = true;
	}

	private void attractEnemies() {
		List<?> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(10D, 10D, 10D));
		for (int i = 0; i < list.size(); i++) {
			Entity entity = (Entity) list.get(i);
			if (!(entity instanceof EntityMob)) {
				continue;
			}
			EntityMob entitymob = (EntityMob) entity;
			if (canEntityBeSeen(entitymob)) {
				entitymob.setTarget(this);
			}
		}
	}

	private float distToPlayer() {
		if (owner != null)
			return this.getDistanceToEntity(owner);
		else
			return -55;
	}

	private void findMushroom() {
		boolean flag = false;
		Block bid = rand.nextFloat() < 0.5F ? Blocks.brown_mushroom : Blocks.red_mushroom;
		int j = -1;
		int k = -1;
		int l = -1;
		for (int i1 = 0; i1 < 10; i1++) {
			j = MathHelper.floor_double((posX + rand.nextInt(13)) - 6D);
			k = MathHelper.floor_double((posY + rand.nextInt(7)) - 3D);
			l = MathHelper.floor_double((posZ + rand.nextInt(13)) - 6D);
			if (bid.canBlockStay(worldObj, j, k, l) && worldObj.isAirBlock(j, k, l)) {
				flag = true;
				break;
			}
		}
		if (flag) {
			worldObj.setBlock(j, k, l, rand.nextFloat() < 0.5 ? Blocks.brown_mushroom : Blocks.red_mushroom, 0, 2);
			owner.addChatComponentMessage(new ChatComponentText("�a<".concat(getCustomNameTag()).concat("> �e").concat("I smell a mushroom close by!")));
		}
	}

	private void levelingEffect() {
		if (levelMessageTimer <= 0)
			return;
		levelMessageTimer--;
		float f0 = rand.nextFloat() - rand.nextFloat();
		float f1 = rand.nextFloat() - rand.nextFloat();
		float f2 = rand.nextFloat() - rand.nextFloat();
		worldObj.spawnParticle("reddust", posX + f0, posY + f1, posZ + f2, rand.nextDouble(), rand.nextDouble(), rand.nextDouble());
		if (levelMessageTimer <= 1)
			levelMessage = null;
	}

	private void sayAttackPhrase() {
		owner.addChatComponentMessage(new ChatComponentText("�a<".concat(getCustomNameTag()).concat("> �e").concat(attackPhrases[rand.nextInt(attackPhrases.length)]).replace("$T$", EntityList.getEntityString(entityToAttack))));
	}

	private void sayHurt(Entity entity) {
		String str = "";
		if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).getCommandSenderName().equals(getOwnerName())) {
			if (getHealth() > 2 && hunger < 12000) {
				str = hurtPhrases[rand.nextInt(hurtPhrases.length)].replace("$T$", EntityList.getEntityString(entity));
			} else
				str = "....";
		} else {
			switch ((int) getHealth()) {
			case 6:
				str = "Ouch! Watch where you're aiming, $P$!";
				recoverTimer = 160;
				break;
			case 5:
				str = "Oof! Why are you hurting me?";
				recoverTimer = 160;
				break;
			case 4:
				str = "Cut that out! We're friends!";
				recoverTimer = 160;
				break;
			case 3:
				str = "Please...stop...$P$...";
				recoverTimer = 640;
				moveSpeed = 0.5F;
				break;
			case 2:
				str = "I thought...";
				recoverTimer = 640;
				moveSpeed = 0.2F;
				break;
			case 1:
				str = "...we were friends...Ugh...";
				break;
			}
		}
		owner.addChatComponentMessage(new ChatComponentText("�a<".concat(getCustomNameTag()).concat("> �e").concat(str.replace("$P$", getOwnerName()))));
	}

	private void sayIdlePhrase() {
		owner.addChatComponentMessage(new ChatComponentText("�a<".concat(getCustomNameTag()).concat("> �e").concat(idlePhrases[rand.nextInt(idlePhrases.length)]).replace("$P$", getOwnerName())));
	}
}
