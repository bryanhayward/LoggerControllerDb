package elC;
/**
 * @(#)LoggerController: LcHndlrResultPage.java
 * Handle requests for logged date from the web and
 * format as requested.
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 * 
 * CSV or HTM
 * eg: http://192.168.2.5/LoggerController/CSV/NEXT/20140511174257/10 or
 * http://192.168.2.5/LoggerController/HTM/POWER/20140511174257/10 top N records from dtm for power or
 * http://192.168.2.5/LoggerController/HTM/POWER/LATEST/10 top N most recent records for power
 */

import java.io.BufferedWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LcHndlrResultPage extends LcHndlr
{
	public LcHndlrResultPage(Socket iPclient, int iPClientNo, LcParams iPlcp, Logger logger)
	{
		super(iPclient, iPClientNo, iPlcp, logger);
	}

	protected boolean doPage(BufferedWriter response, String reqdFile) throws Exception
	{ // /LoggerController/{type}/{direction}/{date}/{number of records}
		if (!chkParas(reqdFile))
		{
			new LcHndlrIndexPage(client, clientNo, lcp, LOGGER).doPage(response);
			return false;
		}
		StringBuilder sb = new StringBuilder();
		sb.setLength(0);
		String reqType = getLev(2, reqdFile);
		String reqDirn = getLev(3, reqdFile);
		String reqDate = getLev(4, reqdFile);
		if (isDtm(reqDate))
		{
			LocalDateTime toDay = LocalDateTime.now();
			String now = toDay.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
			if (new BigInteger(reqDate).longValueExact() >= new BigInteger(now).longValueExact())
				toNowReq = true;
		}
		int nRecs = getiLev(5, reqdFile);
		LocalDateTime toDay = LocalDateTime.now();
		if (reqDate.equals("LATEST"))
			reqDate = toDay.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "235959";
		constructResponseHeader(200, sb);
		response.write(sb.toString());
		response.newLine();
		LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections")) ? Level.INFO : Level.FINEST),
				"Client#: " + Integer.toString(clientNo) + ", page type (lev2): " + reqType
						+ " requested: direction (lev3): " + reqDirn + ", nRecs: " + Integer.toString(nRecs)
						+ ", dtmFromTo (lev4): " + reqDate + ", sent 200 header, about to send page...");
		if (nRecs < 1)
			nRecs = 1;
		if (nRecs > 10 && reqType.equals("HTM"))
			nRecs = 10;
		if (nRecs > 1000 && reqType.equals("CSV"))
			nRecs = 1000;
		if (nRecs > 3 && reqDirn.equals("POWER"))
			nRecs = 3;
		response.newLine();
		response.flush();
		Map<String, LcSolarReading> lcRdMap = LcSQLcrud.getReadings(reqDirn, nRecs, reqDate, lcp, LOGGER);
		WritRes wr = null;
		if (reqDirn.equals("POWER"))
		{
			wr = lcWritePowerMap(lcRdMap, nRecs, "getRefs", response);
			response.write(mkPageStart(nRecs, reqType, reqDirn, toDay, "Requesting highest power records from "
					+ ((nRecs == 1) ? " day" : Integer.toString(nRecs) + " days") + " power records to " + reqDate + "..."));
			wr = lcWritePowerMap(lcRdMap, nRecs, reqType, response);
			response.write(mkPageEnd(nRecs, Integer.toString(wr.nRecsWrit)
					+ ((wr.nRecsWrit != 1) ? " records" : " record") + " returned.<br />"));
		} else
		{
			wr = lcWriteMap(lcRdMap, nRecs, "getRefs", response);
			response.write(mkPageStart(nRecs, reqType, reqDirn, toDay,
					"Requested " + reqType + " " + ((reqDirn.equals("NEXT")) ? "from " : "to ") + reqDate + ", "
							+ Integer.toString(nRecs) + ((nRecs != 1) ? " records" : " record") + "...<br />"
							+ "<a href=\"/LOGGERCONTROLLER/" + reqType + "/PREV/" + wr.firstDtm + "/"
							+ Integer.toString(nRecs) + "\">PREV</a>        " + "<a href=\"/LOGGERCONTROLLER/" + reqType
							+ "/NEXT/" + wr.lastDtm + "/" + Integer.toString(nRecs) + "\">NEXT</a><br />"));
			wr = lcWriteMap(lcRdMap, nRecs, reqType, response);
			response.write(mkPageEnd(nRecs,
					Integer.toString(wr.nRecsWrit) + ((wr.nRecsWrit != 1) ? " records" : " record") + " returned.<br />"
							+ "<a href=\"/LOGGERCONTROLLER/" + reqType + "/PREV/" + wr.firstDtm + "/"
							+ Integer.toString(nRecs) + "\">PREV</a>        " + "<a href=\"/LOGGERCONTROLLER/" + reqType
							+ "/NEXT/" + wr.lastDtm + "/" + Integer.toString(nRecs) + "\">NEXT</a><br />"));
		}
		response.newLine();
		response.flush();
		LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections")) ? Level.INFO : Level.FINEST),
				"Client#: " + Integer.toString(clientNo) + ", ################## " + reqType
						+ " DONE ###################\n");
		return true;
	}

	protected static String mkPageStart(long nrec, String type, String dirn, LocalDateTime toDay, String text)
	{ // HTML5
		StringBuilder pageStart = new StringBuilder();
		String head = "<!DOCTYPE html><html>" + "<head>" + "<meta charset=\"UTF-8\" />"
				+ "<meta accept-charset=\"UTF-8\" />"
				+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">";
		String bodyStart = "<body><div class=\"content\">";
		String scrollBarStyle = " body {" + " margin: 0;" + " font-size: 12px;" + " }\n" + " .header {"
				+ " position: fixed;" + " bottom: 0;" + " z-index: 1;" + " width: 100%;" + " background-color: blue;"
				+ " }\n" + " .progress-container {" + " width: 100%;" + " height: 8px;" + " background: #ccc;" + " }\n"
				+ " .progress-bar {" + " height: 8px;" + " background: blue;" + " width: 0%;" + " }\n" + " .content {"
				+ " padding: 10px 0;" + " margin: 10px auto 0 auto;" + " width: 99%; font-size: 18px;" + " }\n"
				+ " input[type=number]{width: 80px;} input[type=date]{width: 140px;} input[type=time]{width: 140px;}\n"
				+ ".formStyle { font-size: 18px; padding: 10px; border 10 px; background-color: lightgreen;}\n";
		String script = "<script>\n" + "    // When the user scrolls the page, execute barFunction\n"
				+ "    window.onscroll = function () {\n" + "        barFunction()\n" + "    };\n"
				+ "    function barFunction() {\n"
				+ "        var winScroll = document.body.scrollTop || document.documentElement.scrollTop;\n"
				+ "        var height = document.documentElement.scrollHeight - document.documentElement.clientHeight;\n"
				+ "        var scrolled = (winScroll / height) * 100\n;"
				+ "        document.getElementById(\"theBar\").style.width = scrolled + \"%\";\n"
				+ "    }\n</script>\n";
		String scrollBar = "<div class=\"header\">" + "  <div class=\"progress-container\">"
				+ "    <div class=\"progress-bar\" id=\"theBar\"></div>" + " </div></div>";
		String form = "<form action=\"/loggercontroller/"
				+ ((dirn.equalsIgnoreCase("POWER")) ? "pagerequestp.htm" : "pagerequest.htm")
				+ "\" accept-charset=\"UTF-8\"><div class=\"formStyle\">\n" + " Request "
				+ "  <input type=\"number\" name=\"nrec\" min=\"1\" value=\"" + Long.toString(nrec) + "\"> "
				+ ((dirn.equalsIgnoreCase("POWER")) ? ((nrec == 1) ? "max power in day" : "max power in days")
						: ((nrec == 1) ? "record" : "records"))
				+ " as <BR>" + "  <input type=\"radio\" name=\"Type\" value=\"CSV\" "
				+ ((type.equals("CSV")) ? "checked" : "") + "> CSV or "
				+ "  <input type=\"radio\" name=\"Type\" value=\"HTM\" " + ((type.equals("HTM")) ? "checked" : "")
				+ "> HTM from:<BR>" + " <input type=\"date\" name=\"dt\" min=\"2014-01-01\" value=\""
				+ toDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\">\n"
				+ "  <input type=\"" + ((dirn.equalsIgnoreCase("POWER"))?"hidden":"time") + "\" name=\"tm\" value=\"" + toDay.format(DateTimeFormatter.ofPattern("HH:mm"))
				+ "\">\n" + "<input type=\"submit\" value=\"Send Request\">\n</div></form><BR>\n";
		String tableStyle = " table {border-collapse: collapse;}	table, td, th {	border: 1px solid black;}\n";
		if (nrec > 2)
		{
			pageStart.append(head.concat("<style>\n"));
			pageStart.append(scrollBarStyle);
			pageStart.append(tableStyle);
			pageStart.append("</style>\n");
			pageStart.append(script);
			pageStart.append("</head>\n");
			pageStart.append(bodyStart);
			pageStart.append(scrollBar);
			pageStart.append(form);
			pageStart.append("<font size=\"2\">" + text + "<br />\n");
		} else
		{
			pageStart.append(head.concat("<style>\n"));
			pageStart.append(scrollBarStyle);
			pageStart.append(tableStyle);
			pageStart.append("</style>\n");
			pageStart.append(script);
			pageStart.append("</head>\n");
			pageStart.append(bodyStart);
			pageStart.append(form);
			pageStart.append("<font size=\"2\">" + text + "<br />\n");
		}
		return (pageStart.toString());
	}

	protected static String mkPageEnd(long nrec, String rec)
	{
		if (nrec > 2) // end scroll bar division.
			return rec
					+ "<BR>SolarLoggerController, Bryan Hayward, 2018 - gb.planet@ntlworld.com.</div></font></body></html>";
		else
			return rec + "SolarLoggerController, Bryan Hayward, 2018 - gb.planet@ntlworld.com.</font></body></html>";
	}

	protected String mkPageBodyPre(int pageNo, LcReading rd)
	{ // HTML table
		/* + */
		String pre = "<head><style>table {border-collapse: collapse;}"
				+ "	table, td, th {	border: 1px solid black;font-size: 50%;}</style></head>";
		return pre;
	}

	protected String mkPageBody(int pageNo, LcReading rd)
	{ // HTML table
		String page = "<table style=\"width:50%\">" + "<tr>" + "<th>SolarLoggerController</th>"
				+ "<th>Values at Dtm (page: " + Integer.toString(pageNo) + ")</th>" + "</tr>" + "<tr>" + "<th>Date</th>"
				+ "<th>" + rd.getDate() + "</th>" + "</tr>" + "<tr>" + "<th>Time</th>" + "<th>" + rd.getTime() + "</th>"
				+ "</tr>" + "<tr>" + "<th>Runtime</th>" + "<th>" + rd.getTimeLapse() + "</th>" + "</tr>" + "<tr>"
				+ "<th>DTM</th>" + "<th>" + rd.getDtm() + "</th>" + "</tr>" + "<tr>" + "<th>HWCin</th>" + "<th>"
				+ rd.getShwcIn(1) + " degC</th>" + "</tr>" + "<tr>" + "<th>HWClower</th>" + "<th>" + rd.getShwcLower(1)
				+ " degC</th>" + "</tr>" + "<tr>" + "<th>HWCout</th>" + "<th>" + rd.getShwcOut(1) + " degC</th>"
				+ "</tr>" + "<tr>" + "<th>Tpanel</th>" + "<th>" + rd.getStPanel(1) + " degC</th>" + "</tr>" + "<tr>"
				+ "<th>Tin</th>" + "<th>" + rd.getStIn(1) + " degC</th>" + "</tr>" + "<tr>" + "<th>HWCupper</th>"
				+ "<th>" + rd.getShwcUpper(1) + " degC</th>" + "</tr>" + "<tr>" + "<th>FlowRateGPS</th>" + "<th>"
				+ rd.getSflowRateGPS(1) + " g/s</th>" + "</tr>" + "<tr>" + "<th>Psun</th>" + "<th>" + rd.getSpSun(1)
				+ "</th>" + "</tr>" + "<tr>" + "<th>ImmersionOverride</th>" + "<th>" + rd.getSimmersionOverride()
				+ "</th>" + "</tr>" + "<tr>" + "<th>ImmersionDemand</th>" + "<th>" + rd.getSimmersionDemand() + "</th>"
				+ "</tr>" + "<tr>" + "<th>ImmersionSupply</th>" + "<th>" + rd.getSimmersionSupply() + "</th>" + "</tr>"
				+ "<tr>" + "<th>Pump</th>" + "<th>" + rd.getSpumpOn() + "</th>" + "</tr>" + "<tr>" + "<th>Bypass</th>"
				+ "<th>" + rd.getSbypassOn() + "</th>" + "</tr>" + "<tr>" + "<th>Current Solar Power</th>" + "<th>"
				+ rd.getSpower(1) + " Watts</th>" + "</tr>" + "<tr>" + "<th>Qlengths: Disp, File, Data</th>" + "<th>"
				+ rd.getSdispQsize() + ", " + rd.getSfileQsize() + ", " + rd.getSdataQsize() + "</th>" + "</tr>"
				+ "</table>";
		return (page);
	}

	protected String mkPowerPageBody(int pageNo, LcReading rd)
	{ // HTML table
		String page = "";
		page = "<table style=\"width:50%\">" + "<tr><style>font-size: 50%;</style>"
				+ "<th>Solar LoggerController</th>" + "<th>Values at Dtm (page: " + pageNo + ")</th>" + "</tr>" + "<tr>"
				+ "<th>Date</th>" + "<th>" + rd.getDate() + "</th>" + "</tr>" + "<tr>" + "<th>Time</th>" + "<th>"
				+ rd.getTime() + " " + "<a href=\"/LOGGERCONTROLLER/HTM/NEXT/" + rd.getDtm() + "/1" + "\">[view]</a>"
				+ "</th>" + "</tr>" + "<th>Current Solar Power</th>" + "<th>" + rd.getSpower(1) + " Watts</th>"
				+ "</tr>" + "</table>";
		return (page);
	}

	protected String mkPowerCSV(LcReading rd)
	{
		String str = rd.getDtm() + "," + rd.getSpower(0) + "<a href=\"/LOGGERCONTROLLER/HTM/NEXT/" + rd.getDtm() + "/25"
				+ "\"> [view]</a>" + "<br />";
		return str;
	}

	private boolean chkParas(String paras)
	{
		String lev0 = getLev(0, paras);
		String lev1 = getLev(1, paras);
		String lev2 = getLev(2, paras);
		String lev3 = getLev(3, paras);
		String lev4 = getLev(4, paras);
		String lev5 = getLev(5, paras);
		LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections")) ? Level.INFO : Level.FINEST),
				"Client#: " + Integer.toString(clientNo) + ", levs: 0:" + lev0 + ", 1:" + lev1 + ", 2:" + lev2 + ", 3:"
						+ lev3 + ", 4:" + lev4 + ", 5:" + lev5);
		if (!lev2.equalsIgnoreCase("CSV") && !lev2.equalsIgnoreCase("HTM") && !lev2.equalsIgnoreCase("BIGCSV")
				&& !lev2.equalsIgnoreCase("BIGHTM"))
			return false;
		if (!lev3.equalsIgnoreCase("PREV") && !lev3.equalsIgnoreCase("NEXT") && !lev3.equalsIgnoreCase("POWER")
				&& !lev3.equalsIgnoreCase("BIGPOWER"))
			return false;
		if (!lev4.equalsIgnoreCase("LATEST") && !isDigits(lev4))
			return false;
		if (!lev5.isEmpty() && !isDigits(lev5))
			return false;
		return true;
	}
}
