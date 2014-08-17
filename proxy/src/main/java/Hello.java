import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class Hello extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		//res.setContentType("text/html");
		String proxyUrl = "http://localhost:8080/proxy/hello?name=";
		String baseUrl = req.getParameter("name");
		String action = req.getParameter("action");
		// It's a form submission so set the base url to the action
		if(action != null) {
			baseUrl = action+"?"+req.getQueryString();
			baseUrl = baseUrl.replaceAll("action=.+?&", "&");
		}
		
		if (baseUrl.contains("/")) {
			baseUrl = URLEncoder.encode(baseUrl, "UTF-8");
		}
		String decodedBaseUrl = URLDecoder.decode(baseUrl, "UTF-8");
		baseUrl = baseUrl.replaceAll("(.*%2F%2F.*%2F).*", "$1");
		URL url = new URL(decodedBaseUrl);
		InputStream in = url.openStream();

		try {
			byte[] webpage = IOUtils.toByteArray(in);

			if (isText(webpage)) {
				StringBuffer webpageBuffer = new StringBuffer(new String(
						webpage));
				Pattern pattern = Pattern.compile(
						"(?<=(src|href)=\").+?(?=\")|(?<=url\\().+?(?=\\))",
						Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(webpageBuffer);
				int offset = 0;
				while (matcher.find(offset)) {
					String resource;
					if(matcher.group().contains("http")) {
						resource = matcher.group();
					} else if(matcher.group().startsWith("/")){
						resource = url.getProtocol()+"://"+url.getAuthority();
						resource += matcher.group();
					} else {
						resource = url.getProtocol()+"://"+url.getAuthority()+"/";
						String path = url.getPath().substring(0, url.getPath().lastIndexOf("/"));
						resource += path+ "/" + matcher.group();
					}
					resource = URLEncoder.encode(resource, "UTF-8");
					webpageBuffer.replace(matcher.start(), matcher.end(),
							proxyUrl + resource);
					offset = matcher.start() + proxyUrl.length()
							+ resource.length();
				}
				pattern = Pattern.compile(
						"<form.+?action=\"(.+?)\".*?>(.)",
						Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
				matcher = pattern.matcher(webpageBuffer);
				offset = 0;
				while (matcher.find(offset)) {
					String resource;
					if(matcher.group(1).contains("http")) {
						resource = matcher.group(1);
					} else if(matcher.group(1).startsWith("/")){
						resource = url.getProtocol()+"://"+url.getAuthority();
						resource += matcher.group(1);
					} else {
						resource = url.getProtocol()+"://"+url.getAuthority()+"/";
						String path = url.getPath().substring(0, url.getPath().lastIndexOf("/"));
						resource += path+ "/" + matcher.group(1);
					}
					resource = URLEncoder.encode(resource, "UTF-8");
					String actionHtml = "<input name=\"action\" value=\""+resource+"\"/>"+matcher.group(2);
					webpageBuffer.replace(matcher.start(2), matcher.end(2),
							actionHtml);
					webpageBuffer.replace(matcher.start(1), matcher.end(1),
							proxyUrl);
					offset = matcher.start(2);
				}
				
				String webpageString = webpageBuffer.toString();
				webpageString = webpageString.replaceAll("Alison Keenor",
						"Peeley Wheeler");
				//webpageString = webpageString.replaceAll("(?i)methord", "method");
				PrintWriter out = res.getWriter();
				out.print(webpageString);
			} else {
				OutputStream out = res.getOutputStream();
				out.write(webpage);

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(in);
		}
		/*
		 * out.println("<HTML>"); out.println("<HEAD><TITLE>Hello, " + name +
		 * "</TITLE></HEAD>"); out.println("<BODY>"); out.println("Hello, " +
		 * name); out.println("</BODY></HTML>");
		 */
	}
	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
    {
		this.doGet(req, resp);
    }

	public String getServletInfo() {
		return "A servlet that knows the name of the person to whom it's"
				+ "saying hello";
	}

	private boolean isText(byte[] data) throws Exception {
		String s = new String(data, "UTF-8");
		String s2 = s.replaceAll(
				"[a-zA-Z0-9ßöäü\\.\\*!\"§\\$\\%&/()=\\?@~'#:,;\\"
						+ "+><\\|\\[\\]\\{\\}\\^°²³\\\\ \\n\\r\\t_\\-`´âêîô"
						+ "ÂÊÔÎáéíóàèìòÁÉÍÓÀÈÌÒ©‰¢£¥€±¿»«¼½¾™ª]", "");
		// will delete all text signs

		double d = (double) (s.length() - s2.length()) / (double) (s.length());
		// percentage of text signs in the text
		return d > 0.95;
	}
}