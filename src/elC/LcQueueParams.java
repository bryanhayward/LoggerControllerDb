package elC;
/**
 * @(#)LoggerController: LcQueueParams.java
 * Define and maintain parameters for Queue objects.
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class LcQueueParams
{
	private static Logger LOGGER = null;
	private Node qParasNode = null;
	private LcParams pLcp;
	public String queueType = "";
	public int queueMaxLen = 0;
	public long dwellSuccess = 0L;
	public long dwellFailure = 0L;
	public int retries = 0;

	public LcQueueParams(String qType, LcParams passedLcp, Logger logger)
	{
		queueType = qType;
		pLcp = passedLcp;
		LOGGER = logger;
		extractQparas();
	}

	public void extractQparas()
	{
		qParasNode = pLcp.getQparas(queueType);
		for (int k = 0; k < qParasNode.getChildNodes().getLength(); k++)
		{
			Node qParaChildNode = qParasNode.getChildNodes().item(k);
			if (qParaChildNode instanceof Element)
			{
				switch (qParaChildNode.getNodeName())
				{
				case "queueMaxLen":
					queueMaxLen = Integer.parseInt(qParaChildNode.getTextContent());
					break;
				case "dwellSuccess":
					dwellSuccess = Long.parseLong(qParaChildNode.getTextContent());
					break;
				case "dwellFailure":
					dwellFailure = Long.parseLong(qParaChildNode.getTextContent());
					break;
				case "retries":
					retries = Integer.parseInt(qParaChildNode.getTextContent());
					break;
				}
			}
		}
		LOGGER.log(((pLcp.dbg.contains("LcQueueParams")) ? Level.INFO : Level.FINEST),
				"LcQueueParams: type: " + queueType + ", queueMaxLen: " + Integer.toString(queueMaxLen)
						+ ", dwellSuccess: " + Long.toString(dwellSuccess) + ", dwellFailure: "
						+ Long.toString(dwellFailure) + ", retries: " + Integer.toString(retries));
	}
}
