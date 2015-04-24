package by.grodno.bus;

import android.content.Context;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarHelper {
    private static int getDayNumber() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        return c.get(Calendar.DAY_OF_WEEK);
    }

    public static String getHour() {
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH");
        Date now = new Date();
        return sdfTime.format(now).replace("00.", "24.");
    }

    public static String now(){
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH.mm");
        Date now = new Date();
        return sdfTime.format(now).replace("00.", "24.");
    }

    public static String getDate() {
        SimpleDateFormat sdfTime = new SimpleDateFormat("dd.MM.yyyy");
        Date now = new Date();
        return sdfTime.format(now);
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
            case 0:
                return context.getResources().getString(R.string.sun);
        }
        return null;
    }

    public static List<String> getDaySynonims(String day, Context context) {
        List<String> sn = new ArrayList<>();
        sn.add(day);
        String sun = context.getResources().getString(R.string.sun);
        String sat = context.getResources().getString(R.string.sat);
        String dayOff = context.getResources().getString(R.string.dayoff);
        String workDay = context.getResources().getString(R.string.workday);
        if (day.equals(sun)||day.equals(sat)) {
            sn.add(dayOff);
        } else if (day.equals(dayOff)) {
            sn.add(sun);
            sn.add(sat);
        } else if (day.equals(workDay)) {
            sn.add(context.getResources().getString(R.string.mon));
            sn.add(context.getResources().getString(R.string.tue));
            sn.add(context.getResources().getString(R.string.wed));
            sn.add(context.getResources().getString(R.string.tue));
            sn.add(context.getResources().getString(R.string.fri));
        } else
        {
            sn.add(workDay);
        }
        return sn;
    }

    public static String getDay2(Context context) {
        int i = getDayNumber() - 1;
        if (i == 0 || i == 6) {
            return context.getResources().getString(R.string.dayoff);
        } else {
            return context.getResources().getString(R.string.workday);
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
