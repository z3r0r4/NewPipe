#!/bin/bash
sdk=/d/Programme/AndroidSDK/platforms/android-30
date=$(date '+%Y-%m-%d')
echo "moving hack api into sdk folder and replacing the original"
cp ./app/libs/android-30fixed.jar $sdk #copy hack to sdk location
mv $sdk/android.jar $sdk/android.jar.old #rename originalapi to unused
mv $sdk/android-30fixed.jar $sdk/android.jar #rename hackapi to used
echo "done replacing original!"
echo

#add code that fetches and asks for merge

echo "checking hack branch"
git checkout volume-btn-long-press-skip
echo

read -p "fetch upstream ( /n)" -n 1 -r
if [[ $REPLY =~ ^[Nn]$ ]]
then
	echo "skipping"
else
	git fetch upstream
	echo "done"
fi

read -p "MERGE WITH UPSTREAM ( /n)" -n 1 -r
if [[ $REPLY =~ ^[Nn]$ ]]
then
	echo "skipping"
else
	git merge upstream/dev
	echo "merged with upstream"
fi

echo
echo "Status"
git status
echo

read -p "build gradle and install ( /n)" -n 1 -r
echo    
if [[ $REPLY =~ ^[Nn]$ ]]
then
	echo "skipping"
else
	./gradlew installDebug #run gradle build and install
fi


read -p "create release ( /n)" -n 1 -r
echo    
if [[ $REPLY =~ ^[Nn]$ ]]
then
	echo "skipping"
else
	gh release create hiddenApi-v$date
fi
#gh release delete hiddenApi-vTest


read -p "upload generated apk ( /n)" -n 1 -r
echo    
if [[ $REPLY =~ ^[Nn]$ ]]
then
	echo "skipping"
else
	gh release upload hiddenApi-v$date ./app/build/outputs/apk/debug/app-debug.apk
fi


read -p "removing hack api from sdk folder and placing the original (Enter)" </dev/tty
mv $sdk/android.jar $sdk/android.jar.hack 		
#rename used hackapi to unused
mv $sdk/android.jar.old $sdk/android.jar 		
#rename unused originalapi to used
rm $sdk/android.jar.hack
echo "done replacing hack!" #check size to prove if you want original is ~20mb hack is ~28mb
read -p "Press Enter to close" </dev/tty