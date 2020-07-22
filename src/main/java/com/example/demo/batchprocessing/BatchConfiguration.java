package com.example.demo.batchprocessing;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
	
	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	private OrderRepository orderRepository;
	
	@Bean
	public ItemReader<Order> reader() {
		return new FlatFileItemReaderBuilder<Order>()
			.name("orderItemReader")
			.resource(new ClassPathResource("orders.csv"))
			.delimited()
			.names(new String[] {"CustomerId", "ItemId", "ItemPrice", "ItemName", "PurchaseDate"})
			.fieldSetMapper(new BeanWrapperFieldSetMapper<Order>() {{
				setTargetType(Order.class);	
			}})
			.build();
	}
	
	@Bean
	public ItemProcessor<Order, Order> processor() {
		return new ItemProcessor<Order, Order>() {

			@Override
			public Order process(final Order order) throws Exception {
				return order;
			}
		};
	}
	

	@Bean
	public ItemWriter<Order> writer() {
		RepositoryItemWriter<Order> writer = new RepositoryItemWriter<>(); 
		writer.setRepository(orderRepository);
		writer.setMethodName("save");
		return writer;
	}


	@Bean
	public Job importOrderJob()
	{
		return jobBuilderFactory.get("importOrderJob")
			.incrementer(new RunIdIncrementer())
			.listener(new JobCompletionNotificationListener())
			.flow(step1())
			.end()
			.build();
	}

	@Bean
	public Step step1() {
	  return stepBuilderFactory.get("step1")
	    .<Order, Order> chunk(10)
	    .reader(reader())
	    .processor(processor())
	    .writer(writer())
	    .build();
	}
}
