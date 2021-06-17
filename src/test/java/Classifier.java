import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Arrays.asList;

public class Classifier {
	private static byte[] bytes = new byte[1024*16];

	private static List<Function<String, String>> rules = new ArrayList<>();

	static {
		rules.add(s->s.contains("---") ? "???" : null);
		rules.add(s->s.contains("<title>Screenwriter</title>") ? "TMS" : null);
		rules.add(s->s.contains("<p>The document has moved") ? "iDRAC" : null);
		rules.add(s->s.contains("\"name\":\"dcp2000\"") ? "DCP2000" : null);
		rules.add(s->s.contains("window.location.host + \"/en\" + window.location.search;") ? "redirect:/cgi-bin/en/about.cgi" : null);
		rules.add(s->s.contains("<!-- VERSION 2.3.8 -->") ? "redirect:/pages/home.html" : null);
		rules.add(s->s.contains("<!-- VERSION 2.2.7 -->") ? "redirect:/pages/home.html" : null);
		rules.add(s->s.contains("<!-- VERSION 2.2.6 -->") ? "redirect:/pages/home.html" : null);
		rules.add(s->s.contains("<!-- VERSION 2.2.5 -->") ? "redirect:/pages/home.html" : null);
		rules.add(s->s.contains("<th>Cisco Meraki cloud</th>") ? "Cisco Meraki" : null);
		rules.add(s->s.contains("GDC SX-3000 WebSMS") ? "GDC SX-3000 WebSMS" : null);
		rules.add(s->s.contains("Dolby Multichannel Amplifier DMA") ? "Dolby Multichannel Amplifier DMA" : null);
		rules.add(s->{
			int index = s.indexOf("&nbsp; type = ");
			if (index < 0) return null;
			return s.substring(index+14, s.indexOf("<br>",index));
		});
		rules.add(s->{
			int index = s.indexOf("<h1 class=\"featured-title\" id=\"homeDeviceType\">");
			if (index < 0) return null;
			return s.substring(index+47, s.indexOf("</h1>",index));
		});
	}

	public static void mainx(String[] args) {
		String[] split = "b\n------\na--\n".split("\n-+\n");
		for (String s : split) {
			System.out.println("*"+s);
		}
	}

	public static void maind(String[] args) throws IOException {
		List<String> services = Files.readAllLines(new File("network2.tsv").toPath());
		//services = asList("10.101.4.6\thttp");
		for (String service : services) {
			String[] split = service.split("\t");
			String ip = split[0];
			if (split.length > 1 && !hasHttp(split[1])) continue;
			String category = classify(new URL("http://" + ip));
			if (category == null) {
				System.out.println(ip);
				break;
			}
			else
				System.out.println(ip + "\t" + category);
		}
	}

	private static String classify(URL url) throws IOException {
		String content = read(url);
		String category = classify(content);
		if (category == null) {
			System.out.println(content);
			return null;
		}
		return category.startsWith("redirect:") ?
				classify(new URL(url + category.substring(9))) :
				category;
	}

	private static String classify(String content) {
		return rules.stream()
				.map(rule -> rule.apply(content))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}

	private static String read(URL url) throws IOException {
		try (InputStream is = new BufferedInputStream(url.openStream(),bytes.length)) {
			StringBuilder content = new StringBuilder();
			while (true) {
				int length = is.read(bytes);
				if (length < 0) break;
				content.append(new String(bytes, 0, length));
			}
			return content.toString();
		}
		catch (IOException e) {
			return "---";
		}
	}

	private static boolean hasHttp(String s1) {
		return asList(s1.split(" ")).contains("http");
	}
}
