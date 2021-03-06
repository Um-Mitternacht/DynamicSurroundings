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

package org.blockartistry.DynSurround.client.footsteps.system;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.footsteps.implem.AcousticsManager;
import org.blockartistry.DynSurround.client.footsteps.implem.BlockMap;
import org.blockartistry.DynSurround.client.footsteps.implem.PrimitiveMap;
import org.blockartistry.DynSurround.client.footsteps.interfaces.ISoundPlayer;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IStepPlayer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Isolator {
	private AcousticsManager acoustics;
	private Solver solver;
	private BlockMap blockMap;
	private PrimitiveMap primitiveMap;

	private ISoundPlayer soundPlayer;
	private IStepPlayer defaultStepPlayer;

	private Generator generator;

	public Isolator() {
		this.blockMap = new BlockMap(this);
	}

	public void onFrame(@Nonnull final EntityPlayer player) {
		if (this.generator == null)
			return;

		this.generator.generateFootsteps(player);
		this.acoustics.think();
	}

	//

	@Nonnull
	public AcousticsManager getAcoustics() {
		return this.acoustics;
	}

	@Nonnull
	public Solver getSolver() {
		return this.solver;
	}

	@Nonnull
	public BlockMap getBlockMap() {
		return this.blockMap;
	}

	@Nonnull
	public PrimitiveMap getPrimitiveMap() {
		return this.primitiveMap;
	}

	@Nonnull
	public ISoundPlayer getSoundPlayer() {
		return this.soundPlayer;
	}

	@Nonnull
	public IStepPlayer getDefaultStepPlayer() {
		return this.defaultStepPlayer;
	}

	//

	public void setAcoustics(@Nonnull final AcousticsManager acoustics) {
		this.acoustics = acoustics;
	}

	public void setSolver(@Nonnull final Solver solver) {
		this.solver = solver;
	}

	public void setBlockMap(@Nonnull final BlockMap blockMap) {
		this.blockMap = blockMap;
	}

	public void setPrimitiveMap(@Nonnull final PrimitiveMap primitiveMap) {
		this.primitiveMap = primitiveMap;
	}

	public void setSoundPlayer(@Nonnull final ISoundPlayer soundPlayer) {
		this.soundPlayer = soundPlayer;
	}

	public void setDefaultStepPlayer(@Nonnull final IStepPlayer defaultStepPlayer) {
		this.defaultStepPlayer = defaultStepPlayer;
	}

	//

	public void setGenerator(@Nonnull final Generator generator) {
		this.generator = generator;
	}
}
