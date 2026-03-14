# CoderDojo Workflow Automation

This Spring Boot application provides a command-line interface (CLI) to automate CoderDojo
workflows, specifically for managing Eventbrite events.

## Prerequisites

* Java 21 or higher
* Maven

## Configuration

The application requires Eventbrite and Mailchimp API credentials. You can provide them by creating
a `.env` file
in the project root or by setting environment variables.

### Environment Variables / .env file

```env
EVENTBRITE_API_TOKEN=your_eventbrite_api_token
EVENTBRITE_ORG_ID=your_eventbrite_organization_id
MAILCHIMP_API_KEY=your_mailchimp_api_key
MAILCHIMP_LIST_ID=your_mailchimp_list_id
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

### `unsubscribe-mailchimp`

Tag and unsubscribe a list of comma-separated email addresses from Mailchimp.

**Usage:**
`unsubscribe-mailchimp <emails> [--debug <true|false>]`

**Parameters:**

* `<emails>`: Comma-separated list of email addresses.
* `--debug`: (Optional, default: `false`) If set to `true`, the application will log the API URI and
  request body instead of calling the Mailchimp API.

## Development

The project uses:

* **Java 21**
* **Spring Boot 4.0.3**
* **Spring Shell 3.4.0**
* **Lombok** to reduce boilerplate code.
* **Maven** for dependency management.

## CI/CD Workflow

The project uses GitHub Actions for continuous integration and delivery.

### Automated Builds

Every push or pull request to the `main` branch triggers an automated build and test suite to ensure
code quality.

### Creating a New Release

The release process is automated and follows these steps:

1. **Create a GitHub Release:**
    - Go to the "Releases" section of the repository.
    - Click "Draft a new release".
    - Create a new tag (e.g., `v1.2.3`). **Note:** The workflow uses Semantic Versioning (SemVer).
    - Give the release a title and description.
    - Click "Publish release".

2. **Automated Pipeline:**
   Once a release is published, a GitHub Action is triggered that:
    - Extracts the version from the tag (e.g., `v1.2.3` becomes `1.2.3`).
    - Updates the version in `pom.xml` and commits it back to the `main` branch with `[skip ci]`.
    - Builds a Docker image using a multi-stage build.
    - Pushes the Docker image to the GitHub Container Registry (GHCR) with tags for the full
      version, major.minor, and major version.
    - Increments the version to the next SNAPSHOT (e.g., `1.3.0-SNAPSHOT`) and pushes the change
      back to the `main` branch.

### Docker Image

The Docker image is available at `ghcr.io/${{ github.repository }}`.
It uses `eclipse-temurin:21-jre-alpine` as the runtime base for a lightweight and secure image.
