# Overview

Android currently does not allow for some contacts and/or applications to bypass its **vibrate** mode and produce audible alerts for calls and notifications — a feature I refer to as **"priority ring"**. The closest alternative is a feature called _Modes_ (Settings > Modes), such as the Do Not Disturb (DND) mode. Unlike priority ring, using modes **silences** the device and **restricts** calls and notifications to approved sources only. For users who prefer to keep their phones on vibrate to remain aware of calls and notifications without disturbing others, Modes falls short.

Although priority ring is not a native Android feature, users can "implement" it without using any 3rd-party apps or special tools:
1. Set the phone ringer to normal (loud).
2. Set the default ringtone and notification sound to `none`.
3. Ensure the phone is set to vibrate on calls and notifications.
4. Set custom ringtones and notification sounds for each priority contact and app.

To simplify this process, this app automates steps 1-3 by adding a quick settings tile. The tile allows users to toggle priority ring ON/OFF. Despite that, to achieve true priority ring, users are still required to implement step 4 manually (See [Installation and setup](#installation-and-setup)).

> [!NOTE]
> This app does not automate the entire process outlined above due to Android's strict security policies. For the app to fully implement the "priority ring" feature, it will require the user to grant sensitive permissions and will require complicated solution.

# Installation and setup

To enable priority ring, the user must install the app **and** setup each priority contact and app individually:

## 1. Installation

1. Download or [build](#build-the-app) the app.
2. Install the app using any Android file manager.
3. Add the Priority Ring tile to you quick settings tiles. Make sure to stretch the tile horizontally to allow for extra information to be visible.
4. Long-press the tile to go to its app info page.
5. Under the **Advanced** section, enable the **Modify system settings** option.
6. Done.

## 2. Setup

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

Please note:
- Before using the app, make sure to follow the steps outlines in the [Installation and setup](#installation-and-setup)section.
- Turning priority ring ON while the phone is on vibrate will switch the phone to loud (as well as set the ringtone and notification sound to `none`).
- Priority ring is unavailable while the phone is on Silent mode. This is due to Android's strict permission policies.

# Developer notes

- The app was developed without using Android Studio.
- Only the bare minimum dependencies and tooling was used.
- The app consists of a single service class (TileService) that handles the app logic and UI (the tile).

## Development environment setup

> [!IMPORTANT]
> The steps below assume the use of Podman containers.

1. On your local machine, create a dedicated directory for Android development (e.g., `~/android-dev`).
2. Clone this repository into it:
	```
	cd ~/android-dev && git clone https://github.com/drudnitsky/priority-ring-app
	```
3. Download the following dependencies for **Linux OS** (binary only, if available):
	1. [OpenJDK v21.0.2](https://jdk.java.net/archive/)
	2. [Android command line tools - latest version](https://developer.android.com/studio#command-line-tools-only)

	> [!TIP]
	> The dependencies above are general dependencies for Android app development.
	> App-specific dependencies, such as various Google Android libraries, will be imported automatically during the application build process.

4. Unpack the downloaded dependencies to mirror the following directory structure:
	```
	android-dev/
		├── priority-ring-app/
		├── jdk-21.0.2/
		└── android-sdk/
			└── cmdline-tools/
				└── latest/
					├── bin/
					├── lib/
					├── source.properties
					└── notice.txt
	```
	> [!IMPORTANT]
	> Notice the addition of the `android-sdk` directory and the `latest` subdirectory under the `cmdline-tools` directory. This structure is required for the build process to successfully resolve dependencies.

5. Create a new Linux Debian container. Make sure to mount your local `~/android-dev` folder:
	```
	podman container create -t --name "android-dev" -v ~/android-dev:/home debian:latest
	```
	> [!IMPORTANT]
	> if your host OS is **MacOS with Apple chip**, You **must** provide the 64-bit Intel/AMD libraries that the AAPT2 (Android Asset Packaging Tool) binary is expecting:
	> ```
	> dpkg --add-architecture amd64
	> apt-get update
	> apt install -y libc6:amd64 libstdc++6:amd64 zlib1g:amd64
	> ```

6. Start the container.
7. Add the following to `/.bashrc` or `/etc/bash.bashrc`:
	```
	export ANDROID_HOME="/home/android-sdk"
	export JAVA_HOME="/home/jdk-21.0.2"
	export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$JAVA_HOME/bin"		
	```

Lastly, I recommend using VS Code with the Dev Containers plugin to connect to your container and work on the project. In addition, you should consider importing additional Android command line tools for development (like `adb`):
```
sdkmanager "platform-tools" "build-tools;36.0.0" "platforms;android-36"
```

## Build the app

In order to build the app, Android requires developers to accepts its SDK end user agreement. You will only need to do this once before the first time you build the app. 

```
yes | sdkmanager --licenses
```

To build the app:

1. Navigate to the project root folder (`/priority-ring-app`).
2. Enter the following command:
	```
	./gradlew build
	```
3. Upon completion, the APK will be available at:
	```
	~/android-dev/priority-ring/app/build/outputs/apk/debug/app-debug.apk
	```

To install and use the app on an Android device, see the [installation](#installation) and [usage](#usage) sections. 
