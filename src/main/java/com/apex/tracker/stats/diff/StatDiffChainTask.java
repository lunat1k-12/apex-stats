package com.apex.tracker.stats.diff;

public interface StatDiffChainTask {

    void process(DiffWorkspace wspace);

    void nextOrExit(DiffWorkspace wspace);

    StatDiffChainTask setNext(StatDiffChainTask task);
}
