# Ptolemy IDML command line tool

A unix-style command line utility for transforming JSON

## Building
`sbt assembly` will build a shaded jar

## Usage

```
Ptolemy IDML command line tool.
Usage: idml [options] <file>...

  --help
        Show usage information and flags
  --pretty <value>
        Enable pretty printing of output
  <file>...
        one or more mapping files to run the data through
```

## Example

```
andi@andi-workstation idml-tool > ./idml idml-examples/footobar.idml
>{"foo": "contents of foo is here"}
<{"bar":"contents of foo is here"}
```
