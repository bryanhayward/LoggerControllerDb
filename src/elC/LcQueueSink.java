package elC;
/**
 * @(#)LoggerController: LcQueueSink.java
 * Super class for objects that consume Queues.
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LcQueueSink implements Runnable
{
	protected BlockingQueue<LcMessage> queue;
	protected LcParams qLcp;
	protected static Logger LOGGER = null;

	public void run() { }

	public LcQueueSink(BlockingQueue<LcMessage> q, LcParams passedLcp, Logger logger)
	{
		queue = q;
		qLcp = passedLcp;
		LOGGER = logger;
		LOGGER.log(((qLcp.dbg.contains("LcQueueSink"))?Level.INFO:Level.FINEST), "Queue initialised...");
	}
}