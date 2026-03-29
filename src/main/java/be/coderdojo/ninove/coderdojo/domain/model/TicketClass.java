package be.coderdojo.ninove.coderdojo.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TicketClass {
    private String id;
    private String name;
    private Integer capacity;
    private Integer quantityTotal;
}
