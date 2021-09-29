#!/bin/bash
sdk=/d/Programme/AndroidSDK/platforms/android-30
echo "moving hack api into sdk folder and replacing the original"
cp ./app/libs/android-30fixed.jar $sdk #copy hack to sdk location
mv $sdk/android.jar $sdk/android.jar.old #rename originalapi to unused
mv $sdk/android-30fixed.jar $sdk/android.jar #rename hackapi to used
echo "done replacing original!"
echo "execute gradle build and install"
#check size to prove if you want original is ~20mb hack is ~28mb