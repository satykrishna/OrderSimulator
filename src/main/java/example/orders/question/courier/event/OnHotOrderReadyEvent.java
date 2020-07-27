package example.orders.question.courier.event;

import example.orders.question.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class OnHotOrderReadyEvent extends OrderEvent {

	public OnHotOrderReadyEvent(Order order, boolean isReadyToBeDelivered) {
		
		this.order = order;
		
		this.isHotShelfOrder = true;
		
		this.isColdShelfOrder = false;
		
		this.isFreezeShelfOrder = false;
		
		this.isReadyToBeDelivered = isReadyToBeDelivered;
	}
	
	
}
