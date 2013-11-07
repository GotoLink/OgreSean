package ogresean.talkingpig;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class PigGuiHandler implements IGuiHandler {
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == 0) {
			List<EntityTalkingPig> entities = world.getEntitiesWithinAABB(EntityTalkingPig.class, player.boundingBox.expand(5.0, 5.0, 5.0));
			for (EntityTalkingPig pig : entities) {
				if (pig.getOwnerName().equals(player.getCommandSenderName())) {
					return new GUITalkingPig(pig);
				}
			}
		}
		return null;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}
}
