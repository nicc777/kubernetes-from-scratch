# Quick Catch-Up

- [Quick Catch-Up](#quick-catch-up)
  - [Preparation before running any commands !!](#preparation-before-running-any-commands-)
  - [Reset](#reset)
    - [Chapter 01](#chapter-01)
    - [Chapter 02](#chapter-02)
    - [Chapter 03](#chapter-03)
    - [Chapter 04](#chapter-04)
    - [Chapter 05](#chapter-05)
    - [Chapter 06](#chapter-06)
    - [Chapter 07](#chapter-07)
    - [Chapter 08](#chapter-08)
    - [Chapter 09](#chapter-09)
  - [Quick Catchup](#quick-catchup)
    - [Chapter 01](#chapter-01-1)
    - [Chapter 02](#chapter-02-1)
    - [Chapter 03](#chapter-03-1)
    - [Chapter 04](#chapter-04-1)
    - [Chapter 05](#chapter-05-1)
    - [Chapter 06](#chapter-06-1)
    - [Chapter 07](#chapter-07-1)
    - [Chapter 08](#chapter-08-1)
    - [Chapter 09](#chapter-09-1)

This guide will quickly catch you up with each chapter.

For each chapter you will find:

* _Reset_ section: How to quickly rest everything in order to start the chapter
* _Quick Catchup_ section: How to run all the commands required to see the chapter successfully through

To start with, however, a quick couple of commands to "reset" short of a complete re-installation. There are also directly after, all the commands to run to ensure you have all required software installed to start with chapter 01.

## Preparation before running any commands !!

Start by clearing this cloned repository on your machine. Assuming your `$GIT_PROJECT_DIR` points to where you clone your git repositories - something like `$HOME\git` perhaps.

```shell
export GIT_PROJECT_DIR=$HOME/git

cd $GIT_PROJECT_DIR
```

## Reset

These commands can be run in each chapter to reset the environment to a pre chapter 01 state, assuming you are at the end of the chapter.

### Chapter 01

This guide assumes a linux system is installed. All tests were done on [Ubuntu 20.04 LTS](https://wiki.ubuntu.com/FocalFossa/ReleaseNotes)

To reset for this chapter, you can consider re-installing the OS.

### Chapter 02

Basic steps:

* Get all running containers 
* Stop all running containers
* Remove all containers
* Remove all images
* Stop Docker
* Uninstall Docker

```shell
docker container ls --all
```

Then, for each container and using the name under the `NAMES` column run (assuming the name is in `$CONTAINER_NAME`):

```shell
docker container stop $CONTAINER_NAME
```

Now, remove everything:

_*Note*_: The following will completely remove ALL your containers and completely clean your docker installation. If there is something you need to keep, either figure out how you can reset less destructively or first backup all important data.

```shell
docker rm -f $(docker ps -a -q)
docker system prune -a -f --volumes
sudo service docker stop
sudo apt remove -y docker.io
sudo apt-get purge -y docker.io
sudo apt -y autoremove
```

Finally, cleanup any data in the NFS mount:

```shell
sudo rm -frR /data/kubernetes_from_scratch_nfs_persistence/*
```

### Chapter 03

Start by clearing this cloned repository on your machine. Assuming your `$GIT_PROJECT_DIR` points to where you clone your git repositories - something like `$HOME\git` perhaps.

```shell
rm -frR kubernetes-from-scratch
```

### Chapter 04

_*Note*_: A quick way to reset and catch up to the end of chapter 04 is by running the command `sh $GIT_PROJECT_DIR/kubernetes-from-scratch/kubernetes_cluster_reset.sh`. You can then skip the rest of the commands below and follow the instructions printed on the terminal.

This reset will get a _fresh_ Kubernetes cluster up and running again.

_*Important*_: The following command will stop and purge ALL your multipass instances

```shell
multipass stop --all

multipass delete --all --purge

sh $GIT_PROJECT_DIR/kubernetes-from-scratch/chapter_04/k3s-multipass.sh

export KUBECONFIG=$HOME/k3s.yaml
```

Test:

```shell
kubectl cluster-info
```

### Chapter 05

TODO 

### Chapter 06

TODO 

### Chapter 07

TODO 

### Chapter 08

TODO 

### Chapter 09

TODO 

## Quick Catchup

Below are commands per chapter without any explanation to help you quickly catch-up to the end of that chapter

### Chapter 01

Basically just ensure that you have a test system. The recommended system as was used for testing in this guide:

* 4-Core, 2 threads per core CPU
* 32GN RAM
* more than 100 GiB free hard drive space
* A decent Operating System. This guide was tested on [Ubuntu 20.04 LTS](https://wiki.ubuntu.com/FocalFossa/ReleaseNotes) and should provide the best results, although technically almost any other modern Operating System should do the trick.
* Ensure you install Docker

Commands:

```shell
sudo apt update
sudo apt upgrade
```

If there were Kernel upgrades, consider rebooting

```shell
sudo apt-get install docker.io
```

For detailed installation instruction, refer to [the documentation](https://docs.docker.com/engine/install/ubuntu/)

### Chapter 02

Re-install docker:

```shell
sudo apt install -y docker.io
```

Mount the NFS volume:

```shell
export LOCAL_USERNAME=`whoami`

export NFS_IP=xxx.xxx.xxx.xxx
```

_*Note*_: Ensure you use the right IP address for `$NFS_IP` - it must be pointing to the NFS server LAN IP address

```shell
mkdir ~/test-mount

sudo mount -t nfs -o proto=tcp,port=2049 $NFS_IP:/ /home/$LOCAL_USERNAME/test-mount
```

Verify the mount was successful with the following commands, which should work without any errors:

```shell
echo test > /home/$LOCAL_USERNAME/test-mount/test.txt

cat /home/$LOCAL_USERNAME/test-mount/test.txt
```

Docker test:

```shell
mkdir /home/$LOCAL_USERNAME/test-mount/testdb1

chmod 777 /home/$LOCAL_USERNAME/test-mount/testdb1

docker run --name testdb1 \
-e POSTGRES_PASSWORD=mysecretpassword \
-e PGDATA=/var/lib/postgresql/data/pgdata \
-v /home/$LOCAL_USERNAME/test-mount/testdb1:/var/lib/postgresql/data \
-p 127.0.0.1:5432:5432 \
-d postgres
```

Test that you can log into the DB.

When done, you can stop and remove the test DB:

```shell
docker container stop testdb1

docker container rm testdb1

sudo rm -frR /home/$LOCAL_USERNAME/test-mount/testdb1/

sudo umount /home/$LOCAL_USERNAME/test-mount
```

### Chapter 03

Start by cloning this repo and changing into the source directory for the demo project:

```shell
git clone git@github.com:nicc777/kubernetes-from-scratch.git

cd $GIT_PROJECT_DIR/kubernetes-from-scratch/chapter_03/project_source_code/conversions
```

Build the package:

```shell
./mvnw clean && ./mvnw package
```

Build the docker image:

```shell
docker build -t conversions .
```

Test:

```shell
docker run --name conversions-app -p 8888:8888 conversions
```

To stop the container, press `CTRL+C`. Once again, it should take 5 seconds to stop.

Cleanup:

```shell
docker container rm conversions-app
```

### Chapter 04

_*Note*_: A quick way to reset and catch up to the end of chapter 04 is by running the command `sh $GIT_PROJECT_DIR/kubernetes-from-scratch/kubernetes_cluster_reset.sh`. You can then skip the rest of the commands below and follow the instructions printed on the terminal.

Run the following commands to fast forward to the end state of this chapter:

```shell
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v0.47.0/deploy/static/provider/baremetal/deploy.yaml
```

This process may take several minutes. You can issue the following command to watch the progress:

```shell
kubectl get pods -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx --watch
```

Once you see something like `ingress-nginx-controller-55bc4f5576-v774x   1/1   Running    0   100s`, the ingress controller should be ready.

Now run:

```shell
kubectl create namespace pocs

kubectl config set-context --current --namespace=pocs
```

### Chapter 05

TODO 

### Chapter 06

TODO 

### Chapter 07

TODO 

### Chapter 08

TODO 

### Chapter 09

TODO 

