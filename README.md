<div align="center">
<p>
    <img width="80" src="https://github.com/corgifist/barley-lang/blob/main/dist/logo.png">
</p>
<h1>The Barley Programming Language</h1>
 </div>

[![CI](https://github.com/elixir-lang/elixir/workflows/CI/badge.svg?branch=main)](https://app.circleci.com/pipelines/github/corgifist/barley-lang/10/workflows/45b5d058-5c12-49f8-a44a-0a7f76fdf0c9)

 ## Key features
    
 - Functional programming, manage data in functional style
 - Hand memory management
 - Supports Java interoperability
 - Pattern matching 
 -  - Variables (see `Pattern matching` in `Examples`)
 -  - Functions (see `Pattern matching` in `Examples`)
 -  - Case Expression `case [1, 2, 2] -> of [H :: T]: io:fwriteln("%s | %s", H, T). end.`
 - Multi-assignment
 - Null safety
 - Spawn process, send message and don't worry about errors in it
 - Generator jamming (eval GeneratorAST before running the program)
 - Easily distribute programs by using `dist` module
 - Usually, distributed program is very small in terms of memory
 - Built-in optimization (constant folding, propagation, expression simplification and more)
 - Two Barley instances can talk to each other by sending signals (see `examples/chat.barley`)
 - Clean syntax
 - Built-in terminal code editor


Barley is a interpreted erlang-like language based on JVM. It have simplified syntax and normal strings XD

It have a stack of useful modules like: BTS (Barley-Term-Storage), string, lists, math, types and also barley (the main module)

## Installation

Install the latest release and Java SE 16.

If you are using windows, use `runner.bat` for running scripts and baked apps.

### My `script.barley` doesn't work!

If your script is running in shell, but don't working in Barley.jar

Try to bake it! See more in `Coding our first distributed program`

## Examples

Barley have simplified syntax and other cool things that normal erlang don't have

Simplified syntax and global variables:

```erlang
global ST = stack:new().

eval(S) ->
    String = string:split(S),
    io:fwriteln(String),
    process(Part) || Part -> String,
    pop().

process(P) -> rpn(P).

rpn("+") ->
    B = pop(),
    A = pop(),
    push(A + B).

rpn("-") ->
    B = pop(),
    A = pop(),
    push(A - B).

rpn("*") ->
    B = pop(),
    A = pop(),
    push(A * B).

rpn("/") ->
    B = pop(),
    A = pop(),
    push(A / B).

rpn(X) ->
    io:fwriteln(X),
    push(read(X)).
    
read(N) ->
    case string:as_number(N) ->
        of error: CaughtError.
        of Number: Number.
    end.

push(Value) -> stack:push(ST, Value).
pop() -> stack:pop(ST).
stack_trace() -> io:writeln(stack:stack_to_list(ST)).

main() ->
    io:fwriteln("result: " + eval("2 2 + 3 - 10 *")).
```
    
### Pattern matching

It wouldn't be called erlang if it didn't have pattern matching! Barley supports matching against arrays, H | T, variables, and just constants.

`Barley/Java15 [barley-runtime0.1] [amd64] [threads-2]`

`>>> [A, H|T, "string", [C, D]] = [1, [2, 3, 4], "string", [5, 6]].`

`>>> barley:b().`

`{ST=#Reference<326549596>, A=1, C=5, T=[3, 4], D=6, H=2}`

(barley:b() shows current binding)

Unlike Erlang, variables in Barely can be reassigned!

`Barley/Java15 [barley-runtime0.1] [amd64] [threads-2]`

`>>> A = 15.`

`>>> A = 14.`

Pattern matching is also supported for functions.

This could be seen in example 1, but now it will be better revealed.
```erlang
-module(test).`

-doc("Calculator").`

transform([binary_op, "+", Left, Right]) -> transform(Left) + transform(Right).`

transform([binary_op, "-", Left, Right]) -> transform(Left) - transform(Right).`

transform([binary_op, "*", Left, Right]) -> transform(Left) * transform(Right).`

transform([negate, Left]) -> -Left.`

transform(Expr) -> Expr.`

main() ->

    io:fwriteln(transform([negate, 5])),
    
    io:fwriteln(transform([binary_op, "+", 5, 3])),
    
    io:fwriteln(transform([binary_op, "-", 5, 3])),
    
    io:fwriteln(transform([binary_op, "*", 5, 3])).
```    

Resulst are:

`-5`

`8`

`2`

`15.00`

### Saving data in processes

Okay, this is harder than pattern matching. But I know that you will understand!

To spawn process you need to use `barley:spawn() % => Spawns a process with value 0 and returns PID (process-id)`

Or `barley:spawn(Value) % => Spawns a process with value Value and returns PID(process-id)`

### Receive clause

To catch messages to process you need to create a receive clause.

The recommended function to spawn a process is a:
`start(Value) ->
    receive barley:spawn(Value) -> EXPR.`
    
 When process receives a message Barley automatically creates a variables for make it possible and useful.
 
 Rest - the current value of process.
 
 Message - sent message
 
 ### Isolation of processes
 
 Let's say you've done a process with your receive clause. You send messages to him, extract data from him, 
 
 when suddenly you have not designed some aspects of communication with the process! And you are shown an error. 
 
 Fear not, this error did not affect other processes. Isolation of processes is another Barley feature.
 
 ### Simple process-based program
 
 ```erlang
 start() ->
    receive barley:spawn(0) -> Rest + Message.

call(Pid, Value) ->
    Pid ! Value.

main() ->
    Pid = start(),
    io:writeln(Pid),
    call(Pid, 6),
    call(Pid, 4),
    io:writeln(barley:extract_pid(Pid)).
```

The first call will set process value to 6.

And the second call will set process value to 10.

Everytime you spawn process you will get a unique process-id.

PID is a 3 random values in range of 0.300


The probability that two identical id's will be generated is minimal.

### References

Erlang also have a references. But references in Barley is different to erlang ones.

Reference contains a java class =D

Yes, it is does!

Reference is using in BTS and Stack modules.

You unable to create a reference in barley code.

Because you don't need it!

### Saving && Loading AST in file

Parsing is one of the most difficult processes in executing Barley code. 

With 40 lines of code, it takes about 60 milliseconds! 

You can speed up your program by compiling the code into a Byte-AST file beforehand.

To humans, it looks like a lot of random numbers. But Barley can convert that to AST and execute it!

The conversion process is fast and no data loss! 

Conversion usually takes 0-10 milliseconds for a file of 33 kilobytes. And execution usually takes 1-3 milliseconds! Isn't that wonderful?

Use `barley:compile(Path)` to compile code to ast. The AST file will be created at the path you specified.

Use `barley:ast_from_bianry(Path)` to transform byte-ast to normal ast and run it!

### Signals

Two or more Barley instances can talk to each other by messages.

This messages is named singnals!

Before you will throw && catch messages you need to `signal:create()`

To throw message you need to use `signal:throw(Type, Message)`

To catch message you need to use `signal:on_signal(def (Type, Msg) -> Body. end)`

To catch specific message you need to use `signal:on_named_signal(Type, def(Type, Msg) -> Body. end)`

See `examples/chat.barley` for more information

## Thinking about distribution

All releases of programs written in Barley must be distributed in a baked package method.

For this, Barley has a "dist" module. Why not distribute the entire program or its AST? 

This is too expensive in terms of memory and upgradeability. 

Also, this method reliably protects the source code of the program!

## Coding our first distributed program

So, I'll write Magic-8-Ball. 

It is desirable that the entire program be split into a client file and a server file. 

For example I'll name my files "m_ball_client" and "m_ball_server".

m_ball_server:

```erlang
-module(ball_server).
-doc("Magic ball server").

answer(ID) when ID == 1 -> io:fwriteln("Certainly.").
answer(ID) when ID == 2 -> io:fwriteln("I don't like your tone.").
answer(ID) when ID == 3 -> io:fwriteln("Never.").
answer(ID) when ID == 4 -> io:fwriteln("*Runs away*").
answer(ID) when ID == 5 -> io:fwriteln("Yes.").
answer(ID) when ID == 6 -> io:fwriteln("No.").
answer(ID) when ID == 7 -> io:fwriteln("Of course not.").
answer(ID) when ID == 8 -> io:fwriteln("Of course yes.").
answer(ID) when ID == 9 -> io:fwriteln("Doubtful.").
answer(ID) when ID == 10 -> io:fwriteln("Try again later.").
answer(ID) -> io:fwriteln("*Silence*").

ask(Question) ->
    Answer = math:range(1, 10),
    answer(Answer).
```


m_ball_client:
```erlang
-module(ball_client).
-doc("Ask vital questions!").

ask_loop() ->
    Prompt = read(),
    ball_server:ask(Prompt),
    ask_loop().

read() ->
    io:fwrite(">>> "),
    io:readline().

main() ->
    barley:docs(ball_client),
    ask_loop().
```

Okay, the logic is written. But we still haven't published our program!

It needs to be corrected

Warm up your Barley instance and let's go!

First you need to create a entry point.

Entry point - reference that contains 2 atoms,

First is a module that contains main function,

Second is a name of function.

You better save entry point in variable

Use `dist:entry(Module, Target)` to spawn entry point

So, I made it

```
Barley/Java16 [barley-runtime0.1] [amd64] [threads-2]
>>> Entry = dist:entry(ball_client, main).
>>> 
```

Next you need to bake all logic of your app in one array!

`dist:bake(NameOfApp, Description, EntryPoint, Modules...)` will help you!

You can put unlimited count of modules at the end of arguments!

### !! WARNING !!
To bake modules, you need to have this modules compiled.

If you don't, use `barley:reparse(Dir)`. This will parse and save module
### !! WARNING !!

What are we waiting for?

```
Barley/Java16 [barley-runtime0.1] [amd64] [threads-2]
>>> Entry = dist:entry(ball_client, main).
>>> barley:reparse("examples/magic_ball/m_ball_server.barley").
>>> barley:reparse("examples/magic_ball/m_ball_client.barley").
>>> App = dist:bake("Magic Ball", "Ask vital questions", Entry, ball_client, ball_server).
>>> io:fwriteln(App).
[[name, Magic Ball], [desc, Ask vital questions], [globals, [Entry, #Reference<445884362>]
, [G, 15]], [modules, [ball_client, [read, ...]]
, [ask_loop, ...]], [main, ...]], [doc, Ask vital questions!]], [ball_server, [answer, ...]
, [ask, ...], [doc, Magic ball server]]], [entry_point, #Reference<1031980531>]]
>>> 
```

You're almost ready!

Let's write our info in file


```
Barley/Java16 [barley-runtime0.1] [amd64] [threads-2]
>>> Entry = dist:entry(ball_client, main).
>>> barley:reparse("examples/magic_ball/m_ball_server.barley").
>>> barley:reparse("examples/magic_ball/m_ball_client.barley").
>>> App = dist:bake("Magic Ball", "Ask vital questions", Entry, ball_client, ball_server).
>>> dist:write("baked", App).
```
You can see that in your dir was appeared a wild `baked.app` file.

Here you are your baked module!

Now you have a cool program that you can distribute without any problems!

You can run it using `dist:app(Dir)`

Dist module also have a function to run a bare baked module by using `dist:raw_app(App)`

It's all!

## Rescue Trap (Selective Optimization)

Usually programs written in Barley run within acceptable speed limits. 

But sometimes the application can be so loaded that the speed drops below average, and sometimes low.


Sometimes Barley's built-in optimization can speed up a program by 30-50%. But only for slow programs =( 

Why is this happening?

During optimization, there are many checks, cycles and replacements of the AST. With small programs, this will just slow it down!

Optimization is disabled initially. It can be enabled using `-opt()`

# Amethyst: Parser && Lexer Generator

Barley comes with a bundle of useful modules to create a comfortable environment for programmers!

A separate section will be dedicated to one of them.

Amethyst is a module containing functions for generating parsers and lexers for a given grammar.

### Amethyst Lexer Generator

Let's look at the basics of grammar first.

The grammar file is divided into three segments: Macros, Rules, Catches

```
**Macros**

Rules

**Rules**

Catches

**Catches**
```

#### Macros

Macro is a variable that can be used in rules definitions.

`PLUS = "+"`

This is a simplest macro definition.

### Rules

Each rule is one line divided into segments!

At the time of generation, they are converted into pointers for the lexer.

The format of the rule is:
`TYPE expr -> final_expr`

### Rule Types

Each rule has its own type. The type defines the shape and behavior of a pointer in a lexer

The types can be:

`once expr -> final_expr` at the moment of generation, this rule turns into:
```erlang
process_part(Parts, Symbol) when Symbol == EXPR -> 
  next(Parts),
  FINAL_EXPR.
```

Example:
`once "+" -> [plus, Line, "+"]`

Turns into:

```erlang
process_part(Parts, Symbol) when Symbol == "+" -> 
  next(Parts),
  [plus, Line, "+"].
```

`once_expr expr -> final_expr` at the moment of generation, this rule turns into:

```erlang
process_part(Parts, Symbol) when EXPR -> 
  next(Parts),
  FINAL_EXPR.
```

`skip -> expr` at the moment of generation turns into:

```erlang
process_part(Parts, Symbol) when Symbol == expr ->
  next(Parts), 
  [skip, Line, ""].
```

`anyway -> expr`: if no rules were matched, the anyway clause will run.

`line_increase -> expr`: skips the current char and increases Line variable.

`no_advance expr -> final_expr`: it is like `once` but when it matches expr, it's not increases position.

It is useful for creating ID or DIGIT tokens.

`no_advance_expr expr -> final_expr`: it is like `once_expr` but when it matches expr, it's not increases position.

Now you know a little bit more about Amethyst grammar.

## Example Grammar

Ok, now you can understand the Amethsyt grammar.

So, let's write a simple lexer!

It is will support +-*/, digits and words

```erlang
MINUS = "-"
PLUS = "+"
STAR = "*"
SLASH = "/"
IS_DIGIT = def (S) -> not (string:as_number(S) == error). end
IS_ID = def (S) -> string:is_identifier(S). end

Rules

once "+" -> [plus, Line, "+"]
once "-" -> [minus, Line, "-"]
once "*" -> [star, Line, "*"]
once "/" -> [slash, Line, "/"]
once "=" -> [eq, Line, "="]

no_advance_expr IS_DIGIT(Symbol) -> [number, Line, catch_while_numbers(Parts)]
no_advance_expr IS_ID(Symbol) -> [var, Line, catch_while_id(Parts)]

skip -> " "

line_increase -> "\n"

anyway -> illegal_character(Symbol, Line)

Catches

catch_while_id(Parts) -> catch_while_id(Parts, Pos, Pos).
catch_while_id(Parts, OldPos, NewPos) when lists:nth(Parts, NewPos) == end_of_list ->
    string:join(barley:sublist(Parts, OldPos, NewPos), "").
catch_while_id(Parts, OldPos, NewPos) when IS_ID(peek(Parts, 0)) ->
    next(Parts),
    catch_while_id(Parts, OldPos, NewPos + 1).
catch_while_id(Parts, OldPos, NewPos) -> string:join(barley:sublist(Parts, OldPos, NewPos), "").

catch_while_numbers(Parts) -> catch_while_numbers(Parts, Pos, Pos).
catch_while_numbers(Parts, OldPos, NewPos) when lists:nth(Parts, NewPos) == end_of_list ->
    string:join(barley:sublist(Parts, OldPos, NewPos), "").
catch_while_numbers(Parts, OldPos, NewPos) when not (string:as_number(lists:nth(Parts, NewPos)) == error) ->
    next(Parts),
    catch_while_numbers(Parts, OldPos, NewPos + 1).
catch_while_numbers(Parts, OldPos, NewPos) -> Pos = NewPos, string:join(barley:sublist(Parts, OldPos, NewPos), "").
```

You are probably wondering why catch functions are needed

Catch function captures all characters as long as the condition given to it in its guard is true

### Filtering the output

By default, Amethyst filters the result from unnecessary EOF and SKIP. 
But with incorrect catch functions, extra tokens can be generated. 
You can get rid of them with `lists:filter`

Example of cleaning output of empty VAR tokens: 
```erlang
filter(String) ->
    T = lexer:lex(String),
    T = lists:filter(def (X) -> (not ((lists:nth(X, 0) == var) and lists:nth(X, 2) == "")). end, T),.
```

### Amethyst Parser Generator

The Amethyst's parser generator have simple structure.

But hard syntax.

Each grammar file has only 2 sections: the root expression and states. 

Usually the root expression is one line, it looks like `Root EXPR_NAME`

States is the largest section of the grammar. It contains all the expressions for generating the parser.

Example of grammar that returns math tree:
 
```erlang
Root expression

-opt().

%% Safe-Position state
expression() -> assignment().

assignment() -> make_assign(Pos, text(get(0))).
make_assign(OldPos, Text) when match(var) and match(eq) -> [assign, Text, additive()].
make_assign(OldPos, T) -> Pos = OldPos, additive().

additive() ->  make_add(multiplicative()).
make_add(Expr) when match(plus) -> make_add([binary_op, "+", Expr, multiplicative()]).
make_add(Expr) when match(minus) -> make_add([binary_op, "-", Expr, multiplicative()]).
make_add(Expr) -> Expr.

multiplicative() -> make_mult(unary()).
make_mult(Expr) when match(star) -> make_mult([binary_op, "*", Expr, unary()]).
make_mult(Expr) when match(slash) -> make_mult([binary_op, "/", Expr, unary()]).
make_mult(Expr) -> Expr.

unary() when match(minus) -> [unary_op, "-", primary(text(get(0)))].
unary() -> primary(text(get(0))).

primary(Text) when match(lparen) ->
    Expr = expr(),
    match(rparen),
    Expr.

primary(Text) when match(number) ->
    [value, Text].

primary(Text) when match(var) ->
    [var, Text].
```

The `%% ...` is a comment

This grammar generates this:

```erlang
-module(parser).

global Pos = 0.
global Size = 0.
global Tokens = [].
global Result = [].


type(Tok) -> lists:nth(Tok, 0).
text(Tok) -> lists:nth(Tok, 2).

consume_in_bounds(P) when P < Size -> P.
consume_in_bounds(P) -> Size - 1.

consume_type(Token, Type) -> type(Token) == Type.

get(RelativePos) ->
    FinalPosition = Pos + RelativePos,
    P = consume_in_bounds(FinalPosition),
    lists:nth(Tokens, P).

eval_match(C, T) when type(C) == T -> Pos = Pos + 1, true.

eval_match(C, T) -> false.

match(TokenType) ->
    C = get(0),
    eval_match(C, TokenType).

expr() -> expression
().


-opt().

%% Safe-Position state
expression() -> assignment().

assignment() -> make_assign(Pos, text(get(0))).
make_assign(OldPos, Text) when match(var) and match(eq) -> [assign, Text, additive()].
make_assign(OldPos, T) -> Pos = OldPos, additive().

additive() ->  make_add(multiplicative()).
make_add(Expr) when match(plus) -> make_add([binary_op, "+", Expr, multiplicative()]).
make_add(Expr) when match(minus) -> make_add([binary_op, "-", Expr, multiplicative()]).
make_add(Expr) -> Expr.

multiplicative() -> make_mult(unary()).
make_mult(Expr) when match(star) -> make_mult([binary_op, "*", Expr, unary()]).
make_mult(Expr) when match(slash) -> make_mult([binary_op, "/", Expr, unary()]).
make_mult(Expr) -> Expr.

unary() when match(minus) -> [unary_op, "-", primary(text(get(0)))].
unary() -> primary(text(get(0))).

primary(Text) when match(lparen) ->
    Expr = expr(),
    match(rparen),
    Expr.

primary(Text) when match(number) ->
    [value, Text].

primary(Text) when match(var) ->
    [var, Text].
make_parse() when match(eof) -> Result.
make_parse() -> Expr = [expr()],
                Result = Result + Expr,
                make_parse().

parse(Toks) ->
    Pos = 0,
    Tokens = Toks,
    Size = barley:length(Toks),
    Result = [],
    make_parse().
```

Ok, let's combine a power of generated lexer and generated parser...

And we will get...

(I made a simple interpreter for evaluating tree)

```
>> a = 2 * 3 + 4
[[var, 1, a], [eq, 1, =], [number, 1, 2], [star, 1, *], [number, 1, 3], [plus, 1, +], [number, 1, 4], [eof, -1, ]]
[[assign, a, [binary_op, +, [binary_op, *, [value, 2], [value, 3]], [value, 4]]]]
======================
Evaluation: 0 milliseconds
Lexing: 13 milliseconds
Parsing: 8 milliseconds
Summary: 21 milliseconds

#Reference<1973538135>
```

Yay!!!

It works! Let's try another expression.

```
>> a + 2 + 1
[[var, 1, a], [plus, 1, +], [number, 1, 2], [plus, 1, +], [number, 1, 1], [eof, -1, ]]
[[binary_op, +, [binary_op, +, [var, a], [value, 2]], [value, 1]]]
======================
Evaluation: 0 milliseconds
Lexing: 10 milliseconds
Parsing: 3 milliseconds
Summary: 13 milliseconds

13.00
>> 
```

It is very cool, but our calculator don't support grouping expression.

Grouping expression: `(2 + 2)`

### Adding new expressions

First, let's add a new rule for lexer.

Add a new macros:
```
LPAREN = "("
RPAREN = ")"
```

And a new rules:
```
once LPAREN -> [lparen, Line, "("]
once RPAREN -> [rparen, Line, ")"]
```

Let's test:
```
>> (2 + 2)
[[lparen, 1, (], [number, 1, 2], [plus, 1, +], [number, 1, 2], [rparen, 1, )], [eof, -1, ]]
```

Ok, lexer is done.

Grouping state:
```erlang
primary(Text) when match(lparen) ->
    Expr = expr(),
    match(rparen),
    Expr.
```

I made a grouping expression as the primary.

Let's test:

```
>> (2 + 2) * 2
[[lparen, 1, (], [number, 1, 2], [plus, 1, +], [number, 1, 2], [rparen, 1, )], [star, 1, *], [number, 1, 2], [eof, -1, ]]
[[binary_op, *, [binary_op, +, [value, 2], [value, 2]], [value, 2]]]
======================
Evaluation: 0 milliseconds
Lexing: 4 milliseconds
Parsing: 2 milliseconds
Summary: 6 milliseconds

8.00
```

Yay!!! We did it!

That's all. Have a nice day!

## Pointers

Pointer is a address of given expression!

To point expression use `#EXPR`

To unpoint expression use `##PTR`

To change var of pointer use `PTR >> EXPR`

## Externals

External functions are low-order functions, they are global and can be accessed with `extern` keyword!

Let's see basic external functions and define own!

### Allocations

All basic `extern` functions are made for memory allocations control. They are can have only 1 clause, and compiles to externals table at the moment of parsing.

#### EXTERN FUNCTIONS ARE GLOBAL

`extern sizeof(OBJ)` - returns byte size of object

`extern alloc(SIZE)` - returns a pointer to allocated memory with size SIZE

`extern free(PTR)` - destroys PTR and its object from memory

`extern altlst(ALLOC)` - returns ALLOC's list representation 

`extern alinst(ALLOC, OBJ)` - inserts a OBJ in ALLOC, throws SEGMENTATION ERROR if not enough memory

`extern realloc(ALLOC)` - returns a new pointer to a ALLOC

`extern alcpy(ALLOC)` - returns a pointer to a copy of ALLOC

`extern alcmp(ALLOC1, ALLOC2)` - return `true` if `ALLOC1` and `ALLOC2` are equals

`alszs()` - returns a byte size of left memory

See `examples/externals.barley` to learn more


# Java interoperability

Barley can instantiate java classes!

To get class use `reflection:class("CLASSPATH")`

To instantiate class use `reflection:instance(Class, ARG1, ARGS2, ...)`

Instance args automatically transforms to a java objects

To call methods from instance use `reflection:call(Instance, METHOD_NAME_STR, ARG1, ARG2, ...)`

Calls always returns a `ObjectValue{}`, it is a new data type, that can't be generated from other code!

See `examples/reflection.barley` to learn more
