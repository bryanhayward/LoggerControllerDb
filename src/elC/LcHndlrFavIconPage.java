package elC;
/**
 * @(#)LoggerController: LcHndlrFavIconPage.java
 * Handle requests for faviconi.ico from the web
 * received via LcSvr
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LcHndlrFavIconPage extends LcHndlr
{
	public LcHndlrFavIconPage(Socket iPclient, int iPClientNo, LcParams iPlcp, Logger logger)
	{
		super(iPclient, iPClientNo, iPlcp, logger);
	}

	protected byte[] getFavicon(File favFile) throws IOException
	{
		if (!favFile.exists()) return null;
		byte favBytes[] = Files.readAllBytes(favFile.toPath());
		return favBytes;
	}
	
	protected boolean doPage(BufferedWriter response) throws IOException
	{
			/*********************** FAVICON *********************************/
		int i = 0;
		File favFile = new File(lcp.iniFilePath.substring(0, lcp.iniFilePath.replace("\\", "/").lastIndexOf("/") + 1) + "Favicon.ico");
		StringBuilder sb2 = new StringBuilder();
		sb2.setLength(0);
		StringBuilder sb = new StringBuilder();
		sb.setLength(0);
		byte[] favicon = getFavicon(favFile);
		sb.append("HTTP/1.1 200 OK\r\n");
		sb.append("Accept-Ranges: bytes\r\n");
		sb.append("Content-Length: " + Long.toString(favFile.length()) + "\r\n");
		sb.append("Content-Type: image/x-icon\r\n");
		sb.append("Date: " + getTimeStamp() + "\r\n");
		sb.append("Server: localhost\r\n");
		sb.append("Last Modified: " + getTimeStamp(favFile.lastModified()) + "\r\n"); // 
		response.write(sb.toString());
		response.newLine();
		response.flush();
		LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections"))?Level.INFO:Level.FINEST),
				"Client#: " + Integer.toString(clientNo) + ", FAVICON requested, sent 200 header, about to send ico and end...");
		DataOutputStream dos = new DataOutputStream(client.getOutputStream());
		if (favicon != null)
			while ( i < favicon.length) dos.writeByte(favicon[i++]);
		response.newLine();
		response.flush();
		LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections"))?Level.INFO:Level.FINEST),
				"Client#: " + Integer.toString(clientNo) + ", ################## FAVICON DONE ###################\n");
		return true;
	}
}
