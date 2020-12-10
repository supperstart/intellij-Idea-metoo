package com.metoo.core.tools;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.TimeZone;

/**
 * TODO: a short class description here
 * User: Johnson
 * Date: Nov 29, 2005
 * Time: 1:35:00 PM
 */
public class DateUtil
{
    /**
     * A log instance for this class
     */
    private static Log log = LogFactory.getLog(DateUtil.class);

    // Dates constants

    /**
     * "JAN" to represent January
     */
    public static final String MTH_JAN = "Jan";

    /**
     * "FEB" to represent February
     */
    public static final String MTH_FEB = "Feb";

    /**
     * "MAR" to represent March
     */
    public static final String MTH_MAR = "Mar";

    /**
     * "APR" to represent April
     */
    public static final String MTH_APR = "Apr";

    /**
     * "MAY" to represent May
     */
    public static final String MTH_MAY = "May";

    /**
     * "JUNE" to represent June
     */
    public static final String MTH_JUN = "Jun";

    /**
     * "JUL" to represent July
     */
    public static final String MTH_JUL = "Jul";

    /**
     * "AUG" to represent August
     */
    public static final String MTH_AUG = "Aug";

    /**
     * "SEP" to represent September
     */
    public static final String MTH_SEP = "Sep";

    /**
     * "OCT" to represent October
     */
    public static final String MTH_OCT = "Oct";

    /**
     * "NOV" to represent November
     */
    public static final String MTH_NOV = "Nov";

    /**
     * "DEC" to represent December
     */
    public static final String MTH_DEC = "Dec";

    /**
     * The date pattern in this format 'ddMMyyyy  HHmm'
     */
    public static String DATETIME_FORMAT = "ddMMyyyy  HHmm";

    public static String DATE_FORMAT = "dd MMM yyyy";
    public static String TIMESTAMP_FORMAT = "dd MMM yyyy HH:mm:ss";

    public static String DATE_FORMAT_YYYYMMDD = "yyyy-MM-dd";
    public static final String DATE_FORMAT_YYYYMMDDHHMMSS = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_NEW = "yyyy/MM/dd HH:mm:ss";
    public static final String DATEFORMAT_FOR_PAY = "yyyyMMddHHmmss";

    /**
     * Returns a date object from input string indicating year, month and day
     *
     * @param year  Year Indicator
     * @param month Month indicator, 1=jan 2=feb ...
     * @param day   Date indicator eg: any day from 1...31.
     * @return date java.util.Date object in millisecond.
     * @since 15/05/2000
     */
    public static Date getDate(int year, int month, int day)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, 0, 0, 0);

        return cal.getTime();
    }

    /**
     * Compares the 2 dates: Returns true if the 2 dates are equal.
     *
     * @param date1 Date to compare
     * @param date2 Date to compare
     * @return true if <code>date1</code> equals to <code>date2</code>.
     * @since 24/04/2001
     */
    public static boolean isDateEqual(Date date1, Date date2)
    {
        if((date1 == null) || (date2 == null))
        {
            return false;
        }

        return resetTime(date1).compareTo(resetTime(date2)) == 0;
    }

    /**
     * Sets the default timezone to the specified timezone ID.  Note: The
     * system time will remain unchanged. Only the Time zone for the current
     * thread is set.
     *
     * @param timeZoneID The timezone ID. Example: "America/Los_Angeles", "CCT"
     *                   which stands for China/Taiwan = S'pore
     */
    public static void setDefaultTimeZone(String timeZoneID)
    {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZoneID));
    }

    /**
     * Calculates the elapsed time between 2 dates. The elapsed time calculated
     * could either be in years, months or days
     *
     * @param type      (int) The variable type determines the calculation of the
     *                  elapsed time to be based on either years, months or days. To
     *                  compute the elapsed time in year input type set to Calendar.YEAR
     *                  To compute the elapsed time in month input type set to
     *                  Calendar.MONTH  By default the elapsed time will compute in days
     * @param startDate start date
     * @param endDate   end date
     * @return the elapsed time (int)
     */
    public static int getElapsedTime(int type, Date startDate,
                                     Date endDate)
    {
        int elapsed = 0;

        if((startDate == null) || (endDate == null))
        {
            return -1;
        }

        if(startDate.after(endDate))
        {
            return -1;
        }

        GregorianCalendar gc1 = (GregorianCalendar) GregorianCalendar.getInstance();
        GregorianCalendar gc2 = (GregorianCalendar) gc1.clone();
        gc1.setTime(startDate);
        gc2.setTime(endDate);

        gc1.clear(Calendar.MILLISECOND);
        gc1.clear(Calendar.SECOND);
        gc1.clear(Calendar.MINUTE);
        gc1.clear(Calendar.HOUR_OF_DAY);
        gc2.clear(Calendar.MILLISECOND);
        gc2.clear(Calendar.SECOND);
        gc2.clear(Calendar.MINUTE);
        gc2.clear(Calendar.HOUR_OF_DAY);

        if((type != Calendar.MONTH) && (type != Calendar.YEAR))
        {
            type = Calendar.DATE;
        }

        if(type == Calendar.MONTH)
        {
            gc1.clear(Calendar.DATE);
            gc2.clear(Calendar.DATE);
        }

        if(type == Calendar.YEAR)
        {
            gc1.clear(Calendar.DATE);
            gc2.clear(Calendar.DATE);
            gc1.clear(Calendar.MONTH);
            gc2.clear(Calendar.MONTH);
        }

        while(gc1.before(gc2))
        {
            gc1.add(type, 1);
            elapsed++;
        }

        return elapsed;
    }

    /**
     * This method will determine if the date is the last day of the month
     *
     * @param date date
     * @return returns true if the date falls on the last day of the month else
     *         returns false
     */
    public static boolean isEndOfTheMonth(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        return cal.get(Calendar.DATE) == maxDay;

    }

    /**
     * This method will determine if the date is the last day of the year
     *
     * @param date date
     * @return returns true if the date falls on the last day of the year else
     *         returns false
     */
    public static boolean isEndOfTheYear(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return (cal.get(Calendar.MONTH) == 11) && (cal.get(Calendar.DATE) == 31);

    }

    /**
     * This method will return the last day of the months
     *
     * @param date date
     * @return returns the last day of the month
     */
    public static int getLastDayOfTheMonth(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * Returns the numeric value of the specified month
     *
     * @param month The 3 letter month representation. Example: "Jan", "Feb", etc
     * @return the numeric value of the specified month
     */
    public static int getMthInInt(String month)
    {
        if(month.equalsIgnoreCase(MTH_JAN))
        {
            return 1;
        } else if(month.equalsIgnoreCase(MTH_FEB))
        {
            return 2;
        } else if(month.equalsIgnoreCase(MTH_MAR))
        {
            return 3;
        } else if(month.equalsIgnoreCase(MTH_APR))
        {
            return 4;
        } else if(month.equalsIgnoreCase(MTH_MAY))
        {
            return 5;
        } else if(month.equalsIgnoreCase(MTH_JUN))
        {
            return 6;
        } else if(month.equalsIgnoreCase(MTH_JUL))
        {
            return 7;
        } else if(month.equalsIgnoreCase(MTH_AUG))
        {
            return 8;
        } else if(month.equalsIgnoreCase(MTH_SEP))
        {
            return 9;
        } else if(month.equalsIgnoreCase(MTH_OCT))
        {
            return 10;
        } else if(month.equalsIgnoreCase(MTH_NOV))
        {
            return 11;
        } else if(month.equalsIgnoreCase(MTH_DEC))
        {
            return 12;
        } else
        {
            return 0;
        }
    }

    /**
     * Return the date of the next working day
     *
     * @return the date of the next working day
     */
    public static Date getNextWorkingDay()
    {
        Date nextWorkingDay = DateUtil.addDaysToDate(DateUtil.getSystemDate(), 1);
        Calendar c = Calendar.getInstance();
        c.setTime(nextWorkingDay);

        int day = c.get(Calendar.DAY_OF_WEEK);

        if(day == Calendar.SUNDAY)
        {
            nextWorkingDay = DateUtil.addDaysToDate(nextWorkingDay, 1);
        }

        return nextWorkingDay;
    }

    /**
     * Compares the 2 dates: Returns true if the start date is before the end
     * date.
     *
     * @param startDate Starting date of a particular time period.
     * @param endDate   Ending date of a particular time period.
     * @return true if the <code>startDate</code> is before
     *         <code>endDate</code>.
     * @since 24/03/2001
     */
    public static boolean isStartBeforeEndDate(Date startDate, Date endDate)
    {
        if((startDate == null) || (endDate == null))
        {
            return false;
        }

        return resetTime(startDate).compareTo(resetTime(endDate)) < 0;
    }

    /**
     * This method will determine if the date occurs on the beginning of the
     * month
     *
     * @param date date
     * @return returns true if date is on the beginning of the month else
     *         returns false
     */
    public static boolean isStartOfTheMonth(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.DATE) == 1;

    }

    /**
     * This method will return
     * month
     *
     * @param date date
     * @return returns month in int
     */
    public static int getMonth(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.MONTH);

    }

    public static int getYear(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.YEAR);

    }

    /**
     * This method will determine if the date occurs at the beginning of the
     * year
     *
     * @param date date
     * @return returns true if the date occurs on the beginning of the year
     *         else returns false
     */
    public static boolean isStartOfTheYear(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return (cal.get(Calendar.MONTH) == 1) && (cal.get(Calendar.DATE) == 1);

    }

    /**
     * Returns the corresponding 3 letter string value of the month specified by numeric month value
     *
     * @param month The numeric value of the specified month
     * @return the corresponding 3 letter string value of the month specified by numeric month value
     */
    public static String getStrMth(int month)
    {
        if(month == 1)
        {
            return MTH_JAN;
        } else if(month == 2)
        {
            return MTH_FEB;
        } else if(month == 3)
        {
            return MTH_MAR;
        } else if(month == 4)
        {
            return MTH_APR;
        } else if(month == 5)
        {
            return MTH_MAY;
        } else if(month == 6)
        {
            return MTH_JUN;
        } else if(month == 7)
        {
            return MTH_JUL;
        } else if(month == 8)
        {
            return MTH_AUG;
        } else if(month == 9)
        {
            return MTH_SEP;
        } else if(month == 10)
        {
            return MTH_OCT;
        } else if(month == 11)
        {
            return MTH_NOV;
        } else if(month == 12)
        {
            return MTH_DEC;
        } else
        {
            return "";
        }
    }

    /**
     * Calculates the duration in years, months and days.
     *
     * @param startDate Start Date of a period.
     * @param endDate   End date of a period.
     * @return int [] result    [0]=duration in years, [1]=duration in months,
     *         [2]=duration in days.
     */
    public static int[] computeDuration(Date startDate, Date endDate)
    {
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.setTime(startDate);
        to.setTime(endDate);

        int birthYYYY = from.get(Calendar.YEAR);
        int birthMM = from.get(Calendar.MONTH);
        int birthDD = from.get(Calendar.DAY_OF_MONTH);
        int asofYYYY = to.get(Calendar.YEAR);
        int asofMM = to.get(Calendar.MONTH);
        int asofDD = to.get(Calendar.DAY_OF_MONTH);
        int ageInYears = asofYYYY - birthYYYY;
        int ageInMonths = asofMM - birthMM;
        int ageInDays = asofDD - birthDD + 1;

        if(ageInDays < 0)
        {
            /*
             * Guaranteed after this single treatment, ageInDays will be >= 0.
             * that is ageInDays = asofDD - birthDD + daysInBirthMM.
             */
            ageInDays += from.getActualMaximum(Calendar.DAY_OF_MONTH);
            ageInMonths--;
        }

        if(ageInDays == to.getActualMaximum(Calendar.DAY_OF_MONTH))
        {
            ageInDays = 0;
            ageInMonths++;
        }

        if(ageInMonths < 0)
        {
            ageInMonths += 12;
            ageInYears--;
        }

        if((birthYYYY < 0) && (asofYYYY > 0))
        {
            ageInYears--;
        }

        if(ageInYears < 0)
        {
            ageInYears = 0;
            ageInMonths = 0;
            ageInDays = 0;
        }

        int[] result = new int[3];
        result[0] = ageInYears;
        result[1] = ageInMonths;
        result[2] = ageInDays;

        return result;
    }

    /**
     * Returns the current SQL date.
     *
     * @return Date
     */
    public static java.sql.Date getSystemDate()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return new java.sql.Date(cal.getTime().getTime());
    }

    /**
     * Returns the current timestamp.
     *
     * @return Timestamp
     */
    public static Timestamp getSystemTimestamp()
    {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * Returns true if the length of the year is of 4 digits
     *
     * @param s String value Year
     * @return True   if the year length is of 4 digits, False  otherwise
     * @since 20/04/2001
     */
    public static boolean isValidYearFormat(String s)
    {
        if(s == null)
        {
            return false;
        } else if(s.trim().length() == 4)
        {
            return true;
        }

        return false;
    }

    /**
     * This method convert the date to string
     *
     * @param date      date
     * @param strFormat the string format
     * @return date as string format
     */
    public static String getDate(Date date, String strFormat)
    {
        return DateUtil.parseDate(date, strFormat);
    }

    /**
     * Returns true if the String is a valid date.
     *
     * @param strDate The date in format ddmmyyyy.
     * @return True, if it is a valid date. False, otherwise.
     */
    public static boolean isValidDate(String strDate)
    {
        return DateUtil.toDate(strDate, "ddMMyyyy") != null;
    }

    /**
     * Returns true if the String is a valid date by specifying the date format to be verified.
     *
     * @param strDate       The date.
     * @param dateStrFormat The date format of the specified strDate
     * @return True, if it is a valid date. False, otherwise.
     */
    public static boolean isValidDate(String strDate, String dateStrFormat)
    {
        return DateUtil.toDate(strDate, dateStrFormat) != null;
    }

    /**
     * Add year, month or day to a date
     * To subtract the specified number of Days to the specified Date object, juz use a negative number
     * Example: DateUtil.addDaysToDate(date, -5) ==  subtracting 5 days from the specified date.
     * The same applies to month and year.
     *
     * @param type (int).  Indicates the input num value is in year, month, or
     *             days. Valid values are Calendar.YEAR, Calendar.MONTH,
     *             Calendar.DATE
     * @param date (java.sql.Date).
     * @param num  (int).  The value to be added to the input date.
     * @return java.sql.Date.
     */
    public static Date addDate(int type, Date date, int num)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.add(type, num);

        return new Date(cal.getTime().getTime());
    }

    /**
     * Adds the specified number of Days to the specified Date object
     * To subtract the specified number of Days to the specified Date object, juz use a negative number
     * Example: DateUtil.addDaysToDate(date, -5) ==  subtracting 5 days from the specified date.
     *
     * @param date    Date to be add
     * @param numDays Number of days to add
     * @return date  Added Date
     */
    public static Date addDaysToDate(Date date, int numDays)
    {
        if(date == null)
        {
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, numDays);

        return c.getTime();
    }

    /**
     * Adds the specified number of Hours to the specified Date object
     * To subtract the specified number of hours to the specified Date object, juz use a negative number
     * Example: DateUtil.addDaysToDate(date, -5) ==  subtracting 5 hours from the specified date.
     *
     * @param date     Date to be add
     * @param numHours A valued byte that could possibly be of negative value.
     * @return date  Added Date
     * @since 27/10/2001
     */
    public static Date addHoursToDate(Date date, int numHours)
    {
        if(date == null)
        {
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.HOUR_OF_DAY, numHours);

        return c.getTime();
    }

    /**
     * Adds the specified number of Minutes to the specified Date object
     * To subtract the specified number of Minutes to the specified Date object, juz use a negative number
     * Example: DateUtil.addDaysToDate(date, -5) ==  subtracting 5 minutes from the specified date.
     *
     * @param date    Date to be add
     * @param numMins Number of minutes to add
     * @return date  Added Date
     * @since 27/10/2001
     */
    public static Date addMinutesToDate(Date date, int numMins)
    {
        if(date == null)
        {
            return null;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MINUTE, numMins);

        return c.getTime();
    }

    /**
     * Adds the specified number of Months to the specified Date object
     *
     * @param date      Date to be add
     * @param numMonths Number of months to add
     * @return date  Added Date
     */
    public static Date addMonthsToDate(Date date, int numMonths)
    {
        if(date == null)
        {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, numMonths);
        return c.getTime();
    }

    /**
     * Adds the specified number of Years to the specified Date object
     *
     * @param date     Date to be add
     * @param numYears Number of years to add
     * @return date  Added Date
     */
    public static Date addYearsToDate(Date date, int numYears)
    {
        if(date == null)
        {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.YEAR, numYears);
        return c.getTime();
    }

    /**
     * The method will compares 2 dates (excluding the HH MM SS)
     *
     * @param date1 1st date parameter
     * @param date2 2nd date parameter
     * @return returns -1 if 1st date parameter is earlier than 2nd date
     *         parameter retuns 0 if both dates parameter is the same day
     *         retuns 1 if 1st date parameter is later than 2nd date parameter
     */
    public static int compareDates(Date date1, Date date2)
    {
        if((date1 == null) && (date2 == null))
        {
            return 0;
        }

        if(date1 == null)
        {
            return -1;
        }

        if(date2 == null)
        {
            return 1;
        }

        String strFormat = "yyyyMMdd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(strFormat);

        int intDate1 = Integer.parseInt(dateFormat.format(date1));
        int intDate2 = Integer.parseInt(dateFormat.format(date2));

        if(intDate1 == intDate2)
        {
            return 0;
        }

        if(intDate1 > intDate2)
        {
            return 1;
        }

        return -1;
    }

    /**
     * Parses Date object to formatted string
     *
     * @param date      date to be converted
     * @param formatStr Date/Time pattern. Example: ddMMyyyy or HHmmss or any
     *                  other patterns
     * @return String in required format  Format  : dd = Day MM = Month yyyy =
     *         Year HH = Hour mm = Minute ss = Second All format same as
     *         SimpleDateFormat. Null is returned if the date object is null.
     * @since 22/03/2001
     */
    public static String parseDate(Date date, String formatStr)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(formatStr);

        if(date == null)
        {
            return null;
        } else
        {
            return dateFormat.format(date);
        }
    }

    /**
     * Parses Date object to date-time formatted string
     *
     * @param date THe date to be converted
     * @return String in required format. Null is returned if the date object
     *         is null. (All format same as SimpleDateFormat)
     * @since 25/10/2001
     */
    public static String parseDate(Date date)
    {
        return parseDate(date, DATETIME_FORMAT);
    }

    /**
     * Resets time fields of date to 00:00
     *
     * @param date Date to be reset the time to zero
     * @return date  Converted Date
     */
    public static Date resetTime(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    /**
     * Converts the specified date-time string to Date object based on the
     * specified date-time format. <CODE>null</CODE> is returned if the
     * specified date is invalid.
     *
     * @param strDateTime    The date string in this format 'ddMMyyyy  HHmm'.
     * @param dateTimeFormat The date pattern in this format 'ddMMyyyy  HHmm'
     * @return the Date representation.  Returns null if the date object or the
     *         strDateTime or the dateTimeFormat is null.
     */
    public static Date toDate(String strDateTime, String dateTimeFormat)
    {
        if((strDateTime == null) || (strDateTime.length() == 0) ||
           (dateTimeFormat == null) || (dateTimeFormat.length() == 0))
        {
            return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(dateTimeFormat);
        Date date = dateFormat.parse(strDateTime, new ParsePosition(0));

        if(date == null)
        {
            return null;
        }

        String dateStr = parseDate(date, dateTimeFormat);

        if(!strDateTime.equals(dateStr))
        {
            return null;
        }

        return date;
    }

    /**
     * Converts the specified date-time string to Date object based on the
     * specified date-time format. <CODE>null</CODE> is returned if the
     * specified date is invalid.
     *
     * @param strDateTime The date string in this format 'ddMMyyyy  HHmm'.
     * @return the Date representation.  Returns null if the date object or the
     *         strDateTime or the dateTimeFormat is null.
     */
    public static Date toDate(String strDateTime)
    {
        return toDate(strDateTime, DATETIME_FORMAT);
    }

    /**
     * Gets an integer string representation of the specified month.
     *
     * @param mthMMM Month of three letter string.  For example, "JAN",
     *               "FEB",..
     * @return a string number equivalent of the specified month string.  If
     *         the specified month is unknown, zero string is returned that is
     *         "00".
     * @since 27/03/2000
     */
    public static String toMMFormat(String mthMMM)
    {
        if(mthMMM.equalsIgnoreCase(MTH_JAN))
        {
            return "01";
        } else if(mthMMM.equalsIgnoreCase(MTH_FEB))
        {
            return "02";
        } else if(mthMMM.equalsIgnoreCase(MTH_MAR))
        {
            return "03";
        } else if(mthMMM.equalsIgnoreCase(MTH_APR))
        {
            return "04";
        } else if(mthMMM.equalsIgnoreCase(MTH_MAY))
        {
            return "05";
        } else if(mthMMM.equalsIgnoreCase(MTH_JUN))
        {
            return "06";
        } else if(mthMMM.equalsIgnoreCase(MTH_JUL))
        {
            return "07";
        } else if(mthMMM.equalsIgnoreCase(MTH_AUG))
        {
            return "08";
        } else if(mthMMM.equalsIgnoreCase(MTH_SEP))
        {
            return "09";
        } else if(mthMMM.equalsIgnoreCase(MTH_OCT))
        {
            return "10";
        } else if(mthMMM.equalsIgnoreCase(MTH_NOV))
        {
            return "11";
        } else if(mthMMM.equalsIgnoreCase(MTH_DEC))
        {
            return "12";
        }

        return null;
    }

    /**
     * Gets a specified month string as JAN, FEB..
     *
     * @param mthMM The month as 2 digits For example, "01", "02",..
     * @return a specified month string.  If the specified month is unknown,
     *         empty string ("") is returned.
     * @since 27/03/2000
     */
    public static String toMMMFormat(String mthMM)
    {
        if("01".equals(mthMM))
        {
            return MTH_JAN;
        } else if("02".equals(mthMM))
        {
            return MTH_FEB;
        } else if("03".equals(mthMM))
        {
            return MTH_MAR;
        } else if("04".equals(mthMM))
        {
            return MTH_APR;
        } else if("05".equals(mthMM))
        {
            return MTH_MAY;
        } else if("06".equals(mthMM))
        {
            return MTH_JUN;
        } else if("07".equals(mthMM))
        {
            return MTH_JUL;
        } else if("08".equals(mthMM))
        {
            return MTH_AUG;
        } else if("09".equals(mthMM))
        {
            return MTH_SEP;
        } else if("10".equals(mthMM))
        {
            return MTH_OCT;
        } else if("11".equals(mthMM))
        {
            return MTH_NOV;
        } else if("12".equals(mthMM))
        {
            return MTH_DEC;
        }

        return null;
    }

    /**
     * Converts the specified date-time string to SQL Date object based on the
     * specified date-time format. <CODE>null</CODE> is returned if the
     * specified date is invalid.
     *
     * @param strDateTime    The date string in this format 'ddMMyyyy  HHmm'.
     * @param dateTimeFormat The date pattern in this format 'ddMMyyyy  HHmm'
     * @return the SQL Date representation.  Returns null if the date object or
     *         the strDateTime or the dateTimeFormat is null.
     */
    public static java.sql.Date toSQLDate(String strDateTime,
                                          String dateTimeFormat)
    {
        Date date = toDate(strDateTime, dateTimeFormat);

        if(date == null)
        {
            return null;
        }

        return new java.sql.Date(date.getTime());
    }

    /**
     * Converts the Date object to SQL Date object.
     *
     * @param date The date to be converted.
     * @return the SQL Date representation.
     */
    public static java.sql.Date toSQLDate(Date date)
    {
        if(date == null)
        {
            return null;
        }

        return new java.sql.Date(date.getTime());
    }

    /**
     * Converts the specified date-time string to SQL Date object based on the
     * specified date-time format. <CODE>null</CODE> is returned if the
     * specified date is invalid.
     *
     * @param strDateTime The date string in this format 'ddMMyyyy  HHmm'.
     * @return the SQL Date representation.  Returns null if the date object or
     *         the strDateTime or the dateTimeFormat is null.
     */
    public static java.sql.Date toSQLDate(String strDateTime)
    {
        return toSQLDate(strDateTime, DATETIME_FORMAT);
    }

    /**
     * Converts the specified date-time string to Timestamp.
     *
     * @param dateTimeStr The String object in this ddMMyyyy  HHmm format
     * @return Timestamp object. Returns null if dateTimeStr is null Format
     *         used is meant for Oracle dbs only
     */
    public static Timestamp toTimestamp(String dateTimeStr)
    {
        return toTimestamp(toDate(dateTimeStr));
    }

    /**
     * Converts the specified date-time string to Timestamp.
     *
     * @param dateTimeStr    The String object in this ddMMyyyy  HHmm format
     * @param dateTimeFormat The date pattern in this format 'ddMMyyyy  HHmm'
     * @return Timestamp object. Returns null if dateTimeStr is null Format
     *         used is meant for Oracle dbs only
     */
    public static Timestamp toTimestamp(String dateTimeStr,
                                        String dateTimeFormat)
    {
        return toTimestamp(toDate(dateTimeStr, dateTimeFormat));
    }

    /**
     * Converts the specified Calendar to Timestamp.
     *
     * @param date The Date object.
     * @return Timestamp object.  Returns null if date object is null Format
     *         used is meant for Oracle dbs only
     */
    public static Timestamp toTimestamp(Date date)
    {
        if(date == null)
        {
            return null;
        }

        return new Timestamp(date.getTime());
    }

    /**
     * Converts the specified Calendar to Timestamp.
     *
     * @param timeStamp The TimeStamp object.
     * @return Date object.  Returns null if timestamp object is null Format
     *         used is meant for Oracle dbs only
     */
    public static Date toDate(Timestamp timeStamp)
    {
        if(timeStamp == null)
        {
            return null;
        }

        return new Date(timeStamp.getTime());
    }

    /**
     * to determine is the date is infinite. ie is the year is 99999.
     * 9999 is used to detnote the expiry date does not expire for ever
     *
     * @param dateToCheck Date the date to check is it infinite
     * @return result      Boolean if the date is infinity it will return true. or it will return false
     */
    public static Boolean isInfinity(Date dateToCheck)
    {
        if(dateToCheck == null)
        {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(dateToCheck);
        int year = cal.get(Calendar.YEAR);
        log.info("The date is infinity. The given year :" + year);
        if(year >= 9999)
        {
            log.info("The date is infinity");
            return Boolean.TRUE;
        }

        log.info("The date is not infinity");
        return Boolean.FALSE;

    }

    /**
     * this method is designed to calculate the first/last day of current week
     *
     * @return a Map object that contains:
     *         <pre>
     *                 key             value<br>
     *                 startOfWeek     the first day of current week<br>
     *                 endOfWeek       the last day of current week<br>
     *                 </pre>
     */
    public static Map calcCurrentWeek()
    {
        Calendar curCal = Calendar.getInstance();
        int i = curCal.get(Calendar.DAY_OF_WEEK);
        Date startOfWeek = addDaysToDate(curCal.getTime(), -i + 2);
        Date endOfWeek = addDaysToDate(curCal.getTime(), 7 - i + 1);

        Map resMap = new HashMap();
        resMap.put("startOfWeek", startOfWeek);
        resMap.put("endOfWeek", endOfWeek);
        return resMap;
    }

    //~ Static fields/initializers =============================================

    private static String timePattern = SysPropertiesUtil.getString("system.date.pattern.time", "HH:mm");

    //~ Methods ================================================================

    /**
     * Return default datePattern (MM/dd/yyyy)
     *
     * @return a string representing the date pattern on the UI
     */
    public static synchronized String getDatePattern()
    {
//        Locale locale = LocaleContextHolder.getLocale();
        String defaultDatePattern;
        try
        {
            defaultDatePattern = SysPropertiesUtil.getString("system.date.pattern.common", "MM/dd/yyyy");
        } catch(MissingResourceException mse)
        {
            defaultDatePattern = "MM/dd/yyyy";
        }

        return defaultDatePattern == null ? "MM/dd/yyyy" : defaultDatePattern;
    }

    /**
     * This method attempts to convert an Oracle-formatted date
     * in the form dd-MMM-yyyy to mm/dd/yyyy.
     *
     * @param aDate date from database as a string
     * @return formatted string for the ui
     */
    public static String getDate(Date aDate)
    {
        SimpleDateFormat df;
        String returnValue = "";

        if(aDate != null)
        {
            df = new SimpleDateFormat(getDatePattern());
            returnValue = df.format(aDate);
        }

        return (returnValue);
    }

    /**
     * This method generates a string representation of a date/time
     * in the format you specify on input
     *
     * @param aMask   the date pattern the string is in
     * @param strDate a string representation of a date
     * @return a converted Date object
     * @throws java.text.ParseException
     * @see java.text.SimpleDateFormat
     */
    public static Date convertStringToDate(String aMask, String strDate)
            throws ParseException
    {
        SimpleDateFormat df;
        Date date;
        df = new SimpleDateFormat(aMask);

        if(log.isDebugEnabled())
        {
            log.debug("converting '" + strDate + "' to date with mask '"
                      + aMask + "'");
        }

        try
        {
            date = df.parse(strDate);
        } catch(ParseException pe)
        {
            //log.error("ParseException: " + pe);
            throw new ParseException(pe.getMessage(), pe.getErrorOffset());
        }

        return (date);
    }

    /**
     * This method returns the current date time in the format:
     * MM/dd/yyyy HH:MM a
     *
     * @param theTime the current time
     * @return the current date/time
     */
    public static String getTimeNow(Date theTime)
    {
        return getDateTime(timePattern, theTime);
    }

    /**
     * This method returns the current date in the format: MM/dd/yyyy
     *
     * @return the current date
     */
    public static Calendar getToday()
    {
        Date today = new Date();
        SimpleDateFormat df = new SimpleDateFormat(getDatePattern());

        // This seems like quite a hack (date -> string -> date),
        // but it works ;-)
        String todayAsString = df.format(today);
        Calendar cal = new GregorianCalendar();
        cal.setTime(convertStringToDate(todayAsString));

        return cal;
    }

    /**
     * This method generates a string representation of a date's date/time
     * in the format you specify on input
     *
     * @param aMask the date pattern the string is in
     * @param aDate a date object
     * @return a formatted string representation of the date
     * @see java.text.SimpleDateFormat
     */
    public static String getDateTime(String aMask, Date aDate)
    {
        SimpleDateFormat df;
        String returnValue = "";

        if(aDate == null)
        {
            Date today = new Date();
            df = new SimpleDateFormat(aMask);
            returnValue = df.format(today);
        } else
        {
            df = new SimpleDateFormat(aMask);
            returnValue = df.format(aDate);
        }

        return (returnValue);
    }

    /**
     * This method generates a string representation of a date based
     * on the System Property 'dateFormat'
     * in the format you specify on input
     *
     * @param aDate A date to convert
     * @return a string representation of the date
     */
    public static String convertDateToString(Date aDate)
    {
        return getDateTime(getDatePattern(), aDate);
    }

    public static String convertDateToString(String format, Date aDate)
    {
        return getDateTime(format, aDate);
    }

    /**
     * This method converts a String to a date using the datePattern
     *
     * @param strDate the date to convert (in format MM/dd/yyyy)
     * @return a date object
     */
    public static Date convertStringToDate(String strDate)
    {
        Date aDate = null;

        try
        {
            if(log.isDebugEnabled())
            {
                log.debug("converting date with pattern: " + getDatePattern());
            }

            aDate = convertStringToDate(getDatePattern(), strDate);
        } catch(ParseException pe)
        {
            log.error("Could not convert '" + strDate
                      + "' to a date, throwing exception");
            pe.printStackTrace();
        }

        return aDate;
    }

    /**
     * Date Arithmetic function. Adds the specified (signed) amount of time to
     * the given time field, based on the calendar's rules.
     * <p/>
     * For example, to subtract 5 days from a specific date, it can be achieved
     * by calling: <p>
     * DateUtil.add(date, Calendar.DATE, -5).
     * <p/>
     *
     * @param date   The date to perform the arithmetic function on
     * @param field  A Calendar constant to retrieve the field value from the Date
     *               object. Same as for {}.
     * @param amount the amount of date or time to be added to the field
     * @return The date as a result of the execution of the arithmetic function.
     */
    public static Date add(Date date, int field, int amount)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(field, amount);

        return cal.getTime();
    }
    

      public static long  differTime(java.sql.Timestamp startTs,java.sql.Timestamp endTs){
        String s1 = DateUtil.getDate(startTs,DATE_FORMAT_YYYYMMDD);//"2005-04-28";
        String s2 = DateUtil.getDate(endTs,DATE_FORMAT_YYYYMMDD);//"2005-04-28";
//        String s2 = "2005-04-20";

        SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        ParsePosition pos1 = new ParsePosition(0);
        Date dt1=formatter.parse(s1,pos);
        Date dt2=formatter.parse(s2,pos1);
        long l = (dt1.getTime() - dt2.getTime())/(3600*24*1000);

//        out.println("���"+l+"��");
        return l;
    }
   
	
	/**
	 * 根据格式返回和当前时间相差若干天的时间
	 * @param parttern
	 * @param amount
	 * @return
	 */
	public static String getTime(String parttern, int amount) {
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, amount);
		return new SimpleDateFormat(parttern).format(calendar.getTime());
	}
	public static String formatDateByFormat(java.util.Date date, String format) {
		String result = "";
		if (date != null) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				result = sdf.format(date);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

    
    /**
	 * 将字符串解析为Date
	 * 
	 * @param strTime
	 *            待解析的字符串
	 * @return 解析出来的Date
	 * @throws ParseException
	 */
	public static Date getDate(String strTime) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_YYYYMMDDHHMMSS, Locale.US);
		return sdf.parse(strTime);
	}
	
	/**
	 * 将字符串解析为Date
	 * 
	 * @param strTime
	 *            待解析的字符串
	 * @return 解析出来的Date
	 * @throws ParseException
	 */
	public static Date getDate(String strTime, String parttern) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(parttern, Locale.US);
		return sdf.parse(strTime);
	}
	
	/**
	 * 根据格式返回和当前时间相差若干月的时间
	 * @param parttern
	 * @param amount
	 * @return
	 */
	public static String getMonthAgoTime(String parttern, int amount) {
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, amount);
		return new SimpleDateFormat(parttern).format(calendar.getTime());
	}
	
	/**
	 * 根据格式返回当前时间,例"yyyy-MM-dd HH:mm:ss"
	 */
	public static String getCurrentTime(String pattern) {
		return new SimpleDateFormat(pattern).format(new Date());
	}
	
	/**
	 * 使用参数Format将字符串转为Date
	 */
	public static Date parse(String strDate, String pattern) throws ParseException {
		return StringUtils.isBlank(strDate) ? null : new SimpleDateFormat(pattern).parse(strDate);
	}
	
	/**
	 * 使用预设Format格式化Date成字符串
	 */
	public static String format(Date date) {
		return date == null ? "" : format(date, getDatePattern());
	}

	/**
	 * 使用参数Format格式化Date成字符串
	 */
	public static String format(Date date, String pattern) {
		return date == null ? "" : new SimpleDateFormat(pattern).format(date);
	}
	/**
	 * 取传入时间的月第一天
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static Date getFristDateOfMonth(Date date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-01");
        String s = sdf.format(date);
        return sdf.parse(s);
    }
	/**
	 * 取传入时间的月最后一天
	 * @param date
	 * @return
	 * @throws ParseException
	 */
    public static Date getLastDayOfMonth(Date date) throws ParseException{
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int days = c.getActualMaximum(Calendar.DAY_OF_MONTH);
//        System.out.println(days);
        c.set(Calendar.DAY_OF_MONTH,days);
        c.set(Calendar.HOUR_OF_DAY,23);
        c.set(Calendar.MINUTE,59);
        return c.getTime();
    }
    
    /**
	 * 比对两时间
	 * @param DATE1
	 * @param DATE2
	 * @return 1：date1>date2  -1:date1<date2  0:date1=date1
	 * @author tanpeng
	 */
	public static int compareDate(String date1, String date2) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date dt1 = df.parse(date1);
			Date dt2 = df.parse(date2);
			if (dt1.getTime() > dt2.getTime()) {
//				System.out.println("dt1 在dt2前");
				return 1;
			} else if (dt1.getTime() < dt2.getTime()) {
//				System.out.println("dt1在dt2后");
				return -1;
			} else {
				return 0;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0;
	}

}
