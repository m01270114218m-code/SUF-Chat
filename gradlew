#!/bin/sh
set -e
if [ -x "$JAVA_HOME/bin/java" ]; then JAVA="$JAVA_HOME/bin/java"; else JAVA=java; fi
exec "$JAVA" -version >/dev/null
exec gradle "$@"
