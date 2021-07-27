#!/bin/sh

if [ -z "$GIT_PROJECT_DIR" ]
then
      echo "\$GIT_PROJECT_DIR is empty"
      exit
else
      echo "Proceeding with cluster reset"
fi

cd $GIT_PROJECT_DIR

multipass stop --all

multipass delete --all --purge

sh $GIT_PROJECT_DIR/kubernetes-from-scratch/chapter_04/k3s-multipass.sh

export KUBECONFIG=$HOME/k3s.yaml

kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v0.47.0/deploy/static/provider/baremetal/deploy.yaml

kubectl create namespace pocs

cat /etc/hosts | egrep -vi "node1|node2|node3" > $HOME/etc_hosts_backup
cp $HOME/etc_hosts_backup $HOME/etc_hosts_modified
IP_NODE1=$(multipass info node1 | grep IPv4 | awk '{print $2}')
IP_NODE2=$(multipass info node2 | grep IPv4 | awk '{print $2}')
IP_NODE3=$(multipass info node3 | grep IPv4 | awk '{print $2}')

echo "${IP_NODE1} node1" >> $HOME/etc_hosts_modified
echo "${IP_NODE2} node2" >> $HOME/etc_hosts_modified
echo "${IP_NODE3} node3" >> $HOME/etc_hosts_modified

sudo cp $HOME/etc_hosts_modified /etc/hosts
