package byzas.works.springbatchsample;

import byzas.works.springbatchsample.domain.SampleTable2Entity;
import byzas.works.springbatchsample.domain.SampleTableEntity;
import byzas.works.springbatchsample.repository.SampleTable2Repository;
import byzas.works.springbatchsample.repository.SampleTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
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
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StopWatch;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author ext0280263 on 12.04.2020
 * @project IntelliJ IDEA
 */

@Configuration
@RequiredArgsConstructor
@Log4j2
@EnableBatchProcessing
public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public TaskExecutor taskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(64);
        executor.setMaxPoolSize(64);
        executor.setQueueCapacity(64);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("Parallel-");
        return executor;
    }

    @Bean
    public Job processSampleTableDataJob(SampleTableRepository sampleTableRepository, SampleTable2Repository sampleTable2Repository) {
        return jobBuilderFactory.get("processSampleTableDataJob")
                .preventRestart()
                .incrementer(new RunIdIncrementer()).listener(listener())
                .flow(processSampleTableDataStep(sampleTableRepository, sampleTable2Repository))
                .end().build();
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
//                log.info("Processor {}", o.getId());
//                Thread.sleep(1000);
//                if(1==1) throw new RuntimeException("Exception handle");
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
//                log.info("----------------------------------------");
//                log.info("Record Count is {} that will be write", list.size());
//                list.stream().forEach(System.out::println);
//                log.info("----------------------------------------");
//                sampleTable2Repository.saveAll(list);
            }
        };


        return stepBuilderFactory.get("orderStep1").<SampleTableEntity, SampleTable2Entity>chunk(5)
                .reader(reader)
                .processor(processor)
                .writer(sampleTable2Writer)
                .faultTolerant()
                .retryLimit(5)
                .retry(RuntimeException.class)
//                .retry(DeadlockLoserDataAccessException.class)
//                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public JobExecutionListener listener() {
        return new JobCompletionListener();
    }

}

@Log4j2
class JobCompletionListener extends JobExecutionListenerSupport {
    StopWatch watch = new StopWatch("SampleDataProcess");

    @Override
    public void beforeJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.STARTED){
            log.info("Job Started : {}", new Date());
            watch.start();
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("Job Finished : {}", new Date());
            watch.stop();
            log.info("Total time : {}", watch.getTotalTimeSeconds());
        }
    }


}
