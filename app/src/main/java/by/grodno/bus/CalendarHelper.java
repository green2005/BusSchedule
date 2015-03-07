package by.grodno.bus;

import android.content.Context;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CalendarHelper {
    private static int getDayNumber() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        return c.get(Calendar.DAY_OF_WEEK);
    }

    public static String getDay1(Context context) {
        //todo fix bug in case of time > 00.00 and time<04.00
        int i = getDayNumber() - 1;
        switch (i) {
            case 1:
                return context.getResources().getString(R.string.mon);
            case 2:
                return context.getResources().getString(R.string.tue);
            case 3:
                return context.getResources().getString(R.string.wed);
            case 4:
                return context.getResources().getString(R.string.thu);
            case 5:
                return context.getResources().getString(R.string.fri);
            case 6:
                return context.getResources().getString(R.string.sat);
            case 7:
                return context.getResources().getString(R.string.sun);
        }
        return null;
    }

    public static String getDay2(Context context) {
        if (getDayNumber() - 1 < 6) {
            return context.getResources().getString(R.string.workday);
        } else {
            return context.getResources().getString(R.string.dayoff);
        }
    }

    public static String getTimeDiff(Context context, int sqlMinutes) {
        int minutes = getMinutes();
        int diff = sqlMinutes - minutes;
        String s;
        if (diff > 60) {
            int mins = diff % 60;
            int hours = diff / 60;
            s = String.valueOf(hours) + " " + context.getString(R.string.hour) + " " +
                    String.valueOf(mins) + " " + context.getString(R.string.minute);
        } else if (diff < 0) {
            s = "";
        } else {
            s = String.valueOf(diff) + " " + context.getString(R.string.minute);
        }
        if (!TextUtils.isEmpty(s)) {
            s = context.getResources().getString(R.string.ina) + " " + s;
        }
        return s;
    }

    public static String getTime() {
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH.mm");
        Date now = new Date();
        return sdfTime.format(now);
    }

    public static int getMinutes() {
        Calendar cl = Calendar.getInstance();
        int hour = cl.get(Calendar.HOUR_OF_DAY);
        int minute = cl.get(Calendar.MINUTE);
        return hour * 60 + minute;
    }

}
