# Releasing

 1. Change the version in [`gradle.properties`](gradle.properties) to a non-SNAPSHOT version.
 2. Update [the `CHANGELOG.md`](CHANGELOG.md) for the impending release.
 3. Update [the `README.md`](README.md) with the new version.
 4. `git commit -am "Prepare for release X.Y.Z"` (where `X.Y.Z` is the new version).
 5. `git tag -a X.Y.Z -m "Version X.Y.Z"` (where `X.Y.Z` is the new version).
 6. Update the [`gradle.properties`](gradle.properties) to the next SNAPSHOT version.
 7. `git commit -am "Prepare next development version"`
 8. `git push && git push --tags`
 9. Wait for [the CI server](https://app.bitrise.io/build/58bb63258d8118db) to upload the artifact.
 10. Visit [Sonatype Nexus](https://oss.sonatype.org) and promote the artifact.
