# Releasing

## Versioning

1. Run the [prepare release script](https://github.com/MiSikora/laboratory/blob/master/library/prepare-release.sh) and bump the desirable version part.
2. If there are no errors `git push && git push --tags`.
3. Wait for [the CI server](https://github.com/MiSikora/laboratory/actions) to upload the artifacts.
4. Visit [Sonatype Nexus](https://oss.sonatype.org) and promote the artifacts.

## Documentation updates

Website documentation lives under [`/library/docs`](https://github.com/MiSikora/laboratory/tree/master/library/docs) directory and is deployed with [MkDocs](https://www.mkdocs.org/) using [Material Theme](https://squidfunk.github.io/mkdocs-material/). A new site is built and published for the latest commits on the `master` branch.

If you want to test the website locally before pushing changes, you need to follow these steps.

Make sure you have Python 3 and pip installed.

```sh
$ python --version
Python 3.8.5

$ pip --version
pip 20.2.4
```

Install MkDocs and Material Theme.

```sh
$ pip install mkdocs mkdocs-material
$ mkdocs --version
mkdocs, version 1.1.2
```

Navigate to the library directory and run the site locally.

```sh
$ ./library mkdocs serve
INFO    -  Building documentation...
INFO    -  Cleaning site directory
INFO    -  Documentation built in 0.73 seconds
[I 201026 22:51:56 server:335] Serving on http://127.0.0.1:8000
INFO    -  Serving on http://127.0.0.1:8000
```
