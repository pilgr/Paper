package util;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. User: o.masnyi Date: 08.06.12 Time: 14:34 To change this template use
 * File | Settings | File Templates.
 */
public class DelayMeasurer {

    private static final String TAG = DelayMeasurer.class.getSimpleName();

    private static final Map<String, Long> tStartTimeMap = new HashMap<>();
    private static final Map<String, Long> tLastCheckTimeMap = new HashMap<>();

    public static void start(String tag) {
        tStartTimeMap.put(tag, System.currentTimeMillis());
    }

    public static void finish(String tag) {
        finish(tag, 1);
    }

    public static void finish(String tag, int divideResult) {
        checkTime(tag, null, divideResult);
        tStartTimeMap.remove(tag);
        tLastCheckTimeMap.remove(tag);
    }

    public static void checkTime(String tag, String stage) {
        checkTime(tag, stage, 1);
    }

    public static void checkTime(String tag, String stage, int divideTo) {
        long spentTime = getSpentTime(tag) / divideTo;
        if (spentTime == -1) return;
        String dividerLbl = divideTo == 1 ? "" : "/" + divideTo;
        String toPrint = tag + dividerLbl + " " + spentTime + "ms";
        if (stage != null) {
            long checkPointTime = getCheckPointTime(tag) / divideTo;
            toPrint = toPrint + ": " + stage + ": " + checkPointTime;
        }
        Log.d(TAG, toPrint);
    }

    private static long getCheckPointTime(String tag) {
        long curr = System.currentTimeMillis();
        Long lastCheck = tLastCheckTimeMap.get(tag);
        tLastCheckTimeMap.put(tag, curr);
        if (lastCheck != null) {
            return curr - lastCheck;
        }
        return 0;
    }

    public static long getSpentTime(String tag) {
        Long startTime = tStartTimeMap.get(tag);
        if (startTime == null) {
            return -1;
        }
        return System.currentTimeMillis() - startTime;
    }
}
