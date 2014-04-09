package ogresean.talkingpig;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.client.model.ModelPig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TalkingPig{
	public void load(boolean isClient, Object mod) {
		EntityRegistry.registerModEntity(EntityTalkingPig.class, "TalkingPig", 10, mod, 80, 3, true);
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		if (isClient) {
			addRenderer(mod);
		}
	}

	@SubscribeEvent
	public void onPlayerAttack(LivingHurtEvent event) {
		if (event.entityLiving instanceof EntityAnimal) {
			Entity source = event.source.getEntity();
			if (source instanceof EntityPlayer && !source.getEntityData().getBoolean("TalkingPigDead")) {
				List<EntityTalkingPig> entities = source.worldObj.getEntitiesWithinAABB(EntityTalkingPig.class, source.boundingBox.expand(10.0, 3.0, 10.0));
				for (EntityTalkingPig pig : entities) {
					if (pig.getOwnerName().equals(source.getCommandSenderName())) {
						String s;
						Random rand = new Random();
						if (event.entityLiving instanceof EntityPig)
							s = rand.nextBoolean() ? "$P$! We pigs have feelings! Don't hurt him!" : "$P$! How could you!? He's my kind!";
						else
							s = rand.nextBoolean() ? "Leave that poor $A$ alone, $P$!" : "$P$, don't abuse that $A$!";
						((EntityPlayer) source).addChatComponentMessage(new ChatComponentText("�a<".concat(pig.getCustomNameTag()).concat("> �e")
                                .concat(s.format(s, "$P$", pig.getOwnerName(), "$A$", EntityList.getEntityString(event.entityLiving)))));
						break;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		//spawn pig if not already dead
		if (!event.player.getEntityData().getBoolean("TalkingPigDead") && !event.player.getEntityData().hasKey("TalkingPigName")) {
			//Spawn new Talking pig
			EntityTalkingPig etp = new EntityTalkingPig(event.player.worldObj);
			etp.setPosition(event.player.posX, event.player.posY, event.player.posZ);
			etp.setOwner(event.player.getCommandSenderName());
            event.player.getEntityData().setString("TalkingPigName", etp.getCustomNameTag());
            event.player.addChatComponentMessage(new ChatComponentText("�a<".concat(etp.getCustomNameTag()).concat("> �e").concat("Hi ").concat(etp.getOwnerName()).concat("! My name is ")
					.concat(etp.getCustomNameTag().concat(" the pig. Nice to meet you!"))));
			if (!event.player.worldObj.isRemote) {
                event.player.worldObj.spawnEntityInWorld(etp);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void addRenderer(Object mod) {
		RenderingRegistry.registerEntityRenderingHandler(EntityTalkingPig.class, new RenderTalkingPig(new ModelPig(), new ModelPig(0.5F), 0.7F));
		NetworkRegistry.INSTANCE.registerGuiHandler(mod, new PigGuiHandler());
	}
}
