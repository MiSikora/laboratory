#!/bin/bash
set -e

if [[ $(git rev-parse --abbrev-ref HEAD) != master ]]; then
  echo Release version can be prepared automatically only from the master branch.
  exit 0
fi

currentVersion=$(git describe --abbrev=0)
currentMajor=$(echo $currentVersion | cut -d. -f1)
currentMinor=$(echo $currentVersion | cut -d. -f2)
currentPatch=$(echo $currentVersion | cut -d. -f3)

echo "Laboratory $currentVersion:"

PS3=$"What do you want to do? "
options=("Bump major" "Bump minor" "Bump patch" "Quit")
select option in "${options[@]}"
do
  case $option in
    "Bump major" )
      newVersion="$((currentMajor + 1)).0.0"
      break ;;
    "Bump minor" )
      newVersion="$currentMajor.$((currentMinor + 1)).0"
      break ;;
    "Bump patch" )
      newVersion="$currentMajor.$currentMinor.$((currentPatch + 1))"
      break ;;
    "Quit" )
      exit 0 ;;
    * )
      echo "Invalid option '$REPLY'."
      exit 0 ;;
  esac
done

function confirmBump {
  read -rp "Bump version from $currentVersion to $newVersion? [Y/n] " choice
  case "$choice" in
    [nN][oO]|[n|N] ) echo 1 ;;
    * ) echo 0 ;;
  esac
}

if [[ $(confirmBump) != 0 ]] ; then
  echo "No"
  exit 0
else
  echo "Yes"
fi

# Replace current version in ./library/gradle.properties
propertiesFile="./gradle.properties"
versionNameKey="VERSION_NAME"
sed -i "" "s/.*$versionNameKey.*/$versionNameKey=$newVersion/g" $propertiesFile

# Replace current version in CHANGELOG.md and update hyperlinks
changelogFile="./CHANGELOG.md"
sed -i "" "s/## \[Unreleased\]/## \[Unreleased\]"$'\\\n\\\n'"## \[$newVersion\]/g" $changelogFile
newVersionTag="[$newVersion]: https:\/\/github.com\/MiSikora\/Laboratory\/releases\/tag\/$newVersion"
sed -i "" "s/$currentVersion...HEAD/$newVersion...HEAD"$'\\\n'"$newVersionTag""/g" $changelogFile

# Replace current version in README.md
readmeFile="./README.md"
sed -i "" "s/$currentVersion/$newVersion/g" $readmeFile

git reset &> /dev/null
git commit -am "Prepare for release $newVersion" &> /dev/null
git tag -a $newVersion -m "Version $newVersion" &> /dev/null

# Update current version to a snapshot one
newMajor="$(cut -d"." -f1 <<<"$newVersion")"
newMinor="$(cut -d"." -f2 <<<"$newVersion")"
newPatch="$(cut -d"." -f3 <<<"$newVersion")"
newSnapshotVersion="$newMajor.$newMinor.$((newPatch + 1))-SNAPSHOT"
sed -i "" "s/.*$versionNameKey.*/$versionNameKey=$newSnapshotVersion/g" $propertiesFile

git commit -am "Prepare next development version" &> /dev/null

echo "\
Version bumped successfully to $newVersion and commit is tagged. \
Changes can be pushed to the origin."
