package bl4ckscor3.mod.particleculling;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Ignore;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid=ParticleCulling.MODID, name="Particle Culling", version="v1.3", clientSideOnly=true)
@EventBusSubscriber(modid=ParticleCulling.MODID)
public class ParticleCulling
{
	public static final String MODID = "particleculling";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static Class<?> particleClass = null;

	static {
		try
		{
			particleClass = Class.forName("net.minecraft.client.particle.Particle");
		}
		catch(ClassNotFoundException e)
		{
			LOGGER.error("Could not find vanilla particle class! Something is wrong.");
		}
	}

	@Config(modid=MODID)
	public static class Configuration
	{
		@Comment("Set this to false if you do not want to cull particles that technically are in the player's view but are obstructed by blocks.")
		public static boolean cullBehindBlocks = true;

		@Comment("Set this to true if you don't want particles to be rendered behind glass and other transparent blocks. This does nothing if \"cullBehindBlocks\" is turned off.")
		public static boolean cullBehindGlass = false;

		@Comment("Set this to false to disable particle culling while being in spectator mode. This is useful to take screenshots without having particles removed.")
		public static boolean cullInSpectator = true;

		@Comment("The minimum amount of blocks behind which particles start to get culled. Only effective if \"cullBehindBlocks\" is turned on.")
		@RangeInt(min=1, max=50)
		public static int blockBuffer = 1;

		@Comment("Set this to false to disable all of particle culling's features.")
		public static boolean cullingEnabled = true;

		@Comment({"Add particle classes here that should be ignored by Particle Culling.",
		"Example: To ignore Minecraft's breaking particles and any derivates, add \"net.minecraft.client.particle.ParticleBreaking\" to the list"})
		public static String[] ignoredParticles = {
				"org.orecruncher.dsurround.client.fx.particle.mote.ParticleCollection",
				"com.TominoCZ.FBP.particle.FBPParticleBlock",
				"xzeroair.trinkets.client.particles.ParticleGreed",
		};

		@Ignore
		public static List<Class<?>> ignoredParticleClasses;
	}

	@EventHandler
	public void onLoadComplete(FMLLoadCompleteEvent event)
	{
		updateIgnoredParticles();
	}

	@SubscribeEvent
	public static void onConfigChanged(OnConfigChangedEvent event)
	{
		if(event.getModID().equals(MODID))
		{
			ConfigManager.sync(MODID, Config.Type.INSTANCE);
			updateIgnoredParticles();
		}
	}

	private static void updateIgnoredParticles()
	{
		Configuration.ignoredParticleClasses = new ArrayList<>();

		for(String className : Configuration.ignoredParticles)
		{
			try
			{
				Class<?> clazz = Class.forName(className);

				if(particleClass.isAssignableFrom(clazz)) //returns true if the vanilla particle class is equal to or a super class of clazz
					Configuration.ignoredParticleClasses.add(clazz);
				else
					LOGGER.warn(className + " is not a particle class or does not extend the vanilla particle class!");

			}
			catch(ClassNotFoundException e)
			{
				LOGGER.warn("Could not find particle class " + className + ". If the mod is not installed, this can be ignored.");
			}
		}
	}
}
