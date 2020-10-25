# Releasing

 1. Run [prepare release script](https://github.com/MiSikora/Laboratory/library/prepare-release.sh) and bump desirable version part.
 2. If there are no errors `git push && git push --tags`.
 3. Wait for [the CI server](https://github.com/MiSikora/Laboratory/actions) to upload the artifact.
 4. Visit [Sonatype Nexus](https://oss.sonatype.org) and promote the artifact.
