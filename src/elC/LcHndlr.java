package elC;
/**
 * @(#)SolarLoggerController: LcHndlr.java
 * Handle web requests received via LcSvr forwarding to
 * the appropriate sub class which sends the response
 * page containing the data requested in the format
 * requested or sends the requested favicon.ico,
 * robots.txt or Google verification string. 
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LcHndlr implements Runnable
{
	protected class WritRes
	{
		int nRecsWrit = 0;
		String firstDtm = "";
		String lastDtm = "";
	}

	protected Socket client;
	protected int clientNo;
	protected boolean toNowReq;
	protected LcParams lcp;
	protected LcHndlr lhp;
	protected Logger LOGGER;

	public LcHndlr()
	{
	}

	public LcHndlr(Socket iPclient, int iPclientNo, LcParams iPlcp, Logger logger)
	{
		client = iPclient;
		clientNo = iPclientNo;
		lcp = iPlcp;
		LOGGER = logger;
		toNowReq = false;
	}

	@Override
	public void run()
	{
		try
		{
			LOGGER.log(
					((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections")) ? Level.INFO : Level.FINEST),
					"Thread started with name:" + Thread.currentThread().getName());
			readResponse();
			return;
		} catch (Exception ex)
		{
			LOGGER.log(
					((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections")) ? Level.INFO : Level.FINEST),
					"Could not process request!: " + ex.toString());
		}
	}

	private void readResponse() throws IOException, Exception
	{
		BufferedWriter responseErr = null;
		LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections")) ? Level.INFO : Level.FINEST),
				"Client#: " + Integer.toString(clientNo) + ", begin...");
		try (BufferedReader request  = new BufferedReader(new InputStreamReader(client.getInputStream()));
			 BufferedWriter response = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))	)
		{
			responseErr = response; // use this for error response after handler subclass exception if possible
			String cmd[] = new String[15];
			int cmdCnt = 0;
			cmd[0] = "";
			String temp = "";
			do
			{
				LOGGER.log(((lcp.dbg.contains("LcHndlr")) ? Level.INFO : Level.FINEST),
						"About to read next input line...");
				temp = request.readLine();
				if (temp == null)
					temp = "";
				LOGGER.log(((lcp.dbg.contains("LcHndlr")) ? Level.INFO : Level.FINEST),
						"Next input line (#" + Integer.toString(cmdCnt + 1) + "): " + temp);
				cmd[cmdCnt] = temp;
				cmdCnt++;
				if (nonAscii(temp))
				{
					LOGGER.log(((lcp.dbg.contains("LcHndlr")) ? Level.INFO : Level.FINEST),
							"Client#: " + Integer.toString(clientNo) + ", requestHeader contains non-ascii.");
					return;
				}
			} while (!temp.isEmpty() && cmdCnt < 15);
			if (cmd[0].isEmpty() || cmdCnt < 2)
			{
				LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections")) ? Level.INFO
						: Level.FINEST), "Client#: " + Integer.toString(clientNo) + ", requestHeader empty.");
				return;
			}
			LOGGER.log(((lcp.dbg.contains("LcHndlr")) ? Level.INFO : Level.FINEST),
					"Client#: " + Integer.toString(clientNo) + ", cmd[] = " + cmd[0] + ", " + cmd[1] + ", " + cmd[2]
							+ ", " + cmd[3] + ", " + cmd[4] + ", " + cmd[5] + ", " + cmd[6] + ", " + cmd[7] + ", "
							+ cmd[8] + ", " + cmd[9] + ", " + cmd[10] + ", " + cmd[11] + ", " + cmd[12]);
			// Get the method from HTTP header
			boolean notFound = false;
			if (cmd[0].startsWith("GET"))
			{
				String reqdFile = cmd[0].split(" ")[1].toUpperCase();
				String reqdParams = cmd[0].split(" ")[2].toUpperCase();
				String reqSect = getLev(1, reqdFile);
				String reqType = getLev(2, reqdFile);
				if (reqdParams == null)
					reqdParams = "";
				LOGGER.log(
						((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections")) ? Level.INFO
								: Level.FINEST),
						"Client#: " + Integer.toString(clientNo) + ", about to service GET from client, reqdFile: <"
								+ reqdFile + ">, path: <" + ">, reqdParams: <" + reqdParams + ">...");
				if (reqdFile.contains("GOOGLE")) // </GOOGLE{hex string}.HTML>
					new LcHndlrGoogleVerifyPage(client, clientNo, lcp, LOGGER).doPage(response);
				else if (reqdFile.contains("ROBOTS.TXT"))
					new LcHndlrRobotsPage(client, clientNo, lcp, LOGGER).doPage(response);
				else if (reqdFile.contains("PAGEREQ"))
					new LcHndlrPagePequest(client, clientNo, lcp, LOGGER).doPage(response, reqdFile);
				else if (reqdFile.contains("FAVICON.ICO"))
					new LcHndlrFavIconPage(client, clientNo, lcp, LOGGER).doPage(response);
				else if (!reqSect.equalsIgnoreCase("LOGGERCONTROLLER") || reqSect.equalsIgnoreCase("INDEX")
						|| reqType.equalsIgnoreCase("INDEX"))
					new LcHndlrIndexPage(client, clientNo, lcp, LOGGER).doPage(response);
				else if (reqType.equalsIgnoreCase("CSV") || reqType.equalsIgnoreCase("BIGCSV")
						|| reqType.equalsIgnoreCase("HTM") || reqType.equalsIgnoreCase("BIGHTM"))
					new LcHndlrResultPage(client, clientNo, lcp, LOGGER).doPage(response, reqdFile);
				else
					notFound = true;
			} else
				notFound = true;
			if (notFound)
			{
				new LcHndlrNotFoundPage(client, clientNo, lcp, LOGGER).doPage(response, cmd[0] + " not found here.");
			}
			LOGGER.log(((lcp.dbg.contains("LcHndlr")) ? Level.INFO : Level.FINEST), "ReadResponse: FIN.");
		} catch (com.mysql.jdbc.exceptions.jdbc4.CommunicationsException ce)
		{
			LOGGER.log(
					((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections")) ? Level.INFO : Level.FINEST),
					"Caught ce: " + ce.toString());
			if (lcp.dbg.contains("LcHndlr"))
				ce.printStackTrace();
			if (responseErr != null)
				new LcHndlrErrorPage(client, clientNo, lcp, LOGGER).doPage(
						"com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: " + ce.getLocalizedMessage(),
						responseErr);
		} catch (java.net.SocketException se)
		{
			LOGGER.log(
					((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections")) ? Level.INFO : Level.FINEST),
					"Caught se: " + se.toString());
			if (lcp.dbg.contains("LcHndlr"))
				se.printStackTrace();
			if (responseErr != null)
				new LcHndlrErrorPage(client, clientNo, lcp, LOGGER).doPage(se.getLocalizedMessage(), responseErr);
		} catch (Exception e)
		{
			LOGGER.log(
					((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections")) ? Level.INFO : Level.FINEST),
					"Caught e: " + e.toString());
			if (lcp.dbg.contains("LcHndlr"))
				e.printStackTrace();
			if (responseErr != null)
				new LcHndlrErrorPage(client, clientNo, lcp, LOGGER).doPage(e.getLocalizedMessage(), responseErr);
		} finally
		{
			if (this.client != null)
				client.close();
		}
	}

	// Construct Response Header
	protected static void constructResponseHeader(int responseCode, StringBuilder sb)
	{
		if (responseCode == 200)
		{
			sb.append("HTTP/1.1 200 OK\r\n");
			sb.append("Date:" + getTimeStamp() + "\r\n");
			sb.append("Server:localhost\r\n");
			sb.append("Content-Type: text/html\r\n");
			sb.append("Connection: Closed\r\n");
			sb.append("\r\n");
		} else if (responseCode == 404)
		{
			sb.append("HTTP/1.1 404 NotFound.\r\n");
			sb.append("Date:" + getTimeStamp() + "\r\n");
			sb.append("Server:localhost\r\n");
			sb.append("Content-Type: text/html\r\n");
			sb.append("Connection: Closed\r\n");
			//sb.append("\r\n");
		}
	}

	// TimeStamp
	protected static String getTimeStamp()
	{
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("E dd M yyyy HH:mm:ss z X");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	protected static String getTimeStamp(Long theTime)
	{
		Date date = new Date(theTime);
		SimpleDateFormat sdf = new SimpleDateFormat("E dd M yyyy HH:mm:ss z X");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	private static String mkCSV(LcReading rd)
	{
		String str = rd.getDate() + "," + rd.getTime() + "," + rd.getTimeLapse() + "," + rd.getDtm() + ","
				+ rd.getShwcIn(1) + "," + rd.getShwcLower(1) + "," + rd.getShwcOut(1) + "," + rd.getStPanel(1) + ","
				+ rd.getStIn(1) + "," + rd.getShwcUpper(1) + "," + rd.getSflowRateGPS(1) + "," + rd.getSpSun(1) + ","
				+ rd.getSimmersionOverride() + "," + rd.getSpumpOn() + "," + rd.getSimmersionDemand() + ","
				+ rd.getSbypassOn() + "," + rd.getSimmersionSupply() + "," + rd.getSpower(1) + "," + rd.getSdispQsize()
				+ "," + rd.getSfileQsize() + "," + rd.getSdataQsize() + "<br />";
		return str;
	}

	protected static String mkPageStart(long nrec, LocalDate toDay, String text)
	{
		return "<!DOCTYPE html><html>" + "<head>" + "<meta charset=\"utf-8\" />"
				+ "<style>table {border-collapse: collapse;}	table, td, th {	border: 1px solid black;}" + "</style>"
				+ "</head>" + "<body><font size=\"2\">" + text + "<br />";
	}

	protected static String mkPageEnd(long nrec, String rec)
	{
		return rec + "</font></body></html>";
	}

	protected static String getLev(int lev, String reqdFile)
	{
		try
		{
			String dtmFrom = reqdFile.split("/")[lev];
			return dtmFrom;
		} catch (Exception e)
		{
			return "";
		}
	}

	protected static int getiLev(int lev, String reqdFile)
	{
		try
		{
			String sNrecs = "";
			int nRecs = (((sNrecs = reqdFile.split("/")[lev]) != null) ? Integer.parseInt(sNrecs) : 0);
			return nRecs;
		} catch (Exception e)
		{
			return 0;
		}
	}

	protected static int getNlev(String reqdFile)
	{
		try
		{
			return reqdFile.length() - reqdFile.replace("/", "").length();
		} catch (Exception e)
		{
			return 0;
		}
	}

	protected boolean isDtm(String str)
	{ // just check string is all digits and 14 digits long for now
		if (!isDigits(str) || str.length() != 14)
		{
			LOGGER.log(
					((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections")) ? Level.INFO : Level.FINEST),
					"NotDtm!!");
			return false;
		}
		return true;
	}

	protected boolean isDigits(String str)
	{
		for (int i = 0; i < str.length();)
		{
			char nextChar = str.charAt(i++);
			if (nextChar < '0' || nextChar > '9')
			{
				LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections")) ? Level.INFO
						: Level.FINEST), "NotDigits!!");
				return false;
			}
		}
		return true;
	}

	private boolean nonAscii(String str)
	{ // find non-ascii in string
		for (int i = 0; i < str.length();)
		{
			char nextChar = str.charAt(i++);
			if (nextChar < 0x00 || nextChar > 0x7F)
			{
				LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections")) ? Level.INFO
						: Level.FINEST), "NonAscii!!");
				return true;
			}
		}
		return false;
	}

	protected boolean doPage(BufferedWriter response, String reqdFile) throws Exception
	{
		return true;
	}

	protected boolean doPage(BufferedWriter response) throws IOException
	{
		return true;
	}

	protected String mkPageBody(int pageNo, LcReading rd)
	{
		return "";
	}

	protected String mkPowerPageBody(int pageNo, LcReading rd)
	{
		return "";
	}

	protected String mkPowerCSV(LcReading rd)
	{
		return "";
	}

	protected WritRes lcWriteMap(Map<String, LcSolarReading> lcRdMap, int expectedNrecs, String type,
			BufferedWriter response) throws IOException
	{
		boolean firstRec = true;
		WritRes wr = new WritRes();
		wr.nRecsWrit = 0;
		wr.lastDtm = "";
		wr.firstDtm = "";
		SortedSet<String> lcRdKeys = null;
		// use TreeMap to sort by key = dtm (natural order)
		lcRdKeys = new TreeSet<String>(lcRdMap.keySet());
		for (String lcRdKey : lcRdKeys)
		{
			LcReading rd = lcRdMap.get(lcRdKey);
			if (wr.firstDtm.isEmpty())
				wr.firstDtm = rd.getDtm();
			LOGGER.log(((lcp.dbg.contains("LcHndlr")) ? Level.INFO : Level.FINEST),
					"WriteMap: " + type + ", next dtm: " + rd.getDtm() + ", firstDtm: " + wr.firstDtm + ", nRecsWrit: "
							+ Integer.toString(wr.nRecsWrit) + ", lcRdKeys.size: " + Integer.toString(lcRdKeys.size())
							+ ", expectedNrecs: " + Integer.toString(expectedNrecs));
			if (wr.nRecsWrit == lcRdKeys.size() - 1)
			{ // but do not output last record -this is for [NEXT]
				if (wr.nRecsWrit == expectedNrecs && !toNowReq && lcRdKeys.size() != 1)
				{
					wr.lastDtm = rd.getDtm();
					LOGGER.log(((lcp.dbg.contains("LcHndlr")) ? Level.INFO : Level.FINEST),
							"WriteMap: readResponse: did not add last reading (" + rd.getDtm() + " to web page...");
				} else
				{ // ... unless last record is now!!
					wr.lastDtm = rd.getDtm();
					if (!type.contains("getRefs"))
						response.write((type.equalsIgnoreCase("CSV") ? mkCSV(rd) : mkPageBody(wr.nRecsWrit + 1, rd)));
					wr.nRecsWrit++;
					response.newLine();
					LOGGER.log(((lcp.dbg.contains("LcHndlr")) ? Level.INFO : Level.FINEST),
							"WriteMap: readResponse: added the very last (ie now) reading (" + rd.getDtm()
									+ " to web page...");
				}
			} else
			{
				wr.lastDtm = rd.getDtm();
				LOGGER.log(((lcp.dbg.contains("LcHndlr")) ? Level.INFO : Level.FINEST),
						"WriteMap: this=" + this.toString());
				if (!type.contains("getRefs"))
				{
					if ((!firstRec && !toNowReq))
					{
						response.write((type.equalsIgnoreCase("CSV") ? mkCSV(rd) : mkPageBody(wr.nRecsWrit + 1, rd)));
						wr.nRecsWrit++;
						response.newLine();
					}
					firstRec = false;
				}
				LOGGER.log(((lcp.dbg.contains("LcHndlr")) ? Level.INFO : Level.FINEST),
						"WriteMap: readResponse: added the very last reading (" + rd.getDtm() + " to web page...");

			}
		}
		return wr;
	}

	protected WritRes lcWritePowerMap(Map<String, LcSolarReading> lcRdMap, int expectedNrecs, String type,
			BufferedWriter response) throws IOException
	{
		WritRes wr = new WritRes();
		wr.nRecsWrit = 0;
		wr.lastDtm = "";
		wr.firstDtm = "";
		SortedSet<String> lcRdKeys = null;
		// use TreeMap to sort by key = display order
		lcRdKeys = new TreeSet<String>(lcRdMap.keySet());

		for (String lcRdKey : lcRdKeys)
		{
			LcReading rd = lcRdMap.get(lcRdKey);
			if (wr.firstDtm.isEmpty())
				wr.firstDtm = rd.getDtm();
			LOGGER.log(((lcp.dbg.contains("LcHndlr")) ? Level.INFO : Level.FINEST),
					"WriteMap: " + type + ", next dtm: " + rd.getDtm() + ", firstDtm: " + wr.firstDtm + ", nRecsWrit: "
							+ Integer.toString(wr.nRecsWrit) + ", lcRdKeys.size: " + Integer.toString(lcRdKeys.size())
							+ ", expectedNrecs: " + Integer.toString(expectedNrecs));
			if (wr.nRecsWrit == lcRdKeys.size() - 1)
			{
				if (wr.nRecsWrit == expectedNrecs)
				{
					wr.lastDtm = rd.getDtm(); // but do not output last record -
												// this is to provide value for
												// [NEXT]
					LOGGER.log(((lcp.dbg.contains("LcHndlr")) ? Level.INFO : Level.FINEST),
							"WriteMap: readResponse: did not add last reading (" + rd.getDtm() + " to web page...");
				} else
				{
					wr.lastDtm = rd.getDtm();
					if (!type.contains("getRefs"))
						response.write((type.equalsIgnoreCase("CSV") ? mkPowerCSV(rd)
								: mkPowerPageBody(wr.nRecsWrit + 1, rd)));
					wr.nRecsWrit++;
					response.newLine();
					LOGGER.log(((lcp.dbg.contains("LcHndlr")) ? Level.INFO : Level.FINEST),
							"WriteMap: readResponse: added the very last (ie now) reading (" + rd.getDtm()
									+ " to web page...");
				}
			} else
			{
				wr.lastDtm = rd.getDtm();
				LOGGER.log(((lcp.dbg.contains("LcHndlr")) ? Level.INFO : Level.FINEST),
						"WriteMap: this=" + this.toString());
				if (!type.contains("getRefs"))
					response.write(
							(type.equalsIgnoreCase("CSV") ? mkPowerCSV(rd) : mkPowerPageBody(wr.nRecsWrit + 1, rd)));
				wr.nRecsWrit++;
				response.newLine();
				LOGGER.log(((lcp.dbg.contains("LcHndlr")) ? Level.INFO : Level.FINEST),
						"WriteMap: readResponse: added the very last reading (" + rd.getDtm() + " to web page...");

			}
		}
		return wr;
	}
}
