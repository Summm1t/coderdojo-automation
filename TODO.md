# TODO

## Eventbrite

* Publish event:
    * Add CLI argument for publishing event: `publish`, optional and defaults to False
    * Publish event using Eventbrite API:
      https://www.eventbrite.com/platform/api#/reference/event/publish/publish-an-event

## Eventbrite - Mailerlite integration

* Transfer new attendees to Mailerlite:
    * Add CLI argument for Mailerlite integration: `mailerlite` CLI command, with optional `event`
      argument, which takes a date for an event (in format dd/MM/yyyy), or `latest`. Default value
      for `event` is `latest`.
    * Get the event in Eventbrite according to the `event` argument
    * Get the attendees for that event in Eventbrite
    * Add the attendees to Mailerlite Subscribers:
        * When the attendee already exists in Mailerlite (based on email address):
            * If the attendee does not want to receive emails: check if they have opted out of
              emails in Mailerlite. If not, opt out in Mailerlite.
            * If there is more information in the Eventbrite attendee: add extra information to
              Mailerlite subscriber.
            * If there is no more information in the Eventbrite attendee: do nothing.
        * When the attendee does not exist in Mailerlite:
            * Add the attendee to Mailerlite Audience
            * If the attendee does not want to receive emails: opt out in Mailerlite.

* Create new campaign:
    * Add CLI command to copy an existing campaign: `campaign` CLI command, with optional
      `original-campaign-title` argument, which takes the title of an existing campaign, or
      `latest`. Default value for `original-campaign-title` is `latest`.
    * Add `date` argument, which takes the date of the new campaign (in format dd/MM/yyyy). This
      argument is required.
    * Add `link` argument, which takes the Eventbrite registration link for the new campaign. This
      argument is required.
    * Get the latest campaign, or the campaign with title `original-campaign-title` from Mailerlite
    * Make a copy of the campaign, and update the title, content and registration link:
        * Take the title of the original campaign (for example "Coderdojo Ninove Nieuwsbrief april
          2026"), and replace the date with the new date (e.g. "Coderdojo Ninove Nieuwsbrief april
          2026" becomes "Coderdojo Ninove Nieuwsbrief mei 2026")
        * Update the Eventbrite registration link in the content with the new link provided as CLI
          argument. An Eventbrite registration link is in the format
          `https://www.eventbrite.com/e/.*`

## Mailerlite

* Unsubscribe contacts: also archive them

