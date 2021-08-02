# Chapter 08 - Managing multiple versions of the same service in Kubernetes

- [Chapter 08 - Managing multiple versions of the same service in Kubernetes](#chapter-08---managing-multiple-versions-of-the-same-service-in-kubernetes)
  - [Objectives for this Chapter](#objectives-for-this-chapter)
  - [Starting on a Clean Slate](#starting-on-a-clean-slate)
    - [Deploy Version v1.1.3](#deploy-version-v113)
  - [Updates for version v2.0.0](#updates-for-version-v200)
    - [Code Changes](#code-changes)
  - [Deploy version v2.0.0](#deploy-version-v200)
  - [Update a Running Version (V2.0.0 to v2.0.1)](#update-a-running-version-v200-to-v201)
  - [Conclusion](#conclusion)

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

kubectl delete ingress conversions-ingress-v2 ; kubectl delete service conversions-service-v2 ; kubectl delete deployment conversions-deployment-v2
```

When you now run `kubectl get all` the output should be `No resources found in pocs namespace.`

### Deploy Version v1.1.3

The version tagged v1.1.3 in the [nicc777/java-conversions-app](https://github.com/nicc777/java-conversions-app) repository will point to the Docker image [hosted on GitHub](https://github.com/nicc777/java-conversions-app/pkgs/container/java-conversions-app/3994948)

In fact, since GitHub creates a unique version string, we actually need to adjust it accordingly in the `conversions_k8s.yaml` file, which is pointing to `v1.1.3`. You may also need to adjust your own version accordingly, if you are using your own repository.

_*Note*_: In some future chapter we will be looking in more detail at the concept of `GitOps`, where we will start to host our Kubernetes manifest files in a separate repository. I guess you can see why this is a good idea as it may avoid situations like this where we have to update our manifest after a release to reflect the new image location.

Therefore, our manifest will now look like this:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: conversions-deployment-v1
spec:
  selector:
    matchLabels:
      app: conversions-v1
  replicas: 2
  template:
    metadata:
      labels:
        app: conversions-v1
    spec:
      containers:
      - name: conversions-v1
        image: ghcr.io/nicc777/java-conversions-app:v1.1.3
        ports:
        - containerPort: 8888
        livenessProbe:
          httpGet:
            path: /conversions/v1/liveness
            port: 8888
            scheme: HTTP
          initialDelaySeconds: 20
          periodSeconds: 5
          successThreshold: 1
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /conversions/v1/readiness
            port: 8888
            scheme: HTTP
          initialDelaySeconds: 20
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: conversions-service-v1
spec:
  type: NodePort
  selector:
    app: conversions-v1
  ports:
    - protocol: TCP
      port: 9080
      targetPort: 8888
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: conversions-ingress-v1
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
            name: conversions-service-v1
            port:
              number: 9080
```

_*Note*_: To make it easier to distinguish versions, also the various components (deployment, service and ingress) have the version appended to their names. This will make it easier to delete the specific version in future without affecting any other running version of the application.

_*Also Note*_: In this new version I also updated items like `replicas` and `initialDelaySeconds` in order not to stress my test system too much. You may have to adjust these to suite your environment as well.

Deploy with the following command:

```shell
kubectl apply -f conversions_k8s.yaml
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

_*Important*_: Note that `dev` as the API Gateway service path is now used exclusively to point to the appropriate Kubernetes cluster, assuming your UAT and Production clusters are separate. There are also different strategies to hosting multiple environments and this guide is not about the different choices - it merely makes an assumption of a potential strategy you may find/choose.

## Updates for version v2.0.0

The project was updated with the major change obviously being a breaking API change. In fact, the change is rather significant in that it changed from two GET methods that was exposed specifically for Celsius to/from Fahrenheit conversion, to a more generic conversion POST method that can easily be extended to support many other types of conversion. Refer to [semantic versioning](https://semver.org/) again to understand why a breaking change like this will result in a new major version.

In terms of organizing the different versions in the repository, I opted for having major version branches - `v1` and `v2`. You or your teams approach may probably be different, but for the purposes of this guide, this approach is good enough. The latest stable version will always be merged to `main`, although that is not really that important for this guide.

Yet, it is worthwhile to examine the strategy perhaps a little closer even if it is just to ignite some thinking for you or your team. What needs to be considered for any strategy is how you would be able to support previous versions (`v1`) with bug and security fixes, while actively working on new major versions (`v2`, `v3`, etc.). 

In this example project, I opted to keep each major version on it's own branch. bug fixes and other work done on `v1`, will ultimately be merged into `v1` and when a new release (minor version) is required, I will trigger the pipeline with a tag in that branch. Therefore, each major branch (`v1` and `v2` etc.) can be independently maintained. At any time a new release tag can be added within any branch, and the build system will then produce a release from that branch, as can be seen in the screenshot below:

<a href="https://github.com/nicc777/kubernetes-from-scratch/raw/main/chapter_08/builds.png" target="_blank"><img src="https://github.com/nicc777/kubernetes-from-scratch/raw/main/chapter_08/builds.png" height="572" width="800" /></a>

The middle column provides some insights as to which branch or tag was referenced and the actual commit identifier can also be seen. 

The end result is that we have multiple Docker images available to us:

<a href="https://github.com/nicc777/kubernetes-from-scratch/raw/main/chapter_08/releases-01.png" target="_blank"><img src="https://github.com/nicc777/kubernetes-from-scratch/raw/main/chapter_08/releases-01.png" height="227" width="310" /></a>

<a href="https://github.com/nicc777/kubernetes-from-scratch/raw/main/chapter_08/releases-02.png" target="_blank"><img src="https://github.com/nicc777/kubernetes-from-scratch/raw/main/chapter_08/releases-02.png" height="570" width="800" /></a>

<a href="https://github.com/nicc777/kubernetes-from-scratch/raw/main/chapter_08/releases-03.png" target="_blank"><img src="https://github.com/nicc777/kubernetes-from-scratch/raw/main/chapter_08/releases-03.png" height="570" width="800" /></a>

We can therefore now much easier distinguish between the releases in our Kubernetes manifest by treating each major release completely on it's own. These differences in the manifest is also maintained on each branch, but ultimately we will be moving these to a different repository in upcoming chapters for a more efficient GitOps workflow:

<a href="https://github.com/nicc777/kubernetes-from-scratch/raw/main/chapter_08/diff.png" target="_blank"><img src="https://github.com/nicc777/kubernetes-from-scratch/raw/main/chapter_08/diff.png" height="1214" width="437" /></a>

_*Note*_: Before the manifest files can be applied, we will still need to replace the image tags with the actual values. This information is only available after the release, hence not properly synchronized in the current files. Also note that if you are using a different image registry, and assuming you properly tag your images in the build pipeline, you may actually be able to keep the versions of the images as it is in the file - just pointing to your registry of course.

### Code Changes

The most obvious change must be the conversion of the GET methods to a single POST method that can handle multiple conversion requests and that can also be easily extended to support much more than our simple celsius and fahrenheit conversions without needing to change the API.

There is also an additional custom exception that can be thrown when a request for unsupported conversions is received (effectively generate a [HTTP 400 response](https://en.wikipedia.org/wiki/List_of_HTTP_status_codes)). 

To support future conversions, a [strategy pattern](https://en.wikipedia.org/wiki/Strategy_pattern) was chosen. The implementation was contained in an `ENUM` which allow the strategy lookup to be done dynamically based on the `POST` data. In this particular case I chose this pattern as it will remain easy to extend the `ENUM`, even with 100's of different conversion scenarios, as the actual logic is all contained outside in the `service` layer.

Further reading:

* [An Enum implementation of the Strategy pattern](https://readlearncode.com/design-patterns/an-enum-implementation-of-the-strategy-pattern/)

## Deploy version v2.0.0

To deploy the new version, and assuming you are in the root directory of [the project](https://github.com/nicc777/java-conversions-app), run the following command:

```shell
git checkout v2
```
Now, update the `conversions_k8s.yaml` and point the image to version `ghcr.io/nicc777/java-conversions-app:v2.0.0`.

In a separate window, or pane, by running the command `watch kubectl get all`, you should notice some like the following, updating every 5 seconds:

```text
Every 2.0s: kubectl get all                        nicc777-G3-3779: Wed Jul 21 04:49:53 2021
NAME                                          READY   STATUS    RESTARTS   AGE
pod/conversions-deployment-v1-5797d97988-5mld8   1/1  Running   0          5m7s
pod/conversions-deployment-v1-5797d97988-6gqvm   1/1  Running   0          5m7s

NAME                             TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
service/conversions-service-v1   NodePort   10.43.188.241   <none>        9080:30590/TCP   5m7s

NAME                                        READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/conversions-deployment-v1   2/2     2            2           5m7s

NAME                                                   DESIRED   CURRENT   READY   AGE
replicaset.apps/conversions-deployment-v1-5797d97988   2         2         2       5m7s
```

The above running version is still `v1` deployed earlier.

We can now apply the changes for `v2`:

```shell
kubectl apply -f conversions_k8s.yaml
```

Once the new version is running, you should see the following:

```text
Every 2.0s: kubectl get all                        nicc777-G3-3779: Wed Jul 21 04:49:53 2021
NAME                                             READY   STATUS    RESTARTS   AGE
pod/conversions-deployment-v1-5797d97988-5mld8   1/1     Running   0          5m7s
pod/conversions-deployment-v1-5797d97988-6gqvm   1/1     Running   0          5m7s
pod/conversions-deployment-v2-6cf7d94b6b-dgkrb   1/1     Running   0          44s
pod/conversions-deployment-v2-6cf7d94b6b-bc44g   1/1     Running   0          44s

NAME                             TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
service/conversions-service-v1   NodePort   10.43.188.241   <none>        9080:30590/TCP   5m7s
service/conversions-service-v2   NodePort   10.43.89.64     <none>        9080:32632/TCP   44s

NAME                                        READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/conversions-deployment-v1   2/2     2            2           5m7s
deployment.apps/conversions-deployment-v2   2/2     2            2           44s

NAME                                                   DESIRED   CURRENT   READY   AGE
replicaset.apps/conversions-deployment-v1-5797d97988   2         2         2       5m7s
replicaset.apps/conversions-deployment-v2-6cf7d94b6b   2         2         2       44s
```

Let's test the two versions:

VERSION 1:

```shell
curl http://192.168.0.160:8000/dev/conversions/v1/convert/c-to-f/15
```

VERSION 2:

```shell
curl -X 'POST' \
  'http://192.168.0.160:8000/dev/conversions/v2/convert' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "sourceUnit": "Fahrenheit",
  "destinationUnit": "Celsius",
  "value": "59"
}'
```

And there you have it! Two different major versions of the same applications running at the same time!

## Update a Running Version (V2.0.0 to v2.0.1)

As it turns out, the [Snyk security scanner](https://support.snyk.io/hc/en-us/articles/360004032117-GitHub-integration) identified two issues and created a couple of PR's (see chapter 07, `Looking at a typical Snyk PR`).

The end result was that `v2` was updated with a new release to `v2.0.1`.

To apply this change in our Kubernetes cluster is as easy as just updating the `conversions_k8s.yaml` file and applying it again as before. In this example, the new container image is located at `ghcr.io/nicc777/java-conversions-app:v2.0.1`

When you apply the updated manifest, you will notice how Kubernetes replaced one pod ata time. You can continue to run the `curl` tests during this time and you should notice absolutely no downtime.

## Conclusion

First of all, below is a a table with the images you can use from the example repository, should you wish to make use of them:

| Major Version | Effective Version | Image URL                                     |
|:-------------:|:-----------------:|-----------------------------------------------|
| v1            | v1.1.3            | `ghcr.io/nicc777/java-conversions-app:v1.1.3` |
| v2            | v2.0.0            | `ghcr.io/nicc777/java-conversions-app:v2.0.0` |
| v2            | v2.0.1            | `ghcr.io/nicc777/java-conversions-app:v2.0.1` |

At this point, you should have two version of the same application running. You can also experiment updating a running version in place and observe how this upgrade is applied without any service interruption. You can also apply the same steps to roll back to a previous version (from `v2.0.1` back to `v2.0.0` for example). 

Every time you update and apply the manifest to the cluster, Kubernetes will check for the runtime difference and apply the required changes until the environment resembles the manifest. This is why Kubernetes is referred to as an Orchestration platform. You could have just run everything as normal Docker images, but all the steps would require manual intervention or some fancy scripting to achieve the same result. This may be very possible with only one or two applications, but it quickly becomes unmanageable as you add applications and other services.
