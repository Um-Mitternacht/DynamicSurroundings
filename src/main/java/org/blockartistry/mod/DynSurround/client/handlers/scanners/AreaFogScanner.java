/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.mod.DynSurround.client.handlers.scanners;

import javax.annotation.Nonnull;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.handlers.AreaSurveyHandler;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.weather.WeatherProperties;
import org.blockartistry.mod.DynSurround.registry.BiomeInfo;
import org.blockartistry.mod.DynSurround.registry.BiomeRegistry;
import org.blockartistry.mod.DynSurround.registry.DimensionRegistry;
import org.blockartistry.mod.DynSurround.registry.RegistryManager;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.mod.DynSurround.util.Color;
import org.blockartistry.mod.DynSurround.util.MathStuff;

import com.google.common.base.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AreaFogScanner implements ITickable {

	private static final int HAZE_THRESHOLD = 15;
	private static final int RANGE = 10;
	private static final double AREA = (RANGE * 2 + 1) * (RANGE * 2 + 1);

	private static final Color OVERWORLD_FOG_COLOR = new Color(0.7529412F, 0.84705883F, 1.0F);
	private static final Color NETHER_FOG_COLOR = new Color(0.20000000298023224D, 0.029999999329447746D,
			0.029999999329447746D);
	private static final Color END_FOG_COLOR = new Color(0.627451F * 0.15F, 0.5019608F * 0.15F, 0.627451F * 0.15F);

	private final BiomeRegistry biomes = RegistryManager.get(RegistryType.BIOME);
	private final DimensionRegistry dimensions = RegistryManager.get(RegistryType.DIMENSION);
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	private BlockPos lastPos = BlockPos.ORIGIN;
	private int lastDim = 0;
	private float lastIntensity = 0;

	private float biomeWeight;
	private float weightDefault;
	private Color blendedColor;
	private float fogDensity;

	private float planeDistanceScale;

	// TODO: Do we need this to clear up fog for buildings?
	@SuppressWarnings("unused")
	private float insideFogOffset;

	public AreaFogScanner() {

	}

	private float calcHazeBand(final World world, final EntityPlayer player) {
		final float distance = MathHelper
				.abs(this.dimensions.getCloudHeight(world) - (float) (player.posY + player.getEyeHeight()));
		final float hazeBandRange = HAZE_THRESHOLD * (1.0F + WeatherProperties.getIntensityLevel() * 2);
		if (distance < hazeBandRange)
			return (hazeBandRange - distance) / hazeBandRange * ModOptions.elevationHazeFactor * 0.7F;

		return 0.0F;
	}

	private float calcHazeGradient(final World world, final EntityPlayer player) {
		final float factor = 1.0F + WeatherProperties.getIntensityLevel();
		final float skyHeight = this.dimensions.getSkyHeight(world) / factor;
		final float groundLevel = this.dimensions.getSeaLevel(world);
		final float ratio = (MathHelper.floor_double(player.posY + player.getEyeHeight()) - groundLevel)
				/ (skyHeight - groundLevel);
		return ratio * ratio * ratio * ratio * ModOptions.elevationHazeFactor;
	}

	@Nonnull
	private Color worldFogColor(@Nonnull final World world) {
		switch (world.provider.getDimension()) {
		case -1:
			return NETHER_FOG_COLOR;
		case 1:
			return END_FOG_COLOR;
		}
		return OVERWORLD_FOG_COLOR;
	}

	@Override
	public void update() {

		final BlockPos playerPos = EnvironState.getPlayerPosition();
		if (this.lastPos.equals(playerPos) && this.lastDim == EnvironState.getDimensionId()
				&& this.lastIntensity == WeatherProperties.getIntensityLevel())
			return;

		this.lastPos = playerPos;
		this.lastDim = EnvironState.getDimensionId();
		this.lastIntensity = WeatherProperties.getIntensityLevel();

		final World world = EnvironState.getWorld();
		final float rainStrength = world.getRainStrength(1.0F);
		final Color worldFogColor = worldFogColor(world);
		final Color scaledDefaultFogColor = Color.scale(worldFogColor, 1F - rainStrength);

		this.blendedColor = new Color(0, 0, 0);
		this.fogDensity = 0;
		this.biomeWeight = 0;

		float heightFog = 0;
		if (ModOptions.enableElevationHaze && this.dimensions.hasHaze(world)) {
			heightFog = ModOptions.elevationHazeAsBand ? calcHazeBand(world, EnvironState.getPlayer())
					: calcHazeGradient(world, EnvironState.getPlayer());
		}

		for (int dX = -RANGE; dX <= RANGE; dX++)
			for (int dZ = -RANGE; dZ <= RANGE; dZ++) {
				this.pos.setPos(playerPos.getX() + dX, playerPos.getY(), playerPos.getZ() + dZ);
				final BiomeInfo biome = this.biomes.get(world.getBiome(pos));
				final Color theColor;
				float fog = 0F;
				if (ModOptions.enableBiomeFog && biome.getHasFog()) {
					theColor = biome.getFogColor();
					fog = (biome.getFogDensity() * 0.4F + 0.5F) * ModOptions.biomeFogFactor;
				} else if (ModOptions.allowDesertFog && biome.getHasDust() && this.lastIntensity > 0) {
					theColor = Color.scale(biome.getDustColor(), rainStrength).add(scaledDefaultFogColor);
					fog = (float) (this.lastIntensity * 0.5F + 0.4F) * ModOptions.desertFogFactor * rainStrength;
				} else {
					theColor = worldFogColor;
				}

				this.fogDensity += 1 - Math.max(heightFog, fog);
				this.biomeWeight++;
				this.blendedColor.add(theColor);
			}

		this.weightDefault = (float) (AREA - this.biomeWeight);

		final float fpDistanceBiomeFogAvg = (this.biomeWeight == 0) ? 0 : this.fogDensity / this.biomeWeight;
		final float farPlaneDistanceScaleBiome = (0.1f * (1 - fpDistanceBiomeFogAvg) + 0.75f * fpDistanceBiomeFogAvg);
		this.planeDistanceScale = (float) ((farPlaneDistanceScaleBiome * this.biomeWeight + 0.75f * this.weightDefault)
				/ AREA);

		this.insideFogOffset = AreaSurveyHandler.getCeilingCoverageRatio() * 15.0F;

	}

	public float getPlaneDistance(final float eventDistance) {
		final float result = (float) ((this.fogDensity * 240 + eventDistance * this.weightDefault) / AREA);
		return Math.min(result, eventDistance);
	}

	public float getPlaneDistanceScale() {
		return this.planeDistanceScale;
	}

	@Nonnull
	public Color getFogColor(@Nonnull final World world, final float partialTick) {

		final Color defaultFogColor = new Color(world.getFogColor(partialTick));

		if (this.blendedColor == null || this.biomeWeight == 0)
			return defaultFogColor;

		final int dimId = world.provider.getDimension();

		// Default to NETHER/End scale factors
		double scaleRed = 1.0D;
		double scaleGreen = 1.0D;
		double scaleBlue = 1.0D;

		if (dimId != -1 && dimId != 0) {

			// Overworld and not nether

			// WorldProvider.getFogColor() - need to calculate the scale based
			// on sunlight and stuff.
			final float angle = world.getCelestialAngle(partialTick);
			final float base = MathHelper.clamp_float(MathStuff.cos(angle * MathStuff.PI_F * 2.0F) * 2.0F + 0.5F, 0, 1);

			scaleRed = base * 0.94F + 0.06F;
			scaleGreen = base * 0.94F + 0.06F;
			scaleBlue = base * 0.91F + 0.09F;

			// EntityRenderer.updateFogColor() - adjust the scale further
			// based on rain and thunder.
			final float intensity = world.getRainStrength(partialTick);
			if (intensity > 0) {
				final float s = 1F - intensity * 0.5F;
				scaleRed *= s;
				scaleGreen *= s;
				scaleBlue *= 1 - intensity * 0.4F;
			}

			final float strength = world.getThunderStrength(partialTick);
			if (strength > 0) {
				final float s = 1 - strength * 0.5F;
				scaleRed *= s;
				scaleGreen *= s;
				scaleBlue *= s;
			}
		}

		// Normalize the blended color components based on the biome weight.
		// The components contain a summation of all the fog components
		// in the area around the player.
		float r = (float) (this.blendedColor.red * scaleRed / this.biomeWeight);
		float g = (float) (this.blendedColor.green * scaleGreen / this.biomeWeight);
		float b = (float) (this.blendedColor.blue * scaleBlue / this.biomeWeight);

		// Mix the blended color with the existing fog color based on the
		// areas they occupy.
		r = (float) ((r * this.biomeWeight + defaultFogColor.red * this.weightDefault) / AREA);
		g = (float) ((g * this.biomeWeight + defaultFogColor.green * this.weightDefault) / AREA);
		b = (float) ((b * this.biomeWeight + defaultFogColor.blue * this.weightDefault) / AREA);

		// Cook it!
		return new Color(r, g, b);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("color", getFogColor(EnvironState.getWorld(), 1.0F))
				.add("planeDistanceScale", this.planeDistanceScale).toString();
	}
}
