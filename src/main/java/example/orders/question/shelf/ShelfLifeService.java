package example.orders.question.shelf;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import example.orders.question.model.Order;

@Service
public class ShelfLifeService implements ShelfConstants{

	
	public double getOrderValue(boolean isAnyTempShelf, Order order) {
		
		LocalDateTime now = LocalDateTime.now();
		
		long orderAge = order.getOrderReceivedTime().until(now, ChronoUnit.SECONDS);
		
		double decayRate = order.getDecayRate();
		
		int shelfLife = order.getShelfLife();
		
		int shelfDecayModifier = isAnyTempShelf?2:1;
		
		double val = (shelfLife - decayRate * orderAge * shelfDecayModifier)/shelfDecayModifier;
		
		return val;
	}

	public boolean isShelfLifeValid(boolean isAnyTempShelf, Order order) {
		return getOrderValue(isAnyTempShelf, order) > EPSILON;
	}
}
