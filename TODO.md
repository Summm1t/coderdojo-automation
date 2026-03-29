# TODO

## General
* Execute all commands in one command: `next-event`

## Eventbrite

* Publish event:
    * Add CLI argument for publishing event: `publish`, optional and defaults to False
    * Publish event using Eventbrite API:
      https://www.eventbrite.com/platform/api#/reference/event/publish/publish-an-event

## Eventbrite - Mailerlite integration

~~* Transfer new attendees to Mailerlite:
    * Add CLI argument for Mailerlite integration: `transfer-attendees` CLI command, with optional
      `event` argument, which takes a date for an event (in format dd/MM/yyyy), or `latest`. Default
      value for `event` is `latest`.
    * Get the event in Eventbrite according to the `event` argument
    * Get the attendees for that event in Eventbrite. Don't use the attendee list endpoint, but use
      the "retrieve attendee
      report" (https://www.eventbrite.be/creator/reporting/api/reporting/attendees).
    * You will 
    * Add the attendees to Mailerlite Subscribers:
        * When the attendee already exists in Mailerlite (based on email address):
            * If the attendee does not want to receive emails: check if they have opted out of
              emails in Mailerlite. If not, opt out in Mailerlite.
            * If there is more information in the Eventbrite attendee: add extra information to
              Mailerlite subscriber.
            * If there is no more information in the Eventbrite attendee: do nothing.
        * When the attendee does not exist in Mailerlite:
            * Add the attendee to Mailerlite Audience
            * If the attendee does not want to receive emails: opt out in Mailerlite.~~

## Mailerlite

* Unsubscribe contacts: also archive them



