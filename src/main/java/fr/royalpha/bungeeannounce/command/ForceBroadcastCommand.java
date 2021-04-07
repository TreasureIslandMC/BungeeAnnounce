package fr.royalpha.bungeeannounce.command;

import java.util.ArrayList;
import java.util.List;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import fr.royalpha.bungeeannounce.BungeeAnnouncePlugin;
import fr.royalpha.bungeeannounce.util.BAUtils;
import fr.royalpha.bungeeannounce.manager.AnnouncementManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

/**
 * @author Royalpha
 */
public class ForceBroadcastCommand extends BaseCommand {

	private BungeeAnnouncePlugin plugin;

	public ForceBroadcastCommand(BungeeAnnouncePlugin plugin) {
		this.plugin = plugin;
	}

	@CommandAlias("forcebroadcast|bungee:fbc|fbc")
	@CommandPermission("bungeecord.command.forcebroadcast")
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
	@Deprecated
	public void execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /fbc <announcement>"));
			return;
		}
		Configuration schedulerSection = this.plugin.getConfigManager().getConfig().getSection("scheduler");
		String taskName = args[0];
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
}
