package bl4ckscor3.mod.particleculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import bl4ckscor3.mod.particleculling.CullCheck;
import net.minecraft.client.particle.Particle;

@Mixin(Particle.class)
public class MixinParticle implements CullCheck {
	@Unique
	private boolean culled = true; //set to true by default so particles that should be culled but have not been checked yet don't flicker on the screen for a short moment

	@Override
	public void setCulled(boolean culled) {
		this.culled = culled;
	}

	@Override
	public boolean isCulled() {
		return culled;
	}
}
