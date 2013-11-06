package ogresean.talkingpig;

import java.util.List;
import java.util.Random;

import net.minecraft.client.model.ModelPig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TalkingPig implements IPlayerTracker {
	public void load(boolean isClient, Object mod) {
		EntityRegistry.registerModEntity(EntityTalkingPig.class, "TalkingPig", 6, mod, 80, 3, true);
		GameRegistry.registerPlayerTracker(this);
		MinecraftForge.EVENT_BUS.register(this);
		if (isClient) {
			addRenderer(mod);
		}
	}

	@ForgeSubscribe
	public void onPlayerAttack(LivingHurtEvent event) {
		if (event.entityLiving instanceof EntityAnimal) {
			Entity source = event.source.getEntity();
			if (source instanceof EntityPlayer && !source.getEntityData().getBoolean("TalkingPigDead")) {
				List<EntityTalkingPig> entities = source.worldObj.getEntitiesWithinAABB(EntityTalkingPig.class, source.boundingBox.expand(10.0, 3.0, 10.0));
				for (EntityTalkingPig pig : entities) {
					if (pig.owner.username == ((EntityPlayer) source).username) {
						String s;
						Random rand = new Random();
						if (event.entityLiving instanceof EntityPig)
							s = rand.nextBoolean() ? "$P$! We pigs have feelings! Don't hurt him!" : "$P$! How could you!? He's my kind!";
						else
							s = rand.nextBoolean() ? "Leave that poor $A$ alone, $P$!" : "$P$, don't abuse that $A$!";
						((EntityPlayer) source).addChatMessage("§a<".concat(pig.getEntityName()).concat("> §e")
								.concat(s.replace("$P$", pig.getPlayerName().replace("$A$", EntityList.getEntityString(event.entityLiving)))));
						break;
					}
				}
			}
		}
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {
	}

	@Override
	public void onPlayerLogin(EntityPlayer player) {
		//if new world, spawn pig if not already dead
		if (!player.getEntityData().getBoolean("TalkingPigDead")) {
			//Spawn new Talking pig
			EntityTalkingPig etp = new EntityTalkingPig(player.worldObj);
			etp.setPosition(player.posX, player.posY, player.posZ);
			etp.owner = player;
			if (player.getEntityData().hasKey("TalkingPigName")) {
				etp.setCustomNameTag(player.getEntityData().getString("TalkingPigName"));
			} else {
				player.getEntityData().setString("TalkingPigName", etp.getEntityName());
			}
			if (!player.worldObj.isRemote) {
				player.worldObj.spawnEntityInWorld(etp);
			}
			player.addChatMessage("§a<".concat(etp.getEntityName()).concat("> §e").concat(("Hi $P$! My name is ").replace("$P$", etp.getPlayerName()))
					.concat(etp.getEntityName().concat(" the pig. Nice to meet you!")));
		}
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {
		if (!player.getEntityData().getBoolean("TalkingPigDead")) {
			for (Object pig : player.worldObj.getEntitiesWithinAABB(EntityTalkingPig.class, player.boundingBox.expand(30, 30, 30))) {
				if (((EntityTalkingPig) pig).owner.username.equals(player.username)) {
					((EntityTalkingPig) pig).setDead();
				}
			}
		}
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
	}

	@SideOnly(Side.CLIENT)
	public static void addRenderer(Object mod) {
		RenderingRegistry.registerEntityRenderingHandler(EntityTalkingPig.class, new RenderTalkingPig(new ModelPig(), new ModelPig(0.5F), 0.7F));
		NetworkRegistry.instance().registerGuiHandler(mod, new PigGuiHandler());
	}
}
