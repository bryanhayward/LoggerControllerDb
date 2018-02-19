package elC;
/**
 * @(#)LoggerController: LcHndlrRobotsPage.java
 * Handle robots.txt web requests received via LcSvr
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 * 
 * eg: reqdFile: &lt;/ROBOTS.TXT&gt;, path: &lt;&gt;, reqdParams: &lt;HTTP/1.1&gt;
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LcHndlrRobotsPage extends LcHndlr
{
	public LcHndlrRobotsPage(Socket iPclient, int iPClientNo, LcParams iPlcp, Logger logger)
	{
		super(iPclient, iPClientNo, iPlcp, logger);
	}

	protected String getRobots() throws IOException
	{
		File roboFile = new File(lcp.iniFilePath.substring(0, lcp.iniFilePath.replace("\\", "/").lastIndexOf("/") + 1) + "Robots.txt");
		if (!roboFile.exists()) return "";
		List<String> lines = Files.readAllLines( roboFile.toPath(),
                StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder(1024);
        lines.forEach(line -> sb.append(line + "\r\n"));
		String robots = sb.toString();
		return robots;
	}
	protected boolean doPage(BufferedWriter response) throws IOException
	{
			/*********************** FAVICON *********************************/
		StringBuilder sb = new StringBuilder();
		sb.setLength(0);
		constructResponseHeader(200, sb);
		response.write(sb.toString());
		response.newLine();
		LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections"))?Level.INFO:Level.FINEST),
				"Client#: " + Integer.toString(clientNo) + ", ROBOTS requested, sent 200 header, about to send page and end...");
		response.write(getRobots());
		response.newLine();
		response.flush();
		LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections"))?Level.INFO:Level.FINEST),
				"Client#: " + Integer.toString(clientNo) + ", ################## ROBOTS DONE ###################\n");
		return true;
	}
}
