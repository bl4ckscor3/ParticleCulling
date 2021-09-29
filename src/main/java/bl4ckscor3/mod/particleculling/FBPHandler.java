package bl4ckscor3.mod.particleculling;

import com.TominoCZ.FBP.particle.FBPParticleBlock;

import net.minecraft.client.particle.Particle;

public class FBPHandler
{
	public static boolean isBlockPlaceParticle(Particle particle)
	{
		return particle instanceof FBPParticleBlock;
	}
}
