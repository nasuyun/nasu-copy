package com.nasuyun.tool.copy.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Version {
    String version;
    int major;
    int minor;
    int revision;

    public boolean majorLte(int major) {
        return this.major <= major;
    }

    public boolean majorEqual(int major) {
        return major == this.major;
    }

    public static Version of(String version) {
        String[] specs = version.split("\\.");
        int major = Integer.valueOf(specs[0]);
        int minor = Integer.valueOf(specs[1]);
        int revision = Integer.valueOf(specs[2]);
        return new Version(version, major, minor, revision);
    }

    interface Versionable {
        Version version();
    }
}
