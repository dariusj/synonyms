# Synonyms

An application with multiple entrypoints to perform synonym-related tasks.

This project is a personal playground in Scala to try out different libraries and techniques.

## Requirements

* [Scala-cli](https://scala-cli.virtuslab.org)

## Entrypoints

### HTTP Server

The server can be started by executing the following scala-cli command:

```bash
scala-cli run . --restart -M synonyms.SynonymsApi
```

The following endpoints are supported:

* Check if two words are synonyms\
  `GET /synonyms?words=foo&words=bar[&thesaurus=datamuse]`
* List synonyms for a given word\
  `GET /synonyms/foo[?thesaurus=datamuse]`

### Command line

Command line invocation supports the following commands:

* Check if two words are synonyms\
  `scala-cli run . -M synonyms.SynonymsCli -- check [--source|-s datamuse] house pad`
  `bloop run synonyms -m synonyms.SynonymsCli --args check --args '--format=json' --args house --args pad`
* List synonyms for a given word\
  `scala-cli run . -M synonyms.SynonymsCli -- list [--source|-s datamuse] bar`
  `bloop run synonyms -m synonyms.SynonymsCli --args list --args '--format=json' --args bar`
## Thesauruses

The app currently supports the following thesauruses:

* [Cambridge](https://dictionary.cambridge.org/thesaurus/) (`cambridge`)
* [Datamuse](https://www.datamuse.com/api/) (`datamuse`)
* [Merriam-Webster](https://www.merriam-webster.com/thesaurus) (`mw`)

Default is all thesauruses

## Run tests

`scala-cli test --resource-dirs src/test/resources/`

## Create a Docker image

`scala-cli --power package . -M synonyms.SynonymsApi --docker --docker-image-repository dariusj/synonyms`
