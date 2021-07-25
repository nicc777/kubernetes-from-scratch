# Quick Catch-Up

- [Quick Catch-Up](#quick-catch-up)
  - [Reset](#reset)

This guide will quickly catch you up with each chapter.

For each chapter you will find:

* How to run all the commands required to see the chapter successfully through
* When at the end of the chapter, how to roll back to the beginning of that chapter

To start with, however, a quick couple of commands to "reset" short of a complete re-installation. There are also directly after, all the commands to run to ensure you have all required software installed to start with chapter 01.

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
docker container rm $CONTAINER_NAME
```

### Chapter 03

TODO 

### Chapter 04

TODO 

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

TODO 

### Chapter 03

TODO 

### Chapter 04

TODO 

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

