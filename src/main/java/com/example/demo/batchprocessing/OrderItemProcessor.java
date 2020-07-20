package com.example.demo.batchprocessing;

import org.springframework.batch.item.ItemProcessor;

import com.example.demo.accessingdatamysql.Order;

public class OrderItemProcessor implements ItemProcessor<Order, Order> {
	
	@Override
	public Order process(final Order order) throws Exception {
		return order;
	}
}
