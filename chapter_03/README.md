# Chapter 03 - Intro to preparing a Spring Boot Application for Docker, and alignment to the 12-factor application principles

## Introduction to the Project

This demonstration project will do simple conversions. In this first iteration, we will convert between degrees celsius and degrees fahrenheit (and vice versa) through a REST API.

This will be implemented as a Spring Boot application and containerized as a Docker image.

## Functional Features

This project will not require any external data sources or anything special at this point - it's a standalone service that is used to compute temperature conversions between degrees celsius and degrees fahrenheit (and vice versa).

The end-points will be exposed as REST endpoints with two functional paths:

* `/convert/c-to-f/{degrees}` - for converting degrees celsius and degrees fahrenheit
* `/convert/f-to-c/{degrees}` - for converting degrees fahrenheit and degrees celsius

Both end-points support only the `GET` method and the response will be a JSON object with the following structure:

```json
{
    "inputDegreesUnit": "string",
    "inputDegrees": 123,
    "convertedDegrees": 123,
    "convertedDegreesUnit": "string"
}
```

| Field                  | Example      | Description |
|------------------------|--------------|-------------|
| `inputDegreesUnit`     | `celsius`    | Assumed input unit. For the `/convert/c-to-f` endpoint this will be `celsius` and for `/convert/f-to-c` it will be `fahrenheit`. |
| `inputDegrees`         | `15.0`       | The actual number that was passed as input. The input value will be converted to a `double`.                                     |
| `convertedDegrees`     | `59.0`       | The calculated target value (the result), returned as a `double`.                                                                |
| `convertedDegreesUnit` | `fahrenheit` | The target unit. For the `/convert/c-to-f` endpoint this will be `fahrenheit` and for `/convert/f-to-c` it will be `celsius`.    |

Further reading:

* [REST with Spring Tutorial](https://www.baeldung.com/rest-with-spring-series)
* [Spring boot cheat sheet](https://www.javagists.com/spring-boot-cheatsheet)

## None-functional Requirements (12-factor Application)

| Non-Functional Area | Requirement                                                                                                                                                                                             |
|---------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Codebase            | The project will be hosted in GitHub as part of this guide repository. The project path will be `chapter_03/project_source_code`.                                                                       |
| Dependencies        | All dependencies will be defined using the Maven `pom.xml` file.                                                                                                                                        |
| Configuration       | For this project, the exposed PORT can be modified via an environment variable called `SERVER_PORT`.                                                                                                    |
| Backing services    | No backing services are required for this project                                                                                                                                                       |
| Build, release, run | This project will not yet be part of a build pipeline. This will be completed in a future chapter                                                                                                       |
| Processes           | The project will be packaged and deployed as a stateless container image. Multiple instances can be run without any issues.                                                                             |
| Port binding        | The default port will be TCP port 8080. No TLS is required yet. The port will be exposed via the container service.                                                                                     |
| Concurrency         | The service can be scaled horizontally by starting as many instances as required. No vertical scaling should be required.                                                                               |
| Disposability       | The service will react to `SIGTERM` by setting the replies to the readiness probe to false. Other endpoints will return HTTP 503. A singleton class will be used to check state within the application. |
| Dev/Prod Parity     | The application can run unchanged regardless of environment it runs in.                                                                                                                                 |
| Logs                | All logs will be pushed to STDOUT. From a Docker perspective, the logs will be accessible via the `docker logs` command.                                                                                |
| Admin processes     | No applicable for this project.                                                                                                                                                                         |

Further reading:

* [Twelve-Factor Methodology in a Spring Boot Microservice](https://www.baeldung.com/spring-boot-12-factor)
* Information about signal handling and shutting down gracefully:
  * [Quick Guide to Spring Bean Scopes](https://www.baeldung.com/spring-bean-scopes)
  * [Docker, Java, Signals and Pid 1](https://blog.no42.org/code/docker-java-signals-pid1/)
  * [Stack Overflow discussion on handling `SIGTERM` in Java in Docker](https://stackoverflow.com/questions/31836498/sigterm-not-received-by-java-process-using-docker-stop-and-the-official-java-i)
  * [Spring PostConstruct and PreDestroy Annotations](https://www.baeldung.com/spring-postconstruct-predestroy)
  * [Spring Shutdown Callbacks](https://www.baeldung.com/spring-shutdown-callbacks)
  * [Shutdown a Spring Boot Application](https://www.baeldung.com/spring-boot-shutdown)
* [How to Change the Default Port in Spring Boot](https://www.baeldung.com/spring-boot-change-port)
* [Docker Runtime options with Memory, CPUs, and GPUs](https://docs.docker.com/config/containers/resource_constraints/)
* [Externalized Configuration](https://docs.spring.io/spring-boot/docs/1.5.6.RELEASE/reference/html/boot-features-external-config.html)

## Bootstrapping the Project

Using the [Spring Inializr](https://start.spring.io/), the project was bootstrapped with the following options:

* Project: Maven
* Language: Java
* Spring Boot: 2.5.2
* Dependencies:
  * Spring Web
  * Lombok
  * Spring Boot Actuator
  * Prometheus
* Packaging: JAR
* Java Version: 16
* Project Meta Data:
  * Group: com.example
  * Artifact: conversions
  * Name: Conversions Demo
  * Description: REST application to do various conversions.
  * Package Name: com.example.conversions

A ZIP version of the naked bootstrapped project is [available in the file `conversions.zip_archive`](conversions.zip_archive)


