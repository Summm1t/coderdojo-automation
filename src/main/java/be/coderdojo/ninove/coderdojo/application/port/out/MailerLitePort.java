package be.coderdojo.ninove.coderdojo.application.port.out;

import be.coderdojo.ninove.coderdojo.domain.model.Campaign;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MailerLitePort {
    Optional<Campaign> findLatestCampaign();
    Optional<Campaign> findCampaignByTitle(String title);
    Optional<Campaign> getCampaignDetails(String campaignId);
    Campaign createCampaign(String title, String content, String fromName, String fromEmail, String subject,
                            List<String> groups, List<String> segments, String languageId, Map<String, Object> settings, boolean debug);
}
