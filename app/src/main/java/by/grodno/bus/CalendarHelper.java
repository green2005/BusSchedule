package by.grodno.bus;

import android.content.Context;

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
        int i = getDayNumber() - 1;
        switch (i){
            case 1:return context.getResources().getString(R.string.mon);
            case 2:return context.getResources().getString(R.string.tue);
            case 3:return context.getResources().getString(R.string.wed);
            case 4:return context.getResources().getString(R.string.thu);
            case 5:return context.getResources().getString(R.string.fri);
            case 6:return context.getResources().getString(R.string.sat);
            case 7:return context.getResources().getString(R.string.sun);
        }
        return null;
    }

    public static String getDay2(Context context) {
        if (getDayNumber() < 6) {
            return context.getResources().getString(R.string.workday);
        } else {
            return context.getResources().getString(R.string.dayoff);
        }
    }

    public static String getTime() {
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
        Date now = new Date();
        return sdfTime.format(now);
    }

    public static int getMinutes(){
        Calendar cl = Calendar.getInstance();
        int hour = cl.get(Calendar.HOUR_OF_DAY);
        int minute = cl.get(Calendar.MINUTE);
        return hour*60 + minute;
    }

}
