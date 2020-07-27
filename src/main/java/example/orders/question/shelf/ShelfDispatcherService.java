package example.orders.question.shelf;

import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service(value = "dispatcherService")
@Data
@Slf4j
public class ShelfDispatcherService {

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

	public final static double EPSILON = 0.1;

	public final static int COURIER_MIN_DELAY_SEC = 2;

	public final static int COURIER_MAX_DELAY_SEC = 6;

	public double getOrderValue(boolean isAnyTempShelf, Order order) {

		LocalDateTime now = LocalDateTime.now();

		long orderAge = order.getOrderReceivedTime().until(now, ChronoUnit.SECONDS);

		double decayRate = order.getDecayRate();

		int shelfLife = order.getShelfLife();

		int shelfDecayModifier = isAnyTempShelf ? 2 : 1;

		double val = (shelfLife - decayRate * orderAge * shelfDecayModifier) / shelfDecayModifier;

		return val;
	}

	public boolean isShelfLifeValid(boolean isAnyTempShelf, Order order) {
		return getOrderValue(isAnyTempShelf, order) > EPSILON;
	}

	@Async
	public void dispatchHotOrders() {

		log.info(
				"--- DISPATCH HOT ITEMS AS LONG AS THERE ARE SOME ORDERS FROM KITCHEN TO SHELF AND HOTSHELF IS NOT EMPTY----");

		while (!ordersQueue.isEmpty() || !hotShelf.isEmpty()) {

			List<Order> validOrders = hotShelf.stream().filter(order -> this.isShelfLifeValid(false, order))
					.collect(toList());

			if (validOrders.size() > 0) {

				log.info("VALID HOT ORDERS FOR DISPATCH TO COURIER SERVICE: {}", validOrders);

			}

			List<OrderEvent> events = validOrders.stream().map(order -> new OnHotOrderReadyEvent(order, true))
					.collect(toList());

			events.forEach(this::eventDeliveryForCourier);

			hotShelf.removeIf(validOrders::contains);

			List<Order> inValidOrders = hotShelf.stream().filter(order -> !this.isShelfLifeValid(false, order))
					.collect(toList());

			hotShelf.removeIf(inValidOrders::contains);

			if (inValidOrders.size() > 0) {

				log.info("DISPOSED INVALID HOT ORDERS FOR DISPOSAL: {}, NOW HOT_SHELF: {}", inValidOrders, hotShelf);

			}
		}

		log.info("--- ALL HOT ITEMS ARE EMPTY. GOOD BYE HOT-SHELF----");

	}

	@Async
	public void dispatchColdOrders() {

		log.info(
				"--- DISPATCH COLD ITEMS AS LONG AS THERE ARE SOME ORDERS FROM KITCHEN TO SHELF AND COLD SHELF IS NOT EMPTY----");

		while (!ordersQueue.isEmpty() || !coldShelf.isEmpty()) {

			List<Order> validOrders = coldShelf.stream().filter(order -> this.isShelfLifeValid(false, order))
					.collect(toList());

			if (validOrders.size() > 0) {
				log.info("VALID COLD ORDERS FOR DISPATCH TO COURIER SERVICE: {}", validOrders);
			}

			List<OrderEvent> events = validOrders.stream().map(order -> new OnColdOrderEvent(order, true))
					.collect(toList());

			events.forEach(this::eventDeliveryForCourier);

			coldShelf.removeIf(validOrders::contains);

			List<Order> inValidOrders = coldShelf.stream().filter(order -> !this.isShelfLifeValid(false, order))
					.collect(toList());

			coldShelf.removeIf(inValidOrders::contains);

			if (inValidOrders.size() > 0) {
				log.info("DISPOSED INVALID COLD ORDERS FOR DISPOSAL DUE TO AGED ORDERS: {}, NOW COLD_SHELF: {}",
						inValidOrders, coldShelf);
			}

		}

		log.info("--- ALL COLD ITEMS ARE EMPTY. GOOD BYE COLD-SHELF----");

	}

	@Async
	public void dispatchFrozeOrders() {

		log.info(
				"--- DISPATCH FROZE ITEMS AS LONG AS THERE ARE SOME ORDERS FROM KITCHEN TO SHELF AND FROZE SHELF IS NOT EMPTY----");

		while (!ordersQueue.isEmpty() || !frozenShelf.isEmpty()) {

			List<Order> validOrders = frozenShelf.stream().filter(order -> this.isShelfLifeValid(false, order))
					.collect(toList());

			if (validOrders.size() > 0) {
				log.info("VALID FROZEN ORDERS FOR DISPATCH TO COURIER SERVICE: {}", validOrders);
			}

			List<OrderEvent> events = validOrders.stream().map(order -> new OnFreezeOrderReadyEvent(order, true))
					.collect(toList());

			events.forEach(this::eventDeliveryForCourier);

			frozenShelf.removeIf(validOrders::contains);

			List<Order> inValidOrders = frozenShelf.stream().filter(order -> !this.isShelfLifeValid(false, order))
					.collect(toList());

			frozenShelf.removeIf(inValidOrders::contains);

			if (inValidOrders.size() > 0) {
				log.info("DISPOSED INVALID FROZEN ORDERS FOR DISPOSAL: {}, NOW FROZEN_SHELF: {}", inValidOrders,
						frozenShelf);
			}

		}

		log.info("--- ALL FROZE ITEMS ARE EMPTY. GOOD BYE FROZE-SHELF----");

	}

	@Async
	public void removeOutdatedHotOrders() {

		while (!ordersQueue.isEmpty() || !hotShelf.isEmpty()) {

			List<Order> inValidOrders = hotShelf.stream().filter(order -> !this.isShelfLifeValid(false, order))
					.collect(toList());

			if (inValidOrders.size() > 0) {
				log.info("--- COLLECTED OUTDATED ORDERS FROM HOT SHELF {} ----", inValidOrders);
				hotShelf.removeIf(inValidOrders::contains);
				log.info("--- AFTER REMOVING OUTDATED ORDERS FROM HOT SHELF  {} ----", hotShelf);
			}

		}
	}

	@Async
	public void removeOutdatedColdOrders() {

		while (!ordersQueue.isEmpty() || !coldShelf.isEmpty()) {

			List<Order> inValidOrders = coldShelf.stream().filter(order -> !this.isShelfLifeValid(false, order))
					.collect(toList());

			coldShelf.removeIf(inValidOrders::contains);

			if (inValidOrders.size() > 0) {

				log.info("--- COLLECTED OUTDATED ORDERS FROM COLD SHELF {} ----", inValidOrders);

				log.info("--- AFTER REMOVING OUTDATED ORDERS FROM COLD SHELF {} ----", coldShelf);

			}
		}
	}

	@Async
	public void removeOutdatedFrozenOrders() {

		while (!ordersQueue.isEmpty() || !frozenShelf.isEmpty()) {

			List<Order> inValidOrders = frozenShelf.stream().filter(order -> !this.isShelfLifeValid(false, order))
					.collect(toList());

			frozenShelf.removeIf(inValidOrders::contains);

			if (inValidOrders.size() > 0) {

				log.info("--- COLLECTED OUTDATED ORDERS FROM FROZE SHELF {} ----", inValidOrders);

				log.info("--- AFTER REMOVING OUTDATED ORDERS FROM FREEZE SHELF {} ----", frozenShelf);

			}
		}
	}

	@Async
	public void removeOutdatedAnyTempOrders() {

		while (!ordersQueue.isEmpty() || !anyTemperatureShelf.isEmpty()) {

			List<Order> inValidOrders = anyTemperatureShelf.stream()
					.filter(order -> !this.isShelfLifeValid(true, order)).collect(toList());

			
			anyTemperatureShelf.removeIf(inValidOrders::contains);
			
			if (inValidOrders.size() > 0) {

				log.info("--- COLLECTED OUTDATED ORDERS FROM ANY_TEMP SHELF {} ----", inValidOrders);

				log.info("--- AFTER REMOVING OUTDATED ORDERS FROM ANY_TEMP SHELF {} ----", anyTemperatureShelf);
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

		log.info("Order:{}, courierDelayInterval: {}, published event", event.getOrder(), courierDelayInterval);

	}
}
