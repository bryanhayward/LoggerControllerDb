package elC;
/**
 * @(#)LoggerController: LcImmCtl.java
 * 
 *
 * @author Bryan Hayward
 * @version 1.01 2018/02/20
 * 
 * This is immersion heater controller.
 * Two methods handle events that could result in an immersion
 *  supply state change.
 *  1. doImmOvr: is invoked by a listener that detects a hardware event:
 *   the Immersion Heater override button requests toggle of the immersion heater supply.
 *  2. ckImmCtl: switches off override after half hour.
 */

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LcImmCtl
{
	private static Logger LOGGER = null;
	public static boolean immersionOverrideState = false;
	public static boolean immersionOverride = false;
	public static boolean lastOverrideState = false;
	public static long immOvrStartMillis = 0L;
	private static boolean ctlInProgress = false;

	public LcImmCtl(Logger logger)
	{
		LOGGER = logger;
	}
	public static void doImmOvr(LcParams lcp) throws InterruptedException
	{ // this is accessed aysync to logger loop by listener
		if (ctlInProgress)
		{
			LOGGER.log(((lcp.dbg.contains("ImmCtl")) ? Level.INFO : Level.FINEST), "CtlInProgress!!");
			return;
		}
		else
			ctlInProgress = true;
		try
		{
			Calendar curDt = Calendar.getInstance();
			LOGGER.log(((lcp.dbg.contains("ImmCtl")) ? Level.INFO : Level.FINEST),
					"ImmersionOverrideState = " + Boolean.toString(immersionOverrideState) + ", lastOverrideState = "
							+ Boolean.toString(lastOverrideState));
			if (immersionOverrideState != lastOverrideState)
			{
				if (immersionOverrideState) // turn it off user pressed button
											// again
				{
					immersionOverride = false;
					lastOverrideState = immersionOverrideState;
				} else // turn it on
				{
					immersionOverride = true;
					immOvrStartMillis = curDt.getTimeInMillis();
					lastOverrideState = immersionOverrideState;
				}
			}
			if (immersionOverride)
			{
				LOGGER.log(((lcp.dbg.contains("ImmCtl")) ? Level.INFO : Level.FINEST),
						"ImmersionOverriding: start: " + Long.toString(immOvrStartMillis) + ", start+: "
								+ Long.toString(immOvrStartMillis + 1800000) + ", cur: "
								+ Long.toString(curDt.getTimeInMillis()));
				if (immOvrStartMillis + 1800000 < curDt.getTimeInMillis()) // 30minutes
				{ // time up - turn it off
					immersionOverride = false;
				}
			}
		} catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "LcImmCtl.doImmOvr: " + e.toString());
		} finally
		{
			Thread.sleep(200);
			ctlInProgress = false;
		}
	}

	public static void ckImmCtl(LcParams lcp) throws InterruptedException
	{
		if (ctlInProgress)
		{
			LOGGER.log(((lcp.dbg.contains("ImmCtl")) ? Level.INFO : Level.FINEST), "CtlInProgress.");
			return;
		}
		else
			ctlInProgress = true;
		try
		{
			Calendar curDt = Calendar.getInstance();
			LOGGER.log(((lcp.dbg.contains("ImmCtl")) ? Level.INFO : Level.FINEST),
					"ImmersionOverrideState = " + Boolean.toString(immersionOverrideState) + " ,lastOverrideState = "
							+ Boolean.toString(lastOverrideState));
			if (immersionOverride)
			{
				LOGGER.log(((lcp.dbg.contains("ImmCtl")) ? Level.INFO : Level.FINEST),
						"ImmersionOverriding: start: " + Long.toString(immOvrStartMillis) + ", start+: "
								+ Long.toString(immOvrStartMillis + 1800000) + ", cur: "
								+ Long.toString(curDt.getTimeInMillis()));
				if (immOvrStartMillis + 1800000 < curDt.getTimeInMillis()) // 30minutes
				{ // time up - turn it off
					immersionOverride = false;
				}
			}
		} catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "LcImmCtl.doImmCtl: " + e.toString());
		}
		Thread.sleep(200);
		ctlInProgress = false;
	}
}
