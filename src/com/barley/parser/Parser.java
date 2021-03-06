package com.barley.parser;

import com.barley.Main;
import com.barley.ast.*;
import com.barley.optimizations.TableEmulator;
import com.barley.optimizations.VariableInfo;
import com.barley.patterns.*;
import com.barley.runtime.*;
import com.barley.units.UnitBase;
import com.barley.units.Units;
import com.barley.utils.*;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

public final class Parser implements Serializable {

    private static final Token EOF = new Token(TokenType.EOF, "", -1);
    private final List<Token> tokens;
    private final int size;
    public HashMap<String, Function> methods;
    public boolean opt;
    private int pos;
    private String module, doc;
    private String source;
    private boolean parserStrict = false;
    public boolean ast = false;
    private TableEmulator emulator;
    private boolean inPatterns = false;
    private ArrayList<String> badVars;
    private static HashMap<TokenType, Function> binaryOperators = new HashMap<>();
    private static HashMap<TokenType, Function> unaryOperators = new HashMap<>();
    private static HashMap<String, AST> inlines = new HashMap<>();

    public Parser(List<Token> tokens, String filename) {
        this.tokens = tokens;
        size = tokens.size();
        opt = false;
        methods = new HashMap<>();
        module = null;
        doc = null;
        this.source = filename;
        this.emulator = new TableEmulator();
        this.badVars = new ArrayList<>();
    }

    private String currentLine() {
        List<String> lines = List.of(source.split("\n"));
        try {
            return lines.get(line() - 1);
        } catch (IndexOutOfBoundsException ex) {
            return lines.get(lines.size() - 1);
        }
    }

    public List<AST> parse() {
        final List<AST> result = new ArrayList<>();
        while (!match(TokenType.EOF)) {
            AST expr = declaration();
            result.add(expr);
            consume(TokenType.DOT, "unterminated term.\n    where term: \n        " + expr);
        }
        result.add(new CompileAST(module, methods));
        if (doc != null) Modules.docs.put(module, doc);
        return result;
    }

    public List<AST> parseExpr() {
        final List<AST> result = new ArrayList<>();
        while (!match(TokenType.EOF)) {
            AST expr = expression();
            result.add(expr);
            consume(TokenType.DOT, "unterminated term.\n    where term: \n        " + expr);
        }
        return result;
    }

    private AST block() {
        ArrayList<AST> block = new ArrayList<>();
        emulator.push();
        while (!lookMatch(0, TokenType.DOT)) {
            block.add(expression());
            match(TokenType.COMMA);
        }
        emulator.pop();
        return new BlockAST(block);
    }

    private AST declaration() {
        Token current = get(0);
        String text = current.getText();
        if (match(TokenType.ATOM) || (match(TokenType.DEFGUARD) && !(text = consume(TokenType.ATOM, "expected guard name at line " + line()).getText()).equals(""))) {
            return method(text);
        } else if (match(TokenType.MINUS)) {
            if (match(TokenType.UNIT)) {
                consume(TokenType.LPAREN, "expected '(' before unit name");
                String name = consume(TokenType.ATOM, "expected unit name").getText();
                consume(TokenType.RPAREN, "expected ')' after unit name");
                consume(TokenType.STABBER, "expected '->' after ')'");
                ArrayList<String> fields = new ArrayList<>();
                HashMap<String, AST> defaults = new HashMap<>();
                while (!lookMatch(0, TokenType.DOT)) {
                    fields.add(consume(TokenType.VAR, "expected var name ").getText());
                    if (match(TokenType.EQ)) {
                        defaults.put(fields.get(fields.size() - 1), expression());
                    }
                    match(TokenType.COMMA);
                }
                Units.put(name, new UnitBase(fields, defaults));
            }
            if (match(TokenType.MODULE)) {
                consume(TokenType.LPAREN, "expected '(' before module name");
                module = expression().toString();
                if (Modules.containsKey(module)) {
                    warnParser("Module '" + module + "' overrides pre-compiled library//module");
                }
                consume(TokenType.RPAREN, "expected ')' after module name");
            }
            if (match(TokenType.MODULEDOC)) {
                consume(TokenType.LPAREN, "expected '(' before module doc");
                doc = expression().toString();
                consume(TokenType.RPAREN, "expected ')' after module doc");
            }
            if (match(TokenType.OPT)) {
                consume(TokenType.LPAREN, "expected '(' before opt");
                consume(TokenType.RPAREN, "expected ')' after opt");
                opt = true;
            }
            if (match(TokenType.STRICT)) {
                consume(TokenType.LPAREN, "expected '(' before strict");
                parserStrict = true;
                consume(TokenType.RPAREN, "expected ')' after strict");
                return new ConstantAST(new BarleyNumber(0));
            }
            if (match(TokenType.AST)) {
                consume(TokenType.LPAREN, "expected '(' before ast");
                ast = true;
                consume(TokenType.RPAREN, "expected ')' after ast");
            }
            if (match(TokenType.BINARY_OPERATION)) {
                consume(TokenType.LPAREN, "expected '(' before binary_operation");
                TokenType type = consume(get(0).getType(), "expected qualified token").getType();
                consume(TokenType.RPAREN, "expected ')' before binary_operation");
                consume(TokenType.STABBER, "expected '->' after binary_operation");
                AST expr = expression();
                return parseBinaryExpr(type, expr);
            }

            if (match(TokenType.UNARY_OPERATION)) {
                consume(TokenType.LPAREN, "expected '(' before unary_operation");
                TokenType type = consume(get(0).getType(), "expected qualified token").getType();
                consume(TokenType.RPAREN, "expected ')' before unary_operation");
                consume(TokenType.STABBER, "expected '->' after unary_operation");
                AST expr = expression();
                return parseUnaryExpr(type, expr);
            }

            if (match(TokenType.INLINE)) {
                consume(TokenType.LPAREN, "expected '(' before inline");
                String name = consume(TokenType.ATOM, "expected name after '('").getText();
                consume(TokenType.RPAREN, "expected ')' before inline");
                consume(TokenType.STABBER, "expected '->' after inline");
                inlines.put(name, block());
            }
            match(TokenType.COMMA);
            return new ConstantAST(new BarleyNumber(0));
        } else if (match(TokenType.RECIEVE)) {
            return receive();
        } else if (match(TokenType.EXTERN)) {
            return extern();
        } else if (match(TokenType.GLOBAL)) {
            return global();
        } else throw new BarleyException("BadCompiler", "bad declaration '" + current + "'");
    }

    private AST parseBinaryExpr(TokenType type, AST expr) {
        ArrayList<AST> args = new ArrayList<>();
        args.add(new ExtractBindAST("Left", line(), currentLine()));
        args.add(new ExtractBindAST("Right", line(), currentLine()));
        Clause clause = new Clause(args, null, expr);
        ArrayList<Clause> clauses = new ArrayList<>();
        clauses.add(clause);
        binaryOperators.put(type, new UserFunction(clauses));
        return new ConstantAST(new BarleyNumber(0));
    }

    private AST parseUnaryExpr(TokenType type, AST expr) {
        ArrayList<AST> args = new ArrayList<>();
        args.add(new ExtractBindAST("Operand", line(), currentLine()));
        Clause clause = new Clause(args, null, expr);
        ArrayList<Clause> clauses = new ArrayList<>();
        clauses.add(clause);
        unaryOperators.put(type, new UserFunction(clauses));
        return new ConstantAST(new BarleyNumber(0));
    }

    private AST extern() {
        String name = consume(TokenType.ATOM, "expected name after 'extern'").getText();
        Clause cl = clause();
        consume(TokenType.STABBER, "expected '->' after clause");
        cl.setResult(block());
        ArrayList<Clause> cls = new ArrayList<>();
        cls.add(cl);
        Externals.put(name, new UserFunction(cls));
        return new ConstantAST(new BarleyNumber(0));
    }

    private AST global() {
        return expression();
    }

    private AST receive() {
        AST pid = expression();
        consume(TokenType.STABBER, "error after term '" + pid + "' in receive at line " + line());
        AST body = block();
        return new RecieveAST(pid, body);
    }

    private AST method(String name) {
        Clause clause = clause();
        clause.result = new BlockAST(new ArrayList<AST>());
        if (parserStrict) {
            ((BlockAST) clause.result).block.add(new StrictAST());
        }
        consume(TokenType.STABBER, "error at '" + name + "' declaration");
        if (name.equals("main")) {
            clause.result = new BlockAST(new ArrayList<>());
            BlockAST as = ((BlockAST) clause.getResult());
            as.block.add(new StrictAST());
            clause.result = as;
        }
        clause.setResult(block());
        if (name.equals("main")) {
            BlockAST as = ((BlockAST) clause.getResult());
            as.block.add(new UnStrictAST());
            clause.result = as;
        }
        ArrayList<Clause> clauses = new ArrayList<>();
        if (methods.containsKey(name)) {
            clauses.addAll(((UserFunction) methods.get(name)).getClauses());
        }
        clauses.add(clause);
        methods.put(name, new UserFunction(clauses));
        return new MethodAST(this, new UserFunction(clauses), name);
    }

    private AST expression() {
        if (inlines.containsKey(get(0).getText())) {
            AST result = inlines.get(get(0).getText());
            match(get(0).getType());
            return result;
        }
        return ternary();
    }

    private AST ternary() {
        AST term = or();

        while (true) {
            if (match(TokenType.QUESTION)) {
                AST left = or();
                consume(TokenType.CC, "expected '::' after term in ternary expr at line " + line());
                AST right = or();
                term = new TernaryAST(term, left, right, line(), currentLine());
            }
            break;
        }

        return term;
    }

    private AST or() {
        AST result = and();

        while (true) {
            if (match(TokenType.OR)) {
                result = new BinaryAST(result, and(), 'o', line(), currentLine());
                continue;
            }
            break;
        }

        return result;
    }

    private AST and() {
        AST result = generator();

        while (true) {
            if (match(TokenType.AND)) {
                result = new BinaryAST(result, generator(), 'a', line(), currentLine());
                continue;
            }
            break;
        }

        return result;
    }

    private AST generator() {
        AST generator = assignment();

        if (match(TokenType.BARBAR)) {
            String var = consume(TokenType.VAR, "expected var name after '||' at line " + line()).getText();
            emitVariable(var);
            consume(TokenType.STABBER, "expected '->' after var name at line " + line());
            AST iterable = assignment();
            return new GeneratorAST(generator, var, iterable, line(), currentLine());
        }

        return generator;
    }

    private AST assignment() {
        AST result = conditional();

        while (true) {
            if (match(TokenType.EQ)) {
                emitBind(result);
                result = new BindAST(result, expression(), line(), currentLine());
                continue;
            }
            break;
        }

        if (result instanceof ExtractBindAST p && !emulator.isExists(p.toString())) {
            warnParser("Variable '" + p.toString() + "' is not defined in this scope");
        }

        return result;
    }

    private void emitBind(AST result) {
        Pattern pattern = pattern(result);
        processEmulation(pattern);
    }

    private void processEmulation(Pattern pattern) {
        if (pattern instanceof VariablePattern ex) {
            emitVariable(ex.toString());
        } else if (pattern instanceof ListPattern ex) {
            var patterns = ex.getArr();
            for (AST ast : patterns) {
                processEmulation(pattern(ast));
            }
        } else if (pattern instanceof ConsPattern cons) {
            emitVariable(cons.getLeft());
            emitVariable(cons.getRight());
        } else if (pattern instanceof PackPattern pack) {
            emitVariable(pack.toString());
        }
    }

    private AST conditional() {
        AST result = additive();

        if (match(TokenType.LT)) {
            return new BinaryAST(result, additive(), '<', line(), currentLine());
        }

        if (match(TokenType.GT)) {
            return new BinaryAST(result, additive(), '>', line(), currentLine());
        }

        if (match(TokenType.LTEQ)) {
            return new BinaryAST(result, additive(), 't', line(), currentLine());
        }

        if (match(TokenType.GTEQ)) {
            return new BinaryAST(result, additive(), 'g', line(), currentLine());
        }

        if (match(TokenType.EQEQ)) {
            return new BinaryAST(result, additive(), '=', line(), currentLine());
        }

        if (binaryOperators.containsKey(get(0).getType())) {
            TokenType type = get(0).getType();
            match(get(0).getType());
            AST right = additive();
            ArrayList<AST> args = new ArrayList<>();
            args.add(result); args.add(right);
            return new CallAST(new ConstantAST(new BarleyFunction(binaryOperators.get(type))), args, line(), currentLine());
        }
        return result;
    }

    private AST additive() {
        AST result = multiplicative();

        while (true) {
            if (match(TokenType.PLUS)) {
                result = new BinaryAST(result, multiplicative(), '+', line(), currentLine());
                continue;
            }
            if (match(TokenType.MINUS)) {
                result = new BinaryAST(result, multiplicative(), '-', line(), currentLine());
                continue;
            }
            break;
        }

        return result;
    }

    private AST multiplicative() {
        AST result = unary();

        while (true) {
            // 2 * 6 / 3
            if (match(TokenType.STAR)) {
                result = new BinaryAST(result, unary(), '*', line(), currentLine());
                continue;
            }
            if (match(TokenType.SLASH)) {
                result = new BinaryAST(result, unary(), '/', line(), currentLine());
                continue;
            }

            if (match(TokenType.BANG)) {
                result = new ProcessCallAST(result, unary(), line(), currentLine());
            }

            if (match(TokenType.BAR)) {
                result = new ConsAST(result, unary(), line(), currentLine());
            }

            if (match(TokenType.GTGT)) {
                result = new PointShiftAST(result, expression());
                continue;
            }

            break;
        }

        return result;
    }

    private AST unary() {
        if (match(TokenType.MINUS)) {
            if (match(TokenType.UNIT)) {
                consume(TokenType.LPAREN, "expected '(' before unit name");
                String name = consume(TokenType.ATOM, "expected unit name").getText();
                consume(TokenType.RPAREN, "expected ')' after unit name");
                consume(TokenType.STABBER, "expected '->' after ')'");
                ArrayList<String> fields = new ArrayList<>();
                HashMap<String, AST> defaults = new HashMap<>();
                while (!lookMatch(0, TokenType.DOT)) {
                    fields.add(consume(TokenType.VAR, "expected var name ").getText());
                    if (match(TokenType.EQ)) {
                        defaults.put(fields.get(fields.size() - 1), expression());
                    }
                    match(TokenType.COMMA);
                }
                Units.put(name, new UnitBase(fields, defaults));
                return new ConstantAST(new BarleyNumber(0));
            }
            if (match(TokenType.MODULE)) {
                consume(TokenType.LPAREN, "expected '(' before module name");
                module = expression().toString();
                consume(TokenType.RPAREN, "expected ')' after module name");
                return new ConstantAST(new BarleyNumber(0));
            }
            if (match(TokenType.MODULEDOC)) {
                consume(TokenType.LPAREN, "expected '(' before module doc");
                doc = expression().toString();
                consume(TokenType.RPAREN, "expected ')' after module doc");
                return new ConstantAST(new BarleyNumber(0));
            }
            if (match(TokenType.OPT)) {
                consume(TokenType.LPAREN, "expected '(' before opt");
                consume(TokenType.RPAREN, "expected ')' after opt");
                opt = true;
                return new ConstantAST(new BarleyNumber(0));
            }

            if (match(TokenType.STRICT)) {
                consume(TokenType.LPAREN, "expected '(' before strict");
                Table.strict = true;
                consume(TokenType.RPAREN, "expected ')' after strict");
                return new ConstantAST(new BarleyNumber(0));
            }
            return new UnaryAST(call(), '-', line(), currentLine());
        }

        if (match(TokenType.NOT)) {
            return new UnaryAST(call(), 'n', line(), currentLine());
        }

        if (match(TokenType.UNBIN))
        {
            return buildCall("barley", "from_binary", new ArrayList<>(List.of(primary())));
        }

        if (match(TokenType.PACK)) {
            return new PackAST(consume(TokenType.VAR, "expected var name after 'pack'").getText(), line(), currentLine());
        }

        if (match(TokenType.UNPACK)) {
            return new UnPackAST(expression(), line(), currentLine());
        }

        if (match(TokenType.POINT)) {
            if (match(TokenType.LBRACKET)) {
                return map();
            }
            return new PointerAST(call());
        }

        if (match(TokenType.UNPOINT)) {
            return new UnPointAST(call(), line(), currentLine());
        }

        if (unaryOperators.containsKey(get(0).getType())) {
            TokenType type = get(0).getType();
            match(get(0).getType());
            AST right = call();
            ArrayList<AST> args = new ArrayList<>();
            args.add(right);
            return new CallAST(new ConstantAST(new BarleyFunction(unaryOperators.get(type))), args, line(), currentLine());
        }

        return call();
    }

    private AST map() {
        HashMap<AST, AST> map = new HashMap<>();
        while (!match(TokenType.RBRACKET)) {

            AST key = expression();
            consume(TokenType.STABBER, "expected '->' after key in map expression");
            map.put(key, expression());
            match(TokenType.COMMA);
        }
        return new MapAST(map);
    }

    private AST call() {
        if (lookMatch(0, TokenType.ATOM) && lookMatch(1, TokenType.LPAREN)) return expandCall();
        AST result = remote();

        while (true) {
            if (lookMatch(0, TokenType.LPAREN)) {
                ArrayList<AST> args = arguments();
                result = new CallAST(result, args, line(), currentLine());
                continue;
            }
            break;
        }

        return result;
    }

    private AST remote() {
        AST result = index();

        while (true) {
            if (match(TokenType.COLON)) {
                result = new RemoteAST(result, index(), line(), currentLine());
                continue;
            }
            break;
        }

        return result;
    }

    private AST index() {
        AST result = primary();

        while (true) {
            if (match(TokenType.LBRACKET)) {
                ArrayList<AST> args = new ArrayList<>();
                args.add(result);
                args.add(expression());
                if (args.get(1) instanceof StringAST) {
                    warnParser("Index expression can't process string indexes");
                }
                consume(TokenType.RBRACKET, "expected ']' after expression in index expression");
                result = buildCall("lists", "nth", args);
                continue;
            }
            break;
        }

        return result;
    }

    private AST list() {
        LinkedList<AST> array = new LinkedList<>();
        while (!(match(TokenType.RBRACKET))) {
            array.add(expression());
            match(TokenType.COMMA);
        }
        return new ListAST(array);
    }

    private AST primary() {
        final Token current = get(0);
        if (match(TokenType.NUMBER)) {
            try {
                return new ConstantAST(new BarleyNumber(Double.parseDouble(current.getText())));
            } catch (NumberFormatException ex) {
                return new ConstantAST(new BarleyNumber(Integer.parseInt(current.getText(), 32)));
            }
        }
        if (match(TokenType.STRING)) {
            return new StringAST(current.getText(), line(), currentLine(), 0);
        }
        if (match(TokenType.LPAREN)) {
            AST result = expression();
            match(TokenType.RPAREN);
            return result;
        }
        if (match(TokenType.VAR)) {
            if (!emulator.isExists(current.getText())) {
                if (inPatterns);
                else
                    warnBadVar("Variable '" + current.getText() + "' is not defined in this scope");
            }
            return new ExtractBindAST(current.getText(), line(), currentLine());
        }
        if (match(TokenType.ATOM)) {
            return new ConstantAST(new BarleyAtom(current.getText()));
        }

        if (match(TokenType.LBRACKET)) {
            return list();
        }

        if (match(TokenType.CASE)) {
            return match();
        }

        if (match(TokenType.DEF)) {
            return lambda();
        }

        if (match(TokenType.LTLT)) {
            AST toBinary = expression();
            match(TokenType.GTGT);
            ArrayList<AST> list = new ArrayList<>();
            list.add(toBinary);
            return buildCall("barley", "binary", list);
        }

        if (match(TokenType.EXTERN)) {
            return externCall();
        }


        if (match(TokenType.RECIEVE)) return receive();
        throw new BarleyException("BadCompiler", "Unknown term\n    where term:\n        " + current + "\n    when current line:\n      " + currentLine());
    }

    private void warnBadVar(String s) {
        badVars.add(s);
    }

    private void warnParser(String s) {

    }

    private AST externCall() {
        String name = consume(TokenType.ATOM, "expected extern function name after 'extern'").getText();
        if (!Externals.containsKey(name)) throw new BarleyException("BadExtern", "unknown extern function '" + name + "' at line " + line());
        ArrayList<AST> args = arguments();
        return new CallAST(new ConstantAST(new BarleyFunction(Externals.get(name))), args, line(), currentLine());
    }

    private AST lambda() {
        ArrayList<Clause> clauses = new ArrayList<>();
        while (!(match(TokenType.END))) {
            match(TokenType.DEF);
            Clause clause = clause();
            consume(TokenType.STABBER, "error at lambda declaration at line " + line());
            clause.setResult(block());
            match(TokenType.DOT);
            clauses.add(clause);
        }
        return new ClosureAST(new UserFunction(clauses));
    }

    private CaseAST match() {
        final AST expression = expression();
        consume(TokenType.STABBER, "expected '->' after '" + expression + "' in case-of expr at line " + line());
        final List<CaseAST.Pattern> patterns = new ArrayList<>();
        while (!match(TokenType.END)) {
            consume(TokenType.OF, "expected 'of' in case-of clauses at line " + line());
            CaseAST.Pattern pattern = null;
            final Token current = get(0);
            if (match(TokenType.NUMBER)) {
                // case 0.5:
                pattern = new CaseAST.ConstantPattern(new
                        BarleyNumber(Double.parseDouble(current.getText()))
                );
            } else if (match(TokenType.STRING)) {
                // case "text":
                pattern = new CaseAST.ConstantPattern(
                        new BarleyString(current.getText())
                );
            } else if (match(TokenType.VAR)) {
                // case value:
                pattern = new CaseAST.VariablePattern(current.getText());
            } else if (match(TokenType.ATOM)) {
                pattern = new CaseAST.ConstantPattern(new BarleyAtom(current.getText()));
            } else if (match(TokenType.LBRACKET)) {
                // case [x :: xs]:
                final CaseAST.ListPattern listPattern = new CaseAST.ListPattern();
                while (!match(TokenType.RBRACKET)) {
                    listPattern.add(consume(TokenType.VAR, "expected var name in list pattern at line " + line()).getText());
                    match(TokenType.COMMA);
                    match(TokenType.CC);
                }
                pattern = listPattern;
            } else if (match(TokenType.LPAREN)) {
                // case (1, 2):
                final CaseAST.TuplePattern tuplePattern = new CaseAST.TuplePattern();
                while (!match(TokenType.RPAREN)) {
                    if ("_".equals(get(0).getText())) {
                        tuplePattern.addAny();
                        consume(TokenType.VAR, "expected var name in tuple pattern at line " + line());
                    } else {
                        tuplePattern.add(expression());
                    }
                    match(TokenType.COMMA);
                }
                pattern = tuplePattern;
            }

            if (pattern == null) {
                throw new BarleyException("BadCompiler", "wrong pattern in case-of expression at line " + line());
            }
            if (match(TokenType.WHEN)) {
                // case e when e > 0:
                pattern.optCondition = expression();
            }

            consume(TokenType.COLON, "expected ':' after clause");
            pattern.result = block();
            match(TokenType.DOT);
            patterns.add(pattern);
        }
        ;

        return new CaseAST(expression, patterns);
    }

    private AST expandCall() {
        AST target = remote();
        return new CallAST(new RemoteAST(new ConstantAST(new BarleyAtom(module)), target, line(), currentLine()), arguments(), line(), currentLine());
    }

    private Clause clause() {
        ArrayList<AST> args = arguments();
        AST guard = null;
        if (match(TokenType.WHEN)) guard = expression();
        return new Clause(args, guard, null);
    }

    private int line() {
        return get(0).getLine();
    }

    private ArrayList<AST> arguments() {
        consume(TokenType.LPAREN, "error at '(' at line " + line());
        ArrayList<AST> args = new ArrayList<>();
        while (!(match(TokenType.RPAREN))) {
            args.add(expression());
            match(TokenType.COMMA);
        }
        emitArgs(args);
        return args;
    }

    private void emitArgs(ArrayList<AST> args) {
        for (AST ast : args) {
            processEmulation(pattern(ast));
        }
    }

    private boolean lookMatch(int pos, TokenType type) {
        return get(pos).getType() == type;
    }

    private Token consume(TokenType type, String text) {
        final Token current = get(0);
        if (type != current.getType()) throw new BarleyException("BadCompiler", text + "\n    at line " + current.getLine() + "\n      when current line:\n            " + currentLine());
        pos++;
        return current;
    }

    private boolean match(TokenType type) {
        final Token current = get(0);
        if (type != current.getType()) return false;
        pos++;
        return true;
    }

    private Token get(int relativePosition) {
        final int position = pos + relativePosition;
        if (position >= size) return EOF;
        return tokens.get(position);
    }

    private AST buildCall(String module, String method, ArrayList<AST> args) {
        return new CallAST(new RemoteAST(new ConstantAST(new BarleyAtom(module)), new ConstantAST(new BarleyAtom(method)), line(), currentLine()), args, line(), currentLine());
    }

    private void emitVariable(String name) {
        emulator.set(name, new VariableInfo(new BarleyNull(), 0));
    }

    private Pattern pattern(AST ast) {
        if (ast instanceof ExtractBindAST) {
            return new VariablePattern(ast.toString());
        } else if (ast instanceof ConstantAST) {
            return new ConstantPattern(ast.execute());
        } else if (ast instanceof BindAST) {
            return new ConstantPattern(ast.execute());
        } else if (ast instanceof ListAST) {
            return new ListPattern(((ListAST) ast).getArray());
        } else if (ast instanceof ConsAST) {
            ConsAST cons = (ConsAST) ast;
            return new ConsPattern(cons.getLeft().toString(), cons.getRight().toString());
        };
        return null;
    }

    private LinkedList<Pattern> pattern(ListPattern pattern) {
        LinkedList<AST> asts = pattern.getArr();
        LinkedList<Pattern> patterns = new LinkedList<>();
        for (AST ast : asts) {
            patterns.add(pattern(ast));
        }
        return patterns;
    }
}
