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

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.lib.collections.ObjectArray;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class EffectManager {

	private static EffectManager INSTANCE = null;

	private final ObjectArray<EffectHandlerBase> effectHandlers = new ObjectArray<EffectHandlerBase>();

	private EffectManager() {
	}

	private void init() {
		// These two first in this order
		this.effectHandlers.add(new EnvironStateHandler());
		this.effectHandlers.add(new ExpressionStateHandler());

		this.effectHandlers.add(new AreaSurveyHandler());
		this.effectHandlers.add(new FogEffectHandler());
		this.effectHandlers.add(new ParticleSystemHandler());
		this.effectHandlers.add(new PlayerActionHandler());
		this.effectHandlers.add(new SoundCullHandler());
		this.effectHandlers.add(new PopoffEffectHandler());
		this.effectHandlers.add(new FootstepsHandler());
		this.effectHandlers.add(new AreaSoundEffectHandler());
		this.effectHandlers.add(new EntityEmojiHandler());
		this.effectHandlers.add(new AuroraEffectHandler());
		this.effectHandlers.add(new SpeechBubbleHandler());
		this.effectHandlers.add(new WeatherHandler());

		// Add this one last so it goes last
		this.effectHandlers.add(SoundEffectHandler.INSTANCE);

		for (final EffectHandlerBase h : this.effectHandlers)
			h.connect0();
	}

	private void fini() {
		for (final EffectHandlerBase h : this.effectHandlers)
			h.disconnect0();
		this.effectHandlers.clear();
	}

	public static void register() {
		INSTANCE = new EffectManager();
		INSTANCE.init();
		MinecraftForge.EVENT_BUS.register(INSTANCE);
	}

	public static void unregister() {
		if (INSTANCE != null) {
			MinecraftForge.EVENT_BUS.unregister(INSTANCE);
			INSTANCE.fini();
			INSTANCE = null;
		}
	}

	@SubscribeEvent
	public void clientTick(final TickEvent.ClientTickEvent event) {
		if (event.phase == Phase.END || Minecraft.getMinecraft().isGamePaused())
			return;

		final EntityPlayer player = FMLClientHandler.instance().getClient().player;
		if (player == null)
			return;

		final World world = FMLClientHandler.instance().getClient().world;
		if (world == null)
			return;

		final boolean tickProfile = DSurround.log().testTrace(ModOptions.Trace.TICK_PROFILE);
		DSurround.getProfiler().startSection("DSurroundEffectManager");

		if (tickProfile) {
			for (int i = 0; i < this.effectHandlers.size(); i++) {
				final EffectHandlerBase handler = this.effectHandlers.get(i);
				DSurround.getProfiler().startSection(handler.getHandlerName());
				handler.process(world, player);
				DSurround.getProfiler().endSection();
			}
		} else {
			for (int i = 0; i < this.effectHandlers.size(); i++)
				this.effectHandlers.get(i).process(world, player);
		}

		DSurround.getProfiler().endSection();
	}

}
