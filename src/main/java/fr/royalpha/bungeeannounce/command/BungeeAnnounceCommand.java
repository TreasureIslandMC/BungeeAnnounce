package fr.royalpha.bungeeannounce.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import fr.royalpha.bungeeannounce.BungeeAnnouncePlugin;
import fr.royalpha.bungeeannounce.manager.ConfigManager;
import fr.royalpha.bungeeannounce.manager.MsgManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author sarhatabaot
 */
@CommandAlias("ba|bungeeannounce")
public class BungeeAnnounceCommand extends BaseCommand {
    private static final String CANNOT_IGNORE = "bungeeannounce.ignore.cannot";
    private final BungeeAnnouncePlugin plugin;
    private final MsgManager msgManager;

    public BungeeAnnounceCommand(final BungeeAnnouncePlugin plugin) {
        this.plugin = plugin;
        this.msgManager = plugin.getMsgManager();
    }

    @Subcommand("reload")
    @CommandPermission("bungeeannounce.command.reload")
    @Description("Reloads BungeeAnnounce.")
    public void onReload(final CommandSender sender) {
        this.plugin.getLogger().info(ChatColor.stripColor("[" + sender.getName() + "]: Reloading BungeeAnnounce plugin ..."));
        int tasks = plugin.getProxy().getScheduler().cancel(plugin);
        sender.sendMessage(new TextComponent(ChatColor.DARK_GRAY + "> " + ChatColor.RED + tasks + " task" + (tasks > 1 ? "s" : "") + " were cancelled."));
        this.plugin.getScheduledAnnouncement().clear();
        sender.sendMessage(new TextComponent(ChatColor.DARK_GRAY + "> " + ChatColor.YELLOW + "Loading BungeeAnnounce ..."));
        this.plugin.load();
        sender.sendMessage(new TextComponent(ChatColor.DARK_GRAY + "> " + ChatColor.GREEN + "BungeeAnnounce plugin is now loaded."));
    }

    @CommandAlias("msg")
    @Subcommand("message|msg")
    @CommandCompletion("@players @nothing")
    @CommandPermission("bungeeannounce.command.message")
    @Description("Send a private message to another player.")
    public void onMessage(final ProxiedPlayer sender, @Single final ProxiedPlayer receiver, final String... message) {
        if (!msgManager.isToggled(sender)) {
            sender.sendMessage(new TextComponent("You cannot send messages as you have toggled off messages."));
        }
        if (msgManager.isIgnored(sender, receiver)) {
            sender.sendMessage(new TextComponent(ConfigManager.Field.PM_PLAYER_NOT_ONLINE.getString().replaceAll("%PLAYER%", plugin.getMsgManager().getReplierName(sender))));
            return;
        }

        if (!msgManager.isToggled(receiver)) {
            sender.sendMessage(new TextComponent(ConfigManager.Field.MESSAGES_OFF.getString()));
            return;
        }

        msgManager.message(sender, receiver, getFinalMessage(sender, message));
    }

    private String getFinalMessage(final ProxiedPlayer sender, final String... message) {
        StringBuilder msgBuilder = new StringBuilder();
        for (String msg : message)
            msgBuilder.append(msg).append(" ");
        if (msgBuilder.toString().trim().equals(""))
            return "";
        if (sender.hasPermission("bungeeannounce.colors"))
            return ChatColor.translateAlternateColorCodes('&', msgBuilder.toString());

        return msgBuilder.toString();
    }

    @CommandAlias("r|reply")
    @Subcommand("r|reply")
    @CommandPermission("bungeeannounce.command.reply")
    @Description("Reply to a private message.")
    public void onReply(final ProxiedPlayer sender, final String... message) {
        if (!msgManager.hasReplier(sender)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "You don't have any player to reply."));
            return;
        }

        if (!msgManager.isReplierOnline(sender)) {
            sender.sendMessage(new TextComponent(ConfigManager.Field.PM_PLAYER_NOT_ONLINE.getString().replaceAll("%PLAYER%", plugin.getMsgManager().getReplierName(sender))));
            return;
        }

        final ProxiedPlayer receiver = msgManager.getReplier(sender);
        if (msgManager.isIgnored(sender, receiver)) {
            sender.sendMessage(new TextComponent(ConfigManager.Field.PM_PLAYER_NOT_ONLINE.getString().replaceAll("%PLAYER%", plugin.getMsgManager().getReplierName(sender))));
            return;
        }

        StringBuilder msgBuilder = new StringBuilder();
        for (final String arg : message) msgBuilder.append(arg).append(" ");
        if (msgBuilder.toString().trim().equals(""))
            return;
        msgManager.message(sender, receiver, msgBuilder.toString());
    }

    @CommandAlias("msgignore")
    @Subcommand("ignore")
    @CommandPermission("bungeeannounce.command.ignore")
    @CommandCompletion("@players")
    @Description("Ignore a player.")
    public void onIgnore(final ProxiedPlayer player, @Single final ProxiedPlayer toIgnore) {
        if (toIgnore.hasPermission(CANNOT_IGNORE)) {
            player.sendMessage(new TextComponent("&cYou cannot ignore this player."));
            return;
        }

        msgManager.ignore(player, toIgnore);
    }

    @CommandAlias("msgtoggle")
    @Subcommand("toggle")
    @CommandPermission("bungeeannounce.command.toggle")
    @Description("Toggle message sending. While this is off, no one can send you messages. And you can't send any messages.")
    public void onToggle(final ProxiedPlayer player) {
        msgManager.toggle(player);
    }


}
