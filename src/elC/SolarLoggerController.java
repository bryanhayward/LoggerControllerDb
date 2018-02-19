package elC;
/**
 * @(#)SolarLoggerController: SolarLoggerController.java
 * The 'main' method is found here:
 * 1. Initialise hardware.
 * 2. Start other processes (queue sinks).
 * 3. Add listener for immersion override facility.
 * 4. Start loop:
 * 5.   Request data.
 * 6.   Perform control actions.
 * 7.   Write data to one or more queues.
 * 8.   Wait measurement interval by flashing LED.
 * 9. Repeat.
 *
 * The hardware takes about 2 seconds to read all inputs and
 * set all outputs, an additional second is used to allow a
 * hardware state sensing device time to settle after an
 * output is set that might affect an input:
 *   Immersion supply -> Immersion demand.
 * Measurements are obtained from the hardware every 12 seconds and each
 * reading record is added to the display queue for immediate display.
 * The interval between recorded (file and or database) will be the value
 * configured in <logPeriod> in the ini xml file plus 3 to 5 seconds
 * (33 seconds gives 960000 rows just inside the Excel 2007 1M rows per
 * sheet limit).
 * 
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.pi4j.io.gpio.PinEdge;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class SolarLoggerController
{
	public SolarLoggerController() { }

	private static boolean immersionOverrideState = false;

	public static void main(String args[]) throws InterruptedException, IOException
	{
		Calendar curDt = Calendar.getInstance();
		Calendar startDt = curDt;
		LcParams lcp = new LcParams();
		
		// Create logger
		final LogManager lm = LogManager.getLogManager();
		final Logger LOGGER = lm.getLogger(Logger.GLOBAL_LOGGER_NAME);
		
		Handler consoleHandler = null;
		Handler fileHandler  = null;
		try{ //Create consoleHandler and fileHandler, add to logger
			consoleHandler = new ConsoleHandler();
			fileHandler  = new FileHandler("./LoggerController" + Long.toHexString(startDt.getTime().getTime()) + ".log");
			//Assigning handlers to LOGGER object
			LOGGER.addHandler(consoleHandler);
			LOGGER.addHandler(fileHandler);
			//Setting levels to handlers and LOGGER
			consoleHandler.setLevel(Level.FINEST);
			fileHandler.setLevel(Level.OFF);
			LOGGER.setLevel(Level.FINEST);
			LOGGER.config("Configuration done.");
			//Remove console handler if not required
			if (lcp.dbg.isEmpty() && lcp.msgLev == 0) LOGGER.removeHandler(consoleHandler);
		}catch(IOException ioe){
			LOGGER.log(Level.SEVERE, "Error occured creaating handlers: ", ioe.toString());
		}		
		lcp.parseParams(args, LOGGER);
		LOGGER.log(Level.FINE, "Begin LoggerController main...");
		try
		{
			LOGGER.log(Level.FINE, "Begin LoggerController main...");
			LcIoPins iop = new LcIoPins(LOGGER); // create gpio controller
			iop.provLcIoPins(lcp.clone());
			iop.ioPin01.setDebounce(1000);
			LOGGER.log(Level.FINE, "Created IoPins about to add GpioPinListenerDigital...");
			iop.ioPin01.addListener(new GpioPinListenerDigital() // could not lambda access to pins this as it seems GpioPinListener does not support
			{
				@Override
				public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event)
				{ // waiting for push button press event - non latching switch
					LOGGER.log(Level.FINE, "GpioPinListenerDigital immersionOverrideState was: "
								+ Boolean.toString(immersionOverrideState));
					if (event.getPin().getName() == "Input-A1"
							&& (event.getEdge().equals(PinEdge.BOTH) || event.getEdge().equals(PinEdge.RISING)))
					{
						if (!LcImmCtl.immersionOverrideState)
							LcImmCtl.immersionOverrideState = true;
						else
							LcImmCtl.immersionOverrideState = false;
					}
					LOGGER.log(Level.FINE, "GpioPinListenerDigital immersionOverrideState now: "
								+ Boolean.toString(immersionOverrideState));
				}
			});
			new LcImmCtl(LOGGER);
			LOGGER.log(Level.FINE, "Added GpioPinListenerDigital, creating queues...");
			// Loop control vars and vars used to provide hysteresis
			boolean lcontinue = true;
			boolean prevPumpOn = false;
			boolean pumpOn = false;
			boolean prevBypassOn = false;
			boolean bypassOn = false;
			// create queues
			int displayQlen = new LcQueueParams("Display", lcp, LOGGER).queueMaxLen;
			BlockingQueue<LcMessage> displayQueue = new ArrayBlockingQueue<>(displayQlen);
			int fileQlen = new LcQueueParams("SaveFile", lcp, LOGGER).queueMaxLen;
			BlockingQueue<LcMessage> fileQueue = new ArrayBlockingQueue<>(fileQlen);
			int dbQlen = new LcQueueParams("SaveData", lcp, LOGGER).queueMaxLen;
			BlockingQueue<LcMessage> dbQueue = new ArrayBlockingQueue<>(dbQlen);
			LcQueuer displayQueuer = null;
			LcQueuer fileQueuer = null;
			LcQueuer dbQueuer = null;
			if (lcp.showDisp)
				try
				{
					LOGGER.log(((lcp.dbg.contains("LcDisplayReadings"))?Level.INFO:Level.FINE), "Disp: creating display queue...");
					displayQueuer = new LcQueuer(displayQueue, LOGGER);
					LOGGER.log(((lcp.dbg.contains("LcDisplayReadings"))?Level.INFO:Level.FINE), "Created display queue...");
					LcQueueSink displayer = new LcDisplayReadings(displayQueue, lcp.clone(), LOGGER);
					LOGGER.log(((lcp.dbg.contains("LcDisplayReadings"))?Level.INFO:Level.FINE), "Created LcDisplayReadings thread, about to start LcDisplayReadings thread...");
					new Thread(displayer).start();
					LOGGER.log(((lcp.dbg.contains("LcDisplayReadings"))?Level.INFO:Level.FINE), "Started LcDisplayReadings thread...");
				} catch (Exception e)
				{
					LOGGER.log(Level.SEVERE, "Tried to start LcDisplayReadings but is broken: " + e.toString());
					e.printStackTrace();
				}
			if (lcp.saveFile)
				try
				{
					LOGGER.log(((lcp.dbg.contains("LcFileSaver"))?Level.INFO:Level.FINE), "File: creating file queue...");
					fileQueuer = new LcQueuer(fileQueue, LOGGER);
					LOGGER.log(((lcp.dbg.contains("LcFileSaver"))?Level.INFO:Level.FINE), "Created save file queue...");
					LcQueueSink fileSaver = new LcFileSaver(fileQueue, lcp.clone(), LOGGER);
					LOGGER.log(((lcp.dbg.contains("LcFileSaver"))?Level.INFO:Level.FINE), "Created LcFileSaver thread, about to start LcFileSaver thread...");
					new Thread(fileSaver).start();
					LOGGER.log(((lcp.dbg.contains("LcFileSaver"))?Level.INFO:Level.FINE), "Started LcFileSaver thread...");
				} catch (Exception e)
				{
					LOGGER.log(Level.SEVERE, "Tried to start fileSaver but is broken: " + e.toString());
					e.printStackTrace();
				}
			if (lcp.saveDb)
				try
				{
					LOGGER.log(((lcp.dbg.contains("LcDbSaver"))?Level.INFO:Level.FINE), "Loading dbDrivers...");
					if (lcp.remainConn) LcSQLcrud.loadDbDrv(lcp.clone(), LOGGER);
					LOGGER.log(((lcp.dbg.contains("LcDbSaver"))?Level.INFO:Level.FINE), "Loaded DbDrv, creating db queue...");
					dbQueuer = new LcQueuer(dbQueue, LOGGER);
					LOGGER.log(((lcp.dbg.contains("LcDbSaver"))?Level.INFO:Level.FINE), "Created save db queue...");
					LcQueueSink dbSaver = new LcDbSaver(dbQueue, lcp.clone(), LOGGER);
					LOGGER.log(((lcp.dbg.contains("LcDbSaver"))?Level.INFO:Level.FINE), "Created LcDbSaver thread, about to start LcDbSaver thread...");
					new Thread(dbSaver).start();
					LOGGER.log(((lcp.dbg.contains("LcDbSaver"))?Level.INFO:Level.FINE), "Started LcDbSaver thread...");
				} catch (Exception e)
				{
					LOGGER.log(Level.SEVERE, "Tried to start dbSaver but is broken: " + e.toString());
				}
			if (lcp.portNumber > 1)
				try
				{ // initialising the Socket Server
					LOGGER.log(((lcp.dbg.contains("LCServer"))?Level.INFO:Level.FINE), "LCServer: starting...");
					LcSvr sSvr = new LcSvr(lcp.portNumber, lcp.clone(), LOGGER); //
					new Thread(sSvr).start();
					LOGGER.log(((lcp.dbg.contains("LCServer"))?Level.INFO:Level.FINE), "LCServer: started.");
				} catch (Exception e)
				{
					LOGGER.log(Level.SEVERE, "Tried to start LCServer but is broken: " + e.toString());
					e.printStackTrace();
				}
			LOGGER.log(((lcp.dbg.contains("LCServer"))?Level.INFO:Level.FINE), "Some queue(s) perhaps created, display and web server may have been started.");
			LcImmCtl.immersionOverrideState = false; // (toggle begins period of
														// immersion supply,
														// toggling while on
														// switches off)
			LcImmCtl.immOvrStartMillis = 0;
			Long runSeconds = 0L;
			Long lastSaveRunSecs = runSeconds;
			LcImmCtl.lastOverrideState = false;
			while (lcontinue)
			{
				LcReading rd = new LcSolarReading();
				curDt = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat();
				sdf.applyPattern("yyyyMMddHHmmss");
				LOGGER.log(Level.FINER, "Starting new measurement at: " + sdf.format(curDt.getTime())); 
				rd.setDtm(sdf.format(curDt.getTime()));
				runSeconds = (curDt.getTimeInMillis() - startDt.getTimeInMillis()) / 1000;
				rd.setTimeLapse(Long.toString(runSeconds));
				LcAdcReader.readAdc(iop, rd, lcp.clone(), LOGGER);
				// do immersion controller stuff, date time control and...
				LcImmCtl.ckImmCtl(lcp); // check if override requested or if override time limit reached
				doImmCtl(lcp, rd, iop, LOGGER);
				// ******************************************************************************************
				// ************* find lowest HWC temperature...
				// HWCout could be warmer when no flow and HWC is cold otherwise usually cooler
				Double lowest = 0.0;
				if (rd.getHwcLower() < rd.getHwcOut())
					lowest = rd.getHwcLower();
				else
					lowest = rd.getHwcOut();
				// ******************************************************************************************
				// ************* apply control logics...
				prevPumpOn = pumpOn;
				pumpOn = doControl("Pump", lowest, pumpOn, lcp, rd, iop, LOGGER);
				prevBypassOn = bypassOn;
				bypassOn = doControl("Byps", lowest, bypassOn, lcp, rd, iop, LOGGER);
				// ******************************************************************************************
				// ************** Calculate power
				if (prevPumpOn && !prevBypassOn) // last period must have flow in exchanger
				{ // ### P = Tdiff (degK) * flow (g/s) * spHeat (J/g/degK) = J/s ie Watt ###
					try
					{
						LOGGER.log(((lcp.dbg.contains("InstantPower"))?Level.INFO:Level.FINER), "About to calculate instantaneous power: ("
									+ Double.toString(rd.getHwcIn()) + " - " + Double.toString(rd.getHwcOut())
									+ ")degKchange * " + Double.toString(rd.getFlowRateGPS()) + "gps * 4J/g/degK ...");
						double diff = rd.getHwcIn() - rd.getHwcOut();
						rd.setPower(diff * rd.getFlowRateGPS() * 4.0);
						LOGGER.log(((lcp.dbg.contains("InstantPower"))?Level.INFO:Level.FINER), "Calculated instantaneous power: " + Double.toString(rd.getPower()));
					} catch (Exception x)
					{
						LOGGER.log(((lcp.dbg.contains("InstantPower"))?Level.INFO:Level.SEVERE), "Cannot calculate Current instantaneous power: " + x.toString());
					}
				} else
				{
					rd.setPower(0.0);
					LOGGER.log(((lcp.dbg.contains("InstantPower"))?Level.INFO:Level.FINER), "Set instantaneous power to zero: prevPumpOn: " + prevPumpOn + ", prevBypassOn: " + prevBypassOn);
				}
				// ******************************************************************************************
				// ************************ write to database etc... ****************************************
				// ******************************************************************************************
				// allow demand detection circuit time to settle after possible
				// supply state change
				Thread.sleep(1000);
				// 14 immersionDemand update log file etc...
				rd.setImmersionDemand((iop.gpio.getState(iop.ioPin03).toString() == "HIGH") ? true : false);
				LOGGER.log(Level.FINER, "Getting current queue lengths... ");
				if (displayQueuer != null)
					rd.setDispQsize(displayQueue.size());
				if (fileQueuer != null)
					rd.setFileQsize(fileQueue.size());
				if (dbQueuer != null)
					rd.setDataQsize(dbQueue.size());
				LOGGER.log(Level.FINER, "Writing data into queues: " + rd.toString());
				if (lastSaveRunSecs + lcp.logPeriod < runSeconds || lastSaveRunSecs == 0L)
				{ // readings saved less frequently than displayed
					if (fileQueuer != null)
						fileQueuer.queueAdd((LcSolarReading) rd);
					if (dbQueuer != null)
						dbQueuer.queueAdd((LcSolarReading) rd);
					lastSaveRunSecs = runSeconds;
				}
				if (displayQueuer != null)
					displayQueuer.queueAdd((LcSolarReading) rd);
				LOGGER.log(Level.FINER, "Sleeping...");
				LcHeartbeats.mkHeartbeats(iop); // kill 10 seconds
			}
			if (lcp.msgLev > 1)
				LOGGER.log(Level.FINER, "LoggerController: shutting down iop.gpio...");
			iop.gpio.shutdown();
			if (lcp.msgLev > 1)
				LOGGER.log(Level.FINER, "LoggerController: setPin: GPIO shut down.");
		} catch (Exception e)
		{
			if (lcp.msgLev > 1)
				LOGGER.log(Level.SEVERE, "LoggerController: caught: " + e.toString());
			e.printStackTrace();
		} finally
		{
			if (lcp.msgLev > 1)
				LOGGER.log(Level.FINER, "Exiting LoggerController");
			System.out.close();
			System.exit(0);
		}
	}
	private static boolean doControl(String type, double ctlVal, boolean ctlOn, LcParams lcp, LcReading rd, LcIoPins iop, Logger LOGGER)
	{
		Boolean prevCtlOn = ctlOn;
		DecimalFormat df = new DecimalFormat("##0.000");
		if (type.equals("Byps"))
		{	//bypassOn = doControl("Byps", bypassOn, lcp, rd, iop, LOGGER);
			// ******************************************************************************************
			// ************************* apply hysteresis and do bypass control
			// ******************************************************************************************
			// when next state change is -ve going add hysNegPump to ctlVal
			// or when +ve going add hysPosPump to ctlVal
			// => pumpTrigger
			double bypsTrigger = Double.valueOf(df.format(ctlVal + ((prevCtlOn)?lcp.hysNegByps:lcp.hysPosByps)));
			LOGGER.log(((lcp.dbg.contains("BypsCtl"))?Level.INFO:Level.FINER),
						"****BtpsCtl: prevBypsOn: " + Boolean.toString(prevCtlOn) + 
						", trigger = " + Double.toString(bypsTrigger));
			if (rd.getTin() > bypsTrigger)
			{
				iop.gpio.setState(false, iop.ioPin06);
				ctlOn = false;
				rd.setBypassOn(false);
				LOGGER.log(((lcp.dbg.contains("BypsCtl"))?Level.INFO:Level.FINER), "####*BtpsCtl: Bypass:OFF, Tin = " +
						rd.getStIn(3));
			} else
			{
				iop.gpio.setState(true, iop.ioPin06);
				ctlOn = true;
				rd.setBypassOn(true);
				LOGGER.log(((lcp.dbg.contains("BypassCtl"))?Level.INFO:Level.FINER), "####BtpsCtl: Bypass:ON, Tin = " +
						rd.getStIn(3));
			}
		}
		else if (type.equals("Pump"))
		{
			// ******************************************************************************************
			// apply hysteresis and do pump control
			// ******************************************************************************************
			// when next state change is -ve going add hysNegPump to ctlVal
			// or when +ve going add hysPosPump to ctlVal
			// => pumpTrigger
			double pumpTrigger = Double.valueOf(df.format(ctlVal + ((prevCtlOn)?lcp.hysNegPump:lcp.hysPosPump)));
			LOGGER.log(((lcp.dbg.contains("PumpCtl"))?Level.INFO:Level.FINER),
						"**PumpCtl: prevPumpOn: " + Boolean.toString(prevCtlOn) + 
						", trigger = " + Double.toString(pumpTrigger));
			if (rd.getTpanel() > pumpTrigger)
			{
				iop.gpio.setState(true, iop.ioPin05); // #pump on
				ctlOn = true;
				rd.setPumpOn(true);
				LOGGER.log(((lcp.dbg.contains("PumpCtl"))?Level.INFO:Level.FINER), "**PumpCtl: Pump:ON, Tpanel = " +
						rd.getStPanel(3));
			} else
			{
				iop.gpio.setState(false, iop.ioPin05); // #pump off
				ctlOn = false;
				rd.setPumpOn(false);
				LOGGER.log(((lcp.dbg.contains("PumpCtl"))?Level.INFO:Level.FINER), "**PumpCtl: Pump:OFF, Tpanel = " +
						rd.getStPanel(3));
			}
		}
		return ctlOn;
	}
	private static void doImmCtl(LcParams lcp, LcReading rd, LcIoPins iop, Logger LOGGER)
	{
		Calendar curDt = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern("HH");
		int thisHour = Integer.parseInt(sdf.format(curDt.getTime()));
		LOGGER.log(Level.FINER, "thisHour = " + sdf.format(curDt.getTime()));
		if (LcImmCtl.immersionOverride)
		{
			iop.gpio.setState(true, iop.ioPin04); // supply
			iop.gpio.setState(true, iop.ioPin02);  // indicator
			rd.setImmersionSupply(true);
			rd.setImmersionOverride(true);
			LOGGER.log(((lcp.dbg.contains("ImmCtl"))?Level.INFO:Level.FINER), "OVR: ImmersionSupply:ON");
	
		} else
		{
			rd.setImmersionOverride(false);
			if (thisHour <= 7 || thisHour > 16
					|| !(curDt.get(Calendar.MONTH) > 4 && curDt.get(Calendar.MONTH) < 10))
			{
				iop.gpio.setState(true, iop.ioPin04); // supply
				iop.gpio.setState(true, iop.ioPin02); // indicator
				rd.setImmersionSupply(true);
				LOGGER.log(((lcp.dbg.contains("ImmCtl"))?Level.INFO:Level.FINER), "ImmersionSupply:ON");
			} else
			{
				iop.gpio.setState(false, iop.ioPin04);
				iop.gpio.setState(false, iop.ioPin02);
				rd.setImmersionSupply(false);
				LOGGER.log(((lcp.dbg.contains("ImmCtl"))?Level.INFO:Level.FINER), "ImmersionSupply:OFF");
			}
		}
	}
}
