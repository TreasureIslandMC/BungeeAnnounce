package fr.royalpha.bungeeannounce.manager;

import java.util.HashMap;
import java.util.Map;

import fr.royalpha.bungeeannounce.util.BAUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Royalpha
 */
public class MsgManager {

	private final Map<String, String> map;
	
	public MsgManager() {
		this.map = new HashMap<>();
	}
	
	public void message(ProxiedPlayer from, ProxiedPlayer to, String message) {
		//TODO
		final String fromFormat = BAUtils.translatePlaceholders(ConfigManager.Field.PM_SENT.getString(), from, to, from).replaceAll("%MESSAGE%", message.trim());
		final String toFormat = BAUtils.translatePlaceholders(ConfigManager.Field.PM_RECEIVED.getString(), from,to,from).replaceAll("%MESSAGE%", message.trim());
		from.sendMessage(new TextComponent(fromFormat));
		to.sendMessage(new TextComponent(toFormat));
		if (to == from)
			from.sendMessage(new TextComponent(ConfigManager.Field.PM_SENDER_EQUALS_RECEIVER.getString()));
		if (hasReplier(to))
			map.remove(to.getName());
		map.put(to.getName(), from.getName());
	}
	
	public ProxiedPlayer getReplier(ProxiedPlayer player) {
		return ProxyServer.getInstance().getPlayer(map.get(player.getName()));
	}
	
	public String getReplierName(ProxiedPlayer player) {
		return map.get(player.getName());
	}
	
	public boolean hasReplier(ProxiedPlayer player) {
		return map.containsKey(player.getName());
	}
	
	public boolean isReplierOnline(ProxiedPlayer player) {
		return (hasReplier(player) && getReplier(player) != null && getReplier(player).isConnected());
	}
	
}
