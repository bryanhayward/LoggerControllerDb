package elC;
/**
 * @(#)LoggerController: LcDataDisplayTest.java
 * Tester for swing GUI display window.
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

import static org.junit.Assert.*;

import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.Test;

public class LcDataDisplayTest
{

	@Test
	public void test() throws InterruptedException
	{final LogManager lm = LogManager.getLogManager();
	final Logger LOGGER = lm.getLogger(Logger.GLOBAL_LOGGER_NAME);
		LcDataDisplay disp = new LcDataDisplay(new LcParams(), LOGGER);
		LcSolarReading rd = new LcSolarReading(); 
		LcMessage msg = new LcMessage(rd);
		boolean dispOk = disp.displayData(rd);
		if (!dispOk) fail("Failed...");
		Thread.sleep(10000000);
	}

}
