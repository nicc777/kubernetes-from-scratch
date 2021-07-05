# Chapter 02 - Intro to Docker and installing it on Ubuntu Linux

- [Chapter 02 - Intro to Docker and installing it on Ubuntu Linux](#chapter-02---intro-to-docker-and-installing-it-on-ubuntu-linux)
  - [Light Introduction and Getting Started Overview](#light-introduction-and-getting-started-overview)
  - [Preparations: Creating a Network File Share using NFS](#preparations-creating-a-network-file-share-using-nfs)
  - [Testing Out Docker - Get an Image and Run It](#testing-out-docker---get-an-image-and-run-it)

## Light Introduction and Getting Started Overview

To be honest, the [Docker online documentation](https://docs.docker.com/engine/install/ubuntu/) for installing Docker on Ubuntu is fairly comprehensive and any other effort would probably fall short of these excellent instructions.

It is therefore assumed that Ubuntu users have followed these instructions. Other operating system users will also find [excellent documentation](https://docs.docker.com/engine/install/) on the Docker website.

For the purpose of this guide, it is also assumed you have created at least a free [Docker Hub](https://hub.docker.com/) account and that you managed to login using the docker command line:

```shell
docker login
```

Unfortunately Docker has recently removed certain features from free accounts, but that should not be a problem. Keep in mind that in order to create multiple images on the free account, the bulk of them would need to be public, but again, that should not be a problem for this particular guide.

Another option is to use a private Docker registry, but that can be challenging in terms of getting the TLS certificate set-up, especially if you do not own a domain. Therefore, this will not be covered in this guide.

A final option is to use another third-party hosted Docker registry and almost all the major public cloud service providers offer a solution:

* [AWS Container Registry](https://aws.amazon.com/ecr/)
* [Microsoft Azure Container Registry](https://azure.microsoft.com/en-us/services/container-registry/)

The commands to use these registries are not that different from just using the Docker Hub default registry, so you are more than welcome to use on of these alternative public cloud registries.

## Preparations: Creating a Network File Share using NFS

For this test, we will be running the PostgreSQL Database Image from the [official PostgreSQL Repo](https://hub.docker.com/_/postgres)

On the PostgreSQL Docker Hub landing page is a number of examples. In order for us to build up to a practical future exercise, we will be using a network filesystem volume for persistent storage. In order to do that, we first need to setup a NFS server on our Ubuntu system:

```shell
sudo mkdir -p /data/kubernetes_from_scratch_nfs_persistence
sudo chmod 777 /data/kubernetes_from_scratch_nfs_persistence
sudo apt install -y nfs-kernel-server
```

The contents of `/etc/default/nfs-kernel-server` (comments removed):

```text
RPCNFSDCOUNT=8
RPCNFSDPRIORITY=0
RPCMOUNTDOPTS="--manage-gids"
NEED_SVCGSSD="no"
RPCSVCGSSDOPTS=""
```

The contents of `/etc/idmapd.conf` (comments removed):

```text
[General]
Verbosity = 0
Pipefs-Directory = /run/rpc_pipefs

[Mapping]
Nobody-User = nobody
Nobody-Group = nogroup
```

The content of `/etc/exports`:

```text
/data/kubernetes_from_scratch_nfs_persistence 192.168.0.0/24(rw,fsid=0,insecure,no_subtree_check,async)
```

_*Note*_: You may need to change the Private IP Address range to suite your needs. Also note that later we will need to also add our Kubernetes cluster IP range to this file. For now this is good enough for testing.

Next, restart the NFS server:

```shell
sudo service nfs-kernel-server restart
```

In order to test, it would be best if you could use a separate Linux system, but if none is available to you, you could also run the following commands on your current Ubuntu system. Before running, I assume you have your NFS Server IP address exported to the `$NFS_IP` environment variable and your username is exported to `$LOCAL_USERNAME`

```shell
sudo apt install nfs-common
mkdir ~/test-mount
sudo mount -t nfs -o proto=tcp,port=2049 $NFS_IP:/ /home/$LOCAL_USERNAME/test-mount
```

To verify everything worked, run the following on the client:

```shell
mount | grep test-mount                                                                                                                                                                                             
```

The output might look something like this:

```text
192.168.0.xxx:/ on /home/xxxx/test-mount type nfs4 (rw,relatime,vers=4.0,rsize=1048576,wsize=1048576,namlen=255,hard,proto=tcp,timeo=600,retrans=2,sec=sys,clientaddr=xxx.xxx.xxx.xxx,local_lock=none,addr=192.168.0.xxx)
```

As a final test create a file on the filesystem mounted on the client:

```shell
echo TEST > ~/test-mount/test.txt
cat ~/test-mount/test.txt
```

And check also on the server:

```shell
cat /data/kubernetes_from_scratch_nfs_persistence/test.txt
```

Both `cat` command should give you the following output:

```text
TEST
```

NFS References:

* [Ubuntu NFS Guide](https://help.ubuntu.com/community/SettingUpNFSHowTo)


## Testing Out Docker - Get an Image and Run It

TODO
