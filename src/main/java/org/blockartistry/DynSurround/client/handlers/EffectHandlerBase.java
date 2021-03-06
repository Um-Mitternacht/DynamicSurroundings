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

package org.blockartistry.DynSurround.client.handlers;

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.lib.random.XorShiftRandom;

import com.google.common.base.MoreObjects;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public abstract class EffectHandlerBase {
	
	protected final Random RANDOM = XorShiftRandom.current();
	
	private final String handlerName;
	
	EffectHandlerBase(@Nonnull final String name) {
		this.handlerName = name;
	}
	
	// Used to obtain the name of the handler for logging purposes
	public final String getHandlerName() {
		return this.handlerName;
	}
	
	// The meat of the processing logic.  Invoked every client tick.
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {
		
	}
	
	// Called when the client is connecting to a server.  Useful for initializing
	// data to a baseline state (i.e. flushing out the crap).
	public void onConnect() {
	}
	
	// Called when the client disconnects from a server.  Useful for cleaning up
	// state space.
	public void onDisconnect() {
	}
	
	//////////////////////////////
	//
	//  DO NOT HOOK THESE EVENTS!
	//
	//////////////////////////////
	final void connect0() {
		this.onConnect();
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	final void disconnect0() {
		MinecraftForge.EVENT_BUS.unregister(this);
		this.onDisconnect();
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("name", this.getHandlerName()).toString();
	}
	
}
