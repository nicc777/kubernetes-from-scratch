# Chapter 08 - Managing multiple versions of the same service in Kubernetes

- [Chapter 08 - Managing multiple versions of the same service in Kubernetes](#chapter-08---managing-multiple-versions-of-the-same-service-in-kubernetes)
  - [Objectives for this Chapter](#objectives-for-this-chapter)
  - [Starting on a Clean Slate](#starting-on-a-clean-slate)
    - [Deploy Version v1.1.1](#deploy-version-v111)

## Objectives for this Chapter

Our main objective is to demonstrate how you could run two different versions of exactly the same application in a Kubernetes cluster (`v1` and `v2` of a service).

But why is this a thing?

Well, by following [semantic versioning](https://semver.org/) principles, you may have noticed that any [major version updates](https://semver.org/#spec-item-8) are used for changes where the applications public API's changes in a way that may break other application depending on these API's.

But, not all dependant applications may be able to change at the same pace, and it is therefore important to have a strategy which will allow you to support multiple versions of your applications (major versions), for a period of time in order to give other applications time to update.

The actual time period depends on a number of factors. If it's only internal application within a company, one or two months may be considered sufficient time and the time is more flexible depending on various internal factors to that company. However, if you have public facing API's or API's to which other organizations integrate with, you may have to consider a lot longer time period - say three, six or even twelve months. You can see an example of this approach in [this MS Azure deprecation notice](https://azure.microsoft.com/en-us/updates/azure-api-management-update-oct-18/). Microsoft generally provides about one year before removing an older version, unless there is a critical security issue which may prompt a shorter time (typically one month).

Since changes is a reality of life, it is good to learn early on how to deal with such changes and what it involves from a Kubernetes perspective.

## Starting on a Clean Slate

_*Note*_: Make sure you are in the `pocs` namespace.

For this chapter, we first need to clean out our current running services in Kubernetes. You can do this with the following command:

```shell
kubectl delete ingress conversions-ingress ; kubectl delete service conversions-service; kubectl delete deployment conversions-deployment

kubectl delete ingress conversions-ingress-v1 ; kubectl delete service conversions-service-v1 ; kubectl delete deployment conversions-deployment-v1
```

When you now run `kubectl get all` the output should be `No resources found in pocs namespace.`

### Deploy Version v1.1.1

The version tagged v1.1.1 in the [nicc777/java-conversions-app](https://github.com/nicc777/java-conversions-app) repository will point to the Docker image [hosted on GitHub](https://github.com/nicc777/java-conversions-app/pkgs/container/java-conversions-app/3994948)

In fact, since GitHub creates a unique version string, we actually need to adjust it accordingly in the `conversions_k8s.yaml` file, which is pointing to `v1.1.1`. You may also need to adjust your own version accordingly, if you are using your own repository.

_*Note*_: In some future chapter we will be looking in more detail at the concept of `GitOps`, where we will start to host our Kubernetes manifest files in a separate repository. I guess you can see why this is a good idea as it may avoid situations like this where we have to update our manifest after a release to reflect the new image location.

Therefore, our manifest will now look like this:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: conversions-deployment
spec:
  selector:
    matchLabels:
      app: conversions
  replicas: 4 # tells deployment to run 4 pods matching the template
  template:
    metadata:
      labels:
        app: conversions
    spec:
      containers:
      - name: conversions
        image: ghcr.io/nicc777/java-conversions-app@sha256:dd422457f5ba60879e26a212d8d1f9d53d88a3b7e31281248ce79c372df41baa
        ports:
        - containerPort: 8888
        livenessProbe:
          httpGet:
            path: /conversions/v1/liveness
            port: 8888
            scheme: HTTP
          initialDelaySeconds: 15
          periodSeconds: 5
          successThreshold: 1
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /conversions/v1/readiness
            port: 8888
            scheme: HTTP
          initialDelaySeconds: 15
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: conversions-service
spec:
  type: NodePort
  selector:
    app: conversions
  ports:
    - protocol: TCP
      port: 9080
      targetPort: 8888
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: conversions-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - http:
      paths:
      - path: /conversions/v1
        pathType: Prefix
        backend:
          service:
            name: conversions-service
            port:
              number: 9080
```

Deploy with the following command:

```shell
kubectl apply conversions_k8s.yaml
```

If it is the first time you download the image, and depending on your Internet speed, this deployment may take a couple of minutes.

Once the Pods are all in a `Running` state, we can test quickly with:

```shell
curl http://node2/conversions/v1/convert/c-to-f/15
```

The expected output:

```text
{"inputDegreesUnit":"celsius","inputDegrees":15.0,"convertedDegrees":59.0,"convertedDegreesUnit":"fahrenheit"}
```

_*Important*_: Note that due to the base path that have changed it is not the exact same command as we have run before. However, functionally, everything is still the same.

The updated path also needs to be reflected in our external network configuration:

* `HAProxy` config does not need an update at this time
* `Kong` requires an update to reflect the new path - a new copy of the `kong` configuration is available in this chapter subdirectory.

On your system, update `/etc/kong/kong.conf` to point to the updated `kong.yml` in this chapter. You need to edit the path to suite your environment.

Finally, restart `kong` with the command:

```shell
sudo kong restart
```

And a quick test:

```shell
curl http://k8s-dev:8000/dev/conversions/v1/convert/c-to-f/15
```

This will once again produce the output:

```text
{"inputDegreesUnit":"celsius","inputDegrees":15.0,"convertedDegrees":59.0,"convertedDegreesUnit":"fahrenheit"}
```

The new network integration diagram now looks like this:

<a href="https://github.com/nicc777/kubernetes-from-scratch/raw/main/chapter_08/network_integration_diagram.png" target="_blank"><img src="https://github.com/nicc777/kubernetes-from-scratch/raw/main/chapter_08/network_integration_diagram.png" height="696" width="731" /></a>

Obvisouly we also need a small rethink of our multi version strategy, which is reflected in the following diagram:

<a href="https://github.com/nicc777/kubernetes-from-scratch/raw/main/chapter_08/updated_multiple_version_strategy.png" target="_blank"><img src="https://github.com/nicc777/kubernetes-from-scratch/raw/main/chapter_08/updated_multiple_version_strategy.png" height="561" width="800" /></a>

