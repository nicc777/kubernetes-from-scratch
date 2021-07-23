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
| [05](./chapter_05/README.md) | Taking a basic 12-factor application to a Kubernetes cluster                                                   | Ready       |
| [06](./chapter_06/README.md) | External Load Balancer                                                                                         | Ready       |
| [07](./chapter_07/README.md) | Source Control and Automated Build (Continuous Integration) with GitHub and GitHub Actions                     | Ready       |
| [08](./chapter_08/README.md) | Managing multiple versions of the same service in Kubernetes                                                   | Ready       |
| [09](./chapter_09/README.md) | HELM Charts and package management for Kubernetes                                                              | In Progress |
| [10](./chapter_10/README.md) | GitOps and managing deployments with Argo                                                                      | Not started |
| [11](./chapter_11/README.md) | A more complex application example: a solutions for recording telemetry from various devices                   | Not started |
| [12](./chapter_12/README.md) | Using Keycloak for authentication & authorization. Introduction to Oauth2 and OIDC                             | Not started |
| [13](./chapter_13/README.md) | How to validate tokens on the Kong API Gateway                                                                 | Not started |
| [14](./chapter_14/README.md) | Deploying RabbitMQ for a highly available messaging service                                                    | Not started |
| [15](./chapter_15/README.md) | Kubernetes Secrets Management                                                                                  | Not started |
| [16](./chapter_16/README.md) | Putting it all together: Packaging and deploying our telemetry recording service                               | Not started |
| [17](./chapter_17/README.md) | _Chapter 17 and beyond is in planning..._                                                                      | Not started |

## Status

Current status: Early development, not ready for general use or reference
