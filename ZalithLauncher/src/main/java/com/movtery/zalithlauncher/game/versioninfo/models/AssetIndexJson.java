package com.movtery.zalithlauncher.game.versioninfo.models;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class AssetIndexJson {
    @SerializedName("map_to_resources")
    private boolean mapToResources;

    @SerializedName("objects")
    private Map<String, ObjectInfo> objects;

    @SerializedName("virtual")
    private boolean virtual;

    public boolean isMapToResources() {
        return mapToResources;
    }

    public Map<String, ObjectInfo> getObjects() {
        return objects;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public static class ObjectInfo {
        @SerializedName("hash")
        private String hash;
        @SerializedName("size")
        private int size;

        public String getHash() {
            return hash;
        }
        public int getSize() {
            return size;
        }
    }
}