package byzas.works.springbatchsample;

import byzas.works.springbatchsample.domain.SampleTable2Entity;
import byzas.works.springbatchsample.domain.SampleTableEntity;
import byzas.works.springbatchsample.repository.SampleTable2Repository;
import byzas.works.springbatchsample.repository.SampleTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.data.domain.Sort;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author ext0280263 on 12.04.2020
 * @project IntelliJ IDEA
 */

@Configuration
@RequiredArgsConstructor
@Log4j2
public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;



    @Bean
    public Job processSampleTableDataJob(SampleTableRepository sampleTableRepository, SampleTable2Repository sampleTable2Repository) {
        return jobBuilderFactory.get("processSampleTableDataJob")
                .incrementer(new RunIdIncrementer()).listener(listener())
                .flow(processSampleTableDataStep(sampleTableRepository, sampleTable2Repository)).end().build();
    }

    public Step processSampleTableDataStep(SampleTableRepository sampleTableRepository, SampleTable2Repository sampleTable2Repository) {
        HashMap<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("id", Sort.Direction.DESC);
        RepositoryItemReader<SampleTableEntity> reader = new RepositoryItemReaderBuilder<SampleTableEntity>()
                .repository(sampleTableRepository)
                .methodName("findRecords")
                .pageSize(10)
                .sorts(sorts)
                .saveState(false)
                .build();

        ItemProcessor<SampleTableEntity, SampleTable2Entity> processor = new ItemProcessor<SampleTableEntity, SampleTable2Entity>() {
            @Override
            public SampleTable2Entity process(SampleTableEntity o) throws Exception {
                log.info("Processor {}", o.getId());
                SampleTable2Entity otherEntity =
                        SampleTable2Entity.builder()
                                .y(o.getY())
                                .x(o.getX())
                                .z(new Date())
                                .t(new Date()).build();
                return otherEntity;
            }
        };

        ItemWriter<SampleTable2Entity> sampleTable2Writer = new ItemWriter<SampleTable2Entity>() {
            @Override
            public void write(List<? extends SampleTable2Entity> list) throws Exception {
                log.info("----------------------------------------");
                log.info("Record Count is {} that will be write", list.size());
                list.stream().forEach(System.out::println);
                log.info("----------------------------------------");
            }
        };


        return stepBuilderFactory.get("orderStep1").<SampleTableEntity, SampleTable2Entity>chunk(5)
                .reader(reader)
                .processor(processor)
                .writer(sampleTable2Writer)
                .faultTolerant()
                .retryLimit(3)
                .retry(RuntimeException.class)
                .retry(DeadlockLoserDataAccessException.class)
                .build();
    }

    @Bean
    public JobExecutionListener listener() {
        return new JobCompletionListener();
    }

}

class JobCompletionListener extends JobExecutionListenerSupport {
    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            System.out.println("BATCH JOB COMPLETED SUCCESSFULLY");
        }
    }
}
