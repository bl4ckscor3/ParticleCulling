//minecraft package to get around particle positions being protected without access transformers
package net.minecraft.client.particle;

import bl4ckscor3.mod.particleculling.CameraHolder;
import bl4ckscor3.mod.particleculling.ParticleCulling.Configuration;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class CullHook
{
	public static void renderParticle(Particle particle, BufferBuilder buffer, Entity entity, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
	{
		if(!Configuration.cullInSpectator && Minecraft.getMinecraft().player.isSpectator())
		{
			particle.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
			return;
		}

		ICamera camera = ((CameraHolder)Minecraft.getMinecraft().entityRenderer).getCamera();

		if(camera == null)
		{
			double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
			double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
			double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

			camera = new Frustum();
			camera.setPosition(x, y, z);
		}

		if(camera.isBoundingBoxInFrustum(particle.getBoundingBox()))
		{
			if(Configuration.cullBehindBlocks)
			{
				RayTraceResult result = entity.world.rayTraceBlocks(entity.getPositionVector().add(0, entity.getEyeHeight(), 0), new Vec3d(particle.posX, particle.posY, particle.posZ), false, true, true);

				if(result != null && result.typeOfHit == RayTraceResult.Type.BLOCK)
				{
					IBlockState state = entity.world.getBlockState(result.getBlockPos());

					if(Configuration.cullBehindGlass || (state.isFullCube() && state.isOpaqueCube()))
						return;
				}
			}

			particle.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
		}
	}
}
