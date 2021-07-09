# Chapter 05 - Taking a basic 12-factor application to a Kubernetes cluster

- [Chapter 05 - Taking a basic 12-factor application to a Kubernetes cluster](#chapter-05---taking-a-basic-12-factor-application-to-a-kubernetes-cluster)
  - [Goals of this chapter](#goals-of-this-chapter)
  - [More tools - tmux](#more-tools---tmux)
  - [Preparing the application](#preparing-the-application)
  - [Preparing and applying our deployment](#preparing-and-applying-our-deployment)
  - [Testing](#testing)
  - [Cleaning Up](#cleaning-up)

## Goals of this chapter

In this chapter will take a copy of our project from [chapter 03](../chapter_03/README.md) and adapt it for deployment in our kubernetes cluster we created in [chapter 04](../chapter_04/README.md)

The changes in the source code include the following:

* Added `liveness` and `readiness` end points to our application (refer to the "_[Configure Liveness, Readiness and Startup Probes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)_" documentation for kubernetes)
* Created our deployment yaml file in order to deploy our application in the cluster

We will then experiment a little with long startup times and terminate times to see how kubernetes manage those instances

## More tools - tmux

At this point I would also like to suggest a `tmux` layout that is typically used for testing. 

```text
+------------------------------------+----------------------------------------+
|                                    |                                        |
|                                    |                                        |
|                                    |                                        |
|                pane 0              |                  pane 1                |
|                                    |                                        |
|                                    |                                        |
+------------------------------------+----------------------------------------|
|                                                                             |
|                                pane 2                                       |
|                                                                             |
+-----------------------------------------------------------------------------+
```

<a href="https://github.com/nicc777/kubernetes-from-scratch/raw/main/chapter_05/tmux_session.png"  target="_blank"><img src="https://github.com/nicc777/kubernetes-from-scratch/raw/main/chapter_05/tmux_session.png" height=454 width=844><a>

I get this layout by starting `tmux` and then first split horizontally (`CTRL+B` and then `SHIFT+"`).

Next, make sure you are on the top pane. If you press keys and it appears on the bottom pane, press `CTRL+B` and then press just the `UP_ARROW`.

Now, split the upper pane vertically with `CTRL+B` and then `SHIFT+%`.

The pane numbers above I got by pressing `CTRL+B` and then `q`. Your layout may be different, but for reference I will use the top layout.

Typically I use the panes for the following:

* Pane 0 - Where I run things like `watch curl ....` when testing endpoints.
* Pane 1 - Where I usually run `watch kubectl get all` within a certain namespace, just to see what is happening inside your namespace
* Pane 2 - Where I run things like `kubectl apply ...` and then I can see the effect on the other two panes.

The `tmux` tool is extremely handy as you can also save your session and completely disconnect. Later, when you log back into your remote system, you can restore the session and everything will still be running as you left it.

A nice cheat sheet I often reference [can be found here](https://tmuxcheatsheet.com/)

Right now, just ensure you run `export KUBECONFIG=$HOME/k3s.yaml` in each of these panes to ensure you reference the correct cluster.

## Preparing the application

You may want to refer back to [chapter 03](../chapter_03/README.md) if you unsure of what the following commands all do.

Basically we need to:

* Build the application to create a new `jar` file
* Build a local Docker image
* Push the new updated image to our Docker Hub project

If using `tmux` (which I assume), all the following commands are run from `pane 2`.

Assuming you start from the project ROOT directory, first run `cd chapter_05/project_source_code/conversions` to change into the application project directory.

No run:

```shell
./mvnw clean

./mvnw package

docker build -t conversions .

docker login

docker image tag conversions $DOCKER_HUB_USER/conversions:latest

docker image tag conversions $DOCKER_HUB_USER/conversions:v0.0.2

docker image push --all-tags $DOCKER_HUB_USER/conversions
```

## Preparing and applying our deployment

Now, ensure you are in the correct namespace:

```shell
kubectl config view -o jsonpath='{.contexts[].context.namespace}'
```

The output should be `pocs`.

In `pane 1`, run `watch -n1 kubectl get all`. You can switch back to `pane 2` when ready.

You can view the details of the deployment in the `conversions_k8s.yaml` file.

Now run the following command to deploy our application:

```shell
kubectl apply -f conversions_k8s.yaml
```

In `pane 1` you should see things starting to happen... It will take a minute or two for the image to download from DOcker Hub, depending of course on your Internet speed. On my system, it took just under 4 minutes for all pods to get to the `Running` state.

_*Important*_: If you did not edit the `conversions_k8s.yaml` file, you are actually using the image I created for this guide. This is not an issue at all. However, you can edit line 17 of the file to point to your own image in Docker Hub.

## Testing

First, we need to find where our endpoints are listening:

```shell
kubectl describe ingress
```

Output:

```text
Name:             conversions-ingress
Namespace:        pocs
Address:          10.0.50.149,10.0.50.246,10.0.50.29
Default backend:  default-http-backend:80 (<error: endpoints "default-http-backend" not found>)
Rules:
  Host        Path  Backends
  ----        ----  --------
  *
              /   conversions-service:9080   10.42.1.8:8888,10.42.1.9:8888,10.42.2.10:8888 + 1 more...)
Annotations:  nginx.ingress.kubernetes.io/rewrite-target: /
Events:
  Type    Reason  Age                 From                      Message
  ----    ------  ----                ----                      -------
  Normal  Sync    11s (x8 over 3m1s)  nginx-ingress-controller  Scheduled for sync
```

Our service can be described by the command:

```shell
kubectl get service -o wide
```

Output:

```text
NAME                  TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE    SELECTOR
conversions-service   NodePort   10.43.210.211   <none>        9080:31346/TCP   143m   app=conversions
```

We can also see how our pods have been distributed across the nodes:

```shell
kubectl get pods -o wide
```

Output:

```text
NAME                                      READY   STATUS    RESTARTS   AGE   IP           NODE    NOMINATED NODE   READINESS GATES
conversions-deployment-78bb955d57-m9nsx   1/1     Running   0          12m   10.42.2.9    node3   <none>           <none>
conversions-deployment-78bb955d57-w9t6l   1/1     Running   0          12m   10.42.1.9    node2   <none>           <none>
conversions-deployment-78bb955d57-wq6zs   1/1     Running   0          12m   10.42.1.8    node2   <none>           <none>
conversions-deployment-78bb955d57-k4m75   1/1     Running   0          12m   10.42.2.10   node3   <none>           <none>
```

To see all of this in a diagram, you could form the following mental picture:

![network-diagram](network_traffic.png)

## Cleaning Up

To delete your deployments, you can run the following command:

```shell
kubectl delete ingress conversions-ingress ; kubectl delete service conversions-service; kubectl delete deployment conversions-deployment; 
```

