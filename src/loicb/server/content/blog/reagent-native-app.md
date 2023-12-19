#:post{:id "reagent-native-app"
       :order 7
       :page :blog
       :date "2023" ;; 03/02
       :title "Reagent React Native Mobile App"
       :css-class "blog-reagent-native"
       :tags ["Clojure" "React Native" "Reagent" "Figwheel"]
       :image #:image{:src "/assets/loic-blog-logo.png"
                      :src-dark "/assets/loic-blog-logo.png"
                      :alt "Logo referencing Aperture Science"}}
+++
+++
## Prerequisites

This project is stored alongside the backend and the web frontend in the mono-repo: [skydread1/flybot.sg](https://github.com/skydread1/flybot.sg)

The codebase is a full-stack **Clojure(Script)** app.
The backend is written in **Clojure** and the web and mobile clients are written in **ClojureScript**.

For the web app, we use [reagent](https://github.com/reagent-project/reagent), a ClojureScript interface for `React`.

For the mobile app, we use [reagent-react-native](https://github.com/vouch-opensource/reagent-react-native), a ClojureScript interface for `React Native`.

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

So far, the RN app has only been tested on iOS locally.

## Rational

The goal was to have a mobile app targeting both iOS and Android, written in `ClojureScript`, which can reuse most of our web frontend logic.

To do so, I used `React Native` for the following reasons:

- Integrate very well with [figwheel-main](https://github.com/bhauman/figwheel-main) and [re-frame](https://github.com/day8/re-frame)
- Target both iOS and Android
- Does not necessitate too much configuration to get it running
- React Native has an overall good documentation

## Setup

To get React Native working, you need to follow a few steps.

The setup steps are well described in the [Figwheel doc](https://figwheel.org/docs/react-native.html).

### npm

The Figwheel doc has a [dedicated section](https://figwheel.org/docs/npm.html) to install and setup NPM in a project. The best way to install npm is to use [nvm](https://github.com/nvm-sh/nvm).

### React Native

To do mobile dev, some tools need to be installed and the react native [doc](https://reactnative.dev/docs/next/environment-setup) has the instructions on how to prepare the environment.

### Ruby

The default Ruby version installed on MacOS is not enough to work with React Native. Actually, React Native needs a specific version of Ruby hence the use of a ruby version manager. I used [rbenv](https://github.com/rbenv/rbenv).

```bash
~:brew install rbenv ruby-build

~:rbenv -v
rbenv 1.2.0
```

React Native uses [this version](https://github.com/facebook/react-native/blob/main/template/_ruby-version) of ruby so we need to download it.

```bash
# install proper ruby version
~:rbenv install 2.7.6

# set ruby version as default
~:rbenv global 2.7.6
```

We also need to add these 2 lines to the .zshrc

```bash
export PATH="$HOME/.rbenv/bin:$PATH"
eval "$(rbenv init -)"
```

Finally we make sure we have the correct version:

```bash
~:ruby -v
ruby 2.7.6p219 (2022-04-12 revision c9c2245c0a) [arm64-darwin22]
```

### Ruby's Bundler

From the doc:

Ruby's [Bundler](https://bundler.io/) is a Ruby gem that helps managing the Ruby dependencies of your project. We need Ruby to install Cocoapods and using Bundler will make sure that all the dependencies are aligned and that the project works properly.

```bash
# install the bundler
~:gem install bundler
Fetching bundler-2.4.5.gem
Successfully installed bundler-2.4.5
...

# Check the location where gems are being installed
~:gem env home
/Users/loicblanchard/.rbenv/versions/2.7.6/lib/ruby/gems/2.7.0
```

### Xcode

From the doc:

> The easiest way to install `Xcode` is via the [Mac App Store](https://itunes.apple.com/us/app/xcode/id497799835?mt=12)
. Installing Xcode will also install the iOS Simulator and all the necessary tools to build your iOS app.

I downloaded it from the apple store.

`Xcode command line` tools also needs to be installed. It can be chosen in `Xcode→Settings→Locations`

```bash
~:xcode-select -p
/Library/Developer/CommandLineTools
```

### Installing an iOS Simulator in Xcode

It should be already installed.

### React Native Command Line Interface

We can use `npx` directly because it was shipped with `npm`.

### CocoaPods

[CocoaPods](https://github.com/CocoaPods/CocoaPods) is required to use the Ruby’s Bundler and we can install it using [rubygems](https://github.com/rubygems/rubygems):

```bash
sudo gem install cocoapods

# check version
~:gem which cocoapods
/Users/loicblanchard/.rbenv/versions/2.7.6/lib/ruby/gems/2.7.0/gems/cocoapods-1.11.3/lib/cocoapods.rb
```

### Troubleshooting

In case of the error [Multiple Profiles](https://github.com/CocoaPods/CocoaPods/issues/11641), we need to switch to the Xcode cli manually like so:

```bash
sudo xcode-select --switch /Applications/Xcode.app
```

## Create Project

We now should have all the tools installed to start a React Native project on Mac targeting iOS.

```bash
# setup project
npx react-native init MyAwesomeProject

```

### Running the project

```bash
npx react-native run-ios
```

This should open a simulator with the welcome React Native display.

## Integrate RN with Clojure and Figwheel

Add an alias to the deps.edn:

```clojure
:cljs/ios {:main-opts ["--main"  "figwheel.main"
                       "--build" "ios"
                       "--repl"]}
```

Note: We need to use cljs version `1.10.773` because the latest version causes this [error](https://github.com/log4js-node/log4js-node/issues/1171) which is hard to debug.

Also, we need to add the figwheel config for `ios` in `ios.cljs.edn` :

```clojure
^{:react-native :cli
  :watch-dirs ["client/mobile/src" "client/common/src"]}
{:main flybot.client.mobile.core
 :closure-defines {flybot.client.common.db.event/BASE-URI "http://localhost:9500"}}
```

And then we add the source files in the src folder like explained in the [figwheel doc](https://figwheel.org/docs/react-native.html).

To run the project, we start a REPLs (clj and cljs) with the proper aliases and in another terminal, we can run `run npm ios` to start the Xcode simulator.

For more details regarding the aliases: have a look at the [README](https://github.com/skydread1/flybot.sg)

## Deps management

If we want to add a npm package, we need 2 steps:

```bash
npm i my-npm-package
cd ios
pod install
cd ..
```

## Troubleshooting

In case of the error [RNSScreenStackHeaderConfig](https://stackoverflow.com/questions/73268848/i-am-trying-to-work-with-react-navigation-library-but-this-issue-keeps-coming), we need to:

```bash
npm i react-native-gesture-handler
cd ios
pod install
cd ..

# We restart the similutor and the error should be gone
```

## APP architecture and features

### HTTP

Regarding the http request made by the re-frame fx `http-xhrio`, it should work right away, same as for the web, but we just need to manually pass the cookie to the header as RN do not manage cookie for us like the web does.

Passing the cookie in the request was quite straight forward, I just added `:headers {:cookie my-cookie}` to the `:http-xhrio` fx for all the requests that require a session for the mobile app.

### Markdown to Native components

I use [react-native-markdown-package](https://github.com/andangrd/react-native-markdown-package)

```bash
npm i react-native-markdown-package --save
```

### Font

On iOS, I had to add the fonts in the `info.plist` like so:

```xml
<key>UIAppFonts</key>
	<array>
	  <string>AntDesign.ttf</string>
	  <string>Entypo.ttf</string>
	  <string>EvilIcons.ttf</string>
	  <string>Feather.ttf</string>
	  <string>FontAwesome.ttf</string>
	  <string>FontAwesome5_Brands.ttf</string>
	  <string>FontAwesome5_Regular.ttf</string>
	  <string>FontAwesome5_Solid.ttf</string>
	  <string>Foundation.ttf</string>
	  <string>Ionicons.ttf</string>
	  <string>MaterialIcons.ttf</string>
	  <string>MaterialCommunityIcons.ttf</string>
	  <string>SimpleLineIcons.ttf</string>
	  <string>Octicons.ttf</string>
	  <string>Zocial.ttf</string>
	</array>
```

## Navigation

### Navigators

As for now we have 2 Navigators:

[Tab Navigator](https://reactnavigation.org/docs/tab-based-navigation/)

- `login` screen
- `blog` screen: [Stack Navigator](https://reactnavigation.org/docs/stack-navigator/)

[Stack Navigator](https://reactnavigation.org/docs/stack-navigator/)

- `post-lists` screen
- `post-read` screen
- `post-edit` screen
- `preview` screen

So the Stack Navigator is inside the Tab Navigator blog screen.

#### How to navigate

For the navigation, we can use `re-frame` dispatch to change the navigation object ref to the new route.

Since we are using re-frame, we might not be able to access `props.navigation.navigate`.

However, we could store a reference to the navigation object in our re-frame DB so we can [Navigate without the navigation prop](https://reactnavigation.org/docs/navigating-without-navigation-prop/).

Therefore, just using `re-frame/dispatch` to store the navigation ref to the `re-frame/db` and use `re-frame/subscribe` to get the ref (and so the nav params) is enough to handle navigation in our case. Thus, we do not use the props at all.

Regarding the hot reloading, the only way I found is to store the js state and navigation objects in atoms via `defonce` so we can remain on the same screen with same params as before the reload.

Note: Maybe I could use the AsyncStorage instead of the atoms even though it is only for dev purposes.

## Env variables

One of the env variables we need to define is for the `uri`. For the web app, we can use relative path such as `/posts/all` but on mobile, there is no such thing as path and we would need to pass an absolute path such as `http://localhost:9500/posts/all` for instance in our case.

Therefore, we need to have some config to pass to the cljs build. It is possible to do so via the compiler option [:closure-defines](https://clojurescript.org/reference/compiler-options#closure-defines).

`:closure-defines` is a ClojureScript compiler option that allows you to specify a list of key-value pairs to be passed as JavaScript defines to the Google Closure Compiler. These defines can be used to conditionally compile code based on the value of the defined key. For example, you can define `:foo true` as a closure define and then use `#?(:foo some-code)` in your ClojureScript code to include `some-code` only when `:foo` is true.

Luckily, figwheel allows us to [setup the closures-define in the config files](https://figwheel.org/docs/compile_config.html).

## OAuth2.0

I redirect the request back to an intermediate end point that will directly fetch the user info and create a ring-session that contains the google tokens, the user-name and user-permissions. Then ring encrypts that for us and put that `ring-session` in a cookie that is sent to the client.

Thus, my clients only receive this ring-session id that will be passed to every request made (automatic for browser, manually added to request for mobile).

When the user logout, ring still passes a `ring-session` but it will be nil once decrypted by the server.

### How to redirect back to the mobile app

To go back to the app after OAuth2.0 success, I had to add the scheme following to the `info.plist` for iOS:

```xml
<key>CFBundleURLTypes</key>
	<array>
	<dict>
		<key>CFBundleURLSchemes</key>
		<array>
		<string>flybot-app</string>
		</array>
	</dict>
```

Also, in `ios/AppDelegate.mm`, I added:

```jsx
#import <React/RCTLinkingManager.h>

/// listen to incoming app links during your app's execution
- (BOOL)application:(UIApplication *)application
   openURL:(NSURL *)url
   options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options
{
  return [RCTLinkingManager application:application openURL:url options:options];
}
```

## Cookie management

I store the cookie in async-storage for this because it is enough for our simple use case.

```jsx
npm install @react-native-async-storage/async-storage
```

### AsyncStorage with re-frame

Once the `ring-session` cookie is received from the server, a re-frame dispatch is triggered to set a cookie name `ring-session` in the device AsyncStorage. This event also updates the re-frame db value of `:user/cookie`.

One of the issues with AsyncStorage is that it returns a `Promise`. Therefore, we cannot access the value directly but only do something in the `.then` method. So, once the Promise is resolved, in the .then, we `re-frame/dispatch` an event that will update the re-frame/db.

The Promises to get or set a cookie from storage, being side effects, are done in a re-frame `reg-fx`. These `reg-fx` will be done inside `reg-event-fx` event. We want to respect the principle: `reg-fx` for pulling with side effect and `reg-event-fx` for pushing pure event.

### Ensure order of events

We want to be sure the cookie is pulled from AsyncStorage before the db is initialised and all the posts and the user pulled. However, we cannot just dispatch the event to pull the cookie from AsyncStorage (returns a Promise that will then dispatch another event to update re-frame/db), and then dispatch the event to get all the posts from the server because there is no guarantee the cookie will be set before the request is made.

The solution is to dispatch the initialisation event inside the event from the Promise like so:

```clojure
;; setup all db param and do get request to get posts, pages and user using cookie
(rf/reg-event-fx
 :evt.app/initialize
 (fn [{:keys [db]} _] 
   {:db         (assoc db ...)
    :http-xhrio {:method          :post
                 :uri             (base-uri "/pages/all")
                 :headers         {:cookie (:user/cookie db)}
                 :params          ...
                 :format          (edn-request-format {:keywords? true})
                 :response-format (edn-response-format {:keywords? true})
                 :on-success      [:fx.http/all-success]
                 :on-failure      [:fx.http/failure]}}))

;; Impure fx to fet cookie from storage and dispatch new event to update db
(rf/reg-fx ;; 2)
 :fx.app/get-cookie-async-store
 (fn [k]
   (-> (async-storage/get-item k) ;; Promise
       (.then #(rf/dispatch [:evt.cookie/get %])))))

;; Pure event triggered at the start of the app
(rf/reg-event-fx ;; 1)
 :evt.app/initialize-with-cookie
 (fn [_ [_ cookie-name]]
   {:fx [[:fx.app/get-cookie-async-store cookie-name]]}))

;; Pure event triggered by :fx.app/get-cookie-async-store
(rf/reg-event-fx ;; 3)
 :evt.cookie/get
 (fn [{:keys [db]} [_ cookie-value]]
   {:db (assoc db :user/cookie cookie-value)
    :fx [[:dispatch [:evt.app/initialize]]]}))
```

## Styling

As for now, the styling is directly done in the `:style` keys of the RN component’s hiccups. Some more complex components have some styling that takes functions and or not in the `:style` keyword.

## Conclusion

I hope that this unusual mobile app stack made you want to consider `ClojureScript` as a good alternative to build mobile apps.

It is important to note that the state management logic (re-frame) is the same at 90% for both the web app and the mobile app which is very convenient.

Finally, the web app is deployed but not the mobile app. All the codebase is open-source so feel free to take inspiration.
