# Planetiler example project

Java 22 adds the ability to [run multi-file Java programs directly from .java files](https://openjdk.org/jeps/458) without a compile step or build tools. Vscode's [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) lets you edit Java files like this with a single jar file as a dependency. These make it easy to edit, run, and debug custom Java profiles for planetiler without complex setup or external tools.

To get started:

1. Clone this repo
1. Install Java 22 or later. You can download Java JDK (not JRE) manually from [Adoptium](https://adoptium.net) or use a [package manager](https://adoptium.net/installation/) or [platform installer](https://adoptium.net/installation/#_installers)
1. Download latest planetiler release jar `wget https://github.com/onthegomap/planetiler/releases/download/v0.8.0-pre.1/planetiler.jar`

Then run the profile:

```bash
java -cp planetiler.jar Power.java --download
```

And it should create `power.pmtiles` that you can inspect [here](https://protomaps.github.io/PMTiles/).

## IDE Setup

1. Install [VSCode](https://code.visualstudio.com/download) and the [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
1. In VSCode, click `File -> Open` and navigate to Planetiler directory (If VSCode asks (and you trust the code) then click `Yes I trust the authors`)
1. Open `Power.java`, it should automatically recognize this is a Java project and add `planetiler.jar` to the project classpath.
1. Click `Run` button that shows up above the `public static void main(String[] args)` method.

If VSCode can't find the Java 22 installation, open `Java: Configure Java Runtime` from the command pallete and point it to the installation.
