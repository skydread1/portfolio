#:post{:id "port-clojure-to-clr-with-magic"
       :order 0
       :page :blog
       :title "Port Clojure to CLR with Magic"
       :css-class "clojure-magic-port"
       :creation-date "08/04/2022"
       :show-dates? true}
+++

# Port your Clojure lib to the CLR with MAGIC

In this article, I will show you:
1. how to handle CLR interop to prepare your Clojure code for the CLR
2. how to use type hints to have your code more performant on the CLR
3. how to manage dependencies
4. how to compile to the CLR using Nostrand
5. how to test in the CLR using Nostrand

Note: the steps for packing the code into nugget package, pushing it to remote github and fetching it in Unity are highlighted in another article.

## Rational

### What is the Magic Compiler

Magic is a bootsrapped compiler writhen in Clojure that take Clojure code as input and produces dotnet assemblies (.dll) as output.

Compiler Bootstrapping is the technique for producing a self-compiling compiler that is written in the same language it intends to compile. In our case, MAGIC is a **Clojure** compiler that compiles **Clojure** code to .**NET** assemblies (.dll and .exe files).

It means we need the old dlls of MAGIC to generate the new dlls of the MAGIC compiler. We repeat this process until the compiler is good enough. 

The very first magic dlls were generated with the [clojure/clojure-clr](https://github.com/clojure/clojure-clr) project which is also a Clojure compiler to CLR but written in **C#** with limitations over the dlls generated (the problem MAGIC is intended to solve).

### Why the Magic Compiler

The already existing clojure->clr compiler [clojure/clojure-clr](https://github.com/clojure/clojure-clr). However, clojure-clr uses a technology called the DLR (dynamic language runtime) to optimize dynamic call sites but it emits self modifying code which make the assemblies not usable on mobile devices (IL2CPP in Unity). So we needed a way to have a compiler that emit assemblies that can target both Desktop and mobile (IL2CPP), hence the Magic compiler.

## Step 1: Interop

### Reader conditionals

We don’t want separate branches for JVM and CLR so we use reader conditionals.

You can find how to use the reader conditionals in this [guide](https://clojure.org/guides/reader_conditionals).

You will mainly need them for the `require` and `import` as well as the function parameters.

Don’t forget to change the extension of your file from `.clj` to `.cljc`.

### Clj-kondo Linter supporting reader conditionals

In `Emacs` (with `spacemacs` distribution), you might encounter some lint issues if you are using reader conditionals and some configuration might be needed.

The Clojure linter library  [clj-kondo/clj-kondo](https://github.com/clj-kondo/clj-kondo) supports the reader conditionals.

All the instruction on how to integrate it to the editor you prefer [here](https://github.com/clj-kondo/clj-kondo/blob/master/doc/editor-integration.md).

To use [clj-kondo](https://github.com/clj-kondo/clj-kondo) with [syl20bnr/spacemacs](https://github.com/syl20bnr/spacemacs), you need the layer [borkdude/flycheck-clj-kondo](https://github.com/borkdude/flycheck-clj-kondo).

However, there is no way to add configuration in the `.spacemacs` config file.

The problem is that we need to set `:clj` as the default language to be checked.

In `VScode` I did not need any config to make it work.

### Setting up the default reader conditionals of the Clj-kondo linter

It has nothing to do with the `:default` reader conditional key such as:

```clojure
#?(:clj  (Clojure expression)
   :cljs (ClojureScript expression)
   :cljr (Clojure CLR expression)
   :default (fallthrough expression))
```

In the code above, the `:default` reader is used if none of the other reader matches the platform the code is run on. There is no need to add the `:default` tag everywhere as the code will be ran only on 2 potential environment: `:clj` and `:cljr`.

For our linter, on your Clojure environment (in case of Emacs with [syl20bnr/spacemacs](https://github.com/syl20bnr/spacemacs) distribution), you can highlight the codes for the `:clj` reader only.

The `:cljr` code will be displayed as comments. 

To add the default `:clj` reader, we need to add it in the config file : `~/.config/clj-kondo/config.edn` (to affect all our repos). It is possible to add config at project level as well as stated [here](https://cljdoc.org/d/clj-kondo/clj-kondo/2020.09.09/doc/configuration).

Here is the config to setup `:clj` as default reader:

```clojure
{:cljc {:features #{:clj}}}
```

If you don’t specify a default reader, `clj-kondo` will trigger lots of error if you don’t provide the `:default` reader because it assumes that you might run the code on a platform that doesn’t match any of the provided reader.

## Step 2 (optional):  Add type hints

Magic supports the same shorthands as in Clojure: [Magic types shorthands](https://github.com/nasser/magic/blob/master/src/magic/analyzer/types.clj#L37).

### Value Type hints

We want to add Magic type hints in our Clojure code to prevent slow argument boxing at run time.

The main place we want to add the type hints are the function arguments such as in:

```clojure
(defn straights-n
  "Returns all possible straights with given length of cards."
  [n cards wheel?]
  #?(:clj  [n cards wheel?]
     :cljr [^int n cards ^Boolean wheel?])
  (...))
```

Note the user conditionals here to not affect our Clojure codes and tests to be run on the JVM. 

I did not remove the reader conditionals here (the shorthands being the same in both Clojure and Magic It would run), because we don’t want our Clojure tests to be affected and we want to keep the dynamic idiom of Clojure. Also `wheel?` could very likely have the value `nil`, passed by one of the tests, which is in fact not a boolean.

So we want to keep our type hints in the `:cljr` reader to prevent Magic from doing slow reflection but we don’t want to affect our `:clj` reader that must remain dynamic and so type free to not alter our tests.

### Ref Type hints

One of the best benefit of type hinting for Magic is to type hint records and their fields.

Here is an example of a record fields type hinting:

```clojure
(defrecord GameState #?(:clj  [players next-pos game-over?]
                        :cljr [players ^long next-pos ^boolean game-over?])
(...))
```

As you can see, not all fields are type hinted because for some, we don’t have a way to do so.

There is no way to type hints a collection parameter in Magic.

`players` is a vector of `Players` records. We don’t have a way to type hints such type. Actually we don’t have a way to type hints a collection in Magic. In Clojure (Java), we can type hint a collection of a known types such as:

```clojure
;; Clojure file
user> (defn f
      "`poker-cards` is a vector of `PokerCard`."
      [^"[Lmyproj.PokerCard;" poker-cards]
         (map :num poker-cards))
;=> #'myproj.combination/f

;; Clojure REPL
user> (f [(->PokerCard :d :3) (->PokerCard :c :4)])
;=> (:3 :4)
```

However, in Magic, such thing is not possible.

parameters which are `maps` do not benefit much from type hinting because a map could be a `PersistentArrayMap`, a `PersistentHashMap` or even a `PersistentTreeMap` so we would need to just `^clojure.lang.APersistentMap` just to be generic which is not really relevant.

To type hint a record as parameter, it is advices to `import` it first to avoid having to write the fully qualified namespace:

```clojure
;; Import the Combination class so we can use type hint format ^Combination
#?(:cljr (:import [myproj.combination Combination]))
```

Then we can type hint a parameter which is a record conveniently such as:

```clojure
(defn pass?
  "Returns true it the combi is a pass."
  #?(:clj [combi]
     :cljr [^Combination combi])
  (combi/empty-combi? combi))
```

A record field can also a be a known record types such as:

```clojure
(defrecord Player #?(:clj  [combi penalty?]
                     :cljr [^Combination combi
                            ^boolean penalty?]))
```

### Type hints and testing

Since in Clojure, we tend to use simplified parameters to our function to isolate the logic being tested (a map instead of a record, nil instead of false, a namespaced keyword instead of a map etc.), naturally lots of tests will fail in the CLR because of the type hints.

We don’t want to change our test suite with domain types so you can just add a reader conditionals to the tests affected by the type hints in the CLR.

### Interop common cases

#### Normal case

For interop, you can use the reader conditionals such as in:

```clojure
(defn round-perc
  "Rounds the given `number`."
  [number]
  #?(:clj  (-> number double Math/round)
     :cljr (-> number double Math/Round long)))
```

#### Deftype equals methods override

For the `deftype` to work in the CLR, we need to override different equals methods than the Java ones. In Java we use `hashCode` and `equal` but in .net we use `hasheq` and `equiv`.

Here is an example on how to override such methods:

```clojure
(deftype MyRecord [f-conj m rm]
  ;; Override equals method to compare two MyRecord.
  #?@(:clj
      [Object
       (hashCode [_] (.hashCode m))
       (equals [_ other]
               (and (instance? MyRecord other) (= m (.m other))))]
      :cljr
      [clojure.lang.IHashEq
       (hasheq [_] (hash m))
       clojure.lang.IPersistentCollection
       (equiv [_ other]
              (and (instance? MyRecord other) (= m (.m other))))]))
```

#### Defecord empty method override for IL2CCP

For the `defrecord` to work in case we target **IL2CPP** (all our apps), you need to override the default implementation of the `empty` method such as:

```clojure
(defrecord PokerCard [^clojure.lang.Keyword suit ^clojure.lang.Keyword num]
  #?@(:cljr
      [clojure.lang.IPersistentCollection
       (empty [_] nil)]))
```

Note the vector required with the **splicing** reader conditional `#?@`.

## Step 3: Manage dependencies

Since magic was created before `tools.deps` or `leiningen`, it has its own deps management system and the dedicated file for it is `project.edn`.

Here is an example of a project.edn:
```clojure
{:name         "My project"
 :source-paths ["src" "test"]
 :dependencies [[:github skydread1/clr.test.check "magic"
                 :sha "a23fe55e8b51f574a63d6b904e1f1299700153ed"
                 :paths ["src"]]
                [:gitlab my-private-lib1 "master"
                 :paths ["src"]
                 :sha "791ef67978796aadb9f7aa62fe24180a23480625"
                 :token "r7TM52xnByEbL6mfXx2x"
                 :domain "my.domain.sg"
                 :project-id "777"]]}
```

Refer to the Nostrand [README](https://github.com/nasser/nostrand/blob/master/README.md) for more details.

So you need to add a `project.edn`at the root of your directory with other libraries.

## Step 4: Compile to the CLR

### Nostrand

[nasser/nostrand](https://github.com/nasser/nostrand) is for magic what [tools.deps](https://github.com/clojure/tools.deps.alpha) or [leiningen](https://github.com/technomancy/leiningen) are for a regular Clojure project. Magic has its own dependency manager and does not use tools.deps or len because it was implemented before these deps manager came out!

You can find all the information you need to build and test your libraries in dotnet in the [README](https://github.com/nasser/nostrand/blob/master/README.md).

In short, you need to clone nostrand and create a dedicated Clojure namespace at the root of your project to run function with Nostrand.

### Build your Clojure project to .net

In my case I named my nostrand namespace `dotnet.clj`.

You cna have a look at the [clr.test.check/dotnet.clj](https://github.com/skydread1/clr.test.check/blob/magic/dotnet.clj), it is a port of clojure/test.check that compiles in both JVM and CLR.

We have the following require:
```
(:require [clojure.test :refer [run-all-tests]]
          [magic.flags :as mflags])
```

Don’t forget to set the 2 magic flags to true:

```clojure
(defn build
  "Compiles the project to dlls.
  This function is used by `nostrand` and is called from the terminal in the root folder as:
  nos dotnet/build"
  []
  (binding [*compile-path*                  "build"
            *unchecked-math*                *warn-on-reflection*
            mflags/*strongly-typed-invokes* true
            mflags/*direct-linking*         true
            mflags/*elide-meta*             false]
    (println "Compile into DLL To : " *compile-path*)
    (doseq [ns prod-namespaces]
      (println (str "Compiling " ns))
      (compile ns))))
```

To build to the `*compile-path*` folder, just run the `nos` command at the root of your project:

```clojure
nos dotnet/build
```

## Step 5: Test your Clojure project to .net

Same remark as for the build section:

```clojure
(defn run-tests
  "Run all the tests on the CLR.
  This function is used by `nostrand` and is called from the terminal in the root folder as:
  nos dotnet/run-tests"
  []
  (binding [*unchecked-math*                *warn-on-reflection*
            mflags/*strongly-typed-invokes* true
            mflags/*direct-linking*         true
            mflags/*elide-meta*             false]
    (doseq [ns (concat prod-namespaces test-namespaces)]
      (require ns))
    (run-all-tests)))
```

To run the tests, just run the `nos` command at the root of your project:

```clojure
nos dotnet/run-tests
```

## Example of a Clojure library ported to Magic

An example of a Clojure library that has been ported to Magic is [skydread1/clr.test.check](https://github.com/skydread1/clr.test.check/tree/magic), a fork of clojure/clr.test.check.
My fork uses reader conditionals so it can be run and tested in both JVM and CLR.

## Learn more

Now that your library is compiled to dotnet, you can learn how to package it to nuget, push in to your host repo and import in Unity in this article:
- [Pack, Push and Import Clojure to Unity](https://www.loicblanchard.me/#/blog/run-clojure-in-unity)