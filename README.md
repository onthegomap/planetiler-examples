# Planetiler Examples

This repo is a template for writing a standalone Java profile to make vector tiles with [planetiler](https://github.com/onthegomap/planetiler).

Until recently you needed to set up a build tool like [maven](https://maven.apache.org/) or [gradle](https://gradle.org/) to write a custom Java planetiler profile. Java 22 [adds the ability](https://openjdk.org/jeps/458) to run multi-file programs directly from .java files without a compile step, and vscode's [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) lets you easily edit and debug simple Java projects like this.

To get started:

1. Clone this repo
1. Install Java 22 or later. You can download Java JDK (not JRE) manually from [Adoptium](https://adoptium.net) or use a [package manager](https://adoptium.net/installation/) or [platform installer](https://adoptium.net/installation/#_installers)
   - **Linux get Java 22**
     - Download JDK .tar.gz version 22 for your architecture from https://adoptium.net/temurin/releases/?version=22
     - `tar -xzvf OpenJDK22U-jdk_x64_linux_hotspot_22.0.1_8.tar.gz`
     - `sudo mv jdk-22.0.1+8 /usr/lib/jvm/`
     - `sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/jdk-22.0.1+8/bin/java 1`
     - `sudo update-alternatives --config java` and select openjdk-22
1. Download latest planetiler release jar `wget https://github.com/onthegomap/planetiler/releases/download/v0.8.0-pre.1/planetiler.jar`

Then run the profile:

```bash
java -cp planetiler.jar Power.java --download
```

It should create a file `power.pmtiles` showing powerlines in Rhode Island that you can inspect with https://protomaps.github.io/PMTiles/.

## IDE Setup

1. Install [VSCode](https://code.visualstudio.com/download) and the [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
1. In VSCode, click `File -> Open` and navigate to Planetiler directory (If VSCode asks then click `Yes I trust the authors`)
1. Open `Power.java`, it should automatically recognize this is a Java project and add `planetiler.jar` to the project classpath.
1. Click `Run` button that shows up above the `public static void main(String[] args)` method.

<img width="702" alt="image" src="https://github.com/onthegomap/planetiler-example/assets/1480504/43f5a03d-54ed-4bf0-8ca2-89c50486b9a0">

## Docker

You can also run within docker by using:

```bash
docker run  -v$(pwd):/data --workdir=/data docker.io/library/eclipse-temurin:22-jdk java -cp planetiler.jar Power.java
```

NOTE: You will still need to download the JDK to edit with vscode.

## Next Steps

- Run the profile with other regions from [geofabrik](https://download.geofabrik.de/) by adding `--area=australia`
- Learn Java basics: https://www.baeldung.com/java-tutorial ([chatgpt](https://chat.openai.com/) is also useful when getting started)
- Learn more about working with Java in vscode: https://code.visualstudio.com/docs/java/java-tutorial
- Read through the other example profiles in this repo to learn what you can do with planetiler

You should be able to start a simple profile in one file, split out into multiple files as complexity grows, and only introduce a build tool if you need to manage external dependencies. `planetiler.jar` already contains the most popular dependencies you are likely to need like [Google guava](https://github.com/google/guava), [JTS](https://github.com/locationtech/jts), readers for openstreetmap, shapefiles, geopackage, and geoparquet and writers for mbtiles and pmtiles archives.

TODO:

- add more OSM examples
- add overture example
