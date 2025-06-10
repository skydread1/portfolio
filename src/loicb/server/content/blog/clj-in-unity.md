#:post{:id "clojure-in-unity"
       :page :blog
       :date ["2022-04-22"]
       :title "Pack, Push and Import Clojure to Unity"
       :repos [["Magic" "https://github.com/nasser/magic"] 
               ["Nostrand" "https://github.com/nasser/nostrand"]
               ["Magic.Unity" "https://github.com/nasser/Magic.Unity"]]
       :css-class "blog-clj-in-unity"
       :tags ["Clojure" "Compiler" "CLR" "Unity"]
       :image #:image{:src "/assets/loic-blog-logo.png"
                      :src-dark "/assets/loic-blog-logo.png"
                      :alt "Logo referencing Aperture Science"}}
+++
How to package a Compiled Clojure lib (With MAGIC compiler) to NuGet, push it to remote and import it to Unity.
+++
## Prerequisites

Your Clojure library is assumed to be already compiled to dotnet.

To know how to do this, refer to the article: [Port your Clojure lib to the CLR with MAGIC](https://www.loicb.dev/blog/port-clj-lib-to-clr)

## Goal

In this article, I will show you:
- how to package your lib to NuGet
- push it in to your host repo
- import in Unity in this article

## Build the dlls with Nostrand

Just use the command `nos dotnet/build` at the root of the Clojure project.

The dlls are by default generated in a `/build` folder.

## Dependency management

A `.csproj` file (XML) must be added at the root of the Clojure project.

You can find an example here: [clr.test.check.csproj](https://github.com/skydread1/clr.test.check/blob/magic/clr.test.check.csproj)

```xml
<Project Sdk="Microsoft.NET.Sdk">
    <PropertyGroup>
        <TargetFrameworks>netstandard2.0</TargetFrameworks>
    </PropertyGroup>
    <PropertyGroup>
        <NuspecFile>clr.test.check.nuspec</NuspecFile>
        <RestoreAdditionalProjectSources>
            https://api.nuget.org/v3/index.json
        </RestoreAdditionalProjectSources>
    </PropertyGroup>
</Project>
```

There is no need to add References as they were already built by Nostrand in the `/build` folder.

Note the `NuspecFile` that is required to use the nuspec.

## Package Manager

A `.nuspec` file (XML) must be added at the root of the Clojure project.

The `references` are the references to the dlls in `/build`.

You can find an example here: [clr.test.check.nuspec](https://github.com/skydread1/clr.test.check/blob/magic/clr.test.check.nuspec)

```xml
<?xml version="1.0" encoding="utf-8"?>
<package>
    <metadata>
        <id>clr.test.check</id>
        <version>1.1.1</version>
        <title>clr.test.check</title>
        <authors>skydread1</authors>
        <description>Contains the core references for the Clojure lib test.check.</description>
        <repository type="git" url="https://github.com/skydread1/clr.test.check" />
        <dependencies>
            <group targetFramework="netstandard2.0"></group>
        </dependencies>
    </metadata>
    <files>
        <file src="build\*.clj.dll" target="lib\netstandard2.0" />
    </files>
</package>
```

The `dependency` tag is required to indicate the targeted framework.

The `file` (using a wild card to avoid adding the files one by one) is required to add the dlls files that will be available for the consumer. So the target must be `lib\TFM`.

In our case, Unity recommends to use `netstandard2.0` so our target is `lib\netstandard2.0`.

## GitHub/GitLab local config

To push the package to a git host, one of the most convenient way is to have a `nuget.config` (XML) locally at the root of the project.

### The nuget.config for GitHub

```xml
<?xml version="1.0" encoding="utf-8"?>
<configuration>
    <packageSources>
        <clear />
        <add key="github" value="https://nuget.pkg.github.com/skydread1/index.json" />
    </packageSources>
    <packageSourceCredentials>
        <github>
            <add key="Username" value="skydread1" />
            <add key="ClearTextPassword" value="PAT" />
        </github>
    </packageSourceCredentials>
</configuration>
```

In order to push a Package to a `Package Registry` to your GitHub project repo, you will need to create a **PAT** (Personal Access Token) with the `write:packages` ,`:read:packages` and `delete:packages` permissions.

Replace Username value by your Github username

Replace Token value by your newly created access token

Replace the repo URL by the path to your GitHub **account page** (not the repo).

*Note: Do not push your config in GitHub as it contains sensitive info (your PAT), it is just for local use.*

### The nuget.config for GitLab

```xml
<?xml version="1.0" encoding="utf-8"?>
<configuration>
    <packageSources>
        <clear />
        <add key="gitlab" value="https://sub.domain.sg/api/v4/projects/777/packages/nuget/index.json" />
    </packageSources>
    <packageSourceCredentials>
        <gitlab>
            <add key="Username" value="deploy-token-name" />
            <add key="ClearTextPassword" value="deploy-token-value" />
        </gitlab>
    </packageSourceCredentials>
</configuration>
```

In order to push a Package to a `Package Registry` to your GitLab project repo, you will need to create a **deploy token** (not access token) with the `read_package_registry` and `write_package_registry` permissions.

Replace Username value by your token username

Replace Token value by your newly created deploy token

Replace the domain (for private server) and project number in the GitLab URL. (don’t forget the index.json at the end)

*Note: Do not push your config in GitLab as it contains sensitive info (your deploy token), it is just for local use.*

## Pack and Push nuget packages with Nostrand

At the root of the project, the `dotnet.clj` contains the convenient function to be used with [nasser/nostrand](https://github.com/nasser/nostrand).

You can find an example here: [dotnet.clj](https://github.com/skydread1/clr.test.check/blob/magic/dotnet.clj)

We added to our Clojure library a convenient function to avoid having to manually use the dotnet commands, you can just run at the root at the Clojure directory:

```bash
nos dotnet/nuget-push
```

This will create the nuget code package `.nupkg` file in the folder `bin/Release`. the name is the package name and the version such as `clr.test.check.1.1.1.nupkg`.

It will then push it to either Gitlab or Github depending on the host using the credentials in `nuget.config`.

It is equivalent to the 2 dotnet commands:

```bash
dotnet pack --configuration Release
dotnet nuget push "bin/Release/clr.test.check.1.1.1.nupkg" --source "github"
```

**Note**: for a Clojure project, you can let the default option for the packing. There is no need to build in theory as we already have our dlls ready in our `/build` folder. The `dotnet build` will just create a unique dll with the name of your library that you can just ignore.

## Download nuget Packages

Using package references is the new way of doing this but it does not work with Unity.

### Import nuget packages to a regular C# project

The new way of importing the nuget packages is to use the `PackageReference` tag directly in the `.csproj` file such as:

```bash
<PackageReference Include="Sitecore.Kernel" Version="12.0.*" />
```

But this method only works if you are using the `.csproj` file which we don’t use in Unity as we use the `manifest.json`.

## Import nuget packages to a Unity project

Unity uses a json file in `Packages/manifest.json` to download deps. However it does not work for nuget packages.

There is no `.csproj` at the root so we cannot use the method above, and all the other underlying `csproj` are generated by Unity so we cannot change them.

The only choice we have is to use the old way of importing the nuget packages which is to use a `packages.config` and then use the command `nuget restore` to fetch the packages last versions.

So we need to add 2 config files in our root of our Unity project:

- `nuget.config` : github/gitlab credentials
- `packages.config` : packages name and their version/target

### nuget.config

In order to fetch all the packages at once using `nuget restore`, we need to add locally the `nuget.config` with the different sources and credentials.

So to restore our GitHub and GitLab packages from our example, we use the following `nuget.restore`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<configuration>
    <config>
	      <add key="repositoryPath" value="Assets/ClojureLibs" />
	  </config>
    <packageSources>
        <clear />
        <add key="gitlab" value="https://sub.domain.sg/api/v4/projects/777/packages/nuget/index.json" />
        <add key="github" value="https://nuget.pkg.github.com/skydread1/index.json" />
    </packageSources>
    <packageSourceCredentials>
        <gitlab>
            <add key="Username" value="deploy-token-name" />
            <add key="ClearTextPassword" value="deploy-token-value" />
        </gitlab>
        <github>
            <add key="Username" value="skydread1" />
            <add key="ClearTextPassword" value="PAT" />
        </github>
    </packageSourceCredentials>
</configuration>
```

The `repositoryPath` allows us to get our packages in a specific directory.
In our case, we put it in `Assets/ClojureLibs` (it needs to be in the `Asset` dir anywhere)

### packages.config

To tell Unity which packages to import while running `nuget restore`, we need to provide the `packages.config`. Here is the config in our example:

```bash
<?xml version="1.0" encoding="utf-8"?>
<packages>
  <package id="Magic.Unity" version="1.0.0" targetFramework="netstandard2.0" />
  <package id="my-private-proj" version="1.0.0" targetFramework="netstandard2.0" />
  <package id="clr.test.check" version="1.1.1" targetFramework="netstandard2.0" />
</packages>
```

### Magic.Unity

To run clojure in Unity, you need [Magic.Unity](https://github.com/nasser/Magic.Unity). It is a the runtime for Clojure compiles with Magic in Unity.

Note the `Magic.Unity` in the `packages.config` above. Magic.Unity has its own nuget package deployed the same way as you would deploy a Clojure library, so you import it along side your nuget packages with your compiles clojure libs.

### nuget restore

Once you have the github/gitlab credentials ready in `nuget.config` and the packages and their version/target listed in `packages.config`, you can run the command `nuget restore` at the root of the unity project.

If running `nuget restore` do not fetch the last version, it is because it is using the local cache.
In this case you need to force restore using those [commands](https://docs.microsoft.com/en-us/nuget/consume-packages/package-restore#force-restore-from-package-sources).

Most of the time, ignoring the cache is fixing this issue:

```bash
nuget restore -NoCache
```

Here is the packages tree of our project for instance:

```bash
~/workspaces/unity-projects/my-proj:
.
├── clr.test.check-legacy.1.1.1
│   ├── clr.test.check-legacy.1.1.1.nupkg
│   └── lib
│       └── netstandard2.0
│           ├── clojure.test.check.clj.dll
│           ├── clojure.test.check.clojure_test.assertions.clj.dll
│           ├── clojure.test.check.clojure_test.clj.dll
│           ├── clojure.test.check.generators.clj.dll
│           ├── clojure.test.check.impl.clj.dll
│           ├── clojure.test.check.random.clj.dll
│           ├── clojure.test.check.results.clj.dll
│           └── clojure.test.check.rose_tree.clj.dll
├── my-private-lib.1.0.0
│   ├── my-private-lib.1.0.0.nupkg
│   └── lib
│       └── netstandard2.0
│           ├── domain.my_prate_lib.core.clj.dll
│           └── domain.my_prate_lib.core.utils.clj.dll
```

Finally, You can add Magic.Unity (runtime for magic inside Unity) in the manifest.json like so:

```json
{
  "dependencies": {
	  ...,
    "sr.nas.magic.unity": "https://github.com/nasser/Magic.Unity.git"
	}
}
```

## Conclusion

Once you have the proper required config files ready, you can use `Nostrand` to
Build your dlls:

```
nos dotnet/build
```

Pack your dlls in a nuget package and push to a remote host:

```
nos dotnet/nuget-push
```

Import your packages in Unity:
```
nuget restore
```

`Magic.Unity` is the Magic runtime for Unity and is already nuget packaged on its public repo
