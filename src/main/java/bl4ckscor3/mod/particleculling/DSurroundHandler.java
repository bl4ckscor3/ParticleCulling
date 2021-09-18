//make position accessible without ATs
package bl4ckscor3.mod.particleculling;

import org.orecruncher.dsurround.client.fx.particle.mote.ParticleCollection;

import net.minecraft.client.particle.Particle;

public class DSurroundHandler
{
	public static boolean isParticleCollection(Particle particle)
	{
		return particle instanceof ParticleCollection;
	}
}
