# CoderDojo Workflow Automation

This Spring Boot application provides a command-line interface (CLI) to automate CoderDojo
workflows, specifically for managing Eventbrite events.

## Prerequisites

* Java 17 or higher
* Maven

## Configuration

The application requires Eventbrite API credentials. You can provide them by creating a `.env` file
in the project root or by setting environment variables.

### Environment Variables / .env file

```env
EVENTBRITE_API_TOKEN=your_eventbrite_api_token
EVENTBRITE_ORG_ID=your_eventbrite_organization_id
```

The application is configured to automatically import an optional `.env` file if it exists.

## Getting Started

### Build the application

To build the application and run tests, use:

```bash
./mvnw clean install
```

### Run the application

You can run the application in interactive mode:

```bash
java -jar target/coderdojo-0.0.1-SNAPSHOT.jar
```

Once in the shell, you can list all available commands with `help`.

Alternatively, run a single command and exit:

```bash
java -jar target/coderdojo-0.0.1-SNAPSHOT.jar copy-event --source-event latest --date "21/04/2026"
```

## Available Commands

### `copy-event`

Copies an existing event to a new date.

**Usage:**
`copy-event --source-event <date|latest> --date <new-date> [--place <place>] [--debug <true|false>]`

**Parameters:**

* `--source-event`: Date of the event to copy (format: `dd/MM/yyyy`) or the string `latest` to pick
  the most recent one.
* `--date`: The date for the new event (format: `dd/MM/yyyy`).
* `--place`: (Optional) The location for the new event.
* `--debug`: (Optional, default: `false`) If set to `true`, the application will show the details of
  the event it would create without actually making any changes on Eventbrite.

## Development

The project uses:

* **Spring Boot 3.x**
* **Spring Shell** for the CLI interface.
* **Lombok** to reduce boilerplate code.
* **Maven** for dependency management.

For more technical details on the Spring Boot configuration, refer to [HELP.md](HELP.md).
