package ogresean;

import net.minecraft.client.renderer.entity.RenderCreeper;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CreeperSwarm {
	public static boolean enableCustomSpeed = true;
	public static boolean enableCustomHealth = true;
	public static boolean enableCustomExplosionTime = true;
	public static int easyMax = 6;
	public static int normalMax = 8;
	public static int hardMax = 10;
	public static final String NAME = "CreeperSwarm";

	public static void load(boolean isClient, Object mod) {
		EntityRegistry.registerModEntity(EntityCreeperSwarm.class, "CreeperSwarm", 0, mod, 80, 3, true);
		EntityRegistry.addSpawn(EntityCreeperSwarm.class, 5, 1, 1, EnumCreatureType.monster, WorldType.base11Biomes);
		if (isClient) {
			addRenderer();
		}
	}

	public static void preLoad(Configuration config) {
		enableCustomSpeed = config.get(NAME, "Enable custom speed", enableCustomSpeed).getBoolean(true);
		enableCustomHealth = config.get(NAME, "Enable custom health", enableCustomHealth).getBoolean(true);
		enableCustomExplosionTime = config.get(NAME, "Enable custom explosion time", enableCustomExplosionTime).getBoolean(true);
		easyMax = config.get(NAME, "Max spawn in easy mode", easyMax).getInt();
		normalMax = config.get(NAME, "Max spawn in normal mode", normalMax).getInt();
		hardMax = config.get(NAME, "Max spawn in hard mode", hardMax).getInt();
	}

	@SideOnly(Side.CLIENT)
	private static void addRenderer() {
		RenderingRegistry.registerEntityRenderingHandler(EntityCreeperSwarm.class, new RenderCreeper());
	}
}
