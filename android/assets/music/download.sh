#!/bin/bash
# download the music from soundimage.org (all credit to Eric Matyas)
# wget is needed

# Display non-redistribution clause and credits for Eric
cat << EOF
This is the McMinos music downloader.
It will download the fixed and random music into the current folder.

Most of the music is from Eric Matyas at soundimage.org.
You are not allowed to redistribute his music without consent.
You can though use it for your own games when attributing
Eric, soundimage.org, and his music as described here on http://soundimage.org

If you build McMinos, you must make sure that the attribution to soundimage.org
is present in the credit text (linked from the flavor folder) to
android/assets/text in all languages.

Do you accept these terms? (type all capital YES to accept).
EOF

read answer
if test "$answer" != "YES"; then
  echo "You declined the terms, not downloading."
  exit 1
fi

mkdir -p fixed
mkdir -p random
cd fixed
wget -c "http://soundimage.org/wp-content/uploads/2015/03/Chamber-of-Jewels.mp3"
# legacy mcminos themesong is already available as binary checkin here
wget -c "http://soundimage.org/wp-content/uploads/2015/06/The-Princess-Haunted.mp3"
cd ../random
wget -c "http://soundimage.org/wp-content/uploads/2014/08/Cyber-Streets.mp3"
wget -c "http://soundimage.org/wp-content/uploads/2015/07/The-Toy-Factory.mp3"
wget -c "http://soundimage.org/wp-content/uploads/2014/12/The-Triumph-of-Technology_v001.mp3"
wget -c "http://soundimage.org/wp-content/uploads/2015/05/Urban-Sci-Fi-Heroes.mp3"
