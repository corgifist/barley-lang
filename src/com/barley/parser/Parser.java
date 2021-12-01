package com.barley.parser;

import com.barley.ast.*;
import com.barley.runtime.*;
import com.barley.units.UnitBase;
import com.barley.units.Units;
import com.barley.utils.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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

    public Parser(List<Token> tokens, String filename) {
        this.tokens = tokens;
        size = tokens.size();
        opt = false;
        methods = new HashMap<>();
        module = null;
        doc = null;
        this.source = filename;
    }

    private String currentLine() {
        List<String> lines = List.of(source.split("\n"));
        return lines.get(line() - 1);
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
        while (true) {
            if (lookMatch(0, TokenType.DOT)) break;
            block.add(expression());
            match(TokenType.COMMA);
        }

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
                return new StrictAST();
            }
            match(TokenType.COMMA);
            return new ConstantAST(new BarleyNumber(0));
        } else if (match(TokenType.RECIEVE)) {
            return receive();
        } else if (match(TokenType.GLOBAL)) {
            return global();
        } else throw new BarleyException("BadCompiler", "bad declaration '" + current + "'");
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
        consume(TokenType.STABBER, "error at '" + name + "' declaration");
        if (name.equals("main")) {
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
        return ternary();
    }

    private AST ternary() {
        AST term = or();

        while (true) {
            if (match(TokenType.QUESTION)) {
                AST left = or();
                consume(TokenType.CC, "expected '::' after term in ternary expr at line " + line());
                AST right = or();
                term = new TernaryAST(term, left, right);
            }
            break;
        }

        return term;
    }

    private AST or() {
        AST result = and();

        while (true) {
            if (match(TokenType.OR)) {
                result = new BinaryAST(result, and(), 'o');
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
                result = new BinaryAST(result, generator(), 'a');
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
            consume(TokenType.STABBER, "expected '->' after var name at line " + line());
            AST iterable = assignment();
            return new GeneratorAST(generator, var, iterable);
        }

        return generator;
    }

    private AST assignment() {
        AST result = conditional();

        while (true) {
            if (match(TokenType.EQ)) {
                result = new BindAST(result, conditional());
                continue;
            }
            break;
        }

        return result;
    }

    private AST conditional() {
        AST result = additive();

        if (match(TokenType.LT)) {
            return new BinaryAST(result, additive(), '<');
        }

        if (match(TokenType.GT)) {
            return new BinaryAST(result, additive(), '>');
        }

        if (match(TokenType.LTEQ)) {
            return new BinaryAST(result, additive(), 't');
        }

        if (match(TokenType.GTEQ)) {
            return new BinaryAST(result, additive(), 'g');
        }

        if (match(TokenType.EQEQ)) {
            return new BinaryAST(result, additive(), '=');
        }
        return result;
    }

    private AST additive() {
        AST result = multiplicative();

        while (true) {
            if (match(TokenType.PLUS)) {
                result = new BinaryAST(result, multiplicative(), '+');
                continue;
            }
            if (match(TokenType.MINUS)) {
                result = new BinaryAST(result, multiplicative(), '-');
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
                result = new BinaryAST(result, unary(), '*');
                continue;
            }
            if (match(TokenType.SLASH)) {
                result = new BinaryAST(result, unary(), '/');
                continue;
            }

            if (match(TokenType.BANG)) {
                result = new ProcessCallAST(result, unary());
            }

            if (match(TokenType.BAR)) {
                result = new ConsAST(result, unary());
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
            return new UnaryAST(call(), '-');
        }

        if (match(TokenType.NOT)) {
            return new UnaryAST(call(), 'n');
        }

        if (match(TokenType.UNBIN))
        {
            return buildCall("barley", "from_binary", new ArrayList<>(List.of(primary())));
        }

        if (match(TokenType.PACK)) {
            return new PackAST(consume(TokenType.VAR, "expected var name after 'pack'").getText());
        }

        if (match(TokenType.UNPACK)) {
            return new UnPackAST(expression());
        }
        return call();
    }

    private AST call() {
        if (lookMatch(0, TokenType.ATOM) && lookMatch(1, TokenType.LPAREN)) return expandCall();
        AST result = remote();

        while (true) {
            if (lookMatch(0, TokenType.LPAREN)) {
                ArrayList<AST> args = arguments();
                result = new CallAST(result, args);
            }
            break;
        }

        return result;
    }

    private AST remote() {
        AST result = primary();

        while (true) {
            if (match(TokenType.COLON)) {
                result = new RemoteAST(result, primary());
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
            return new ConstantAST(new BarleyNumber(Double.parseDouble(current.getText())));
        }
        if (match(TokenType.STRING)) {
            return new ConstantAST(new BarleyString(current.getText()));
        }
        if (match(TokenType.LPAREN)) {
            AST result = expression();
            match(TokenType.RPAREN);
            return result;
        }
        if (match(TokenType.VAR)) {
            return new ExtractBindAST(current.getText());
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


        if (match(TokenType.RECIEVE)) return receive();
        throw new BarleyException("BadCompiler", "Unknown term\n    where term:\n        " + current + "\n      when current line:\n      " + currentLine());
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
        return new ConstantAST(new BarleyFunction(new UserFunction(clauses)));
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
        return new CallAST(new RemoteAST(new ConstantAST(new BarleyAtom(module)), target), arguments());
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
        return args;
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
        return new CallAST(new RemoteAST(new ConstantAST(new BarleyAtom(module)), new ConstantAST(new BarleyAtom(method))), args);
    }
}
