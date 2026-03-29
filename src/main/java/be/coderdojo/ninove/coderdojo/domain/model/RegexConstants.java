package be.coderdojo.ninove.coderdojo.domain.model;

public interface RegexConstants {

  /**
   * Regular expressions to map Eventbrite answers to Attendee fields.
   */
  String VOORNAAM = "Voornaam \\(ouder\\/voogd\\)";
  String ACHTERNAAM = "Achternaam \\(ouder\\/voogd\\)";
  String OPTIN_QUESTION = ".*Mogen we jou via mail op de hoogte brengen over volgende CoderDojo.*";
  String OPTIN_ANSWER_TRUE = "Je mag mij contacteren.*";

  /**
   * Regular expressions to find text in the mailing campaign (MailerLite) title and content.
   */
  String DAY = "(maandag|dinsdag|woensdag|donderdag|vrijdag|zaterdag|zondag)";
  String MONTH = "(januari|februari|maart|april|mei|juni|juli|augustus|september|oktober|november|december)";
  // This regex tries to find "Month YYYY" at the end of the title
  // In Dutch: januari, februari, maart, april, mei, juni, juli, augustus, september, oktober, november, december
  String MONTH_AND_YEAR = MONTH + "\\s+\\d{4}";
  // Sentence pattern: "De volgende Coderdojo gaat door op [date] in de ..."
  // date pattern: "zondag 29 maart 2026"
  // In Dutch: [dagnaam] [dag] [maand] [jaar]
  String NEXT_EVENT_DATE = DAY + "\\s+\\d{1,2}\\s+" + MONTH + "\\s+\\d{4}";
  String NEXT_EVENT_DATE_PREFIX = "volgende Coderdojo gaat door op";
  String NEXT_EVENT_DATE_SENTENCE = NEXT_EVENT_DATE_PREFIX + "\\s+" + NEXT_EVENT_DATE;
  String EVENTBRITE_LINK = "https://www\\.eventbrite\\.[a-z.]+/e/[\\w-]+";

}
