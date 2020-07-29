package example.orders.question.shelf;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import example.orders.question.courier.event.OnColdOrderEvent;
import example.orders.question.courier.event.OnFreezeOrderReadyEvent;
import example.orders.question.courier.event.OnHotOrderReadyEvent;
import example.orders.question.courier.event.OrderEvent;
import example.orders.question.kitchen.KitchenService;
import example.orders.question.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Service
@Data
@Slf4j
public class ShelfDispatcherService implements ShelfConstants {

	@Autowired
	private ShelfLifeService shelfLifeService;
	
	@Autowired
	@Qualifier("hotShelf")
	private BlockingQueue<Order> hotShelf;

	@Autowired
	@Qualifier("coldShelf")
	private BlockingQueue<Order> coldShelf;

	@Autowired
	@Qualifier("freezeShelf")
	private BlockingQueue<Order> frozenShelf;

	@Autowired
	@Qualifier("anyTempShelf")
	private BlockingQueue<Order> anyTemperatureShelf;

	@Autowired
	private KitchenService kitchenService;

	@Autowired
	private ApplicationEventPublisher orderDispatcher;

	@Autowired
	@Qualifier("ordersQueue")
	private BlockingQueue<Order> ordersQueue;

	@Async
	public void dispatchHotOrders() {

		log.debug(
				"--- DISPATCH HOT ITEMS AS LONG AS THERE ARE SOME ORDERS FROM KITCHEN TO SHELF AND HOTSHELF IS NOT EMPTY----");

		while (!ordersQueue.isEmpty() || !hotShelf.isEmpty()) {

			List<Order> validOrders = hotShelf.stream().filter(order -> shelfLifeService.isShelfLifeValid(false, order))
					.collect(toList());

			if (validOrders.size() > 0) {

				log.debug("VALID HOT ORDERS FOR DISPATCH TO COURIER SERVICE: {}", validOrders);

			}

			List<OrderEvent> events = validOrders.stream().map(order -> new OnHotOrderReadyEvent(order, true))
					.collect(toList());

			events.forEach(this::eventDeliveryForCourier);

			hotShelf.removeIf(validOrders::contains);

			List<Order> inValidOrders = hotShelf.stream().filter(order -> !shelfLifeService.isShelfLifeValid(false, order))
					.collect(toList());

			hotShelf.removeIf(inValidOrders::contains);

			if (inValidOrders.size() > 0) {

				log.debug("DISPOSED INVALID HOT ORDERS FOR DISPOSAL: {}, NOW HOT_SHELF: {}", inValidOrders, hotShelf);

			}
		}

		log.debug("--- ALL HOT ITEMS ARE EMPTY. GOOD BYE HOT-SHELF----");

	}

	@Async
	public void dispatchColdOrders() {

		log.debug(
				"--- DISPATCH COLD ITEMS AS LONG AS THERE ARE SOME ORDERS FROM KITCHEN TO SHELF AND COLD SHELF IS NOT EMPTY----");

		while (!ordersQueue.isEmpty() || !coldShelf.isEmpty()) {

			List<Order> validOrders = coldShelf.stream().filter(order -> shelfLifeService.isShelfLifeValid(false, order))
					.collect(toList());

			if (validOrders.size() > 0) {
				log.debug("VALID COLD ORDERS FOR DISPATCH TO COURIER SERVICE: {}", validOrders);
			}

			List<OrderEvent> events = validOrders.stream().map(order -> new OnColdOrderEvent(order, true))
					.collect(toList());

			events.forEach(this::eventDeliveryForCourier);

			coldShelf.removeIf(validOrders::contains);

			List<Order> inValidOrders = coldShelf.stream().filter(order -> !shelfLifeService.isShelfLifeValid(false, order))
					.collect(toList());

			coldShelf.removeIf(inValidOrders::contains);

			if (inValidOrders.size() > 0) {
				log.debug("DISPOSED INVALID COLD ORDERS FOR DISPOSAL DUE TO AGED ORDERS: {}, NOW COLD_SHELF: {}",
						inValidOrders, coldShelf);
			}

		}

		log.debug("--- ALL COLD ITEMS ARE EMPTY. GOOD BYE COLD-SHELF----");

	}

	@Async
	public void dispatchFrozeOrders() {

		log.debug(
				"--- DISPATCH FROZE ITEMS AS LONG AS THERE ARE SOME ORDERS FROM KITCHEN TO SHELF AND FROZE SHELF IS NOT EMPTY----");

		while (!ordersQueue.isEmpty() || !frozenShelf.isEmpty()) {

			List<Order> validOrders = frozenShelf.stream().filter(order -> shelfLifeService.isShelfLifeValid(false, order))
					.collect(toList());

			if (validOrders.size() > 0) {
				log.debug("VALID FROZEN ORDERS FOR DISPATCH TO COURIER SERVICE: {}", validOrders);
			}

			List<OrderEvent> events = validOrders.stream().map(order -> new OnFreezeOrderReadyEvent(order, true))
					.collect(toList());

			events.forEach(this::eventDeliveryForCourier);

			frozenShelf.removeIf(validOrders::contains);

			List<Order> inValidOrders = frozenShelf.stream().filter(order -> !shelfLifeService.isShelfLifeValid(false, order))
					.collect(toList());

			frozenShelf.removeIf(inValidOrders::contains);

			if (inValidOrders.size() > 0) {
				log.debug("DISPOSED INVALID FROZEN ORDERS FOR DISPOSAL: {}, NOW FROZEN_SHELF: {}", inValidOrders,
						frozenShelf);
			}

		}

		log.debug("--- ALL FROZE ITEMS ARE EMPTY. GOOD BYE FROZE-SHELF----");

	}

	@Async
	public void removeOutdatedHotOrders() {

		while (!ordersQueue.isEmpty() || !hotShelf.isEmpty()) {

			List<Order> inValidOrders = hotShelf.stream().filter(order -> !shelfLifeService.isShelfLifeValid(false, order))
					.collect(toList());

			if (inValidOrders.size() > 0) {
				log.debug("--- COLLECTED OUTDATED ORDERS FROM HOT SHELF {} ----", inValidOrders);
				hotShelf.removeIf(inValidOrders::contains);
				log.debug("--- AFTER REMOVING OUTDATED ORDERS FROM HOT SHELF  {} ----", hotShelf);
			}

		}
	}

	@Async
	public void removeOutdatedColdOrders() {

		while (!ordersQueue.isEmpty() || !coldShelf.isEmpty()) {

			List<Order> inValidOrders = coldShelf.stream().filter(order -> !shelfLifeService.isShelfLifeValid(false, order))
					.collect(toList());

			coldShelf.removeIf(inValidOrders::contains);

			if (inValidOrders.size() > 0) {

				log.debug("--- COLLECTED OUTDATED ORDERS FROM COLD SHELF {} ----", inValidOrders);

				log.debug("--- AFTER REMOVING OUTDATED ORDERS FROM COLD SHELF {} ----", coldShelf);

			}
		}
	}

	@Async
	public void removeOutdatedFrozenOrders() {

		while (!ordersQueue.isEmpty() || !frozenShelf.isEmpty()) {

			List<Order> inValidOrders = frozenShelf.stream().filter(order -> !shelfLifeService.isShelfLifeValid(false, order))
					.collect(toList());

			frozenShelf.removeIf(inValidOrders::contains);

			if (inValidOrders.size() > 0) {

				log.debug("--- COLLECTED OUTDATED ORDERS FROM FROZE SHELF {} ----", inValidOrders);

				log.debug("--- AFTER REMOVING OUTDATED ORDERS FROM FREEZE SHELF {} ----", frozenShelf);

			}
		}
	}

	@Async
	public void removeOutdatedAnyTempOrders() {

		while (!ordersQueue.isEmpty() || !anyTemperatureShelf.isEmpty()) {

			List<Order> inValidOrders = anyTemperatureShelf.stream()
					.filter(order -> !shelfLifeService.isShelfLifeValid(true, order)).collect(toList());

			
			anyTemperatureShelf.removeIf(inValidOrders::contains);
			
			if (inValidOrders.size() > 0) {

				log.debug("--- COLLECTED OUTDATED ORDERS FROM ANY_TEMP SHELF {} ----", inValidOrders);

				log.debug("--- AFTER REMOVING OUTDATED ORDERS FROM ANY_TEMP SHELF {} ----", anyTemperatureShelf);
			}

		}
	}

	public void eventDeliveryForCourier(OrderEvent event) {

		int courierDelayInterval = (int) (Math.random() * (COURIER_MAX_DELAY_SEC - COURIER_MIN_DELAY_SEC + 1)
				+ COURIER_MIN_DELAY_SEC);

		try {
			TimeUnit.SECONDS.sleep(courierDelayInterval);
		} catch (InterruptedException e) {
		}

		orderDispatcher.publishEvent(event);

		log.debug("Order:{}, courierDelayInterval: {}, published event", event.getOrder(), courierDelayInterval);

	}
	
	@AllArgsConstructor
	@ToString
	class OrderDisplay {
		
		Order order;
		
		double shelfLifeValue;
	}
	
	@Async
	public void showStatsforHotShelf() {
		
		while(!ordersQueue.isEmpty() || !hotShelf.isEmpty()) {

			sleep();
			
			List<OrderDisplay> hotShelfStatsList = hotShelf.stream().map(order-> new OrderDisplay(order, shelfLifeService.getOrderValue(false, order))).collect(toList());
			
			log.info("Current HOT SHELF ITEMS : {} ", hotShelfStatsList);
		}
	}
	
	@Async
	public void showStatsforColdShelf() {
		
		while(!ordersQueue.isEmpty() || !coldShelf.isEmpty()) {

			sleep();
			
			List<OrderDisplay> stats = coldShelf.stream().map(order-> new OrderDisplay(order, shelfLifeService.getOrderValue(false, order))).collect(toList());

			log.info("[STATS_TIME]: {}, Current COLD SHELF ITEMS : {} ", LocalDateTime.now(), stats);
		}
	}
	
	@Async
	public void showStatsforfrozeShelf() {
		
		while(!ordersQueue.isEmpty() || !frozenShelf.isEmpty()) {

			sleep();
			
			List<OrderDisplay> stats = frozenShelf.stream().map(order-> new OrderDisplay(order, shelfLifeService.getOrderValue(false, order))).collect(toList());

			log.info("[STATS_TIME]: {}, Current FROZE SHELF ITEMS : {} ", LocalDateTime.now(), stats);
		}
	}
	
	@Async
	public void showStatsforAnyTempShelf() {
		
		while(!ordersQueue.isEmpty() || !anyTemperatureShelf.isEmpty()) {

			sleep();
			
			List<OrderDisplay> stats = anyTemperatureShelf.stream().map(order-> new OrderDisplay(order, shelfLifeService.getOrderValue(false, order))).collect(toList());

			log.info(" [STATS_TIME]:{}, Current ANY TEMP SHELF ITEMS : {} ", LocalDateTime.now(), stats);
		}
	}

	@Async
	public void closeNotification() {
		
		boolean isCompleted = false;
		
		while(!isCompleted) {
			
			if(!ordersQueue.isEmpty()) {
				continue;
			}
			
			if(!coldShelf.isEmpty()) {
				continue;
			}
			
			if(!hotShelf.isEmpty()) {
				continue;
			}
			
			if(!anyTemperatureShelf.isEmpty()) {
				continue;
			}
			
			if(!frozenShelf.isEmpty()) {
				continue;
			}
			
			isCompleted = true;
		}
		
		log.info("ALL ORDERS ARE SERVED FROM [KITCHEN] TO [SHELF] and FROM [SHELF] TO [COURIER] AND FROM [COURIER] TO [CUSTOMER] WITH IN GIVEN TIME.. !!! GOOD BYE");
	}
	
	public  void sleep() {
		
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
		}
	}
 }
