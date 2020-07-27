package example.orders.question.courier.event;

import example.orders.question.model.Order;

public class OnFreezeOrderReadyEvent extends OrderEvent {

	public OnFreezeOrderReadyEvent(Order order, boolean isReadyToBeDelivered) {
		
		this.order = order;
		
		this.isHotShelfOrder = false;
		
		this.isColdShelfOrder = false;
		
		this.isFreezeShelfOrder = true;
		
		this.isReadyToBeDelivered = isReadyToBeDelivered;
	}
}
