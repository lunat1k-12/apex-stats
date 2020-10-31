package com.apex.tracker.stats.diff.conf;

import com.apex.tracker.repository.StatRepository;
import com.apex.tracker.stats.diff.DiffTaskChain;
import com.apex.tracker.stats.diff.StatDiffChainTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DiffConfig {

    @Bean
    public DiffTaskChain taskChain(List<StatDiffChainTask> tasks,
                                   StatRepository statRepository) {
        return DiffTaskChain.create()
                .statRepository(statRepository)
                .tasks(tasks);
    }
}
