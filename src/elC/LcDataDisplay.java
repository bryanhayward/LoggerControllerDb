package elC;

/**
 * @(#)LoggerController.dataDisplay.java
 * Defines swing GUI window.
 * A [Dump] button and associated date range field is 
 * defined to provide desktop access to a data dump facility.
 *
 * @author Bryan Hayward
 * @version 1.00 2018/02/20
 */
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalField;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import elC.LcHndlr.WritRes;

public class LcDataDisplay
{
	private static LcParams lcp;
	private static Logger LOGGER = null;
	private static String cc1 = "";
	private static String yy1 = "";
	private static String mm1 = "";
	private static String dd1 = "";
	private static String hr1 = "";
	private static String mn1 = "";
	private static String sc1 = "";
	private static String cc2 = "";
	private static String yy2 = "";
	private static String mm2 = "";
	private static String dd2 = "";
	private static String hr2 = "";
	private static String mn2 = "";
	private static String sc2 = "";
	private static long nFetch = 0L;
	static JFrame frame = null;
	static JLabel name1 = null;
	static JLabel name2 = null;
	static JLabel name3 = null;
	static JLabel name4 = null;
	static JLabel name5 = null;
	static JLabel name6 = null;
	static JLabel name7 = null;
	static JLabel name8 = null;
	static JLabel name9 = null;
	static JLabel name10 = null;
	static JLabel name11 = null;
	static JLabel name12 = null;
	static JLabel name13 = null;
	static JLabel name14 = null;
	static JLabel name15 = null;
	static JLabel name16 = null;
	static JLabel name17 = null;
	static JLabel name18 = null;
	static JLabel name19 = null;
	static JLabel name20 = null;
	static JLabel label1 = null;
	static JLabel label2 = null;
	static JLabel label3 = null;
	static JLabel label4 = null;
	static JLabel label5 = null;
	static JLabel label6 = null;
	static JLabel label7 = null;
	static JLabel label8 = null;
	static JLabel label9 = null;
	static JLabel label10 = null;
	static JLabel label11 = null;
	static JLabel label12 = null;
	static JLabel label13 = null;
	static JLabel label14 = null;
	static JLabel label15 = null;
	static JLabel label16 = null;
	static JLabel label17 = null;
	static JLabel label18 = null;
	static JLabel label19 = null;
	static JLabel label20 = null;

	public LcDataDisplay(LcParams passedLcp, Logger logger)
	{
		LOGGER = logger;
		lcp = passedLcp;
		createAndShowGUI();
	}

	private static void createAndShowGUI()
	{
		frame = new JFrame("LoggerController1.1");
		frame.setSize(400, 600);// width, height
		frame.setLayout(new GridLayout(22, 2));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		name1 = new JLabel("Date");
		name2 = new JLabel("Time");
		name3 = new JLabel("Runtime");
		name4 = new JLabel("DTM");
		name5 = new JLabel("HWCin");
		name6 = new JLabel("HWClower");
		name7 = new JLabel("HWCout");
		name8 = new JLabel("Tpanel");
		name9 = new JLabel("Tin");
		name10 = new JLabel("HWCupper");
		name11 = new JLabel("FlowRateGPS");
		name12 = new JLabel("Psun");
		name13 = new JLabel("ImmersionOverride");
		name14 = new JLabel("ImmersionDemand");
		name15 = new JLabel("ImmersionSupply");
		name16 = new JLabel("Pump");
		name17 = new JLabel("Bypass");
		name18 = new JLabel("Current Solar Power");
		name19 = new JLabel("Qlengths: Disp, File, Data   ");
		name20 = new JLabel("");
		label1 = new JLabel("");
		label2 = new JLabel("");
		label3 = new JLabel("");
		label4 = new JLabel("");
		label5 = new JLabel("");
		label6 = new JLabel("");
		label7 = new JLabel("");
		label8 = new JLabel("");
		label9 = new JLabel("");
		label10 = new JLabel("");
		label11 = new JLabel("");
		label12 = new JLabel("");
		label13 = new JLabel("");
		label14 = new JLabel("");
		label15 = new JLabel("");
		label16 = new JLabel("");
		label17 = new JLabel("");
		label18 = new JLabel("");
		label19 = new JLabel("");
		label20 = new JLabel("Bryan Hayward 201711");
		frame.getContentPane().add(name1);
		frame.getContentPane().add(label1);
		frame.getContentPane().add(name2);
		frame.getContentPane().add(label2);
		frame.getContentPane().add(name3);
		frame.getContentPane().add(label3);
		frame.getContentPane().add(name4);
		frame.getContentPane().add(label4);
		frame.getContentPane().add(name5);
		frame.getContentPane().add(label5);
		frame.getContentPane().add(name6);
		frame.getContentPane().add(label6);
		frame.getContentPane().add(name7);
		frame.getContentPane().add(label7);
		frame.getContentPane().add(name8);
		frame.getContentPane().add(label8);
		frame.getContentPane().add(name9);
		frame.getContentPane().add(label9);
		frame.getContentPane().add(name10);
		frame.getContentPane().add(label10);
		frame.getContentPane().add(name11);
		frame.getContentPane().add(label11);
		frame.getContentPane().add(name12);
		frame.getContentPane().add(label12);
		frame.getContentPane().add(name13);
		frame.getContentPane().add(label13);
		frame.getContentPane().add(name14);
		frame.getContentPane().add(label14);
		frame.getContentPane().add(name15);
		frame.getContentPane().add(label15);
		frame.getContentPane().add(name16);
		frame.getContentPane().add(label16);
		frame.getContentPane().add(name17);
		frame.getContentPane().add(label17);
		frame.getContentPane().add(name18);
		frame.getContentPane().add(label18);
		frame.getContentPane().add(name19);
		frame.getContentPane().add(label19);
		frame.getContentPane().add(name20);
		frame.getContentPane().add(label20);
		
		final JTextField tf=new JTextField();  
		tf.setBounds(50,50, 150,20);  
		JButton b=new JButton("Dump to CSV D1 < D2");  
		b.setBounds(50,100,95,30);  
		b.addActionListener(new ActionListener()
		{  
			public void actionPerformed(ActionEvent e)
			{
				if (validDates(tf.getText())) dumpRange(tf.getText());
				else tf.setText("20170430000000,20170512000000");  
			}  
		});  
		frame.add(b);
		frame.add(tf);
		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}
	
	private static void dumpRange(String dates)
	{
		String dtmFrom = cc1 + yy1 + mm1 + dd1 + hr1 + mn1 + sc1;
		String dtmTo = cc2 + yy2 + mm2 + dd2 + hr2 + mn2 + sc2;
		LocalDate fromDate = LocalDate.parse(cc1 + yy1 + "-" + mm1 + "-" + dd1);
		LocalDate toDate = LocalDate.parse(cc2 + yy2 + "-" + mm2 + "-" + dd2);
		LOGGER.log( ((lcp.dbg.contains("DumpRange"))?Level.INFO:Level.FINEST), "dtmFrom: " + dtmFrom + ", dtmTo: " + dtmTo);
		class WritRes
		{
			int nRecsWrit = 0;
			String firstDtm = "";
			String lastDtm = "";
		}
		
		LOGGER.log( ((lcp.dbg.contains("DumpRange"))?Level.INFO:Level.FINEST), "nFetch: " + Long.toString(nFetch) );
		WritRes wr = new WritRes();
		wr.nRecsWrit = 0;
		wr.lastDtm = dtmTo;
		wr.firstDtm = dtmFrom;
		OutputStreamWriter ostreamWtr = null;
		Map<String, LcSolarReading> lcRdMap = null;
		nextBlk:
		do
		{
			try
			{
				FileOutputStream ostream = new FileOutputStream("DTM" + dtmFrom + ".txt", true);
				ostreamWtr = new OutputStreamWriter(ostream);
				lcRdMap = LcSQLcrud.getReadings("NEXT", nFetch, dtmFrom, lcp, LOGGER);
			} catch (Exception e1)
			{
				LOGGER.log(((lcp.dbg.contains("DumpRange"))?Level.INFO:Level.SEVERE), "Could not get data to write to output file, caught: " + e1.toString());
				e1.printStackTrace();
			}
			// use TreeMap to sort by key = dtm (natural order)
			SortedSet<String> lcRdKeys = new TreeSet<String>(lcRdMap.keySet());
			for (String lcRdKey : lcRdKeys)
			{
				LcReading rd = lcRdMap.get(lcRdKey);
				LOGGER.log( ((lcp.dbg.contains("DumpRangeLines"))?Level.INFO:Level.FINEST), "WriteMap: next dtm: " + rd.getDtm() + ", firstDtm: " + wr.firstDtm
							+ ", nRecsWrit: " + Integer.toString(wr.nRecsWrit) + ", lcRdKeys.size: "
							+ Integer.toString(lcRdKeys.size()) );
				// write data to local csv file
				try
				{
					if ( rd.getDtm().equals(dtmTo) ) break;
					wr.lastDtm = rd.getDtm();
					String dataLine = rd.toCSV();
					ostreamWtr.write(dataLine, 0, dataLine.length());
					ostreamWtr.write('\r');
					ostreamWtr.write('\n');
					if (wr.nRecsWrit++ % 1000 == 0) ostreamWtr.flush();
				}
				catch (Exception e)
				{
					LOGGER.log(((lcp.dbg.contains("DumpRange"))?Level.INFO:Level.SEVERE), "Could not write to output file, caught: " + e.toString());
					e.printStackTrace();
				}
			}
			try
			{
				ostreamWtr.flush();
				ostreamWtr.close();
				dtmFrom = wr.lastDtm;
				LOGGER.log( ((lcp.dbg.contains("DumpRange"))?Level.INFO:Level.FINEST), "Loop end: dtmFrom now set to lastDtm: " + dtmFrom +
						", about to test if after to date...");
			}
			catch (Exception e)
			{
				LOGGER.log(((lcp.dbg.contains("LcDataDisplay"))?Level.INFO:Level.SEVERE), "Could not write to output file, caught: " + e.toString());
				e.printStackTrace();
			}
			fromDate = dtm2Date(dtmFrom); //LocalDate.parse(cc1 + yy1 + "-" + mm1 + "-" + dd1);
		} while(!fromDate.isAfter(toDate) && !fromDate.isEqual(toDate));
		LOGGER.log( ((lcp.dbg.contains("DumpRange"))?Level.INFO:Level.FINEST), "Done dump.");
	}
	
	private static LocalDate dtm2Date(String dtm)
	{
		String cc = dtm.substring(0, 2);
		String yy = dtm.substring(2, 4);
		String mm = dtm.substring(4, 6);
		String dd = dtm.substring(6, 8);
		LocalDate dte = LocalDate.parse(cc + yy + "-" + mm + "-" + dd);
		return dte;
	}
	private static boolean validDates(String dates)
	{ // validates dates also populates static fields - expects "CCYYMMDDhhmmss,CCYYMMDDhhmmss"
		try
		{
			cc1 = dates.substring(0, 2);
			yy1 = dates.substring(2, 4);
			mm1 = dates.substring(4, 6);
			dd1 = dates.substring(6, 8);
			hr1 = dates.substring(8, 10);
			mn1 = dates.substring(10, 12);
			sc1 = dates.substring(12, 14);
			cc2 = dates.substring(15, 17);
			yy2 = dates.substring(17, 19);
			mm2 = dates.substring(19, 21);
			dd2 = dates.substring(21, 23);
			hr2 = dates.substring(23, 25);
			mn2 = dates.substring(25, 27);
			sc2 = dates.substring(27, 29);
			LocalDate fromDate = LocalDate.parse(cc1 + yy1 + "-" + mm1 + "-" + dd1);
			LocalDate toDate = LocalDate.parse(cc2 + yy2 + "-" + mm2 + "-" + dd2);
			// date 2 must be >= date 1
			if (fromDate.isAfter(toDate)) return false;
			// estimate number of records in range
			long l1 = toDate.getLong(java.time.temporal.ChronoField.EPOCH_DAY);
			long l2 = fromDate.getLong(java.time.temporal.ChronoField.EPOCH_DAY);
			nFetch = (l1 - l2) * 24 * 3600 / 30;
			if (nFetch > 100000) nFetch = 100000;
		} catch (Exception e) { return false; }
		return true;
	}
	public boolean displayData(LcReading rd)
	{
		label1.setText(rd.getDate());
		label2.setText(rd.getTime());
		label3.setText(rd.getTimeLapse());
		label4.setText(rd.getDtm());
		label5.setText(rd.getShwcIn(3));
		label6.setText(rd.getShwcLower(3));
		label7.setText(rd.getShwcOut(3));
		label8.setText(rd.getStPanel(3));
		label9.setText(rd.getStIn(3));
		label10.setText(rd.getShwcUpper(3));
		label11.setText(rd.getSflowRateGPS(3));
		label12.setText(rd.getSpSun(3));
		label13.setText(rd.getSimmersionOverride());
		label14.setText(rd.getSimmersionDemand());
		label15.setText(rd.getSimmersionSupply());
		label16.setText(rd.getSpumpOn());
		label17.setText(rd.getSbypassOn());
		label18.setText(rd.getSpower(3));
		label19.setText(rd.getSdispQsize() + ", " + rd.getSfileQsize() + ", " + rd.getSdataQsize());
		return true;
	}
}
