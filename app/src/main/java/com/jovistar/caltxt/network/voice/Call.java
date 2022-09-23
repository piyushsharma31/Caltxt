package com.jovistar.caltxt.network.voice;

import java.util.concurrent.TimeUnit;

/**
 * Created by jovika on 2/1/2017.
 */

public class Call {
    String number;
    long millis;

    Call(String n, long t) {
        number = n;
        millis = t;
    }

    public String toString() {
        return number +
                String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(millis),
                        TimeUnit.MILLISECONDS.toMinutes(millis) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                        TimeUnit.MILLISECONDS.toSeconds(millis) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
}
