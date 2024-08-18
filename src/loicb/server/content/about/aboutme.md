#:post{:id "about-me"
       :page :about
       :date ["2024-01-06"]
       :employer "My journey so far"
       :repos [["My GitHub" "https://github.com/skydread1"]]
       :articles [["My Tech Blog" "../blog"]]
       :title "About Me"
       :css-class "about-me"
       :image #:image{:src "/assets/loic-logo.png"
                      :src-dark "/assets/loic-logo.png"
                      :alt "Loic Logo"}}
+++
+++
## Work Experiences

### 2024-now: Staff Software Engineer | [Flybot Pte Ltd](https://www.flybot.sg/), Singapore

- Lead the Flybot's engineering team into meeting our client's expectations.
- Report to the CEO directly to gather client's needs and plan accordingly
- Design software architecture and delegate project responsibilities to the team's engineers. 

### 2023: Senior Software Engineer | [Flybot Pte Ltd](https://www.flybot.sg/), Singapore
- Designed a challenge recommender that suggests personal challenges to Golden Island's players. The recommender is a Clojure application deployed in a POD in AWS EKS that consume events from kafka topics and produces personalized challenges to a dedicated kafka topic. It uses Datomic as storage solution within the EKS cluster | *Clojure, AWS EKS, k8s, datomic, kafka*
- Developed the company blog mobile app with React Native framework. The mobile frontend and web frontend share most of the re-frame (state management) logic | *ClojureScript, ReactNative* (open-source)
- Conducted technical interviews for junior developers and onboarding of new employees

### 2020-2023: Software Engineer | [Flybot Pte Ltd](https://www.flybot.sg/), Singapore

- Developed the company full-stack web app. The website has a blog. Oauth2 is used for authentication. The website is deployed in a docker container on AWS. It showcases some of Flybot's open-source libs for dependency injections and data pulling | *Clojure, ClojureScript, React* (open-source)
- Developed a basic Monte Carlo Tree Search bot for our card games | *Clojure*
- Ported our Clojure backend libraries to Unity so the Unity frontend developers can use the Clojure logic in Unity | *Clojure, C#*
- Improved the Nostrand project management to ease the compilation with the Magic compiler (compile Clojure file to .NET assemblies) | *Clojure, C#* (open-source)
- Developed a library called `MetaGame` to compose card games (play multiple rounds, make it a tournament). An entire tournament can be sent up using pure Clojure data | *Clojure*
- Developed online Chinese card games (Pǎo Dé Kuài (跑得快) and Big two (锄大地) ) backend | *Clojure*

### 2019: End of study project | [Bosch SEA Pte Ltd](https://www.bosch.com.sg/our-company/bosch-in-singapore/), Singapore
- Modeled and provisioned infrastructure using AWS CloudFormation for a project that consists in facilitating the diagnosis of damaged automobile pieces via trend detection
- Deployed and maintained AWS resources with Jenkins
- Cohered Agile Software Development using Jira Kanban and Scrum as frameworks, Git for version-control system and Atlassian software *| Bitbucket, Jira and SourceTree*

### 2017-2018: One-year internship | [Electriduct Inc](https://www.electriduct.com/), Fort Lauderdale, Florida, USA
- Improved Web Design and responsivity | *HTML, CSS, JS, 3dcart templates*
- Optimized online ad campaigns | *Google AdWords/Shopping/Analytics*
- Developed an inventory management program using UPC barcode reading | *PHP, SQL, HTML, CSS, JS*
- Developed a customized barcode generator for either sheet printers or thermal printer | *C#, SQL*

## Education

### 2015-2019: Master’s Degree | [CPE](https://www.cpe.fr/en/) Lyon, France

- **Specialization**: Software Engineering
- **Major Project**: Full-stack JS web app and Mobile App development allowing users to find new friends to go to common interest nearby events together *| Node.js, ReactJS, React Native*
- **Secondary Projects**: Android Chat App *(Java)*, Big Data hackathon *(Hadoop, Tableau)*, Chess Game *(Java)*, Siam Game *(C)*, UX design *(Balsamiq)*

### 2014-2015: Undergraduate in Engineering Sciences | [CPE](https://www.cpe.fr/en/) Lyon, France
- **Major**: mathematics and physics
- **Minor**: computer sciences and automatism

## Skills

### ICTS             
- **Back-End**: Clojure, Python, Java, PHP, Node.js, C, C++, C#
- **HTTP**: Clojure Ring, Clojure Aleph
- **Front-End**: ClojureScript, HTML, CSS, JS, C#, Reagent (React), Re-frame, figwheel-main
- **Database**: MySQL, PostgreSQL, Datomic, Datalevin, Cassandra
- **Mobile**: Reagent React Native, figwheel-main
- **Cloud**: AWS, Vercel, Netlify
- **Containers**: Docker, k8s, AWS EKS
- **Event Streaming**: Kafka
- **Proj Management**: GitHub, Gitlab, Bitbucket, Trello, Jira, Slack, Jenkins

### Certifications
- **AWS**: Solutions Architect - Associate
