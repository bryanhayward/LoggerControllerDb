package elC;
/**
 * @(#)SolarLoggerController: LcSolarReading.java
 * This object encapsulates all of the data obtained
 * from the PI IO and ADC extender hardware. 
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

public class LcSolarReading implements LcReading, Cloneable
{
	private boolean bypassOn = false;
	private int dataQsize = 0;
	private int dispQsize = 0;
	private String dTm = "";
	private int fileQsize = 0;
	private Double flowRateGPS = 0.0;
	private Double hwcIn = 0.0;
	private Double hwcLower = 0.0;
	private Double hwcOut = 0.0;
	private Double hwcUpper = 0.0;
	private boolean immersionDemand = false;
	private boolean immersionOverride = false;
	private boolean immersionSupply = false;
	private Double power = 0.0;
	private Double pSun = 0.0;
	private boolean pumpOn = false;
	private String timeLapse = "";
	private Double tIn = 0.0;
	private Double tPanel = 0.0;

	public LcSolarReading()
	{
		pumpOn = false;
		bypassOn = false;
		immersionDemand = false;
		immersionOverride = false;
		immersionSupply = false;
		dispQsize = 0;
		fileQsize = 0;
		dataQsize = 0;
		tPanel = 0.0;
		hwcIn = 0.0;
		hwcLower = 0.0;
		hwcOut = 0.0;
		tIn = 0.0;
		hwcUpper = 0.0;
		flowRateGPS = 0.0;
		pSun = 0.0;
		power = 0.0;
		timeLapse = "0";
		dTm = "20140511024500";
	}

    public LcSolarReading clone() {
        try {
            return (LcSolarReading)super.clone();
        }
        catch (CloneNotSupportedException e) {
            // This should never happen
        }
		return null;
    }

	@Override
	public boolean getBypassOn()
	{
		return bypassOn;
	}
	@Override
	public int getDataQsize()
	{
		return dataQsize;
	}
	@Override
	public int getDispQsize()
	{
		return dispQsize;
	}
	@Override
	public String getDtm()
	{
		return dTm;
	}
	@Override
	public int getFileQsize()
	{
		return fileQsize;
	}
	@Override
	public double getFlowRateGPS()
	{
		return flowRateGPS;
	}
	@Override
	public double getHwcIn()
	{
		return hwcIn;
	}
	@Override
	public double getHwcLower()
	{
		return hwcLower;
	}
	@Override
	public double getHwcOut()
	{
		return hwcOut;
	}
	@Override
	public double getHwcUpper()
	{
		return hwcUpper;
	}
	@Override
	public boolean getImmersionDemand()
	{
		return immersionDemand;
	}
	@Override
	public boolean getImmersionOverride()
	{
		return immersionOverride;
	}
	/* (non-Javadoc)
	 * @see LC.LcRead#getImmersionSupply()
	 */
	@Override
	public boolean getImmersionSupply()
	{
		return immersionSupply;
	}
	@Override
	public double getPower()
	{
		return power;
	}
	@Override
	public double getPsun()
	{
		return pSun;
	}
	@Override
	public boolean getPumpOn()
	{
		return pumpOn;
	}
	@Override
	public String getTimeLapse()
	{
		return timeLapse;
	}
	@Override
	public double getTin()
	{
		return tIn;
	}
	@Override
	public double getTpanel()
	{
		return tPanel;
	}
	@Override
	public void setBypassOn(boolean bypassOn)
	{
		this.bypassOn = bypassOn;
	}
	@Override
	public void setDataQsize(int dataQsize)
	{
		this.dataQsize = dataQsize;
	}
	@Override
	public void setDispQsize(int dispQsize)
	{
		this.dispQsize = dispQsize;
	}
	@Override
	public void setDtm(String dTm)
	{
		this.dTm = dTm;
	}
	@Override
	public void setFileQsize(int fileQsize)
	{
		this.fileQsize = fileQsize;
	}
	@Override
	public void setFlowRateGPS(double flowRateGPS)
	{
		this.flowRateGPS = flowRateGPS;
	}
	@Override
	public void setHwcIn(double hwcIn)
	{
		this.hwcIn = hwcIn;
	}
	@Override
	public void setHwcLower(double hwcLower)
	{
		this.hwcLower = hwcLower;
	}
	@Override
	public void setHwcOut(double hwcOut)
	{
		this.hwcOut = hwcOut;
	}
	@Override
	public void setHwcUpper(double hwcUpper)
	{
		this.hwcUpper = hwcUpper;
	}
	@Override
	public void setImmersionDemand(boolean immersionDemand)
	{
		this.immersionDemand = immersionDemand;
	}
	@Override
	public void setImmersionOverride(boolean immersionOverride)
	{
		this.immersionOverride = immersionOverride;
	}
	@Override
	public void setImmersionSupply(boolean pImmersionSupply)
	{
		this.immersionSupply = pImmersionSupply;
	}
	@Override
	public void setPower(double power)
	{
		this.power = power;
	}
	@Override
	public void setPsun(double pSun)
	{
		this.pSun = pSun;
	}
	@Override
	public void setPumpOn(boolean pumpOn)
	{
		this.pumpOn = pumpOn;
	}
	@Override
	public void setTimeLapse(String timeLapse)
	{
		this.timeLapse = timeLapse;
	}
	@Override
	public void setTin(double tIn)
	{
		this.tIn = tIn;
	}
	@Override
	public void setTpanel(double tPanel)
	{
		this.tPanel = tPanel;
	}
	@Override
	public String toCSV()
	{ // LoggerController file CSV format
		return (getDate() + "," + getTime() + "," + timeLapse + "," + dTm + "," + getShwcIn(3) + "," + getShwcLower(3)
				+ "," + getShwcOut(3) + "," + getStPanel(3) + "," + getStIn(3) + "," + getShwcUpper(3) + ","
				+ getSflowRateGPS(3) + "," + getSpSun(3) + "," + getSimmersionOverride() + "," + getSpumpOn() + ","
				+ getSimmersionDemand() + "," + getSbypassOn() + "," + getSimmersionSupply() + "," + getSpower(3) + ","
				+ getSdispQsize() + "," + getSfileQsize() + "," + getSdataQsize());
	}

	@Override
	public String toString()
	{
		StringBuilder bs = new StringBuilder();
		bs.append("Reading:\n");
		bs.append("theDate=" + getDate() + "\n");
		bs.append("theTime=" + getTime() + "\n");
		bs.append("timeLapse=" + timeLapse + "\n");
		bs.append("dTm=" + dTm + "\n");
		bs.append("hwcIn=" + getShwcIn(3) + "\n");
		bs.append("hwcLower=" + getShwcLower(3) + "\n");
		bs.append("hwcOut=" + getShwcOut(3) + "\n");
		bs.append("tPanel=" + getStPanel(3) + "\n");
		bs.append("tIn=" + getStIn(3) + "\n");
		bs.append("hwcUpper=" + getShwcUpper(3) + "\n");
		bs.append("flowRateGPS=" + getSflowRateGPS(3) + "\n");
		bs.append("pSun=" + getSpSun(3) + "\n");
		bs.append("ImmersionOverride=" + getSimmersionOverride() + "\n");
		bs.append("PumpOn=" + getSpumpOn() + "\n");
		bs.append("ImmersionDemand=" + getSimmersionDemand() + "\n");
		bs.append("BypassOn=" + getSbypassOn() + "\n");
		bs.append("ImmersionSupply=" + getSimmersionSupply() + "\n");
		bs.append("Power=" + getSpower(3) + "\n");
		bs.append("DispQsize=" + getSdispQsize() + "\n");
		bs.append("Filesize=" + getSfileQsize() + "\n");
		bs.append("DataQsize=" + getSdataQsize() + "\n");
		return (bs.toString());
	}

}
