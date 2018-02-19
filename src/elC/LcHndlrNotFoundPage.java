package elC;
/**
 * @(#)SolarLoggerController: LcHndlrNotFoundPage.java
 * Send '404 Not Found' response to client using 200 header.
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LcHndlrNotFoundPage extends LcHndlr
{
	public LcHndlrNotFoundPage(Socket iPclient, int iPClientNo, LcParams iPlcp, Logger logger)
	{
		super(iPclient, iPClientNo, iPlcp, logger);
	}

	protected boolean doPage(BufferedWriter response, String txt) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		sb.setLength(0);
		constructResponseHeader(200, sb);
		response.write(sb.toString());
		response.newLine();
		LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections"))?Level.INFO:Level.FINEST),
			"Client#: " + Integer.toString(clientNo) + ", INDEX requested, sent 200 header, about to send page and end...");
		response.write(mkPageStart(0, LocalDate.now(), ""));
		response.write("<!DOCTYPE html><html>" + "<head>" + "<body>HTTP/1.1 404 NotFound.<BR>" + txt
				+ "<BR></body>\n" + "</head></html>\n");
		response.write(mkPageEnd(0, ""));
		response.newLine();
		response.flush();
		LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections"))?Level.INFO:Level.FINEST),
				"LcHndlrNotFoundPage: client#: " + Integer.toString(clientNo) + ", ################### NOT FOUND DONE ##################\n");
		return true;
	}
}


