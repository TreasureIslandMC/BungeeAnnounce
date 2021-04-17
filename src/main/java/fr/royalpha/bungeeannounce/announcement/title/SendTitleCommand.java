package fr.royalpha.bungeeannounce.announcement.title;

import fr.royalpha.bungeeannounce.manager.AnnouncementManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Royalpha
 */
public class SendTitleCommand extends Command {

	public SendTitleCommand() {
		super("sendtitle", "bungeecord.command.sendtitle", "bungee:sendtitle");
	}

	public void execute(CommandSender sender, String[] args) {
		if (args.length < 4) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /sendtitle <fadeIn> <stay> <fadeOut> <title>"));
			return;
		}
		int fadeIn;
		int stay;
		int fadeOut;
		try {
		    fadeIn = Integer.parseInt(args[0])*20;
		    stay = Integer.parseInt(args[1])*20;
		    fadeOut = Integer.parseInt(args[2])*20;
		} catch (NumberFormatException e) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /sendtitle <fadeIn> <stay> <fadeOut> <title>"));
			return;
		}
		
		StringBuilder titleBuilder = new StringBuilder();
		for (int i = 3; i < args.length; i++) {
			titleBuilder.append(args[i]).append(" ");
		}
		AnnouncementManager.sendToServer(AnnouncementManager.TITLE, sender instanceof ProxiedPlayer ? (ProxiedPlayer) sender : null, titleBuilder.toString().trim(), null, true, "", fadeIn, stay, fadeOut);
	}
}
