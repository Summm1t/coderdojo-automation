package be.coderdojo.ninove.coderdojo.domain.model;

import java.util.regex.Pattern;

public interface Constants {

  public static final String LATEST = "latest";

  public static final String MAILING_TITLE_PREFIX = "Coderdojo Ninove Nieuwsbrief";
  public static final String MAILING_TITLE_REGEX = MAILING_TITLE_PREFIX + " (\\p{L}+) (\\d{4})";
  public static final Pattern MAILING_TITLE_PATTERN = Pattern.compile(MAILING_TITLE_REGEX);

}
