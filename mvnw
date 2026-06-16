#!/bin/sh
# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup script, version 3.3.2
# ----------------------------------------------------------------------------

# Required ENV vars:
#   JAVA_HOME - location of a JDK home dir

if [ -z "$MAVEN_SKIP_RC" ] ; then
  if [ -f /usr/local/etc/mavenrc ] ; then
    . /usr/local/etc/mavenrc
  fi
  if [ -f /etc/mavenrc ] ; then
    . /etc/mavenrc
  fi
  if [ -f "$HOME/.mavenrc" ] ; then
    . "$HOME/.mavenrc"
  fi
fi

cygwin=false
mingw=false
case "$(uname)" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true ;;
esac

if [ -z "$JAVA_HOME" ] ; then
  if [ -r /etc/gentoo-release ] ; then
    JAVA_HOME=$(java-config --jre-home)
  fi
fi

if $cygwin || $mingw ; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=$(cygpath --unix "$JAVA_HOME")
  [ -n "$CLASSPATH" ] && CLASSPATH=$(cygpath --path --unix "$CLASSPATH")
fi

if $mingw ; then
  [ -n "$MAVEN_PROJECTBASEDIR" ] \
    && MAVEN_PROJECTBASEDIR="$(cd "$(dirname "$MAVEN_PROJECTBASEDIR")" 2>/dev/null || true; pwd)"
fi

if [ -z "$MAVEN_PROJECTBASEDIR" ] ; then
  PRG="$0"
  while [ -h "$PRG" ] ; do
    ls=$(ls -ld "$PRG")
    link=$(expr "$ls" : '.*-> \(.*\)$')
    if expr "$link" : '/.*' > /dev/null; then
      PRG="$link"
    else
      PRG="$(dirname "$PRG")/$link"
    fi
  done
  SAVED="$(pwd)"
  cd "$(dirname "$PRG")/" >/dev/null
  MAVEN_PROJECTBASEDIR="$(pwd)"
  cd "$SAVED" >/dev/null
fi

if [ -r "$MAVEN_PROJECTBASEDIR/.mvn/jvm.config" ] ; then
  JVM_CONFIG_MAVEN_PROPS=$(tr '\n' ' ' < "$MAVEN_PROJECTBASEDIR/.mvn/jvm.config")
fi

WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

exec "$JAVA_HOME/bin/java" \
  $JVM_CONFIG_MAVEN_PROPS \
  $MAVEN_OPTS \
  $MAVEN_DEBUG_OPTS \
  -classpath "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar" \
  "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
  ${WRAPPER_LAUNCHER} "$@"
