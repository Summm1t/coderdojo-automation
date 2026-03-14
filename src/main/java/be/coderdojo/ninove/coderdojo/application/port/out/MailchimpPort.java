package be.coderdojo.ninove.coderdojo.application.port.out;

import java.util.List;

public interface MailchimpPort {
    boolean tagUser(String email, List<String> tags, boolean debug);
    boolean unsubscribeUser(String email, boolean debug);
}
