#:post{:id "clojure-mobile-app"
       :page :blog
       :home-page? true
       :employer "Flybot Pte Ltd" 
       :date ["2023-02-13" "2023-08-08"]
       :repos [["Flybot" "https://github.com/skydread1/flybot.sg"]]
       :articles [["Reagent React Native Mobile App" "../blog/reagent-native-app"]
                  ["Clojure Mono Repo example : server + 2 clients" "../blog/clojure-mono-repo"]]
       :title "Flybot Mobile App"
       :tags ["ClojureScript" "Figwheel" "Re-Frame" "Reagent React Native"]
       :css-class "flybot-mobile-app"
       :image #:image{:src "https://www.flybot.sg/assets/flybot-logo.png"
                      :src-dark "https://www.flybot.sg/assets/flybot-logo.png"
                      :alt "Flybot Logo"}}
+++
At Flybot, I developed a mobile app using **ClojureScript** with `React Native` using `Figwheel` and `Reagent React Native` (to interact with reagent in ClojureScript).

The code for the mobile app resides in the same repo as the server and the web client of the [flybot.sg](https://www.flybot.sg/) website.

The goal of the mobile app was to allow employees to write blog posts using an app instead of the web UI and also to evaluate if our ClojureScript frontend stack could reuse most of the `re-frame` logic in both the mobile and web UIs.
+++
## Rational

The goal is to have a mobile app targeting both iOS and Android, written in **ClojureScript**, which can reuse most of our web frontend logic.

To do so, I used React Native for the following reasons:

- Integrate very well with [bhauman/figwheel-main](https://github.com/bhauman/figwheel-main) and [day8/re-frame](https://github.com/day8/re-frame)
- Target both iOS and Android
- Does not necessitate too much configuration to get it running
- React Native has an overall good documentation

## Repo

You can have a look at the code on my [GitHub repo](https://github.com/skydread1/flybot.sg)

We use a mono repo structure where the `server` (clj files), and `client` (cljs files) reside alongside each others.
A `common` (cljc files) top folder is also used for data validation that applies for both server and client.

We actually have 2 clients: web and mobile.
So the mobile app frontend resides in the same repo as the web frontend and the 2 share most of the re-frame events.

The mono-repo structure is as followed:

```
├── client
│   ├── common
│   │   ├── src
│   │   │   └── flybot.client.common
│   │   └── test
│   │       └── flybot.client.common
│   ├── mobile
│   │   ├── src
│   │   │   └── flybot.client.mobile
│   │   └── test
│   │       └── flybot.client.mobile
│   └── web
│       ├── src
│       │   └── flybot.client.web
│       └── test
│           └── flybot.client.web
├── common
│   ├── src
│   │   └── flybot.common
│   └── test
│       └── flybot.common
├── server
│   ├── src
│   │   └── flybot.server
│   └── test
│       └── flybot.server
```

You can read more about it in my article: [Clojure Mono Repo example : server + 2 clients](../blog/clojure-mono-repo).

## Stack

The backend is the same as for the web app.

The `mobile` frontend is very similar to the `web` frontend.

The main differences with the web frontend are the following:
- markup: RN using native components, we cannot reuse the hiccup markup used in the web frontend (hence the use of `reagent-react-native`)
- navigation: Tab and Stack Navigators for mobile instead of reitit.frontend.easy for web
- markdown support: convert markdown to native components instead of react components for the web
- cookie management: I manually store the cookie in AsyncStorage and manually pass it to the request

For the rest, most re-frame events remain the same between the 2 UIs, hence most of the re-frame logic is done in the `client.common` namespace.

## Hot reloading

[figwheel-main](https://github.com/bhauman/figwheel-main) also works on mobile after a few setup steps required to get a react native app ready. To install the different native libraries, I just use npm. Once again, Figwheel is really convenient to use and provide clear configurations to get the hot reloading working.

## CI/CD

I tested the app on iOS only via the iOS Simulator in Xcode locally.
The app has not been deployed on any Store yet.

## Learn more

You can learn more about how the mobile app was setup in the article linked at the top of the page.

Also, feel free to visit [flybot.sg](https://www.flybot.sg/) and especially the [blog](https://www.flybot.sg/blog).
