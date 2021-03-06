package example.orders.question.courier;

import java.time.LocalDateTime;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import example.orders.question.courier.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HotOrderCourierServiceListener implements CourierServiceListener {

	@EventListener(condition = "#event.isReadyToBeDelivered and #event.isHotShelfOrder")
	public void onReceivingOrder(OrderEvent event) {

		log.info("[HOT-ORDER-DELIVERED] DELIVERY TIME {} - {}", LocalDateTime.now(), event.getOrder());

	}
}
