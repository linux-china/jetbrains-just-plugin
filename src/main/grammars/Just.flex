package org.mvnsearch.plugins.just.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import org.mvnsearch.plugins.just.lang.psi.JustTypes;
import static org.mvnsearch.plugins.just.lang.psi.JustTypes.*;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import com.intellij.psi.TokenType;

%%


%{
  public JustLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class JustLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

WHITE_SPACE=[ \t]|(\\\n)
NEW_LINE=\n
ASSIGN=(:=)
SEPERATOR=(:)
OPEN_PAREN = [\(]
CLOSE_PAREN = [\)]
COMMA = [,]
EQUAL = [=]
OPEN_BRACE = [{]
CLOSE_BRACE = [}]
OPEN_BRACKET = ("[")
CLOSE_BRACKET =  ("]")
QUESTION_MARK = ("?")
BACKTICK=`[^`]*`
BOOL_LITERAL=(true) | (false)
NUMBER_LITERAL=[\d]+
INDENTED_BACKTICK=(```)([`]{0,2}([^`]))*(```)
RAW_STRING=('[^']*')
RAW_EXPANDED_STRING=(x'[^']*')
INDENTED_RAW_STRING=(''')([']{0,2}([^']))*(''')
STRING=(\"[^\"]*\")
INDENTED_STRING=(\"\"\")([\"]{0,2}([^\"]))*(\"\"\")
PAREN_STRING=\([^\(]*\)
ID=[a-zA-Z_][a-zA-Z0-9_\-]*
ATTRIBUTE_NAME=([a-zA-Z0-9_\-]+)
ID_LITERAL=[a-zA-Z_][a-zA-Z0-9_\-]*
SETTING=[a-zA-Z_][a-zA-Z0-9_\-]*
MOD_NAME=[a-zA-Z_][a-zA-Z0-9_\-]*
MOD_PATH=[\"'][^\n]*[\"']
IMPORT_PATH=[\"'][^\n]*[\"']
RECIPE_NAME=[a-zA-Z_][a-zA-Z0-9_\-]*
DEPENDENCY_NAME=[a-zA-Z_][a-zA-Z0-9_\-]*
DEPENDENCY_WITH_PARAMS=[a-zA-Z_][a-zA-Z0-9_\-]*\([^\(\n]*\)
DEPENDENCY_PARAMS=[^\(\n\)]*
LITERAL=([^\n \t]*)
SHEBANG=("#!")[^\n]*
COMMENT=("#")[^\n]*
DOUBLE_AND=("&&")
VARIABLE=([a-zA-Z_][a-zA-Z0-9_-]*)
VARIABLE_DECLARE=([a-zA-Z_][a-zA-Z0-9_-]*)(\s*)(":=")
RECIPE_PARAMS=([^:\n]*)
RECIPE_PARAM_NAME=[$*+]?[a-zA-Z_][a-zA-Z0-9_\-]*
RECIPE_SIMPLE_PARARM1=[$*+]?[a-zA-Z_][a-zA-Z0-9_\-]*
RECIPE_SIMPLE_PARARM2=[$*+]?[a-zA-Z_][a-zA-Z0-9_\-]*=[a-zA-Z0-9_\-]*
RECIPE_PAIR_PARARM1=[$*+]?[a-zA-Z_][a-zA-Z0-9_\-]*='[^':]*'
RECIPE_PAIR_PARARM2=[$*+]?[a-zA-Z_][a-zA-Z0-9_\-]*=`[^`:]*`
RECIPE_PAIR_PARARM3=[$*+]?[a-zA-Z_][a-zA-Z0-9_\-]*=\([^:\(]*\)
CODE=((\n[ \t]+[^\n]*)|(\n[ \t]*))*

KEYWORD_ALIAS=(alias)
KEYWORD_EXPORT=(export)
KEYWORD_SET=(set)
KEYWORD_MOD=(mod)
KEYWORD_IMPORT=(import)
KEYWORD_IF=(if)
KEYWORD_ELSE=(else)

%state MOD IMPORT ALIAS VARIABLE CONDITIONAL EXPORT EXPORT_VALUE SET SET_VALUE ATTRIBUTE RECIPE PARAMS PARAM_WITH_VALUE DEPENDENCIES DEPENDENCY_WITH_PARAMS

%%

<MOD> {
  {QUESTION_MARK}    {  yybegin(MOD); return QUESTION_MARK; }
  {WHITE_SPACE}+     {  yybegin(MOD); return TokenType.WHITE_SPACE; }
  {MOD_NAME}         {  yybegin(MOD); return MOD_NAME; }
  {MOD_PATH}         {  yybegin(MOD); return MOD_PATH; }
  {NEW_LINE}         {  yybegin(YYINITIAL); return JustTypes.NEW_LINE; }
}

<IMPORT> {
  {QUESTION_MARK}    {  yybegin(IMPORT); return QUESTION_MARK; }
  {WHITE_SPACE}+     {  yybegin(IMPORT); return TokenType.WHITE_SPACE; }
  {IMPORT_PATH}      {  yybegin(IMPORT); return IMPORT_PATH; }
  {NEW_LINE}         {  yybegin(YYINITIAL); return JustTypes.NEW_LINE; }
}

<ALIAS> {
  {WHITE_SPACE}+     {  yybegin(ALIAS); return TokenType.WHITE_SPACE; }
  {ASSIGN}           {  yybegin(ALIAS); return ASSIGN; }
  {RECIPE_NAME}      {  yybegin(ALIAS); return RECIPE_NAME; }
  {NEW_LINE}         {  yybegin(YYINITIAL); return JustTypes.NEW_LINE; }
}

<EXPORT> {
  {WHITE_SPACE}+     {  yybegin(EXPORT); return TokenType.WHITE_SPACE; }
  {ID}               {  yybegin(EXPORT_VALUE); return ID; }
}

<EXPORT_VALUE> {
   {WHITE_SPACE}+     {  yybegin(EXPORT_VALUE); return TokenType.WHITE_SPACE; }
   {ASSIGN}           {  yybegin(EXPORT_VALUE); return ASSIGN; }
   {STRING}           {  yybegin(EXPORT_VALUE); return STRING; }
   {RAW_STRING}       {  yybegin(EXPORT_VALUE); return RAW_STRING; }
   {INDENTED_BACKTICK} {  yybegin(EXPORT_VALUE); return INDENTED_BACKTICK; }
   {BACKTICK}         {  yybegin(EXPORT_VALUE); return BACKTICK; }
   {LITERAL}          {  yybegin(EXPORT_VALUE); return LITERAL; }
   {NEW_LINE}         {  yybegin(YYINITIAL); return JustTypes.NEW_LINE; }
}

<SET> {
  {WHITE_SPACE}+      {  yybegin(SET); return TokenType.WHITE_SPACE; }
  {SETTING}           {  yybegin(SET_VALUE); return SETTING; }
}

<SET_VALUE> {
   {WHITE_SPACE}+      {  yybegin(SET_VALUE); return TokenType.WHITE_SPACE; }
   {ASSIGN}           {  yybegin(SET_VALUE); return ASSIGN; }
   {BOOL_LITERAL}      {  yybegin(SET_VALUE); return BOOL_LITERAL; }
   {LITERAL}           {  yybegin(SET_VALUE); return LITERAL; }
   {NEW_LINE}          {  yybegin(YYINITIAL); return JustTypes.NEW_LINE; }
}

<VARIABLE> {
  {WHITE_SPACE}+               {  yybegin(VARIABLE); return TokenType.WHITE_SPACE; }
  {ASSIGN}                     {  yybegin(VARIABLE); return ASSIGN; }
  {KEYWORD_IF} / {WHITE_SPACE} {  yybegin(CONDITIONAL); return KEYWORD_IF; }
  {INDENTED_BACKTICK}          {  yybegin(VARIABLE); return INDENTED_BACKTICK; }
  {INDENTED_RAW_STRING}        {  yybegin(VARIABLE); return INDENTED_RAW_STRING; }
  {INDENTED_STRING}            {  yybegin(VARIABLE); return INDENTED_STRING; }
  {STRING}                     {  yybegin(VARIABLE); return STRING; }
  {RAW_STRING}                 {  yybegin(VARIABLE); return RAW_STRING; }
  {RAW_EXPANDED_STRING}        {  yybegin(VARIABLE); return RAW_EXPANDED_STRING; }
  {BACKTICK}                   {  yybegin(VARIABLE); return BACKTICK; }
  {PAREN_STRING}               {  yybegin(VARIABLE); return PAREN_STRING; }
  {LITERAL}                    {  yybegin(VARIABLE); return LITERAL; }
  {NEW_LINE}                   {  yybegin(YYINITIAL); return JustTypes.NEW_LINE; }
}

<CONDITIONAL> {
  {WHITE_SPACE}+             {  yybegin(CONDITIONAL); return TokenType.WHITE_SPACE; }
  {NEW_LINE}             {  yybegin(CONDITIONAL); return NEW_LINE; }
  {OPEN_BRACE}               {  yybegin(CONDITIONAL); return OPEN_BRACE; }
  {CLOSE_BRACE}               {  yybegin(CONDITIONAL); return CLOSE_BRACE; }
  {KEYWORD_IF}/ {WHITE_SPACE}               {  yybegin(CONDITIONAL); return KEYWORD_IF; }
  {KEYWORD_ELSE}/ {WHITE_SPACE}               {  yybegin(CONDITIONAL); return KEYWORD_ELSE; }
  {INDENTED_BACKTICK}          {  yybegin(CONDITIONAL); return INDENTED_BACKTICK; }
    {INDENTED_RAW_STRING}        {  yybegin(CONDITIONAL); return INDENTED_RAW_STRING; }
    {INDENTED_STRING}            {  yybegin(CONDITIONAL); return INDENTED_STRING; }
    {STRING}                     {  yybegin(CONDITIONAL); return STRING; }
    {RAW_STRING}                 {  yybegin(CONDITIONAL); return RAW_STRING; }
    {BACKTICK}                   {  yybegin(CONDITIONAL); return BACKTICK; }
    {PAREN_STRING}               {  yybegin(CONDITIONAL); return PAREN_STRING; }
    {LITERAL}                    {  yybegin(CONDITIONAL); return LITERAL; }
  {CLOSE_BRACE} / {NEW_LINE}        {  yybegin(YYINITIAL); return CLOSE_BRACE; }
}


<PARAMS> {
  {WHITE_SPACE}+             {  yybegin(PARAMS); return TokenType.WHITE_SPACE; }
  {RECIPE_PARAM_NAME}        {  yybegin(PARAM_WITH_VALUE); return RECIPE_PARAM_NAME; }
  {SEPERATOR}                {  yybegin(DEPENDENCIES); return SEPERATOR; }
}

<PARAM_WITH_VALUE>{
  {EQUAL}                     {  yybegin(PARAM_WITH_VALUE); return EQUAL; }
  {STRING}                    {  yybegin(PARAMS); return STRING; }
  {RAW_STRING}                {  yybegin(PARAMS); return RAW_STRING; }
  {BACKTICK}                  {  yybegin(PARAMS); return BACKTICK; }
  {PAREN_STRING}              {  yybegin(PARAMS); return BACKTICK; }
  {ID_LITERAL}                {  yybegin(PARAMS); return ID_LITERAL; }
  {WHITE_SPACE}+              {  yybegin(PARAMS); return TokenType.WHITE_SPACE; }
  {SEPERATOR}                 {  yybegin(DEPENDENCIES); return SEPERATOR; }
}

<DEPENDENCIES> {
  {WHITE_SPACE}+           {  yybegin(DEPENDENCIES); return TokenType.WHITE_SPACE; }
  {DOUBLE_AND}             {  yybegin(DEPENDENCIES); return DOUBLE_AND; }
  {DEPENDENCY_NAME}        {  yybegin(DEPENDENCIES); return DEPENDENCY_NAME; }
  {OPEN_PAREN}             {  yybegin(DEPENDENCY_WITH_PARAMS); return OPEN_PAREN; }
  {CODE}                   {  yybegin(YYINITIAL); return CODE; }
  {NEW_LINE}               {  yybegin(YYINITIAL); return JustTypes.NEW_LINE; }
}

<DEPENDENCY_WITH_PARAMS> {
 {WHITE_SPACE}+                          {  yybegin(DEPENDENCY_WITH_PARAMS); return TokenType.WHITE_SPACE; }
 {DEPENDENCY_NAME} / {WHITE_SPACE}           {  yybegin(DEPENDENCY_WITH_PARAMS); return DEPENDENCY_NAME; }
 {STRING}                                {  yybegin(DEPENDENCY_WITH_PARAMS); return STRING; }
 {RAW_STRING}                            {  yybegin(DEPENDENCY_WITH_PARAMS); return RAW_STRING; }
 {BACKTICK}                              {  yybegin(DEPENDENCY_WITH_PARAMS); return BACKTICK; }
 {PAREN_STRING}                          {  yybegin(DEPENDENCY_WITH_PARAMS); return BACKTICK; }
 {ID_LITERAL}                            {  yybegin(DEPENDENCY_WITH_PARAMS); return BACKTICK; }
 {COMMA}                                 {  yybegin(DEPENDENCY_WITH_PARAMS); return COMMA; }
 {CLOSE_PAREN}                           {  yybegin(DEPENDENCIES); return CLOSE_PAREN; }
}

<ATTRIBUTE> {
 {ATTRIBUTE_NAME}                       {  yybegin(ATTRIBUTE); return ATTRIBUTE_NAME; }
 {WHITE_SPACE}+                          {  yybegin(ATTRIBUTE); return TokenType.WHITE_SPACE; }
 {SEPERATOR}                           {  yybegin(ATTRIBUTE); return SEPERATOR; }
 {OPEN_PAREN}                           {  yybegin(ATTRIBUTE); return OPEN_PAREN; }
 {STRING}                                {  yybegin(ATTRIBUTE); return STRING; }
 {RAW_STRING}                            {  yybegin(ATTRIBUTE); return RAW_STRING; }
 {CLOSE_PAREN}                           {  yybegin(ATTRIBUTE); return CLOSE_PAREN; }
 {CLOSE_BRACKET}                           {  yybegin(YYINITIAL); return CLOSE_BRACKET; }
}

<RECIPE> {
  {WHITE_SPACE}+     {  yybegin(PARAMS); return TokenType.WHITE_SPACE; }
  {SEPERATOR}        {  yybegin(DEPENDENCIES); return SEPERATOR; }
  {NEW_LINE}         {  yybegin(YYINITIAL); return JustTypes.NEW_LINE; }
}

<YYINITIAL> {
  {SHEBANG}                            { yybegin(YYINITIAL); return JustTypes.SHEBANG; }
  {COMMENT}                            { yybegin(YYINITIAL); return JustTypes.COMMENT; }

  {KEYWORD_MOD}                        { yybegin(MOD); return JustTypes.KEYWORD_MOD; }
  {KEYWORD_IMPORT}                     { yybegin(IMPORT); return JustTypes.KEYWORD_IMPORT; }
  {KEYWORD_ALIAS}                      { yybegin(ALIAS); return JustTypes.KEYWORD_ALIAS; }
  {KEYWORD_EXPORT}                     { yybegin(EXPORT); return JustTypes.KEYWORD_EXPORT; }
  {KEYWORD_SET}                        { yybegin(SET); return JustTypes.KEYWORD_SET; }
  {OPEN_BRACKET}                       { yybegin(ATTRIBUTE); return JustTypes.OPEN_BRACKET; }

  // Flex: Lookahead predicate
  {VARIABLE} / (\s*)(":=")             { yybegin(VARIABLE); return JustTypes.VARIABLE; }
  @?{RECIPE_NAME}                      { yybegin(RECIPE); return JustTypes.RECIPE_NAME; }
}

{NEW_LINE}                             { yybegin(YYINITIAL); return JustTypes.NEW_LINE; }

[^]                                    { return TokenType.BAD_CHARACTER; }