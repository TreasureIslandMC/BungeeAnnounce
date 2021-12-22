package fr.royalpha.bungeeannounce.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import fr.royalpha.bungeeannounce.BungeeAnnouncePlugin;
import fr.royalpha.bungeeannounce.manager.AnnouncementManager;
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

	@CommandAlias("announce")
	@Subcommand("announce")
	@CommandPermission("bungeecord.command.announce")
	public void onAnnounce(final CommandSender sender, final String message) {
		AnnouncementManager.sendToServer(AnnouncementManager.ANNOUNCE, sender instanceof ProxiedPlayer ? sender : null, message.trim(), null, true, "");
	}
}
