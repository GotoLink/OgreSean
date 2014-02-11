package ogresean.talkingpig;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class PigGuiHandler implements IGuiHandler {
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == 0) {
			Entity pig = world.getEntityByID(x);
            if (pig instanceof EntityTalkingPig && ((EntityTalkingPig)pig).getOwnerName().equals(player.getCommandSenderName())) {
                return new GUITalkingPig((EntityTalkingPig)pig);
            }
		}
		return null;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}
}
