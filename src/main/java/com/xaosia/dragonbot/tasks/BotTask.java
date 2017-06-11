package com.xaosia.dragonbot.tasks;


public abstract class BotTask implements Runnable {

    private String taskName;

    private BotTask() {
    }

    public BotTask(String taskName) {
        this.taskName = taskName;
    }

    public boolean repeat(long delay, long interval) {
        return Scheduler.scheduleRepeating(this, taskName, delay, interval);
    }

    public void delay(long delay) {
        Scheduler.delayTask(this, delay);
    }

    public boolean cancel() {
        return Scheduler.cancelTask(taskName);
    }
}
