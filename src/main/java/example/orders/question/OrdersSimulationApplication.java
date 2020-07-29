package example.orders.question;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import example.orders.question.kitchen.KitchenService;
import example.orders.question.model.Order;
import example.orders.question.shelf.ShelfDispatcherService;
import example.orders.question.shelf.ShelfStorageService;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class OrdersSimulationApplication implements CommandLineRunner {

	@Autowired
	@Qualifier("ordersQueue")
	private BlockingQueue<Order> ordersQueue;
	
	@Autowired
	private KitchenService kitchenService;
	
	@Autowired
	private ShelfDispatcherService dispatcherService;

	private List<Order> orders;
 
	@Autowired
	private ObjectMapper mapper;


	public static void main(String[] args) {
		SpringApplication.run(OrdersSimulationApplication.class, args);
	}

	
	public File loadOrdersFile() throws FileNotFoundException {
		return ResourceUtils.getFile("classpath:orders.json");
	}
	
	@Override
	public void run(String... args) throws Exception {
		
		orders =   mapper.readValue(loadOrdersFile(), new TypeReference<ArrayList<Order>>() {});
		
		ordersQueue.addAll(orders);
		
		initializeCourierService();
		
		kitchenService.getOrdersToKitchen();
	}


	private void initializeCourierService() {

		dispatcherService.dispatchColdOrders();
		
		dispatcherService.dispatchHotOrders();
		
		dispatcherService.dispatchFrozeOrders();
		
		dispatcherService.removeOutdatedAnyTempOrders();
		
		dispatcherService.removeOutdatedColdOrders();
		
		dispatcherService.removeOutdatedFrozenOrders();
		
		dispatcherService.removeOutdatedHotOrders();
		
		dispatcherService.showStatsforfrozeShelf();
		
		dispatcherService.showStatsforColdShelf();
		
		dispatcherService.showStatsforfrozeShelf();
		
		dispatcherService.showStatsforAnyTempShelf();
		
		dispatcherService.closeNotification();
	}
		

}
