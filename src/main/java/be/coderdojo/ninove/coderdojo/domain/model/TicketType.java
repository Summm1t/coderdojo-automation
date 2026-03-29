package be.coderdojo.ninove.coderdojo.domain.model;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum TicketType {
    KIND_VAN_VRIJWILLIGER("Kind van vrijwilliger"),
    VRIJWILLIGER("Vrijwilliger"),
    DEELNEMER("Deelnemer"),
    MET_UITNODIGING("Met uitnodiging"),
    UNKNOWN("Unknown");

    private final String description;

    TicketType(String description) {
        this.description = description;
    }

    public static TicketType fromDescription(String description) {
        return Arrays.stream(TicketType.values())
                .filter(type -> type.description.equalsIgnoreCase(description))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
