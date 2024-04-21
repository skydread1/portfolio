#:post{:id "magic-clojure-compiler-to-clr"
       :page :portfolio
       :employer "Flybot Pte Ltd" 
       :date ["2021-02-01" "2022-12-09"]
       :repos [["Magic" "https://github.com/nasser/magic"] 
               ["Nostrand" "https://github.com/nasser/nostrand"]
               ["Magic.Unity" "https://github.com/nasser/Magic.Unity"] 
               ["magic-pipeline" "https://github.com/magic-clojure/magic"]]
       :articles [["Port your Clojure lib to the CLR with MAGIC" "../blog/port-clj-lib-to-clr"]
                  ["Pack, Push and Import Clojure to Unity" "../blog/clojure-in-unity"]]
       :title "Magic Compiler and Nostrand"
       :css-class "magic-nostrand"
       :tags ["Clojure" "C#" "JVM" "CLR" "Compiler" "Interop" "NuGet" "Unity"]
       :image #:image{:src "/assets/magic-book.jpg"
                      :src-dark "/assets/magic-book.jpg"
                      :alt "Data Light Wallpaper"}}
+++
More acting as a devops this time, I worked on integrating the Magic Compiler and its tooling to Flybot's development workflow.

`Magic` is a bootstrapped compiler written in Clojure that takes Clojure code as input and produces dotnet assemblies (.dll) as output. The dlls produced by Magic can be run in the Game engine `Unity` which Flybot uses for their card game UIs. The goal was to be able to compile our backend Clojure APIs to dlls so we can used that logic in Unity directly.

Working closely with the author of the Magic compiler [Ramsey Nasser](https://github.com/nasser), I helped improving the tooling around the Magic compiler so it integrates well with our workflow Clojure Backend/Unity Frontend. I notably simplified the way the a clojure project and its dependencies are compiled to dlls, packed to NuGet, deployed to online repos and finally imported to Unity.
+++
## Rational

More acting as a devops this time, I worked on integrating the Magic Compiler and its tooling to Flybot's development workflow.

`Magic` is a bootstrapped compiler written in Clojure that takes Clojure code as input and produces dotnet assemblies (.dll) as output. The dlls produced by Magic can be run in the Game engine `Unity` which Flybot uses for their card game UIs. The goal was to be able to compile our backend Clojure APIs to dlls so we can used that logic in Unity directly.

There are 4 main open-source libraries involved:
- [nasser/magic](https://github.com/nasser/magic): clojure->dotnet compiler written in Clojure
- [nasser/nostrand](https://github.com/nasser/nostrand): dependencies manager for the magic compiler
- [nasser/Magic.Unity](https://github.com/nasser/Magic.Unity): runtime for Unity
- [magic-clojure/magic](https://github.com/magic-clojure/magic): pipeline to build magic and update tools

Working closely with the author of the Magic compiler [Ramsey Nasser](https://github.com/nasser), I helped improving the tooling around the Magic compiler so it integrates well with our workflow Clojure Backend/Unity Frontend.

My contributions were to:
- Fix some high level issues on the compiler that were preventing us from compiling our Clojure projects
- Report compiling issues and performance issues to Ramsey Nasser so he can improve the compiler.
- Improve the tooling around the Magic compiler to make it easier for our developers to compile/test/package Clojure libraries in Dotnet
- Successfully port our Clojure projects to Unity
- Improve the way a project and its dependencies are compiled the dlls
- Make it easy to package the newly compiled dlls in NuGet packages
- Allow developers to deploy these packages in online GitHub repos (public or private)
- Package the dlls in a way it is easy to import them into Unity projects

## Nostrand

### Why Nostrand

`nostrand` is for magic what [tools.deps](https://github.com/clojure/tools.deps.alpha) or [leiningen](https://github.com/technomancy/leiningen) are for a regular Clojure project. Magic has its own dependency manager and does not use `tools.deps` or `len` because it was implemented before these deps manager came out!

### Private Gitlab support

Since we wanted to compile private gitlab projects with deps on both private gitlab repos and public repos, I added the Gitlab support and private repo supports using the Github/GitLab tokens.

### Nuget pack and push

Adding a `.csproj` that refers to a `.nuspec` to the Clojure repo at the root allows me to pack and deploy the generated dlls to a nuget package that will be store on the Remote git repo. For private repositories, a `nuget.config` can be added to specify the `PAT` token for GitHub or `Deploy token` for Gitlab. The package is then added to GitHub/GitLab Package Registry.

### Example of a Clojure library ported to Magic

An example of a Clojure library that has been ported to Magic is [skydread1/clr.test.check](https://github.com/skydread1/clr.test.check/tree/magic), a fork of clojure/clr.test.check.

If you have a look at the [dotnet.clj](https://github.com/skydread1/clr.test.check/blob/magic/dotnet.clj) namespace, you can see the different convenient function that can be called by `nostrand` with the command `nos`:

- compile the clojure codebase to dotnet assemblies:
```
nos dotnet/build
```
- run all the clojure tests using the CLR:
```
nos dotnet/run-tests
```
- pack and push NuGet packages to the GitHub/GitLab Package Registries:
```
nos dotnet/nuget-push
```

So it only 3 commands, a developer can compile a Clojure project and its deps to dotnet assemblies that can be run in Unity, test that all the tests are passing in the CLR and push a NuGet Package in a remote public or private repository.

## Magic Unity

The goal of Magic.Unity is to provide a Clojure runtime in Unity.

[Magic.Unity](https://github.com/nasser/Magic.Unity) used to have a compilation UI and a runtime. However, there was a mismatch between the Magic dlls of Nostrand and Magic.Unity. Also the compilation UI was not easy to use and we wanted to use Nostrand directly. The compilation has since been removed and Magic.Unity is now only a runtime that can use the last magic dlls.

Also, I added the `.nuspec` and `dotnet.clj` to the repo so we can easily package it with NuGet and push it to the repo. Therefore, it can be imported in Unity the same way we import our Clojure libraries.

## Magic compiler

### What is the Magic Compiler

Magic is a bootstrapped compiler written in Clojure that takes Clojure code as input and produces dotnet assemblies (.dll) as output.

Compiler Bootstrapping is the technique for producing a self-compiling compiler that is written in the same language it intends to compile. In our case, MAGIC is a **Clojure** compiler that compiles **Clojure** code to .**NET** assemblies (.dll and .exe files).

It means we need the old dlls of MAGIC to generate the new dlls of the MAGIC compiler. We repeat this process until the compiler is good enough. 

The very first magic dlls were generated with the [clojure/clojure-clr](https://github.com/clojure/clojure-clr) project which is also a Clojure compiler to CLR but written in **C#** with limitations over the dlls generated (the problem MAGIC intends to solve).

### Why the Magic Compiler

There is already a clojure->clr compiler [clojure/clojure-clr](https://github.com/clojure/clojure-clr). However, clojure-clr uses a technology called the DLR (dynamic language runtime) to optimize dynamic call sites but it emits self modifying code which make the assemblies not usable on mobile devices (IL2CPP in Unity). So we needed a way to have a compiler that emit assemblies that can target both Desktop and mobile (IL2CPP), hence the Magic compiler.

### Documentations and Bug reports

I do not have the knowledge for such low level compiler implementation, so I did not fix any issues on the compiler myself. However, I could help Ramsey Nasser on improving the documentation for both user and potential contributors and fix some high level small issues. I was also reporting all the bugs and creating the issues on the different related repos.

### GitHub Action

I added a GitHub action to perform the bootstrapping at every push to automate the process and make the latest dlls available in the GitHub action artifact to anybody.

## Importing the nuget packages to our frontend Unity projects

Since all the Clojure libraries and the Magic.Unity were packaged via nugget and pushed to the GitHub/GitLab repo, we can use a `packages.config` to list our packages and use the command `nuget restore` to import them. Same as to push packages, a `nuget.config` can be added with the credentials.

## Learn more

You can learn more about `Magic` in my blog articles below.
