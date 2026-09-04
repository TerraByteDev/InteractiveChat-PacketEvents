package net.skullian.updater;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.skullian.util.ChatUtils;
import net.skullian.util.GithubBuildInfo;
import net.skullian.util.GithubUtils;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.Locale;

public final class Updater {

    private Updater() {
    }

    public static UpdateStatus checkUpdate(CommandSender... senders) {
        GithubBuildInfo currentBuild = GithubBuildInfo.CURRENT;
        UpdateStatus updateStatus = new UpdateStatus(false, false);

        try {
            if (currentBuild.stable()) {
                return checkStableUpdate(currentBuild, senders, updateStatus);
            }

            return checkDevelopmentUpdate(currentBuild, senders, updateStatus);
        } catch (IOException error) {
            ChatUtils.sendMessage(
                    "<red>Failed to check for updates. Please try again later.",
                    senders);

            updateStatus.setFailed(true);
            return updateStatus;
        }
    }

    private static UpdateStatus checkStableUpdate(
            GithubBuildInfo currentBuild,
            CommandSender[] senders,
            UpdateStatus updateStatus) throws IOException {
        GithubBuildInfo latestBuild = GithubUtils.lookupLatestRelease();

        // Stable builds must compare release versions, not a release tag
        // against the installed build's commit SHA.
        if (currentBuild.name().equals(latestBuild.name())) {
            updateStatus.setUpToDate(true);
            return updateStatus;
        }

        String url = "https://github.com/TerraByteDev/InteractiveChat-PacketEvents/releases/tag/"
                + latestBuild.id();

        ChatUtils.sendMessage(
                "<green>A new version of InteractiveChat-PacketEvents is available: "
                        + latestBuild.name() + "!",
                senders);
        ChatUtils.sendMessage(
                "<grey>Download at: <click:open_url:'" + url + "'>"
                        + url + "</click>",
                senders);

        return updateStatus;
    }

    private static UpdateStatus checkDevelopmentUpdate(
            GithubBuildInfo currentBuild,
            CommandSender[] senders,
            UpdateStatus updateStatus) throws IOException {
        GithubUtils.GitHubStatusLookup lookupStatus = GithubUtils.compare(
                GithubUtils.MAIN_BRANCH,
                currentBuild.id());

        if (lookupStatus.isBehind()) {
            ChatUtils.sendMessage(
                    "<yellow>You are running a development build of InteractiveChat-PacketEvents!\n"
                            + "The latest available development build is "
                            + String.format(Locale.ROOT, "%,d", lookupStatus.getDistance())
                            + " commits ahead.",
                    senders);

            return updateStatus;
        }

        if (lookupStatus.getStatus() == GithubUtils.GitHubStatus.FAILURE
                || lookupStatus.getStatus() == GithubUtils.GitHubStatus.UNKNOWN
                || lookupStatus.getStatus() == GithubUtils.GitHubStatus.DIVERGED) {
            ChatUtils.sendMessage(
                    "<red>Failed to determine whether a newer development build is available.",
                    senders);
            updateStatus.setFailed(true);
            return updateStatus;
        }

        updateStatus.setUpToDate(true);
        return updateStatus;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static final class UpdateStatus {
        private boolean isUpToDate;
        private boolean failed;
    }
}
