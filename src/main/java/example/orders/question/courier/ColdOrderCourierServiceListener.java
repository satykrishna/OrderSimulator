package example.orders.question.courier;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import example.orders.question.courier.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ColdOrderCourierServiceListener implements CourierServiceListener {

	@EventListener(condition = "#event.isReadyToBeDelivered && #event.isColdShelfOrder")
	public void onReceivingOrder(OrderEvent event) {

		log.info("Received The Order.. Prepare For Delivery  {}", event.getOrder());

		log.info("Delivered The Order {}", event.getOrder());
	}

}
