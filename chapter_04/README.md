# Chapter 04 - Install a Kubernetes cluster on Ubuntu Linux using Multipass as a virtual host management system

- [Chapter 04 - Install a Kubernetes cluster on Ubuntu Linux using Multipass as a virtual host management system](#chapter-04---install-a-kubernetes-cluster-on-ubuntu-linux-using-multipass-as-a-virtual-host-management-system)
  - [Accelerated Development Cluster Options](#accelerated-development-cluster-options)
  - [Virtual Servers](#virtual-servers)
  - [Installing kubectl application](#installing-kubectl-application)
  - [Lets install a kubernetes cluster already](#lets-install-a-kubernetes-cluster-already)
  - [Configuring an Ingress Gateway](#configuring-an-ingress-gateway)
  - [Namespaces](#namespaces)

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

_*Note*_: Assuming your `$GIT_PROJECT_DIR` points to where you clone your git repositories - something like `$HOME\git` perhaps.

Make sure `multipass` and `wget` is installed. You can check if multipass is installed by running the following command:

```shell
multipass version
```

You should get the following output (version may differ):

```text
multipass  1.6.2
multipassd 1.6.2
```

_*Important*_: This next step will create 3x nodes, each dedicated to 2x CPU's and 4GiB or RAM each. You may have to adjust the settings to suite your needs. Please consult the [multipass documentation](https://multipass.run/docs/launch-command) and adjust the settings on line 2 of the script if required. The script source is maintained [using a GitHub Gist](https://gist.github.com/nicc777/0f620c9eb2958f58173224f29b23a2ff)

```shell
cd $GIT_PROJECT_DIR/kubernetes-from-scratch/chapter_04 && chmod 700 k3s-multipass.sh && ls -lahrt k3s-multipass.sh
```

You should get the following output (more-or-less):

```text
-rwx------ 1 xxxxxxx xxxxxx 1.1K Jul  8 08:50 k3s-multipass.sh
```

All you have to do now is run the script:

```shell
./k3s-multipass.sh

mv k3s.yaml $HOME/
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
sed -i "s/127.0.0.1/$IP/" $HOME/k3s.yaml
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

Further reading:

* [Learn about Kubernetes key concepts](https://kubernetes.io/docs/concepts/)
* [k3s quick start guide](https://rancher.com/docs/k3s/latest/en/quick-start/)

## Configuring an Ingress Gateway

An ingress controller is required to ensure that traffic to our deployed services is reachable from the outside, more specifically, outside the cluster - be that from your LAN or even from the Internet.

We will be using an `nginx` ingress controller for kubernetes and you can find more detailed information [from the official installation documentation](https://kubernetes.github.io/ingress-nginx/deploy/).

We will follow the `bare metal` [installation steps](https://kubernetes.github.io/ingress-nginx/deploy/#bare-metal) for our cluster. Run the following command:

```shell
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v0.47.0/deploy/static/provider/baremetal/deploy.yaml
```

You should see the following output:

```text
namespace/ingress-nginx created
serviceaccount/ingress-nginx created
configmap/ingress-nginx-controller created
clusterrole.rbac.authorization.k8s.io/ingress-nginx created
clusterrolebinding.rbac.authorization.k8s.io/ingress-nginx created
role.rbac.authorization.k8s.io/ingress-nginx created
rolebinding.rbac.authorization.k8s.io/ingress-nginx created
service/ingress-nginx-controller-admission created
service/ingress-nginx-controller created
deployment.apps/ingress-nginx-controller created
validatingwebhookconfiguration.admissionregistration.k8s.io/ingress-nginx-admission created
serviceaccount/ingress-nginx-admission created
clusterrole.rbac.authorization.k8s.io/ingress-nginx-admission created
clusterrolebinding.rbac.authorization.k8s.io/ingress-nginx-admission created
role.rbac.authorization.k8s.io/ingress-nginx-admission created
rolebinding.rbac.authorization.k8s.io/ingress-nginx-admission created
job.batch/ingress-nginx-admission-create created
job.batch/ingress-nginx-admission-patch created
```

According to the documentation, this process may take several minutes. You can issue the following command to watch the progress:

```shell
kubectl get pods -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx --watch
```

Once you see something like `ingress-nginx-controller-55bc4f5576-v774x   1/1   Running    0   100s`, the ingress controller should be ready.

To test, run the following commands:

```shell
export POD_NAMESPACE=ingress-nginx

export POD_NAME=$(kubectl get pods -n $POD_NAMESPACE -l app.kubernetes.io/name=ingress-nginx --field-selector=status.phase=Running -o jsonpath='{.items[0].metadata.name}')

kubectl exec -it $POD_NAME -n $POD_NAMESPACE -- /nginx-ingress-controller --version
```

Our output should look something like this:

```shell
-------------------------------------------------------------------------------
NGINX Ingress controller
  Release:       v0.46.0
  Build:         6348dde672588d5495f70ec77257c230dc8da134
  Repository:    https://github.com/kubernetes/ingress-nginx
  nginx version: nginx/1.19.6

-------------------------------------------------------------------------------
```

## Namespaces

It's always a good idea to work within the context of a namespace. It helps organize workloads and also makes it easier to define security policies and other context specific configuration options.

For our initial tests, we will use the namespace `pocs` (short for `proof of concepts`). This is a namespace I often use to just play around or test things in a cluster without affecting anything else in the cluster. I keep this naming convention for this guide series as well for my own convenience, but you could obviously use any name you wish.

Run the following command to create a namespace:

```shell
kubectl create namespace pocs
```

The output you should expect:

```text
namespace/pocs created
```

To list all your cluster's namespaces run:

```shell
kubectl get namespaces
```

Output:

```text
NAME              STATUS   AGE
default           Active   60m
kube-system       Active   60m
kube-public       Active   60m
kube-node-lease   Active   60m
ingress-nginx     Active   10m
pocs              Active   31s
```

To set your new `pocs` namespace as the default for all subsequent `kubectl` commands, run the following:

```shell
kubectl config set-context --current --namespace=pocs
```

Output:

```text
Context "default" modified.
```

If you now run something like `kubectl get all` you should see the following output:

```text
No resources found in pocs namespace.
```

If you ever want to run `kubectl` once off for another namespace, you can always just append the namespace parameter:

```shell
kubectl get all --namespace ingress-nginx
```

Output:

```text
NAME                                            READY   STATUS      RESTARTS   AGE
pod/ingress-nginx-admission-create-ktgdf        0/1     Completed   0          15m
pod/ingress-nginx-admission-patch-sdnlb         0/1     Completed   0          15m
pod/ingress-nginx-controller-55bc4f5576-v774x   1/1     Running     0          15m

NAME                                         TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)                      AGE
service/ingress-nginx-controller-admission   ClusterIP   10.43.144.32   <none>        443/TCP                      15m
service/ingress-nginx-controller             NodePort    10.43.88.40    <none>        80:31022/TCP,443:30530/TCP   15m

NAME                                       READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/ingress-nginx-controller   1/1     1            1           15m

NAME                                                  DESIRED   CURRENT   READY   AGE
replicaset.apps/ingress-nginx-controller-55bc4f5576   1         1         1       15m

NAME                                       COMPLETIONS   DURATION   AGE
job.batch/ingress-nginx-admission-create   1/1           22s        15m
job.batch/ingress-nginx-admission-patch    1/1           23s        15m
```