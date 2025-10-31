JetBrains Just plugin
========================

<!-- Plugin description -->
**JetBrains Just Command Runner Plugin** is a plugin to integrate [just commander](https://github.com/casey/just) in
JetBrains IDEs.

Plugin features:

* Just language and file types
* Syntax highlight for shell script
* Run recipes by click
* Run configuration support: `Run -> Edit Configurations -> Just` or right click on recipe name
* New justfile by project type, such as Maven, Gradle, Cargo, uv, zig etc.
* Code completion: settings names, recipe dependencies, variables with dotenv support
* Navigation for dependency name
* Language injection for legal Shell script
* Justfile structure view
* Sub modules/import resolver
* Interesting miscellaneous: DuckDB, Bun, Nushell etc.

How to use?

* Download and install `just` command line from https://github.com/casey/just/releases
* Install JetBrains Just plugin
* Invoke 'Justfile' item from New file group, and a `justfile` file will be created with following code:

```
#!/usr/bin/env just --justfile
                    
hello:
  echo "hello world"
```

* Click run button in Gutter and run a recipe/target!

Quick to understand `justfile`, please
click [Justfile cheat sheet](https://cheatography.com/linux-china/cheat-sheets/justfile/)

**Attention**: keywords could not be recipe names, such as `import`, `export`, `alias` etc.

**Tips**: Please put just parameter in quotation mark, such as `echo "{{NAME}}"` or `echo '{{NAME}}'`,
and `echo {{NAME}}` is not legal script and will not be highlighted.
If param as command name, such as `{{MVN}} --version`, please use `exec '{{MVN}}' --version` instead.

<!-- Plugin description end -->

# References

* justfile grammar: https://github.com/casey/just/blob/master/GRAMMAR.md
* justfile on GitHub: https://github.com/search?q=path%3A**%2Fjustfile&type=code
* just VS Code extension: https://marketplace.visualstudio.com/items?itemName=skellock.just
* Justfile cheat sheet: https://cheatography.com/linux-china/cheat-sheets/justfile/
* Grammar-Kit: https://github.com/JetBrains/Grammar-Kit
* just: https://github.com/casey/just
* Developing an Intellij IDEA Plugin for a Custom Language— Tutorial 1-Setting up the
  environment: https://medium.com/@raveensr/developing-an-intellij-idea-plugin-for-a-custom-language-tutorial-1-setting-up-the-environment-aefe310bfcf9
* Custom Language Support: https://plugins.jetbrains.com/docs/intellij/custom-language-support.html
* simple_language_plugin: https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/simple_language_plugin
* Makefile plugin: https://github.com/JetBrains/intellij-plugins/tree/master/makefile
