package com.medx.android.classes.notification.timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by alexey on 10/19/16.
 */

public class ActionTimer {
    List<ActionTimerListener> listeners = new ArrayList<ActionTimerListener>();

    public final int INTERVAL = 500;
    public final int ONE_SECOND = 1000;
    public int DELAY = 3*ONE_SECOND;

    private long startTimeInMillis = -1L;
    private Timer timer = null;

    public ActionTimer() {

    }

    /**
     * Start the timer with the default time delay setting.
     */
    public void startTimer() {
        startTimer(3*ONE_SECOND);
    }

    /**
     * Start the timer with a custom time delay.
     *
     * @param delayInMillis milliseconds to delay
     */
    public void startTimer(int delayInMillis) {
        startTimeInMillis = System.currentTimeMillis();

        DELAY = delayInMillis;

        if (timer == null) {
            timer = new Timer();
            timer.schedule(new ActionTimerTask(), INTERVAL, INTERVAL);
        }
    }

    /**
     * Stop the active timer.
     */
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Add a class as a listener to this timer.
     *
     * @param listener to be added.
     */
    public void addListener(ActionTimerListener listener) {

        if(!listeners.contains(listener))
            this.listeners.add(listener);
    }

    /**
     * Remove a class as a listener for this timer.
     *
     * @param listener to be removed.
     */
    public void removeListener(ActionTimerListener listener) {
        if(listeners.contains(listener))
            this.listeners.remove(listener);
    }

    private class ActionTimerTask extends TimerTask {

        @Override
        public void run() {

            long currentTimeInMillis = System.currentTimeMillis();

            if ((currentTimeInMillis - startTimeInMillis) >= DELAY) {

                for(ActionTimerListener l : listeners)
                    l.actionTimerCompleted();

                stopTimer();
            }
        }
    }
}
