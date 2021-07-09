# Chapter 06 - External Load Balancer

- [Chapter 06 - External Load Balancer](#chapter-06---external-load-balancer)
  - [Background and Orientation](#background-and-orientation)
  - [Preparing for changes](#preparing-for-changes)

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

