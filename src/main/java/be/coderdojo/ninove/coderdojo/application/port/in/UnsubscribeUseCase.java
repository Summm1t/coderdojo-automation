package be.coderdojo.ninove.coderdojo.application.port.in;

import java.util.List;

public interface UnsubscribeUseCase {
    void unsubscribe(List<String> emails, boolean debug);
}
