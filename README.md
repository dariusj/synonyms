# Synonyms

An application with multiple entrypoints to perform synonym-related tasks.

This project is a personal playground in Scala to try out different libraries and techniques.

## Requirements

* [Bloop](https://scalacenter.github.io/bloop/) (or [sbt](https://www.scala-sbt.org/))
* [Java 17](https://www.oracle.com/uk/java/technologies/downloads/)

## Entrypoints

### HTTP Server

The server can be started by executing the following command:

```bash
bloop run synonyms -m synonyms.SynonymsApi
```

The following endpoints are supported:

* Check if two words are synonyms\
  `GET /synonyms?words=foo&words=bar[&thesaurus=datamuse]`
* List synonyms for a given word\
  `GET /synonyms/foo[?thesaurus=datamuse]`

### Command line

Command line invocation supports the following commands:

* Check if two words are synonyms\
  `bloop run synonyms -m synonyms.SynonymsCli --args check [--args '--format=json'] --args house --args pad`
* List synonyms for a given word\
  `bloop run synonyms -m synonyms.SynonymsCli --args list [--args '--format=json'] [--args '--source|-s datamuse'] --args bar`
## Thesauruses

The app currently supports the following thesauruses:

* [Cambridge](https://dictionary.cambridge.org/thesaurus/) (`cambridge`)
* [Datamuse](https://www.datamuse.com/api/) (`datamuse`)
* [Merriam-Webster](https://www.merriam-webster.com/thesaurus) (`mw`)

Default is all thesauruses

## Run tests

`bloop test synonyms-test`

## Create a Docker image locally

`sbt Docker/publishLocal`
