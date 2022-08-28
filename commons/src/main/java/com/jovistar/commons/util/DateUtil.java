package com.jovistar.commons.util;

import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

public class DateUtil extends Date {

    static DateUtil instance;

    public static DateUtil getInstance() {
        if(instance==null)
            instance = new DateUtil();
        return instance;
    }

    public Date valueOfDate(String datestr) {
        if (datestr == null) {
            return null;
        //SQL format: yyyy-mm-dd hh:mm:ss.fffffffff
        }
//CCMIDlet.debug("datestr:"+datestr);
        int year = Integer.valueOf(datestr.substring(0, 4)).intValue();
        int month = Integer.valueOf(datestr.substring(5, 7)).intValue();
        int day = Integer.valueOf(datestr.substring(8, 10)).intValue();
        int hour = Integer.valueOf(datestr.substring(11, 13)).intValue();
        int min = Integer.valueOf(datestr.substring(14, 16)).intValue();
//        int sec = Integer.valueOf(datestr.substring(17, 19)).intValue();
//CCMIDlet.debug("yr:"+year);
//CCMIDlet.debug("month:"+month);
//CCMIDlet.debug("day:"+day);
//CCMIDlet.debug("hour:"+hour);
//CCMIDlet.debug("min:"+min);
//CCMIDlet.debug("sec:"+sec);

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month-1);
        cal.set(Calendar.DATE, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
//        cal.set(Calendar.SECOND, sec);

        return cal.getTime();
    }

    public Date valueOfTime(String timestr) {
        //SQL format: hh:mm:ss

        int hour = Integer.valueOf(timestr.substring(0, 2)).intValue();
        int min = Integer.valueOf(timestr.substring(3, 5)).intValue();
        int sec = Integer.valueOf(timestr.substring(6, 8)).intValue();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, sec);

        return cal.getTime();
    }

    public String toSQLString(Date date) {
        if (date == null) {
            return null;
        //Date toString() format: dow mon dd hh:mm:ss zzz yyyy
        }
        String datestr = date.toString();
        String year = datestr.substring(datestr.length() - 4);
        String month = datestr.substring(4, 7);
        String day = datestr.substring(8, 10);
        String hour = datestr.substring(11, 13);
        String min = datestr.substring(14, 16);
        String sec = datestr.substring(17, 18);

        return year + "-" + monthasint(month) + "-" + day + " " + hour + ":" + min + ":" + sec;
    }

    public String monthasint(String mon) {

        if (mon.toUpperCase().equals("JAN")) {
            return "01";
        } else if (mon.toUpperCase().equals("FEB")) {
            return "02";
        } else if (mon.toUpperCase().equals("MAR")) {
            return "03";
        } else if (mon.toUpperCase().equals("APR")) {
            return "04";
        } else if (mon.toUpperCase().equals("MAY")) {
            return "05";
        } else if (mon.toUpperCase().equals("JUN")) {
            return "06";
        } else if (mon.toUpperCase().equals("JUL")) {
            return "07";
        } else if (mon.toUpperCase().equals("AUG")) {
            return "08";
        } else if (mon.toUpperCase().equals("SEP")) {
            return "09";
        } else if (mon.toUpperCase().equals("OCT")) {
            return "10";
        } else if (mon.toUpperCase().equals("NOV")) {
            return "11";
        } else if (mon.toUpperCase().equals("DEC")) {
            return "12";
        } else {
            return "";
        }
    }

    public String monthasstr(int mon) {

        if (mon==1) {
            return "january";
        } else if (mon==2) {
            return "february";
        } else if (mon==3) {
            return "march";
        } else if (mon==4) {
            return "april";
        } else if (mon==5) {
            return "may";
        } else if (mon==6) {
            return "june";
        } else if (mon==7) {
            return "july";
        } else if (mon==8) {
            return "august";
        } else if (mon==9) {
            return "september";
        } else if (mon==10) {
            return "october";
        } else if (mon==11) {
            return "november";
        } else if (mon==12) {
            return "december";
        } else {
            return "";
        }
    }

    public String sqldatewithoutyear(String dt) {
        //IN, SQL format: yyyy-mm-dd 1980-01-02
        //OUT, String format: dd-mmm 02-jan
        StringBuffer sb = new StringBuffer(7);
        if(dt==null)
            return "";
        else if(dt.length()>9) {
            sb.append(dt.substring(8, 10)).append("-");
            sb.append(monthasstr(Integer.parseInt(dt.substring(5,7))));
            return sb.toString();
            //return dt.substring(5).concat(" ").concat(sb.toString());
        } else
            return dt;
    }

    public String sqltimewithoutyear(String dt) {
        //IN, SQL format: yyyy-mm-dd hh:mm:ss.fffffffff
        //OUT, SQL format: dd-mmm 02-jan hh:mm
        StringBuffer sb = new StringBuffer(13);
        if(dt==null)
            return "";
        else if(dt.length()>15) {
            sb.append(dt.substring(8, 10)).append("-");
            sb.append(monthasstr(Integer.parseInt(dt.substring(5,7)))).append(" ");
            sb.append(dt.substring(11, 16));
            return sb.toString();
            //return dt.substring(5, 16);
        }
        else
            return dt;
    }

    public String sqltimewithyear(String dt) {
        //IN, SQL format: yyyy-mm-dd hh:mm:ss.fffffffff
        //OUT, SQL format: yyyy-mm-dd hh:mm:ss
        if(dt.length()>=19)
            return dt.substring(0, 19);
        else
            return dt;
    }
}
