# Chapter 08 - Managing multiple versions of the same service in Kubernetes

- [Chapter 08 - Managing multiple versions of the same service in Kubernetes](#chapter-08---managing-multiple-versions-of-the-same-service-in-kubernetes)
  - [Objectives for this Chapter](#objectives-for-this-chapter)
  - [Starting on a Clean Slate](#starting-on-a-clean-slate)

## Objectives for this Chapter

Our main objective is to demonstrate how you could run two different versions of exactly the same application in a Kubernetes cluster (`v1` and `v2` of a service).

But why is this a thing?

Well, by following [semantic versioning](https://semver.org/) principles, you may have noticed that any [major version updates](https://semver.org/#spec-item-8) are used for changes where the applications public API's changes in a way that may break other application depending on these API's.

But, not all dependant applications may be able to change at the same pace, and it is therefore important to have a strategy which will allow you to support multiple versions of your applications (major versions), for a period of time in order to give other applications time to update.

The actual time period depends on a number of factors. If it's only internal application within a company, one or two months may be considered sufficient time and the time is more flexible depending on various internal factors to that company. However, if you have public facing API's or API's to which other organizations integrate with, you may have to consider a lot longer time period - say three, six or even twelve months. You can see an example of this approach in [this MS Azure deprecation notice](https://azure.microsoft.com/en-us/updates/azure-api-management-update-oct-18/). Microsoft generally provides about one year before removing an older version, unless there is a critical security issue which may prompt a shorter time (typically one month).

Since changes is a reality of life, it is good to learn early on how to deal with such changes and what it involves from a Kubernetes perspective.

## Starting on a Clean Slate

_*Note*_: Make sure you are in the `pocs` namespace.

For this chapter, we first need to clean out our current running services in Kubernetes. You can do this with the following command:

```shell
kubectl delete ingress conversions-ingress ; kubectl delete service conversions-service; kubectl delete deployment conversions-deployment

kubectl delete ingress conversions-ingress-v1 ; kubectl delete service conversions-service-v1 ; kubectl delete deployment conversions-deployment-v1
```

When you now run `kubectl get all` the output should be `No resources found in pocs namespace.`

TODO - wip
