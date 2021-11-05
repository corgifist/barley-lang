# Barley

Barley is a interpreted erlang-like language based on JVM. It have simplified syntax and normal strings XD

It have a stack of useful modules like: BTS (Barley-Term-Storage), string, lists, math, types and also barley (the main module)

## Examples

Barley have simplified syntax and other cool things that normal erlang don't have

Simplified syntax and global variables:

`global ST = stack:new().`

`eval(S) ->`
    `String = string:split(S),`
    `io:fwriteln(String),`
    `process(Part) || Part -> String,`
   `pop().`

`process(P) -> rpn(P).`

`rpn("+") ->`
    `B = pop(),`
   ` A = pop(),`
    `push(A + B).`

`rpn("-") ->`
    `B = pop(),`
    `A = pop(),`
    `push(A - B).`

`rpn("*") ->`
   ` B = pop(),`
    `A = pop(),`
    `push(A * B).`

`rpn("/") ->`
    `B = pop(),`
    `A = pop(),`
    `push(A / B).`

`rpn(X) ->`
    `io:fwriteln(X),`
    `push(read(X)).`

`read(N) ->`
    `case string:as_number(N) ->`
        `of error: CaughtError.`
        `of Number: Number.`
    `end.`

`push(Value) -> stack:push(ST, Value).`
`pop() -> stack:pop(ST).`
`stack_trace() -> io:writeln(stack:stack_to_list(ST)).`

`main() ->`
    `io:fwriteln("result: " + eval("2 2 + 3 - 10 *")).`

    
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

`-module(test).`

`-doc("Calculator").`

`transform([binary_op, "+", Left, Right]) -> transform(Left) + transform(Right).`

`transform([binary_op, "-", Left, Right]) -> transform(Left) - transform(Right).`

`transform([binary_op, "*", Left, Right]) -> transform(Left) * transform(Right).`

`transform([negate, Left]) -> -Left.`

`transform(Expr) -> Expr.`

`main() ->`

    io:fwriteln(transform([negate, 5])),
    
    io:fwriteln(transform([binary_op, "+", 5, 3])),
    
    io:fwriteln(transform([binary_op, "-", 5, 3])),
    
    io:fwriteln(transform([binary_op, "*", 5, 3])).
    

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
 
 `start() ->`
    `receive barley:spawn(0) -> Rest + Message.`

`call(Pid, Value) ->`
    `Pid ! Value.`

`main() ->`
    `Pid = start(),`
    
    `io:writeln(Pid),`
    
    `call(Pid, 6),`
    
    `call(Pid, 4),`
    
    `io:writeln(barley:extract_pid(Pid)).`
    
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
