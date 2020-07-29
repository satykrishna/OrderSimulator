package example.orders.question.courier;

import java.time.LocalDateTime;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import example.orders.question.courier.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ColdOrderCourierServiceListener implements CourierServiceListener {

	@EventListener(condition = "#event.isReadyToBeDelivered && #event.isColdShelfOrder")
	public void onReceivingOrder(OrderEvent event) {

		log.info("[COLD-ORDER-DELIVERED] DELIVERY TIME {} - {}", LocalDateTime.now(), event.getOrder());
	}

}
