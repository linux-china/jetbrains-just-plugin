#!/usr/bin/env just --justfile

# record some of the colour formatting
bold := '\033[1m'
normal := '\033[0m'
red := '\033[0;31m'
blue := '\033[0;34m'

# Default with basic intro text
welcome:
    @echo "{{blue}}{{bold}}Welcome{{normal}}"
    @echo "this is normal"

# Generate Parser and Lexer
generate:
  rm -rf src/main/gen/org/mvnsearch
  ./gradlew generateLexer generateParser

# Build just plugin
build-plugin: clean generate
  ./gradlew patchPluginXml buildPlugin

# verify
verify:
   jbang org.jetbrains.intellij.plugins:verifier-cli:1.398:all check-plugin "build/distributions/Just JetBrains plugin-0.6.33.zip" "$HOME/Applications/IntelliJ IDEA 2025.3.1 Release Candidate.app/Contents"

# clean
clean:
  rm -rf build/distributions
  rm -rf build/resources
  rm -rf build/classes
  rm -rf build/instrumented
  rm -rf build/tmp
