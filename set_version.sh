#!/bin/bash
export LANG=C
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
