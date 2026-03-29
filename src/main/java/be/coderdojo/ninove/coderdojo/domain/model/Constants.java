package be.coderdojo.ninove.coderdojo.domain.model;

import java.time.ZoneId;
import java.util.regex.Pattern;

public interface Constants {
  String LATEST = "latest";
  String INPUT_DATE_FORMAT = "dd/MM/yyyy";
  String MAILING_TITLE_PREFIX = "Coderdojo Ninove Nieuwsbrief";
  String MAILING_TITLE_REGEX = MAILING_TITLE_PREFIX + " (\\p{L}+) (\\d{4})";
  Pattern MAILING_TITLE_PATTERN = Pattern.compile(MAILING_TITLE_REGEX);
  ZoneId BRUSSELS_ZONE = ZoneId.of("Europe/Brussels");

}
