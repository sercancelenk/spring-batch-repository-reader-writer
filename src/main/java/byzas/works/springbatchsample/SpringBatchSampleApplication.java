package byzas.works.springbatchsample;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.task.TaskExecutor;

@SpringBootApplication
@RequiredArgsConstructor
public class SpringBatchSampleApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchSampleApplication.class, args);
	}


	private final JobLauncher jobLauncher;
	private final Job processSampleTableDataJob;
	private final TaskExecutor taskExecutor;

	@Override
	public void run(String... args) throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("time", System.currentTimeMillis(), true)
				.toJobParameters();

		jobLauncher.run(processSampleTableDataJob, jobParameters);
		System.out.println("Batch job has been invoked");

		Thread.sleep(30000);
	}
}
