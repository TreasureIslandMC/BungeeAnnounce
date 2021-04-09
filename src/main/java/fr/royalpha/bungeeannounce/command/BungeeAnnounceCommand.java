package fr.royalpha.bungeeannounce.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import fr.royalpha.bungeeannounce.BungeeAnnouncePlugin;
import fr.royalpha.bungeeannounce.handler.PlayerAnnouncer;
import fr.royalpha.bungeeannounce.manager.AnnouncementManager;
import fr.royalpha.bungeeannounce.manager.ConfigManager;
import fr.royalpha.bungeeannounce.util.BAUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.List;

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
	@CommandPermission("bungeeannounce.command.reload")
	public void onReload(final CommandSender sender){
		this.plugin.getLogger().info(ChatColor.stripColor("\u00a77[" + sender.getName() + "]: \u00a7aReloading BungeeAnnounce plugin ..."));
		int tasks = plugin.getProxy().getScheduler().cancel(plugin);
		sender.sendMessage(new TextComponent(ChatColor.DARK_GRAY + "> " + ChatColor.RED + tasks + " task" + (tasks > 1 ? "s" : "") +" were cancelled."));
		this.plugin.getScheduledAnnouncement().clear();
		PlayerAnnouncer.playerAnnouncers.clear();
		sender.sendMessage(new TextComponent(ChatColor.DARK_GRAY + "> " + ChatColor.YELLOW + "Loading BungeeAnnounce ..."));
		this.plugin.load();
		sender.sendMessage(new TextComponent(ChatColor.DARK_GRAY + "> " + ChatColor.GREEN + "BungeeAnnounce plugin is now load."));
	}

	@Subcommand("forcebroadcast|fbc")
	@CommandPermission("bungeeannounce.command.forcebroadcast")
	public void onForceBroadcast(final CommandSender sender, final String taskName) {
		Configuration schedulerSection = this.plugin.getConfigManager().getConfig().getSection("scheduler");
		try {
			String type = schedulerSection.getString(taskName + ".type", "");

			AnnouncementManager announcement = AnnouncementManager.getAnnouncement(type);
			if (announcement == null) {
				sender.sendMessage(new TextComponent(BAUtils.colorizz("&cError when loading announcement \"" + taskName + "\", the field 'type' wasn't recognized (It can also means that this announcement doesn't exist).")));
				return;
			}

			String message = schedulerSection.getString(taskName + ".message", "<No message was set for this announcement>");
			List<String> servers = schedulerSection.getStringList(taskName + ".servers");
			String permission = schedulerSection.getString(taskName + ".permission", "");
			Integer[] optionalTitleArgs = BAUtils.getOptionalTitleArgsFromConfig(announcement, type);

			List<ServerInfo> serversInfo = new ArrayList<>();
			for (String entry : servers) {
				if (entry.trim().equalsIgnoreCase("all")) {
					serversInfo.clear();
					break;
				} else {
					ServerInfo info = plugin.getProxy().getServerInfo(entry);
					if (info != null) {
						serversInfo.add(info);
					} else {
						sender.sendMessage(new TextComponent(BAUtils.colorizz("&eServer \"" + entry
								+ "\" for announcement \"" + taskName + "\" doesn't exist ! Skipping it ...")));
					}
				}
			}

			AnnouncementManager.sendToServer(announcement, sender, message, serversInfo, false, permission, optionalTitleArgs);

		} catch (Exception ex) {
			sender.sendMessage(new TextComponent(BAUtils.colorizz("&cAn error occured ! There is no announcement named \"" + taskName + "\" in the config file.")));
		}
	}

	@Subcommand("colorcode|colorcodes")
	public void onColorCode(final CommandSender sender) {
		sender.sendMessage(new TextComponent("Minecraft Colors:"));
		sender.sendMessage(new TextComponent("\u00a70&0  \u00a71&1  \u00a72&2  \u00a73&3"));
		sender.sendMessage(new TextComponent("\u00a74&4  \u00a75&5  \u00a76&6  \u00a77&7"));
		sender.sendMessage(new TextComponent("\u00a78&8  \u00a79&9  \u00a7a&a  \u00a7b&b"));
		sender.sendMessage(new TextComponent("\u00a7c&c  \u00a7d&d  \u00a7e&e"));
		sender.sendMessage(new TextComponent(""));
		sender.sendMessage(new TextComponent("Minecraft formats:"));
		sender.sendMessage(new TextComponent("&k \u00a7kmagic\u00a7r &l \u00a7lBold"));
		sender.sendMessage(new TextComponent("&m \u00a7mStrike\u00a7r &n \u00a7nUnderline"));
		sender.sendMessage(new TextComponent("&o \u00a7oItalic\u00a7r &r \u00a7rReset"));
	}
}
