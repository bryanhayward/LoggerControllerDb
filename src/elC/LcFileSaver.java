package elC;
/**
 * @(#)LoggerController: LcFileSaver.java
 * Takes reading record from head of queue and writes to data file also
 * handling limit to file size and copies to flash drives ("SOLARDATA" and "SOLARJAVA").
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LcFileSaver extends LcQueueSink implements Runnable
{
	private LcQueueParams lcqp = null;
	
	public LcFileSaver(BlockingQueue<LcMessage> q, LcParams passedLcp, Logger logger)
	{
		super(q, passedLcp, logger);
		lcqp = new LcQueueParams("SaveFile", passedLcp, logger);
		LOGGER.log(((passedLcp.dbg.contains("LcFileSaver"))?Level.INFO:Level.FINEST), "Queue initialised...");
	}

	@Override
	public void run()
	{
		try
		{
			lcqp.extractQparas();
			LcMessage msg;
			int i = 0;
			LOGGER.log(((qLcp.dbg.contains("LcFileSaver"))?Level.INFO:Level.FINEST), "About to start waiting for fileQueue items...");
			while (true)
			{
				msg = queue.take();
				LcReading rd = msg.getReading();
				boolean saveRes = saveData(rd);
				if (saveRes)
				{
					Thread.sleep(lcqp.dwellSuccess);
					i = 0;
				}
				else
				{
					LOGGER.log(Level.SEVERE, "Save failed, resting " + Long.toString(lcqp.dwellFailure) + " ms...");
					Thread.sleep(lcqp.dwellFailure);
					if (++i == lcqp.retries) return;
				}
			}
		} catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "LcFileSaver: caught: " + e.toString());
			e.printStackTrace();
		}
	}

	protected boolean saveData(LcReading rd)
	{
		boolean retval = true;
		String flash1 = "/media/pi/SOLARDATA/results";
		String flash2 = "/media/pi/SOLARJAVA/results";
		LOGGER.log(((qLcp.dbg.contains("LcFileSaver"))?Level.INFO:Level.FINEST), "About to check for history folder...");
		File fFlash1 = new File(flash1);
		File fFlash2 = new File(flash2);
		try
		{
			File sf = new File("adclogj.txt");
			Long curLn = sf.length();
			LOGGER.log(((qLcp.dbg.contains("LcFileSaver"))?Level.INFO:Level.FINEST), "Checking log size: " + Long.toString(curLn));
			if (curLn > 0X4FFFFFL)
			{
				SimpleDateFormat sdf = new SimpleDateFormat();
				sdf.applyPattern("yyyyMMddHHmmss");
				Calendar curDte = Calendar.getInstance();
				File logBak = new File("adclogj" + sdf.format(curDte.getTime()) + ".txt");
				sf.renameTo(logBak);
				LOGGER.log(((qLcp.dbg.contains("LcFileSaver"))?Level.INFO:Level.FINEST), "Renamed log to history file.");
				Thread.sleep(lcqp.dwellSuccess);
			}
		} catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Could not rename max size logfile: " + e.toString());
			e.printStackTrace();
		}
		// write data to local log file
		try
		{
			FileOutputStream ostream = new FileOutputStream("adclogj.txt", true);
			OutputStreamWriter ostreamWtr = new OutputStreamWriter(ostream);
			String dataLine = rd.toCSV();
			ostreamWtr.write(dataLine, 0, dataLine.length());
			ostreamWtr.write('\r');
			ostreamWtr.write('\n');
			ostreamWtr.flush();
			ostreamWtr.close();
		} catch (Exception e)
		{
			retval = false; // consider this the only failure - the flash may be
							// removed ad hoc
			LOGGER.log(Level.SEVERE, "saveData: could not create or append to logfile: " + e.toString());
			e.printStackTrace();
		}
		if (fFlash1.exists()) copyToFlash(fFlash1);
		if (fFlash2.exists()) copyToFlash(fFlash2);
		return retval;
	}
	private void copyToFlash(File fFlash)
	{
		try (DirectoryStream<Path> fnStream = Files.newDirectoryStream(FileSystems.getDefault().getPath("."),
				"adclogj*.txt"))
		{
			nextFile: for (Path filePath : fnStream)
			{
				LOGGER.log(((qLcp.dbg.contains("LcFileSaver"))?Level.INFO:Level.FINEST), "Copying historic logbak " + filePath.getFileName() + " to flash...");
				try
				{
					Files.copy(filePath,
							FileSystems.getDefault().getPath(fFlash.getAbsolutePath() + "/" + filePath.getFileName()));
				} catch (FileAlreadyExistsException fe)
				{
					LOGGER.log(((qLcp.dbg.contains("LcFileSaver"))?Level.INFO:Level.FINEST), "FileAlreadyExists on Flash.");
				} catch (Exception e)
				{
					LOGGER.log(((qLcp.dbg.contains("LcFileSaver"))?Level.INFO:Level.FINEST), "Could not copy logbak to flash: " + e.toString());
					e.printStackTrace();
					continue nextFile;
				}
				LOGGER.log(((qLcp.dbg.contains("LcFileSaver"))?Level.INFO:Level.FINEST), "Copied or ignored historic logbak " + filePath.getFileName()
							+ " to flash.");
			}
		} catch (IOException | DirectoryIteratorException e)
		{
			LOGGER.log(((qLcp.dbg.contains("LcFileSaver"))?Level.INFO:Level.FINEST), "Could not copy logbak to flash: " + e.toString());
		}
		LOGGER.log(((qLcp.dbg.contains("LcFileSaver"))?Level.INFO:Level.FINEST), "Done copy historic logbaks to flash.");
	}
}
