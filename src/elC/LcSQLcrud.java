package elC;
/**
 * @(#)LoggerController: LcSQLcrud.java
 * mySQL access class
 * Create and Read implemented so-far...
 * 
 * @author Bryan Hayward
 * @version 2.00 2018/02/20
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LcSQLcrud
{
	private static Connection connect = null;
	private static PreparedStatement preparedStatement = null;
	private static ResultSet resultSet = null;

	public LcSQLcrud()
	{
	}

	public static void loadDbDrv(LcParams lcp, Logger LOGGER) throws Exception
	{ // also worked ok: open, write result set, close for each reading...
		LOGGER.log(((lcp.dbg.contains("LcSQLcrud")) ? Level.INFO : Level.FINEST),
				"LcSQLcrud: maybe about to load MySQL driver: remainConn: " + Boolean.toString(lcp.remainConn)
						+ " and connected: " + Boolean.toString(connect != null));
		if (!lcp.remainConn || connect == null)
		{
			// This will load the MySQL driver, each DB has its own driver
			LOGGER.log(((lcp.dbg.contains("LcSQLcrud")) ? Level.INFO : Level.FINEST),
					"LcSQLcrud: about to load MySQL driver...");
			Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
			LOGGER.log(((lcp.dbg.contains("LcSQLcrud")) ? Level.INFO : Level.FINEST),
					"LcSQLcrud: about to connect to mysql://.../readings... with:\n" + "jdbc:mysql://" + lcp.host
							+ "/readings?" + "user=" + lcp.userName + "&password=" + lcp.pwd.toString()
							+ "&useSSL=false");
			try
			{
				connect = DriverManager.getConnection("jdbc:mysql://" + lcp.host + "/readings?" + "user=" + lcp.userName
						+ "&password=" + lcp.pwd.toString() + "&useSSL=false");
			} catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, "LcSQLcrud: did not load MySQL driver because of exception: " + e.toString());
			}
		} else
			LOGGER.log(((lcp.dbg.contains("LcSQLcrud")) ? Level.INFO : Level.FINEST),
					"LcSQLcrud: did not load MySQL driver because: remainConn == true: "
							+ Boolean.toString(lcp.remainConn) + " and connected: "
							+ Boolean.toString(connect != null));
	}

	public static Map<String, LcSolarReading> getReadings(String direction, long nFetch, String fromToDtm, LcParams lcp,
			Logger LOGGER) throws Exception
	{
		Map<String, LcSolarReading> lcRdMap = new HashMap<String, LcSolarReading>();
		try
		{
			loadDbDrv(lcp, LOGGER);
			LOGGER.log(((lcp.dbg.contains("LcSQLcrud")) ? Level.INFO : Level.FINEST),
					"LcSQLcrud: getLastNReadings: about to select max " + Long.toString(nFetch) + " readings "
							+ ((direction.equals("NEXT")) ? "from " : "to ") + fromToDtm + "...");
			try (Statement stmt = connect.createStatement();)
			{
				ResultSet rs = null;
				if (direction.equals("PREV"))
				{
					if (fromToDtm.isEmpty())
						fromToDtm = "99999999999999";
					LOGGER.log(((lcp.dbg.contains("LcSQLcrud")) ? Level.INFO : Level.FINEST),
							"LcSQLcrud: getLastNReadings: about to select max " + Long.toString(nFetch)
									+ " readings from " + fromToDtm + "...");
					rs = stmt.executeQuery("select * from reading where Dtm <= \"" + fromToDtm
							+ "\" order by Dtm DESC LIMIT " + Long.toString(nFetch + 1));
				} else
				{
					if (fromToDtm.isEmpty())
						fromToDtm = "00000000000000";
					if (direction.equals("POWER"))
					{ // Power records for day on date:
						LocalDate endDay = LocalDate.of(Integer.parseInt(fromToDtm.substring(0, 4)),
								Integer.parseInt(fromToDtm.substring(4, 6)),
								Integer.parseInt(fromToDtm.substring(6, 8)));
						LocalDate startDay = endDay.minusDays(nFetch);
						endDay = endDay.plusDays(1L);
						String dtmStart = startDay.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "999999";
						String dtmEnd = endDay.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "000000";
						LOGGER.log(((lcp.dbg.contains("LcSQLcrud")) ? Level.INFO : Level.FINEST),
								"LcSQLcrud: getLastNReadings: about to select max " + Long.toString(nFetch)
										+ " power <> 0 readings from " + dtmStart + " to " + dtmEnd + "...");
						rs = stmt.executeQuery("select * from reading where " + "Dtm > \"" + dtmStart
								+ "\" and Dtm < \"" + dtmEnd + "\" and Power > 0 ORDER by Power DESC LIMIT 1000"); //
					} else
					{ // NEXT
						LOGGER.log(((lcp.dbg.contains("LcSQLcrud")) ? Level.INFO : Level.FINEST),
								"LcSQLcrud: getLastNReadings: about to select max " + Long.toString(nFetch)
										+ " readings to " + fromToDtm + "...");
						rs = stmt.executeQuery("select * from reading where Dtm >= \"" + fromToDtm
								+ "\" order by Dtm ASC LIMIT " + Long.toString(nFetch + 1));
					}
				}
				int order = 0;
				while (rs.next())
				{
					LcSolarReading rd = new LcSolarReading();
					rd.setImmersionOverride(rs.getBoolean("ImmersionOverride"));
					rd.setPumpOn(rs.getBoolean("PumpOn"));
					rd.setImmersionDemand(rs.getBoolean("ImmersionDemand"));
					rd.setBypassOn(rs.getBoolean("BypassOn"));
					rd.setImmersionSupply(rs.getBoolean("ImmersionSupply"));
					rd.setTpanel(rs.getDouble("Tpanel"));
					rd.setHwcIn(rs.getDouble("HwcIn"));
					rd.setHwcLower(rs.getDouble("HwcLower"));
					rd.setHwcOut(rs.getDouble("HwcOut"));
					rd.setTin(rs.getDouble("Tin"));
					rd.setHwcUpper(rs.getDouble("HwcUpper"));
					rd.setFlowRateGPS(rs.getDouble("FlowRateGPS"));
					rd.setPsun(rs.getDouble("Psun"));
					rd.setPower(rs.getDouble("Power"));
					rd.setTimeLapse(Long.toString(rs.getLong("TimeLapse")));
					rd.setDtm(rs.getString("Dtm"));

					if (!direction.equals("POWER"))
						lcRdMap.put(rs.getString("Dtm"), rd);
					else
					{
						String orderStr = "00000000" + Integer.toString(order++);
						lcRdMap.put(orderStr.substring(orderStr.length() - 6), rd);
					}
					LOGGER.log(((lcp.dbg.contains("LcSQLcrud")) ? Level.INFO : Level.FINEST),
							"LcSQLcrud: getLastNReadings: added record " + Integer.toString(lcRdMap.size())
									+ " to lcRdMap.");
				}
			} catch (SQLException e)
			{
				LOGGER.log(((lcp.dbg.contains("LcSQLcrud")) ? Level.INFO : Level.FINEST),
						"LcSQLcrud: getReadings: SQLException: " + e.toString());
			}
			return lcRdMap;
		} catch (Exception e)
		{
			throw e;
		} finally
		{
			LOGGER.log(((lcp.dbg.contains("LcSQLcrud")) ? Level.INFO : Level.FINEST),
					"LcSQLcrud: getReadings: remainConn (if true does not) close connection?: "
							+ Boolean.toString(lcp.remainConn));
			if (!lcp.remainConn)
				close();
		}
	}

	protected static boolean writeReading(LcReading rd, Calendar curDte, LcParams lcp, Logger LOGGER) throws Exception
	{
		try
		{
			loadDbDrv(lcp, LOGGER);
			LOGGER.log(((lcp.dbg.contains("LcSQLcrud")) ? Level.INFO : Level.FINEST),
					"LcSQLcrud: about to prepare SQL statement...");
			preparedStatement = connect
					.prepareStatement("insert into reading values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			preparedStatement.setString(1, rd.getDtm());
			// TimeLapse becomes SQL BIGINT (9223372036854775807 = 290*10^6
			// years!)
			preparedStatement.setLong(2, Long.parseLong(rd.getTimeLapse()));
			preparedStatement.setDouble(3, rd.getHwcIn(3));
			preparedStatement.setDouble(4, rd.getHwcLower(3)); // HWClower
			preparedStatement.setDouble(5, rd.getHwcOut(3)); // HWCout
			preparedStatement.setDouble(6, rd.getTpanel(3)); // Tpanel
			preparedStatement.setDouble(7, rd.getTin(3)); // Tin
			preparedStatement.setDouble(8, rd.getHwcUpper(3)); // HWCupper
			preparedStatement.setDouble(9, rd.getFlowRateGPS(3)); // FlowRateGPS
			preparedStatement.setDouble(10, rd.getPsun(3)); // Psun
			preparedStatement.setBoolean(11, rd.getImmersionOverride()); // ImmersionOverride
			preparedStatement.setBoolean(12, rd.getImmersionDemand()); // ImmersionDemand
			preparedStatement.setBoolean(13, rd.getImmersionSupply()); // ImmersionSupply
			preparedStatement.setBoolean(14, rd.getPumpOn()); // Pump
			preparedStatement.setBoolean(15, rd.getBypassOn()); // Bypass
			preparedStatement.setDouble(16, rd.getPower(3)); // Current
			// instantaneous
			// power in
			LOGGER.log(((lcp.dbg.contains("LcSQLcrud")) ? Level.INFO : Level.FINEST),
					"LcSQLcrud: about to executeUpdate...");
			preparedStatement.executeUpdate();
			LOGGER.log(((lcp.dbg.contains("LcSQLcrud")) ? Level.INFO : Level.FINEST), "New record created.");
		} catch (Exception e)
		{
			LOGGER.log(((lcp.dbg.contains("LcSQLcrud")) ? Level.INFO : Level.FINEST),
					"LcSQLcrud: cannot create record about to close() and return NOK: " + e.getCause());
			close();
			return false;
		} finally
		{
			if (resultSet != null)
			{
				resultSet.close();
			}
			if (preparedStatement != null)
			{
				preparedStatement.close();
			}
			if (!lcp.remainConn)
			{
				LOGGER.log(((lcp.dbg.contains("LcSQLcrud")) ? Level.INFO : Level.FINEST),
						"LcSQLcrud: remainConn is False: closing connection.");
				close();
			}
		}
		LOGGER.log(((lcp.dbg.contains("LcSQLcrud")) ? Level.INFO : Level.FINEST),
				"LcSQLcrud: writeReading: about to return ok...");
		return true;
	}

	private static void close()
	{
		try
		{
			if (connect != null)
			{
				connect.close();
			}
		} catch (Exception e)
		{
		}
	}
}
/*
 * Howto on RPi: sudo /etc/init.d/mysql start
 * 
 * pi@raspberrypi:/var/lib $ mysql -p -u root readings Enter password: ...
 * 
 * sudo /etc/init.d/mysql stop
 * 
 * mysql> CREATE TABLE reading( -> Dtm CHAR(14) NOT NULL, -> TimeLapse BIGINT
 * NOT NULL, -> HwcIn DOUBLE NOT NULL, -> HwcLower DOUBLE NOT NULL, -> HwcOut
 * DOUBLE NOT NULL, -> Tpanel DOUBLE NOT NULL, -> Tin DOUBLE NOT NULL, ->
 * HwcUpper DOUBLE NOT NULL, -> FlowRateGPS DOUBLE NOT NULL, -> Psun DOUBLE NOT
 * NULL, -> ImmersionOverride BIT NOT NULL, -> ImmersionDemand BIT NOT NULL, ->
 * ImmersionSupply BIT NOT NULL, -> PumpOn BIT NOT NULL, -> BypassOn BIT NOT
 * NULL, -> Power DOUBLE NOT NULL); Query OK, 0 rows affected (0.02 sec)
 * 
 * mysql> describe reading;
 * +-------------------+------------+------+-----+---------+-------+ | Field |
 * Type | Null | Key | Default | Extra |
 * +-------------------+------------+------+-----+---------+-------+ | Dtm |
 * char(14) | NO | | NULL | | | TimeLapse | bigint(20) | NO | | NULL | | | HwcIn
 * | double | NO | | NULL | | | HwcLower | double | NO | | NULL | | | HwcOut |
 * double | NO | | NULL | | | Tpanel | double | NO | | NULL | | | Tin | double |
 * NO | | NULL | | | HwcUpper | double | NO | | NULL | | | FlowRateGPS | double
 * | NO | | NULL | | | Psun | double | NO | | NULL | | | ImmersionOverride |
 * bit(1) | NO | | NULL | | | ImmersionDemand | bit(1) | NO | | NULL | | |
 * ImmersionSupply | bit(1) | NO | | NULL | | | PumpOn | bit(1) | NO | | NULL |
 * | | BypassOn | bit(1) | NO | | NULL | | | Power | double | NO | | NULL | |
 * +-------------------+------------+------+-----+---------+-------+ 16 rows in
 * set (0.00 sec)
 * 
 * mysql> mysql> select * from reading; ... ... vvvv Values from unconfigured
 * floating adc inputs vvvv | 20171026145654 | 235 | 22400 | 22400 | 16000 |
 * 8400 | 16000 | 2400 | 762.72 | 840 | | | |  |  | 0 | | 20171026145801 | 301
 * | 2000 | 2000 | 2000 | 2000 | 18000 | 26880 | 1634.4 | 2240 | | | |  | | 0 |
 * | 20171026145907 | 368 | 0 | 22400 | 22400 | 16000 | 22400 | 7680 | 2033.92 |
 * 0 | | | |  |  | 0 | | 20171026150013 | 434 | 0 | 16000 | 16000 | 16000 |
 * 22400 | 19200 | 1452.8 | 0 | | | |  | | -92979200 |
 * +----------------+-----------+-------+----------+--------+--------+-------+--
 * --------+-------------+------+-------------------+-----------------+---------
 * --------+--------+----------+---------------------+ 407 rows in set (0.03
 * sec)
 * 
 * mysql>
 */
