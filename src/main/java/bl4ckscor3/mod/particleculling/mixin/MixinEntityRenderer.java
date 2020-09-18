package bl4ckscor3.mod.particleculling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import bl4ckscor3.mod.particleculling.CameraHolder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.culling.ICamera;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer implements CameraHolder
{
	private ICamera camera;

	@ModifyVariable(method="renderWorldPass", at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/culling/ICamera;setPosition(DDD)V"))
	private ICamera setCamera(ICamera camera)
	{
		this.camera = camera;
		return camera;
	}

	@Override
	public ICamera getCamera()
	{
		return camera;
	}
}
