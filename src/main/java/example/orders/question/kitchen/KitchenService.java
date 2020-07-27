package example.orders.question.kitchen;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import example.orders.question.model.Order;
import example.orders.question.shelf.ShelfStorageService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Data
public class KitchenService {

	private static final String SERVICE_NAME = "KITCHEN";
	
	@Autowired
	@Qualifier("ordersQueue")
	private BlockingQueue<Order> ordersQueue;
	
	@Value("${orders.ingestion.rate:2}")
	private int ingestionRate;
		
	@Autowired
	private ShelfStorageService shelfService;

	public void getOrdersToKitchen() {
		
		while(true) {
			if(ordersQueue.isEmpty()) {
				log.info("[{}] - All Orders are Serviced !!! About to Take Rest..!!!", SERVICE_NAME);
				break;
			}
			
			int index = 0;

			LocalDateTime  currentTime = LocalDateTime.now();
			
			while(!ordersQueue.isEmpty() && index < ingestionRate) {
				
				Order order = ordersQueue.poll();
				
				order.setOrderReceivedTime(currentTime);
				
				log.info("[{}] at Time: {}  [RECEIVED] an Order {} ", SERVICE_NAME, currentTime, order);
				
				shelfService.addOrder(order);
				
				index++;
			}
		}
		
	}	
 }
