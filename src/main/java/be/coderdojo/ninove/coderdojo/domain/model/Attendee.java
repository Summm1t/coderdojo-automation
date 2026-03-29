package be.coderdojo.ninove.coderdojo.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Attendee {
    private String id;
    private String firstName;
    private String lastName;
    @EqualsAndHashCode.Include
    private String email;
    private boolean optIn;
    private TicketType ticketType;
}
