package fr.royalpha.bungeeannounce.manager;

import java.util.List;

import fr.royalpha.bungeeannounce.BungeeAnnouncePlugin;
import fr.royalpha.bungeeannounce.announcement.announce.GlobalAnnounceAction;
import fr.royalpha.bungeeannounce.handler.AnnounceAction;
import fr.royalpha.bungeeannounce.util.BAUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Royalpha
 */
public enum AnnouncementManager {
	ANNOUNCE(ConfigManager.Field.ANNOUNCE_PREFIX, new GlobalAnnounceAction());

	private final String rawType;
	private final ConfigManager.Field prefix;
	private final AnnounceAction action;

	AnnouncementManager(ConfigManager.Field prefix, AnnounceAction action) {
		this.rawType = "announce";
		this.prefix = prefix;
		this.action = action;
	}
	
	@Override
	public String toString() {
		return this.rawType;
	}
	
	public ConfigManager.Field getFieldPrefix() {
		return this.prefix;
	}
	
	/**
	 * Send any type of announcement with a lot of possibilities by specifying all of each following parameters.
	 *
	 * @author Royalpha
	 * @param player The player to whom we must send the message.
	 * @param message Message of the announcement.
	 * @param optionalTitleArgs Optional title arguments. Put three integers, and they will be used for fadeIn, stay and fadeOut values.
	 */
	public void send(ProxiedPlayer player, TextComponent message, Integer... optionalTitleArgs) {
		this.action.onAction(player, message, optionalTitleArgs);
	}

	public void send(ProxiedPlayer player, TextComponent[] messages, Integer... optionalTitleArgs) {
		this.action.onAction(player, messages, optionalTitleArgs);
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
	 * Send any type of announcement to a server with a lot of possibilities by specifying all of each following parameters.
	 *
	 * @author Royalpha
	 * @param announcement The announcement type (title/subtitle/warn/announce/action).
	 * @param sender The sender who's supposed to had sent this announcement. Put <b>null</b> if ignored.
	 * @param message Message of the announcement.
	 * @param servers Servers on which the announcement will be displayed. Put <b>null</b> if you want to display the announcement on all your bungee servers.
	 * @param prefix Does the announcement use pre-defined prefix in config.yml.
	 * @param permission Permission which is required to see this announcement. Put an empty string if ignored.
	 * @param optionalTitleArgs Optional title arguments. Put three integers, and they will be used for fadeIn, stay and fadeOut values.
	 */
	public static void sendToServer(AnnouncementManager announcement, CommandSender sender, String message, List<ServerInfo> servers, boolean prefix, String permission, Integer... optionalTitleArgs) {
		sendToServer(announcement, sender, null, message, servers, prefix, permission, optionalTitleArgs);
	}
	
	/**
	 * Send any type of announcement to a server with a lot of possibilities by specifying all of each following parameters.
	 *
	 * @author Royalpha
	 * @param announcement The announcement type (title/subtitle/warn/announce/action).
	 * @param sender The sender who's supposed to had sent this announcement. Put <b>null</b> if ignored.
	 * @param player The player involved by the message (put null to ignore). 
	 * @param message Message of the announcement.
	 * @param servers Servers on which the announcement will be displayed. Put <b>null</b> if you want to display the announcement on all your bungee servers.
	 * @param prefix Does the announcement use pre-defined prefix in config.yml.
	 * @param permission Permission which is required to see this announcement. Put an empty string if ignored.
	 * @param optionalTitleArgs Optional title arguments. Put three integers, and they will be used for fadeIn, stay and fadeOut values.
	 */
	public static void sendToServer(AnnouncementManager announcement, CommandSender sender, ProxiedPlayer player, String message, List<ServerInfo> servers, boolean prefix, String permission, Integer... optionalTitleArgs) {
		permission = permission.trim();
		final String prefixedMessage = ((prefix ? announcement.getFieldPrefix().getString() : "") + message).replace("[ln]","\n");
		final String[] splitMessage = BAUtils.splitIntoArray(prefixedMessage);
		if (servers == null || servers.isEmpty()) {
			for (ProxiedPlayer receiver : BungeeAnnouncePlugin.getProxyServer().getPlayers()) {
				if ((!permission.equals("") && !receiver.hasPermission(permission)) || receiver.getServer() == null || receiver.getServer().getInfo() == null) 
					continue;
				message = BAUtils.translatePlaceholders(message, sender, receiver, player);
				announcement.send(receiver, BAUtils.parse(splitMessage), optionalTitleArgs);
			}
		} else {
			for (ServerInfo server : servers) {
				for (ProxiedPlayer receiver : server.getPlayers()) {
					if (!permission.equals("") && !receiver.hasPermission(permission)) 
						continue;
					message = BAUtils.translatePlaceholders(message, sender, receiver, player);
					announcement.send(receiver, BAUtils.parse(splitMessage), optionalTitleArgs);
				}
			}
		}
		BungeeAnnouncePlugin.getLoggerSystem().announce(announcement, sender, message);
	}
}
