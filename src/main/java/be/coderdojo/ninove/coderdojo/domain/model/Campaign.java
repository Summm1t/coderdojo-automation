package be.coderdojo.ninove.coderdojo.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Campaign {
    private String id;
    private String title;
    private String content;
    private String status;
}
