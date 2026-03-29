package be.coderdojo.ninove.coderdojo.application.port.in;

public interface CopyMailingUseCase {
    String copyMailing(String originalCampaignTitle, String newDate, String eventbriteLink, boolean debug);
}
