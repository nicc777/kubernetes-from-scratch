# Chapter 04 - Install a Kubernetes cluster on Ubuntu Linux using Multipass as a virtual host management system

## Accelerated Development Cluster Options

There are a number of ways to get a Kubernetes playground going. However, since we aim to reduce dev-prod parity (12-factor application principle), it sounds like a better idea to run more than one kubernetes node as it would be done in production.

For this purpose we turn to `k3s` - "_a highly available, certified Kubernetes distribution <span style="text-decoration:underline">*designed for production workloads*</span> in unattended, <span style="text-decoration:underline">*resource-constrained*</span>, remote locations or inside IoT appliances_" (from [the home page](https://k3s.io/) of `k3s`).

It seems `k3s` is perfect for a typical developer workstation then!

## Virtual Servers

The way we are going to deploy `k3s` on our local workstation is by running all the nodes in separate virtual machines. It should be safe to assume by now that most IT professionals (even noobs), should understand the concept of a virtual machine.

Again, there are several options. For our purposes, we will use [multipass](https://multipass.run/) which is an easy and quick way to get started with Ubuntu virtual machines on almost any base platform. For these guides, it is assumed that `multipass` in installed on your system.

## Installing kubectl application

Follow [these instructions](https://kubernetes.io/docs/tasks/tools/install-kubectl-linux/) to install `kubectl`.

Since you do not yet have a running cluster, you can run `kubectl version` from which you should see at least the client version.

Finally, to make life a little easier, consider assigning the alias `k` as a shortcut to `kubectl`. In this guide, the full executable name will always be used, but in reality you can always just use `k`, for example `k version`.

## Lets install a kubernetes cluster already

_*Note*_: The following assumes you are using a Unix type operating system like Linux or OSX. This may also work on WIndows through WSL, but I have not tested this. If you do try it on WSL, I would suggest using an Ubuntu distro with WSL.

_*Note*_: Ensure you are in your home directory. You can easily do this by running `cd $HOME`

Make sure `multipass` and `wget` is installed. You can check if multipass is installed by running the following command:

```shell
multipass version
```

You should get the following output (version may differ):

```text
multipass  1.6.2
multipassd 1.6.2
```

In almost all cases `wget` should already be installed on your system, but if it is not, it realy is very easy to install - please consult your package manager of choice documentation on how to do this. Now run the following:

```shell
wget -O k3s-multipass.sh https://gist.github.com/nicc777/0f620c9eb2958f58173224f29b23a2ff/raw/938513b9ce35b9a0a1aaf4f82e58cce073d3157a/k3s-multipass.sh && chmod 700 k3s-multipass.sh && ls -lahrt k3s-multipass.sh
```

You should get the following output (more-or-less):

```text
--2021-07-08 08:50:35--  https://gist.github.com/nicc777/0f620c9eb2958f58173224f29b23a2ff/raw/938513b9ce35b9a0a1aaf4f82e58cce073d3157a/k3s-multipass.sh
Resolving gist.github.com (gist.github.com)... 140.82.121.3
Connecting to gist.github.com (gist.github.com)|140.82.121.3|:443... connected.
HTTP request sent, awaiting response... 301 Moved Permanently
Location: https://gist.githubusercontent.com/nicc777/0f620c9eb2958f58173224f29b23a2ff/raw/938513b9ce35b9a0a1aaf4f82e58cce073d3157a/k3s-multipass.sh [following]
--2021-07-08 08:50:36--  https://gist.githubusercontent.com/nicc777/0f620c9eb2958f58173224f29b23a2ff/raw/938513b9ce35b9a0a1aaf4f82e58cce073d3157a/k3s-multipass.sh
Resolving gist.githubusercontent.com (gist.githubusercontent.com)... 185.199.109.133, 185.199.108.133, 185.199.111.133, ...
Connecting to gist.githubusercontent.com (gist.githubusercontent.com)|185.199.109.133|:443... connected.
HTTP request sent, awaiting response... 200 OK
Length: 1040 (1.0K) [text/plain]
Saving to: ‘k3s-multipass.sh’

k3s-multipass.sh                                                      100%[=========================================================================================================================================================================>]   1.02K  --.-KB/s    in 0s

2021-07-08 08:50:37 (17.7 MB/s) - ‘k3s-multipass.sh’ saved [1040/1040]

-rwx------ 1 nicc777 nicc777 1.1K Jul  8 08:50 k3s-multipass.sh
```

All you have to do now is run the script:

```shell
./k3s-multipass.sh
```

On my system (with an average download speed of around 20 mbps), the entire process took about 8 minutes.

To see the running nodes, run `multipass list` and you should see the following output:

```text
Name                    State             IPv4             Image
node1                   Running           10.0.50.29       Ubuntu 20.04 LTS
                                          10.42.0.0
                                          10.42.0.1
node2                   Running           10.0.50.149      Ubuntu 20.04 LTS
                                          10.42.1.0
                                          10.42.1.1
node3                   Running           10.0.50.246      Ubuntu 20.04 LTS
                                          10.42.2.0
                                          10.42.2.1
```

_*Note*_: Your IP addresses may look somewhat different.

Next we have to get the master node IP address and store in in an environment variable:

```shell
export IP=$(multipass info node1 | grep IPv4 | awk '{print $2}')
```

Now, one final step to fix our configuration:

```shell
sed -i "s/127.0.0.1/$IP/" k3s.yaml
```

Now, to ensure `kubectl` is using the config, run the following:

```shell
export KUBECONFIG=$HOME/k3s.yaml
```

_*Note*_: You have to run this every time your open a new terminal if you want `kubectl` to reference this development cluster. There are numerous other ways you can configure multiple clusters, so adjust this to your needs. You can refer to [the kubernetes documentation](https://kubernetes.io/docs/tasks/access-application-cluster/configure-access-multiple-clusters/) for more information on how to configure the `kubectl` application for multiple clusters.

Final test to see that everything is working:

```shell
kubectl cluster-info
```

The output may look something like this:

```text
Kubernetes master is running at https://10.0.50.29:6443
CoreDNS is running at https://10.0.50.29:6443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
Metrics-server is running at https://10.0.50.29:6443/api/v1/namespaces/kube-system/services/https:metrics-server:/proxy

To further debug and diagnose cluster problems, use 'kubectl cluster-info dump'.
```

To see all your nodes, run:

```shell
kubectl get nodes
```

The output should look like this:

```text
NAME    STATUS   ROLES                  AGE   VERSION
node3   Ready    <none>                 20m   v1.21.2+k3s1
node1   Ready    control-plane,master   22m   v1.21.2+k3s1
node2   Ready    <none>                 21m   v1.21.2+k3s1
```


