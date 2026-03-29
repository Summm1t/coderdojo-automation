package be.coderdojo.ninove.coderdojo.application.port.in;

public interface CopyMailingUseCase {
    String copyMailing(String titleArgument, String newDateArgument, String eventBriteLinkArgument, boolean debug);
}
