package fr.royalpha.bungeeannounce.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import fr.royalpha.bungeeannounce.BungeeAnnouncePlugin;
import fr.royalpha.bungeeannounce.manager.AnnouncementManager;
import fr.royalpha.bungeeannounce.manager.ConfigManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author sarhatabaot
 */
@CommandAlias("ba|bungeeannounce")
public class BungeeAnnounceCommand extends BaseCommand {
	private final BungeeAnnouncePlugin plugin;

	public BungeeAnnounceCommand(final BungeeAnnouncePlugin plugin) {
		this.plugin = plugin;
	}

	@Subcommand("reload")
	@CommandPermission("bungee.command.reload")
	public void onReload(final CommandSender sender){
		this.plugin.getLogger().info("[" + sender.getName() + "]:Reloading BungeeAnnounce plugin ...");
		int tasks = plugin.getProxy().getScheduler().cancel(plugin);
		sender.sendMessage(new TextComponent(ChatColor.DARK_GRAY + "> " + ChatColor.RED + tasks + " task" + (tasks > 1 ? "s" : "") +" were cancelled."));
		this.plugin.getScheduledAnnouncement().clear();
		sender.sendMessage(new TextComponent(ChatColor.DARK_GRAY + "> " + ChatColor.YELLOW + "Loading BungeeAnnounce ..."));
		this.plugin.load();
		sender.sendMessage(new TextComponent(ChatColor.DARK_GRAY + "> " + ChatColor.GREEN + "BungeeAnnounce plugin is now loaded."));
	}

	@CommandAlias("msg|bungee:msg")
	public void onMessage(final ProxiedPlayer sender, final ProxiedPlayer receiver, final String... message){
		StringBuilder msgBuilder = new StringBuilder();
		for(String msg: message)
			msgBuilder.append(msg).append(" ");
		if (msgBuilder.toString().trim().equals(""))
			return;
		plugin.getMsgManager().message(sender, receiver, msgBuilder.toString());
	}

	@CommandAlias("reply|r|bungee:reply")
	public void onReply(final ProxiedPlayer player, final String... message) {
		if (!plugin.getMsgManager().hasReplier(player)) {
			player.sendMessage(new TextComponent(ChatColor.RED + "You don't have any player to reply."));
			return;
		}

		if (!plugin.getMsgManager().isReplierOnline(player)) {
			player.sendMessage(new TextComponent(ConfigManager.Field.PM_PLAYER_NOT_ONLINE.getString().replaceAll("%PLAYER%", plugin.getMsgManager().getReplierName(player))));
			return;
		}


		ProxiedPlayer to = plugin.getMsgManager().getReplier(player);
		StringBuilder msgBuilder = new StringBuilder();
		for (final String arg : message) msgBuilder.append(arg).append(" ");
		if (msgBuilder.toString().trim().equals(""))
			return;
		plugin.getMsgManager().message(player, to, msgBuilder.toString());
	}

	@CommandAlias("announce")
	@Subcommand("announce")
	@CommandPermission("bungeecord.command.announce")
	public void onAnnounce(final CommandSender sender, final String message) {
		AnnouncementManager.sendToServer(AnnouncementManager.ANNOUNCE, sender instanceof ProxiedPlayer ? sender : null, message.trim(), null, true, "");
	}
}
