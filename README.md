Client
=============

## Cloning
If you are using Git, use this command to clone the project: `git clone git@github.com:Spoutcraft/Client.git`

## Setup
__For [Eclipse]__  
1. Make sure you have the Gradle plugin installed (Help > Eclipse Marketplace > Gradle Integration Plugin)
2. Import Client as a Gradle project (File > Import)
3. Select the root folder for Client and click 'Build Model'
4. Check Client when it finishes building and click 'Finish'

__For [IntelliJ]__  
1. Make sure you have the Gradle plugin enabled (File > Settings > Plugins).
2. Click File > Import Module and select the 'build.gradle' file for Client.

## Building
__Note:__ If you do not have [Gradle] installed you can use the gradlew files included with the project in place of 'gradle' in the following command(s). If you are using Git Bash, Unix or OS X then use './gradlew'. If you are using Windows then use 'gradlew.bat'.

In order to build Client you simply need to run the `gradle` command. You can find the compiled JAR files in `~/build/libs`.

[Gradle]: http://www.gradle.org/
[Eclipse]: http://www.eclipse.org/
[IntelliJ]: http://www.jetbrains.com/idea/