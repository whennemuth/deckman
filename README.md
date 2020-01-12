## Deckman

Website originally created for thedeckmancapecod.com in 2008.
Being updated here to be built with maven and containerized to run as a webapp on a linux docker host.

#### Steps to install and run on AWS

1. **Install git to download this repository**
   [Git Download](https://git-scm.com/downloads)
   This will come with gitbash which will be needed to run bash commands if on a PC.

2. **Install and configure the AWS CLI**

   A lot of the steps to follow involve creating resources in your AWS account. Most of these can be done by logging into the AWS console with your browser and manually completing the steps. Instead, all the resource creation is going to be done on the command line, which requires installing the AWS command line interface (CLI): 

   1. Read and follow the directions on the [CLI Download page](https://docs.aws.amazon.com/cli/latest/userguide/install-windows.html)
      Or skip all the reading and download the MSI directly: [MSI Download](https://s3.amazonaws.com/aws-cli/AWSCLI64PY3.msi)

   2. Log into your AWS account [here](https://console.aws.amazon.com/console/home?region=us-east-1)

   3. Create an access key for yourself (root user) to be used later with the AWS CLI temporarily.
      Full instructions are here: [Creating Access Keys for the Root User](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_root-user.html#id_root-user_manage_add-key)

      1. Click your username from the top right of the window and select `"My Security Credentials"`

      2. Click `"Access keys (access key ID and secret access key)"`, followed by the `"Create New Access Key"` button

      3. In the popup, click "Show Access Key", copy key and ID values, and paste to a text editor somewhere temporarily. IMPORTANT: If you close the popup, these values cannot be retrieved and you must delete the Access key and repeat.

      4. Open up a gitbash window and execute the following command to put the access key values where the aws cli will find them *(NOTE: Replace the aws_access_key_id and aws_secret_access_key with the values you pasted for safekeeping earlier)*:

         ```
         mkdir ~/.aws && \
         cat <<EOF > ~/.aws/config
         [default]
         region=us-east-1
         aws_access_key_id=AKIAIOSFODNN7EXAMPLE
         aws_secret_access_key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
         EOF
         ```

      5. Test access as follows:

         ```
         aws sts get-caller-identity
         ```

         You should see a return value like this:

         ```
         {
             "UserId": "465278141405",
             "Account": "465278141405",
             "Arn": "arn:aws:iam::465278141405:root"
         }
         ```

         

3. **Upload Cloud-formation templates**
   Get the cloud-formation templates from this repository and upload them to an AWS S3 bucket.
   Open a gitbash console and run the following:

   ```
   # Download the 2 cloudformation templates from this repository:
   cd ~ && \
   curl https://raw.githubusercontent.com/whennemuth/deckman/master/aws/deckman.yaml > deckman.yaml && \
   curl https://raw.githubusercontent.com/whennemuth/deckman/master/aws/ami-lookup.yaml > ami-lookup.yaml
   
   # Or you could get them by cloning this git repo"
   cd ~ && \
   git clone https://github.com/whennemuth/deckman.git && \ 
   cd deckman/aws
   
   # Create the S3 bucket and upload the templates there.
   aws s3 mb s3://deckman-cloudformation && \
   aws s3 cp deckman.yaml s3://deckman-cloudformation/ && \
   aws s3 cp ami-lookup.yaml s3://deckman-cloudformation/
   ```
   
   
   
4. **Upload SSH key**

   When the ec2 instance is being created, it will need to reference a pre-existing ssh key for remote access to it. The default ec2-user will get the public key in its ~/.ssh directory so that you can shell in as this user using the private key. Create and upload it in this step as follows:
   Open a gitbash console and run the following:

   ```
   cd ~/.ssh
   ssh-keygen -b 2048 -t rsa -f deckman-ec2-rsa -q -N ""
   aws ec2 import-key-pair --key-name "deckman-ec2-rsa" --public-key-material file://~/.ssh/deckman-ec2-rsa.pub
   ```

   

5. **Run cloud-formation**
   RESUME NEXT: