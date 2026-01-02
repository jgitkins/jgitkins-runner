#!/bin/sh
set -eu

BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROJECT_DIR=$(CDPATH= cd -- "$BASE_DIR/.." && pwd)

JAVA_BIN=${JAVA_BIN:-java}
RUNNER_JAR=${RUNNER_JAR:-"$PROJECT_DIR/build/libs/jgitkins-runner-0.0.1.jar"}
RUNNER_CONFIG=${RUNNER_CONFIG:-"$PROJECT_DIR/application.yml"}
RUNNER_PID=${RUNNER_PID:-"$PROJECT_DIR/.runner/runner.pid"}
RUNNER_LOG=${RUNNER_LOG:-"$PROJECT_DIR/.runner/runner.log"}

mkdir -p "$(dirname "$RUNNER_PID")"

is_running() {
  if [ ! -f "$RUNNER_PID" ]; then
    return 1
  fi
  pid=$(cat "$RUNNER_PID" 2>/dev/null || true)
  if [ -z "$pid" ]; then
    return 1
  fi
  if kill -0 "$pid" 2>/dev/null; then
    return 0
  fi
  return 1
}

start() {
  if is_running; then
    echo "runner is already running (pid=$(cat "$RUNNER_PID"))."
    return 0
  fi
  if [ ! -f "$RUNNER_JAR" ]; then
    echo "runner jar not found: $RUNNER_JAR"
    return 1
  fi
  nohup "$JAVA_BIN" -jar "$RUNNER_JAR" start --spring.config.location="$RUNNER_CONFIG" \
    >>"$RUNNER_LOG" 2>&1 &
  echo $! > "$RUNNER_PID"
  echo "runner started (pid=$!)."
}

stop() {
  if ! is_running; then
    echo "runner is not running."
    rm -f "$RUNNER_PID"
    return 0
  fi
  pid=$(cat "$RUNNER_PID")
  kill "$pid" 2>/dev/null || true
  for _ in $(seq 1 20); do
    if ! kill -0 "$pid" 2>/dev/null; then
      rm -f "$RUNNER_PID"
      echo "runner stopped."
      return 0
    fi
    sleep 0.2
  done
  kill -9 "$pid" 2>/dev/null || true
  rm -f "$RUNNER_PID"
  echo "runner stopped (forced)."
}

status() {
  if is_running; then
    echo "runner is running (pid=$(cat "$RUNNER_PID"))."
    return 0
  fi
  echo "runner is not running."
  return 1
}

restart() {
  stop
  start
}

activate() {
  if [ ! -f "$RUNNER_JAR" ]; then
    echo "runner jar not found: $RUNNER_JAR"
    return 1
  fi
  "$JAVA_BIN" -jar "$RUNNER_JAR" activate "$@"
}

case "${1:-}" in
  start) shift; start "$@" ;;
  stop) shift; stop "$@" ;;
  status) shift; status "$@" ;;
  restart) shift; restart "$@" ;;
  activate) shift; activate "$@" ;;
  *)
    echo "Usage: $0 {start|stop|status|restart|activate}"
    echo "Env: JAVA_BIN RUNNER_JAR RUNNER_CONFIG RUNNER_PID RUNNER_LOG"
    exit 2
    ;;
esac
