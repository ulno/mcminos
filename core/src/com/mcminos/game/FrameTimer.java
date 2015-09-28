package com.mcminos.game;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by ulno on 25.09.15.
 */
public class FrameTimer {
    long lastFrame = -1;
    long nowFrame = 0;
    ArrayList<Task> tasks = new ArrayList<>();

    /**
     *  @param task to schedule
     * @param interval from now when it is supposed to be executed*/
    public void schedule(Task task, int interval) {
        task.scheduleFrame = nowFrame + interval; // set search-key
        int index = Collections.binarySearch(tasks, task); // make sure it's sorted TODO: here could be a race
        if(index<0)
            index = -index - 1;
        tasks.add(index, task);
    }

    public void update(long gameFrame) {
        nowFrame = gameFrame;
        while (tasks.size() > 0 && tasks.get(0).scheduleFrame <= nowFrame) {
            Task t = tasks.get(0);
            tasks.remove(0);
            t.run(); // this can create new tasks, so make sure this task has been removed from lsit before
        }
        lastFrame = gameFrame;
    }

    static abstract public class Task implements Runnable,Comparable<Task> {
        long scheduleFrame=-1; // The frame where this should be executed

        abstract public void run ();

        @Override
        public int compareTo(Task task) {
            return  (int)(scheduleFrame - task.scheduleFrame); // TODO: consider overrun
        }
    }

}
