# Synonyms

![Continuous Integration](https://github.com/dariusj/synonyms/workflows/Continuous%20Integration/badge.svg)

An application with multiple entrypoints to perform synonym-related tasks.

This project is a personal playground in Scala to try out different libraries and techniques.

## Requirements

* [Java 17](https://adoptium.net/en-GB/temurin/releases/)
* [sbt](https://www.scala-sbt.org/)
* [Optional] [Bloop](https://scalacenter.github.io/bloop/) (examples use bloop)

## Entrypoints

### HTTP Server

The server can be started by executing the following command:

```bash
bloop run http
```

The following endpoints are supported:

* Check if two words are synonyms\
  `GET /synonyms?words=foo&words=bar[&thesaurus=datamuse]`
* List synonyms for a given word\
  `GET /synonyms/foo[?thesaurus=datamuse]`

### Command line

Command line invocation supports the following commands:

* Check if two words are synonyms\
  `bloop run cli --args check [--args '--format=json'] --args house --args pad`
* List synonyms for a given word\
  `bloop run cli --args list [--args '--format=json'] [--args '--source|-s datamuse'] --args bar`

## Thesauruses

The app currently supports the following thesauruses:

* [Cambridge](https://dictionary.cambridge.org/thesaurus/) (`cambridge`)
* [Datamuse](https://www.datamuse.com/api/) (`datamuse`)
* [Merriam-Webster](https://www.merriam-webster.com/thesaurus) (`merriamwebster`)
* [PowerThesaurus](https://powerthesaurus.org) (`powerthesaurus`)
* [WordHippo](https://www.wordhippo.com) (`wordhippo`)

Default is all thesauruses.

## Run tests

`bloop test core cli http`

## Build a Docker image for the http server

`sbt http/Docker/publishLocal`
