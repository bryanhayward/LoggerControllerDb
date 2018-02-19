package elC;
/**
 * @(#)LoggerController: LcHndlrErrorPage.java
 * Return database errors to client.
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

import java.io.BufferedWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LcHndlrErrorPage extends LcHndlr
{
	public LcHndlrErrorPage(Socket iPclient, int iPClientNo, LcParams iPlcp, Logger logger)
	{
		super(iPclient, iPClientNo, iPlcp, logger);
	}

	protected boolean doPage(String msg, BufferedWriter response) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		constructResponseHeader(200, sb);
		response.write(sb.toString());
		response.newLine();
		LocalDate toDay =  LocalDate.now();
		sb.setLength(0);
			response.write(mkPageStart(0, toDay, "Error on server: " + msg));
			response.write(mkPageEnd(0, ""));
		response.newLine();
		response.flush();
		LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections")) ? Level.INFO : Level.FINEST),
				"Client#: " + Integer.toString(clientNo) + ", ################## ERROR RESPONSE DONE ###################\n");
		return true;
	}
}
