
Notification Service
====================

This project implements a notification service for cryptocurrency price alerts using a clean hexagonal architecture. The application monitors crypto price updates and dispatches notifications when configured thresholds are met. Ktor is used as a web server and KMongo for interacting with a MongoDB database.

Table of Contents
-----------------

-   [Features](#features)
-   [Architecture](#architecture)
-   [Getting Started](#getting-started)
-   [Building the Project](#building-the-project)
-   [Running the Application](#running-the-application)
-   [Testing](#testing)
-   [Docker](#docker)
-   [CI/CD](#cicd)
    -   [Automatic Releases](#automatic-releases)
    -   [CI/CD for Docker](#cicd-for-docker)
    -   [Code Quality and Testing](#code-quality-and-testing)
-   [Code Quality](#code-quality)
    -   [ktlint](#ktlint)
    -   [Commitlint](#commitlint)
-   [License](#license)

Features
--------

-   **Alert Management**: Create, update, and delete price alerts.
-   **Price Updates**: Process real-time cryptocurrency price updates coming from Event Dispatcher.
-   **Notifications**: Dispatch notifications to Event Dispatcher when price thresholds are met.
-   **JWT Authentication**: Secure sensible endpoints using JWT tokens for authentication.
-   **Clean Architecture**: Domain, Application, and Infrastructure layers are clearly separated.
-   **Unit & Integration Tests**: Tests provided with JUnit5 and Konsist for architectural validations.

Architecture
------------

The project follows a hexagonal architecture:

-   **Domain Layer**: Contains business models such as `it.unibo.domain.PriceAlert` and `it.unibo.domain.Currency`.
-   **Application Layer**: Implements business rules and notifications in `it.unibo.application.NotificationServiceImpl`.
-   **Infrastructure Adapter**: Adapters for external systems like MongoDB (`it.unibo.infrastructure.adapter.MongoPriceAlertRepository`), HTTP routing (`it.unibo.infrastructure.adapter.WebServer`), and JWT authentication (`it.unibo.infrastructure.adapter.AuthAdapter`).

Getting Started
---------------

### Clone the repository


``` git clone https://github.com/CryptoMonitorASW-SPE/notification.git ```

```cd notification```


Building the Project
--------------------

Use the Gradle wrapper to build the project. The build configuration is defined in `build.gradle.kts`.

Run the following command:
```
./gradlew build
```

This will compile the code, run tests, and generate the fat JAR in the build output.

Running the Application
-----------------------

The application starts a Ktor server. The main entry point is `Main.kt`.

Run the application with:

```./gradlew run```

Or execute the generated JAR:

```java -jar build/libs/notification-service.jar```

The server listens on port 8080 by default. You can access the following endpoints:

-   **POST `/data`**: Submit cryptocurrency price updates.
-   **POST `/createAlert`**: Create a new alert (JWT authentication required).
-   **GET `/alerts`**: Get alerts for a user (JWT authentication required).
-   **PUT `/active`**: Update alert status (JWT authentication required).
-   **DELETE `/alerts`**: Delete an alert.

Testing
-------

The project includes unit and integration tests with JUnit5.

To run tests:

`./gradlew test`

Test files can be found in:

-   `NotificationServiceTest.kt`
-   `AuthenticationTest.kt`
-   `DependencyRuleTest.kt`

Docker
------

A Dockerfile is provided to build a Docker image of the application.

To build and run the Docker container:

`docker build -t notification .\`
`docker run -p 8080:8080 notification`


CI/CD
-----

### Automatic Releases

When a push is made to the main branch, an automatic release process is triggered. If a new release is detected, a new Docker package is built and published to [GitHub Container Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry).


### CI/CD for Docker

The CI/CD pipeline ensures that every release includes an updated Docker image. When a new release is created:

-   A Docker image is built.

-   The image is pushed to GHCR.

-   The latest tag is updated.

### Code Quality and Testing

For each pull request, the following checks are performed:

-   **Code Quality and Smell Analysis**: The code is analyzed using [Detekt](https://detekt.dev/) to ensure it adheres to Kotlin best practices and to check for code smells.

-   **Test Validation**: The test suite is executed to verify that all tests pass before merging.

### KtLint

The project uses [KtLint](https://github.com/pinterest/ktlint) for Kotlin code style enforcement.\
It is integrated into the commit process using Husky, meaning you cannot commit code that violates style rules.

### Commitlint

[Commitlint](https://commitlint.js.org/) is configured to enforce commit message conventions.\
It ensures that all commits follow a standardized format, improving readability and maintainability.

License
-------

This project is licensed under the MIT License.
