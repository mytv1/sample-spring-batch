package com.example.demo.batchprocessing;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.example.demo.accessingdatamysql.Order;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
	
	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Bean
	public FlatFileItemReader<Order> reader() {
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
	public OrderItemProcessor processor() {
		return new OrderItemProcessor();
	}
	
	@Bean
	public JdbcBatchItemWriter writer(javax.sql.DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<Order>()
			.itemSqlParameterSourceProvider(new
				BeanPropertyItemSqlParameterSourceProvider<>())
			.sql("INSERT INTO orders (customer_id, item_id, item_price, item_name, purchase_date) VALUES (:CustomerId, :ItemId, :ItemPrice, :ItemName, :PurchaseDate)")
			.dataSource(dataSource)
			.build();
	}
	
	@Bean
	public Job importOrderJob(JobCompletionNotificationListener listener, Step step1)
	{
		return jobBuilderFactory.get("importOrderJob")
			.incrementer(new RunIdIncrementer())
			.listener(listener)
			.flow(step1)
			.end()
			.build();
	}

	@Bean
	public Step step1(JdbcBatchItemWriter<Order> writer) {
	  return stepBuilderFactory.get("step1")
	    .<Order, Order> chunk(10)
	    .reader(reader())
	    .processor(processor())
	    .writer(writer)
	    .build();
	}
}
