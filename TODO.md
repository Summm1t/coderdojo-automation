# TODO

## Eventbrite

* Number of participants: set capacity and quantity_total for each ticket class
    * Add CLI arguments for each class: `deelnemers`, `vrijwilligers`, `kind_van_vrijwilliger` and
      `met_uitnodiging`, all optional and defaults to deelnemers=20, vrijwilligers=15,
      kind_van_vrijwilliger=10, met_uitnodiging=5
    * Get all ticket classes for the event:
      https://www.eventbrite.com/platform/api#/reference/ticket-class/list/list-ticket-classes-by-event
    * Then, update the ticket classes with the new `capacity` and `quantity_total` with:
      https://www.eventbrite.com/platform/api#/reference/ticket-class/update/update-a-ticket-class

* Publish event:
    * Add CLI argument for publishing event: `publish`, optional and defaults to False
    * Publish event using Eventbrite API:
      https://www.eventbrite.com/platform/api#/reference/event/publish/publish-an-event

## ~~Eventbrite - Mailchimp integration~~

* ~~Transfer new attendees to Mailchimp:~~
    * ~~Add CLI argument for Mailchimp integration: `mailchimp` CLI command, with optional `event`
      argument, which takes a date for an event (in format dd/MM/yyyy), or `latest`. Default value
      for `event` is `latest`.~~
    * ~~Get the event in Eventbrite according to the `event` argument~~
    * ~~Get the attendees for that event in Eventbrite~~
    * ~~Add the attendees to Mailchimp Audience~~
        * ~~When the attendee already exists in Mailchimp (based on email address):~~
            * ~~If the attendee does not want to receive emails: check if they have opted out of
              emails in Mailchimp. If not, opt out in Mailchimp.~~
            * ~~If there is more information in the Eventbrite attendee: add extra information to
              Mailchimp~~
            * ~~If there is no more information in the Eventbrite attendee: do nothing~~
        * ~~When the attendee does not exist in Mailchimp:~~
            * ~~Add the attendee to Mailchimp Audience~~
            * ~~If the attendee does not want to receive emails: opt out in Mailchimp.~~

* ~~Create new mailing:~~
    * ~~Add CLI argument for mailing creation: `mailing` CLI command, with optional `event`~~

## ~~Mailchimp~~
* ~~Unsubscribe contacts: also archive them~~


