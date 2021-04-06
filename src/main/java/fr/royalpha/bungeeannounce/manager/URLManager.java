package fr.royalpha.bungeeannounce.manager;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import net.md_5.bungee.api.plugin.Plugin;

/**
 * @author Royalpha
 */
public class URLManager {

	private URL url;
	private static String latestVersion;

	static {
		latestVersion = "null";
	}

	public enum Link {

		GITHUB_PATH("https://royalphax.github.io/BungeeAnnounce/auto-updater"), 
		BUNGEE_ANNOUNCE_PATH("http://royalphax.ddns.net/home/projects/plugins/Bungee_Announce");

		private final String url;

		Link(String url) {
			this.url = url;
		}

		public String getURL() {
			return this.url;
		}
	}
	
	public URLManager(Link link, Boolean localhost) throws MalformedURLException {
		this(link.getURL(), localhost);
	}

	public URLManager(String url, Boolean localhost) throws MalformedURLException {
		String urlCopy = url;
		String[] urlSplit = url.split("/");
		if (localhost && (!urlSplit[2].equals("localhost"))) {
			urlCopy = urlCopy.replaceAll(urlSplit[2], "localhost");
		}
		for (Link link : Link.values()) {
			if (urlCopy.contains("%" + link.toString() + "%"))
				urlCopy = urlCopy.replaceAll("%" + link.toString() + "%", link.getURL());
		}
		this.url = new URL(urlCopy);
	}

	public String read() throws IOException {
		URLConnection con = url.openConnection();
		con.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");

		InputStream in = con.getInputStream();
		String encoding = con.getContentEncoding();
		encoding = encoding == null ? "UTF-8" : encoding;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		int len;
		while ((len = in.read(buf)) != -1) {
			baos.write(buf, 0, len);
		}
		return baos.toString(encoding);
	}

	public void download(Plugin plugin, String newVersion) {
		plugin.getLogger().info("Updating " + plugin.getDescription().getName() + " ...");
		FileOutputStream fos = null;
		try {
			ReadableByteChannel rbc = Channels.newChannel(this.url.openStream());
			fos = new FileOutputStream("plugins/" + plugin.getDescription().getName() + "-" + newVersion + ".jar");
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		} catch (IOException e) {
			plugin.getLogger().warning("Update aborted: " + e.getMessage());
		} finally {
			try {
				fos.close();
				plugin.getLogger().info(plugin.getDescription().getName() + " is now up to date.");
				plugin.getFile().deleteOnExit();
			} catch (NullPointerException | IOException e) {
				plugin.getLogger().warning("Update aborted: " + e.getMessage());
			}
		}
	}

	public static Boolean checkVersion(String version, Boolean localhost, Link URLPath) {
		Boolean isUpdated = true;
		String content;
		try {
			content = new URLManager(URLPath.getURL() + "/version.txt", localhost).read();
			if (!content.trim().equals(version.trim())) {
				latestVersion = content.trim();
				isUpdated = false;
			}
		} catch (IOException e) {
			return true;
		}
		return isUpdated;
	}

	public static String getLatestVersion() {
		return latestVersion;
	}

	public static void update(Plugin plugin, String newVersion, Boolean localhost, Link URLPath) {
		try {
			new URLManager(URLPath.getURL() + "/latest.jar", localhost).download(plugin, newVersion.trim());
		} catch (MalformedURLException e) {
			plugin.getLogger().warning("Update aborted: " + e.getMessage());
		}
	}
	
	public static URLManager getContentURL(String url, boolean localhost) throws IOException {
		URLManager urlManager = new URLManager(url, localhost);
		String newURL = urlManager.read().trim();
		return new URLManager(newURL, localhost);
	}
}
