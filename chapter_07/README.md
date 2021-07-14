# Chapter 07 - Source Control and Automated Build (Continuous Integration) with GitHub and GitHub Actions

- [Chapter 07 - Source Control and Automated Build (Continuous Integration) with GitHub and GitHub Actions](#chapter-07---source-control-and-automated-build-continuous-integration-with-github-and-github-actions)
  - [Objectives of This Chapter](#objectives-of-this-chapter)
  - [Setup a GitHub Repository](#setup-a-github-repository)
  - [Required Reading](#required-reading)

## Objectives of This Chapter

With the focus on 12-factor application development as well as taking into account the concepts around DevOps, this chapter will focus on GitHub and the services available to create a Java Spring Boot Project in GitHub and use GitHub actions to automate the build process in order to create artifacts such as a Maven repository with our builds and a container registry with our prepared Docker image.

Therefore, the primary objectives can be listed as:

* Creating a GitHub Project
* Create our application and configure various elements
* Setting up actions to build the application:
  * Save our build artifact in a GitHub Maven repository
  * Build and push our application Docker image to the GitHub container registry

Secondary objectives will include the following aspects:

* Develop unit tests that must pass a certain percentage in order for a build to succeed
* Discuss options around security scanning of our source code repository and artifacts

Further reading that may be helpful for this chapter:

* [What is DevOps? A guide to common methods and misconceptions](https://github.blog/2020-10-07-devops-definition/)
* [Getting started with DevOps automation](https://github.blog/2020-10-29-getting-started-with-devops-automation/)
* [DevOps development: top seven tips for faster application development](https://resources.github.com/whitepapers/Seven-Tips-for-Faster-Development/)

## Setup a GitHub Repository

I have created a demonstration project on GitHub called [java-conversions-app](https://github.com/nicc777/java-conversions-app). This project is used mainly to demonstrate all the features and discussion points of this chapter.

Below is a screenshot of how I created the project. You can either choose to attempt to do this from scratch, or you can just fork this project in order to have a copy available on your own account.

![GitHub create new project](github_new_project.png)

On your development system, you now need to clone the newly created repository. Refer to the [GitHub documentation](https://docs.github.com/en/github/creating-cloning-and-archiving-repositories/cloning-a-repository-from-github/cloning-a-repository) for more detailed instructions on how to accomplish this task.

_*Note*_: As chapters of this guide progress, the content of the repository may be changing. This chapter deals with the basics of getting the project started and the examples from file contents shown reflect the state in the initial setup of this repository. However, should you fork the repository, all the concepts refereed to in this chapter should still be relevant and working.

## Required Reading

At this stage, just before we delve into the detail, it is important that you are familiar with the content of the following GitHub documentation resources:

* [Introduction to GitHub Actions](https://docs.github.com/en/actions/learn-github-actions/introduction-to-github-actions)
* [About permissions for GitHub Packages](https://docs.github.com/en/packages/learn-github-packages/about-permissions-for-github-packages)
* [Configuring a package's access control and visibility](https://docs.github.com/en/packages/learn-github-packages/configuring-a-packages-access-control-and-visibility)
* [Encrypted secrets](https://docs.github.com/en/actions/reference/encrypted-secrets)
* [Working with the Apache Maven registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)
* [Publishing Java packages with Maven](https://docs.github.com/en/actions/guides/publishing-java-packages-with-maven)
* [Viewing packages](https://docs.github.com/en/packages/learn-github-packages/viewing-packages)
