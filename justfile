#!/usr/bin/env just --justfile

# Generate Parser and Lexer
generate:
  rm -rf src/main/gen/org/mvnsearch
  ./gradlew generateLexer generateParser

# Build just plugin
build-plugin: generate
  ./gradlew patchPluginXml buildPlugin