package elC;/**
 * @(#)LoggerController: LcHndlrPagePequest.java
 * Handle requests from HTTP portal page from the web
 * received via LcSvr
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 * eg loggercontroller/pagerequest?nrec=1&dt=2018-02-10&tm=23%3A59
 */

import java.io.BufferedWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LcHndlrPagePequest extends LcHndlr
{
	protected class reqParams
	{
		boolean ok = false;
		int nParams = 0;
		String params[];
	}
	public LcHndlrPagePequest(Socket iPclient, int iPClientNo, LcParams iPlcp, Logger logger)
	{
		super(iPclient, iPClientNo, iPlcp, logger);
	}

	protected boolean doPage(BufferedWriter response, String reqdFile) throws Exception
	{ // just reformat request: http://localhost/loggercontroller/pagerequest.htm?nrec=1&Type=CSV&dt=2018-02-10&tm=23%3A59
	  // and give to LcHndlrResultPage
		reqParams rp = getParas(reqdFile);
		if (!rp.ok)
		{
			new LcHndlrNotFoundPage(client, clientNo, lcp, LOGGER).doPage(response);
			return false;
		}
		String nRecs = rp.params[0];
		if (Integer.parseInt(nRecs) > 10 && reqdFile.toLowerCase().contains("pagerequestp"))
			nRecs = "10";
		String reqType = rp.params[1];
		String reqDirn = "NEXT";
		String reqDate = rp.params[2];
		String reqTime = ((!reqDate.equals("LATEST"))?rp.params[3]:"");			
		if (reqdFile.toLowerCase().contains("pagerequestp"))
		{
			nRecs = rp.params[0];
			reqType = rp.params[1];
			reqDirn = "POWER";
			reqDate = rp.params[2];
			reqTime = "000000";
		}
		reqdFile = "/LOGGERCONTROLLER/" +  reqType + "/" + reqDirn + "/" + reqDate + reqTime + "/" + nRecs;
		new LcHndlrResultPage(client, clientNo, lcp, LOGGER).doPage(response, reqdFile);
		LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections"))?Level.INFO:Level.FINEST),
				"LcHndlrNotFoundPage: client#: " + Integer.toString(clientNo) + ", ################### PageRequest DONE ##################\n");
		return true;
	}
	private reqParams getParas(String paras)
	{
		reqParams rp = new reqParams();
		rp.ok = true;
		try
		{
			URL url = new URL("HTTP:" + paras);
			String qryStr = "";
			if ((qryStr = url.getQuery()) != null)
			{
				rp.params = (URLDecoder.decode(qryStr, "UTF-8")).split("&");
				rp.nParams = rp.params.length;
				rp.params[0] = rp.params[0].split("=")[1]; // nrec
				rp.params[1] = rp.params[1].split("=")[1]; // type
				if (rp.nParams < 4)
				{
					rp.ok = false;
					return rp;
				}
				rp.params[2] = rp.params[2].split("=")[1].replaceAll("-", ""); // dt
				rp.params[3] = rp.params[3].split("=")[1].replaceAll(":", ""); // tm
				if (rp.params[0].isEmpty() || !isDigits(rp.params[0])) rp.ok = false;
				if (rp.params[1].isEmpty() ||
					(!rp.params[1].equals("CSV") && !rp.params[1].equals("HTM")))
					rp.ok = false; // dt=2018-02-10&tm=23%3A59
				if (rp.params[2].isEmpty() || rp.params[2].toUpperCase().contains("LATEST")) rp.params[2] = "LATEST";
				else if (!isDigits(rp.params[2])) rp.ok = false;
				if (rp.params[3].isEmpty()) rp.params[3] = "000000";
				else if (!isDigits(rp.params[3])) rp.ok = false;
				if (rp.params[3].length() == 4) rp.params[3] = rp.params[3] + "00";
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			rp.ok = false;
			LOGGER.log(((lcp.dbg.contains("LcHndlr") || lcp.dbg.contains("LogConnections")) ? Level.INFO : Level.WARNING),
					"Client#: " + Integer.toString(clientNo) + " Ka-booom: Exception: " + e.toString());
		}
		return rp;
	}
}
