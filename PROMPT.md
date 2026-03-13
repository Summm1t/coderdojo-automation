I want to make a Spring CLI application. It is made in a hexagonal architecture, just
like https://github.com/SvenWoltmann/hexagonal-architecture-java/tree/with-spring-boot , and will
only use one maven project (no submodules).
I want a choice of multiple commands.
The first one is a command to create an event by copying an existing one (
see https://www.eventbrite.com/platform/docs/create-events). You specify the command (="copy-event")
date of the event to copy ("source-event" argument, which can be "latest" to copy the latest (past)
event), the new event date ("date" argument), and new event title ("title" argument) to the CLI. In
the result, the URL to the new event needs to be shown.
Also, I need a "debug" mode for this (and future) commands. This will show all details about what
will be done, but doesn't modify anything in Eventbrite (so it's read-only; we don't create a new
event, but show how the new event would look like in the stdout).
Also, I need to have unit and integration tests.