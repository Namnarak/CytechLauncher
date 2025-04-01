package com.movtery.zalithlauncher.game.versioninfo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GameManifest {
    private Arguments arguments;
    private AssetIndex assetIndex;
    private String assets;
    private int complianceLevel;
    private Downloads downloads;
    private String id;
    private JavaVersion javaVersion;
    private List<Library> libraries;

    public Arguments getArguments() {
        return arguments;
    }
    public AssetIndex getAssetIndex() {
        return assetIndex;
    }
    public String getAssets() {
        return assets;
    }
    public int getComplianceLevel() {
        return complianceLevel;
    }
    public Downloads getDownloads() {
        return downloads;
    }
    public String getId() {
        return id;
    }
    public JavaVersion getJavaVersion() {
        return javaVersion;
    }
    public List<Library> getLibraries() {
        return libraries;
    }

    public static class Arguments {
        private List<Object> game;
        private List<Object> jvm;

        public List<Object> getGame() {
            return game;
        }
        public List<Object> getJvm() {
            return jvm;
        }
    }

    public static class AssetIndex {
        private String id;
        private String sha1;
        private long size;
        private long totalSize;
        private String url;

        public String getId() {
            return id;
        }
        public String getSha1() {
            return sha1;
        }
        public long getSize() {
            return size;
        }
        public long getTotalSize() {
            return totalSize;
        }
        public String getUrl() {
            return url;
        }
    }

    public static class Downloads {
        private Client client;
        @SerializedName("client_mappings")
        private ClientMappings clientMappings;
        private Server server;
        @SerializedName("server_mappings")
        private ServerMappings serverMappings;

        public Client getClient() {
            return client;
        }
        public ClientMappings getClientMappings() {
            return clientMappings;
        }
        public Server getServer() {
            return server;
        }
        public ServerMappings getServerMappings() {
            return serverMappings;
        }
    }

    public static class Client {
        private String sha1;
        private long size;
        private String url;

        public String getSha1() {
            return sha1;
        }
        public long getSize() {
            return size;
        }
        public String getUrl() {
            return url;
        }
    }

    public static class ClientMappings {
        private String sha1;
        private long size;
        private String url;

        public String getSha1() {
            return sha1;
        }
        public long getSize() {
            return size;
        }
        public String getUrl() {
            return url;
        }
    }

    public static class Server {
        private String sha1;
        private long size;
        private String url;

        public String getSha1() {
            return sha1;
        }
        public long getSize() {
            return size;
        }
        public String getUrl() {
            return url;
        }
    }

    public static class ServerMappings {
        private String sha1;
        private long size;
        private String url;

        public String getSha1() {
            return sha1;
        }
        public long getSize() {
            return size;
        }
        public String getUrl() {
            return url;
        }
    }

    public static class JavaVersion {
        private String component;
        private int majorVersion;

        public String getComponent() {
            return component;
        }
        public int getMajorVersion() {
            return majorVersion;
        }
    }

    public static class Library {
        private DownloadsX downloads;
        private String name;
        private List<Rule> rules;
        private String url;

        public DownloadsX getDownloads() {
            return downloads;
        }
        public void setDownloads(DownloadsX downloads) {
            this.downloads = downloads;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public List<Rule> getRules() {
            return rules;
        }
        public String getUrl() {
            return url;
        }
        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class DownloadsX {
        private Artifact artifact;

        public Artifact getArtifact() {
            return artifact;
        }
        public void setArtifact(Artifact artifact) {
            this.artifact = artifact;
        }
    }

    public static class Artifact {
        private String path;
        private String sha1;
        private long size;
        private String url;

        public String getPath() {
            return path;
        }
        public void setPath(String path) {
            this.path = path;
        }
        public String getSha1() {
            return sha1;
        }
        public void setSha1(String sha1) {
            this.sha1 = sha1;
        }
        public long getSize() {
            return size;
        }
        public void setSize(long size) {
            this.size = size;
        }
        public String getUrl() {
            return url;
        }
        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Rule {
        private String action;
        private Os os;
        private Features features;
        private List<Object> value;

        public String getAction() {
            return action;
        }
        public Os getOs() {
            return os;
        }
        public Features getFeatures() {
            return features;
        }
        public List<Object> getValue() {
            return value;
        }
    }

    public static class Os {
        private String name;
        private String arch;

        public String getName() {
            return name;
        }
        public String getArch() {
            return arch;
        }
    }

    public static class Features {
        @SerializedName("is_demo_user")
        private Boolean isDemoUser;
        @SerializedName("has_custom_resolution")
        private Boolean hasCustomResolution;
        @SerializedName("has_quick_plays_support")
        private Boolean hasQuickPlaysSupport;
        @SerializedName("is_quick_play_singleplayer")
        private Boolean isQuickPlaySingleplayer;
        @SerializedName("is_quick_play_multiplayer")
        private Boolean isQuickPlayMultiplayer;
        @SerializedName("is_quick_play_realms")
        private Boolean isQuickPlayRealms;

        public Boolean getDemoUser() {
            return isDemoUser;
        }
        public Boolean getHasCustomResolution() {
            return hasCustomResolution;
        }
        public Boolean getHasQuickPlaysSupport() {
            return hasQuickPlaysSupport;
        }
        public Boolean getQuickPlaySingleplayer() {
            return isQuickPlaySingleplayer;
        }
        public Boolean getQuickPlayMultiplayer() {
            return isQuickPlayMultiplayer;
        }
        public Boolean getQuickPlayRealms() {
            return isQuickPlayRealms;
        }
    }
}