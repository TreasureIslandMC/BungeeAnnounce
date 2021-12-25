package fr.royalpha.bungeeannounce.handler;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Royalpha
 */
public interface AnnounceAction {
	void onAction(ProxiedPlayer player, TextComponent message, Integer... optionalTitleArgs);
	void onAction(ProxiedPlayer player, TextComponent[] messages, Integer... optionalTitleArgs);
}
