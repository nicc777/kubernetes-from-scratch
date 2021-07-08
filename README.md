# kubernetes-from-scratch

Trying a new approach to document a from-scratch guide to a production ready Kubernetes cluster

This is a re-think of my previous repo called [_learning-kubernetes-basics_](https://github.com/nicc777/learning-kubernetes-basics), which is still online, but I will probably decommission it in the future.

Looking at how many others have organized their book or course git repositories, I opted to device the documentation in chapters, with each chapter covering or focusing on a specific outcome. There will also be specific branches as required for demonstrating certain features, for example how a tool may monitor for Pull Requests on a branch to trigger certain events. 

In reality, the kubernetes playground may need to be refreshed from time-to-time to ensure a consistent experience in order to follow the examples. To make this easier, I will add cluster rebuild sections between chapters where it is required.

I am currently working on Spring Boot applications, so the back-end code examples will be mostly Java based. There may also be some Python code from time-to-time and any front-end application will be based on ReactJS. 

Therefore, the current _chapters_ are planned:

| Chapter #                    | Focus area and outcome                                                                                         | Status      |
|:----------------------------:|----------------------------------------------------------------------------------------------------------------|:-----------:|
| [01](./chapter_01/README.md) | Introduction, assumptions, tooling and pre-requisites                                                          | Ready       |
| [02](./chapter_02/README.md) | Intro to Docker and installing it on Ubuntu Linux                                                              | Ready       |
| [03](./chapter_03/README.md) | Intro to preparing a Spring Boot Application for Docker, and alignment to the 12-factor application principles | Ready       |
| [04](./chapter_04/README.md) | Install a Kubernetes cluster on Ubuntu Linux using Multipass as a virtual host management system               | Ready       |
| [05](./chapter_05/README.md) | Taking a basic 12-factor application to a Kubernetes cluster                                                   | Not started |
| [06](./chapter_06/README.md) | _Chapter 6 and beyond is in planning..._                                                                       | Not started |

## Status

Current status: Early development, not ready for general use or reference
