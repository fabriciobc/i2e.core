package br.com.i2e.core.batch;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
public class CatalogSchelulerConfig {
	private static final Logger logger = LoggerFactory.getLogger( CatalogSchelulerConfig.class );

	@Autowired
	@Qualifier("catalogJobLauncher")
	private SimpleJobLauncher jobsLauncher;
	@Autowired
	private JobBuilderFactory jobsBuilder;
	@Autowired
	private StepBuilderFactory stepsBuilder;
	
	private JobExecution catalogExecution;
	private static String fetchJobName = "fetchCatalogJob";
	
	@Scheduled(cron = "0 * * ? * *")
	public void fetchCatalog() {
		try {
			if ( catalogExecution == null || catalogExecution.getStatus().equals(BatchStatus.COMPLETED) ) {
				
				var startDate = LocalDate.now();
				logger.info( "Job {} iniciado em {}", fetchJobName, startDate );
				catalogExecution = jobsLauncher.run( updateCatalogJob(), 
							new JobParametersBuilder().addLong( "JobID", System.currentTimeMillis() ).toJobParameters() );
			} else {
				
				logger.info("Já existe uma Job {} em execução...", fetchJobName);
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	@Bean
	@Qualifier(value = "updateCatalogJob" )
	public Job updateCatalogJob() throws Exception {
		return this.jobsBuilder.get("remessaJob")
				.start( getUpdateCatalogTasklet() ).build();
	}
	
	@Bean
	public Step getUpdateCatalogTasklet() {
		return stepsBuilder.get("updateCatalogTasklet")
				.tasklet( ( contribution, chunkContext ) -> {
	            	
	            
	            	
	            	return RepeatStatus.FINISHED;
				}).build();
	}

}
