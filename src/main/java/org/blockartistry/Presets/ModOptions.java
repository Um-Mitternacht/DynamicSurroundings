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

package org.blockartistry.Presets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.lib.ConfigProcessor.Comment;
import org.blockartistry.lib.ConfigProcessor.Parameter;
import org.blockartistry.lib.ConfigProcessor.RestartRequired;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public final class ModOptions {

	public static final String CATEGORY_LOGGING_CONTROL = "logging";
	public static final String CONFIG_ENABLE_DEBUG_LOGGING = "Enable Debug Logging";
	public static final String CONFIG_ENABLE_ONLINE_VERSION_CHECK = "Enable Online Version Check";
	private static final List<String> loggingSort = Arrays.asList(CONFIG_ENABLE_ONLINE_VERSION_CHECK,
			CONFIG_ENABLE_DEBUG_LOGGING);

	@Parameter(category = CATEGORY_LOGGING_CONTROL, property = CONFIG_ENABLE_DEBUG_LOGGING, defaultValue = "false", lang = "presets.cfg.logging.EnableDebug")
	@Comment("Enables/disables debug logging of the mod")
	@RestartRequired
	public static boolean enableDebugLogging = false;
	@Parameter(category = CATEGORY_LOGGING_CONTROL, property = CONFIG_ENABLE_ONLINE_VERSION_CHECK, defaultValue = "true", lang = "presets.cfg.logging.VersionCheck")
	@Comment("Enables/disables display of version check information")
	@RestartRequired
	public static boolean enableVersionChecking = true;

	private ModOptions() {
	}

	public static void load(final Configuration config) {
		
		// CATEGORY: Logging
		config.setCategoryRequiresMcRestart(CATEGORY_LOGGING_CONTROL, false);
		config.setCategoryRequiresWorldRestart(CATEGORY_LOGGING_CONTROL, false);
		config.setCategoryComment(CATEGORY_LOGGING_CONTROL, "Defines how Presets! logging will behave");
		config.setCategoryPropertyOrder(CATEGORY_LOGGING_CONTROL, new ArrayList<String>(loggingSort));
		config.setCategoryLanguageKey(CATEGORY_LOGGING_CONTROL, "presets.cfg.logging.cat.Logging");

		// Iterate through the config list looking for properties without
		// comments. These will be scrubbed.
		for (final String cat : config.getCategoryNames())
			scrubCategory(config.getCategory(cat));

	}

	private static void scrubCategory(final ConfigCategory category) {
		final List<String> killList = new ArrayList<String>();
		for (final Entry<String, Property> entry : category.entrySet())
			if (StringUtils.isEmpty(entry.getValue().getComment()))
				killList.add(entry.getKey());

		for (final String kill : killList)
			category.remove(kill);
	}
}
