package com.reeman.delige.models;

public class BackgroundMusicItem {
    public String fileName;
    public boolean isAssetsFile;
    public String netPath;
    public String localPath;
    public volatile boolean isDownloading = false;

    public BackgroundMusicItem(String fileName, boolean assetsFile, String netPath, String localPath) {
        this.fileName = fileName;
        this.isAssetsFile = assetsFile;
        this.netPath = netPath;
        this.localPath = localPath;
    }

    @Override
    public String toString() {
        return "BackgroundMusicItem{" +
                "fileName='" + fileName + '\'' +
                ", assetsFile=" + isAssetsFile +
                ", netPath='" + netPath + '\'' +
                ", localPath='" + localPath + '\'' +
                '}';
    }
}
