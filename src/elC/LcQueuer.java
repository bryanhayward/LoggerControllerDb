package elC;
/**
 * @(#)LoggerController: LcQueuer.java
 * Adds reading records as messages to a Queue, waits one second
 * before silently (logs problem) discarding data if the queue
 * cannot accept.
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LcQueuer
{
	private BlockingQueue<LcMessage> queue;
	private static Logger LOGGER;

	public LcQueuer(BlockingQueue<LcMessage> q, Logger logger)
	{
		queue = q;
		LOGGER = logger;
	}

	public void queueAdd(LcSolarReading rd)
	{
		LcMessage msg = new LcMessage(rd);
		try
		{
			boolean taken = false;
			taken = queue.offer(msg, 1, TimeUnit.SECONDS);
			if (!taken) LOGGER.log(Level.FINEST, "!! not taken: " + rd.getDtm() + ", by: " + queue.toString());
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
