package example.orders.question.courier;

import example.orders.question.courier.event.OrderEvent;

public interface CourierServiceListener {

	public void onReceivingOrder(OrderEvent event);
}
