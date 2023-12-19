#:post{:id "redirect-domain-to-subdomain"
       :order 11
       :page :blog
       :date "2023" ;; 09/11
       :title "Redirecting Domain to Subdomain using AWS ALB+NLB"
       :css-class "blog-redirect-domain"
       :tags ["AWS" "Load Balancers" "DNS" "GoDaddy"]
       :image #:image{:src "/assets/loic-blog-logo.png"
                      :src-dark "/assets/loic-blog-logo.png"
                      :alt "Logo referencing Aperture Science"}}
+++
+++
## Context

My goal was to redirect the domain `flybot.sg` to the subdomain `www.flybot.sg`.

The domain and subdomain are resolved with GoDaddy.

The app is deployed in an AWS EC2 which resides behind an ALB (Application Load Balancer).

We can map www.flybot.sg to the ALB DNS name with a `CNAME` record.

**However, we cannot map flybot.sg (the naked domain) to the ALB because we cannot use CNAME for the domain, only A records are valid.**

## GoDaddy forwarding

GoDaddy provides a way to redirect from domain to subdomain which is great news.

However, as of 2018, it cannot redirect paths:

- [flybot.sg](http://flybot.sg/) -> [www.flybot.sg](http://www.flybot.sg/) (OK)
- [flybot.sg/blog](http://flybot.sg/blog) -> [www.flybot.sg/blog](http://www.flybot.sg/blog) (error 404)

Therefore, this simple solution is not viable.

## Using AWS route53 as Name Server

**Since the ALB has `dynamic` IPs, we cannot use it in our goDaddy `A` record for `flybot.sg`.**

One solution is to use AWS route53 because AWS added the possibility to **register the ALB DNS name in a special ALIAS record**.

So we could add NS records in GoDaddy to specify that for the domain `flybot.sg`, we let AWS handle it. However, we cannot add NS records for the `domain`, only for `subdomain`. The only way to make sure the domain is handled by AWS is to change the `default Name Servers` in our GoDaddy DNS.

This would work, however, since we change the **default** Name Servers, all the subdomains will also be handle by AWS, so we are basically letting AWS handle all our subdomains which is not what we wanted.

### Note

If we wanted to let AWS handles a subdomain such as `test.flybot.sg` for instance, that would be totally possible without affecting the other subdomains (and the domain), because we can add NS records for subdomain to specify what Name Servers to use. The problem arises when we deal with the naked domain.

## ALB+NLB

The solution I chose was to add a Network Load Balancer (NLB) in front of the ALB. The NLB can provide a **static** IP so we can resolve our @ A record to the NLB subnet static IP.

Adding an ALB as target of NLB is a recent [feature](https://aws.amazon.com/blogs/networking-and-content-delivery/using-aws-lambda-to-enable-static-ip-addresses-for-application-load-balancers/) in AWS that allows us to combine the strength of both LBs.

### ALB

The internal ALB purposes:

- redirect naked domain (flybot.sg) to sub domain (www.flybot.sg)
- redirect http to https using the SSL certificates from ACM

Amazon Certificate Manager allows us to requests certificates for `www.flybot.sg` and `flybot.sg` and attach them to the ALB rules to perform path redirection in our case. This is convenient as we do not need to install any ssl certificates or handle any redirects in the instance directly or change the code base.

### NLB

ALB works at level 7 but NLB works at level 4.

Thus, we have for the NLB:

- TCP rule that forwards request to ALB on port 80 (for http)
- TCP rules that forwards request on port 443 (for https)

### Target group

The target group is where the traffic from the load balancers is sent. We have 3 target groups.

The first target group contains the EC2 instance in which the ALB forward request.

The second target group contains the ALB with the protocol TCP 80 in which the NLB forward http requests.

The third target group contains the ALB with the protocol TCP 443 in which the NLB forward https request.

### DNS records

Since the ELB is the internet-facing entry points, we use a `CNAME` record for `www` resolving to the ELB DNS name

For the root domain `@`, we use an `A` record resolving to the static IP of the NLB (for the AZ where the EC2 resides).

### Trade-off

The trade-off on adding an extra load balancer is the cost. Once the free-tier period is over, the minimum cost for a load balancer is $18 per month.

## Conclusion

GoDaddy domain to subdomain forwarding does not support path so it is not viable at all.

Using AWS route53 to enjoy the feature of mapping a domain to the ALB DNS name via the special record ALIAS comes at a cost: all subdomains would need to be resolved using AWS Name Servers.

ALB+NLB is the setup that worked well for me. Having the internal ALB handling redirect to https and to the subdomain is very convenient. Using an internet-facing NLB solves the static IP problem to resolve the domain record at an extra cost of minimum $18 per month.
