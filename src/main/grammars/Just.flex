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
EQEQ = ("==")
NOEQ = ("!=")
REEQ = ("=~")
PLUS = ("+")
OPEN_BRACE = [{]
CLOSE_BRACE = [}]
OPEN_BRACKET = ("[")
CLOSE_BRACKET =  ("]")
QUESTION_MARK = ("?")
BACKTICK=`[^`]*`
BOOL_LITERAL=(true) | (false)
NUMBER_LITERAL=[+-]?([0-9]*[.])?[0-9]+
X_INDICATOR=("x")
STRING_STARTER=[\"'`]
INDENTED_BACKTICK=(```)([`]{0,2}([^`]))*(```)
RAW_STRING=('[^']*')
INDENTED_RAW_STRING=(''')([']{0,2}([^']))*(''')
STRING=(\"[^\"]*\")
INDENTED_STRING=(\"\"\")([\"]{0,2}([^\"]))*(\"\"\")
PAREN_PAIRS=\([^\)]*\)
CONDITIONAL_BLOCK=(\{[^}]*\})
EXPORT_NAME=[a-zA-Z_][a-zA-Z0-9_\-]*
ATTRIBUTE_NAME=([a-zA-Z0-9_\-]+)
ID_LITERAL=[a-zA-Z_][a-zA-Z0-9_\-]*
SETTING=[a-zA-Z_][a-zA-Z0-9_\-]*
MOD_NAME=[a-zA-Z_][a-zA-Z0-9_\-]*
RECIPE_NAME=[a-zA-Z_][a-zA-Z0-9_\-]*
DEPENDENCY_NAME=[a-zA-Z_][a-zA-Z0-9_\-]*
LITERAL=[a-zA-Z0-9_\.-]*
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
KEYWORD_ELSE_IF=("else if")

%state MOD IMPORT ALIAS VARIABLE CONDITIONAL CONDITIONAL_END EXPORT EXPORT_VALUE SET SET_VALUE ATTRIBUTE RECIPE PARAMS PARAM_WITH_VALUE DEPENDENCIES DEPENDENCY_WITH_PARAMS DEPENDENCY_CALL_PARAMS

%%

<MOD> {
  {QUESTION_MARK}    {  yybegin(MOD); return QUESTION_MARK; }
  {WHITE_SPACE}+     {  yybegin(MOD); return TokenType.WHITE_SPACE; }
  {MOD_NAME}         {  yybegin(MOD); return MOD_NAME; }
  {STRING}           {  yybegin(MOD); return STRING; }
  {RAW_STRING}       {  yybegin(MOD); return RAW_STRING; }
  {X_INDICATOR}/ {STRING_STARTER}  {  yybegin(MOD); return X_INDICATOR; }
  {NEW_LINE}         {  yybegin(YYINITIAL); return JustTypes.NEW_LINE; }
}

<IMPORT> {
  {QUESTION_MARK}    {  yybegin(IMPORT); return QUESTION_MARK; }
  {WHITE_SPACE}+     {  yybegin(IMPORT); return TokenType.WHITE_SPACE; }
  {STRING}      {  yybegin(IMPORT); return STRING; }
  {RAW_STRING}      {  yybegin(IMPORT); return RAW_STRING; }
  {X_INDICATOR}/ {STRING_STARTER}  {  yybegin(IMPORT); return X_INDICATOR; }
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
  {EXPORT_NAME}      {  yybegin(EXPORT_VALUE); return EXPORT_NAME; }
}

<EXPORT_VALUE> {
   {WHITE_SPACE}+     {  yybegin(EXPORT_VALUE); return TokenType.WHITE_SPACE; }
   {ASSIGN}           {  yybegin(EXPORT_VALUE); return ASSIGN; }
   {X_INDICATOR}/ {STRING_STARTER}  {  yybegin(EXPORT_VALUE); return X_INDICATOR; }
   {PLUS}           {  yybegin(EXPORT_VALUE); return PLUS; }
   {STRING}           {  yybegin(EXPORT_VALUE); return STRING; }
   {RAW_STRING}       {  yybegin(EXPORT_VALUE); return RAW_STRING; }
   {INDENTED_BACKTICK} {  yybegin(EXPORT_VALUE); return INDENTED_BACKTICK; }
   {BACKTICK}         {  yybegin(EXPORT_VALUE); return BACKTICK; }
   {PAREN_PAIRS}      {  yybegin(EXPORT_VALUE); return PAREN_PAIRS; }
   {ID_LITERAL}          {  yybegin(EXPORT_VALUE); return ID_LITERAL; }
   {NEW_LINE}         {  yybegin(YYINITIAL); return JustTypes.NEW_LINE; }
}

<SET> {
  {WHITE_SPACE}+      {  yybegin(SET); return TokenType.WHITE_SPACE; }
  {SETTING}           {  yybegin(SET_VALUE); return SETTING; }
}

<SET_VALUE> {
   {WHITE_SPACE}+      {  yybegin(SET_VALUE); return TokenType.WHITE_SPACE; }
   {ASSIGN}            {  yybegin(SET_VALUE); return ASSIGN; }
   {BOOL_LITERAL}      {  yybegin(SET_VALUE); return BOOL_LITERAL; }
   {NUMBER_LITERAL}    {  yybegin(SET_VALUE); return NUMBER_LITERAL; }
   {X_INDICATOR}/ {STRING_STARTER}   {  yybegin(SET_VALUE); return X_INDICATOR; }
   {STRING}            {  yybegin(SET_VALUE); return STRING; }
   {RAW_STRING}        {  yybegin(SET_VALUE); return RAW_STRING; }
   {LITERAL}           {  yybegin(SET_VALUE); return LITERAL; }
   {OPEN_BRACKET}      {  yybegin(SET_VALUE); return OPEN_BRACKET; }
   {CLOSE_BRACKET}      {  yybegin(SET_VALUE); return CLOSE_BRACKET; }
   {COMMA}              {  yybegin(SET_VALUE); return COMMA; }
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
  {X_INDICATOR}/ {STRING_STARTER}           {  yybegin(VARIABLE); return X_INDICATOR; }
  {RAW_STRING}                 {  yybegin(VARIABLE); return RAW_STRING; }
  {BACKTICK}                   {  yybegin(VARIABLE); return BACKTICK; }
  {ID_LITERAL}                    {  yybegin(VARIABLE); return ID_LITERAL; }
  {PLUS}                      {  yybegin(VARIABLE); return PLUS; }
  {PAREN_PAIRS}               {  yybegin(VARIABLE); return PAREN_PAIRS; }
  {NEW_LINE}                   {  yybegin(YYINITIAL); return JustTypes.NEW_LINE; }
}

<CONDITIONAL> {
  {WHITE_SPACE}+             {  yybegin(CONDITIONAL); return TokenType.WHITE_SPACE; }
  {EQEQ}                     {  yybegin(CONDITIONAL); return EQEQ; }
  {NOEQ}                     {  yybegin(CONDITIONAL); return NOEQ; }
  {REEQ}                     {  yybegin(CONDITIONAL); return REEQ; }
  {CONDITIONAL_BLOCK}        {  yybegin(CONDITIONAL); return CONDITIONAL_BLOCK; }
  {KEYWORD_ELSE}             {  yybegin(CONDITIONAL_END); return KEYWORD_ELSE; }
  {KEYWORD_ELSE_IF}           {  yybegin(CONDITIONAL); return KEYWORD_ELSE_IF; }
  {INDENTED_BACKTICK}          {  yybegin(CONDITIONAL); return INDENTED_BACKTICK; }
  {INDENTED_RAW_STRING}        {  yybegin(CONDITIONAL); return INDENTED_RAW_STRING; }
  {INDENTED_STRING}            {  yybegin(CONDITIONAL); return INDENTED_STRING; }
  {STRING}                     {  yybegin(CONDITIONAL); return STRING; }
  {X_INDICATOR}/ {STRING_STARTER}            {  yybegin(CONDITIONAL); return X_INDICATOR; }
  {RAW_STRING}                 {  yybegin(CONDITIONAL); return RAW_STRING; }
  {BACKTICK}                   {  yybegin(CONDITIONAL); return BACKTICK; }
  {ID_LITERAL}                    {  yybegin(CONDITIONAL); return LITERAL; }
  {PAREN_PAIRS}               {  yybegin(CONDITIONAL); return PAREN_PAIRS; }
  {NEW_LINE}                  {  yybegin(CONDITIONAL); return JustTypes.NEW_LINE; }
}

<CONDITIONAL_END> {
   {WHITE_SPACE}+             {  yybegin(CONDITIONAL_END); return TokenType.WHITE_SPACE; }
   {NEW_LINE}                  {  yybegin(CONDITIONAL_END); return JustTypes.NEW_LINE; }
   {CONDITIONAL_BLOCK}        {  yybegin(YYINITIAL); return CONDITIONAL_BLOCK; }
}

<PARAMS> {
  {WHITE_SPACE}+             {  yybegin(PARAMS); return TokenType.WHITE_SPACE; }
  {RECIPE_PARAM_NAME}        {  yybegin(PARAM_WITH_VALUE); return RECIPE_PARAM_NAME; }
  {SEPERATOR}                {  yybegin(DEPENDENCIES); return SEPERATOR; }
}

<PARAM_WITH_VALUE>{
  {EQUAL}                     {  yybegin(PARAM_WITH_VALUE); return EQUAL; }
  {STRING}                    {  yybegin(PARAMS); return STRING; }
  {X_INDICATOR}/ {STRING_STARTER}  {  yybegin(PARAMS); return X_INDICATOR; }
  {RAW_STRING}                {  yybegin(PARAMS); return RAW_STRING; }
  {BACKTICK}                  {  yybegin(PARAMS); return BACKTICK; }
  {ID_LITERAL}              {  yybegin(PARAMS); return ID_LITERAL; }
  {PAREN_PAIRS}              {  yybegin(PARAMS); return BACKTICK; }
  {BOOL_LITERAL}              {  yybegin(PARAMS); return BOOL_LITERAL; }
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
 {DEPENDENCY_NAME} / {WHITE_SPACE}       {  yybegin(DEPENDENCY_CALL_PARAMS); return DEPENDENCY_NAME; }
 {CLOSE_PAREN}                           {  yybegin(DEPENDENCIES); return CLOSE_PAREN; }
}

<DEPENDENCY_CALL_PARAMS> {
 {WHITE_SPACE}+                          {  yybegin(DEPENDENCY_CALL_PARAMS); return TokenType.WHITE_SPACE; }
 {STRING}                                {  yybegin(DEPENDENCY_CALL_PARAMS); return STRING; }
 {RAW_STRING}                            {  yybegin(DEPENDENCY_CALL_PARAMS); return RAW_STRING; }
 {BACKTICK}                              {  yybegin(DEPENDENCY_CALL_PARAMS); return BACKTICK; }
 {PAREN_PAIRS}                          {  yybegin(DEPENDENCY_WITH_PARAMS); return BACKTICK; }
 {ID_LITERAL}                              {  yybegin(DEPENDENCY_CALL_PARAMS); return ID_LITERAL; }
 {X_INDICATOR}/ {STRING_STARTER}        {  yybegin(DEPENDENCY_CALL_PARAMS); return X_INDICATOR; }
 {COMMA}                                 {  yybegin(DEPENDENCY_CALL_PARAMS); return COMMA; }
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