#!/bin/bash

JRE_DOWNLOAD_URL__WINDOWS="https://api.adoptium.net/v3/binary/latest/11/ga/windows/x64/jre/hotspot/normal/eclipse?project=jdk"
JRE_DOWNLOAD_URL__LINUX="https://api.adoptium.net/v3/binary/latest/11/ga/linux/x64/jre/hotspot/normal/eclipse?project=jdk"
JRE_DOWNLOAD_URL__MACOS="https://api.adoptium.net/v3/binary/latest/11/ga/mac/x64/jre/hotspot/normal/eclipse?project=jdk"
MAIN_CLASS="co.casterlabs.caffeinated.updater.Launcher"

# Compile everything
if [[ $@ == *"compile"* ]]; then
    mvn clean package
fi

# Reset the dist folder
rm -rf dist/*
mkdir -p dist
mkdir dist/artifacts

if [[ $@ == *"dist-windows"* ]]; then
    echo "Building for Windows..."
    mkdir dist/windows

    if [ ! -f windows_runtime.zip ]; then
        echo "Downloading JRE from ${JRE_DOWNLOAD_URL__WINDOWS}."
        wget -O windows_runtime.zip $JRE_DOWNLOAD_URL__WINDOWS
    fi

    java -jar "packr.jar" \
        --platform windows64 \
        --jdk windows_runtime.zip \
        --executable Casterlabs-Caffeinated-Updater \
        --classpath target/Casterlabs-Caffeinated-Updater.jar \
        --mainclass $MAIN_CLASS \
        --vmargs caffeinated.channel=stable \
        --output dist/windows

    echo "Finished building for Windows."

    cd dist/windows
    zip -r ../artifacts/Windows.zip *
    cd - # Return.
    echo ""
fi

if [[ $@ == *"dist-linux"* ]]; then
    echo "Building for Linux..."
    mkdir dist/linux

    if [ ! -f linux_runtime.tar.gz ]; then
        echo "Downloading JRE from ${JRE_DOWNLOAD_URL__LINUX}."
        wget -O linux_runtime.tar.gz $JRE_DOWNLOAD_URL__LINUX
    fi

    java -jar "packr.jar" \
        --platform linux64 \
        --jdk linux_runtime.tar.gz \
        --executable Casterlabs-Caffeinated-Updater \
        --classpath target/Casterlabs-Caffeinated-Updater.jar \
        --mainclass $MAIN_CLASS \
        --vmargs caffeinated.channel=stable \
        --output dist/linux

    echo "Finished building for Linux."

    cd dist/linux
    tar -czvf ../artifacts/Linux.tar.gz *
    cd - # Return.
    echo ""
fi

if [[ $@ == *"dist-macos"* ]]; then
    echo "Building for MacOS..."
    mkdir dist/macos
    mkdir dist/macos/Casterlabs-Caffeinated.app

    if [ ! -f macos_runtime.tar.gz ]; then
        echo "Downloading JRE from ${JRE_DOWNLOAD_URL__MACOS}."
        wget -O macos_runtime.tar.gz $JRE_DOWNLOAD_URL__MACOS
    fi

    java -jar "packr.jar" \
        --platform mac \
        --jdk macos_runtime.tar.gz \
        --executable Casterlabs-Caffeinated-Updater \
        --icon app_icon.icns \
        --bundle co.casterlabs.caffeinated \
        --classpath target/Casterlabs-Caffeinated-Updater.jar \
        --mainclass $MAIN_CLASS \
        --vmargs caffeinated.channel=stable \
        --output dist/macos/Casterlabs-Caffeinated.app

    echo "Finished building for MacOS."

    cd dist/macos
    tar -czvf ../artifacts/macOS.tar.gz *
    cd - # Return.
    echo ""
fi
