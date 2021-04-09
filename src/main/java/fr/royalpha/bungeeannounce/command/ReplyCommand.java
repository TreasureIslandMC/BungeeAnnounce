package fr.royalpha.bungeeannounce.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import fr.royalpha.bungeeannounce.BungeeAnnouncePlugin;
import fr.royalpha.bungeeannounce.manager.ConfigManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author sarhatabaot
 */
public class ReplyCommand extends BaseCommand {
	private final BungeeAnnouncePlugin plugin;

	public ReplyCommand(final BungeeAnnouncePlugin plugin) {
		this.plugin = plugin;
	}

	@CommandAlias("reply|r")
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
}
