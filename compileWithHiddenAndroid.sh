#!/bin/bash
sdk=/d/Programme/AndroidSDK/platforms/android-30
date=$(date '+%Y-%m-%d')
echo "moving hack api into sdk folder and replacing the original"
cp ./app/libs/android-30fixed.jar $sdk #copy hack to sdk location
mv $sdk/android.jar $sdk/android.jar.old #rename originalapi to unused
mv $sdk/android-30fixed.jar $sdk/android.jar #rename hackapi to used
echo "done replacing original!"
read -p "execute gradle build and install" </dev/tty
./gradlew installDebug #run gradle build and install
gh release create hiddenApi-v$date
read -p "uploading generated apk" </dev/tty
gh release upload hiddenApi-v$date ./app/build/outputs/apk/debug/app-debug.apk
read -p "removing hack api from sdk folder and placing the original" </dev/tty
mv $sdk/android.jar $sdk/android.jar.hack #rename used hackapi to unused
mv $sdk/android.jar.old $sdk/android.jar #rename unused originalapi to used
rm $sdk/android.jar.hack
echo "done replacing hack!" #check size to prove if you want original is ~20mb hack is ~28mb
read -p "Press Enter to continue" </dev/tty