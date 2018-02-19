package elC;
/**
 * @(#)LoggerController: LcReading.java.
 * Maintain reading records.
 * Overridden methods provided to limit number of decimal places in getter return values
 *  and return types as double, string.
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public interface LcReading
{
	public default String getDate()
	{
		LocalDate date = null;
		try
		{
			date = LocalDate.of(Integer.parseInt(getDtm().substring(0, 4)), Integer.parseInt(getDtm().substring(4, 6)),
					Integer.parseInt(getDtm().substring(6, 8))); // ie ccyymmdd
		} catch (NumberFormatException ne) { return "########"; }		
		Month month = date.getMonth();
		Locale locale = Locale.getDefault();
		return getDtm().substring(6, 8) + " " + month.getDisplayName(TextStyle.SHORT, locale) + " " + getDtm().substring(0, 4);
	}
	public default double getFlowRateGPS(int nDp)
	{
		return mkNdp(getFlowRateGPS(), nDp);
	}
	public default double getHwcIn(int nDp)
	{
		return mkNdp(getHwcIn(), nDp);
	}
	public default double getHwcLower(int nDp)
	{
		return mkNdp(getHwcLower(), nDp);
	}
	public default double getHwcOut(int nDp)
	{
		return mkNdp(getHwcOut(), nDp);
	}
	public default double getHwcUpper(int nDp)
	{
		return mkNdp(getHwcUpper(), nDp);
	}
	public default double getPower(int nDp)
	{
		return mkNdp(getPower(), nDp);
	}
	public default double getPsun(int nDp)
	{
		return mkNdp(getPsun(), nDp);
	}
	public default String getSbypassOn()
	{
		return ((getBypassOn()) ? "1" : "0");
	}
	public default String getSdataQsize()
	{
		return Integer.toString(getDataQsize());
	}
	public default String getSdispQsize()
	{
		return Integer.toString(getDispQsize());
	}
	public default String getSfileQsize()
	{
		return Integer.toString(getFileQsize());
	}
	public default String getSflowRateGPS(int nDp)
	{
		return mkNdp(Double.toString(getFlowRateGPS()), nDp);
	}
	public default String getShwcIn(int nDp)
	{
		return mkNdp(Double.toString(getHwcIn()), nDp);
	}
	public default String getShwcLower(int nDp)
	{
		return mkNdp(Double.toString(getHwcLower()), nDp);
	}
	public default String getShwcOut(int nDp)
	{
		return mkNdp(Double.toString(getHwcOut()), nDp);
	}
	public default String getShwcUpper(int nDp)
	{
		return mkNdp(Double.toString(getHwcUpper()), nDp);
	}
	public default String getSimmersionDemand()
	{
		return ((getImmersionDemand()) ? "1" : "0");
	}
	public default String getSimmersionOverride()
	{
		return ((getImmersionOverride()) ? "1" : "0");
	}
	public default String getSimmersionSupply()
	{
		return ((getImmersionSupply()) ? "1" : "0");
	}
	public default String getSpower(int nDp)
	{
		return mkNdp(Double.toString(getPower()), nDp);
	}
	public default String getSpSun(int nDp)
	{
		return mkNdp(Double.toString(getPsun()), nDp);
	}
	public default String getSpumpOn()
	{
		return ((getPumpOn()) ? "1" : "0");
	}

	public default String getStIn(int nDp)
	{
		return mkNdp(Double.toString(getTin()), nDp);
	}

	public default String getStPanel(int nDp)
	{
		return mkNdp(Double.toString(getTpanel()), nDp);
	}

	public default String getTime()
	{
		return getDtm().substring(8, 10) + ":" + getDtm().substring(10, 12) + ":" + getDtm().substring(12, 14);
	}

	public default double getTin(int nDp)
	{
		return mkNdp(getTin(), nDp);
	}

	public default double getTpanel(int nDp)
	{
		return mkNdp(getTpanel(), nDp);
	}

	String toCSV();

	boolean getBypassOn();

	int getDataQsize();

	int getDispQsize();

	String getDtm();

	int getFileQsize();

	double getFlowRateGPS();

	double getHwcIn();

	double getHwcLower();
	
	double getHwcOut();

	double getHwcUpper();

	boolean getImmersionDemand();

	boolean getImmersionOverride();

	boolean getImmersionSupply();
	
	double getPower();
	
	double getPsun();
	
	boolean getPumpOn();
	
	String getTimeLapse();

	double getTin();

	double getTpanel();

	default double mkNdp(double mdpd, int nDp)
	{
		String pattern = "###0.";
		for (int i = 0; i < nDp; i++) pattern = pattern.concat("#");
		DecimalFormat decimalFormat = new DecimalFormat(pattern);
		decimalFormat.setRoundingMode(java.math.RoundingMode.UP);
		double retVal = 0.0;
		try
		{
			retVal = Double.parseDouble(decimalFormat.format(mdpd));
		} catch (Exception ex)
		{
			retVal = 0.0;
		}
		return (retVal);
	}

	default String mkNdp(String mdp, int nDp)
	{ // pads to nDp trailing zeros
		if (mdp == null) return "";
		String pattern = "###0.";
		for (int i = 0; i < nDp; i++) pattern = pattern.concat("#");
		DecimalFormat decimalFormat = new DecimalFormat(pattern);
		decimalFormat.setDecimalSeparatorAlwaysShown(true);
		decimalFormat.setMinimumFractionDigits(nDp);
		decimalFormat.setRoundingMode(java.math.RoundingMode.UP);
		String retVal = "";
		try
		{
			retVal = decimalFormat.format(Double.parseDouble(mdp));
		} catch (Exception ex)
		{
			retVal = "";
		}
		return (retVal);
	}

	void setBypassOn(boolean bypassOn);

	void setDataQsize(int dataQsize);

	void setDispQsize(int dispQsize);

	void setDtm(String dTm);

	void setFileQsize(int fileQsize);

	void setFlowRateGPS(double flowRateGPS);

	void setHwcIn(double hwcIn);

	void setHwcLower(double hwcLower);

	void setHwcOut(double hwcOut);

	void setHwcUpper(double hwcUpper);

	void setImmersionDemand(boolean immersionDemand);

	void setImmersionOverride(boolean immersionOverride);

	void setImmersionSupply(boolean pImmersionSupply);

	void setPower(double power);

	void setPsun(double pSun);

	void setPumpOn(boolean pumpOn);

	void setTimeLapse(String timeLapse);

	void setTin(double tIn);

	void setTpanel(double tPanel);
	
	LcReading clone();
}