#!/usr/bin/env bash
# Full check for a batch: build (with unit tests), then the GameTests headless on both loaders.
# Usage: tools/check.sh            (from anywhere in the repository)
# Exits nonzero if any step fails; per-step logs go to build/check/<step>.log.
cd "$(dirname "$0")/.." || exit 2
# Loom needs JDK 21; the shell's own JAVA_HOME may point at an older one. Override with CB_JAVA_HOME.
export JAVA_HOME="${CB_JAVA_HOME:-/c/Program Files/Microsoft/jdk-21.0.11.10-hotspot}"

logs=build/check
mkdir -p "$logs"
failed=()

# step <name> <timeout seconds> <gradle args...>
step() {
    local name=$1 limit=$2
    shift 2
    echo "== $name"
    local start=$SECONDS
    if timeout "$limit" ./gradlew "$@" --console=plain > "$logs/$name.log" 2>&1; then
        echo "   ok ($((SECONDS - start))s)"
    else
        echo "   FAILED ($((SECONDS - start))s), see $logs/$name.log"
        grep -E "error:|FAILED|tests? failed|failed at|Exception:|BUILD FAILED" "$logs/$name.log" | head -15 | sed 's/^/   /'
        failed+=("$name")
    fi
}

step build 1200 build
# GameTest servers exit with the number of failed required tests; the timeout catches hangs
step gametest-fabric 600 :fabric:runGameTestServer
step gametest-neoforge 600 :neoforge:runGameTestServer

# Summaries from the test reports
for report in common/build/test-results/test/*.xml; do
    [ -f "$report" ] && grep -o '<testsuite name="[^"]*" tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' "$report" \
        | sed 's/<testsuite name="com.carpentersblocks./   unit /'
done
for loader in fabric neoforge; do
    grep -hoE "(All )?[0-9]+ required tests (passed|failed)" "$logs/gametest-$loader.log" 2>/dev/null \
        | sed "s/^/   gametest $loader: /"
done

if [ ${#failed[@]} -gt 0 ]; then
    echo "CHECK FAILED: ${failed[*]}"
    exit 1
fi
echo "CHECK PASSED"
