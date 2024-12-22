#!/bin/bash

set -e -o pipefail

APP_ID="co.casterlabs.caffeinated"
APP_NAME="Casterlabs-Caffeinated-Updater"
MAIN_CLASS="co.casterlabs.caffeinated.updater.Launcher"

if [[ $@ == *"compile"* ]]; then
    echo "------------ Compiling app ------------"
    mvn clean package
    echo "------------ Finishing compiling app ---------"
fi

if [[ $@ == *"dist-windows"* ]]; then
    echo "------------ Bundling for Windows ------------"

    java -jar bundler.jar bundle \
        --arch x86_64 --os windows \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --sign "cmd.exe /C C:\signing\sign.bat $APP_NAME.exe" \
        --java 11 --arg=-Dcaffeinated.channel=stable --dependency target/Casterlabs-Caffeinated-Updater.jar --main $MAIN_CLASS

    echo "------------ Finished bundling for Windows ------------"
fi

if [[ $@ == *"dist-macos"* ]]; then
    echo "------------ Bundling for macOS ------------"

    java -jar bundler.jar bundle \
        --arch aarch64 --os macos \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --java 11 --arg=-Dcaffeinated.channel=stable --dependency target/Casterlabs-Caffeinated-Updater.jar --main $MAIN_CLASS

    java -jar bundler.jar bundle \
        --arch x86_64 --os macos \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --java 11 --arg=-Dcaffeinated.channel=stable --dependency target/Casterlabs-Caffeinated-Updater.jar --main $MAIN_CLASS

    echo "------------ Finished bundling for macOS ------------"
fi

if [[ $@ == *"dist-linux"* ]]; then
    echo "------------ Bundling for Linux ------------"

    java -jar bundler.jar bundle \
        --arch aarch64 --os gnulinux \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --java 11 --arg=-Dcaffeinated.channel=stable --dependency target/Casterlabs-Caffeinated-Updater.jar --main $MAIN_CLASS

    java -jar bundler.jar bundle \
        --arch arm --os gnulinux \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --java 11 --arg=-Dcaffeinated.channel=stable --dependency target/Casterlabs-Caffeinated-Updater.jar --main $MAIN_CLASS

    java -jar bundler.jar bundle \
        --arch x86_64 --os gnulinux \
        --id $APP_ID --name $APP_NAME --icon icon.png \
        --java 11 --arg=-Dcaffeinated.channel=stable --dependency target/Casterlabs-Caffeinated-Updater.jar --main $MAIN_CLASS

    echo "------------ Finished bundling for Linux ------------"
fi

