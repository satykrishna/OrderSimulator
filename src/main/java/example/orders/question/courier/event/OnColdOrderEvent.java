package example.orders.question.courier.event;

import example.orders.question.model.Order;

public class OnColdOrderEvent extends OrderEvent {

	public OnColdOrderEvent(Order order, boolean isReadyToBeDelivered) {

		this.order = order;

		this.isHotShelfOrder = false;

		this.isColdShelfOrder = true;

		this.isFreezeShelfOrder = false;
		
		this.isReadyToBeDelivered = isReadyToBeDelivered;
	}
}
