package bl4ckscor3.mod.particleculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bl4ckscor3.mod.particleculling.CameraHolder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal implements CameraHolder
{
	@Unique
	private ICamera camera;

	@Inject(method="setupTerrain", at=@At("HEAD"))
	private void setCamera(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator, CallbackInfo callback)
	{
		this.camera = camera;
	}

	@Override
	public ICamera getCamera()
	{
		return camera;
	}
}
