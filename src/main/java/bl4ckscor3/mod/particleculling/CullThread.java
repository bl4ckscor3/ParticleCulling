package bl4ckscor3.mod.particleculling;

import bl4ckscor3.mod.particleculling.ParticleCulling.Configuration;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CullThread extends Thread {
	private double sleepOverhead = 0.0D;

	public CullThread() {
		setName("Particle Culling");
		setDaemon(true);
	}

	@Override
	public void run() {
		Minecraft mc = Minecraft.getMinecraft();

		while (!Thread.currentThread().isInterrupted()) {
			try {
				long start = System.nanoTime();

				if (mc.world != null) {
					for (int i = 0; i < 4; i++) {
						for (int j = 0; j < 2; j++) {
							for (Particle particle : mc.effectRenderer.fxLayers[i][j]) {
								((CullCheck) particle).setCulled(shouldCullParticle(particle, mc));
							}
						}
					}
				}

				double d = (System.nanoTime() - start) / 1_000_000.0D + sleepOverhead;
				long sleepTime = 10 - (long) d;

				sleepOverhead = d % 1.0D;

				if (sleepTime > 0)
					Thread.sleep(sleepTime);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			catch (Exception e) {} //TODO: actually fix the CME in the fxLayers loop
		}
	}

	private boolean shouldCullParticle(Particle particle, Minecraft mc) {
		if (!Configuration.cullingEnabled || !Configuration.cullInSpectator && Minecraft.getMinecraft().player.isSpectator())
			return false;

		ICamera camera = ((CameraHolder) Minecraft.getMinecraft().renderGlobal).getCamera();

		if (camera == null)
			return false;

		for (Class<?> ignoredParticleClass : Configuration.ignoredParticleClasses) {
			if (ignoredParticleClass.isAssignableFrom(particle.getClass())) //returns true if ignoredParticleClass is equal to or a super class of the current particle's class
				return false;
		}

		if (camera.isBoundingBoxInFrustum(particle.getBoundingBox())) {
			if (Configuration.cullBehindBlocks) {
				Entity entity = mc.getRenderViewEntity();

				if (entity != null) {
					if (Configuration.blockBuffer == 1) {
						RayTraceResult result = entity.world.rayTraceBlocks(new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ), new Vec3d(particle.posX, particle.posY, particle.posZ), false, true, true);

						if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
							if (Configuration.cullBehindGlass)
								return true;

							IBlockState state = entity.world.getBlockState(result.getBlockPos());

							if (state.isFullCube() && state.isOpaqueCube())
								return true;
						}
					}
					else if (shouldCull(entity.world, new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ), new Vec3d(particle.posX, particle.posY, particle.posZ)))
						return true;
				}
			}

			return false;
		}

		return true;
	}

	//adapted from World#rayTraceBlocks to be able to ray trace through blocks
	private boolean shouldCull(World world, Vec3d from, Vec3d to) {
		if (!Double.isNaN(from.x) && !Double.isNaN(from.y) && !Double.isNaN(from.z) && !Double.isNaN(to.x) && !Double.isNaN(to.y) && !Double.isNaN(to.z)) {
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

			if (state.getCollisionBoundingBox(world, pos) != Block.NULL_AABB && block.canCollideCheck(state, false) && state.collisionRayTrace(world, pos, from, to) != null) {
				blocks++;
				opacityCheck = opacityCheck || Configuration.cullBehindGlass || (state.isFullCube() && state.isOpaqueCube());
			}

			int maxIterations = 50;

			while (maxIterations-- >= 0) {
				if (checkX == toX && checkY == toY && checkZ == toZ)
					return opacityCheck && (++blocks > Configuration.blockBuffer);

				boolean wasXChanged = true;
				boolean wasYChanged = true;
				boolean wasZChanged = true;
				double d0 = 999.0D;
				double d1 = 999.0D;
				double d2 = 999.0D;

				if (toX > checkX)
					d0 = checkX + 1.0D;
				else if (toX < checkX)
					d0 = checkX + 0.0D;
				else
					wasXChanged = false;

				if (toY > checkY)
					d1 = checkY + 1.0D;
				else if (toY < checkY)
					d1 = checkY + 0.0D;
				else
					wasYChanged = false;

				if (toZ > checkZ)
					d2 = checkZ + 1.0D;
				else if (toZ < checkZ)
					d2 = checkZ + 0.0D;
				else
					wasZChanged = false;

				double d3 = 999.0D;
				double d4 = 999.0D;
				double d5 = 999.0D;
				double d6 = to.x - from.x;
				double d7 = to.y - from.y;
				double d8 = to.z - from.z;

				if (wasXChanged)
					d3 = (d0 - from.x) / d6;

				if (wasYChanged)
					d4 = (d1 - from.y) / d7;

				if (wasZChanged)
					d5 = (d2 - from.z) / d8;

				if (d3 == -0.0D)
					d3 = -1.0E-4D;

				if (d4 == -0.0D)
					d4 = -1.0E-4D;

				if (d5 == -0.0D)
					d5 = -1.0E-4D;

				EnumFacing facing;

				if (d3 < d4 && d3 < d5) {
					facing = toX > checkX ? EnumFacing.WEST : EnumFacing.EAST;
					from.x = d0;
					from.y += d7 * d3;
					from.z += d8 * d3;
				}
				else if (d4 < d5) {
					facing = toY > checkY ? EnumFacing.DOWN : EnumFacing.UP;
					from.x += d6 * d4;
					from.y = d1;
					from.z += d8 * d4;
				}
				else {
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

				if (state.getMaterial() == Material.PORTAL || state.getCollisionBoundingBox(world, pos) != Block.NULL_AABB && block.canCollideCheck(state, false) && state.collisionRayTrace(world, pos, from, to) != null) {
					opacityCheck = opacityCheck || Configuration.cullBehindGlass || (state.isFullCube() && state.isOpaqueCube());

					if (++blocks > Configuration.blockBuffer)
						return opacityCheck;
				}
			}

			return opacityCheck && blocks > Configuration.blockBuffer;
		}

		return false;
	}
}
