package elC;
/**
 * JUnit test for http portal module.
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Calendar;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Bry
 *
 */
public class LcSvrTest
{

	//static String[] cmdArgs = new String[] {"ini", "C:\\Users\\Bry\\eclipse\\workspace\\LoggerControllerDb\\src\\lcini-dev.txt"};
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
	}

	@Test
	public void test() throws InterruptedException
	{
		final LogManager lm = LogManager.getLogManager();
		final Logger LOGGER = lm.getLogger(Logger.GLOBAL_LOGGER_NAME);
		

		String[] args = {"ini", "C:\\Users\\Bry\\eclipse\\workspace\\LoggerControllerDb\\src\\lcini-dev.txt"};
		Handler consoleHandler = null;
		Handler fileHandler  = null;
		Calendar startDt = Calendar.getInstance();
		try{ //Create consoleHandler and fileHandler, add to logger
			consoleHandler = new ConsoleHandler();
			fileHandler  = new FileHandler("./LoggerController" + Long.toHexString(startDt.getTime().getTime()) + ".log");
			//Assigning handlers to LOGGER object
			LOGGER.addHandler(consoleHandler);
			LOGGER.addHandler(fileHandler);
			//Setting levels to handlers and LOGGER
			consoleHandler.setLevel(Level.FINEST);
			fileHandler.setLevel(Level.FINEST);
			LOGGER.setLevel(Level.FINEST);
			LOGGER.config("Configuration done.");
			//Console handler removed
			//logger.removeHandler(consoleHandler);
			LcParams lcp = new LcParams(args, LOGGER);
			lcp.parseParams(args, LOGGER);
			LcSvr sSvr = new LcSvr(80, lcp.clone(), LOGGER);
			new Thread(sSvr).start();
			LOGGER.log(((lcp.dbg.contains("LCServer"))?Level.INFO:Level.FINE), "LCServer: started.");
			Thread.sleep(3600000);
		}catch(IOException exception){
			LOGGER.log(Level.SEVERE, "Error occur in FileHandler.", exception);
		}
	}

}
