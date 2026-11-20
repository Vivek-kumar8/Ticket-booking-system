# Ticket Booking System

A Java command-line train ticket booking system built with Gradle.

## What it does

- Train search
- Seat selection
- Booking tickets
- Ticket retrieval
- Cancellation

## Tech stack

- Core Java (OOP, Collections, exception handling)
- Gradle (application plugin, Java toolchain 23)
- JSON-based file persistence (Jackson)
- Lombok, Guava, jBCrypt
- JUnit 4 for tests

The code is organised in a service-based architecture. The entry point is `ticket.booking.App` in `app/src`.

## Project structure

```
.
├── app/
│   ├── build.gradle
│   └── src/
├── gradle/
├── gradlew
├── gradlew.bat
└── settings.gradle
```

## How to run

Prerequisite: JDK 23.

```bash
./gradlew :app:run
```

On Windows:

```bash
gradlew.bat :app:run
```

To run the tests:

```bash
./gradlew test
```

## Author

Vivek Kumar — [GitHub](https://github.com/Vivek-kumar8) · [Portfolio](https://vivek-kumar8.github.io/my-portfolio)
