package elC;
/**
 * @(#)LoggerController: LcDbSaver.java
 * Takes reading record at head of queue and gives to LcSQLcrud
 * to write to MySql database. Repeats, blocking when queue is empty.
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LcDbSaver extends LcQueueSink implements Runnable
{
	private LcQueueParams lcqp = null;

	public LcDbSaver(BlockingQueue<LcMessage> q, LcParams passedLcp, Logger logger)
	{
		super(q, passedLcp, logger);
		lcqp = new LcQueueParams("SaveData", passedLcp, logger);
		if (qLcp.msgLev > 5)
			LOGGER.log(((qLcp.dbg.contains("LcDbSaver"))?Level.INFO:Level.FINEST), "Queue initialised...");
	}

	@Override
	public void run()
	{
		try
		{
			lcqp.extractQparas();
			LcMessage msg;
			LOGGER.log(((qLcp.dbg.contains("LcDbSaver"))?Level.INFO:Level.FINEST), "About to start waiting for dbQueue...");
			while (true) // consuming messages forever
			{
				msg = queue.take();
				LcReading rd = msg.getReading();
				Calendar curDte = null;
				curDte = Calendar.getInstance();
				if (qLcp.msgLev > 5 || qLcp.dbg.contains("LcDbSaver"))
					LOGGER.log(((qLcp.dbg.contains("LcDbSaver"))?Level.INFO:Level.FINEST), "Got one: "
							+ rd.getDtm() + " and saving to db...");
				retryLoop: for (int i = 0; i < (lcqp.retries > 0 ? lcqp.retries + 1 : 1); i++)
				{
					LOGGER.log(((qLcp.dbg.contains("LcDbSaver"))?Level.INFO:Level.FINEST), "Retry #"
								+ Integer.toString(i) + " attempting save: " + rd.getDtm() + ": to db...");
					boolean writOk = LcSQLcrud.writeReading(rd, curDte, qLcp, LOGGER);
					if (writOk)
					{
						if (qLcp.msgLev > 5 || qLcp.dbg.contains("LcDbSaver"))
						{
							LOGGER.log(((qLcp.dbg.contains("LcDbSaver"))?Level.INFO:Level.FINEST), "Writen ok to database  after " + Integer.toString(i) + " retries, resting "
									+ Long.toString(lcqp.dwellSuccess) + " ms before looking for next...");
						}
						Thread.sleep(lcqp.dwellSuccess);
						break retryLoop;
					} else
					{
						LOGGER.log(((qLcp.dbg.contains("LcDbSaver"))?Level.INFO:Level.FINEST), "Write failed, resting "
											+ Long.toString(lcqp.dwellFailure) + " ms before re-trying...");
						Thread.sleep(lcqp.dwellFailure);
					}
				}
				if (qLcp.msgLev > 5 || qLcp.dbg.contains("LcDbSaver"))
				{
					LOGGER.log(((qLcp.dbg.contains("LcDbSaver"))?Level.INFO:Level.FINEST), "After retry loop, about to take another...");
				}
			}
		} catch (Exception e)
		{
			LOGGER.log(((qLcp.dbg.contains("LcDbSaver"))?Level.INFO:Level.FINEST), "Caught: " + e.toString());
			e.printStackTrace();
		}
	}
}
