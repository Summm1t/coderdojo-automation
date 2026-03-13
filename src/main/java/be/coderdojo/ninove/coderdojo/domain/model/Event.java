package be.coderdojo.ninove.coderdojo.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.ZonedDateTime;

@Getter
@Builder
public class Event {
    private String id;
    private String name;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private String url;
}
