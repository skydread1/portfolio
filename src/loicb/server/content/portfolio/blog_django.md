#:post{:id "blog-django"
       :order 5
       :page :portfolio
       :date "2023"
       :articles [["Deploy Django Blog in AWS Beanstalk" "https://blog.loicblanchard.me/post/8"]]
       :title "Tech Blog with Django"
       :css-class "blog-django"
       :tags ["Python" "Django" "Elastic Beanstalk" "RDS PostgreSQL" "S3" "AWS"]
       :image #:image{:src "/assets/loic-blog-logo.png"
                             :src-dark "/assets/loic-blog-logo.png"
                             :alt "Logo referencing Aperture Science"}}
+++
I developed my tech blog in python using the Django framework. It is then Server-Side Rendered.

It has a `view` mode (for visitors) and `editor`` mode once logged in.

The blog is deployed on AWS Beanstalk, used AWS S3 to serve static files and AWS RDS Postgres to store data.
+++
## Coming soon...