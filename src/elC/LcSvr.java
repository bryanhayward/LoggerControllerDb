package elC;
/**
 * @(#)LoggerController: LcSvr.java
 * Runs in own thread: opens server socket and starts a new LcHndlr
 * thread for each client that connects.
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LcSvr implements Runnable
{
	private static Logger LOGGER;
	private ServerSocket serverSocket;
	private int port;
	private LcParams lcp;

	public LcSvr(int port, LcParams passedLcp, Logger logger)
	{
		this.port = port;
		this.lcp = passedLcp;
		LOGGER = logger;
	}

	@Override
	public void run()
	{
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat();
			sdf.applyPattern("yyyyMMddHHmmss");
			int connCnt = 0;
			serverSocket = new ServerSocket(port);
			LOGGER.log(((lcp.dbg.contains("LcSvr"))?Level.INFO:Level.FINEST), "@@@@@ LcSvr: Starting the socket server at port:" + Integer.toString(port));
			Socket client = null;
			Thread handlr = null;
			while (true)
			{
				try
				{
					LOGGER.log(((lcp.dbg.contains("LcSvr"))?Level.INFO:Level.FINEST), "##### Waiting for client connection #"
								+ Integer.toString(connCnt + 1) + " #####...");
					client = serverSocket.accept();
					connCnt++;
					LOGGER.log(((lcp.dbg.contains("LcSvr") || lcp.dbg.contains("LogConnections"))?Level.INFO:Level.FINEST), "#### Client #" + Integer.toString(connCnt)
								+ " has connected:" + client.getInetAddress().getCanonicalHostName() + " ("
								+ client.getInetAddress().getHostAddress() + ") on port: " + Integer.toString(client.getPort()) + ". Starting *LcHndlr* #####");
					// The client has connected to this server. Start the handler...
					handlr = new Thread(new LcHndlr(client, connCnt, lcp, LOGGER));
					handlr.start();
				} catch (Exception e)
				{
					LOGGER.log(((lcp.dbg.contains("LcSvr") || lcp.dbg.contains("LogConnections"))?Level.INFO:Level.FINEST),
							"#### Client #" + Integer.toString(connCnt) + " has lost connection, Exception: " + e.toString());
					handlr = null; // if recovering after exception handlr is sure to be nfg
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}