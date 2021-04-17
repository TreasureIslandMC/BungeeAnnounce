package fr.royalpha.bungeeannounce.command;

import fr.royalpha.bungeeannounce.manager.ChannelManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Royalpha
 */
public class ChannelCommand extends Command {

	public ChannelManager channel;

	public ChannelCommand(ChannelManager channel) {
		super(channel.getName().replaceAll(" ", "_").toLowerCase(), channel.getPermission(), channel.getCommand());
		this.channel = channel;
	}

	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "You need to be a proxied player !"));
			return;
		}

		ProxiedPlayer player = (ProxiedPlayer) sender;
		if (args.length == 0) {
			if (this.channel.hasPlayer(player)) {
				this.channel.leftPlayer(player);
			} else {
				this.channel.joinPlayer(player);
			}
		} else {
			if (!this.channel.hasPlayer(player))
				this.channel.joinPlayer(player);
			StringBuilder msgBuilder = new StringBuilder();
			for (final String arg : args) msgBuilder.append(arg).append(" ");
			this.channel.sendMessage(player, msgBuilder.toString().trim());
		}

	}
}
