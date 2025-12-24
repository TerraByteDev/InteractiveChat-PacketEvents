package net.skullian.util;

import net.skullian.InteractiveChatPacketEvents;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public record GithubBuildInfo(String id, String name, String branch, boolean stable) {
    public static final GithubBuildInfo CURRENT = readBuildInfo();

    public GithubBuildInfo(String id, String name, String branch, boolean stable) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.branch = Objects.requireNonNull(branch);
        this.stable = stable;
    }

    @Override
    public String toString() {
        return "GithubBuildInfo{" + "id='" + id + "', name='" + name + "', branch='" + branch + ", stable=" + stable + '}';
    }

    private static GithubBuildInfo readBuildInfo() {
        try (InputStream in = InteractiveChatPacketEvents.instance.getResource("version.properties")) {
            if (in == null)
                throw new IOException("No input");

            Properties properties = new Properties();
            properties.load(in);
            return parseBuildInfo(properties);
        } catch (IOException e) {
            throw new AssertionError("Missing version information!", e);
        }
    }

    private static GithubBuildInfo parseBuildInfo(Properties properties) {
        String id = properties.getProperty("git.commit.id.abbrev");
        String name = properties.getProperty("git.build.version");
        String branch = properties.getProperty("git.branch");

        boolean stable = name.indexOf('-') == -1 && name.indexOf('+') == -1;

        return new GithubBuildInfo(id, name, branch, stable);
    }
}
