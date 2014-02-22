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
        drawWorldBackground(1);
		drawCenteredString(fontRendererObj, "Talking Pig Current Stats", width / 2, height / 4 - 40, 0xffffff);
		namefield.drawTextBox();
		drawString(fontRendererObj, (new StringBuilder()).append("\2476Level          \247f").append(String.valueOf(pig.skillLevel)).toString(), (width / 2 - 100) + k, height / 4 + 28 + byte0, 0xff8d13);
		drawString(fontRendererObj, (new StringBuilder()).append("\2476Skill Points  \247f").append(String.valueOf(pig.skillPoints)).toString(), width / 2 + 2 + k, height / 4 + 28 + byte0, 0xff8d13);
		drawString(fontRendererObj, (new StringBuilder()).append("\2476Endurance    \247f").append(String.valueOf(pig.endurance)).toString(), (width / 2 - 100) + k, height / 4 + 43 + byte0, 0xff8d13);
		drawString(fontRendererObj, (new StringBuilder()).append("\2476Speed        \247f").append(String.valueOf(pig.speed)).toString(), width / 2 + 2 + k, height / 4 + 43 + byte0, 0xff8d13);
		drawString(fontRendererObj, (new StringBuilder()).append("\2476Strength      \247f").append(String.valueOf(pig.strength)).toString(), (width / 2 - 100) + k, height / 4 + 58 + byte0, 0xff8d13);
		if ((pig.commands & 4) == 4)
			drawString(fontRendererObj, (new StringBuilder()).append("\2476Max Allies   \247f").append(String.valueOf(pig.followers)).toString(), width / 2 + 2 + k, height / 4 + 58 + byte0, 0xff8d13);
		drawCenteredString(fontRendererObj, (new StringBuilder()).append("\2473Belly  \247f").append(pig.hunger > -6000 ? ((13200 - pig.hunger) / 192) : 100).append('%').toString(), width / 2 + 2 + k,
				height / 4 + 78 + byte0, 0xff8d13);
		super.drawScreen(i, j, f);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		buttonList.clear();
		byte byte0 = -16;
		buttonList.add(new GuiButton(1, width / 2 - 104, height / 4 + 108 + byte0, 100, 20, "BOOST STRENGTH"));
		buttonList.add(new GuiButton(2, width / 2 + 4, height / 4 + 108 + byte0, 100, 20, "BOOST SPEED"));
		buttonList.add(new GuiButton(3, width / 2 - 104, height / 4 + 128 + byte0, 100, 20, "BOOST ENDURANCE"));
		if ((pig.commands & 4) == 4)
			buttonList.add(new GuiButton(4, width / 2 + 4, height / 4 + 128 + byte0, 100, 20, "BOOST MAX ALLIES"));
		buttonList.add(new GuiButton(0, width / 2 - 104, height / 4 + 158 + byte0, 100, 20, "Done"));
		namefield = new GuiTextField(fontRendererObj, width / 2 - 100, height / 4 - 20, 200, 20);
		namefield.setText(pig.getCustomNameTag());
		namefield.setFocused(true);
		namefield.setMaxStringLength(31);
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void updateScreen() {
		namefield.updateCursorCounter();
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (!guibutton.enabled) {
			return;
		}
		if (guibutton.id == 0) {
			if (isSaving) {
				return;
			}
			isSaving = true;
			String s1 = namefield.getText();
			pig.setCustomNameTag(s1 == null || s1.equals("") ? "????" : s1);
			mc.displayGuiScreen(null);
		} else if (guibutton.id == 1 && pig.skillPoints > 0) {
			pig.strength++;
			pig.skillPoints--;
			//unlock aggressive mode when strength high enough
			if ((pig.commands & 2) != 2 && pig.strength > 4) {
				pig.commands |= 2;
				pig.levelUP("Strength increased! Aggressive mode activated!");
			} else
				pig.levelUP("Strength increased!");
		} else if (guibutton.id == 2 && pig.skillPoints > 0) {
			pig.speed++;
			pig.skillPoints--;
			pig.levelUP("Speed increased!");
			if (pig.recoverTimer == 0 && pig.hunger < 12000)
				pig.moveSpeed = 0.7F + (pig.speed) / 10F;
		} else if (guibutton.id == 3 && pig.skillPoints > 0) {
			pig.endurance++;
			pig.skillPoints--;
			pig.levelUP("Endurance increased!");
		} else if (guibutton.id == 4 && pig.skillPoints > 0) {
			pig.followers++;
			pig.skillPoints--;
			pig.levelUP("Followers increased!");
		}
	}

	@Override
	protected void keyTyped(char c, int i) {
		namefield.textboxKeyTyped(c, i);
		if (i == 1) {
			mc.displayGuiScreen(null);
			return;
		}
		if (c == '\r') {
            actionPerformed((GuiButton) buttonList.get(0));
		}
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		namefield.mouseClicked(i, j, k);
	}
}
