#!/bin/bash

set -e

export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"

echo "Using JAVA_HOME:"
echo "$JAVA_HOME"

echo "Java version:"
java -version

echo "jpackage version:"
jpackage --version

APP_NAME="Junqi"
MAIN_CLASS="ui.Launcher"
JAR_NAME="Junqi.jar"

PROJECT_OUT="out/production/Junqi-New"
BUILD_DIR="build"
INPUT_DIR="$BUILD_DIR/package-input"
OUTPUT_DIR="$BUILD_DIR/jpackage-output"

FX_SDK_LIB="$HOME/DevTools/javafx-sdk-21.0.12/lib"
FX_JMODS="$HOME/DevTools/javafx-jmods-21.0.12"

echo "Checking JavaFX SDK path..."
if [ ! -f "$FX_SDK_LIB/javafx.controls.jar" ]; then
    echo "ERROR: Cannot find javafx.controls.jar in:"
    echo "$FX_SDK_LIB"
    exit 1
fi

echo "Checking JavaFX JMODS path..."
if [ ! -f "$FX_JMODS/javafx.controls.jmod" ]; then
    echo "ERROR: Cannot find javafx.controls.jmod in:"
    echo "$FX_JMODS"
    exit 1
fi

echo "Cleaning old build..."
rm -rf "$BUILD_DIR"
mkdir -p "$INPUT_DIR"
mkdir -p "$OUTPUT_DIR"

echo "Creating jar..."
jar --create \
    --file "$INPUT_DIR/$JAR_NAME" \
    --main-class "$MAIN_CLASS" \
    -C "$PROJECT_OUT" .

echo "Skipping jar test. Test the generated app image manually."

echo "Creating app image with jpackage..."
jpackage \
    --type app-image \
    --name "$APP_NAME" \
    --input "$INPUT_DIR" \
    --main-jar "$JAR_NAME" \
    --main-class "$MAIN_CLASS" \
    --dest "$OUTPUT_DIR" \
    --module-path "$FX_JMODS" \
    --add-modules javafx.controls

echo "Done."
echo "App image created at:"
echo "$OUTPUT_DIR/$APP_NAME.app"