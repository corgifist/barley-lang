package com.barley.runtime;

import com.barley.ast.BindAST;
import com.barley.ast.ConstantAST;
import com.barley.ast.ExtractBindAST;
import com.barley.ast.ListAST;
import com.barley.patterns.ConstantPattern;
import com.barley.patterns.ListPattern;
import com.barley.patterns.Pattern;
import com.barley.patterns.VariablePattern;
import com.barley.utils.AST;
import com.barley.utils.BarleyException;
import com.barley.utils.Clause;
import com.barley.utils.Function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class UserFunction implements Function {

    private ArrayList<Clause> clauses;

    public UserFunction(ArrayList<Clause> clauses) {
        this.clauses = clauses;
    }

    @Override
    public BarleyValue execute(BarleyValue... args) {
        boolean found = false;;
        AST toExecute = null;
        ArrayList<String> toDelete = new ArrayList<>();
        for (int i = 0; i < clauses.size(); i++) {
            if (found) break;
            Clause clause = clauses.get(i);
            ArrayList<Pattern> patterns = patterns(clause.getArgs());
            if (patterns.size() != args.length) continue;
            for (int k = 0; k < patterns.size(); k++) {
                Pattern pattern = patterns.get(k);
                BarleyValue arg = args[k];
                System.out.println(pattern);
                System.out.println(arg);
                if (pattern instanceof VariablePattern) {
                    VariablePattern p = (VariablePattern) pattern;
                    Table.set(p.getVariable(), arg);
                    toDelete.add(p.getVariable());
                } else if (pattern instanceof ConstantPattern) {
                    System.out.println("in constant");
                    ConstantPattern p = (ConstantPattern) pattern;
                    if (arg.equals(p.getConstant())) continue;
                    else break;
                }
            }
            toExecute = clause.getResult();
            found = true;
        }
        BarleyValue result = toExecute.execute();
        if (result == null) throw new BarleyException("FunctionClause", "can't find function clause for args " + Arrays.asList(args));
        for (String var : toDelete) {
            Table.remove(var);
        }
        return result;
    }

    private ArrayList<Pattern> patterns(ArrayList<AST> asts) {
        ArrayList<Pattern> result = new ArrayList<>();
        for (AST node : asts) {
            result.add(pattern(node));
        }
        return result;
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
        } else throw new BarleyException("BadMatch", "invalid pattern in function");
    }

    private LinkedList<Pattern> pattern(ListPattern pattern) {
        LinkedList<AST> asts = pattern.getArr();
        LinkedList<Pattern> patterns = new LinkedList<>();
        for (AST ast : asts) {
            patterns.add(pattern(ast));
        }

        return patterns;
    }

    public ArrayList<Clause> getClauses() {
        return clauses;
    }

    @Override
    public String toString() {
        return "UserFunction{" +
                "clauses=" + clauses +
                '}';
    }
}
