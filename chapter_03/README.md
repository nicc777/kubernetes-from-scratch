# Chapter 03 - Intro to preparing a Spring Boot Application for Docker, and alignment to the 12-factor application principles

- [Chapter 03 - Intro to preparing a Spring Boot Application for Docker, and alignment to the 12-factor application principles](#chapter-03---intro-to-preparing-a-spring-boot-application-for-docker-and-alignment-to-the-12-factor-application-principles)
  - [Introduction to the Project](#introduction-to-the-project)
  - [Functional Features](#functional-features)
  - [None-functional Requirements (12-factor Application)](#none-functional-requirements-12-factor-application)
  - [Bootstrapping the Project](#bootstrapping-the-project)
  - [Implementation](#implementation)
  - [Building & Testing](#building--testing)
    - [Building](#building)
    - [API Endpoints](#api-endpoints)
    - [Preparing the Docker Build](#preparing-the-docker-build)
    - [Testing the Docker Image](#testing-the-docker-image)
    - [Prometheus Telemetry](#prometheus-telemetry)

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
| Configuration       | For this project, the exposed PORT can be modified via an environment variable called `SERVER_PORT`. The default port will be set in properties as port 8888. The ready and terminate wait times can also be set with the environment variables `SERVICE_READY_WAITTIME`  and `SERVICE_TERMINATE_WAITTIME` respectively, in milliseconds, to add some wait time to simulate work before ready state or termination. The default value for these variables will be 5 seconds (5000 milliseconds) |
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

Further reading:

* [Spring Boot Tutorial – Bootstrap a Simple Application](https://www.baeldung.com/spring-boot-start)

## Implementation

In addition to the non-functional requirements, we will alo add a OpenAPI 3.0 UI to make testing a little easier through a web interface.

## Building & Testing

### Building

The standard way to build the application can be followed by running the following command, assuming you are in the project root directory:

```shell
cd chapter_03/project_source_code/conversions
./mvnw clean && ./mvnw package
```

The application should now be in `target/conversions-0.0.1-SNAPSHOT.jar`

To start the application with all the defaults set, run the following command:

```shell
java -jar target/conversions-0.0.1-SNAPSHOT.jar
```

In order to pass environmental variables, you can also try something like the following that will simulate a much faster ready state:

```shell
SERVICE_READY_WAITTIME=1 java -jar target/conversions-0.0.1-SNAPSHOT.jar 
```

Another alternative to accomplish the same result:

```shell
java -jar target/conversions-0.0.1-SNAPSHOT.jar --service.ready.waittime=1
```

### API Endpoints

Once the application is in a ready state, you can run the following commands in another terminal:

```shell
curl -s http://127.0.0.1:8888/api/convert/c-to-f/15
```

The expected output should be something like the following:

```json
{
  "inputDegreesUnit":"celsius",
  "inputDegrees":15.0,
  "convertedDegrees":59.0,
  "convertedDegreesUnit":"fahrenheit"
  }
```

Another test (reverse of the above test):

```shell
curl -s http://127.0.0.1:8888/api/convert/f-to-c/59.0
```

The expected output should be something like the following:

```json
{
  "inputDegreesUnit":"fahrenheit",
  "inputDegrees":59.0,
  "convertedDegrees":15.0,
  "convertedDegreesUnit":"celsius"
}
```

### Preparing the Docker Build

TODO

### Testing the Docker Image

TODO

### Prometheus Telemetry

For now, just to test that the application is emitting telemetry, run the following command:

```shell
curl http://127.0.0.1:8888/actuator/prometheus
```

You should get output similar to the following:

```text
# HELP tomcat_sessions_expired_sessions_total
# TYPE tomcat_sessions_expired_sessions_total counter
tomcat_sessions_expired_sessions_total 0.0
# HELP jvm_gc_max_data_size_bytes Max size of long-lived heap memory pool
# TYPE jvm_gc_max_data_size_bytes gauge
jvm_gc_max_data_size_bytes 8.380219392E9
# HELP jvm_memory_committed_bytes The amount of memory in bytes that is committed for the Java virtual machine to use
# TYPE jvm_memory_committed_bytes gauge
jvm_memory_committed_bytes{area="nonheap",id="CodeHeap 'profiled nmethods'",} 9240576.0
jvm_memory_committed_bytes{area="heap",id="G1 Survivor Space",} 4194304.0
jvm_memory_committed_bytes{area="heap",id="G1 Old Gen",} 3.3554432E7
jvm_memory_committed_bytes{area="nonheap",id="Metaspace",} 3.4603008E7
jvm_memory_committed_bytes{area="nonheap",id="CodeHeap 'non-nmethods'",} 2555904.0
jvm_memory_committed_bytes{area="heap",id="G1 Eden Space",} 3.3554432E7
jvm_memory_committed_bytes{area="nonheap",id="Compressed Class Space",} 4390912.0
jvm_memory_committed_bytes{area="nonheap",id="CodeHeap 'non-profiled nmethods'",} 2883584.0
# HELP process_files_max_files The maximum file descriptor count
# TYPE process_files_max_files gauge
process_files_max_files 1048576.0
# HELP tomcat_sessions_active_max_sessions
# TYPE tomcat_sessions_active_max_sessions gauge
tomcat_sessions_active_max_sessions 0.0
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="nonheap",id="CodeHeap 'profiled nmethods'",} 7084160.0
jvm_memory_used_bytes{area="heap",id="G1 Survivor Space",} 3353600.0
jvm_memory_used_bytes{area="heap",id="G1 Old Gen",} 1.4562816E7
jvm_memory_used_bytes{area="nonheap",id="Metaspace",} 3.4250632E7
jvm_memory_used_bytes{area="nonheap",id="CodeHeap 'non-nmethods'",} 1266688.0
jvm_memory_used_bytes{area="heap",id="G1 Eden Space",} 8388608.0
jvm_memory_used_bytes{area="nonheap",id="Compressed Class Space",} 4227200.0
jvm_memory_used_bytes{area="nonheap",id="CodeHeap 'non-profiled nmethods'",} 2227200.0
# HELP system_cpu_usage The "recent cpu usage" for the whole system
# TYPE system_cpu_usage gauge
system_cpu_usage 0.1229865699356566
# HELP jvm_gc_memory_allocated_bytes_total Incremented for an increase in the size of the (young) heap memory pool after one GC to before the next
# TYPE jvm_gc_memory_allocated_bytes_total counter
jvm_gc_memory_allocated_bytes_total 1.048576E8
# HELP jvm_buffer_total_capacity_bytes An estimate of the total capacity of the buffers in this pool
# TYPE jvm_buffer_total_capacity_bytes gauge
jvm_buffer_total_capacity_bytes{id="mapped - 'non-volatile memory'",} 0.0
jvm_buffer_total_capacity_bytes{id="mapped",} 0.0
jvm_buffer_total_capacity_bytes{id="direct",} 57344.0
# HELP tomcat_sessions_rejected_sessions_total
# TYPE tomcat_sessions_rejected_sessions_total counter
tomcat_sessions_rejected_sessions_total 0.0
# HELP system_cpu_count The number of processors available to the Java virtual machine
# TYPE system_cpu_count gauge
system_cpu_count 8.0
# HELP jvm_threads_live_threads The current number of live threads including both daemon and non-daemon threads
# TYPE jvm_threads_live_threads gauge
jvm_threads_live_threads 21.0
# HELP tomcat_sessions_created_sessions_total
# TYPE tomcat_sessions_created_sessions_total counter
tomcat_sessions_created_sessions_total 0.0
# HELP process_cpu_usage The "recent cpu usage" for the Java Virtual Machine process
# TYPE process_cpu_usage gauge
process_cpu_usage 2.245541305004966E-4
# HELP jvm_gc_memory_promoted_bytes_total Count of positive increases in the size of the old generation memory pool before GC to after GC
# TYPE jvm_gc_memory_promoted_bytes_total counter
jvm_gc_memory_promoted_bytes_total 1.0376704E7
# HELP process_files_open_files The open file descriptor count
# TYPE process_files_open_files gauge
process_files_open_files 18.0
# HELP jvm_threads_peak_threads The peak live thread count since the Java virtual machine started or peak was reset
# TYPE jvm_threads_peak_threads gauge
jvm_threads_peak_threads 21.0
# HELP process_uptime_seconds The uptime of the Java virtual machine
# TYPE process_uptime_seconds gauge
process_uptime_seconds 182.17
# HELP http_server_requests_seconds
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",} 2.0
http_server_requests_seconds_sum{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",} 0.05235424
http_server_requests_seconds_count{exception="None",method="GET",outcome="CLIENT_ERROR",status="404",uri="/**",} 3.0
http_server_requests_seconds_sum{exception="None",method="GET",outcome="CLIENT_ERROR",status="404",uri="/**",} 0.020414163
# HELP http_server_requests_seconds_max
# TYPE http_server_requests_seconds_max gauge
http_server_requests_seconds_max{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",} 0.044979671
http_server_requests_seconds_max{exception="None",method="GET",outcome="CLIENT_ERROR",status="404",uri="/**",} 0.018157497
# HELP jvm_classes_loaded_classes The number of classes that are currently loaded in the Java virtual machine
# TYPE jvm_classes_loaded_classes gauge
jvm_classes_loaded_classes 7346.0
# HELP jvm_gc_live_data_size_bytes Size of long-lived heap memory pool after reclamation
# TYPE jvm_gc_live_data_size_bytes gauge
jvm_gc_live_data_size_bytes 0.0
# HELP process_start_time_seconds Start time of the process since unix epoch.
# TYPE process_start_time_seconds gauge
process_start_time_seconds 1.625642573087E9
# HELP jvm_gc_pause_seconds Time spent in GC pause
# TYPE jvm_gc_pause_seconds summary
jvm_gc_pause_seconds_count{action="end of minor GC",cause="G1 Evacuation Pause",} 3.0
jvm_gc_pause_seconds_sum{action="end of minor GC",cause="G1 Evacuation Pause",} 0.014
# HELP jvm_gc_pause_seconds_max Time spent in GC pause
# TYPE jvm_gc_pause_seconds_max gauge
jvm_gc_pause_seconds_max{action="end of minor GC",cause="G1 Evacuation Pause",} 0.0
# HELP logback_events_total Number of error level events that made it to the logs
# TYPE logback_events_total counter
logback_events_total{level="warn",} 0.0
logback_events_total{level="debug",} 0.0
logback_events_total{level="error",} 0.0
logback_events_total{level="trace",} 0.0
logback_events_total{level="info",} 8.0
# HELP jvm_buffer_count_buffers An estimate of the number of buffers in the pool
# TYPE jvm_buffer_count_buffers gauge
jvm_buffer_count_buffers{id="mapped - 'non-volatile memory'",} 0.0
jvm_buffer_count_buffers{id="mapped",} 0.0
jvm_buffer_count_buffers{id="direct",} 7.0
# HELP jvm_classes_unloaded_classes_total The total number of classes unloaded since the Java virtual machine has started execution
# TYPE jvm_classes_unloaded_classes_total counter
jvm_classes_unloaded_classes_total 0.0
# HELP jvm_memory_max_bytes The maximum amount of memory in bytes that can be used for memory management
# TYPE jvm_memory_max_bytes gauge
jvm_memory_max_bytes{area="nonheap",id="CodeHeap 'profiled nmethods'",} 1.22908672E8
jvm_memory_max_bytes{area="heap",id="G1 Survivor Space",} -1.0
jvm_memory_max_bytes{area="heap",id="G1 Old Gen",} 8.380219392E9
jvm_memory_max_bytes{area="nonheap",id="Metaspace",} -1.0
jvm_memory_max_bytes{area="nonheap",id="CodeHeap 'non-nmethods'",} 5840896.0
jvm_memory_max_bytes{area="heap",id="G1 Eden Space",} -1.0
jvm_memory_max_bytes{area="nonheap",id="Compressed Class Space",} 1.073741824E9
jvm_memory_max_bytes{area="nonheap",id="CodeHeap 'non-profiled nmethods'",} 1.22908672E8
# HELP system_load_average_1m The sum of the number of runnable entities queued to available processors and the number of runnable entities running on the available processo
rs averaged over a period of time
# TYPE system_load_average_1m gauge
system_load_average_1m 1.35
# HELP jvm_threads_states_threads The current number of threads having NEW state
# TYPE jvm_threads_states_threads gauge
jvm_threads_states_threads{state="runnable",} 7.0
jvm_threads_states_threads{state="blocked",} 0.0
jvm_threads_states_threads{state="waiting",} 11.0
jvm_threads_states_threads{state="timed-waiting",} 3.0
jvm_threads_states_threads{state="new",} 0.0
jvm_threads_states_threads{state="terminated",} 0.0
# HELP jvm_buffer_memory_used_bytes An estimate of the memory that the Java virtual machine is using for this buffer pool
# TYPE jvm_buffer_memory_used_bytes gauge
jvm_buffer_memory_used_bytes{id="mapped - 'non-volatile memory'",} 0.0
jvm_buffer_memory_used_bytes{id="mapped",} 0.0
jvm_buffer_memory_used_bytes{id="direct",} 57344.0
# HELP jvm_threads_daemon_threads The current number of live daemon threads
# TYPE jvm_threads_daemon_threads gauge
jvm_threads_daemon_threads 17.0
# HELP tomcat_sessions_active_current_sessions
# TYPE tomcat_sessions_active_current_sessions gauge
tomcat_sessions_active_current_sessions 0.0
# HELP tomcat_sessions_alive_max_seconds
# TYPE tomcat_sessions_alive_max_seconds gauge
tomcat_sessions_alive_max_seconds 0.0
```
