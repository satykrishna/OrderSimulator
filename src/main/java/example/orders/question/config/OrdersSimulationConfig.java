package example.orders.question.config;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.fasterxml.jackson.databind.ObjectMapper;

import example.orders.question.model.Order;
import example.orders.question.shelf.ShelfConstants;

@Configuration
@EnableAsync
public class OrdersSimulationConfig implements ShelfConstants {

	@Bean
    @ConditionalOnMissingBean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator daap = new DefaultAdvisorAutoProxyCreator();
        daap.setProxyTargetClass(true);
        return daap;
    }
	
	@Bean
	public ObjectMapper jsonToPojoConverter() {
		return new ObjectMapper();
	}

	@Bean("ordersQueue")
	public BlockingQueue<Order> ordersQueue() {
		return new LinkedBlockingQueue<Order>();
	}

	@Bean("hotShelf")
	public BlockingQueue<Order> hotShelf() {
		return new LinkedBlockingQueue<>(HOT_SHELF_SIZE);
	}
	
	@Bean("coldShelf")
	public BlockingQueue<Order> coldShelf() {
		return new LinkedBlockingQueue<>(COLD_SHELF_SIZE);
	}
	
	@Bean("freezeShelf")
	public BlockingQueue<Order> freezeShelf() {
		return new LinkedBlockingQueue<>(FREEZE_SHELF_SIZE);
	}
	
	@Bean("anyTempShelf")
	public BlockingQueue<Order> anyTempShelf() {
		return new LinkedBlockingQueue<>(ANY_TEMP_SHELF_SIZE);
	}
	
	/*
	 * @Bean(name = "kitchenServiceExecutor") public Executor taskExecutor() { final
	 * ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
	 * executor.setCorePoolSize(10); executor.setMaxPoolSize(10);
	 * executor.setQueueCapacity(10);
	 * executor.setThreadNamePrefix("Kitchen-Shelf-Executor-");
	 * executor.initialize(); return executor; }
	 */

}
