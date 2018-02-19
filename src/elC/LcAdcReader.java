package elC;
/**
 * @(#)LoggerController: LcAdcReader.java
 * Uses adcMap java Map object to query hardware and obtain
 * readings record data.
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

import java.util.logging.Level;
import java.util.logging.Logger;

public class LcAdcReader
{
	public static void readAdc(LcIoPins iop, LcReading rd, LcParams lcp, Logger LOGGER)
	{
		iop.adcMap.forEach((pinKey,pin)->
		{
			if (pinKey == "Channel-0" || pinKey == "Channel-1" ||
				pinKey == "Channel-2" || pinKey == "Channel-3" )
			{
				LOGGER.log(Level.FINEST, "PinKey: " + Double.toString( iop.adcProvA.getAnalogValue(pin.getPin()) ) + ",");
				switch (pinKey)
				{
				case "Channel-0": //1 Tpanel
					rd.setTpanel(iop.adcProvA.getAnalogValue(pin.getPin()) * 100);
					break;
				case "Channel-1": //2 HWCin
					rd.setHwcIn(iop.adcProvA.getAnalogValue(pin.getPin()) * 100);
					break;
				case "Channel-2": //3 HWClower
					rd.setHwcLower(iop.adcProvA.getAnalogValue(pin.getPin()) * 100);
					break;
				case "Channel-3": //4 HWCout
					rd.setHwcOut(iop.adcProvA.getAnalogValue(pin.getPin()) * 100);
					break;
				}
			}
			if (pinKey == "Channel-4" || pinKey == "Channel-5" ||
				pinKey == "Channel-6" || pinKey == "Channel-7" )
			{
				LOGGER.log(Level.FINEST, "PinKey: " + Double.toString( iop.adcProvB.getAnalogValue(pin.getPin()) ) + ",");
				switch (pinKey)
				{
				case "Channel-4": //5 Tin
					rd.setTin(iop.adcProvB.getAnalogValue(pin.getPin()) * 100);
					break;
				case "Channel-5": //6 HWCupper
					rd.setHwcUpper(iop.adcProvB.getAnalogValue(pin.getPin()) * 120);
					break;
				case "Channel-6": //7 FlowRateGPS
					double tmp;
					tmp = iop.adcProvB.getAnalogValue(pin.getPin());
					rd.setFlowRateGPS(((tmp>0)?tmp*9.08:0)); // adc does produce -ve values!! (pulse counter circuit seems to give small -ve voltage when no pulses to count - ignore..) 
					break;
				case "Channel-7": //8 Psun
					rd.setPsun(iop.adcProvB.getAnalogValue(pin.getPin()) * 10);
					break;
				}
			}
		});
	}
}
