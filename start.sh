#!/bin/bash
dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
cd "$dir"
exec ./gradlew desktop:run
