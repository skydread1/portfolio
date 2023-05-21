#:post{:id "host-clojure-fullstack-in-aws"
       :order 4
       :page :blog
       :title "Host Clojure Full Stack on AWS"
       :css-class "clojure-aws"
       :creation-date "20/01/2023"
       :show-dates? true}
+++
# How to deploy full stack Clojure website to AWS

This is an example of how to deploy a containerized full-stack Clojure app in AWS EC2.

I will use the [flybot.sg website](https://github.com/skydread1/flybot.sg) as example of app to deploy.

## Prerequisites

- Use an external DNS manager such as goDaddy for instance
- The app does not handle SSL and domain/protocols redirect
- The app used `datalevin` as embedded database which resides alongside the Clojure code inside a container
- The app is an open-source mono-repo and hosted on my GitHub
- We use ALB for redirects and certificates validations and ELB for static IP entry point.

## Use Jibbit to push to ECR

Instead of using datomic pro and having the burden to have a separate containers for the app and transactor, we decided to use [juji-io/datalevin](https://github.com/juji-io/datalevin) and its embedded storage on disk. Thus, we only need to deploy one container with the app.

To do so, we can use the library [atomisthq/jibbit](https://github.com/atomisthq/jibbit) baed on [GoogleContainerTools/jib](https://github.com/GoogleContainerTools/jib) (Build container images for Java applications).

It does not use docker to generate the image, so there is no need to have docker installed to generate images.

[jibbit](https://github.com/atomisthq/jibbit) can be added as `alias` in deps.edn:

```clojure
:jib
  {:deps {io.github.atomisthq/jibbit {:git/tag "v0.1.14" :git/sha "ca4f7d3"}}
   :ns-default jibbit.core
   :ns-aliases {jib jibbit.core}}
```

The `jib.edn` can be added in the project root with the configs to generate and push the image.

### Testing the app image locally

Example of jibbit config to just create a local docker image:

```clojure
;; example to create an docker image to be run with docker locally
{:main         clj.flybot.core
 :aliases      [:jvm-base]
 :user          "root"
 :group         "root"
 :base-image   {:image-name "openjdk:11-slim-buster"
                :type       :registry}
 :target-image {:image-name "flybot/image:test"
                :type       :docker}}
```

Then we can run the container:
```
docker run \
--rm \
-it \
-p 8123:8123 \
-v db-v2:/datalevin/dev/flybotdb \
-e OAUTH2="secret" \
-e ADMIN_USER="secret" \
-e SYSTEM="{:http-port 8123, :db-uri \"datalevin/dev/flybotdb\", :oauth2-callback \"http://localhost:8123/oauth/google/callback\"}" \
flybot/image:test
```

### AWS profile for CI

[jibbit](https://github.com/atomisthq/jibbit) can also read your local AWS credentials to directly push the generated image to your ECR (Elastic Container Registry).

You need to have aws cli installed (v2 or v1) and you need an env variable `$ECR_REPO` setup with the ECR repo string.

You have several [possibilities](https://github.com/atomisthq/jibbit/blob/main/src/jibbit/aws_ecr.clj) to provide credentials to login to your AWS ECR.

Here is the `jib.edn` for the CI:

```clojure
{:main           clj.flybot.core
 :target-image {:image-name "$ECR_REPO"
                :type       :registry
                :authorizer {:fn   jibbit.aws-ecr/ecr-auth
                             :args {:type         :profile
                                    :profile-name "flybot"
                                    :region       "region"}}}}
```

### ENV variables

I used [repository secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets) to handle AWS credentials on the GitHub repo:

- `AWS_ACCESS_KEY_ID` (must be named like that)
- `AWS_SECRET_ACCESS_KEY` (must be named like that)
- `ECR_REPO`

## AWS EC2

This [article](https://medium.com/appgambit/part-1-running-docker-on-aws-ec2-cbcf0ec7c3f8) explained quite well how to setup docker in EC2 and pull image from ECR.

### IAM policy and role, Security group

The UserData to install docker at first launch of the EC2 instance is the following:

```bash
#! /bin/sh
# For Amazon linux 2022 (might differ in 2023 but the principle remains)
yum update -y
amazon-linux-extras install docker
service docker start
usermod -a -G docker ec2-user
chkconfig docker on
```

To allow the EC2 to pull from ECR we need to add an `IAM policy` and `IAM role`.

Let’s first create the policy `flybot-ECR-repo-access` :

```bash
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "ListImagesInRepository",
            "Effect": "Allow",
            "Action": [
                "ecr:ListImages"
            ],
            "Resource": "arn:aws:ecr:region:acc:repository/flybot-website"
        },
        {
            "Sid": "GetAuthorizationToken",
            "Effect": "Allow",
            "Action": [
                "ecr:GetAuthorizationToken"
            ],
            "Resource": "*"
        },
        {
            "Sid": "ManageRepositoryContents",
            "Effect": "Allow",
            "Action": [
                "ecr:BatchCheckLayerAvailability",
                "ecr:GetDownloadUrlForLayer",
                "ecr:GetRepositoryPolicy",
                "ecr:DescribeRepositories",
                "ecr:ListImages",
                "ecr:DescribeImages",
                "ecr:BatchGetImage",
                "ecr:InitiateLayerUpload",
                "ecr:UploadLayerPart",
                "ecr:CompleteLayerUpload",
                "ecr:PutImage"
            ],
            "Resource": "arn:aws:ecr:region:acc:repository/flybot-website"
        }
    ]
}
```

We then attached the policy `flybot-ECR-repo-access` to a role `flybot-ECR-repo-access-role`

Finally, we attach the role `flybot-ECR-repo-access-role` to our EC2 instance.

We also need a `security group` to allow http(s) request and open our port 8123 for our [aleph](https://github.com/clj-commons/aleph) server.

We attached this SG to the EC2 instance as well.

### Run docker on EC2 instance and pull image from ECR

Then inside the EC2 instance, we can pull the image from ECR and run it:

```bash
# Login to ECR, this command will return a token
aws ecr get-login-password \
--region region \
| docker login \
--username AWS \
--password-stdin acc.dkr.ecr.region.amazonaws.com

# Pull image
docker pull acc.dkr.ecr.region.amazonaws.com/flybot-website:test

# Run image
docker run \
--rm \
-d \
-p 8123:8123 \
-v db-volume:/datalevin/prod/flybotdb \
-e OAUTH2="secret" \
-e ADMIN_USER="secret" \
-e SYSTEM="{:http-port 8123, :db-uri \"/datalevin/prod/flybotdb\", :oauth2-callback \"https://www.flybot.sg/oauth/google/callback\"}" \
acc.dkr.ecr.region.amazonaws.com/flybot-website:test
```

## Load Balancers

Even if we have one single EC2 instance running, there are several benefits we can get from AWS load balancers.

In our case, we have an Application Load Balancer (ALB) as target of a Network Load Balancer (NLB). Easily adding an ALB as target of NLB is a recent [feature](https://aws.amazon.com/blogs/networking-and-content-delivery/using-aws-lambda-to-enable-static-ip-addresses-for-application-load-balancers/) in AWS that allows us to combine the strength of both LBs.

### ALB

The internal ALB purposes:

- redirect naked domain (flybot.sg) to sub domain (www.flybot.sg)
- redirect http to https using the SSL certificates from AWS Certificate Manager (`ACM`)

ACM allows us to requests certificates for `www.flybot.sg` and `flybot.sg` and attach them to the ALB rules to perform path redirection in our case. This is convenient as we do not need to install any ssl certificates or handle any redirects in the instance directly or change the code base.

### NLB

Since the ALB has dynamic IPs, we cannot use it in our goDaddy `A` record for `flybot.sg`. One solution is to use AWS route53 because AWS added the possibility to register the ALB DNS name in a A record (which is not possible with external DNS managers). However, we already use goDaddy as DNS host and we don’t want to depend on route53 for that.

Another solution is to place an internet-facing NLB behind the ALB because NLB provides static IP.

ALB works at level 7 but NLB works at level 4.

Thus, we have for the NLB:

- TCP rule that forwards request to ALB on port 80 (for http)
- TCP rules that forwards request on port 443 (for https)

### Target group

The target group is where the traffic from the load balancers is sent. We have 3 target groups.

- The first target group contains the EC2 instance in which the ALB forward request.

- The second target group contains the ALB with the protocol TCP 80 in which the NLB forward http requests.

- The third target group contains the ALB with the protocol TCP 443 in which the NLB forward https request.

### DNS records

Since the ELB is the internet-facing entry points, we use a `CNAME` record for `www` resolving to the ELB DNS name.

For the root domain `flybot.sg`, we use a `A` record for `@` resolving to the static IP of the ELB (for the AZ where the EC2 resides).

## Learn More

You can have a look at the open-source repo: [skydread1/flybot.sg](https://github.com/skydread1/flybot.sg)



