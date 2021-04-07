package fr.royalpha.bungeeannounce.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import fr.royalpha.bungeeannounce.BungeeAnnouncePlugin;
import fr.royalpha.bungeeannounce.manager.ConfigManager;
import fr.royalpha.bungeeannounce.manager.MsgManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Royalpha
 */
public class MessageCommand extends BaseCommand {
	private final MsgManager msgManager;

	public MessageCommand(MsgManager msgManager) {
		this.msgManager = msgManager;
	}

	@CommandAlias("msg|bungee:msg")
	public void onMessage(final ProxiedPlayer sender, final ProxiedPlayer receiver, final String... message){
		StringBuilder msgBuilder = new StringBuilder();
		for(String msg: message)
			msgBuilder.append(msg).append(" ");
		if (msgBuilder.toString().trim().equals(""))
			return;
		this.msgManager.message(sender, receiver, msgBuilder.toString());
	}

	@Deprecated
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) sender;
			if (args.length == 0) {
				sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /" + " <player> <msg>"));
				return;
			}
			String name = args[0];
			if (ProxyServer.getInstance().getPlayer(name) != null) {
				ProxiedPlayer to = ProxyServer.getInstance().getPlayer(name);
				StringBuilder msgBuilder = new StringBuilder();
				for (int i = 1; i < args.length; i++)
					msgBuilder.append(args[i]).append(" ");
				if (msgBuilder.toString().trim().equals(""))
					return;
				this.msgManager.message(player, to, msgBuilder.toString());
			} else {
				player.sendMessage(new TextComponent(ConfigManager.Field.PM_PLAYER_NOT_ONLINE.getString().replaceAll("%PLAYER%", name)));
			}
		} else {
			sender.sendMessage(new TextComponent(ChatColor.RED + "You need to be a proxied player !"));
		}
	}
}
