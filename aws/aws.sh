#!/bin/bash

# Example calls:
#   sh aws.sh upload ami-lookup.yaml
#   sh aws.sh --task upload (uploads deckman.yaml to s3 bucket)
#   sh aws.sh validate ami-lookup.yaml
#   sh aws.sh validate (validates all yaml files to be found)
#   sh aws.sh create
#   sh aws.sh update
#   sh aws.sh refresh --configset configset1

# Initialize all local variables by processing the named args (task can be named or the only un-named arg)
init() {
  while (( $# )) ; do
    case $1 in
      -t|--task)
        shift
        local temp=$1
        ;;
      -c|--configset)
        shift
        configset=$1
        ;;
      -b|--bucketname)
        shift
        bucketname=$1
        ;;
      -s|--stackname)
        shift
        stackname=$1
        ;;
      -e|--ec2name)
        shift
        ec2name=$1
        ;;
      *)
        temp=$1
        ;;
    esac
    shift
    if [ -n "$temp" ] ; then
      task=$temp
    fi
    if [ -z "$task" ] ; then
      echo "Task parameter is missing!"
      exit 1
    fi
  done

  rootdir=/c/myTechnicalStuff/projects/java/projects/eclipse/workspace/Deckman/aws
  [ -z "$stackname" ] && stackname=deckman-website
  [ -z "$ec2name" ] && ec2name=deckman-ec2-instance
  [ -z "$bucketname" ] && bucketname=deckman-cloudformation
  bucketpath=s3://$bucketname
  buckethttp=https://s3.amazonaws.com/$bucketname

  cat <<EOF
    task=$task
    configset=$configset    
    rootdir=$rootdir
    bucketname=$bucketname
    bucketpath=$bucketpath
    buckethttp=$buckethttp
    stackname=$stackname
    ec2name=$ec2name
EOF
}

upload() {
  if [ $1 ] ; then
    aws \
      s3 cp \
      $rootdir/$1 \
      $bucketpath/$1
  else
    aws \
      s3 cp \
      $rootdir \
      $bucketpath \
      --exclude=* \
      --include=*.template \
      --recursive
  fi
}

validate() {
  local here="$(pwd)"
  cd $rootdir
  find . -type f -iname '*.template' | \
  while read line; do \
    local f=$(printf "$line" | sed 's/^.\///'); \
    [ -n "$1" ] && [ "$1" != "$f" ] && continue; \
    printf $f; \
    aws cloudformation validate-template --template-body "file://./$f"
    echo " "
  done
  cd $here
}

create() {
  stackaction "create-stack"
}

update() {
  stackaction "update-stack"
}

stackaction() {
  local template=$rootdir/deckman.yaml
  if [ $# == 1 ] ; then

    local action=$1

    upload $template

    local parm1="ParameterKey=EC2InstanceType,ParameterValue=t2.medium"
    local parm2="ParameterKey=S3ConfigBucketName,ParameterValue=deckman-cloudformation"
    local parm3="ParameterKey=S3WebsiteBucketName,ParameterValue=deckman-pictures"
    local parms="$parm1 $parm2 $parm3"
    
    aws \
      cloudformation $action \
      --stack-name $stackname \
      --no-use-previous-template \
      --template-url $buckethttp/$template \
      --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM \
      --parameters $parms

    return 0
  fi
  echo "INVALID/MISSING stack action parameter required."
}

metaRefresh() {
  local instanceId=$(aws cloudformation describe-stack-resources \
    --stack-name $stackname \
    --logical-resource-id $ec2name \
    | jq '.StackResources[0].PhysicalResourceId' \
    | sed 's/"//g')

  echo "instanceId = $instanceId"

  if [ -z "$configset" ] ; then
    printf "Enter the name of the configset to run: "
    read configset
  fi
  # NOTE: The following does not seem to work properly:
  #       --parameters commands="/opt/aws/bin/cfn-init -v --configsets $configset --region "us-east-1" --stack "$stackname" --resource $ec2name"
  # Could be a windows thing, or could be a complexity of using bash to execute python over through SSM.
  aws ssm send-command \
    --instance-ids "${instanceId}" \
    --document-name "AWS-RunShellScript" \
    --comment "Implementing cloud formation metadata changes on ec2 instance $ec2name ($instanceId)" \
    --parameters \
    '{"commands":["/opt/aws/bin/cfn-init -v --configsets '"$configset"' --region us-east-1 --stack '"$stackname"' --resource $ec2name"]}'
}

init $@

case ${task,,} in
  init)     exit 0 ;;
  upload)   upload $@ ;;
  validate) validate $@ ;;
  create)   create $@ ;;
  update)   update $@ ;;
  refresh)  metaRefresh $@ ;; 
  *) echo "Unrecognized task \"$task\"" ;;
esac