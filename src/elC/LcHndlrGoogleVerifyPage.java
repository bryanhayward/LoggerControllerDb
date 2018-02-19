package elC;
/**
 * @(#)LoggerController: LcHndlrGoogleVerifyPage.java
 * Handle robots.txt web requests received via LcSvr
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 * 
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

public class LcHndlrGoogleVerifyPage extends LcHndlr
{
	public LcHndlrGoogleVerifyPage(Socket iPclient, int iPClientNo, LcParams iPlcp, Logger logger)
	{
		super(iPclient, iPClientNo, iPlcp, logger);
	}

	protected String getVerification() throws IOException
	{
		String path = lcp.iniFilePath.replace("\\", "/");
		path = path.substring(0, path.lastIndexOf("/"));
		File[] verificationFiles = new File(path).listFiles(file-> file.getName().startsWith("google")); // + "\\google*.html"
		if (verificationFiles.length < 1 || verificationFiles.length > 1)
		{
			LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections"))?Level.INFO:Level.SEVERE),
				"Client#: " + Integer.toString(clientNo) + ", GOOGLE VERIFICATION FAILED cannot find single 'google*.html' file...");
			return "";
		}
		File verificationFile = verificationFiles[0];
		if (!verificationFile.exists()) return "";
		List<String> lines = Files.readAllLines( verificationFile.toPath(),
                StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder(1024);
        lines.forEach(line -> sb.append(line + "\r\n"));
		String verification = sb.toString();
		return verification;
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
				"Client#: " + Integer.toString(clientNo) + ", GOOGLE VERIFICATION requested, sent 200 header, about to send page and end...");
		response.write(getVerification());
		response.newLine();
		response.flush();
		LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections"))?Level.INFO:Level.FINEST),
				"Client#: " + Integer.toString(clientNo) + ", ################## GOOGLE VERIFICATION DONE ###################\n");
		return true;
	}
}
