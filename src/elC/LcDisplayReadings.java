package elC;
/**
 * @(#)LoggerController: LcDisplayReadings.java
 * Takes reading record at head of queue and displays in swing gui
 * window on local desktop. Repeats, blocking when queue is empty.
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LcDisplayReadings extends LcQueueSink implements Runnable
{
	public LcDisplayReadings(BlockingQueue<LcMessage> q, LcParams passedLcp, Logger logger)
	{
		super(q, passedLcp, logger);
		logger.log(((qLcp.dbg.contains("LcDisplayReadings"))?Level.INFO:Level.FINEST), "Queue initialised...");
	}

	@Override
	public void run()
	{
		try
		{
			LcMessage msg;
			LOGGER.log(((qLcp.dbg.contains("LcDisplayReadings"))?Level.INFO:Level.FINEST), "About to create GUI...");
			LcDataDisplay disp = new LcDataDisplay(qLcp,LOGGER);
			LOGGER.log(((qLcp.dbg.contains("LcDisplayReadings"))?Level.INFO:Level.FINEST), "Created GUI, about to start waiting for dispQueue...");
			while (true) // consuming messages forever
			{
				msg = queue.take();
				LcReading rd = msg.getReading();
				LOGGER.log(((qLcp.dbg.contains("LcDisplayReadings"))?Level.INFO:Level.FINEST), "Got one and displaying if display still exists...");
				if (disp != null)
				{
					boolean dispOk = disp.displayData(rd);
					if (dispOk)
						LOGGER.log(((qLcp.dbg.contains("LcDisplayReadings"))?Level.INFO:Level.FINEST), "...data displayed...");
					else
						LOGGER.log(((qLcp.dbg.contains("LcDisplayReadings"))?Level.INFO:Level.FINEST), "...data display failed...");
				} else LOGGER.log(((qLcp.dbg.contains("LcDisplayReadings"))?Level.INFO:Level.FINEST), "...display is no more!...");
			}
		} catch (Exception e)
		{
			LOGGER.log(((qLcp.dbg.contains("LcDisplayReadings"))?Level.INFO:Level.FINEST), "Caught: " + e.toString());
			e.printStackTrace();
		}
	}
}
