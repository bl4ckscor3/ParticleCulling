//minecraft package to get around particle positions being protected without access transformers
package net.minecraft.client.particle;

import bl4ckscor3.mod.particleculling.CameraHolder;
import bl4ckscor3.mod.particleculling.ParticleCulling;
import bl4ckscor3.mod.particleculling.ParticleCulling.Configuration;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(modid=ParticleCulling.MODID, value=Side.CLIENT)
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
				if(Configuration.blockBuffer == 1)
				{
					RayTraceResult result = entity.world.rayTraceBlocks(new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ), new Vec3d(particle.posX, particle.posY, particle.posZ), false, true, true);

					if(result != null && result.typeOfHit == RayTraceResult.Type.BLOCK)
					{
						if(Configuration.cullBehindGlass)
							return;

						IBlockState state = entity.world.getBlockState(result.getBlockPos());

						if(state.isFullCube() && state.isOpaqueCube())
							return;
					}
				}
				else if(shouldCull(entity.world, new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ), new Vec3d(particle.posX, particle.posY, particle.posZ)))
					return;
			}

			particle.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
		}
	}

	//adapted from World#rayTraceBlocks to be able to ray trace through blocks
	public static boolean shouldCull(World world, Vec3d from, Vec3d to)
	{
		if(!Double.isNaN(from.x) && !Double.isNaN(from.y) && !Double.isNaN(from.z) && !Double.isNaN(to.x) && !Double.isNaN(to.y) && !Double.isNaN(to.z))
		{
			boolean opacityCheck = false;
			int blocks = 0;
			int toX = MathHelper.floor(to.x);
			int toY = MathHelper.floor(to.y);
			int toZ = MathHelper.floor(to.z);
			int checkX = MathHelper.floor(from.x);
			int checkY = MathHelper.floor(from.y);
			int checkZ = MathHelper.floor(from.z);
			MutableBlockPos pos = new MutableBlockPos(checkX, checkY, checkZ);
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();

			if(state.getCollisionBoundingBox(world, pos) != Block.NULL_AABB && block.canCollideCheck(state, false) && state.collisionRayTrace(world, pos, from, to) != null)
			{
				blocks++;
				opacityCheck = opacityCheck || Configuration.cullBehindGlass || (state.isFullCube() && state.isOpaqueCube());
			}

			int maxIterations = 50;

			while(maxIterations-- >= 0)
			{
				if(checkX == toX && checkY == toY && checkZ == toZ)
					return opacityCheck && (++blocks > Configuration.blockBuffer);

				boolean wasXChanged = true;
				boolean wasYChanged = true;
				boolean wasZChanged = true;
				double d0 = 999.0D;
				double d1 = 999.0D;
				double d2 = 999.0D;

				if(toX > checkX)
					d0 = checkX + 1.0D;
				else if(toX < checkX)
					d0 = checkX + 0.0D;
				else
					wasXChanged = false;

				if(toY > checkY)
					d1 = checkY + 1.0D;
				else if(toY < checkY)
					d1 = checkY + 0.0D;
				else
					wasYChanged = false;

				if(toZ > checkZ)
					d2 = checkZ + 1.0D;
				else if(toZ < checkZ)
					d2 = checkZ + 0.0D;
				else
					wasZChanged = false;

				double d3 = 999.0D;
				double d4 = 999.0D;
				double d5 = 999.0D;
				double d6 = to.x - from.x;
				double d7 = to.y - from.y;
				double d8 = to.z - from.z;

				if(wasXChanged)
					d3 = (d0 - from.x) / d6;

				if(wasYChanged)
					d4 = (d1 - from.y) / d7;

				if(wasZChanged)
					d5 = (d2 - from.z) / d8;

				if(d3 == -0.0D)
					d3 = -1.0E-4D;

				if(d4 == -0.0D)
					d4 = -1.0E-4D;

				if(d5 == -0.0D)
					d5 = -1.0E-4D;

				EnumFacing facing;

				if(d3 < d4 && d3 < d5)
				{
					facing = toX > checkX ? EnumFacing.WEST : EnumFacing.EAST;
					from.x = d0;
					from.y += d7 * d3;
					from.z += d8 * d3;
				}
				else if(d4 < d5)
				{
					facing = toY > checkY ? EnumFacing.DOWN : EnumFacing.UP;
					from.x += d6 * d4;
					from.y = d1;
					from.z += d8 * d4;
				}
				else
				{
					facing = toZ > checkZ ? EnumFacing.NORTH : EnumFacing.SOUTH;
					from.x += d6 * d5;
					from.y += d7 * d5;
					from.z = d2;
				}

				checkX = MathHelper.floor(from.x) - (facing == EnumFacing.EAST ? 1 : 0);
				checkY = MathHelper.floor(from.y) - (facing == EnumFacing.UP ? 1 : 0);
				checkZ = MathHelper.floor(from.z) - (facing == EnumFacing.SOUTH ? 1 : 0);
				pos.setPos(checkX, checkY, checkZ);
				state = world.getBlockState(pos);
				block = state.getBlock();

				if(state.getMaterial() == Material.PORTAL || state.getCollisionBoundingBox(world, pos) != Block.NULL_AABB && block.canCollideCheck(state, false) && state.collisionRayTrace(world, pos, from, to) != null)
				{
					opacityCheck = opacityCheck || Configuration.cullBehindGlass || (state.isFullCube() && state.isOpaqueCube());

					if(++blocks > Configuration.blockBuffer)
						return opacityCheck;
				}
			}

			return opacityCheck && blocks > Configuration.blockBuffer;
		}

		return false;
	}
}
