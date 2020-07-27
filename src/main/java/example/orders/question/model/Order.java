package example.orders.question.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)

/*"id": "2ec069e3-576f-48eb-869f-74a540ef840c",
"name": "Acai Bowl",
"temp": "cold",
"shelfLife": 249,
"decayRate": 0.3*/

public class Order {

	private String id;
	
	private String name;
	
	private Temperature temp;
	
	private int shelfLife;
	
	private double decayRate;
	
	private LocalDateTime orderReceivedTime;
	
	
}
