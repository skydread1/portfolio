#:post{:id "datomic-setup-examples"
       :page :blog
       :date ["2022-12-02"]
       :title "Datomic Setup examples: embedded, cassandra, docker."
       :css-class "blog-datomic-setup"
       :tags ["Clojure" "Datomic" "Cassandra" "Docker"]
       :image #:image{:src "/assets/loic-blog-logo.png"
                      :src-dark "/assets/loic-blog-logo.png"
                      :alt "Logo referencing Aperture Science"}}
+++
+++
## Introduction

While working on [flybot.sg](http://flybot.sg) , I experimented with `datomic-free`, datomic `starter-pro` with Cassandra and datomic starter-pro with embedded storage.

## Rational

You can read the rationale of Datomic from their [on-prem documentation](https://docs.datomic.com/on-prem/getting-started/brief-overview.html)

Stuart Sierra explained very well how datomic works in the video [Intro to Datomic](https://www.youtube.com/watch?v=R6ObrDWTlYA&t=2776s).

Basically, Datomic works as a layer on top of your underlying storage (in this case, we will use Cassandra db).

Your `application` and a Datomic `transactor` are contained in a `peer`. 

The transactor is the process that controls inbounds, and coordinates persistence to the storage services.

The process acts as a single authority for inbound transactions. A single transactor process allows the to be ACID compliant and fully consistent.

The peer is the process that will query the persisted data.

Since Datomic leverages existing storage services, you can change persistent storage fairly easily.

## Datomic Starter Pro with Cassandra

### Datomic pro starter version

Datomic is closed-source and commercial.

You can see the different pricing models in the page [Get Datomic On-Prem](https://www.datomic.com/get-datomic.html).

There are a few way to get started for free. The first one being to use the [datomic-free](https://blog.datomic.com/2012/07/datomic-free-edition.html) version which comes with in-mem database storage and local-storage transactor. You don’t need any license to use it so it is a good choice to get familiar with the datomic Clojure API.

Then, there is `datomic pro starter` renamed `datomic starter` which is free and maintained for 1 year. After the one year threshold, you won’t benefit from support and you won’t get new versions of Datomic. You need to register to Datomic to get the license key.

### Cassandra, Java and Python version caveats

Datomic only support Cassandra up to version 3.x.x

Datomic start pro version of Cassandra at the time of writting: 3.7.1

Closest stable version of Cassandra: 3.11.10

**Problem 1: Datomic does not support java 11 so we have to have a java 8 version on the machine**

Solution: use [jenv](https://github.com/jenv/jenv) to manage multiple java version

```bash
# jenv to manage java version
brew install jenv
echo 'export PATH="$HOME/.jenv/bin:$PATH"' >> ~/.bash_profile
echo 'eval "$(jenv init -)"' >> ~/.bash_profile
# add cask version
brew tap homebrew/cask-versions
# install java 8 cask
brew install --cask adoptopenjdk8
# add java 11 (current java version) to jenv
jenv add "$(/usr/libexec/java_home)"
# add java 8 to jenv
jenv add /Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home
# update the ${JAVA_HOME} everytim we change version
jenv enable-plugin export
#swith to java 8
jenv global 1.8
```

**Problem 2: cqlsh does not work with python3 with Cassandra running on java8**

Solution: download the python2 pkg directly from [python.org](https://www.python.org/downloads/release/python-2718/)

**Problem 3: `brew install cassandra@3` triggers an execution error hard to debug**

Solution: download the tar.gz directly on [apache.org](https://www.apache.org/dyn/closer.lua/cassandra/3.11.14/apache-cassandra-3.11.14-bin.tar.gz)

### Setup Cassandra locally and run start the transactor

To test Cassandra and datomic locally, we can use the Test Cluster of Cassandra which comes up with only one node.

Datomic instruction for Cassandra [here](https://docs.datomic.com/on-prem/overview/storage.html#cassandra)

```bash
# Check if all the versions are ok
java -version
openjdk version "1.8.0_292"
OpenJDK Runtime Environment (AdoptOpenJDK)(build 1.8.0_292-b10)
OpenJDK 64-Bit Server VM (AdoptOpenJDK)(build 25.292-b10, mixed mode)
python2 -V
Python 2.7.18
cqlsh
Connected to Test Cluster at 127.0.0.1:9042.
[cqlsh 5.0.1 | Cassandra 3.11.14 | CQL spec 3.4.4 | Native protocol v4]
Use HELP for help.

# Start cassandra
cassandra -f

# ===========================================================
# in other terminal

# Only setup replica to 1 for the test cluster locally
# add datomic keyspace and table
cqlsh
CREATE KEYSPACE IF NOT EXISTS datomic WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};
CREATE TABLE IF NOT EXISTS datomic.datomic
(
  id text PRIMARY KEY,
  rev bigint,
  map text,
  val blob
);

# ===========================================================
# in other terminal

# start datomic transactor
# A sample of the cassandra transactor properties is provided in the datomic distribution samples.
# the documentation of datomic mentioned we should have a msg of the shape:
# System starter URI but I do not have URI but it seems to work nonetheless
cd datomic-pro-1.0.6527/
bin/transactor ~/workspaces/myproj/config/cassandra-transactor.properties
Launching with Java options -server -Xms1g -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=50
System started

# ===========================================================
# in other terminal

# Test if the peer works properly on our localhost single node
bin/shell
Datomic Java Shell
Type Shell.help(); for help.
datomic % uri = "datomic:cass://localhost:9042/datomic.datomic/myproj";
<datomic:cass://localhost:9042/datomic.datomic/myproj>
datomic % Peer.createDatabase(uri);
<true>
datomic % conn = Peer.connect(uri);
<{:unsent-updates-queue 0, :pending-txes 0, :next-t 1000, :basis-t 66, :index-rev 0, :db-id "myproj-some-id-here"}>
```

It’s important to note that we do not add `ssl` in the database URI so we don’t have to deal with the [KeyStore and TrustStore](https://docs.datomic.com/on-prem/overview/storage.html#troubleshooting) (for local use only)

### Use Clojure API to create db and perform transactions

Since the peer works using the datomic shell, we can confidently use the Clojure API from our code now.

We just need to add the datomic and Cassandra deps in the `deps.edn`:

```clojure
;; deps.edn : versions are provided upon subscription to datomic-pro
com.datomic/datomic-pro                      {:mvn/version "1.0.6527"}
com.datastax.cassandra/cassandra-driver-core {:mvn/version "3.1.0"}
```

## Datomic Starter Pro with embedded storage

In case of embedded DB, we only need to start a transactor and that’s it.

The URI to connect to the peer is of the shape:

```clojure
"datomic:dev://localhost:4334/myproj-db?password=my-secret"
;; the password is the `storage-datomic-password` setup in the transactor properties.
```

## Datomic in docker container

In case we want to run datomic in a container (and maybe having our app in another container), we can do the following:

- create DockerFile for our app
- create DockerFile for Datomic Starter Pro (you could do the same with datomic-free)
- create docker-compose file to run both the containers
- update the transactors properties to be sure the app and transactor can communicate.

### DockerFiles

We assume that the app has its own DockerFile and run on port 8123 in this example.

Here is a DockerFile example to have Datomic running in a container:

```docker
FROM clojure:lein-2.6.1-alpine

ENV DATOMIC_VERSION 1.0.6527
ENV DATOMIC_HOME /opt/datomic-pro-$DATOMIC_VERSION
ENV DATOMIC_DATA $DATOMIC_HOME/data

RUN apk add --no-cache unzip curl

# Datomic Pro Starter as easy as 1-2-3
# 1. Create a .credentials file containing user:pass
# for downloading from my.datomic.com
ADD .credentials /tmp/.credentials

# 2. Make sure to have a config/ folder in the same folder as your
# Dockerfile containing the transactor property file you wish to use
RUN curl -u $(cat /tmp/.credentials) -SL https://my.datomic.com/repo/com/datomic/datomic-pro/$DATOMIC_VERSION/datomic-pro-$DATOMIC_VERSION.zip -o /tmp/datomic.zip \
  && unzip /tmp/datomic.zip -d /opt \
  && rm -f /tmp/datomic.zip

ADD config $DATOMIC_HOME/config

WORKDIR $DATOMIC_HOME
RUN echo DATOMIC HOME: $DATOMIC_HOME

# 3. Provide a CMD argument with the relative path to the transactor.properties
VOLUME $DATOMIC_DATA

EXPOSE 4334 4335 4336

CMD bin/transactor -Ddatomic.printConnectionInfo=true config/dev-transactor.properties
```

### Docker Compose

Here is a `docker-compose.yml` we could use describing our app and datomic transactor containers

```yaml
version: '3.0'
services:
  datomicdb:
    image: datomic-img
    hostname: datomicdb
    ports:
      - "4336:4336"
      - "4335:4335"
      - "4334:4334"
    volumes:
      - "/data"
  myprojapp:
    image: myproj-img
    ports:
      - "8123:8123"
    depends_on:
      - datomicdb
```

Here are the commands to create the images and run 2 containers.

```docker
# Create datomic transactor image
docker build -t datomic-img .

# Create app image
docker build -t myproj-img .

# run the 2 images in containers
docker-compose up
```

However, this will not work right away as we need to add a few configurations to the datomic transactor properties to make sure the app can communicate with the transactor.

### Transactors Properties

Regarding the transactor properties (datomic provides a template for a transactor with Cassandra storage), when we use docker, we need to pay attention to 3 properties:

- The [`localhost`](http://localhost) is now 0.0.0.0
- `alt-host` must be added with the container name (or IP) or the container running the app.
- `storage-access` must be set to `remote`

Here are the difference between containerized and not containerized properties for a `dev-transactor`: 

```yaml
# If datomic not in container
protocol=dev
host=localhost
port=4334

# If datomic in container
protocol=dev
host=0.0.0.0
port=4334
alt-host=datomicdb
storage-access=remote
```

After updating the transactor properties, you should be able to see the app running on port 8123 and be able to perform transactions as expected.
