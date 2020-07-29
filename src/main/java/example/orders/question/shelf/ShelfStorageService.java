package example.orders.question.shelf;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import example.orders.question.model.Order;
import example.orders.question.model.Temperature;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service(value = "shelfStorage")
@Slf4j
@Data
public class ShelfStorageService implements ShelfConstants {


	@Autowired
	private ShelfLifeService lifeService;
	
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

	public void addOrder(Order order) {

		switch (order.getTemp()) {

		case hot:
			addHotOrder(order);
			break;

		case cold:
			addColdOrder(order);
			break;

		case frozen:
			addFrozenOrder(order);
			break;

		default:
			break;

		}

	}

	private void reOrderAnyTempShelf(Order order) {

		boolean isOrderReplaced = false;

		if (hotShelf.size() < HOT_SHELF_SIZE) {

			Optional<Order> hotOrderOptional = anyTemperatureShelf.stream().filter(o -> o.getTemp() == Temperature.hot)
					.findAny();

			if (hotOrderOptional.isPresent()) {
				Order hot = hotOrderOptional.get();
				hotShelf.add(hot);
				anyTemperatureShelf.add(order);
				log.info("MOVED HOT ORDER FROM ANY_TEMP_SHELF TO HOT_SHELF: {}, [ANY TEMPERATURE SHELF] ADDED order {}",
						hot, order);
				isOrderReplaced = true;
			}
		}

		else if (coldShelf.size() < COLD_SHELF_SIZE && !isOrderReplaced) {

			Optional<Order> coldOrderOptional = anyTemperatureShelf.stream()
					.filter(o -> o.getTemp() == Temperature.cold).findAny();

			if (coldOrderOptional.isPresent()) {
				Order cold = coldOrderOptional.get();
				coldShelf.add(cold);
				anyTemperatureShelf.add(order);
				log.info(
						"MOVED COLD ORDER FROM ANY_TEMP_SHELF TO COLD_SHELF: {}, [ANY TEMPERATURE SHELF] ADDED order {}",
						cold, order);
				isOrderReplaced = true;
			}

		}

		else if (frozenShelf.size() < FREEZE_SHELF_SIZE && !isOrderReplaced) {

			Optional<Order> freezeOrderOptional = anyTemperatureShelf.stream()
					.filter(o -> o.getTemp() == Temperature.frozen).findAny();

			if (freezeOrderOptional.isPresent()) {
				Order freeze = freezeOrderOptional.get();
				frozenShelf.add(freeze);
				anyTemperatureShelf.add(order);
				log.info(
						"MOVED COLD ORDER FROM ANY_TEMP_SHELF TO FREEZE_SHELF: {}, [ANY TEMPERATURE SHELF] ADDED order {}",
						freeze, order);
				isOrderReplaced = true;
			}
		}

		else if (!isOrderReplaced) {

			// Either remove randomly or calculate the value of the order and remove it.

			Optional<Order> aged = anyTemperatureShelf.stream().filter(o -> lifeService.getOrderValue(true, o) < EPSILON).findAny();

			if (aged.isPresent()) {
				anyTemperatureShelf.remove(aged.get());
				anyTemperatureShelf.add(order);

				log.info("REMOVED RANDOM AGED ORDER FROM ANY_TEMP_SHELF: {}, [ANY TEMPERATURE SHELF] ADDED order {}",
						aged.get(), order);

			}

			else {
                Order removed = anyTemperatureShelf.poll();
				anyTemperatureShelf.add(order);
				log.info(
						"DIDN'T FIND ANY AGED ORDER IN ANY_TEMP_SHELF, REMOVE THE FIRST ONE IN QUEUE: {}, [ANY TEMPERATURE SHELF] ADDED order {}",
					    removed, order);
			}

		}
	}

	private void addFrozenOrder(Order order) {

		if (frozenShelf.size() < FREEZE_SHELF_SIZE) {
			frozenShelf.add(order);
			log.info("[FROZEN_SHELF] ADDED order {}, FROZEN_SHELF_SIZE:[{}]", order, hotShelf.size());
		}

		else if (anyTemperatureShelf.size() < ANY_TEMP_SHELF_SIZE) {
			anyTemperatureShelf.add(order);
			log.info("[FROZEN_SHELF] IS FULL. [ANY TEMPERATURE SHELF] ADDED order {}", order);
		}

		else {
			log.info(
					"BOTH [FROZEN_SHELF] [ANY TEMPERATURE SHELF] IS FULL. ABOUT TO REBALANCE EXISTING [ANY_TEMPERATURE_SHELF] FOR FROZEN ORDER {}",
					order);

			reOrderAnyTempShelf(order);
		}
	}

	private void addColdOrder(Order order) {

		if (coldShelf.size() < COLD_SHELF_SIZE) {
			coldShelf.add(order);
			log.info("[COLD_SHELF] ADDED order {}, COLD_SHELF_SIZE:[{}]", order, hotShelf.size());
		}

		else if (anyTemperatureShelf.size() < ANY_TEMP_SHELF_SIZE) {
			anyTemperatureShelf.add(order);
			log.info("[COLD_SHELF] IS FULL. [ANY TEMPERATURE SHELF] ADDED order {}", order);
		}

		else {
			log.info(
					"BOTH [COLD_SHELF] [ANY TEMPERATURE SHELF] IS FULL. ABOUT TO REBALANCE EXISTING [ANY_TEMPERATURE_SHELF] FOR  COLD ORDER {}",
					order);
			reOrderAnyTempShelf(order);
		}
	}

	private void addHotOrder(Order order) {

		if (hotShelf.size() < HOT_SHELF_SIZE) {
			hotShelf.add(order);
			log.info("[HOT SHELF] ADDED order {}, HOT_SHELF_SIZE:[{}]", order, hotShelf.size());
		}

		else if (anyTemperatureShelf.size() < ANY_TEMP_SHELF_SIZE) {
			anyTemperatureShelf.add(order);
			log.info("[HOT SHELF] IS FULL. [ANY TEMPERATURE SHELF] ADDED order {}", order);
		} else {
			log.info(
					"BOTH [HOT SHELF] [ANY TEMPERATURE SHELF] IS FULL. ABOUT TO REBALANCE EXISTING [ANY_TEMPERATURE_SHELF] FOR HOT ORDER {}",
					order);
			reOrderAnyTempShelf(order);
		}
	}

}
