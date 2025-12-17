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
SLASH = ("/")
OR = ("||")
AND = ("&&")
DUOBLE_COLON=("::")
OPEN_BRACE = [{]
CLOSE_BRACE = [}]
OPEN_BRACKET = ("[")
CLOSE_BRACKET =  ("]")
QUESTION_MARK = ("?")
BACKTICK=`[^`]*`
BOOL_LITERAL=(true) | (false)
NUMBER_LITERAL=[+-]?([0-9]*[.])?[0-9]+
X_INDICATOR=("x")
F_INDICATOR=("f")
STRING_STARTER=[\"'`]
INDENTED_BACKTICK=(```)([`]{0,2}([^`]))*(```)
RAW_STRING=('[^']*')
INDENTED_RAW_STRING=(''')([']{0,2}([^']))*(''')
STRING=(\"((\\\")|[^\"])*\")
INDENTED_STRING=(\"\"\")([\"]{0,2}([^\"]))*(\"\"\")
EXPORT_NAME=[a-zA-Z_][a-zA-Z0-9_\-]*
//ATTRIBUTE_NAME=([a-zA-Z0-9_\-]+)
ATTRIBUTE_NAME= arg | confirm | default | doc | extension | group | linux | macos | metadata | ("no-cd") | ("no-exit-message") | ("no-quieet") | openbsd | ("positional-arguments") | private | parallel | script | unix | windows | ("working-directory")
ID_LITERAL=[a-zA-Z_][a-zA-Z0-9_\-]*
SETTING=[a-zA-Z_][a-zA-Z0-9_\-]*
MOD_NAME=[a-zA-Z_][a-zA-Z0-9_\-]*
RECIPE_NAME=[a-zA-Z_][a-zA-Z0-9_\-]*
DEPENDENCY_NAME=[a-zA-Z_][a-zA-Z0-9_\-]*
SHEBANG=("#!")[^\n]*
COMMENT=("#")[^\n]*
DOUBLE_AND=("&&")
VARIABLE=([a-zA-Z_][a-zA-Z0-9_-]*)
VARIABLE_DECLARE=([a-zA-Z_][a-zA-Z0-9_-]*)(\s*)(":=")
RECIPE_PARAMS=([^:\n]*)
RECIPE_PARAM_NAME=[*+]?[$]?[a-zA-Z_][a-zA-Z0-9_\-]*
RECIPE_SIMPLE_PARARM1=[$*+]?[a-zA-Z_][a-zA-Z0-9_\-]*
RECIPE_SIMPLE_PARARM2=[$*+]?[a-zA-Z_][a-zA-Z0-9_\-]*=[a-zA-Z0-9_\-]*
RECIPE_PAIR_PARARM1=[$*+]?[a-zA-Z_][a-zA-Z0-9_\-]*='[^':]*'
RECIPE_PAIR_PARARM2=[$*+]?[a-zA-Z_][a-zA-Z0-9_\-]*=`[^`:]*`
RECIPE_PAIR_PARARM3=[$*+]?[a-zA-Z_][a-zA-Z0-9_\-]*=\([^:\(]*\)
CODE=((\n[ \t]+[^\n]*)|(\n[ \t]*))*

KEYWORD_ALIAS=(alias)
KEYWORD_EXPORT=(export)
KEYWORD_UNEXPORT=(unexport)
KEYWORD_SET=(set)
KEYWORD_MOD=(mod)
KEYWORD_IMPORT=(import)
KEYWORD_IF=(if)
KEYWORD_ELSE=(else)
KEYWORD_ELSE_IF=("else if")

%state MOD IMPORT ALIAS VARIABLE_PAREN VARIABLE_PAREN_FUNCTION_CALL VARIABLE CONDITIONAL CONDITIONAL_BLOCK_BODY CONDITIONAL_END UNEXPORT EXPORT EXPORT_VALUE SET SET_VALUE ATTRIBUTE RECIPE PARAMS PARAM_WITH_VALUE PARAM_WITH_VALUE_FUNCTION_CALL DEPENDENCIES DEPENDENCY_WITH_PARAMS DEPENDENCY_CALL_PARAMS

%%

<MOD> {
  {QUESTION_MARK}    {  yybegin(MOD); return QUESTION_MARK; }
  {WHITE_SPACE}+     {  yybegin(MOD); return TokenType.WHITE_SPACE; }
  {MOD_NAME}         {  yybegin(MOD); return MOD_NAME; }
  {STRING}           {  yybegin(MOD); return STRING; }
  {RAW_STRING}       {  yybegin(MOD); return RAW_STRING; }
  {X_INDICATOR}/ {STRING_STARTER}  {  yybegin(MOD); return X_INDICATOR; }
  {F_INDICATOR}/ {STRING_STARTER}  {  yybegin(MOD); return F_INDICATOR; }
  {COMMENT}         {  yybegin(MOD); return JustTypes.COMMENT; }
  {NEW_LINE}         {  yybegin(YYINITIAL); return JustTypes.NEW_LINE; }
}

<IMPORT> {
  {QUESTION_MARK}    {  yybegin(IMPORT); return QUESTION_MARK; }
  {WHITE_SPACE}+     {  yybegin(IMPORT); return TokenType.WHITE_SPACE; }
  {STRING}      {  yybegin(IMPORT); return STRING; }
  {RAW_STRING}      {  yybegin(IMPORT); return RAW_STRING; }
  {X_INDICATOR}/ {STRING_STARTER}  {  yybegin(IMPORT); return X_INDICATOR; }
  {F_INDICATOR}/ {STRING_STARTER}  {  yybegin(IMPORT); return F_INDICATOR; }
  {COMMENT}         {  yybegin(IMPORT); return JustTypes.COMMENT; }
  {NEW_LINE}         {  yybegin(YYINITIAL); return JustTypes.NEW_LINE; }
}

<ALIAS> {
  {WHITE_SPACE}+     {  yybegin(ALIAS); return TokenType.WHITE_SPACE; }
  {ASSIGN}           {  yybegin(ALIAS); return ASSIGN; }
  {RECIPE_NAME}      {  yybegin(ALIAS); return RECIPE_NAME; }
  {DUOBLE_COLON}      {  yybegin(ALIAS); return DUOBLE_COLON; }
  {COMMENT}         {  yybegin(ALIAS); return JustTypes.COMMENT; }
  {NEW_LINE}         {  yybegin(YYINITIAL); return JustTypes.NEW_LINE; }
}

<UNEXPORT> {
  {WHITE_SPACE}+     {  yybegin(EXPORT); return TokenType.WHITE_SPACE; }
  {EXPORT_NAME}      {  yybegin(YYINITIAL); return EXPORT_NAME; }
}

<EXPORT> {
  {WHITE_SPACE}+     {  yybegin(EXPORT); return TokenType.WHITE_SPACE; }
  {EXPORT_NAME}      {  yybegin(EXPORT_VALUE); return EXPORT_NAME; }
}

<EXPORT_VALUE> {
   {WHITE_SPACE}+     {  yybegin(EXPORT_VALUE); return TokenType.WHITE_SPACE; }
   {ASSIGN}           {  yybegin(EXPORT_VALUE); return ASSIGN; }
   {X_INDICATOR}/ {STRING_STARTER}  {  yybegin(EXPORT_VALUE); return X_INDICATOR; }
   {F_INDICATOR}/ {STRING_STARTER}  {  yybegin(EXPORT_VALUE); return F_INDICATOR; }
   {PLUS}           {  yybegin(EXPORT_VALUE); return PLUS; }
   {SLASH}           {  yybegin(EXPORT_VALUE); return SLASH; }
   {OR}           {  yybegin(EXPORT_VALUE); return OR; }
   {AND}           {  yybegin(EXPORT_VALUE); return AND; }
   {STRING}           {  yybegin(EXPORT_VALUE); return STRING; }
   {RAW_STRING}       {  yybegin(EXPORT_VALUE); return RAW_STRING; }
   {INDENTED_BACKTICK} {  yybegin(EXPORT_VALUE); return INDENTED_BACKTICK; }
   {BACKTICK}         {  yybegin(EXPORT_VALUE); return BACKTICK; }
   {OPEN_PAREN}      {  yybegin(EXPORT_VALUE); return OPEN_PAREN; }
   {CLOSE_PAREN}      {  yybegin(EXPORT_VALUE); return CLOSE_PAREN; }
   {COMMA}      {  yybegin(EXPORT_VALUE); return COMMA; }
   {ID_LITERAL}          {  yybegin(EXPORT_VALUE); return ID_LITERAL; }
   {COMMENT}         {  yybegin(EXPORT_VALUE); return COMMENT; }
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
   {F_INDICATOR}/ {STRING_STARTER}   {  yybegin(SET_VALUE); return F_INDICATOR; }
   {STRING}            {  yybegin(SET_VALUE); return STRING; }
   {RAW_STRING}        {  yybegin(SET_VALUE); return RAW_STRING; }
   {ID_LITERAL}           {  yybegin(SET_VALUE); return ID_LITERAL; }
   {OPEN_BRACKET}      {  yybegin(SET_VALUE); return OPEN_BRACKET; }
   {CLOSE_BRACKET}      {  yybegin(SET_VALUE); return CLOSE_BRACKET; }
   {COMMA}              {  yybegin(SET_VALUE); return COMMA; }
   {COMMENT}          {  yybegin(SET_VALUE); return JustTypes.COMMENT; }
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
  {F_INDICATOR}/ {STRING_STARTER}           {  yybegin(VARIABLE); return F_INDICATOR; }
  {RAW_STRING}                 {  yybegin(VARIABLE); return RAW_STRING; }
  {BACKTICK}                   {  yybegin(VARIABLE); return BACKTICK; }
  {ID_LITERAL}                    {  yybegin(VARIABLE); return ID_LITERAL; }
  {PLUS}                      {  yybegin(VARIABLE); return PLUS; }
  {SLASH}                      {  yybegin(VARIABLE); return SLASH; }
  {OR}                      {  yybegin(VARIABLE); return OR; }
  {AND}                      {  yybegin(VARIABLE); return AND; }
  {OPEN_PAREN}               {  yybegin(VARIABLE); return OPEN_PAREN; }
  {CLOSE_PAREN}               {  yybegin(VARIABLE); return CLOSE_PAREN; }
  {COMMA}               {  yybegin(VARIABLE); return COMMA; }
  {COMMENT}                   {  yybegin(VARIABLE); return JustTypes.COMMENT; }
  {NEW_LINE}                   {  yybegin(YYINITIAL); return JustTypes.NEW_LINE; }
}

<VARIABLE_PAREN> {
  {WHITE_SPACE}+               {  yybegin(VARIABLE_PAREN); return TokenType.WHITE_SPACE; }
  {ASSIGN}                     {  yybegin(VARIABLE_PAREN); return ASSIGN; }
  {INDENTED_BACKTICK}          {  yybegin(VARIABLE_PAREN); return INDENTED_BACKTICK; }
  {INDENTED_RAW_STRING}        {  yybegin(VARIABLE_PAREN); return INDENTED_RAW_STRING; }
  {INDENTED_STRING}            {  yybegin(VARIABLE_PAREN); return INDENTED_STRING; }
  {STRING}                     {  yybegin(VARIABLE_PAREN); return STRING; }
  {X_INDICATOR}/ {STRING_STARTER}           {  yybegin(VARIABLE_PAREN); return X_INDICATOR; }
  {F_INDICATOR}/ {STRING_STARTER}           {  yybegin(VARIABLE_PAREN); return F_INDICATOR; }
  {RAW_STRING}                 {  yybegin(VARIABLE_PAREN); return RAW_STRING; }
  {BACKTICK}                   {  yybegin(VARIABLE_PAREN); return BACKTICK; }
   {ID_LITERAL}  / (\s*){OPEN_PAREN} {  yybegin(VARIABLE_PAREN_FUNCTION_CALL); return ID_LITERAL; }
  {ID_LITERAL}                    {  yybegin(VARIABLE_PAREN); return ID_LITERAL; }
  {PLUS}                      {  yybegin(VARIABLE_PAREN); return PLUS; }
  {SLASH}                      {  yybegin(VARIABLE_PAREN); return SLASH; }
  {OR}                        {  yybegin(VARIABLE_PAREN); return OR; }
  {AND}                      {  yybegin(VARIABLE_PAREN); return AND; }
  {OPEN_PAREN}               {  yybegin(VARIABLE_PAREN); return OPEN_PAREN; }
  {CLOSE_PAREN}               {  yybegin(YYINITIAL); return CLOSE_PAREN; }
  {COMMA}                     {  yybegin(VARIABLE_PAREN); return COMMA; }
  {COMMENT}                   {  yybegin(VARIABLE_PAREN); return JustTypes.COMMENT; }
  {NEW_LINE}                   {  yybegin(VARIABLE_PAREN); return JustTypes.NEW_LINE; }
}

<VARIABLE_PAREN_FUNCTION_CALL> {
    {WHITE_SPACE}+              {  yybegin(VARIABLE_PAREN_FUNCTION_CALL); return TokenType.WHITE_SPACE; }
    {STRING}                    {  yybegin(VARIABLE_PAREN_FUNCTION_CALL); return STRING; }
    {X_INDICATOR}/ {STRING_STARTER}  {  yybegin(VARIABLE_PAREN_FUNCTION_CALL); return X_INDICATOR; }
    {F_INDICATOR}/ {STRING_STARTER}  {  yybegin(VARIABLE_PAREN_FUNCTION_CALL); return F_INDICATOR; }
    {RAW_STRING}                {  yybegin(VARIABLE_PAREN_FUNCTION_CALL); return RAW_STRING; }
    {BACKTICK}                  {  yybegin(VARIABLE_PAREN_FUNCTION_CALL); return BACKTICK; }
    {ID_LITERAL}                {  yybegin(VARIABLE_PAREN_FUNCTION_CALL); return ID_LITERAL; }
    {PLUS}                      {  yybegin(VARIABLE_PAREN_FUNCTION_CALL); return PLUS; }
    {SLASH}                     {  yybegin(VARIABLE_PAREN_FUNCTION_CALL); return SLASH; }
    {OR}                     {  yybegin(VARIABLE_PAREN_FUNCTION_CALL); return OR; }
    {AND}                     {  yybegin(VARIABLE_PAREN_FUNCTION_CALL); return AND; }
    {OPEN_PAREN}                {  yybegin(VARIABLE_PAREN_FUNCTION_CALL); return OPEN_PAREN; }
    {CLOSE_PAREN}               {  yybegin(VARIABLE_PAREN); return CLOSE_PAREN; }
    {COMMA}                     {  yybegin(VARIABLE_PAREN_FUNCTION_CALL); return COMMA; }
    {BOOL_LITERAL}              {  yybegin(VARIABLE_PAREN_FUNCTION_CALL); return BOOL_LITERAL; }
}

<CONDITIONAL> {
  {WHITE_SPACE}+             {  yybegin(CONDITIONAL); return TokenType.WHITE_SPACE; }
  {EQEQ}                     {  yybegin(CONDITIONAL); return EQEQ; }
  {NOEQ}                     {  yybegin(CONDITIONAL); return NOEQ; }
  {REEQ}                     {  yybegin(CONDITIONAL); return REEQ; }
  {OR}                       {  yybegin(CONDITIONAL); return OR; }
  {AND}                      {  yybegin(CONDITIONAL); return AND; }
  {KEYWORD_IF}               {  yybegin(CONDITIONAL); return KEYWORD_IF; }
  {KEYWORD_ELSE}             {  yybegin(CONDITIONAL_END); return KEYWORD_ELSE; }
  {KEYWORD_ELSE_IF}           {  yybegin(CONDITIONAL); return KEYWORD_ELSE_IF; }
  {INDENTED_BACKTICK}          {  yybegin(CONDITIONAL); return INDENTED_BACKTICK; }
  {INDENTED_RAW_STRING}        {  yybegin(CONDITIONAL); return INDENTED_RAW_STRING; }
  {INDENTED_STRING}            {  yybegin(CONDITIONAL); return INDENTED_STRING; }
  {STRING}                     {  yybegin(CONDITIONAL); return STRING; }
  {X_INDICATOR}/ {STRING_STARTER}            {  yybegin(CONDITIONAL); return X_INDICATOR; }
  {F_INDICATOR}/ {STRING_STARTER}            {  yybegin(CONDITIONAL); return F_INDICATOR; }
  {RAW_STRING}                 {  yybegin(CONDITIONAL); return RAW_STRING; }
  {BACKTICK}                   {  yybegin(CONDITIONAL); return BACKTICK; }
  {ID_LITERAL}                    {  yybegin(CONDITIONAL); return ID_LITERAL; }
  {PLUS}               {  yybegin(CONDITIONAL); return PLUS; }
  {SLASH}               {  yybegin(CONDITIONAL); return SLASH; }
  {OPEN_BRACE}               {  yybegin(CONDITIONAL_BLOCK_BODY); return OPEN_BRACE; }
  {CLOSE_BRACE}               {  yybegin(CONDITIONAL); return CLOSE_BRACE; }
  {OPEN_PAREN}               {  yybegin(CONDITIONAL); return OPEN_PAREN; }
  {CLOSE_PAREN}               {  yybegin(CONDITIONAL); return CLOSE_PAREN; }
  {COMMA}                    {  yybegin(CONDITIONAL); return COMMA; }
  {NEW_LINE}                  {  yybegin(CONDITIONAL); return JustTypes.NEW_LINE; }
}

<CONDITIONAL_BLOCK_BODY> {
    {WHITE_SPACE}+               {  yybegin(CONDITIONAL_BLOCK_BODY); return TokenType.WHITE_SPACE; }
    {NEW_LINE}                  {  yybegin(CONDITIONAL_BLOCK_BODY); return JustTypes.NEW_LINE; }
    {INDENTED_BACKTICK}          {  yybegin(CONDITIONAL_BLOCK_BODY); return INDENTED_BACKTICK; }
    {INDENTED_RAW_STRING}        {  yybegin(CONDITIONAL_BLOCK_BODY); return INDENTED_RAW_STRING; }
    {INDENTED_STRING}            {  yybegin(CONDITIONAL_BLOCK_BODY); return INDENTED_STRING; }
    {STRING}                     {  yybegin(CONDITIONAL_BLOCK_BODY); return STRING; }
    {X_INDICATOR}/ {STRING_STARTER}   {  yybegin(CONDITIONAL_BLOCK_BODY); return X_INDICATOR; }
    {F_INDICATOR}/ {STRING_STARTER}   {  yybegin(CONDITIONAL_BLOCK_BODY); return F_INDICATOR; }
    {RAW_STRING}                 {  yybegin(CONDITIONAL_BLOCK_BODY); return RAW_STRING; }
    {BACKTICK}                   {  yybegin(CONDITIONAL_BLOCK_BODY); return BACKTICK; }
    {ID_LITERAL}                 {  yybegin(CONDITIONAL_BLOCK_BODY); return ID_LITERAL; }
    {OPEN_PAREN}               {  yybegin(CONDITIONAL_BLOCK_BODY); return OPEN_PAREN; }
    {CLOSE_PAREN}               {  yybegin(CONDITIONAL_BLOCK_BODY); return CLOSE_PAREN; }
    {PLUS}                    {  yybegin(CONDITIONAL_BLOCK_BODY); return PLUS; }
    {SLASH}                  {  yybegin(CONDITIONAL_BLOCK_BODY); return SLASH; }
    {COMMA}                    {  yybegin(CONDITIONAL_BLOCK_BODY); return COMMA; }
    {CLOSE_BRACE}                {  yybegin(CONDITIONAL); return CLOSE_BRACE; }
}

<CONDITIONAL_END> {
   {WHITE_SPACE}+               {  yybegin(CONDITIONAL_END); return TokenType.WHITE_SPACE; }
   {NEW_LINE}                   {  yybegin(CONDITIONAL_END); return JustTypes.NEW_LINE; }
   {OPEN_BRACE}                 {  yybegin(CONDITIONAL_END); return OPEN_BRACE; }
   {INDENTED_BACKTICK}          {  yybegin(CONDITIONAL_END); return INDENTED_BACKTICK; }
   {INDENTED_RAW_STRING}        {  yybegin(CONDITIONAL_END); return INDENTED_RAW_STRING; }
   {INDENTED_STRING}            {  yybegin(CONDITIONAL_END); return INDENTED_STRING; }
   {STRING}                     {  yybegin(CONDITIONAL_END); return STRING; }
   {X_INDICATOR}/ {STRING_STARTER}   {  yybegin(CONDITIONAL_END); return X_INDICATOR; }
   {F_INDICATOR}/ {STRING_STARTER}   {  yybegin(CONDITIONAL_END); return F_INDICATOR; }
   {RAW_STRING}                 {  yybegin(CONDITIONAL_END); return RAW_STRING; }
   {BACKTICK}                   {  yybegin(CONDITIONAL_END); return BACKTICK; }
   {ID_LITERAL}                 {  yybegin(CONDITIONAL_END); return ID_LITERAL; }
   {OPEN_PAREN}               {  yybegin(CONDITIONAL_END); return OPEN_PAREN; }
   {CLOSE_PAREN}               {  yybegin(CONDITIONAL_END); return CLOSE_PAREN; }
   {PLUS}                    {  yybegin(CONDITIONAL_END); return PLUS; }
   {SLASH}                  {  yybegin(CONDITIONAL_END); return SLASH; }
   {COMMA}                    {  yybegin(CONDITIONAL_END); return COMMA; }
   {CLOSE_BRACE}                {  yybegin(YYINITIAL); return JustTypes.CLOSE_BRACE; }
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
  {F_INDICATOR}/ {STRING_STARTER}  {  yybegin(PARAMS); return F_INDICATOR; }
  {RAW_STRING}                {  yybegin(PARAMS); return RAW_STRING; }
  {PLUS}                       {  yybegin(PARAMS); return PLUS; }
  {SLASH}                       {  yybegin(PARAMS); return SLASH; }
  {BACKTICK}                  {  yybegin(PARAMS); return BACKTICK; }
  {ID_LITERAL} / {OPEN_PAREN}   {  yybegin(PARAM_WITH_VALUE_FUNCTION_CALL); return ID_LITERAL; }
  {ID_LITERAL}                  {  yybegin(PARAMS); return ID_LITERAL; }
  {OPEN_PAREN}               {  yybegin(PARAM_WITH_VALUE_FUNCTION_CALL); return OPEN_PAREN; }
  {BOOL_LITERAL}              {  yybegin(PARAMS); return BOOL_LITERAL; }
  {WHITE_SPACE}+              {  yybegin(PARAMS); return TokenType.WHITE_SPACE; }
  {SEPERATOR}                 {  yybegin(DEPENDENCIES); return SEPERATOR; }
}

<PARAM_WITH_VALUE_FUNCTION_CALL> {
    {WHITE_SPACE}+              {  yybegin(PARAM_WITH_VALUE_FUNCTION_CALL); return TokenType.WHITE_SPACE; }
    {STRING}                    {  yybegin(PARAM_WITH_VALUE_FUNCTION_CALL); return STRING; }
    {X_INDICATOR}/ {STRING_STARTER}  {  yybegin(PARAM_WITH_VALUE_FUNCTION_CALL); return X_INDICATOR; }
    {F_INDICATOR}/ {STRING_STARTER}  {  yybegin(PARAM_WITH_VALUE_FUNCTION_CALL); return F_INDICATOR; }
    {RAW_STRING}                {  yybegin(PARAM_WITH_VALUE_FUNCTION_CALL); return RAW_STRING; }
    {BACKTICK}                  {  yybegin(PARAM_WITH_VALUE_FUNCTION_CALL); return BACKTICK; }
    {ID_LITERAL}                {  yybegin(PARAM_WITH_VALUE_FUNCTION_CALL); return ID_LITERAL; }
    {PLUS}                      {  yybegin(PARAM_WITH_VALUE_FUNCTION_CALL); return PLUS; }
    {SLASH}                     {  yybegin(PARAM_WITH_VALUE_FUNCTION_CALL); return SLASH; }
    {OR}                     {  yybegin(PARAM_WITH_VALUE_FUNCTION_CALL); return OR; }
    {AND}                     {  yybegin(PARAM_WITH_VALUE_FUNCTION_CALL); return AND; }
    {OPEN_PAREN}                {  yybegin(PARAM_WITH_VALUE_FUNCTION_CALL); return OPEN_PAREN; }
    {CLOSE_PAREN}               {  yybegin(PARAMS); return CLOSE_PAREN; }
    {COMMA}                     {  yybegin(PARAM_WITH_VALUE_FUNCTION_CALL); return COMMA; }
    {BOOL_LITERAL}              {  yybegin(PARAM_WITH_VALUE_FUNCTION_CALL); return BOOL_LITERAL; }
}

<DEPENDENCIES> {
  {WHITE_SPACE}+           {  yybegin(DEPENDENCIES); return TokenType.WHITE_SPACE; }
  {DOUBLE_AND}             {  yybegin(DEPENDENCIES); return DOUBLE_AND; }
  {DEPENDENCY_NAME}        {  yybegin(DEPENDENCIES); return DEPENDENCY_NAME; }
  {OPEN_PAREN}             {  yybegin(DEPENDENCY_WITH_PARAMS); return OPEN_PAREN; }
  {COMMENT}                {  yybegin(DEPENDENCIES); return COMMENT; }
  {CODE}                   {  yybegin(YYINITIAL); return CODE; }
  {NEW_LINE}               {  yybegin(YYINITIAL); return JustTypes.NEW_LINE; }
}

<DEPENDENCY_WITH_PARAMS> {
 {WHITE_SPACE}+                          {  yybegin(DEPENDENCY_WITH_PARAMS); return TokenType.WHITE_SPACE; }
 {NEW_LINE}                              {  yybegin(DEPENDENCY_WITH_PARAMS); return JustTypes.NEW_LINE; }
 {DEPENDENCY_NAME}                       {  yybegin(DEPENDENCY_CALL_PARAMS); return DEPENDENCY_NAME; }
 {CLOSE_PAREN}                           {  yybegin(DEPENDENCIES); return CLOSE_PAREN; }
}

<DEPENDENCY_CALL_PARAMS> {
 {WHITE_SPACE}+                          {  yybegin(DEPENDENCY_CALL_PARAMS); return TokenType.WHITE_SPACE; }
 {NEW_LINE}                              {  yybegin(DEPENDENCY_CALL_PARAMS); return JustTypes.NEW_LINE; }
 {STRING}                                {  yybegin(DEPENDENCY_CALL_PARAMS); return STRING; }
 {RAW_STRING}                            {  yybegin(DEPENDENCY_CALL_PARAMS); return RAW_STRING; }
 {BACKTICK}                              {  yybegin(DEPENDENCY_CALL_PARAMS); return BACKTICK; }
 {PLUS}                                  {  yybegin(DEPENDENCY_WITH_PARAMS); return PLUS; }
 {SLASH}                                 {  yybegin(DEPENDENCY_WITH_PARAMS); return SLASH; }
 {OR}                                    {  yybegin(DEPENDENCY_WITH_PARAMS); return OR; }
 {AND}                                   {  yybegin(DEPENDENCY_WITH_PARAMS); return AND; }
 {OPEN_PAREN}                            {  yybegin(DEPENDENCY_WITH_PARAMS); return OPEN_PAREN; }
 {COMMA}                                 {  yybegin(DEPENDENCY_WITH_PARAMS); return COMMA; }
 {ID_LITERAL}                            {  yybegin(DEPENDENCY_CALL_PARAMS); return ID_LITERAL; }
 {X_INDICATOR}/ {STRING_STARTER}         {  yybegin(DEPENDENCY_CALL_PARAMS); return X_INDICATOR; }
 {F_INDICATOR}/ {STRING_STARTER}         {  yybegin(DEPENDENCY_CALL_PARAMS); return F_INDICATOR; }
 {CLOSE_PAREN}                           {  yybegin(DEPENDENCIES); return CLOSE_PAREN; }
}

<ATTRIBUTE> {
 {ATTRIBUTE_NAME}                       {  yybegin(ATTRIBUTE); return ATTRIBUTE_NAME; }
 {ID_LITERAL}                  {  yybegin(ATTRIBUTE); return ID_LITERAL; }
 {WHITE_SPACE}+                          {  yybegin(ATTRIBUTE); return TokenType.WHITE_SPACE; }
 {SEPERATOR}                           {  yybegin(ATTRIBUTE); return SEPERATOR; }
 {COMMA}                               {  yybegin(ATTRIBUTE); return COMMA; }
 {EQUAL}                               {  yybegin(ATTRIBUTE); return EQUAL; }
 {OPEN_PAREN}                           {  yybegin(ATTRIBUTE); return OPEN_PAREN; }
 {X_INDICATOR}/ {STRING_STARTER}        {  yybegin(ATTRIBUTE); return X_INDICATOR; }
 {F_INDICATOR}/ {STRING_STARTER}        {  yybegin(ATTRIBUTE); return F_INDICATOR; }
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
  {KEYWORD_IMPORT}/ ([\?]?)([\s]*)(x?)([\"\'])                   { yybegin(IMPORT); return JustTypes.KEYWORD_IMPORT; }
  {KEYWORD_ALIAS}                      { yybegin(ALIAS); return JustTypes.KEYWORD_ALIAS; }
  {KEYWORD_EXPORT} / (\s*)([a-zA-Z_][a-zA-Z0-9_\-]*)(\s*)(":=")  { yybegin(EXPORT); return JustTypes.KEYWORD_EXPORT; }
  {KEYWORD_UNEXPORT} / (\s*)([a-zA-Z_][a-zA-Z0-9_\-]*)(\s*)  { yybegin(UNEXPORT); return JustTypes.KEYWORD_UNEXPORT; }
  {KEYWORD_SET}                        { yybegin(SET); return JustTypes.KEYWORD_SET; }
  {OPEN_BRACKET}                       { yybegin(ATTRIBUTE); return JustTypes.OPEN_BRACKET; }

  // Flex: Lookahead predicate
  {VARIABLE} / (\s*)(":=")(\s*){OPEN_PAREN}  { yybegin(VARIABLE_PAREN); return JustTypes.VARIABLE; }
  {VARIABLE} / (\s*)(":=")             { yybegin(VARIABLE); return JustTypes.VARIABLE; }
  @?{RECIPE_NAME}                      { yybegin(RECIPE); return JustTypes.RECIPE_NAME; }
}

{NEW_LINE}                             { yybegin(YYINITIAL); return JustTypes.NEW_LINE; }

[^]                                    { return TokenType.BAD_CHARACTER; }