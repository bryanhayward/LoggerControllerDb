package elC;
/**
 * @(#)LoggerController: LcParams.java
 * All configurable parameters are found here.
 * The clone method is provided to allow a process to have its own copy
 *  if that process needs to change a parameter after startup.
 * Write access by processes other than the main thread instance is not intended.
 * 
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class LcParams implements Cloneable
{
	private static Logger LOGGER = null;
	public String iniFilePath = "";
	public Document iniDoc = null;
	public int msgLev = 0;
	public String dbg = "";
	public double offSet = 0; // degC trigger panel temperature for pump
	public double hysPosPump = 0; // degC hysteresis value
	public double hysNegPump = 0; // degC hysteresis value
	public double hysPosByps = 0; // degC hysteresis value
	public double hysNegByps = 0; // degC hysteresis value
	public int logPeriod = 0; // seconds logging period
	public int portNumber = 0; // port for http server
	public boolean showDisp = true;
	public boolean saveFile = true;
	public boolean saveDb = true;
	public boolean remainConn = true;
	public String host = "";
	public String userName = "";
	public StringBuffer pwd = null;

	public LcParams()
	{
	}

	public LcParams(String[] args, Logger logger)
	{
		parseParams(args, logger);
	}

	public LcParams clone()
	{
		try
		{
			return (LcParams) super.clone();
		} catch (CloneNotSupportedException e)
		{
			LOGGER.log(((dbg.contains("LcParams"))?Level.INFO:Level.FINEST), "This should never happen: " + e.toString());
		}
		return null;
	}

	public void applyDefaults()
	{
		iniFilePath = "";
		iniDoc = null;
		msgLev = 0;
		dbg = "";
		offSet = 0.0;
		hysPosPump = 0.5;
		hysNegPump = 0.5;
		hysPosByps = 0.5;
		hysNegByps = 0.5;
		logPeriod = 30;
		portNumber = 80;
		showDisp = true;
		saveFile = true;
		saveDb = true;
		remainConn = true;
		host = "localhost";
		userName = "elC";
		pwd = null;
	}

	public void parseParams(String[] args, Logger logger)
	{
		LOGGER = logger;
		applyDefaults();
		if (args.length > 1)
			try
			{
				if (args[0].toUpperCase().startsWith("INI"))
				{
					if (args[1] == null)
						throw new Exception(
								"INI specified, Param2: is blank - should be path to ini file, use <LoggerController HELP>.");
					iniFilePath = args[1];
					readIniXml();
				} else if (args[0].toUpperCase().startsWith("H"))
				{
					System.out
							.println("Use <LoggerController HELP> or <LoggerController INI {path to xml '.ini' file}>");
					System.out.println("Ini file created with defaults if not found.");
					System.out.println(
							"Typical values (optimised not): msgLev: 0, dbg: , hysPosPump: 0.5, hysNegPump: 0.5, hysPosByps: 0.5, hysNegByps: 0.5"
									+ " logPeriod: 30, portNumber: 80, showDisp: 1, saveFile: 1, saveDb: 1, remainConn: 1,"
									+ " Qpara: , queueType: SaveFile, queueMaxLen: 4096, dwellSuccess: 10, dwellFail: 1001, retries: 0,"
									+ " Qpara: , queueType: SaveData, queueMaxLen: 4096, dwellSuccess: 20, dwellFail: 1002, retries: 1000,"
									+ " Qpara: , queueType: Display, queueMaxLen: 10, dwellSuccess: 1001, dwellFailure: 1000, retries: 1000");
					System.out.println("LoggerController Version 1.1 Nov 2017");
					Thread.sleep(5000);
					System.exit(0);
				}
				if (msgLev < 0 || msgLev > 99)
					msgLev = 0;
			} catch (Exception x)
			{
				System.out.print(x.toString());
				System.exit(0);
			}
		else
			System.out.println("LoggerController: requires parameters, use <LoggerController HELP>.");

		if (msgLev > 0)
			System.out.println("LoggerController ... starting with debugLev: " + Integer.toString(msgLev) +
					" , hysP+: " + Double.toString(hysPosPump) + " , hysP-: " + Double.toString(hysNegPump) +
					" , hysB+: " + Double.toString(hysPosByps) +   " , hysB-: " + Double.toString(hysNegByps) + " , logPeriod: "
					+ Integer.toString(logPeriod) + " , portNumber: " + Integer.toString(portNumber) +
					" , display?: " + Boolean.toString(showDisp) + " , file?: " + Boolean.toString(saveFile) + 
					" , db?: " + Boolean.toString(saveDb) + " , conn?: " + Boolean.toString(remainConn)
					+ ", host: " + host + ", userName: " + userName + ", pwd: (" + pwd.length() + "chars), dbg: " + dbg + ".");
		Handler handlers[] = LOGGER.getHandlers();
		Handler consoleHandler = null;
		Handler fileHandler = null;
		for (Handler handler : handlers)
		{
			if (handler.getClass().getName().contains("ConsoleHandler")) consoleHandler = handler;
			if (handler.getClass().getName().contains("FileHandler")) fileHandler = handler;
		}
		switch(msgLev)
		{
			case 0:
				LOGGER.setLevel(Level.OFF);
				if (consoleHandler != null) consoleHandler.setLevel(Level.OFF);
				if (fileHandler != null) fileHandler.setLevel(Level.OFF);
				break;
			case 1:
				LOGGER.setLevel(Level.SEVERE);
				if (consoleHandler != null) consoleHandler.setLevel(Level.SEVERE);
				if (fileHandler != null) fileHandler.setLevel(Level.SEVERE);
				break;
			case 2:
				LOGGER.setLevel(Level.WARNING);
				if (consoleHandler != null) consoleHandler.setLevel(Level.WARNING);
				if (fileHandler != null) fileHandler.setLevel(Level.WARNING);
				break;
			case 3:
				LOGGER.setLevel(Level.INFO);
				if (consoleHandler != null) consoleHandler.setLevel(Level.INFO);
				if (fileHandler != null) fileHandler.setLevel(Level.INFO);
				break;
			case 4:
				LOGGER.setLevel(Level.CONFIG);
				if (consoleHandler != null) consoleHandler.setLevel(Level.CONFIG);
				if (fileHandler != null) fileHandler.setLevel(Level.CONFIG);
				break;
			case 5:
				LOGGER.setLevel(Level.FINE);
				if (consoleHandler != null) consoleHandler.setLevel(Level.FINE);
				if (fileHandler != null) fileHandler.setLevel(Level.FINE);
				break;
			case 6:
				LOGGER.setLevel(Level.FINER);
				if (consoleHandler != null) consoleHandler.setLevel(Level.FINER);
				if (fileHandler != null) fileHandler.setLevel(Level.FINER);
				break;
			default:
				LOGGER.setLevel(Level.FINEST);
				if (consoleHandler != null) consoleHandler.setLevel(Level.FINEST);
				if (fileHandler != null) fileHandler.setLevel(Level.FINEST);
				break;
		}
		if (!dbg.isEmpty() && LOGGER.getLevel() == Level.OFF)
				LOGGER.setLevel(Level.INFO);
				if (consoleHandler != null) consoleHandler.setLevel(Level.INFO);
				if (fileHandler != null) fileHandler.setLevel(Level.INFO);
	}
	
	private static boolean isBoolean(String str)
	{ // true if any positive integer of any size
		if (str == null)
			return false;
		int length = str.length();
		if (length == 0)
			return false;
		if (str.charAt(0) == '1' || str.charAt(0) == '0')
			return true;
		return false;
	}

	private static boolean isInt(String str)
	{ // true if any positive integer of any size
		if (str == null)
			return false;
		int length = str.length();
		if (length == 0)
			return false;
		for (int i = 0; i < length; i++)
		{
			char c = str.charAt(i);
			if (c < '0' || c > '9')
				return false;
		}
		return true;
	}

	private static boolean isDbl(String str)
	{ // true if any double of any size
		if (str == null)
			return false;
		boolean dp = false;
		int length = str.length();
		if (length == 0) return false;
		for (int i = 0; i < length; i++)
		{
			char c = str.charAt(i);
			if (c != '-')
			{
				if (c != '.')
				{
					if (c < '0' || c > '9')
						return false;
				} else
				{
					if (dp)
						return false;
					dp = true;
				}
			} else
			{
				if (i>1)
					return false;
			}
		}
		return true;
	}

	public Node getQparas(String qType)
	{
		if (iniDoc != null)
			try
			{
				NodeList iDnodeList = iniDoc.getDocumentElement().getChildNodes();
				for (int i = 0; i < iDnodeList.getLength(); i++)
				{
					Node iDnode = iDnodeList.item(i);
					if (iDnode instanceof Element)
						switch (iDnode.getNodeName())
						{
						case "Qpara":
							for (int j = 0; j < iDnode.getChildNodes().getLength(); j++)
							{
								Node childNode = iDnode.getChildNodes().item(j);
								if (childNode instanceof Element)
								{
									switch (childNode.getNodeName())
									{
									case "queueType":
										if (childNode.getTextContent().equals(qType))
										{
											return iDnode;
										}
										break;
									}
								}
							}
							break;
						}
				}
			} catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, "Failed iterating xml doc nodes: " + e.toString());
				System.exit(0);
			}
		LOGGER.log(Level.SEVERE, "IniDoc null or could not find any Qpara, returning null.");
		return null;
	}

	private void readIniXml()
	{
		loadDocument(msgLev); // msgLev will be as set in defaults (0)
		LOGGER.log(Level.FINEST, "LcParams: readIniXml: Root element: " + iniDoc.getDocumentElement().getNodeName());
		try
		{
			NodeList iDnodeList = iniDoc.getDocumentElement().getChildNodes();
			for (int i = 0; i < iDnodeList.getLength(); i++)
			{
				Node iDnode = iDnodeList.item(i);
				if (iDnode instanceof Element)
				{
					switch (iDnode.getNodeName())
					{
					case "msgLev":
						if (!isInt(iDnode.getTextContent()))
							throw new Exception("!!msgLev!! is not an integer, <" + iDnode.getTextContent() + "> use <LoggerController help>.");
						msgLev = Integer.parseInt(iDnode.getTextContent());
						break;
					case "dbg":
						dbg = iDnode.getTextContent();
						break;
					case "hysPosPump":
						if (!isDbl(iDnode.getTextContent()))
							throw new Exception("!!hysPosPump!! is not a double (decimal) <" + iDnode.getTextContent() + ">, use <LoggerController help>.");
						hysPosPump = Double.parseDouble(iDnode.getTextContent());
						break;
					case "hysNegPump":
						if (!isDbl(iDnode.getTextContent()))
							throw new Exception("!!hysNegPump!! is not a double (decimal) <" + iDnode.getTextContent() + ">, use <LoggerController help>.");
						hysNegPump = Double.parseDouble(iDnode.getTextContent());
						break;
					case "hysPosByps":
						if (!isDbl(iDnode.getTextContent()))
							throw new Exception("!!hysPosByps!! is not a double (decimal) <" + iDnode.getTextContent() + ">, use <LoggerController help>.");
						hysPosByps = Double.parseDouble(iDnode.getTextContent());
						break;
					case "hysNegByps":
						if (!isDbl(iDnode.getTextContent()))
							throw new Exception("!!hysPosByps!! is not a double (decimal) <" + iDnode.getTextContent() + ">, use <LoggerController help>.");
						hysNegByps = Double.parseDouble(iDnode.getTextContent());
						break;
					case "logPeriod":
						if (!isInt(iDnode.getTextContent()))
							throw new Exception("!!logPeriod!! is not an integer, <" + iDnode.getTextContent() + "> use <LoggerController help>.");
						logPeriod = Integer.parseInt(iDnode.getTextContent());
						break;
					case "portNumber":
						if (!isInt(iDnode.getTextContent()))
							throw new Exception("!!portNumber!! is not an integer, <" + iDnode.getTextContent() + "> use <LoggerController help>.");
						portNumber = Integer.parseInt(iDnode.getTextContent());
						break;
					case "showDisp":
						if (!isBoolean(iDnode.getTextContent()))
							throw new Exception("!!showDisp!! is not '1' or '0', <" + iDnode.getTextContent() + "> use 'LoggerController help'; ");
						showDisp = (iDnode.getTextContent().charAt(0) != '0' ? true : false);
						break;
					case "saveFile":
						if (!isBoolean(iDnode.getTextContent()))
							throw new Exception("!!saveFile!! is not '1' or '0', <" + iDnode.getTextContent() + "> use 'LoggerController help'; ");
						saveFile = (iDnode.getTextContent().charAt(0) != '0' ? true : false);
						break;
					case "saveDb":
						if (!isBoolean(iDnode.getTextContent()))
							throw new Exception("!!saveDb!! is not '1' or '0', <" + iDnode.getTextContent() + "> use 'LoggerController help'; ");
						saveDb = (iDnode.getTextContent().charAt(0) != '0' ? true : false);
						break;
					case "remainConn":
						if (!isBoolean(iDnode.getTextContent()))
							throw new Exception("!!remainConn!! is not '1' or '0', <" + iDnode.getTextContent() + "> use 'LoggerController help'; ");
						remainConn = (iDnode.getTextContent().charAt(0) != '0' ? true : false);
						break;
					case "host":
						 host= iDnode.getTextContent();
						break;
					case "userName":
						 userName= iDnode.getTextContent();
						break;
					case "pwd":
						pwd = new StringBuffer(iDnode.getTextContent());
						break;
					}
				}
			}
		} catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Failed iterating xml doc nodes: " + e.toString());
			System.exit(0);
		}
		finally {if (pwd == null) pwd = new StringBuffer("");}
		LOGGER.log(Level.FINEST, "Done iterating xml doc nodes.");
	}

	private void loadDocument(int msgLevel)
	{
		try
		{
			LOGGER.log(Level.FINEST, "loadDocument:  about to read: " + getInif() + "...");
			File tstFile = new File(getInif());
			if (!tstFile.exists())
			{
				LOGGER.log(Level.FINEST, "LoadDocument:  " + getInif() + ": Does not Exist!!, creating...");
				iniDoc = createIniFromData(msgLevel);
				return;
			} else
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				LOGGER.log(Level.FINEST, "About to parse: Validating: " + builder.isValidating()
							+ ", NamespaceAware: " + builder.isNamespaceAware() + ", XIncludeAware: "
							+ builder.isXIncludeAware() + ", Schema: " + builder.getSchema());
				iniDoc = builder.newDocument();
				iniDoc = builder.parse(new File(getInif()));
				LOGGER.log(Level.FINEST, "Done read: " + getInif() + ", iniDoc: Root element: "
							+ iniDoc.getDocumentElement().getNodeName() + ".");
				return;
			}
		} catch (SAXException e)
		{
			LOGGER.log(Level.SEVERE, "Could not read: " + getInif() + ":\n" + e.toString());
		} catch (ParserConfigurationException e)
		{
			LOGGER.log(Level.SEVERE, "Could not read: " + getInif() + ".\n" + e.toString());
		} catch (IOException e)
		{
			LOGGER.log(Level.FINEST, "Could not read: " + getInif() + ":\n" + e.toString());
		}
		return;
	}

	private Document createIniFromData(int msgLevel)
	{
		Document doc = null;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.newDocument();
			Element rootElement = doc.createElement("LoggerControllerCfg");
			doc.appendChild(rootElement);

			Element msgLevEle = doc.createElement("msgLev");
			msgLevEle.setTextContent(Integer.toString(msgLevel));
			rootElement.appendChild(msgLevEle);

			Element dbgEle = doc.createElement("dbg");
			dbgEle.setTextContent(dbg);
			rootElement.appendChild(dbgEle);

			Element pLevEle = doc.createElement("offSet");
			pLevEle.setTextContent(Double.toString(offSet));
			rootElement.appendChild(pLevEle);

			Element hysPosPumpEle = doc.createElement("hysPosPump");
			hysPosPumpEle.setTextContent(Double.toString(hysPosPump));
			rootElement.appendChild(hysPosPumpEle);

			Element hysNegPumpEle = doc.createElement("hysNegPump");
			hysNegPumpEle.setTextContent(Double.toString(hysNegPump));
			rootElement.appendChild(hysNegPumpEle);

			Element hysPosBypsEle = doc.createElement("hysPosByps");
			hysPosBypsEle.setTextContent(Double.toString(hysPosByps));
			rootElement.appendChild(hysPosBypsEle);

			Element hysNegBypsEle = doc.createElement("hysNegByps");
			hysNegBypsEle.setTextContent(Double.toString(hysNegByps));
			rootElement.appendChild(hysNegBypsEle);

			Element logPeriodEle = doc.createElement("logPeriod");
			logPeriodEle.setTextContent(Integer.toString(logPeriod));
			rootElement.appendChild(logPeriodEle);

			Element portNumberEle = doc.createElement("portNumber");
			portNumberEle.setTextContent(Integer.toString(portNumber));
			rootElement.appendChild(portNumberEle);

			Element showDispEle = doc.createElement("showDisp");
			showDispEle.setTextContent((showDisp ? "1" : "0"));
			rootElement.appendChild(showDispEle);

			Element saveFileEle = doc.createElement("saveFile");
			saveFileEle.setTextContent((saveFile ? "1" : "0"));
			rootElement.appendChild(saveFileEle);

			Element saveDbEle = doc.createElement("saveDb");
			saveDbEle.setTextContent((saveDb ? "1" : "0"));
			rootElement.appendChild(saveDbEle);

			Element Ele = doc.createElement("remainConn");
			Ele.setTextContent((remainConn ? "1" : "0"));
			rootElement.appendChild(Ele);

			Element saveFileQparaEle = doc.createElement("Qpara");
			rootElement.appendChild(saveFileQparaEle);

			Element sFqueueType = doc.createElement("queueType");
			sFqueueType.setTextContent("SaveFile");
			saveFileQparaEle.appendChild(sFqueueType);

			Element sFqueueMaxLen = doc.createElement("queueMaxLen");
			sFqueueMaxLen.setTextContent("4096");
			saveFileQparaEle.appendChild(sFqueueMaxLen);

			Element sFdwellSucess = doc.createElement("dwellSucess");
			sFdwellSucess.setTextContent("0");
			saveFileQparaEle.appendChild(sFdwellSucess);

			Element sFdwellFail = doc.createElement("dwellFail");
			sFdwellFail.setTextContent("0");
			saveFileQparaEle.appendChild(sFdwellFail);

			Element sFdwellEmpty = doc.createElement("retries");
			sFdwellEmpty.setTextContent("0");
			saveFileQparaEle.appendChild(sFdwellEmpty);

			Element saveDataQparaEle = doc.createElement("Qpara");
			rootElement.appendChild(saveDataQparaEle);

			Element sDqueueType = doc.createElement("queueType");
			sDqueueType.setTextContent("SaveData");
			saveDataQparaEle.appendChild(sDqueueType);

			Element sDqueueMaxLen = doc.createElement("queueMaxLen");
			sDqueueMaxLen.setTextContent("4096");
			saveDataQparaEle.appendChild(sDqueueMaxLen);

			Element sDdwellSucess = doc.createElement("dwellSucess");
			sDdwellSucess.setTextContent("0");
			saveDataQparaEle.appendChild(sDdwellSucess);

			Element sDdwellFail = doc.createElement("dwellFail");
			sDdwellFail.setTextContent("10000");
			saveDataQparaEle.appendChild(sDdwellFail);

			Element sDretries = doc.createElement("retries");
			sDretries.setTextContent("1000000");
			saveDataQparaEle.appendChild(sDretries);

			Element displayQparaEle = doc.createElement("Qpara");
			rootElement.appendChild(displayQparaEle);

			Element dQueueType = doc.createElement("queueType");
			dQueueType.setTextContent("Display");
			displayQparaEle.appendChild(dQueueType);

			Element dQueueMaxLen = doc.createElement("queueMaxLen");
			dQueueMaxLen.setTextContent("4096");
			displayQparaEle.appendChild(dQueueMaxLen);

			Element dDwellSuccess = doc.createElement("dwellSuccess");
			dDwellSuccess.setTextContent("0");
			displayQparaEle.appendChild(dDwellSuccess);

			Element dDwellFailure = doc.createElement("dwellFailure");
			dDwellFailure.setTextContent("0");
			displayQparaEle.appendChild(dDwellFailure);

			Element dDdwellEmpty = doc.createElement("retries");
			dDdwellEmpty.setTextContent("0");
			displayQparaEle.appendChild(dDdwellEmpty);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(getInif()));

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); //

			transformer.transform(source, result);
			LOGGER.log(Level.FINEST, "Created: " + getInif());
		} catch (TransformerConfigurationException tce)
		{
			LOGGER.log(Level.SEVERE, "CreateIniFromData: " + tce.toString());
		} catch (ParserConfigurationException e)
		{
			LOGGER.log(Level.SEVERE, "CreateIniFromData: " + e.toString());
		} catch (TransformerException te)
		{
			LOGGER.log(Level.SEVERE, "CreateIniFromData: " + te.toString());
		}
		return doc;
	}

	private String getInif()
	{
		return (iniFilePath);
	}
}
