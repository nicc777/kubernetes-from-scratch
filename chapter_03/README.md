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
    - [Pushing the Image to Docker Hub](#pushing-the-image-to-docker-hub)
    - [About Logs](#about-logs)
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

An important additional add-on to the 12-factor application, as [described by Microsoft](https://docs.microsoft.com/en-us/dotnet/architecture/cloud-native/definition), is _telemetry_. The application will therefore also include the basic requirements for [Prometheus](https://prometheus.io/) which will later be used to gain more insights in the running of our application. It is not strictly required for the demonstrations in this chapter, but it is good practice to start including these minimum requirements from an early stage so that you get used to the patterns.

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

The complete developed example is in the directory `chapter_03/project_source_code/conversions`, relative to the project ROOT directory.

Further reading:

* [Spring Boot Tutorial – Bootstrap a Simple Application](https://www.baeldung.com/spring-boot-start)
* [Project Source Code on GitHub](https://github.com/nicc777/kubernetes-from-scratch/tree/main/chapter_03/project_source_code/conversions)

## Implementation

_*Application Design Notes*_: Please refer to [the additional application notes](APPLICATION_NOTES.md) for a deeper delve into the design and working of our sample application.

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

_*Note*_:" An [OpenAPI 3](https://swagger.io/specification/) endpoint is also available on the path `/swagger-ui.html`. If you are on your local machine, just [open http://127.0.0.1:8888/swagger-ui.html](http://127.0.0.1:8888/swagger-ui.html).

### Preparing the Docker Build

The Docker image can be build using the following command:

```
docker build -t conversions .
```

The expected output should look something like this:

```text
Sending build context to Docker daemon  19.66MB
Step 1/10 : FROM openjdk:16.0.1
 ---> f4f1dadedfab
Step 2/10 : ARG JAR_FILE=target/*.jar
 ---> Using cache
 ---> cb77da6ff601
Step 3/10 : COPY ${JAR_FILE} app.jar
 ---> ff729388fcac
Step 4/10 : EXPOSE 8888
 ---> Running in c4f814f08d1d
Removing intermediate container c4f814f08d1d
 ---> 3f3bd09617c5
Step 5/10 : EXPOSE 8080
 ---> Running in 0abc8e811ef8
Removing intermediate container 0abc8e811ef8
 ---> 17c72ef03d80
Step 6/10 : EXPOSE 80
 ---> Running in bd087b4f6d01
Removing intermediate container bd087b4f6d01
 ---> c08975e67e53
Step 7/10 : ENV SERVER_PORT 8888
 ---> Running in 09692897ca46
Removing intermediate container 09692897ca46
 ---> eafbf1b5f2b8
Step 8/10 : ENV SERVICE_READY_WAITTIME 3000
 ---> Running in 410d50601aab
Removing intermediate container 410d50601aab
 ---> a2f24dc70541
Step 9/10 : ENV SERVICE_TERMINATE_WAITTIME 3000
 ---> Running in 75fdce150ef1
Removing intermediate container 75fdce150ef1
 ---> 2407cd9bd992
Step 10/10 : ENTRYPOINT ["java","-jar","/app.jar"]
 ---> Running in 6d44fbb5ed59
Removing intermediate container 6d44fbb5ed59
 ---> dede6e13a3a1
Successfully built dede6e13a3a1
Successfully tagged conversions:latest
```

Further reading:

* [Dockerfile reference documentation](https://docs.docker.com/engine/reference/builder/)
* [Docker build command reference documentation](https://docs.docker.com/engine/reference/commandline/build/)

### Testing the Docker Image

Run the newly created image by issuing the following command:

```shell
docker run --name conversions-app -p 8888:8888 conversions
```

You should see the same output as when you did running the Java JAR file manually. You can run the same tests as before to test the application.

To stop the container, press `CTRL+C`. Once again, it should take 5 seconds to stop.

To remove the Docker container after you have stopped it. run the following command:

```shell
docker rm conversions-app
```
_*Note*_: This will remove the container, but the image you built earlier is still available.

To run the image as a container with customized setting, for example a short wait time during startup, run the following command:

```shell
docker run --name conversions-app -p 8888:8888 -e SERVICE_READY_WAITTIME=1000 conversions
```

You can test again, and when you stop the container remember to remove it again as before,

In order to start the container for the first time in the background, we just add a `-d` switch to the command:

```shell
docker run --name conversions-app -p 8888:8888 -d conversions
```

_*Note*_: In this case, the only output to STDOUT will be the container ID. 


You can list your containers with the following command:

```shell
docker container ls
```

Th output should look something like this:

```text
CONTAINER ID   IMAGE           COMMAND                 CREATED         STATUS         PORTS                                       NAMES
398cc3e7c7af   conversions     "java -jar /app.jar"    5 seconds ago   Up 4 seconds   80/tcp, 8080/tcp, 0.0.0.0:8888->8888/tcp    conversions-app
```

To stop the container run the following command:

```shell
docker container stop conversions-app
```

_*Note*_: Since we have a 3 second wait time, it should take at least 3 seconds to stop the container. You can test this with the following command (assuming the container is still running):

```shell
time docker container stop conversions-app
```

Output:

```text
conversions-app
docker container stop conversions-app  0.02s user 0.02s system 1% cpu 3.439 total
```

To start the same container again:

```shell
docker container start conversions-app
```

Further reading:

* [Docker run command reference documentation](https://docs.docker.com/engine/reference/commandline/run/)

### Pushing the Image to Docker Hub

For this exercise, it is assumed your Docker Hub username is stored in the environment variable `DOCKER_HUB_USER`.

You also need to create a project on your Docker Hub account called `conversions`.

Finally, to push the image to Docker Hub, the following steps can be followed:

First, login:

```shell
docker login
```

The expected output should be something like this:

```text
Authenticating with existing credentials...
WARNING! Your password will be stored unencrypted in /home/XXXXXXX/.docker/config.json.
Configure a credential helper to remove this warning. See
https://docs.docker.com/engine/reference/commandline/login/#credentials-store

Login Succeeded
```

Next, we will tag and push our image with the following 3 commands:

```shell
docker image tag conversions $DOCKER_HUB_USER/conversions:latest

docker image tag conversions $DOCKER_HUB_USER/conversions:v0.0.1

docker image push --all-tags $DOCKER_HUB_USER/conversions
```

Expected output may look something like this:

```text
The push refers to repository [docker.io/XXXXXXX/conversions]
7ac16bef77a6: Pushed 
0133af18fed3: Mounted from XXXXXXX/tempconvert 
3b5ee40e11ca: Mounted from XXXXXXX/tempconvert 
389989a49b52: Mounted from XXXXXXX/tempconvert 
latest: digest: sha256:a675d78d9ea5ff11f04da2f6ce214fa2b966f650871eb697d4dc79e2327206f5 size: 1166
7ac16bef77a6: Layer already exists 
0133af18fed3: Layer already exists 
3b5ee40e11ca: Layer already exists 
389989a49b52: Layer already exists 
v0.0.1: digest: sha256:a675d78d9ea5ff11f04da2f6ce214fa2b966f650871eb697d4dc79e2327206f5 size: 1166
```

_*Note*_: For the purpose of this guide, all images are published to PUBLIC Docker Hub projects. There are numerous other registries and options available.

Further reading:

* [Docker image tag command reference documentation](https://docs.docker.com/engine/reference/commandline/image_tag/)
* [Docker image push command reference documentation](https://docs.docker.com/engine/reference/commandline/image_push/)

### About Logs

If you start the application directory from the command line (not using Docker), you will notice the logs are printed on STDOUT in your terminal session, for example:

```text
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.5.2)

2021-07-07 09:39:32.643  INFO 2801116 --- [           main] c.e.c.ConversionsDemoApplication         : Starting ConversionsDemoApplication v0.0.1-SNAPSHOT using Java 16.0.1 on nicc777-G3-3779 with PID 2801116 (/home/nicc777/git/Personal_Repos/GitHub/kubernetes-from-scratch/chapter_03/project_source_code/conversions/target/conversions-0.0.1-SNAPSHOT.jar started by nicc777 in /home/nicc777/git/Personal_Repos/GitHub/kubernetes-from-scratch/chapter_03/project_source_code/conversions)
2021-07-07 09:39:32.647  INFO 2801116 --- [           main] c.e.c.ConversionsDemoApplication         : No active profile set, falling back to default profiles: default
2021-07-07 09:39:33.612  INFO 2801116 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8888 (http)
2021-07-07 09:39:33.623  INFO 2801116 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2021-07-07 09:39:33.623  INFO 2801116 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.48]
2021-07-07 09:39:33.662  INFO 2801116 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2021-07-07 09:39:33.662  INFO 2801116 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 949 ms
2021-07-07 09:39:33.853  INFO 2801116 --- [           main] c.e.c.services.ApplicationStateService   : Starting up...
2021-07-07 09:39:38.854  INFO 2801116 --- [           main] c.e.c.services.ApplicationStateService   : READY
2021-07-07 09:39:39.153  INFO 2801116 --- [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 3 endpoint(s) beneath base path '/actuator'
2021-07-07 09:39:39.185  INFO 2801116 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8888 (http) with context path ''
2021-07-07 09:39:39.196  INFO 2801116 --- [           main] c.e.c.ConversionsDemoApplication         : Started ConversionsDemoApplication in 6.945 seconds (JVM running for 7.354)
2021-07-07 09:41:02.585  INFO 2801116 --- [nio-8888-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2021-07-07 09:41:02.585  INFO 2801116 --- [nio-8888-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2021-07-07 09:41:02.586  INFO 2801116 --- [nio-8888-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
2021-07-07 09:41:02.613  INFO 2801116 --- [nio-8888-exec-1] c.e.c.controllers.TempConvetController   : [nicc777-G3-3779] 15 celsius is 59.0 degrees fahrenheit
2021-07-07 09:42:17.933  INFO 2801116 --- [nio-8888-exec-2] c.e.c.controllers.TempConvetController   : [nicc777-G3-3779] 59.0 fahrenheit is 15.0 degrees celsius
^C2021-07-07 09:46:09.632  INFO 2801116 --- [ionShutdownHook] c.e.c.services.ApplicationStateService   : Shutting down...
2021-07-07 09:46:14.633  INFO 2801116 --- [ionShutdownHook] c.e.c.services.ApplicationStateService   : TERMINATING
```

_*Note*_: The `^C` in the second last line is wjat you see when you press `CTRL+C` on your keyboard, which will in turn send a `SIGTERM` signal to the application. This in turn will trigger the `@PreDestroy` hook and you should notice a 5 second delay between pressing the keys and the application stopping (assuming the default setting for `service.terminate.waittime` is used.)

Now, Assuming your container from the previous section is still running, you can attach to the logs similar to running a `tail -f` which you should be accustomed to:

```shell
docker logs -f conversions-app
```

When pressing `CTRL+C`, you stop following the logs, but the container is still running.

Each time you test and end-point using the previous examples, you should see information being appended to the log output.

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
