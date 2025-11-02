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

package com.movtery.zalithlauncher.game.versioninfo.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VersionManifest {
    @SerializedName("latest")
    private Latest latest;

    @SerializedName("versions")
    private List<Version> versions;

    public Latest getLatest() {
        return latest;
    }

    public List<Version> getVersions() {
        return versions;
    }

    public static class Latest {
        @SerializedName("release")
        private String release;

        @SerializedName("snapshot")
        private String snapshot;

        public String getRelease() {
            return release;
        }

        public String getSnapshot() {
            return snapshot;
        }
    }

    public static class Version {
        @SerializedName("id")
        private String id;

        @SerializedName("type")
        private String type;

        @SerializedName("url")
        private String url;

        @SerializedName("time")
        private String time;

        @SerializedName("releaseTime")
        private String releaseTime;

        @SerializedName("sha1")
        private String sha1;

        @SerializedName("complianceLevel")
        private int complianceLevel;

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getUrl() {
            return url;
        }

        public String getTime() {
            return time;
        }

        public String getReleaseTime() {
            return releaseTime;
        }

        public String getSha1() {
            return sha1;
        }

        public int getComplianceLevel() {
            return complianceLevel;
        }
    }
}
