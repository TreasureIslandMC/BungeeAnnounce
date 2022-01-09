package fr.royalpha.bungeeannounce.manager;

import java.util.List;

import fr.royalpha.bungeeannounce.BungeeAnnouncePlugin;
import fr.royalpha.bungeeannounce.handler.AnnounceAction;
import fr.royalpha.bungeeannounce.util.BAUtils;
import fr.royalpha.bungeeannounce.announcement.action.ActionAction;
import fr.royalpha.bungeeannounce.announcement.action.SendActionCommand;
import fr.royalpha.bungeeannounce.announcement.announce.AnnounceCommand;
import fr.royalpha.bungeeannounce.announcement.subtitle.SendSubtitleCommand;
import fr.royalpha.bungeeannounce.announcement.subtitle.SubtitleAction;
import fr.royalpha.bungeeannounce.announcement.title.SendTitleCommand;
import fr.royalpha.bungeeannounce.announcement.title.TitleAction;
import fr.royalpha.bungeeannounce.announcement.warn.WarnAction;
import fr.royalpha.bungeeannounce.announcement.warn.WarnCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Royalpha
 */
public enum AnnouncementManager {
	ACTION("action", ConfigManager.Field.ACTION_PREFIX, new ActionAction(), new SendActionCommand()), 
	ANNOUNCE("announce", ConfigManager.Field.ANNOUNCE_PREFIX, new fr.royalpha.bungeeannounce.announcement.announce.AnnounceAction(), new AnnounceCommand()),
	WARN("warn", ConfigManager.Field.WARN_PREFIX, new WarnAction(), new WarnCommand()), 
	SUBTITLE("subtitle", ConfigManager.Field.SUBTITLE_PREFIX, new SubtitleAction(), new SendSubtitleCommand()), 
	TITLE("title", ConfigManager.Field.TITLE_PREFIX, new TitleAction(), new SendTitleCommand());

	private final String rawType;
	private final ConfigManager.Field prefix;
	private final AnnounceAction action;
	private final Command command;

	AnnouncementManager(String rawType, ConfigManager.Field prefix, AnnounceAction action, Command command) {
		this.rawType = rawType;
		this.prefix = prefix;
		this.action = action;
		this.command = command;
	}
	
	@Override
	public String toString() {
		return this.rawType;
	}
	
	public ConfigManager.Field getFieldPrefix() {
		return this.prefix;
	}
	
	public Command getCommandClass() {
		return this.command;
	}
	
	/**
	 * Send any type of anouncement with a lot of possibilities by specifying all of each following parameters. 
	 *
	 * @author Royalpha
	 * @param player The player to whom we must send the message.
	 * @param message Message of the announcement.
	 * @param optionalTitleArgs Optional title arguments. Put three integers and they will be used for fadeIn, stay and fadeOut values.
	 */
	public void send(ProxiedPlayer player, TextComponent message, Integer... optionalTitleArgs) {
		this.action.onAction(player, message, optionalTitleArgs);
	}

	public static AnnouncementManager getAnnouncement(String supposedAnnounceType) {
		for (AnnouncementManager announceType : values()) {
			if (announceType.toString().equals(supposedAnnounceType) || supposedAnnounceType.startsWith(announceType.toString())) {
				return announceType;
			}
		}
		return null;
	}
	
	/**
	 * Send any type of anouncement to a server with a lot of possibilities by specifying all of each following parameters. 
	 *
	 * @author Royalpha
	 * @param announcement The announce type (title/subtitle/warn/announce/action).
	 * @param sender The sender who's supposed to had sent this announcement. Put <b>null</b> if ignored.
	 * @param message Message of the announcement.
	 * @param servers Servers on which the announcement will be displayed. Put <b>null</b> if you want to display the announcement on all your bungee servers.
	 * @param prefix Does the announcement use pre defined prefix in config.yml.
	 * @param permission Permission which is required to see this announcement. Put an empty string if ignored.
	 * @param optionalTitleArgs Optional title arguments. Put three integers and they will be used for fadeIn, stay and fadeOut values.
	 */
	public static void sendToServer(AnnouncementManager announcement, CommandSender sender, String message, List<ServerInfo> servers, boolean prefix, String permission, Integer... optionalTitleArgs) {
		sendToServer(announcement, sender, null, message, servers, prefix, permission, optionalTitleArgs);
	}
	
	/**
	 * Send any type of anouncement to a server with a lot of possibilities by specifying all of each following parameters. 
	 *
	 * @author Royalpha
	 * @param announcement The announce type (title/subtitle/warn/announce/action).
	 * @param sender The sender who's supposed to had sent this announcement. Put <b>null</b> if ignored.
	 * @param player The player involved by the message (put null to ignore). 
	 * @param message Message of the announcement.
	 * @param servers Servers on which the announcement will be displayed. Put <b>null</b> if you want to display the announcement on all your bungee servers.
	 * @param prefix Does the announcement use pre defined prefix in config.yml.
	 * @param permission Permission which is required to see this announcement. Put an empty string if ignored.
	 * @param optionalTitleArgs Optional title arguments. Put three integers and they will be used for fadeIn, stay and fadeOut values.
	 */
	public static void sendToServer(AnnouncementManager announcement, CommandSender sender, ProxiedPlayer player, String message, List<ServerInfo> servers, boolean prefix, String permission, Integer... optionalTitleArgs) {
		permission = permission.trim();
		if (servers == null || servers.isEmpty()) {
			for (ProxiedPlayer receiver : BungeeAnnouncePlugin.getProxyServer().getPlayers()) {
				if ((!permission.equals("") && !receiver.hasPermission(permission)) || receiver.getServer() == null || receiver.getServer().getInfo() == null) 
					continue;
				message = BAUtils.translatePlaceholders(message, sender, receiver, player);
				announcement.send(receiver, BAUtils.parse((prefix ? announcement.getFieldPrefix().getString() : "") + message), optionalTitleArgs);
			}
		} else {
			for (ServerInfo server : servers) {
				for (ProxiedPlayer receiver : server.getPlayers()) {
					if (!permission.equals("") && !receiver.hasPermission(permission)) 
						continue;
					message = BAUtils.translatePlaceholders(message, sender, receiver, player);
					announcement.send(receiver, BAUtils.parse((prefix ? announcement.getFieldPrefix().getString() : "") + message), optionalTitleArgs);
				}
			}
		}
		BungeeAnnouncePlugin.instance().getLoggerSystem().announce(announcement, sender, message);
	}

	/**
	 * Send any type of anouncement to a player with a lot of possibilities by specifying all of each following parameters. 
	 *
	 * @author Royalpha
	 * @param announcement The announce type (title/subtitle/warn/announce/action).
	 * @param sender The sender who's supposed to had sent this announcement. Put <b>null</b> if ignored.
	 * @param pplayer The player to whom we must send the message.
	 * @param message Message of the announcement.
	 * @param prefix Does the announcement use pre defined prefix in config.yml.
	 * @param optionalTitleArgs Optional title arguments. Put three integers and they will be used for fadeIn, stay and fadeOut values.
	 */
	public static void sendToPlayer(AnnouncementManager announcement, CommandSender sender, ProxiedPlayer pplayer, String message, boolean prefix, Integer... optionalTitleArgs) {
		if (pplayer.isConnected() && pplayer.getServer() != null && pplayer.getServer().getInfo() != null) {
			message = BAUtils.translatePlaceholders(message, sender, pplayer, null);
			announcement.send(pplayer, BAUtils.parse((prefix ? ConfigManager.Field.ANNOUNCE_PREFIX.getString() : "") + message), optionalTitleArgs);
		}
	}
	
	/**
	 * Send any type of anouncement to a player with a lot of possibilities by specifying all of each following parameters. 
	 *
	 * @author Royalpha
	 * @param announcement The announce type (title/subtitle/warn/announce/action).
	 * @param sender The sender who's supposed to had sent this announcement. Put <b>null</b> if ignored.
	 * @param player The player to whom we must send the message.
	 * @param message Message of the announcement.
	 * @param prefix Does the announcement use pre defined prefix in config.yml.
	 * @param optionalTitleArgs Optional title arguments. Put three integers and they will be used for fadeIn, stay and fadeOut values.
	 */
	public static void sendToPlayer(AnnouncementManager announcement, CommandSender sender, String player, String message, boolean prefix, Integer... optionalTitleArgs) {
		ProxiedPlayer pplayer = BungeeAnnouncePlugin.getProxyServer().getPlayer(player);
		if (pplayer.isConnected() && pplayer.getServer() != null && pplayer.getServer().getInfo() != null) {
			message = BAUtils.translatePlaceholders(message, sender, pplayer, null);
			announcement.send(pplayer, BAUtils.parse((prefix ? ConfigManager.Field.ANNOUNCE_PREFIX.getString() : "") + message), optionalTitleArgs);
		}
	}
}
