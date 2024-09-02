package com.example.bookblossom.models;

public class AppFileData {
    private String VersionName;
    private int VersionCode;

    public AppFileData() { }
    public String getVersionName() {
        return VersionName;
    }

    public void setVersionName(String versionName) {
        VersionName = versionName;
    }

    public int getVersionCode() {
        return VersionCode;
    }

    public void setVersionCode(int versionCode) {
        VersionCode = versionCode;
    }

}
