#:post{:id "magic-clojure-compiler-to-clr"
       :order 1
       :page :portfolio
       :title "2021-2022 | Magic Compiler and Nostrand"
       :css-class "magic-nostrand"
       :image-beside #:image{:src "/assets/datawp.jpg"
                             :src-dark "/assets/datawp.jpg"
                             :alt "Data Light Wallpaper"}}
+++
# Port Clojure libraries to Unity using Magic (2021 - 2022)

## Rational

More acting as a devops this time, I worked on integrating the Magic Compiler and its tooling to our own development workflow.

There are 4 main open-source libraries involved:
- [nasser/magic](https://github.com/nasser/magic): clojure->dotnet compiler written in Clojure
- [nasser/nostrand](https://github.com/nasser/nostrand): deps manager and REPL for the magic compiler
- [nasser/Magic.Unity](https://github.com/nasser/Magic.Unity): runtime for Unity
- [magic-clojure/magic](https://github.com/magic-clojure/magic): pipeline to build magic and update tools

Working closely with the author of the Magic compiler [Ramsey Nasser](https://github.com/nasser), I help improving the tooling around the Magic compiler so it integrates well with our workflow Clojure Backend/Unity Frontend.

My contributions were to:
- Fix some high level issues on the compiler that were preventing us from compiling our Clojure projects
- Report compiling issues and performance issues to Ramsey Nasser so he can improve the compiler.
- Improve the tooling around the Magic compiler to make it easier for our developers to compile/test/package Clojure libraries in Dotnet
- Successfully port our Clojure projects to Unity

## Preparing our Clojure projects for compilation

Firstly, I added the reader conditionals to the namespaces with interop.

## Nostrand

### Why Nostrand

`nostrand` is for magic what [tools.deps](https://github.com/clojure/tools.deps.alpha) or [leiningen](https://github.com/technomancy/leiningen) are for a regular Clojure project. Magic has its own dependency manager and does not use tools.deps or len because it was implemented before these deps manager came out!

### Private Gitlab support

Since we wanted to compile private gitlab projects with deps on both private gitlab repos and public repos, I added the Gitlab support and private repo supports using the Github/GitLab tokens.

### Nuget pack and push

Adding a `.csproj` that refers to a `.nuspec` to the Clojure repo at the root allows us to be able to pack and deploy the generated dlls to a nuget package that will be store on the Remote git repo. For private repositories, a `nuget.config` can be added to specify the `PAT` token for GitHub or `Deploy token` for Gitlab. The package is then added to GitHub/GitLab Package Registry.

### Example of a Clojure library ported to Magic

An example of a Clojure library that has been ported to Magic is [skydread1/clr.test.check](https://github.com/skydread1/clr.test.check/tree/magic), a fork of clojure/clr.test.check.
My fork uses reader conditionals so it can be run and tested in both JVM and CLR.

If you have a look at the [dotnet.clj](https://github.com/skydread1/clr.test.check/blob/magic/dotnet.clj) namespace, you can see the different convenient function that can be called by `nostrand` with the command `nos`:

- compile:
```
nos dotnet/build
```
- run all the tests:
```
nos dotnet/run-tests
```
- pack and push NuGet packages to the repo Package Registry:
```
nos dotnet/nuget-push
```

## Magic Unity

[Magic.Unity](https://github.com/nasser/Magic.Unity) used to have a compilation UI and a runtime. However, there was a mismatch between the Magic dlls of Nostrand and Magic.Unity. Also the compilation UI was not easy to use and we wanted to use Nostrand directly. The compilation has since been removed and Magic.Unity is now only a runtime that can use the last magic dlls

Also, I added the `.nuspec` and `dotnet..clj` to the repo so we can easily nuget pack and push it to the repo. Therefore, it can be imported in Unity the same way we import our Clojure libraries.

## Magic compiler

### What is the Magic Compiler

Magic is a bootsrapped compiler writhen in Clojure that take Clojure code as input and produces dotnet assemblies (.dll) as output.

Compiler Bootstrapping is the technique for producing a self-compiling compiler that is written in the same language it intends to compile. In our case, MAGIC is a **Clojure** compiler that compiles **Clojure** code to .**NET** assemblies (.dll and .exe files).

It means we need the old dlls of MAGIC to generate the new dlls of the MAGIC compiler. We repeat this process until the compiler is good enough. 

The very first magic dlls were generated with the [clojure/clojure-clr](https://github.com/clojure/clojure-clr) project which is also a Clojure compiler to CLR but written in **C#** with limitations over the dlls generated (the problem MAGIC is intended to solve).

### Why the Magic Compiler

The already existing clojure->clr compiler [clojure/clojure-clr](https://github.com/clojure/clojure-clr). However, clojure-clr uses a technology called the DLR (dynamic language runtime) to optimize dynamic call sites but it emits self modifying code which make the assemblies not usable on mobile devices (IL2CPP in Unity). So we needed a way to have a compiler that emit assemblies that can target both Desktop and mobile (IL2CPP), hence the Magic compiler.

### Documentations and Bug reports

I do not have the knowledge for such low level compiler implementation, so I did not fix any issues on the compiler myself. However, I could help Ramsey Nasser on improving the documentation for both user and potential contributors and fix some high level small issues. I was also reporting all the bugs and creating the issues on the different related repos.

### GitHub Action

I added a GitHub action to perform the bootstrapping at every push to automate the process and make the latest dlls available in the GitHub action artifact to anybody.

## Importing the nuget packages to our frontend Unity projects

Since all the Clojure libraries and the Magic.Unity were packaged via nugget and pushed to the GitHub/GitLab repo, we can use a `packages.config` to list our packages and use the command `nuget restore` to import them. Same as to push packages, a `nuget.config` can be added with the credentials.

## Learn more

I will write articles on how to port a library to Magic soon.