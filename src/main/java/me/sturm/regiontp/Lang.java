package me.sturm.regiontp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public enum Lang {
	
	CMD_SHOW("&d[Клик]"),
	NO_PERMS("#eb4934Нет прав"),
	ONLY_PLAYER("#eb4934Only player command"),
	RG_MANAGER_NF("#eb3443Менеджер регионов не найден"),
	REGION_NF("#eb3443Регион #d8eb34%region% #eb3443не найден"),
	PLAYER_NF("#eb3443Игрок #d8eb34%player% #eb3443не найден"),
	HELP("#34e8eb/rgtp <регион> [игрок (опционально)]"),
	TELEPORT("#34eb5bВы телепортированы в регион #d8eb34%region%");

	private String text;
	
	Lang(String text) {this.text = text;}

	public static YamlConfiguration setMessages(YamlConfiguration c) {
		for (Lang sl : Lang.values()) {
			String path = sl.toString().replaceAll("_", "-").toLowerCase();
			String s = c.getString(path);
			if (s != null) sl.text = s;
			else c.set(path, sl.text);
		}
		return c;
	}
	
	public static void load(File lang) {
		if (!lang.exists()) {
			try {lang.createNewFile();} 
			catch (IOException e) {e.printStackTrace();}
		}
		YamlConfiguration l = YamlConfiguration.loadConfiguration(lang);
		l = setMessages(l);
		try {l.save(lang);} 
		catch (IOException e) {e.printStackTrace();}
	}
	
	public static void init(Plugin pl) {
		File f = new File(pl.getDataFolder() + File.separator + "lang.yml");
		f.getParentFile().mkdir();
		if (!f.exists())
			try {f.createNewFile();} 
			catch (IOException e) {e.printStackTrace();}
		load(f);
	}
	
	public String text() {return this.text;}
	public List<String> listText() {
		List<String> ret = new ArrayList<>();
		for (String s : this.text.split(Pattern.compile("\\\\") + "n")) ret.add(s);
		return ret;
	}
	public Component colorText() {return color(this.text);}

	public Component getWithCommand(String command) {
		Component result = colorText();
		result = result.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command));
		result = result.hoverEvent(Lang.CMD_SHOW.colorText());
		return result;
	}

	public boolean sendMessage(CommandSender s) {
		if (s == null || text.equals("null")) return true;
		s.sendMessage(color(text));
		return true;
	}
	
	public boolean sendMessage(CommandSender s, String from, String to) {
		if (s == null || text.equals("null")) return true;
		s.sendMessage(color(text.replaceAll(from, to)));
		return true;
	}
	
	public boolean sendMessage(CommandSender s, String from1, String to1, String from2, String to2) {
		if (s == null || text.equals("null")) return true;
		s.sendMessage(color(text.replaceAll(from1, to1).replaceAll(from2, to2)));
		return true;
	}
	
	public boolean sendMessage(CommandSender s, String[] from, String[] to) {
		if (s == null || text.equals("null")) return true;
		if (from.length != to.length) return true;
		String mes = this.text;
		for (int i = 0; i < from.length; i++) 
			mes = mes.replaceAll(from[i], to[i]);
		s.sendMessage(color(mes));
		return true;
	}

	public static String decolor(Component from) {
		return LegacyComponentSerializer.legacySection().serialize(from);
	}
	
	public static Component color(String from) {
		from = from.replaceAll("&", "§");
		String text = "";
		int c = from.length();
		for (int i = 0; i < c; i++) {
			char ch = from.charAt(i);
			if (ch == '#') text += "§";
			text += String.valueOf(ch);
		}
		Component comp = LegacyComponentSerializer.legacySection().deserialize(text);
		TextReplacementConfig.Builder b1 = TextReplacementConfig.builder().match(Pattern.compile("§"))
				.replacement(x -> Component.empty());
		TextReplacementConfig.Builder b2 = TextReplacementConfig.builder().match(Pattern.compile("\\\\") + "n")
				.replacement(x -> Component.newline());
		TextReplacementConfig.Builder b3 = TextReplacementConfig.builder().match(Pattern.compile("/.*" + Pattern.compile("\\\\")))
				.replacement(url -> 
					Component.text(url.content().substring(0, url.content().length()-1))
					.clickEvent(ClickEvent.runCommand(url.content().substring(0, url.content().length()-1)))
					.hoverEvent(HoverEvent.showText(CMD_SHOW.colorText()))
				);
		return comp.replaceText(b1.build()).replaceText(b2.build()).replaceText(b3.build()).decoration(TextDecoration.ITALIC, false);
	}
	
	public static List<Component> color(List<String> from) {
		List<Component> ret = new ArrayList<>();
		from.forEach(x -> ret.add(color(x)));
		return ret;
	}

}
