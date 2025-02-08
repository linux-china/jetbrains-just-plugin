<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# jetbrains-just-plugin Changelog

## Unreleased

## 0.6.12 - 2025-02-08

### Added

- Adjust native environment variables dialog to set recipe's env 
- Add multi attributes as a single line support: `[no-cd, private]`

## 0.6.11 - 2025-01-17

### Added

- Compatible with JetBrains IDE 2025.1

## 0.6.10 - 2025-01-08

### Added

- Shell highlight for param in quotation mark style: `echo "{{NICK}}"` or `echo '{{NICK}}'`

## 0.6.9 - 2025-01-05

### Added

- Add `venv` live template for Python project with virtualenv
- Add `node` live template for Node.js project
- Add global search for recipe name navigation [#41](https://github.com/linux-china/jetbrains-just-plugin/issues/41)

### Fixed

- Fix region folding problem [#38](https://github.com/linux-china/jetbrains-just-plugin/issues/38)

## 0.6.8 - 2024-11-04

### Added

- Add macro support for Just run configuration: recipe args could
  be [macros](https://www.jetbrains.com/help/idea/built-in-macros.html).

### Fixed

- Fix just formatter long-running issue

## 0.6.7 - 2024-11-01

### Added

- Shell language injection with exported variable names: `export foo := 'hello'` or `hello $foo:`
- Add `&&` and `||` for logical operators : `foo := '' || 'goodbye'`
- Add folding support for custom-defined regions [#38](https://github.com/linux-china/jetbrains-just-plugin/issues/38)
- Add [devenv](https://devenv.sh/) support: load just from `{PROJECT_ROOT}/.devenv/profile/bin/just`

### Fixed

- Fix error for inline comment

## 0.6.6 - 2024-10-20

### Fixed

- Support function call as parameter value: `a b=f('a'):`
- Adjust color output to `auto` mode

## 0.6.5 - 2024-10-17

### Added

- Highlight for x-string(shell expanded): `home_bin := x"${HOME}/bin`
- inlay hints for recipe param and backtick

## 0.6.4 - 2024-10-15

### Fixed

- `import` and `export` could be recipe names.

## 0.6.3 - 2024-10-14

### Fixed

- Fix PATH environment variable injection with project SDK

## 0.6.2 - 2024-10-09

### Added

- Code format for justfile
- Pair Match for `()` and `[]`
- Go To Symbol for recipe names
- live templates: `var`, `exp`, `script`.
- Add project SDK bin directory to PATH env for running justfile
- Enhancement for syntax highlight

## 0.6.1 - 2024-10-08

### Fixed

- Fix justfile name not saved in run configuration
- Change env variable to text area for multiline
- Use 16x16 icon for justfile

### Added

- Structure view: add import and export support
- New justfile for uv, zig projects

## 0.6.0 - 2024-10-06

### Added

- Rewrite lexer and parser to support more features
- Add Just run configuration: `Run -> Edit Configurations -> Just` or right click on recipe name
- Enhance shell language injection with `shell` detection
- Disable shell language injection if the code is not legal shell script

## 0.5.3 - 2024-09-29

### Added

- Fix variable with function call: `foo := env("FOO","default")`

## 0.5.2 - 2024-09-26

### Added

- Shell expended string support: `foobar := x'~/$FOO/${BAR}'`
- Add conditionals support: `foo := if "hello" =~ 'hel+o' { "match" } else { "mismatch" }`
- Add more functions for code completion
- Compatible with JetBrains IDE 2024.3

## 0.5.1 - 2024-07-19

### Added

- Add `JUST_UNSTABLE` environment variable to run recipe

## 0.5.0 - 2024-07-18

### Added

- Refactor `attribute` lexer

## 0.4.4 - 2024-06-15

### Fixed

- Add `[doc('hello')]` attribute support
- Add `set dotenv-required`

## 0.4.3 - 2024-02-28

### Fixed

- Syntax highlighting fails with double quotes inside triple
  quotes [#23](https://github.com/linux-china/jetbrains-just-plugin/issues/23)
- Understand escaped line breaks in dependencies [#24](https://github.com/linux-china/jetbrains-just-plugin/issues/24)

## 0.4.2 - 2024-02-24

### Fixed

- Import statement causes syntax error [#19](https://github.com/linux-china/jetbrains-just-plugin/issues/19)
- Highlighting for `mod` keyword

## 0.4.1 - 2024-02-17

### Changed

- Compatible with JetBrains IDE 2024.1

## 0.4.0 - 2023-12-30

### Changed

- Module support
- Allow to run recipe for `*.just`
- Justfile name pattern updated to `justfile`, `Justfile`, `.justfile`, `*.just`

## 0.3.7 - 2023-10-29

### Changed

- Compatible with JetBrains IDE 2023.3
- Bug fix for expressions in recipe dependencies

## 0.3.6 - 2023-05-13

### Changed

- Compatible with JetBrains IDE 2023.2

## 0.3.5 - 2023-04-25

### Changed

- Fix match pattern for Strings

## 0.3.4 - 2023-01-26

- Compatible with JetBrains IDE 2023.1

## 0.3.3 - 2022-11-26

- Add attributes: `[private]`, `[no-cd]`, `[no-exit-message]`
- Code completion for `set fallback := true`

## 0.3.2 - 2022-11-06

- Use command line `just -l` for recipe list on popup menu

## 0.3.1 - 2022-11-03

- Popup menu for executing Just recipes: `Run Menu -> Run Just Recipe on Root Module` or `ctrl shift alt J` #7

## 0.3.0 - 2022-11-03

### Added

- OS Recipe Attributes support

## 0.2.8 - 2022-10-30

### Changed

- NPE bug fix when shell language not found
- Code completion for `set fallback := false`

## 0.2.7 - 2022-09-25

### Changed

- Compatible with JetBrains IDE 2022.3

## 0.2.6 - 2022-06-21

### Changed

- Shell language injection: sh, bash, zsh and nu
- Justfile template for Node.js app

## 0.2.5 - 2022-06-17

### Changed

- Folding support for recipe and variable
- Justfile structure view
- Fix blank line in recipe's code block

## 0.2.4 - 2022-06-16

### Changed

- Shell language injection
- windows-shell completion

## 0.2.3 - 2022-05-31

### Changed

- Adjust justfile icon in file explorer to 12x12
- Bug fix syntax error for export statement

## 0.2.2 - 2022-05-20

### Changed

- Compatible with JetBrains IDEs 2022.*

## 0.2.1 - 2022-03-27

### Added

- `&&` support for "Running Recipes at the End of a Recipe" syntax

## 0.2.0 - 2022-03-26

### Added

- Adjust title and icon for command console
- Run Anything Support:  Input `just ` then choose recipe name

## 0.1.0 - 2022-02-18

### Added

- Just language
- Line marker to run Just recipe
- Justfile high light
- New justfile action by project type: Maven, Gradle etc
- Code completion: settings name, recipe dependencies, variables
- Navigation for dependency name
