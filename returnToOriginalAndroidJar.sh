#!/bin/bash
sdk=/d/Programme/AndroidSDK/platforms/android-30
echo "removing hack api from sdk folder and placing the original"
mv $sdk/android.jar $sdk/android.jar.hack #rename used hackapi to unused
mv $sdk/android.jar.old $sdk/android.jar #rename unused originalapi to used
rm $sdk/android.jar.hack
echo "done replacing hack!" #check size to prove if you want original is ~20mb hack is ~28mb