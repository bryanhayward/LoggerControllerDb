package elC;
/**
 * @(#)LoggerController: LcHeartbeats.java
 * Makes heart beat like flashes on indicator LED
 *  for ~10 seconds
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

public class LcHeartbeats
{
	// 
	public static void mkHeartbeats(LcIoPins iop) throws InterruptedException
	{
		for (int i = 0 ; i < 10; i++)
		{
			iop.gpio.setState(false, iop.ioPin07); // bus2.write_pin(4, 0) #alarm / heart beat LED
			Thread.sleep(500);
			iop.gpio.setState(true, iop.ioPin07); // bus2.write_pin(4, 0) #alarm / heart beat LED
			Thread.sleep(200);
			iop.gpio.setState(false, iop.ioPin07); // bus2.write_pin(4, 0) #alarm / heart beat LED
			Thread.sleep(100);
			iop.gpio.setState(true, iop.ioPin07); // bus2.write_pin(4, 0) #alarm / heart beat LED
			Thread.sleep(200);
			iop.gpio.setState(false, iop.ioPin07); // bus2.write_pin(4, 0) #alarm / heart beat LED
		}
	}

}
