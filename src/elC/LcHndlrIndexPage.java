package elC;
/**
 * @(#)SolarLoggerController: LcHndlrIndexPage.java
 * Sends Index page to client.
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

public class LcHndlrIndexPage extends LcHndlr
{
	public LcHndlrIndexPage(Socket iPclient, int iPClientNo, LcParams iPlcp, Logger logger)
	{
		super(iPclient, iPClientNo, iPlcp, logger);
	}

	protected String mkUsage()
	{
		String usage = "<font size=\"2\">Use: '{//svr}' GET <br />"
				+ "HTTP (port 80): ['/LoggerController/{type}/{direction}/{date}/{number of records} where:<br />"
				+ "Type = CSV or HTM: output format.<BR>"
				+ "Direction = PREV, NEXT or POWER. POWER: only records with a recorded power are returned.<BR>"
				+ "Date = CCYYMMDDHHmm or LATEST.<BR>"
				+ "Number of records = Maximum number of records to return except POWER when is number of days covered prior to date.<BR>"
				+ "<BR>"
				+ "['/LoggerController/HTM/PREV/LATEST/N']: prevous N (max 100) records to CCYYMMDDHHmm each in HTML table in DateTime order or<br />"
				+ "['/LoggerController/HTM/NEXT/CCYYMMDDHHmm/N next N (max 100) records from CCYYMMDDHHmm each in HTML table in DateTime order or<br />"
				+ "['/LoggerController/HTM/PREV/CCYYMMDDHHmm/N']: prevous N (max 100) records to CCYYMMDDHHmm each in HTML table in DateTime order or<br />"
				+ "['/LoggerController/HTM/POWER/LATEST/N']: max 1000 most recent highest power records in the last N (max 100) days in HTML table in Power order or<br />"
				+ "['/LoggerController/HTM/POWER/CCYYMMDDHHmm/N']: max 1000 most recent highest power records in the last N (max 100) days each in HTML table in Power order or<br />"
				+ "['/LoggerController/CSV/NEXT/CCYYMMDDHHmm/N']: next N (max 1000) records from CCYYMMDDHHmm in CSV in DateTime order or<br />"
				+ "['/LoggerController/CSV/PREV/CCYYMMDDHHmm/N']: previous N (max 1000) records to CCYYMMDDHHmm in CSV in DateTime order or<br />"
				+ "['/LoggerController/CSV/POWER/LATEST/N']: max 1000 most recent highest power records in the last N (max 100) days in CSV in Power order or<br />"
				+ "['/LoggerController/CSV/POWER/CCYYMMDDHHmm/N']: max 1000 most recent highest power records in the last N (max 100) days as CSV in Power order or<br />"
				+ "['/help']: this usage page.<br />"
				+ "*** Currently port 80 only ***<br />"
				+ "Examples: <a href=\"http://82.30.20.239/index\">['http://82.30.20.239/index']</a>,"
				+ "<a href=\"http://82.30.20.239/LoggerController/HTM/PREV/LATEST/10\">['http://82.30.20.239/LoggerController/HTM/PREV/LATEST/10'],</a>"
				+ "<a href=\"http://82.30.20.239/LoggerController/CSV/PREV/20170515/10\">['http://82.30.20.239/LoggerController/CSV/PREV/20170515/10'],</a>"
				+ "<a href=\"http://82.30.20.239/LoggerController/HTM/NEXT/20171108112507/10\">['http://82.30.20.239/LoggerController/HTM/NEXT/20171108112507/10'].</a><br />"
				+ "<a href=\"http://82.30.20.239/LoggerController/HTM/POWER/LATEST/100\">['http://82.30.20.239/LoggerController/HTM/POWER/LATEST/100'],</a>"
				+ "<a href=\"http://82.30.20.239/LoggerController/CSV/POWER/LATEST/1000\">['http://82.30.20.239/LoggerController/CSV/POWER/LATEST/1000'].</a><br />"
				+ "<a href=\"http://82.30.20.239/LoggerController/HTM/POWER/20170512000000/100\">['http://82.30.20.239/LoggerController/HTM/POWER/20170512000000/100'],</a>"
				+ "<a href=\"http://82.30.20.239/LoggerController/CSV/POWER/20170519000000/1000\">['http://82.30.20.239/LoggerController/CSV/POWER/20170519000000/1000'].</a><br />"
				+ "CSV fields: [Date, Time, TimeLapse, Dtm, HwcIn, HwcLower, HwcOut, Tpanel, Tin, HwcUpper, FlowRateGPS, pSun, ImmersionOverride, PumpOn, "
				+ "ImmersionDemand, BypassOn, ImmersionSupply, Power, DispQsize, FileQsize, DataQsize().]<br />"
				+ "CSV fields for Power response: [Dtm, Power.]<br />"
				+ "LoggerController 1.1 Bryan Hayward 201711 </font>";
		return usage;
	}
	protected boolean doPage(BufferedWriter response) throws IOException
	{
			/*********************** INDEX *********************************/
		StringBuilder sb = new StringBuilder();
		sb.setLength(0);
		constructResponseHeader(200, sb);
		response.write(sb.toString());
		response.newLine();
		LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections"))?Level.INFO:Level.FINEST),
				"Client#: " + Integer.toString(clientNo) + ", INDEX requested, sent 200 header, about to send page and end...");
		response.write(mkPageStart(0, LocalDate.now(), ""));
		response.write(mkUsage());
		response.write(mkPageEnd(0, ""));
		response.newLine();
		response.flush();
		LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections"))?Level.INFO:Level.FINEST),
				"Client#: " + Integer.toString(clientNo) + ", ################## INDEX DONE ###################\n");
		return true;
	}
}
