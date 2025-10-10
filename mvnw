#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper Bootstrap Script
# ----------------------------------------------------------------------------
set -eu

MVNW_ROOT="$(cd "$(dirname "$0")"; pwd -P)"
WRAPPER_JAR="$MVNW_ROOT/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$MVNW_ROOT/.mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$WRAPPER_PROPERTIES" ]; then
  echo "Cannot find $WRAPPER_PROPERTIES" >&2
  exit 1
fi

load_property() {
  prop_name="$1"
  grep "^$prop_name=" "$WRAPPER_PROPERTIES" | tail -n1 | cut -d'=' -f2-
}

DOWNLOAD_URL="$(load_property wrapperUrl)"
if [ ! -f "$WRAPPER_JAR" ]; then
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$DOWNLOAD_URL" -o "$WRAPPER_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$DOWNLOAD_URL" -O "$WRAPPER_JAR"
  else
    echo "Error: curl or wget is required to download the Maven Wrapper" >&2
    exit 1
  fi
fi

JAVA_OPTS=${JAVA_OPTS:-}
MAVEN_OPTS=${MAVEN_OPTS:-}

exec "${JAVA_HOME:+$JAVA_HOME/bin/}java" \
  $JAVA_OPTS \
  -Dmaven.multiModuleProjectDirectory="$MVNW_ROOT" \
  -cp "$WRAPPER_JAR" \
  org.apache.maven.wrapper.MavenWrapperMain \
  $MAVEN_OPTS \
  "$@"
