#!/bin/bash
set -e

if [[ $(git rev-parse --abbrev-ref HEAD) != trunk ]]; then
  echo "Release version can be prepared only from the trunk branch."
  exit 1
fi

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Working tree is not clean. Aborting to avoid data loss."
  exit 1
fi

currentVersion=$(git describe --abbrev=0)
currentMajor=$(echo "$currentVersion" | cut -d. -f1)
currentMinor=$(echo "$currentVersion" | cut -d. -f2)
currentPatch=$(echo "$currentVersion" | cut -d. -f3)

echo "Laboratory $currentVersion:"

PS3=$"What do you want to do? "
options=("Bump major" "Bump minor" "Bump patch" "Quit")
select option in "${options[@]}"; do
  case $option in
  "Bump major")
    newVersion="$((currentMajor + 1)).0.0"
    break
    ;;
  "Bump minor")
    newVersion="$currentMajor.$((currentMinor + 1)).0"
    break
    ;;
  "Bump patch")
    newVersion="$currentMajor.$currentMinor.$((currentPatch + 1))"
    break
    ;;
  "Quit")
    exit 0
    ;;
  *)
    echo "Invalid option '$REPLY'."
    exit 0
    ;;
  esac
done

function confirmBump {
  read -rp "Bump version from $currentVersion to $newVersion? [Y/n] " choice
  case "$choice" in
  [nN] | [nN][oO]) echo 1 ;;
  *) echo 0 ;;
  esac
}

if [[ $(confirmBump) != 0 ]]; then
  echo "No"
  exit 0
else
  echo "Yes"
fi

changelogFile="./docs/changelog.md"
propertiesFile="./gradle.properties"
indexFile="./docs/index.md"
readmeFile="./README.md"

# Generate dependencies diff for new version and insert it.
sh ./scripts/deps_dump.sh "${newVersion}"
depsDiff=$(sh ./scripts/deps_diff.sh "${currentVersion}" "${newVersion}")
if [ -n "$depsDiff" ]; then
  tmpDir="$(mktemp -d)"
  trap 'rm -rf "$tmpDir"' EXIT
  tmpDiff="$tmpDir/diff.txt"
  tmpOut="$tmpDir/out.txt"

  echo "$depsDiff" >"$tmpDiff"
  awk -v version="$currentVersion" -v diff="$tmpDiff" '
  BEGIN {
    while ((getline line < diff) > 0) buf = buf line "\n"
    close(diff)
  }

  $0 ~ "^## \\[" version "\\]" {
    printf "%s\n", buf
  }
  { print }
  ' "$changelogFile" >"$tmpOut" && mv "$tmpOut" "$changelogFile"
fi

# Replace current version in changelog.md and update hyperlinks.
today=$(date +%F)
sed -i "" "s/## \[Unreleased\]/## \[Unreleased\]"$'\\\n\\\n'"## \[$newVersion\] - $today/g" $changelogFile
newVersionTag="[$newVersion]: https:\/\/github.com\/MiSikora\/laboratory\/releases\/tag\/$newVersion"
sed -i "" "s/$currentVersion...HEAD/$newVersion...HEAD"$'\\\n'"$newVersionTag""/g" $changelogFile

# Replace current version in gradle.properties.
versionNameKey="VERSION_NAME"
sed -i "" "s/.*$versionNameKey.*/$versionNameKey=$newVersion/g" $propertiesFile

# Replace current version in index.md.
sed -i "" "s/$currentVersion/$newVersion/g" $indexFile

# Replace current version in README.md.
sed -i "" "s/$currentVersion/$newVersion/g" $readmeFile

git add . &>/dev/null
git commit -am "Prepare for release $newVersion" &>/dev/null
git tag -a "$newVersion" -m "Version $newVersion" &>/dev/null

# Update current version to a snapshot one.
newMajor="$(cut -d"." -f1 <<<"$newVersion")"
newMinor="$(cut -d"." -f2 <<<"$newVersion")"
newPatch="$(cut -d"." -f3 <<<"$newVersion")"
newSnapshotVersion="$newMajor.$newMinor.$((newPatch + 1))-SNAPSHOT"
sed -i "" "s/.*$versionNameKey.*/$versionNameKey=$newSnapshotVersion/g" $propertiesFile

git add . &>/dev/null
git commit -am "Prepare next development version" &>/dev/null

echo "\
Version bumped successfully to $newVersion and commit is tagged. \
Changes can be pushed to the origin."
