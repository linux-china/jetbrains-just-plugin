<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# jetbrains-just-plugin Changelog

## [Unreleased]

## [0.3.2] - 2022-11-06

- Use command line `just -l` for recipe list on popup menu

## [0.3.1] - 2022-11-03

- Popup menu for executing Just recipes: `Run Menu -> Run Just Recipe on Root Module` or `ctrl shift alt J` #7

## [0.3.0] - 2022-11-03

### Added

- OS Recipe Attributes support

## [0.2.8] - 2022-10-30

### Changed

- NPE bug fix when shell language not found
- Code completion for `set fallback := false`

## [0.2.7] - 2022-09-25

### Changed

- Compatible with JetBrains IDE 2022.3

## [0.2.6] - 2022-06-21

### Changed

- Shell language injection: sh, bash, zsh and nu
- Justfile template for Node.js app

## [0.2.5] - 2022-06-17

### Changed

- Folding support for recipe and variable
- Justfile structure view
- Fix blank line in recipe's code block

## [0.2.4] - 2022-06-16

### Changed

- Shell language injection
- windows-shell completion

## [0.2.3] - 2022-05-31

### Changed

- Adjust justfile icon in file explorer to 12x12
- Bug fix syntax error for export statement

## [0.2.2] - 2022-05-20

### Changed

- Compatible with JetBrains IDEs 2022.*

## [0.2.1] - 2022-03-27

### Added

- `&&` support for "Running Recipes at the End of a Recipe" syntax

## [0.2.0] - 2022-03-26

### Added

- Adjust title and icon for command console
- Run Anything Support:  Input `just ` then choose recipe name

## [0.1.0] - 2022-02-18

### Added

- Just language
- Line marker to run Just recipe
- Justfile high light
- New justfile action by project type: Maven, Gradle etc
- Code completion: settings name, recipe dependencies, variables
- Navigation for dependency name
