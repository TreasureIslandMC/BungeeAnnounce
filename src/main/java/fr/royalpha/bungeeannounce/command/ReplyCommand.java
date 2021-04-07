package fr.royalpha.bungeeannounce.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import fr.royalpha.bungeeannounce.BungeeAnnouncePlugin;
import fr.royalpha.bungeeannounce.manager.ConfigManager;
import fr.royalpha.bungeeannounce.manager.MsgManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Royalpha
 */
public class ReplyCommand extends BaseCommand {

	private final MsgManager msgManager;

	public ReplyCommand(MsgManager msgManager) {
		this.msgManager = msgManager;
	}

	@CommandAlias("reply|r|bungee:reply")
	@Default
	public void onReply(final ProxiedPlayer player, final String... message) {
		if (!this.msgManager.hasReplier(player)) {
			player.sendMessage(new TextComponent(ChatColor.RED + "You don't have any player to reply."));
			return;
		}

		if (!this.msgManager.isReplierOnline(player)) {
			player.sendMessage(new TextComponent(ConfigManager.Field.PM_PLAYER_NOT_ONLINE.getString().replaceAll("%PLAYER%", this.msgManager.getReplierName(player))));
			return;
		}


		ProxiedPlayer to = this.msgManager.getReplier(player);
		StringBuilder msgBuilder = new StringBuilder();
		for (final String arg : message) msgBuilder.append(arg).append(" ");
		if (msgBuilder.toString().trim().equals(""))
			return;
		this.msgManager.message(player, to, msgBuilder.toString());
	}

	@Deprecated
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) sender;
			if (args.length == 0) {
				sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /reply <msg>"));
				return;
			}
			if (!this.msgManager.hasReplier(player)) {
				player.sendMessage(new TextComponent(ChatColor.RED + "You don't have any player to reply."));
				return;
			}
			if (this.msgManager.isReplierOnline(player)) {
				ProxiedPlayer to = this.msgManager.getReplier(player);
				StringBuilder msgBuilder = new StringBuilder();
				for (final String arg : args) msgBuilder.append(arg).append(" ");
				if (msgBuilder.toString().trim().equals(""))
					return;
				this.msgManager.message(player, to, msgBuilder.toString());
			} else {
				player.sendMessage(new TextComponent(ConfigManager.Field.PM_PLAYER_NOT_ONLINE.getString().replaceAll("%PLAYER%", this.msgManager.getReplierName(player))));
			}
		} else {
			sender.sendMessage(new TextComponent(ChatColor.RED + "You need to be a proxied player !"));
		}
	}
}
