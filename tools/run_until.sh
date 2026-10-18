#!/usr/bin/env bash
# Usage: run_until.sh <gradle task> <log file> <success regex> <timeout seconds>
# Starts a Gradle run task, waits for a log line matching the regex (or the timeout), then stops it.
task=$1; log=$2; pattern=$3; limit=$4
./gradlew "$task" --console=plain > "$log" 2>&1 &
pid=$!
for ((i = 0; i < limit; i += 2)); do
    if grep -Eq "$pattern" "$log"; then echo "MATCHED after ${i}s"; break; fi
    if ! kill -0 $pid 2>/dev/null; then echo "EXITED"; break; fi
    sleep 2
done
# Gradle forks the game JVM; stop the whole tree
taskkill //F //T //PID $(ps -p $pid -o winpid= 2>/dev/null | tr -d ' ') > /dev/null 2>&1 || kill $pid 2>/dev/null
wait $pid 2>/dev/null
