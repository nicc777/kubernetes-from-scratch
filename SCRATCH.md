# Notes, Reminders, Snippets and Random Stuff

This is my scratch pad and is not meant to make sense for anybody else really.

You are still welcome to look through the notes and use what you find useful.

## Links

| Resource | Notes |
|----------|-------|
| [Secure A Kubernetes Cluster With Pod Security Policies](https://docs.bitnami.com/tutorials/secure-kubernetes-cluster-psp/)  | Up to chapter 5 at least, security policies are not required, but this may be useful for later chapters |
| [Installing RabbitMQ Cluster Operator in a Kubernetes Cluster](https://www.rabbitmq.com/kubernetes/operator/install-operator.html) | Getting RabbitMQ up and running ina Kubernetes cluster |
| [SIGTERM not received by java process using 'docker stop' and the official java image](https://stackoverflow.com/questions/31836498/sigterm-not-received-by-java-process-using-docker-stop-and-the-official-java-i) | Read the notes on `Docker ENTRYPOINT and PID 1` below |

## Docker ENTRYPOINT and PID 1

From the [docker documentation](https://docs.docker.com/engine/reference/builder/#entrypoint):

```text
The shell form prevents any CMD or run command line arguments from being used, but has the disadvantage that your ENTRYPOINT will be started as a subcommand of /bin/sh -c, which does not pass signals. This means that the executable will not be the container’s PID 1 - and will not receive Unix signals - so your executable will not receive a SIGTERM from docker stop <container>.
```

Therefore, to properly handle signals we may need to consider using `CMD` rather than `ENTRYPOINT`.

However, testing this using docker with `ENTRYPOINT` and using Spring's `@PreDestroy` annotations all worked as expected, so perhaps Spring has some other way of dealing with this.

More testing and reading up required....
