package com.apex.tracker.stats.diff;

import com.apex.tracker.mapper.DataDtoToStateEntityMapper;
import com.apex.tracker.repository.StatRepository;

import java.util.List;

public class DiffTaskChain {

    private DiffTaskChain() {}

    private StatDiffChainTask task;
    private StatRepository statRepository;

    public static DiffTaskChain create() {
        return new DiffTaskChain();
    }

    public DiffTaskChain statRepository(StatRepository statRepository) {
        this.statRepository = statRepository;
        return this;
    }

    public DiffTaskChain tasks(List<StatDiffChainTask> tasks) {
        StatDiffChainTask firstTask = null;
        StatDiffChainTask lastTask = null;
        for (StatDiffChainTask diffTask : tasks) {
            if (lastTask == null) {
                firstTask = diffTask;
                lastTask = diffTask;
            } else {
                lastTask = lastTask.setNext(diffTask);
            }
        }

        this.task = firstTask;
        return this;
    }

    public void process(DiffWorkspace wspace) {
        this.task.process(wspace);
        if (wspace.isShouldSave()) {
            statRepository.save(DataDtoToStateEntityMapper.toEntity(wspace.getData(), wspace.getUpdateTime()));
        }
    }
}
