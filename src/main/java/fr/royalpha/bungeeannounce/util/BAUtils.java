package fr.royalpha.bungeeannounce.util;

import java.util.ArrayList;
import java.util.Collections;

import fr.royalpha.bungeeannounce.BungeeAnnouncePlugin;
import fr.royalpha.bungeeannounce.handler.Executor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author Royalpha
 */
public class BAUtils {

	public static final String SEPARATOR = "::";

	public static TextComponent[] parse(String[] input) {
		TextComponent[] components = new TextComponent[input.length];
		for(int i = 0; i < components.length - 1; i++) {
			components[i] = parse(input[i]);
		}
		return components;
	}
	public static TextComponent parse(String input) {
		String used = BAUtils.sendCenteredMessage(colorize(input));

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
			StringBuilder valueBuilder = new StringBuilder();
			String text = in.getText();
			if (nbr > 0)
				out.addExtra(" ");
			if (isNecessaryToParse(text)) {
				String[] txtSplit = text.split(SEPARATOR);
				if (valueBuilder.toString().trim().equals("")) {
					for (int i = 2; i < txtSplit.length; i++) {
						if (txtSplit[i] != null) {
							valueBuilder.append(i > 2 ? SEPARATOR : "").append(txtSplit[i]);
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
			if (s.contains(SEPARATOR + exec.getString() + SEPARATOR))
				return true;
		}
		return false;
	}

	public static String colorize(String uncolorizedString) {
		String[] split = uncolorizedString.split("");
		ArrayList<String> bigSplit = new ArrayList<>();
		Collections.addAll(bigSplit, split);

		StringBuilder output = new StringBuilder();

		StringBuilder color = new StringBuilder();
		for (int i = 0; i < bigSplit.size(); i++) {
			String str = bigSplit.get(i);
			if (equals(SEPARATOR, bigSplit, i)) {
				int jump = jumpAfterNextSeparator(" ", bigSplit, i);
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

	public static String[] splitIntoArray(final String colorizedString) {
		return colorizedString.split("\n");
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
			output = output.replace("%RECEIVER_NAME%", receiver.getName());
			output = output.replace("%RECEIVER_DISPLAY_NAME%", receiver.getDisplayName());
			output = output.replace("%RECEIVER_PING%", receiver.getPing() + "");
			output = output.replace("%RECEIVER_UUID%", receiver.getUniqueId().toString());

			if (receiver.getServer() != null && receiver.getServer().getInfo() != null) {
				ServerInfo server = receiver.getServer().getInfo();
				output = output.replace("%RECEIVER_SERVER_NAME%", server.getName());
				output = output.replace("%RECEIVER_SERVER_MOTD%", server.getMotd());
				output = output.replace("%RECEIVER_SERVER_ONLINE_PLAYERS%", server.getPlayers().size() + "");
			}
			
			if (player == null) {
				player = receiver;
			}
		} else {
			output = output.replace("%RECEIVER_NAME%", "unknown");
			output = output.replace("%RECEIVER_DISPLAY_NAME%", "unknown");
			output = output.replace("%RECEIVER_PING%", "-1");
			output = output.replace("%RECEIVER_UUID%", "unknown");
			
			output = output.replace("%RECEIVER_SERVER_NAME%", "unknown");
			output = output.replace("%RECEIVER_SERVER_MOTD%", "unknown");
			output = output.replace("%RECEIVER_SERVER_ONLINE_PLAYERS%", "-1");
		}
		
		if (player != null) {
			output = output.replace("%PLAYER_NAME%", player.getName());
			output = output.replace("%PLAYER_DISPLAY_NAME%", player.getDisplayName());
			output = output.replace("%PLAYER_PING%", player.getPing() + "");
			output = output.replace("%PLAYER_UUID%", player.getUniqueId().toString());

			if (player.getServer() != null && player.getServer().getInfo() != null) {
				ServerInfo server = player.getServer().getInfo();
				output = output.replace("%PLAYER_SERVER_NAME%", server.getName());
				output = output.replace("%PLAYER_SERVER_MOTD%", server.getMotd());
				output = output.replace("%PLAYER_SERVER_ONLINE_PLAYERS%", server.getPlayers().size() + "");
				output = output.replace("%SERVER_NAME%", server.getName());
				output = output.replace("%SERVER_MOTD%", server.getMotd());
				output = output.replace("%SERVER_ONLINE_PLAYERS%", server.getPlayers().size() + "");
			}
		} else {
			output = output.replace("%PLAYER_NAME%", "unknown");
			output = output.replace("%PLAYER_DISPLAY_NAME%", "unknown");
			output = output.replace("%PLAYER_PING%", "-1");
			output = output.replace("%PLAYER_UUID%", "unknown");
			
			output = output.replace("%PLAYER_SERVER_NAME%", "unknown");
			output = output.replace("%PLAYER_SERVER_MOTD%", "unknown");
			output = output.replace("%PLAYER_SERVER_ONLINE_PLAYERS%", "-1");
			output = output.replace("%SERVER_NAME%", "unknown");
			output = output.replace("%SERVER_MOTD%", "unknown");
			output = output.replace("%SERVER_ONLINE_PLAYERS%", "-1");
		}
		
		if (sender != null) {
			output = output.replace("%SENDER_NAME%", sender.getName());
			
			if (sender instanceof ProxiedPlayer senderPlayer) {
				
				output = output.replace("%SENDER_DISPLAY_NAME%", senderPlayer.getDisplayName());
				output = output.replace("%SENDER_PING%", senderPlayer.getPing() + "");
				output = output.replace("%SENDER_UUID%", senderPlayer.getUniqueId().toString());

				if (senderPlayer.getServer() != null && senderPlayer.getServer().getInfo() != null) {
					ServerInfo server = senderPlayer.getServer().getInfo();
					output = output.replace("%SENDER_SERVER_NAME%", server.getName());
					output = output.replace("%SENDER_SERVER_MOTD%", server.getMotd());
					output = output.replace("%SENDER_SERVER_ONLINE_PLAYERS%", server.getPlayers().size() + "");
				}
			} else {
				output = output.replace("%SENDER_DISPLAY_NAME%", "unknown");
				output = output.replace("%SENDER_PING%", "-1");
				output = output.replace("%SENDER_UUID%", "unknown");
				
				output = output.replace("%SENDER_SERVER_NAME%", "unknown");
				output = output.replace("%SENDER_SERVER_MOTD%", "unknown");
				output = output.replace("%SENDER_SERVER_ONLINE_PLAYERS%", "-1");
			}
		}
		
		output = output.replace("%BUNGEE_ONLINE_PLAYERS%", BungeeAnnouncePlugin.getProxyServer().getOnlineCount() + "");
		return output;
	}

	private static final int CENTER_PX = 154;

	public static String sendCenteredMessage(String message){
		message = ChatColor.translateAlternateColorCodes('&', message);

		int messagePxSize = 0;
		boolean previousCode = false;
		boolean isBold = false;

		for(char c : message.toCharArray()){
			if(c == '§'){
				previousCode = true;
			}else if(previousCode){
				previousCode = false;
				isBold = c == 'l' || c == 'L';
			}else{
				DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
				messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
				messagePxSize++;
			}
		}

		int halvedMessageSize = messagePxSize / 2;
		int toCompensate = CENTER_PX - halvedMessageSize;
		int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
		int compensated = 0;
		StringBuilder sb = new StringBuilder();
		while(compensated < toCompensate){
			sb.append(" ");
			compensated += spaceLength;
		}
		return sb + message;
	}

}
