#!/bin/bash
export LANG=C

dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
cd "$dir/.."

if test ! -e FLAVOR; then
  echo "Fatal Error: Make sure you have selected a flavor (run tools/flavor <flavor>)."
  exit 1
fi

DESTFOLDER="android/assets"
revision=$(git rev-parse --short HEAD)
cat > "$DESTFOLDER/VERSION" << EOF
buildDate: $(date)
revision: $revision
version: $2
napplicationName: $3
EOF
export TZ=GMT
mydate=$(date "+%y-%m-%d %H:%M")
echo "$3-$2-$revision $mydate" > "$DESTFOLDER/VERSIONSTRING"
exit 0
