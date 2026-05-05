# Overview

Android currently lacks the ability to set the phone on **vibrate** mode while allowing a select group of contacts and applications to produce audible alerts — a feature I refer to as "priority ring". The closest alternative is Modes (Settings > Modes), such as Do Not Disturb (DND). However, unlike priority ring, modes silence the device and restrict calls and notifications to only approved sources. For users who prefer to keep their phones on vibrate for discreet awareness, this solution falls short.

Priority ring can be achieved by a series of manual tweaks: 
1. Set the phone ringer to normal (loud).
2. Set the default ringtone and notification sound to `none`.
3. Set custom ringtones and notification sounds for each priority contact and app.
4. Ensure the phone is set to vibrate on calls and notifications - if desired.

The Priority Ring app helps automate steps 1 and 2 by surfacing a quick settings tile that toggles priority ring ON/OFF.
The app does not automate the rest of the steps due to Android's strict security policies which require extensive permissions and additional complexity to achieve its goals.

# Installation and setup

To enable priority ring, the user must install the app and follow a series of steps for each priority contact and app:

## Installation

1. Download or [build](#build-the-app) the app.
2. Install the app using any Android file manager.
3. Add the Priority Ring tile to you quick settings tiles (swiping down from the notification area twice and tapping the pencil icon). Make sure to stretch the tile horizontally to allow for extra information to be visible.
4. Long-press the tile to go to its app info page.
5. Under the **Advanced** section, enable the **Modify system settings** option.
6. Done.

## Setup

Users can configure both contacts and applications as priority:

For contact:
1. In the Contacts app, locate the desired contact.
2. Under **Contact settings** > **Contact ringtone**, select any sound other than the default one.
3. In the Messages app, select a conversation with the desired contact. If you currently don't have one, simply compose a new message without sending it.
2. Select the contact name at the top on the message.
3. Under **Notifications** > **Sound**, select any sound other than the default one.

Repeat the steps above for every contact in every messaging/calling app you use (WhatsApp, etc.).

For applications:

Priority ring can be achieved with any application notification as long as the application supports custom sounds for notifications. If so, set a custom sound for the desired notification that is other than the default notification sound.

# Usage

Press the Priority Ring tile to toggle priority ring ON/OFF.

> [!NOTE]
> Pressing the tile alone will simply set the ringer to normal (loud) and set/unset the default ringtone and notification sound to `none`. To achieve a true priority ring, the user must also configure their priority contacts and apps per the instruction in the [setup section](#setup).

Note that:
- Turning priority ring ON while the phone is on vibrate will switch the phone to loud (as well as set the ringtone and notification sound to `none`).
- Priority ring is unavailable while the phone is on Silent mode. This is due to Android's strict permission policies.

# Developer notes

- The app was developed without using Android Studio, so only the bare minimum dependencies and tooling was used.
- The app was developed using containers (Podman).
- The app consists of a single service class (TileService) that handles the app logic and UI.

## Dev environment setup

To recreate a development environment using Podman containers:

1. On your local machine create a dedicated directory for Android development (e.g., `~/android-dev`).
2. Clone this repository into it:
	```
	cd ~/android-dev && git clone [URL]
	```
3. Download the following dependencies for **Linux OS** (binary only, if available):
	1. [OpenJDK v21.0.2](https://jdk.java.net/archive/)
	2. [Android command line tools - latest version](https://developer.android.com/studio#command-line-tools-only)

> [!NOTE]
> The dependencies above are general dependencies for Android app development.
> App-specific dependencies, such as various Google Android libraries, will be imported automatically during the application build process.

4. Unpack and place each dependency in `~/android-dev`.

At this point, the `~/android-dev` directory should resemble the following:

```
.
└── android-dev/
    ├── jdk-21.0.2/
    ├── cmdline-tools/
    ├── gradle-9.4.1/
    └── priority-ring-app/
```

Next:

5. Create a new Linux Debian container. Make sure to mount your local `~/android-dev` folder in the container:
```
podman container create -t --name "android-dev" -v ~/android-dev:/home/android-dev debian:latest
```
6. start the container and add the following to `/.bashrc` or `/etc/bash.bashrc`:

```
export ANDROID_HOME="/home/cmdline-tools"
export JAVA_HOME="/home/jdk"
export GRADLE_HOME="/home/gradle"
export PATH="$PATH:$ANDROID_HOME/bin:$JAVA_HOME/bin:$GRADLE_HOME/bin"		
```
7. **Note**: if your host OS is **MacOS with Apple chip**, You **must** provide the 64-bit Intel/AMD libraries that the AAPT2 (Android Asset Packaging Tool) binary is expecting:
 ```
 dpkg --add-architecture amd64
 apt-get update
 apt install -y libc6:amd64 libstdc++6:amd64 zlib1g:amd64
 ```

Lastly, use VS Code with the Dev Containers plugin to connect to your container and work on the project.

## Build the app

To build the app:

1. Navigate to the project root folder (`/priority-ring-app`).
2. Enter the following command:
	```
	./gradle build
	```
3. Upon completion, the APK will be available at:
	```
	~/android-dev/priority-ring/app/build/outputs/apk/debug/app-debug.apk
	```

To install and use the app, follow the instruction provided [here](#installation).
