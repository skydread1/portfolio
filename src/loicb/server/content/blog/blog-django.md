#:post{:id "blog-django"
       :page :blog
       :date ["2023-05-27" "2023-11-12"]
       :repos [["Blog" "https://github.com/skydread1/blog"]]
       :articles [["Deploy Django Blog in AWS Beanstalk" "../blog/deploy-django-aws-beanstalk"]]
       :title "Tech Blog with Django"
       :css-class "blog-django"
       :tags ["Python" "Django" "Elastic Beanstalk" "RDS PostgreSQL" "S3" "AWS"]
       :image #:image{:src "/assets/loic-blog-logo.png"
                      :src-dark "/assets/loic-blog-logo.png"
                      :alt "Logo referencing Aperture Science"}}
+++
I developed a tech blog in **python** using the `Django` framework. Thus, it is Server-Side Rendered.

The blog was deployed on AWS `Beanstalk`, the static files were served from an AWS `S3` bucket, and the production data was stored in an AWS `RDS Postgres` database.

It used `HTMX` for the search bar.

After the AWS free tier expired, I moved the tech blog to the ClojureScript SPA.
+++
## Rational

I developed a tech blog in **python** using the `Django` framework. Thus, it is Server-Side Rendered.

The blog was deployed on AWS `Beanstalk`, the static files were served from an AWS `S3` bucket, and the production data was stored in an AWS `RDS Postgres` database.

It used `HTMX` for the search bar.

After the AWS free tier expired, I moved the tech blog to the ClojureScript SPA.

## Features

The different features of the` blog are the following:
- Users can create an account `and login/logout
- Logged-in users can create/edit/delete posts
- The posts are written in markdown with preview of what the post will look like before submission
- There is syntax highlighting for the code blocks
- The UI supports light/dark mode toggle
- Posts can be sorted in different categories (such as `clojure`, `python` for instance)
- Users can search a post using the search bar that display the matching posts as they type in the search bar.

## CI/CD

### Env variable

I used `django-environ` to handle env variables.

My settings are divided in 3 files `settings/common.clj`, `settings/dev.clj` and `settings/prod.clj`.

### Static files

The static files are stored in AWS S3 in production.

I use `Django-storages` as a storage backend system for my Django app. Django-storages provides various storage backends, including the one designed to work with AWS S3. It abstracts the process of interacting with different storage solutions, making it easier to switch between them if needed.

Then I use `boto3` which is the official AWS SDK for Python so I can programmatically interact with AWS S3.

Finally, I can run the `collectstatic` django command to gather and upload the static files to the S3 bucket

### Storage

In production, the data is stored in an AWS RDS PostgreSQL database.

The library `psycopg2-binary` allows my app to communicate with a PostgreSQL database.

For dev, I used the default SQLite configuration provided by Django.

### Run the server

In production, I use an AWS Elastic Beanstalk.

I use `gunicorn` as HTTP server.

To deploy new app versions, I rely on the `AWS CLI EB` so I just have to run `eb deploy` to deploy the new app version on the AWS beanstalk.

### Domain

The blog used to be hosted at `blog.loicb.dev`. I used a CNAME record to map my personal subdomain to the Application Load Balancer's DNS.

However I moved the blog content to my clojure SPA instead because after my AWS free tier expired, the monthly cost for hosting the blog was around $50 which was too much for a simple blog.

## Learn more

I wrote an article on how I deployed the Django app in AWS (link at the top of the page).

Have a look at the repo [README](https://github.com/skydread1/blog/blob/master/README.md) for more information.
