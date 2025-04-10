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

    public Runtime(String name) {
        this(name, null, null, 0, false);
    }

    Runtime(String name, String versionString, String arch, int javaVersion, boolean isProvidedByLauncher) {
        this.name = name;
        this.versionString = versionString;
        this.arch = arch;
        this.javaVersion = javaVersion;
        this.isProvidedByLauncher = isProvidedByLauncher;
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
