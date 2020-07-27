package example.orders.question.courier.event;

import example.orders.question.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderEvent {

	
	public Order order;
	
	public boolean isHotShelfOrder;
	
	public boolean isColdShelfOrder;
	
	public boolean isFreezeShelfOrder;
	
	public boolean isReadyToBeDelivered;
}
