<p align="center">
    <img src="https://github.com/SirDank/KnightLauncher/blob/main/src/main/resources/rsrc/img/icon-128.png?raw=true" height="128">
</p>
<h1 align="center">Knight Launcher</h1>
<p align="center">
    <a href="https://github.com/SirDank/KnightLauncher/blob/main/LICENSE"><img alt="GitHub license" src="https://img.shields.io/github/license/SirDank/KnightLauncher?style=flat-square"></a>
    <a href="https://github.com/SirDank/KnightLauncher/issues"><img alt="GitHub issues" src="https://img.shields.io/github/issues/SirDank/KnightLauncher?style=flat-square"></a>
    <a href="https://github.com/SirDank/KnightLauncher/stargazers"><img alt="GitHub stars" src="https://img.shields.io/github/stars/SirDank/KnightLauncher?style=flat-square"></a>
</p>
<p align="center">
    <a href="https://github.com/SirDank/KnightLauncher/releases/"><img alt="Total downloads" src="https://img.shields.io/github/downloads/SirDank/KnightLauncher/total.svg"></a>
    <a href="https://discord.gg/RAf499a"><img alt="Discord" src="https://img.shields.io/discord/653349356459786240" target="_blank"></a>
</p>

Open source game launcher for Spiral Knights. Supports automatic 64-bit Java VM installation, Discord integration, easier modding & much more.

[![ko-fi](https://www.ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/W4W11S2JU)

-----

## Features
- Install and uninstall game mods in an easy and noob-friendly way without worrying about game updates.
- Jump into several editors such as a model viewer and a scene editor to create custom user-generated content.
- Automatically patch your game to use a 64-bit Java VM to improve the game's performance.
- Re-install and verify your game files with ease.
- Intuitive and user-friendly UI for configuring advanced Java VM settings.
- Discord integration that shows your current game activity on your Discord status. This feature is only available on Windows.
- Launch alt accounts with lesser resources assigned to them avoiding losing performance on your main instance.
- Future-proofed to support third party servers. Third party server support __**will not**__ be enabled while Grey Havens' Official servers are still online, unless explicitly permitted by their respective right owners and/or law.
- And more...

## Downloading & Installing

1. Prerequisites:
   - [Java](https://www.java.com/en/download/) installed.
2. Download the latest Knight Launcher release ZIP file [here](https://github.com/SirDank/KnightLauncher/releases/latest).
3. Extract the downloaded `KnightLauncher-x.x.x.zip` ZIP file to your Spiral Knights folder (`x.x.x` will vary depending on the current version).
   - **Windows**: Steam installations can be found by right-clicking the game on your Steam library and selecting "Manage" → "Browse Local Files," you can also look for it at `<Your Steam Folder>\steamapps\common\Spiral Knights`. Standalone installations can be found by right-clicking the Spiral Knights shortcut on your desktop and then clicking "Open File Location," you can also look for it at `<Your User Folder>\AppData\LocalLow\spiral`.
   - **macOS**: Steam installations can be found at `/Users/<Your User Folder>/Library/Application Support/Steam/steamapps/common/Spiral Knights/`. Standalone installations can be found at `/Users/<Your User Folder>/Library/Application Support/spiral`. For both types of installations you'll find a `Spiral Knights.app` file which you need to open, then browse to `Contents/Resources/Java`, this is the game's folder.
   - **Linux**: Steam installations are not natively supported, but the launcher will still call Steam and launch through Proton if you place Knight Launcher's JAR file in the game's folder. Standalone installations are found at your user's home folder then `.getdown/spiral`.
   - Bear in mind that these are the *usual* installation locations, your mileage may vary.
4. Double-click the `KnightLauncher.jar` JAR file. The launcher should now boot up.
5. If nothing happens, double-click the `KnightLauncher_windows.bat` file or `KnightLauncher_mac_linux.sh` file depending on your operating system.

Still not working? Open a post in [Discord's tech support channel](https://discord.gg/m6TT9PM9B7).

## Updating

The launcher is capable of auto-updating itself given the option is enabled in the launcher's settings, which will be by default. To manually update, follow these steps:
1. Download the latest Knight Launcher release ZIP file [here](https://github.com/SirDank/KnightLauncher/releases/latest).
2. Extract the downloaded ZIP file to your Spiral Knights folder, the same way you did when installing it for the first time. Guidance on finding your game folder can be found above at "Downloading & Installing."

After following those steps, the launcher will be up to date.

## Building From Source
To build Knight Launcher manually, follow these steps:

1. Prerequisites 
   - [Java 8 JDK](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html) installed.
   - [Maven](https://maven.apache.org/download.cgi) installed:
     - **Windows**: Download the ZIP from the link above, extract, then add `bin/` to your `PATH`.
     - **macOS (Homebrew)**: `brew install maven`.
     - **Linux (APT)**: `apt install maven`.
   - [Git](https://git-scm.com/downloads) installed:
     - **Windows**: Download the installer from [git-scm.com](https://git-scm.com/downloads) and follow the setup.
     - **macOS (Homebrew)**: `brew install git`.
     - **Linux (APT)**: `apt install git`.
2. Clone the repository.
   - `git clone https://github.com/SirDank/KnightLauncher.git`
3. Copy `projectx-pcode.jar` from your Spiral Knights `code` directory into the project's `lib` directory.
4. Create a copy of `deploy.properties.template` in the repository's root directory then remove the `.template` extension. Fill any missing values.
5. Validate all Maven dependencies.
   - `mvn validate`
6. Build the package using Maven. By default, it will build for a development environment, you can target prod instead by adding `-Denv=prod`.
   - `mvn clean package`
   - `mvn -Denv=prod clean package`
7. Copy the package built by Maven to your Spiral Knights folder and run it.
   - `java -jar KnightLauncher.jar`

## Discord
We've built an amazing community on Discord focused on both helping newcomers get along with the launcher and giving a hand to modders, come join us!

https://discord.gg/RAf499a

-----

## Credits

Unless hyperlinked names shown below represent Discord usernames. Hyperlinked names correspond to GitHub accounts.

### Contributors
<a href="https://github.com/lucasluqui/KnightLauncher/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=lucasluqui/KnightLauncher" />
</a>

### Translators
Translators are credited in their respective language file, [click here to browse](https://github.com/lucasluqui/KnightLauncher/tree/main/src/main/resources/rsrc/lang).

Thank you all for helping to make Knight Launcher usable for everyone worldwide!

### QA/Testing
- Nurr, yihleego, Bidoknight, Carpvindra, Mushspore, CafuneAndChill, Xan, analarmingalarm, parma, loonadra, ultrongr, milliath, Puzovoz, Stret, 3xample, top_platinum, Criptobeta.

### Third Party Libraries
The following open source libraries are used by Knight Launcher:

- [Guava](https://github.com/google/guava)
- [Guice](https://github.com/google/guice)
- [Apache Commons IO](https://github.com/apache/commons-io)
- [Apache Commons Text](https://github.com/apache/commons-text)
- [Image4J](https://github.com/imcdonagh/image4j)
- [Zip4J](https://github.com/srikanth-lingala/zip4j)
- [flatlaf](https://github.com/JFormDesigner/FlatLaf)
- [discord-rpc](https://github.com/Vatuu/discord-rpc)
- [mslinks](https://github.com/DmitriiShamrikov/mslinks)
- [org.json](https://github.com/eskatos/org.json-java)
- [jIconFont](https://github.com/jIconFont)
- [samskivert](https://github.com/samskivert/samskivert)
- [JHLabs Filters](http://www.jhlabs.com/)
- [OSHI](https://github.com/oshi/oshi)
