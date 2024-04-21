#:post{:id "deploy-django-aws-beanstalk"
       :page :blog
       :date ["2023-08-08"]
       :title "Deploy Django Blog in AWS Beanstalk"
       :css-class "blog-django-aws"
       :tags ["Python" "Django" "AWS" "Elastic Beanstalk"]
       :image #:image{:src "/assets/loic-blog-logo.png"
                      :src-dark "/assets/loic-blog-logo.png"
                      :alt "Logo referencing Aperture Science"}}
+++
+++
## Context

My tech blog used to be deployed on AWS (during my AWS free tier period).

In this article, I am going to highlight the different libraries/settings I used to develop and deploy my Django app.

## Project Setup

You might be familiar with Django app setup but I will just recap the different common commands I ran in my case.

### Start python env

```bash
# create env
python -m venv blog_venv

# activate env (mac)
source blog_venv/bin/activate

```

### Install Django

```bash
pip install django
```

### Start Django project

```bash
django-admin startproject loicblog
```

### Migrations

Migrations are Django’s way of propagating changes you make to your models (adding a field, deleting a model, etc.) into your database schema. They’re designed to be mostly automatic, but you’ll need to know when to make migrations, when to run them, and the common problems you might run into.

```bash
# create new migrations based on the changes made to the models
python manage.py makemigrations

# apply and unapply migrations.
python manage.py migrate
```

### Run server

```bash
# be sure to migrate first
python manage.py runserver
```

### Admin

It is very common to have a superuser to handle admin tasks:

```bash
python manage.py createsuperuser --username=myname --email=me@gmail.com
```

## Start app

One project can have multiple apps such as a blog, an authentication system etc
So `loicblog` is the project and `blog` is one app inside the project.

```bash
python manage.py startapp blog

```

- Add the `blog` app to the INSTALLED_APPS array in the project `loicblog/common.py`.
- Add `path('', include('blog.urls'))` to the `urlpatterns` in `loic/blog/urls.py`.

## Blog Post Content in Markdown

I like writing my articles in Markdown with a preview button (like on GitHub for instance). This is how I write the articles in this blog. To do so, I used [django-markdownx](https://neutronx.github.io/django-markdownx/).

### django-markdownx

```bash
pip install markdown django-markdownx
```

- Then we need to add the `markdownx` app to the INSTALLED_APPS array in the project `loicblog/common.py`.
- Add the path to [urls.py](http://urls.py/): `path('markdownx/', include('markdownx.urls'))`.
- Collect MarkdownX assets to your STATIC_ROOT:

```bash
python manage.py collectstatic
```

### Code block syntax highlighting

By default the html code blocks `pre` do not have syntax highlighting. Since, this blog gives code examples in different programming languages, it is important to support syntax highlighting of the code blocks. I used [codehilite](https://python-markdown.github.io/extensions/code_hilite/) for that.

Adding `MARKDOWNX_MARKDOWN_EXTENSIONS = ['fenced_code', 'codehilite']` to the settings enable syntax highlighting.

`codehilite` required the [pygments](https://pygments.org/) package:

```bash
pip install pygments
```

## Env variables

### django-environ

A common python library to deal with ENV variable in django is [django-environ](https://django-environ.readthedocs.io/en/latest/)

```bash
## install
pip install django-environ
```

### Django project settings

I advise to separate your settings in multiple files instead of keeping one default `settings.py`.

I use 3 settings files: `common.py` , `dev.py` and `prod.py`. 

Here is the `common.py`:

```python
from pathlib import Path
import os

# Build paths inside the project like this: BASE_DIR / 'subdir'.
BASE_DIR = Path(__file__).resolve().parent.parent.parent

# Env variables

import environ
# Initialise environment variables
env = environ.Env()
base = environ.Path(__file__) - 3 # 3 folders back
environ.Env.read_env(env_file=base('.env'), overwrite=True) # reading .env file

# SECURITY WARNING: keep the secret key used in production secret!
SECRET_KEY = env('SECRET_KEY')

ALLOWED_HOSTS = []

# Application definition

INSTALLED_APPS = [
    "django.contrib.admin",
    "django.contrib.auth",
    "django.contrib.contenttypes",
    "django.contrib.sessions",
    "django.contrib.messages",
    "django.contrib.staticfiles",
    "blog",
    "members",
    "markdownx",
    "fontawesomefree",
]

MIDDLEWARE = [
    "django.middleware.security.SecurityMiddleware",
    "django.contrib.sessions.middleware.SessionMiddleware",
    "django.middleware.common.CommonMiddleware",
    "django.middleware.csrf.CsrfViewMiddleware",
    "django.contrib.auth.middleware.AuthenticationMiddleware",
    "django.contrib.messages.middleware.MessageMiddleware",
    "django.middleware.clickjacking.XFrameOptionsMiddleware",
]

ROOT_URLCONF = "loicblog.urls"

TEMPLATES = [
    {
        "BACKEND": "django.template.backends.django.DjangoTemplates",
        "DIRS": [],
        "APP_DIRS": True,
        "OPTIONS": {
            "context_processors": [
                "django.template.context_processors.debug",
                "django.template.context_processors.request",
                "django.contrib.auth.context_processors.auth",
                "django.contrib.messages.context_processors.messages",
            ],
        },
    },
]

WSGI_APPLICATION = "loicblog.wsgi.application"

# Password validation
# https://docs.djangoproject.com/en/4.2/ref/settings/#auth-password-validators

AUTH_PASSWORD_VALIDATORS = [
    {
        "NAME": "django.contrib.auth.password_validation.UserAttributeSimilarityValidator",
    },
    {
        "NAME": "django.contrib.auth.password_validation.MinimumLengthValidator",
    },
    {
        "NAME": "django.contrib.auth.password_validation.CommonPasswordValidator",
    },
    {
        "NAME": "django.contrib.auth.password_validation.NumericPasswordValidator",
    },
]

# Internationalization
# https://docs.djangoproject.com/en/4.2/topics/i18n/

LANGUAGE_CODE = "en-us"

TIME_ZONE = "UTC"

USE_I18N = True

USE_TZ = True

# Static files (CSS, JavaScript, Images)
# https://docs.djangoproject.com/en/4.2/howto/static-files/

STATIC_URL = "static/"

# Default primary key field type
# https://docs.djangoproject.com/en/4.2/ref/settings/#default-auto-field

DEFAULT_AUTO_FIELD = "django.db.models.BigAutoField"

# Login/Logout redirects
LOGIN_REDIRECT_URL = 'home'
LOGOUT_REDIRECT_URL = 'home'

# Markdown extensions to handle code blocks and code block highlighting
MARKDOWNX_MARKDOWN_EXTENSIONS = ['fenced_code', 'codehilite']
```

Then, the main differences between `dev` and `prod` are the DB settings and where to store the static files.

Here is the `dev.py`:

```python
from loicblog.settings.common import *

DEBUG = True

# SQLite Database

DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.sqlite3",
        "NAME": BASE_DIR / "db.sqlite3",
    }
}

STATIC_ROOT = "/Users/loicblanchard/workspaces/blog-statics"
```

And here is the `prod.py`:

```python
from loicblog.settings.common import *

DEBUG = False

ALLOWED_HOSTS = ["blog.loicblanchard.me", "*"] # add localhost for local testing

CSRF_TRUSTED_ORIGINS = ['https://blog.loicblanchard.me']

# Amazon S3 configuration
AWS_ACCESS_KEY_ID = env.str('AWS_ACCESS_KEY_ID')
AWS_SECRET_ACCESS_KEY = env.str('AWS_SECRET_ACCESS_KEY')
AWS_STORAGE_BUCKET_NAME = env.str('AWS_STORAGE_BUCKET_NAME')

INSTALLED_APPS += [
   'storages',
]

STORAGES = {
    "staticfiles": {
        "BACKEND": "storages.backends.s3boto3.S3StaticStorage"
    }
}

AWS_S3_CUSTOM_DOMAIN = '%s.s3.amazonaws.com' % AWS_STORAGE_BUCKET_NAME

AWS_S3_FILE_OVERWRITE = True

# Set the static root to the S3 bucket path
STATIC_ROOT = 's3://%s/static' % AWS_STORAGE_BUCKET_NAME

## Admin styling adjustment

ADMIN_MEDIA_PREFIX = '/static/admin/'

# PostgreSQL Database

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.postgresql',
        'NAME': env.str('DB_NAME'),
        'USER': env.str('DB_USER'),
        'PASSWORD': env.str('DB_PASSWORD'),
        'HOST' : env.str('DB_HOST'),
        'PORT': env.str('DB_PORT', default='5432'),
    }
}
```

### .env and .env.dist

You can see that all the sensitive data is stored in env variables and in my case in a `.env` file at the root of the project. Of course, do not push this file to any repo and keep it in a safe place (I use GitHub private Gist or sometimes directly Bitwarden password manager for some credentials).

Another good practice is to have an `env.dist` file that describes the env variables expected to be provided without the actual values.

Here is mine:

```bash
DJANGO_SETTINGS_MODULE=
SECRET_KEY=

AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_STORAGE_BUCKET_NAME=
AWS_S3_REGION_NAME=

DB_NAME=
DB_USER=
DB_PASSWORD=
DB_HOST=
```

In case other developers or your future self want to know what are the env variables required for the project to work (especially in prod), having a look at the `.env.dist` show me what I need to know right away.

Note the `DJANGO_SETTINGS_MODULE` I use for switching from `dev` to `prod` env and therefore load the proper setting file.

If you look at the `prod.py` , you can see that I use AWS S3 to store the static files and AWS RDS Postgres to store the users/posts data.

## AWS S3

The static files are stored in a public AWS S3 bucket.

### django-storages

Django-storages is a Python library that provides a storage backend system for Django web applications.

```bash
pip install -U django-storages
```

### boto3

Official AWS SDK (Software Development Kit) for Python. Boto3 allows Python developers to interact with various Amazon Web Services (AWS) resources and services programmatically.

```bash
pip install -U boto3
```

### Push static files to S3

First, I make sure to use the prod env variable in `.env`:

```bash
DJANGO_SETTINGS_MODULE=loicblog.settings.prod
```

Then in `loicblog`:

```bash
python manage.py collectstatic
```

This command collects all the static files and store them in the location specified via `STATIC_ROOT` (a local dir for `dev` and the S3 bucket for `prod`).

Be sure to have the AWS env variables setup before running the command.

In prod, this command will gather your static files and push it to AWS S3 bucket (you need to have the bucket public so the files can be served everywhere).

## AWS RDS Postgres

First, create AWS RDS Postgres db and Security Group.

For the SG, it needs to have inbound rules for Postgres.

### psycopg2-binary

The package `psycopg2-binary` is a PostgreSQL adapter for Python. It allows Python programs to communicate with a PostgreSQL database.

```bash
pip install psycopg2-binary
```

All the DB configs can be added in `.env` as well.

In order to migrate and setup the superuser properly, we need to run a few commands:

```bash
# apply migrations.
python manage.py migrate

# Create superuser
python manage.py createsuperuser
```

## AWS Elastic Beanstalk

One straight forward way to run the Django app is in an AWS Elastic Beanstalk.

You can read more about it in this guide: [EB guide](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/create-deploy-python-django.html).

First, we need to be sure all the packages are present in the `requirements.txt` as the EB needs it to setup the app:

```bash
pip freeze > requirements.txt
```

### gunicorn

[Gunicorn](https://gunicorn.org/) (Green Unicorn) is a commonly used HTTP server for deploying Python web applications, including Django apps. When deploying a Django app on AWS Elastic Beanstalk, Gunicorn is often used as the application server to handle incoming HTTP requests and serve the Django application.

```bash
pip install gunicorn
```

### EB config

The EB configurations can be found in `.ebextensions/django.config`

```
option_settings:
 aws:elasticbeanstalk:container:python:
  WSGIPath: loicblog.wsgi:application
```

**WSGI Application**: The Web Server Gateway Interface application is responsible for handling the communication between the web server (like Apache or nginx) and the Django application. It translates incoming HTTP requests into a format that Django can process and then sends the responses back to the web server.

In my example, **`loicblog.wsgi`** is the module path, and **`application`** is the variable within that module that represents my WSGI application.

### AWS CLI EB

We can use the AWS CLI to manage the Elastic Beanstalk creation and deployment of new environment and app.

```bash
## first leave python virtual env
deactivate

## then proceed with eb cli
brew install awsebcli

## init eb
eb init

## BE SURE TO HAVE DJANGO_SETTINGS_MODULE=loicblog.settings.prod

## Create all resources
eb create

## (re)deploy
eb deploy
```

## Domain name

### ALB DNS

By default, creating an EB also setup an Application Load Balancer (ALB). The ALB has its own DNS and we want to map our own DNS name to it. The type of record to achieve this is called `CNAME`.

### SSL Certificate

In my case, I own the domain `loicblanchard.me`. I want to have my blog on the subdomain `blog.loicblanchard.me`. I use GoDaddy for DNS provider but the process is quite similar for most providers.

For HTTPS, we can create a SSL certificate using AWS Certificate Manager for the subdomain `blog.loicblanchard.me`.

*Note: ACM provides the CNAME record name and value. For the name, it will provide something like this `_SOME-NUMBERS-HERE.blog.loicblanchard.me.`*

*However, we need to only enter `_SOME-NUMBERS-HERE.blog`for it to work in GoDaddy.*

### Mapping Subdomain to ALB

Then in GoDaddy, to resolve `blog.loicblanchard.me` to the ALB name, we need to add another CNAME record for the `blog` subdomain.

After that, we need to add rules to the ALB to redirect http to https using the ACM certificate.

Finally, we need to be sure the Security Group of the ALB allows inbound HTTPS.

Update the `ALLOWED_HOSTS` and `CSRF_TRUSTED_ORIGINS` with the subdomain and redeploy the EB.

## Conclusion

I provided some general guidelines on how you could develop and deploy a Django app using the example of my own project.

Using AWS EB is very straight forward to setup and cheap solution for low traffic website such as my blog.

Using AWS S3 to serve the static files and AWS RDS to store your production data are common ways to handle your production data.

Be sure to keep your env variables safe and split dev and prod settings to avoid confusion and accidental sensitive data leak.

Since EB comes with ALB, you can easily use a CNAME record to map your own personal subdomain to the ALB.
