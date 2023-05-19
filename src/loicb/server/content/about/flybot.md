#:post{:id "flybot-senior-software-engineer"
       :order 4
       :page :about
       :title "2020 - now | Flybot"
       :css-class "flybot"}
+++
# 🇸🇬 Senior Software Engineer | Flybot Pte Ltd | Singapore

## full-stack Clojure web development including CI/CD

I am currently working on a Clojure(Script) full stack implementation of our company website. Via the UI, software engineers can post articles about our open source projects, our current objectives, or functional programming content for everybody to enjoy. HRs can also post job offers.

🔗 full-stack website code: [skydread1/flybot.sg](https://github.com/skydread1/flybot.sg)

## Technical interviewees and new employees’ onboarding

I take care of the technical interview for junior developers in the recruitment process. I do also review the candidates assessments tests and take part of the hiring decision. Once a candidate was hired, I take care of the technical onboarding.

## Clojure backend card game development

I developed a few Clojure backend libraries providing APIs to run popular Asian card games. I also developed a project that can generate tournaments of the previous mentioned card games. The different card games implementing the same protocol, they can get composed quite easily due to the benefit of data oriented programming. The whole tournament can be describe via simple pure Clojure data.

## Integration of Clojure library in Unity

The Clojure card game projects can compile to both the JVM and the CLR. To compile to the CLR, I helped the owner of the Magic compiler to make it more straight forward to use by improving the tooling around it. The magic compiler is a bootstrapped compiler written in Clojure that compiles Clojure code to .NET assemblies.

🔗 compiler: [nasser/magic](https://github.com/nasser/magic)

Using a project called Nostrand, we can run, test, and package Clojure libraries in the CLR in a few commands.

🔗 compiler tooling: [nasser/nostrand](https://github.com/nasser/nostrand)

We also have easy integration of the compiler to the game Engine Unity. Just importing the Magic runtime to Unity makes the use of Clojure code possible.

🔗 compiler Unity integration: [nasser/Magic.Unity](https://github.com/nasser/Magic.Unity)

## ClojureScript SPA web development

For my first cljs project, I created a Single Page Application webpage for our company so we can explain what our company does and what are the job offers available at the moment. I first designed the SPA using common JavaScript framework such as React. I then quickly switched to ClojureScript to prove myself how efficient it is to use the available open source Clojure projects (Reitit, Malli, Figwheel, Hiccup etc). It took me just a few weeks to get familiar with Clojure frontend development and convert the website from js to cljs.
This SPA has since then been turned into a full-stack Clojure web application and you can read more about it in the Senior Software Engineer Position’s description.