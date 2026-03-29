package be.coderdojo.ninove.coderdojo.domain.model;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Campaign {
    private String id;
    private String title;
    private String content;
    private String status;
    private String fromName;
    private String fromEmail;
    private String subject;
    private List<String> groups;
    private List<String> segments;
    private String languageId;
    private Map<String, Object> settings;
}
