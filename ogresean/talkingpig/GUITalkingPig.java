package ogresean.talkingpig;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import org.lwjgl.input.Keyboard;

public class GUITalkingPig extends GuiScreen {
	private EntityTalkingPig pig;
	private GuiTextField namefield;
	private boolean isSaving;

	public GUITalkingPig(EntityTalkingPig piggy) {
		pig = piggy;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		byte byte0 = -16;
		int k = 0;
        func_146270_b(1);
		drawCenteredString(field_146289_q, "Talking Pig Current Stats", field_146294_l / 2, field_146295_m / 4 - 40, 0xffffff);
		namefield.func_146194_f();
		drawString(field_146289_q, (new StringBuilder()).append("\2476Level          \247f").append(String.valueOf(pig.skillLevel)).toString(), (field_146294_l / 2 - 100) + k, field_146295_m / 4 + 28 + byte0, 0xff8d13);
		drawString(field_146289_q, (new StringBuilder()).append("\2476Skill Points  \247f").append(String.valueOf(pig.skillPoints)).toString(), field_146294_l / 2 + 2 + k, field_146295_m / 4 + 28 + byte0, 0xff8d13);
		drawString(field_146289_q, (new StringBuilder()).append("\2476Endurance    \247f").append(String.valueOf(pig.endurance)).toString(), (field_146294_l / 2 - 100) + k, field_146295_m / 4 + 43 + byte0, 0xff8d13);
		drawString(field_146289_q, (new StringBuilder()).append("\2476Speed        \247f").append(String.valueOf(pig.speed)).toString(), field_146294_l / 2 + 2 + k, field_146295_m / 4 + 43 + byte0, 0xff8d13);
		drawString(field_146289_q, (new StringBuilder()).append("\2476Strength      \247f").append(String.valueOf(pig.strength)).toString(), (field_146294_l / 2 - 100) + k, field_146295_m / 4 + 58 + byte0, 0xff8d13);
		if ((pig.commands & 4) == 4)
			drawString(field_146289_q, (new StringBuilder()).append("\2476Max Allies   \247f").append(String.valueOf(pig.followers)).toString(), field_146294_l / 2 + 2 + k, field_146295_m / 4 + 58 + byte0, 0xff8d13);
		drawCenteredString(field_146289_q, (new StringBuilder()).append("\2473Belly  \247f").append(pig.hunger > -6000 ? ((13200 - pig.hunger) / 192) : 100).append('%').toString(), field_146294_l / 2 + 2 + k,
				field_146295_m / 4 + 78 + byte0, 0xff8d13);
		super.drawScreen(i, j, f);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		field_146292_n.clear();
		byte byte0 = -16;
		field_146292_n.add(new GuiButton(1, field_146294_l / 2 - 104, field_146295_m / 4 + 108 + byte0, 100, 20, "BOOST STRENGTH"));
		field_146292_n.add(new GuiButton(2, field_146294_l / 2 + 4, field_146295_m / 4 + 108 + byte0, 100, 20, "BOOST SPEED"));
		field_146292_n.add(new GuiButton(3, field_146294_l / 2 - 104, field_146295_m / 4 + 128 + byte0, 100, 20, "BOOST ENDURANCE"));
		if ((pig.commands & 4) == 4)
			field_146292_n.add(new GuiButton(4, field_146294_l / 2 + 4, field_146295_m / 4 + 128 + byte0, 100, 20, "BOOST MAX ALLIES"));
		field_146292_n.add(new GuiButton(0, field_146294_l / 2 - 104, field_146295_m / 4 + 158 + byte0, 100, 20, "Done"));
		namefield = new GuiTextField(field_146289_q, field_146294_l / 2 - 100, field_146295_m / 4 - 20, 200, 20);
		namefield.func_146180_a(pig.getCustomNameTag());
		namefield.func_146195_b(true);
		namefield.func_146203_f(31);
	}

	@Override
	public void func_146281_b() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void updateScreen() {
		namefield.func_146178_a();
	}

	@Override
	protected void func_146284_a(GuiButton guibutton) {
		if (!guibutton.field_146124_l) {
			return;
		}
		if (guibutton.field_146127_k == 0) {
			if (isSaving) {
				return;
			}
			isSaving = true;
			String s1 = namefield.func_146179_b();
			pig.setCustomNameTag(s1 == null || s1.equals("") ? "????" : s1);
			field_146297_k.func_147108_a(null);
		} else if (guibutton.field_146127_k == 1 && pig.skillPoints > 0) {
			pig.strength++;
			pig.skillPoints--;
			//unlock aggressive mode when strength high enough
			if ((pig.commands & 2) != 2 && pig.strength > 4) {
				pig.commands |= 2;
				pig.levelUP("Strength increased! Aggressive mode activated!");
			} else
				pig.levelUP("Strength increased!");
		} else if (guibutton.field_146127_k == 2 && pig.skillPoints > 0) {
			pig.speed++;
			pig.skillPoints--;
			pig.levelUP("Speed increased!");
			if (pig.recoverTimer == 0 && pig.hunger < 12000)
				pig.moveSpeed = 0.7F + (pig.speed) / 10F;
		} else if (guibutton.field_146127_k == 3 && pig.skillPoints > 0) {
			pig.endurance++;
			pig.skillPoints--;
			pig.levelUP("Endurance increased!");
		} else if (guibutton.field_146127_k == 4 && pig.skillPoints > 0) {
			pig.followers++;
			pig.skillPoints--;
			pig.levelUP("Followers increased!");
		}
	}

	@Override
	protected void keyTyped(char c, int i) {
		namefield.func_146201_a(c, i);
		if (i == 1) {
			field_146297_k.func_147108_a(null);
			return;
		}
		if (c == '\r') {
            func_146284_a((GuiButton) field_146292_n.get(0));
		}
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		namefield.func_146192_a(i, j, k);
	}
}
