package fr.royalpha.bungeeannounce.announcement.announce;

import fr.royalpha.bungeeannounce.handler.AnnounceAction;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Royalpha
 */
public class GlobalAnnounceAction implements AnnounceAction {

	@Override
	public void onAction(ProxiedPlayer player, TextComponent message, Integer... optionalTitleArgs) {
		player.sendMessage(message);
	}

	@Override
	public void onAction(final ProxiedPlayer player, final TextComponent[] messages, final Integer... optionalTitleArgs) {
		for(TextComponent message: messages){
			onAction(player,message,optionalTitleArgs);
		}
	}
}
