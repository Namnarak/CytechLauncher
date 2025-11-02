/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.multirt;

import androidx.annotation.Nullable;

import com.movtery.zalithlauncher.ZLApplication;
import com.movtery.zalithlauncher.utils.device.Architecture;

import java.util.Objects;

public class Runtime {
    public final String name;
    public final String versionString;
    public final String arch;
    public final int javaVersion;
    public final boolean isProvidedByLauncher;
    public final boolean isJDK;

    public Runtime(String name) {
        this(name, null, null, 0, false, false);
    }

    Runtime(String name, String versionString, String arch, int javaVersion, boolean isProvidedByLauncher, boolean isJDK) {
        this.name = name;
        this.versionString = versionString;
        this.arch = arch;
        this.javaVersion = javaVersion;
        this.isProvidedByLauncher = isProvidedByLauncher;
        this.isJDK = isJDK;
    }

    public boolean isCompatible() {
        return versionString != null && ZLApplication.getDEVICE_ARCHITECTURE() == Architecture.INSTANCE.archAsInt(arch);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Runtime runtime = (Runtime) obj;
        return name.equals(runtime.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
