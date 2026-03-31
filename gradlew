#!/bin/sh

# Gradle wrapper startup script for POSIX (Linux/macOS/CI).
# This script locates the gradle-wrapper.jar and invokes GradleWrapperMain
# which downloads the actual Gradle distribution on first run.

# Resolve the directory this script lives in (follow symlinks)
PRG="$0"
while [ -h "$PRG" ]; do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=$(dirname "$PRG")/"$link"
  fi
done
APP_HOME=$(dirname "$PRG")
APP_HOME=$(cd "$APP_HOME" && pwd)

# Path to the Gradle wrapper bootstrap JAR
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Locate java executable
if [ -n "$JAVA_HOME" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD="java"
fi

# Verify java is available
if ! command -v "$JAVACMD" > /dev/null 2>&1; then
  echo "ERROR: JAVA_HOME is not set and 'java' was not found in PATH." >&2
  exit 1
fi

# Verify the wrapper JAR exists
if [ ! -f "$CLASSPATH" ]; then
  echo "ERROR: gradle/wrapper/gradle-wrapper.jar not found at: $CLASSPATH" >&2
  exit 1
fi

# Build the java arguments carefully.
# Use ${VAR:+"$VAR"} so that empty JAVA_OPTS / GRADLE_OPTS produce NO argument
# (passing an empty string as an argument would confuse Java's class-name parsing).
exec "$JAVACMD" \
  -Xmx64m -Xms64m \
  ${JAVA_OPTS:+"$JAVA_OPTS"} \
  ${GRADLE_OPTS:+"$GRADLE_OPTS"} \
  "-Dorg.gradle.appname=$(basename "$PRG")" \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
