package bl4ckscor3.mod.particleculling;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid=ParticleCulling.MODID, name="Particle Culling", version="1.1")
@EventBusSubscriber(modid=ParticleCulling.MODID)
public class ParticleCulling
{
	public static final String MODID = "particleculling";

	@Config(modid=MODID)
	public static class Configuration
	{
		@Comment("Set this to false if you do not want to cull particles that technically are in the player's view but are obstructed by blocks.")
		public static boolean cullBehindBlocks = true;

		@Comment("Set this to true if you don't want particles to be rendered behind glass and other transparent blocks. This does nothing if \"cullBehindBlocks\" is turned off.")
		public static boolean cullBehindGlass = false;

		@Comment("Set this to false to disable particle culling while being in spectator mode. This is useful to take screenshots without having particles removed.")
		public static boolean cullInSpectator = true;
	}

	@SubscribeEvent
	public static void onConfigChanged(OnConfigChangedEvent event)
	{
		if(event.getModID().equals(MODID))
			ConfigManager.sync(MODID, Config.Type.INSTANCE);
	}
}
