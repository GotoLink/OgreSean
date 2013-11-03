package ogresean.bats;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;

public class SoundHandler {
	@ForgeSubscribe
	public void onSoundLoad(SoundLoadEvent event) {
		event.manager.soundPoolSounds.addSound("ogresean:bat/batDeath.wav");
		for (int i = 1; i < 3; i++) {
			event.manager.soundPoolSounds.addSound("ogresean:bat/batEcho" + i + ".wav");
		}
		event.manager.soundPoolSounds.addSound("ogresean:bat/batHurt.wav");
		for (int i = 1; i < 7; i++) {
			event.manager.soundPoolSounds.addSound("ogresean:bat/batLiving" + i + ".wav");
		}
		event.manager.soundPoolSounds.addSound("ogresean:bat/batWakeMany.wav");
	}
}
