package com.apex.tracker.stats.diff;

public abstract class AbstractStatDiffChainTask implements StatDiffChainTask {

    private StatDiffChainTask nextTask;

    @Override
    public void nextOrExit(DiffWorkspace wspace) {
           if (nextTask != null) {
               nextTask.process(wspace);
           }
    }

    public StatDiffChainTask setNext(StatDiffChainTask task) {
        this.nextTask = task;
        return nextTask;
    }
}
