# CoderDojo Workflow Automation

This Spring Boot application provides a command-line interface (CLI) to automate CoderDojo
workflows, specifically for managing Eventbrite events and MailerLite campaigns.

## Prerequisites

* Java 21 or higher
* Maven

## Configuration

The application requires Eventbrite and MailerLite API credentials. You can provide them by
creating a `.env` file in the project root or by setting environment variables.

### Environment Variables / .env file

```env
EVENTBRITE_API_TOKEN=your_eventbrite_api_token
EVENTBRITE_ORG_ID=your_eventbrite_organization_id
MAILERLITE_API_TOKEN=your_mailerlite_api_token
```

The application is configured to automatically import an optional `.env` file if it exists.

## Getting Started

### Run the application with Docker

You can run the application using Docker, either by building it locally or by pulling the pre-built
image from GitHub Container Registry (GHCR).

#### Prerequisites for Docker

* **Docker** installed on your machine.
* A `.env` file with your Eventbrite credentials in the current directory (
  see [Configuration](#configuration)).

#### Build and run locally

1. **Build the Docker image:**

   ```bash
   docker build -t coderdojo-automation .
   ```

2. **Run the image in interactive mode:**

   ```bash
   docker run -it --env-file .env coderdojo-automation
   ```

3. **Run a single command:**

   ```bash
   docker run -it --env-file .env coderdojo-automation copy-event --source-event latest --date "21/04/2026"
   ```

#### Run using GHCR image

The image is published to GHCR for every release. Replace `<version>` with the desired version (
e.g., `1.0.0`).

```bash
docker run -it --env-file .env ghcr.io/summm1t/coderdojo-automation:<version> \
  copy-event --source-event 'latest' --date '01/06/2026'
```

Alternatively, use the `latest` tag for the most recent version:

```bash
docker run -it --env-file .env ghcr.io/summm1t/coderdojo-automation:latest \
  copy-event --source-event 'latest' --date '01/06/2026'
```

### Build the application

To build the application and run tests, use:

```bash
./mvnw clean install
```

### Run the application

You can run the application in interactive mode:

```bash
java -jar target/coderdojo-workflow-automation.jar
```

Once in the shell, you can list all available commands with `help`.

Alternatively, run a single command and exit:

```bash
java -jar target/coderdojo-workflow-automation.jar copy-event --source-event latest --date '21/04/2026'
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

### `set-participants`

Sets the capacity and quantity_total for each ticket class for a specific event.

**Usage:**
`set-participants [--event <date|latest>] [--deelnemers <count>] [--vrijwilligers <count>] [--kind-van-vrijwilliger <count>] [--met-uitnodiging <count>] [--debug <true|false>]`

**Parameters:**

* `--event`: (Optional, default: `latest`) Date of the event (format: `dd/MM/yyyy`) or the string
  `latest` to pick the most recent one.
* `--deelnemers`: (Optional, default: `20`) Capacity for the "Deelnemers" ticket class.
* `--vrijwilligers`: (Optional, default: `15`) Capacity for the "Vrijwilligers" ticket class.
* `--kind-van-vrijwilliger`: (Optional, default: `10`) Capacity for the "Kind van vrijwilliger" ticket
  class.
* `--met-uitnodiging`: (Optional, default: `5`) Capacity for the "Met uitnodiging" ticket class.
* `--debug`: (Optional, default: `false`) If set to `true`, the application will show the details of
  the updates it would make without actually making any changes on Eventbrite.

### `copy-mailing`

Copies an existing mailing campaign in MailerLite and updates its title, registration link, and
content based on a new date.

**Usage:**
`copy-mailing [--original-campaign-title <title|latest>] --date <date> --link <eventbrite-link> [--debug <true|false>]`

**Parameters:**

* `--original-campaign-title`: (Optional, default: `latest`) Title of the existing campaign to copy,
  or the string `latest` to pick the most recent sent campaign.
* `--date`: The date of the new campaign (format: `dd/MM/yyyy`). This date is used to update the
  campaign title (e.g., replacing "april 2026" with "mei 2026") and is formatted in Dutch.
* `--link`: The new Eventbrite registration link (format: `https://www.eventbrite.com/e/...`).
* `--debug`: (Optional, default: `false`) If set to `true`, the application will show the details of
  the campaign it would create without actually making any changes in MailerLite.

### `transfer-attendees`

Transfers attendees from Eventbrite to MailerLite.

**Usage:**
`transfer-attendees [--event <date|latest>] [--debug <true|false>]`

**Parameters:**

* `--event`: (Optional, default: `latest`) Date of the event (format: `dd/MM/yyyy`) or the string
  `latest` to pick the most recent past event.
* `--debug`: (Optional, default: `false`) If set to `true`, the application will show the details of
  the transfers it would make without actually making any changes in MailerLite.

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

The Docker image is available at `ghcr.io/summm1t/coderdojo-automation`.
It uses `eclipse-temurin:21-jre` as the runtime base.
