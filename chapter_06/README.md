# Chapter 06 - External Load Balancer

- [Chapter 06 - External Load Balancer](#chapter-06---external-load-balancer)
  - [Background and Orientation](#background-and-orientation)
  - [Preparing for changes](#preparing-for-changes)
    - [Host Lookups](#host-lookups)
    - [Bind an Ingress Path to Each Service](#bind-an-ingress-path-to-each-service)
    - [Side Track: Environment Variables](#side-track-environment-variables)
    - [Applying Cluster Changes](#applying-cluster-changes)
  - [Choosing an External Load Balancer](#choosing-an-external-load-balancer)
    - [Getting and Running HAProxy](#getting-and-running-haproxy)
    - [Testing](#testing)

## Background and Orientation

In chapter 5 we got our application running, but unless you are on the host where the nodes are running, it is actually not yet possible to connect from the rest of the LAN to the nodes.

If the nodes were on bare metal, you may be able to connect to each node, but it could still prove problematic in terms of handling failover or other scenarios where a particular node may go down.

The solution is to have an external load balancer that manages the load balancing of traffic to all the nodes.

To set this up, there are some more enhancement we need to do to our running service and also add a load balancer on our hosting system.

_*Note*_: In cloud deployment, for example Amazon AWS, an elastic load balancer would typically fulfill this role.

## Preparing for changes

The following changes need to be made for this exercise:

* Define the hosts for easier lookup (mimic a DNS-like experience).
* Enhance the Kubernetes ingress path logic

### Host Lookups

For host lookups, on the hosting system, we need to add the IP address for each node to the `/etc/hosts` file. First, obtain the addresses for the nodes by running the `kubectl get nodes -o wide` command. Your output should look something like this:

```text
NAME    STATUS   ROLES                  AGE   VERSION        INTERNAL-IP   EXTERNAL-IP   OS-IMAGE             KERNEL-VERSION     CONTAINER-RUNTIME
node3   Ready    <none>                 21h   v1.21.2+k3s1   10.0.50.61    <none>        Ubuntu 20.04.2 LTS   5.4.0-77-generic   containerd://1.4.4-k3s2
node1   Ready    control-plane,master   21h   v1.21.2+k3s1   10.0.50.103   <none>        Ubuntu 20.04.2 LTS   5.4.0-77-generic   containerd://1.4.4-k3s2
node2   Ready    <none>                 21h   v1.21.2+k3s1   10.0.50.199   <none>        Ubuntu 20.04.2 LTS   5.4.0-77-generic   containerd://1.4.4-k3s2
```

Each `INTERNAL-IP` IP address needs to be added to our  `/etc/hosts` file:

```shell
sudo sh -c 'echo "10.0.50.103 node1" >> /etc/hosts'

sudo sh -c 'echo "10.0.50.199 node2" >> /etc/hosts'

sudo sh -c 'echo "10.0.50.61 node3" >> /etc/hosts'
```

The output of `cat /etc/hosts` should look something like this:

```text
127.0.0.1       localhost
127.0.1.1       nicc777-G3-3779

# The following lines are desirable for IPv6 capable hosts
::1     ip6-localhost ip6-loopback
fe00::0 ip6-localnet
ff00::0 ip6-mcastprefix
ff02::1 ip6-allnodes
ff02::2 ip6-allrouters
10.0.50.61 node3
10.0.50.103 node1
10.0.50.199 node2
```

On the local host, we can now reference the nodes by name, for example:

```shell
curl http://node2/api/convert/c-to-f/15
```

Later, when the cluster is re-created or when the IP addresses change for some reason, we only have to update our `/etc/hosts` file and everything else should work fine.

_*Note*_: Proper DNS is the preferred way to deal with this in a production setting.

### Bind an Ingress Path to Each Service

Finally, we use a slightly different configuration where we will add the path `/conversions/v1`, as the base bath for this service. There are also some other small changes in the `conversions_k8s.yaml` file, so feel free to compare the file in this chapter with the file we used in chapter 06. In fact, we also renamed it for this chapter to `conversions-v1_k8s.yaml`.

The primary change, as you may see, is that we now introduce the notion of versions to our applications. This means that in the near future we may be able to run a second version side by side to our first version. This is done in to support any other consumers of the service to update in a more convenient time frame while we can run multiple versions of the same service.

In this implementation we will follow a strategy of path based versioning.

At this point we should also probably mention [semantic versioning](https://semver.org/). In this approach, any breaking changes to the API (interface changes), will require a new `major` version (from v1 to v2 for example), and in our path versioning the major version is therefore used to expose new major versions of the same service. Minor and patch version updates must never break compatibility and is only meant to enhance or implement bug fixes to the code base without affecting any other interface or contract. In a near future chapter we will also start to align our source versioning in out `pom.xml` to align with our Kubernetes manifest file.

Further reading:

* [How to design and version APIs for microservices (part 6)](https://www.ibm.com/cloud/blog/rapidly-developing-applications-part-6-exposing-and-versioning-apis) (IBM Article)
* [Versioning an API](https://cloud.google.com/endpoints/docs/openapi/versioning-an-api) - A Google cloud article, in which we follow the strategy for supporting `Backwards-incompatible changes`

### Side Track: Environment Variables

On a slightly unrelated note to the rest of this chapter, I need to through in a note about environment variables, which was also added to the new deployment manifest.

In Spring Boot, we can use environment variables to set properties for our application. For example, the environment variable `SERVICE_READY_WAITTIME` will be converted to `service.ready.waittime` and the value, which is required to be a string in our Yaml configuration, will be converted automatically to an `Integer` byt the Spring framework.

This approach addresses the `Configuration` principle of [the 12-factor application](https://12factor.net/config) by _storing the configuration in the environment_. This allows for great flexibility especially as our needs change from a development to eventually a production environment.

In a way, it also addresses the [separate build, release, run](https://12factor.net/build-release-run) principle, by separating our configuration completely from the build process and allows us to dynamically inject the appropriate configuration during deployment in an environment of our choosing.

Further reading:

* [Define an environment variable for a container](https://kubernetes.io/docs/tasks/inject-data-application/define-environment-variable-container/)
* [Externalized Configuration in Spring Boot](https://docs.spring.io/spring-boot/docs/1.5.6.RELEASE/reference/html/boot-features-external-config.html)

### Applying Cluster Changes

To ensure we start of a clean slate, we will first delete our current service:

```shell
kubectl delete ingress conversions-ingress ; kubectl delete service conversions-service; kubectl delete deployment conversions-deployment

kubectl delete ingress conversions-ingress-v1 ; kubectl delete service conversions-service-v1 ; kubectl delete deployment conversions-deployment-v1
```

Wait until all resources are gone. When running `kubectl get all` youu should get the output `No resources found in pocs namespace.`.

Next, ensure you are in the project ROOT folder and then change into the `chapter_06` directory:

```shell
cd chapter_06
```

Now, apply the new configuration:

```shell
kubectl apply -f conversions-v1_k8s.yaml
```

Let's see how our ingress configuration looks like by running `kubectl describe ingress`. You should expect output along the lines of the following:

```text
Name:             conversions-ingress-v1
Namespace:        pocs
Address:          10.0.50.103,10.0.50.199,10.0.50.61
Default backend:  default-http-backend:80 (<error: endpoints "default-http-backend" not found>)
Rules:
  Host        Path  Backends
  ----        ----  --------
  *
              /conversions/v1   conversions-service-v1:9080   10.42.1.16:8888,10.42.1.17:8888,10.42.2.14:8888 + 1 more...)
Annotations:  nginx.ingress.kubernetes.io/rewrite-target: /
Events:
  Type    Reason  Age                From                      Message
  ----    ------  ----               ----                      -------
  Normal  Sync    30s (x4 over 52s)  nginx-ingress-controller  Scheduled for sync
```

That means that we need to add the base path `/conversions/v1` to our `curl` request when we test:

```shell
curl http://node2/conversions/v1/api/convert/c-to-f/15
```

![paths](paths.png)


## Choosing an External Load Balancer

For this exercise, `HAProxy` will be deployed in the role of a load balancer, since it is really easy to configure and get going.

_*Note*_: In a public cloud environment like AWS, you may consider using [AWS API Gateway](https://aws.amazon.com/api-gateway/) together with an [AWS Elastic Load Balancer](https://aws.amazon.com/elasticloadbalancing/) to your [AWS EKS Cluster](https://aws.amazon.com/eks/) - other commercial public cloud providers have similar services.

Further reading:

* [Integrate Amazon API Gateway with Amazon EKS](https://aws.amazon.com/blogs/containers/integrate-amazon-api-gateway-with-amazon-eks/)

### Getting and Running HAProxy

For this part, you should be in a terminal window in your home directory. Just to make sure, run the command `cd $HOME`.

_*Note*_: I initially tried to use Kong, but it turned out `HAProxy` was just so much easier. I'm sticking to it for now, but I would like to revisit `Kong` in the future.

The following command will install `HAProxy`:

```shell
sudo apt install -y haproxy
```

Now edit the configuration with a text editor like `vim`:

```shell
sudo vim /etc/haproxy/haproxy.cfg
```

Add the following at the bottom of the file:

```text
frontend http_front
        bind *:80
        stats uri /haproxy?stats
        default_backend http_back

backend http_back
        balance roundrobin
        server node1 node1:80 check
        server node2 node2:80 check
        server node3 node3:80 check
```

And finally restart the service:

```shell
sudo systemctl restart haproxy
```

To verify that it is running, run the following command:

```shell
sudo systemctl status haproxy
```

The output should look like the following:

```text
● haproxy.service - HAProxy Load Balancer
     Loaded: loaded (/lib/systemd/system/haproxy.service; enabled; vendor preset: enabled)
     Active: active (running) since Sat 2021-07-10 21:23:03 SAST; 33s ago
       Docs: man:haproxy(1)
             file:/usr/share/doc/haproxy/configuration.txt.gz
    Process: 3050782 ExecStartPre=/usr/sbin/haproxy -f $CONFIG -c -q $EXTRAOPTS (code=exited, status=0/SUCCESS)
   Main PID: 3050794 (haproxy)
      Tasks: 9 (limit: 38213)
     Memory: 4.0M
     CGroup: /system.slice/haproxy.service
             ├─3050794 /usr/sbin/haproxy -Ws -f /etc/haproxy/haproxy.cfg -p /run/haproxy.pid -S /run/haproxy-master.sock
             └─3050795 /usr/sbin/haproxy -Ws -f /etc/haproxy/haproxy.cfg -p /run/haproxy.pid -S /run/haproxy-master.sock

Jul 10 21:23:03 nicc777-G3-3779 systemd[1]: Starting HAProxy Load Balancer...
Jul 10 21:23:03 nicc777-G3-3779 haproxy[3050794]: Proxy http_front started.
Jul 10 21:23:03 nicc777-G3-3779 haproxy[3050794]: Proxy http_front started.
Jul 10 21:23:03 nicc777-G3-3779 haproxy[3050794]: Proxy http_back started.
Jul 10 21:23:03 nicc777-G3-3779 haproxy[3050794]: [NOTICE] 190/212303 (3050794) : New worker #1 (3050795) forked
Jul 10 21:23:03 nicc777-G3-3779 haproxy[3050794]: Proxy http_back started.
Jul 10 21:23:03 nicc777-G3-3779 systemd[1]: Started HAProxy Load Balancer.
```

### Testing

The load balancer listens on the host on port 80. It will be listening on the LAN interface, so if you have another computer available (or even your mobile phone on WiFI would do the trick), you can test by using the following `curl` command:

```text
curl http://<<IP-address-of-your-host-running-haproxy>>/api/convert/c-to-f/15
```

In a separate terminal you can also watch the `HAProxy` logs to ensure it is getting the request. Run the following on the host with `HAProxy` installed:

```shell
tail -f /var/log/haproxy.log
```

Each time you test the service you should see the following line:

```text
Jul 10 21:26:17 nicc777-G3-3779 haproxy[3050795]: 127.0.0.1:59924 [10/Jul/2021:21:26:17.322] http_front http_back/node1 0/0/1/17/18 200 235 - - ---- 1/1/0/0/0 0/0 "GET /api/convert/c-to-f/15 HTTP/1.1"
Jul 10 21:27:04 nicc777-G3-3779 haproxy[3050795]: 192.168.0.100:61952 [10/Jul/2021:21:27:04.400] http_front http_back/node2 0/0/0/4/5 200 235 - - ---- 1/1/0/0/0 0/0 "GET /api/convert/c-to-f/15 HTTP/1.1"
```

_*Note*_: In the above example we can see it load balanced between `node1` and `node2`

You can also look at the `pod` logs with the following command:

```shell
kubectl logs -f -l app=conversions-v1 | grep -v called
```

You may see entries like the following appear as you test:

```text
2021-07-10 19:32:41.122  INFO 1 --- [nio-8888-exec-6] c.e.c.controllers.TempConvetController   : [conversions-deployment-v1-595d4b5fdd-4wptg] 15 celsius is 59.0 degrees fahrenheit
2021-07-10 19:32:42.346  INFO 1 --- [nio-8888-exec-8] c.e.c.controllers.TempConvetController   : [conversions-deployment-v1-595d4b5fdd-s7bg6] 15 celsius is 59.0 degrees fahrenheit
```

_*Note*_: You can also see different `pods` respond every time.


