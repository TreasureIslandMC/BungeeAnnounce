package fr.royalpha.bungeeannounce.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;

import fr.royalpha.bungeeannounce.BungeeAnnouncePlugin;
import fr.royalpha.bungeeannounce.handler.Executor;
import fr.royalpha.bungeeannounce.manager.AnnouncementManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Royalpha
 */
public class BAUtils {

	public static String separator = "::";
	public static String splittedSeparator = ":";

	public static TextComponent parse(String input) {
		String used = colorizz(input);

		if (!isNecessaryToParse(used))
			return new TextComponent(used);

		ArrayList<TextComponent> outputList = new ArrayList<>();
		String[] split = used.split(" ");
		for (final String s : split) {
			outputList.add(new TextComponent(s));
		}

		TextComponent output = new TextComponent("");

		for (int nbr = 0; nbr < outputList.size(); nbr++) {
			TextComponent in = outputList.get(nbr);
			TextComponent out = new TextComponent("");
			StringBuilder valueBuilder = new StringBuilder("");
			String text = in.getText();
			if (nbr > 0)
				out.addExtra(" ");
			if (isNecessaryToParse(text)) {
				String[] txtSplit = text.split(separator);
				if (valueBuilder.toString().trim().equals("")) {
					for (int i = 2; i < txtSplit.length; i++) {
						if (txtSplit[i] != null) {
							valueBuilder.append(i > 2 ? separator : "").append(txtSplit[i]);
							continue;
						}
						break;
					}
				}
				String value = valueBuilder.toString().trim();
				out.addExtra((txtSplit[0].replace('_', ' ')));
				for (Executor exec : Executor.values()) {
					if (txtSplit[1].equals(exec.getString())) {
						exec.getEA().onParse(out, value);
					}
				}
			} else {
				out.addExtra(text);
			}
			output.addExtra(out);
		}
		return output;
	}

	private static Boolean isNecessaryToParse(String s) {
		for (Executor exec : Executor.values()) {
			if (s.contains(separator + exec.getString() + separator))
				return true;
		}
		return false;
	}

	public static String colorizz(String uncolorizedString) {
		String[] split = uncolorizedString.split("");
		ArrayList<String> bigSplit = new ArrayList<>();
		Collections.addAll(bigSplit, split);

		StringBuilder output = new StringBuilder();

		StringBuilder color = new StringBuilder();
		for (int i = 0; i < bigSplit.size(); i++) {
			String str = bigSplit.get(i);
			if (equals(separator, bigSplit, i)) {
				int jump = jumpAfterNextSeparator(" ", bigSplit, i);
				for (int j = i; j <= jump; j++)
					output.append(bigSplit.get(j));
				i = jump;
				continue;
			} else if (equals("[lang]", bigSplit, i)) {
				int jump = jumpAfterNextSeparator("[/lang]", bigSplit, i);
				for (int j = i; j <= jump; j++)
					output.append(bigSplit.get(j));
				i = jump;
				continue;
			} else if (equals("[ln]", bigSplit, i)) {
				output.append("[ln]");
				i += 3;
				continue;
			}
			if (str.equals("&")) {
				if (i > 1 && !isCombiningColors(bigSplit, i)) {
					color = new StringBuilder();
				}
				color.append(str);
			} else if (isWaitingForColor(color)) {
				color.append(str);
			} else {
				output.append(color.toString().trim()).append(str);
			}
		}
		return ChatColor.translateAlternateColorCodes('&', (output.toString().trim().replace("[ln]", "\n")));
	}

	private static boolean isWaitingForColor(StringBuilder builder) {
		String[] split = builder.toString().trim().split("");
		return split[split.length - 1].equals("&");
	}

	private static boolean isCombiningColors(ArrayList<String> split, int index) {
		return split.get(index - 2).equals("&");
	}

	private static int jumpAfterNextSeparator(String separator, ArrayList<String> split, int index) {
		index++;
		for (int i = index; i < split.size(); i++)
			if (equals(separator, split, i))
				return i + (separator.length() - 1);
		return (split.size() - 1);
	}

	private static boolean equals(String word, ArrayList<String> sentence, int i) {
		int length = word.length();
		if (length + i > sentence.size())
			return false;
		String[] split = word.split("");
		for (int x = 0; x < split.length; x++)
			if (!sentence.get(i + x).equals(split[x]))
				return false;
		return true;
	}

	public static String translatePlaceholders(String input, CommandSender sender, ProxiedPlayer receiver,
			ProxiedPlayer player) {
		String output = input;
		if (receiver != null) {
			output = output.replaceAll("%RECEIVER_NAME%", receiver.getName());
			output = output.replaceAll("%RECEIVER_DISPLAY_NAME%", receiver.getDisplayName());
			output = output.replaceAll("%RECEIVER_PING%", receiver.getPing() + "");
			output = output.replaceAll("%RECEIVER_UUID%", receiver.getUniqueId().toString());

			if (receiver.getServer() != null && receiver.getServer().getInfo() != null) {
				ServerInfo server = receiver.getServer().getInfo();
				output = output.replaceAll("%RECEIVER_SERVER_NAME%", server.getName());
				output = output.replaceAll("%RECEIVER_SERVER_MOTD%", server.getMotd());
				output = output.replaceAll("%RECEIVER_SERVER_ONLINE_PLAYERS%", server.getPlayers().size() + "");
			}
			
			if (player == null) {
				player = receiver;
			}
		} else {
			output = output.replaceAll("%RECEIVER_NAME%", "unknown");
			output = output.replaceAll("%RECEIVER_DISPLAY_NAME%", "unknown");
			output = output.replaceAll("%RECEIVER_PING%", "-1");
			output = output.replaceAll("%RECEIVER_UUID%", "unknown");
			
			output = output.replaceAll("%RECEIVER_SERVER_NAME%", "unknown");
			output = output.replaceAll("%RECEIVER_SERVER_MOTD%", "unknown");
			output = output.replaceAll("%RECEIVER_SERVER_ONLINE_PLAYERS%", "-1");
		}
		
		if (player != null) {
			output = output.replaceAll("%PLAYER_NAME%", player.getName());
			output = output.replaceAll("%PLAYER_DISPLAY_NAME%", player.getDisplayName());
			output = output.replaceAll("%PLAYER_PING%", player.getPing() + "");
			output = output.replaceAll("%PLAYER_UUID%", player.getUniqueId().toString());

			if (player.getServer() != null && player.getServer().getInfo() != null) {
				ServerInfo server = player.getServer().getInfo();
				output = output.replaceAll("%PLAYER_SERVER_NAME%", server.getName());
				output = output.replaceAll("%PLAYER_SERVER_MOTD%", server.getMotd());
				output = output.replaceAll("%PLAYER_SERVER_ONLINE_PLAYERS%", server.getPlayers().size() + "");
				output = output.replaceAll("%SERVER_NAME%", server.getName());
				output = output.replaceAll("%SERVER_MOTD%", server.getMotd());
				output = output.replaceAll("%SERVER_ONLINE_PLAYERS%", server.getPlayers().size() + "");
			}
		} else {
			output = output.replaceAll("%PLAYER_NAME%", "unknown");
			output = output.replaceAll("%PLAYER_DISPLAY_NAME%", "unknown");
			output = output.replaceAll("%PLAYER_PING%", "-1");
			output = output.replaceAll("%PLAYER_UUID%", "unknown");
			
			output = output.replaceAll("%PLAYER_SERVER_NAME%", "unknown");
			output = output.replaceAll("%PLAYER_SERVER_MOTD%", "unknown");
			output = output.replaceAll("%PLAYER_SERVER_ONLINE_PLAYERS%", "-1");
			output = output.replaceAll("%SERVER_NAME%", "unknown");
			output = output.replaceAll("%SERVER_MOTD%", "unknown");
			output = output.replaceAll("%SERVER_ONLINE_PLAYERS%", "-1");
		}
		
		if (sender != null) {
			output = output.replaceAll("%SENDER_NAME%", sender.getName());
			
			if (sender instanceof ProxiedPlayer) {
				ProxiedPlayer senderPlayer = (ProxiedPlayer) sender;
				
				output = output.replaceAll("%SENDER_DISPLAY_NAME%", senderPlayer.getDisplayName());
				output = output.replaceAll("%SENDER_PING%", senderPlayer.getPing() + "");
				output = output.replaceAll("%SENDER_UUID%", senderPlayer.getUniqueId().toString());

				if (senderPlayer.getServer() != null && senderPlayer.getServer().getInfo() != null) {
					ServerInfo server = senderPlayer.getServer().getInfo();
					output = output.replaceAll("%SENDER_SERVER_NAME%", server.getName());
					output = output.replaceAll("%SENDER_SERVER_MOTD%", server.getMotd());
					output = output.replaceAll("%SENDER_SERVER_ONLINE_PLAYERS%", server.getPlayers().size() + "");
				}
			} else {
				output = output.replaceAll("%SENDER_DISPLAY_NAME%", "unknown");
				output = output.replaceAll("%SENDER_PING%", "-1");
				output = output.replaceAll("%SENDER_UUID%", "unknown");
				
				output = output.replaceAll("%SENDER_SERVER_NAME%", "unknown");
				output = output.replaceAll("%SENDER_SERVER_MOTD%", "unknown");
				output = output.replaceAll("%SENDER_SERVER_ONLINE_PLAYERS%", "-1");
			}
		}
		
		output = output.replaceAll("%BUNGEE_ONLINE_PLAYERS%", BungeeAnnouncePlugin.getProxyServer().getOnlineCount() + "");
		return output;
	}

	public static Integer[] getOptionalTitleArgsFromConfig(AnnouncementManager announcement, String rawType) {
		Integer[] emptyOutput = {};
		if (announcement == AnnouncementManager.TITLE || announcement == AnnouncementManager.SUBTITLE) {
			String[] splittedRawType = rawType.split("_");
			if (splittedRawType.length >= 4) {
				int fadeIn;
				int stay;
				int fadeOut;
				try {
					fadeIn = Integer.parseInt(splittedRawType[1]) * 20;
					stay = Integer.parseInt(splittedRawType[2]) * 20;
					fadeOut = Integer.parseInt(splittedRawType[3]) * 20;
					return new Integer[]{ fadeIn, stay, fadeOut };
				} catch (NumberFormatException e) {
					return emptyOutput;
				}
			}
		}
		return emptyOutput;
	}
}
