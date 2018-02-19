package elC;
/**
 * @(#)LoggerController: LcIoPins.java
 * 
 * Performs definition and initialisation of input and output
 * pins of the RPi hardware extenders.
 * 
 * Uses Pi4J and WiringPi libraries for licence see source in ../lib.
 * 
 * The java Map class is used to collect the A2D data.
 * 
 * At debug > 9 on initialisation all outputs are set on for one
 * second to test the power relays and solenoid valves.
 *  
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.gpio.extension.mcp.MCP3424GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP3424Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.i2c.I2CBus;

public class LcIoPins
{
	public Logger LOGGER;
	public GpioController gpio;
	public GpioPinDigitalInput ioPin01;
	public GpioPinDigitalInput ioPin03;
	public GpioPinDigitalOutput ioPin02;
	public GpioPinDigitalOutput ioPin04;
	public GpioPinDigitalOutput ioPin05;
	public GpioPinDigitalOutput ioPin06;
	public GpioPinDigitalOutput ioPin07;
	private GpioPinAnalogInput adcInput1;
	private GpioPinAnalogInput adcInput2;
	private GpioPinAnalogInput adcInput3;
	private GpioPinAnalogInput adcInput4;
	private GpioPinAnalogInput adcInput5;
	private GpioPinAnalogInput adcInput6;
	private GpioPinAnalogInput adcInput7;
	private GpioPinAnalogInput adcInput8;
	public Map<String, GpioPinAnalogInput> adcMap = new HashMap<String, GpioPinAnalogInput>();
	public MCP3424GpioProvider adcProvA;
	public MCP3424GpioProvider adcProvB;
	public MCP23017GpioProvider ioProvA;
	public MCP23017GpioProvider ioProvB;
	
	public LcIoPins()
	{ }
	public LcIoPins(Logger logger)
	{
		LOGGER = logger;
	}
	public void provLcIoPins(LcParams lcp)
	{
		try
		{
			// create gpio controller
			LOGGER.log(Level.FINEST, "About to get GpioController and provision pins...");
			gpio = GpioFactory.getInstance();
			ioProvA = new MCP23017GpioProvider(I2CBus.BUS_1, 0x20); // bus1 = IoPi(i2c_bus, 0x20)
			ioProvB = new MCP23017GpioProvider(I2CBus.BUS_1, 0x21); // bus2 = IoPi(i2c_bus, 0x21)
			adcProvA = new MCP3424GpioProvider(I2CBus.BUS_1, 0x68, 14, 2); // adc = ADCPi(i2c_bus, 0x68, 0x69, 0)
			adcProvB = new MCP3424GpioProvider(I2CBus.BUS_1, 0x69, 14, 2); // adc = ADCPi(i2c_bus, 0x68, 0x69, 0)
			ioPin01 = gpio.provisionDigitalInputPin(ioProvA, MCP23017Pin.GPIO_A1, "Input-A1", PinPullResistance.PULL_UP); // immersion override
			ioPin03 = gpio.provisionDigitalInputPin(ioProvA, MCP23017Pin.GPIO_A3, "Input-A3"); // immersion demand
			// provision gpio output pins and make sure they are all LOW at startup
			LOGGER.log(Level.FINEST, "Now provisioned Ipins, about to provision Opins...");
			ioPin02 = gpio.provisionDigitalOutputPin(ioProvA, MCP23017Pin.GPIO_A2, "Output-A2", PinState.LOW); 
			ioPin04 = gpio.provisionDigitalOutputPin(ioProvB, MCP23017Pin.GPIO_A0, "Output-B4", PinState.LOW);
			ioPin05 = gpio.provisionDigitalOutputPin(ioProvB, MCP23017Pin.GPIO_A1, "Output-B5", PinState.LOW);
			ioPin06 = gpio.provisionDigitalOutputPin(ioProvB, MCP23017Pin.GPIO_A2, "Output-B6", PinState.LOW);
			ioPin07 = gpio.provisionDigitalOutputPin(ioProvB, MCP23017Pin.GPIO_A3, "Output-B7", PinState.LOW);
			// provision gpio input pins from MCP3424
			LOGGER.log(Level.FINEST, "Now provisioned Opins, about to provision ADCpins...");
			adcInput1 = gpio.provisionAnalogInputPin(adcProvA, MCP3424Pin.GPIO_CH0, "Channel-0");
			// adcInput1.setProperty(arg0, arg1);
			adcInput2 = gpio.provisionAnalogInputPin(adcProvA, MCP3424Pin.GPIO_CH1, "Channel-1");
			adcInput3 = gpio.provisionAnalogInputPin(adcProvA, MCP3424Pin.GPIO_CH2, "Channel-2");
			adcInput4 = gpio.provisionAnalogInputPin(adcProvA, MCP3424Pin.GPIO_CH3, "Channel-3");
			adcInput5 = gpio.provisionAnalogInputPin(adcProvB, MCP3424Pin.GPIO_CH0, "Channel-4");
			adcInput6 = gpio.provisionAnalogInputPin(adcProvB, MCP3424Pin.GPIO_CH1, "Channel-5");
			adcInput7 = gpio.provisionAnalogInputPin(adcProvB, MCP3424Pin.GPIO_CH2, "Channel-6");
			adcInput8 = gpio.provisionAnalogInputPin(adcProvB, MCP3424Pin.GPIO_CH3, "Channel-7");

			adcMap.put("Channel-0", adcInput1);
			adcMap.put("Channel-1", adcInput2);
			adcMap.put("Channel-2", adcInput3);
			adcMap.put("Channel-3", adcInput4);
			adcMap.put("Channel-4", adcInput5);
			adcMap.put("Channel-5", adcInput6);
			adcMap.put("Channel-6", adcInput7);
			adcMap.put("Channel-7", adcInput8);

			if (lcp.msgLev > 1) printProps();
			LOGGER.log(Level.FINEST, "Now provisioned IO and ADCpins...");
			gpio.setState(false, ioPin04); // bus2.write_pin(1, 0) #immPower
			gpio.setState(false, ioPin05); // bus2.write_pin(2, 0) #pump
			gpio.setState(false, ioPin06); // bus2.write_pin(3, 0) #bypass
			gpio.setState(false, ioPin07); // bus2.write_pin(4, 0) #alarm
			if (lcp.msgLev > 10)
			{ // flash everything
				gpio.setState(true, ioPin04); // bus2.write_pin(1, 0) #immPower
				gpio.setState(true, ioPin05); // bus2.write_pin(2, 0) #pump
				gpio.setState(true, ioPin06); // bus2.write_pin(3, 0) #bypass
				gpio.setState(true, ioPin07); // bus2.write_pin(4, 0) #alarm
				Thread.sleep(1000);
				gpio.setState(false, ioPin04); // bus2.write_pin(1, 0) #immPower
				gpio.setState(false, ioPin05); // bus2.write_pin(2, 0) #pump
				gpio.setState(false, ioPin06); // bus2.write_pin(3, 0) #bypass
				gpio.setState(false, ioPin07); // bus2.write_pin(4, 0) #alarm
			}
			LOGGER.log(Level.FINEST, "Turned off pump, bypass, immPower and alarm...");
		}
		catch(Exception e)
		{
			LOGGER.log(Level.FINEST, "caught: " + e.toString() + " Exiting @ LcIoPins");
			System.exit(0);
		}
		finally
		{
			LOGGER.log(Level.FINEST, "Finally: done LcIoPins...");
		}
		return;	
	}
	private void printProps()
	{
		LOGGER.log(Level.FINEST, "debug: about to print properties for " + Integer.toString(adcMap.size()) + " adc pins...");
		adcMap.forEach((pinKey,pin)-> // nested j8 map iterators using lambdas
		{
			Map<String, String> propsMap = pin.getProperties();
			LOGGER.log(Level.FINEST, "...debug: adc pin " + pinKey + " properties (" + Integer.toString(propsMap.size()) + "):");
			((Map<String,String>) propsMap).forEach((propKey,prop) -> LOGGER.log(Level.FINEST, "...    Key: " + propKey + ", value: " + prop));
			LOGGER.log(Level.FINEST, "...Pin.toString: " + pin.toString());
			LOGGER.log(Level.FINEST, "...Pin.getMode.toString: " + pin.getMode().toString());
		});
	}
}
