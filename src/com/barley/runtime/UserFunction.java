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
import java.util.List;

public class UserFunction implements Function {

    private ArrayList<Clause> clauses;

    public UserFunction(ArrayList<Clause> clauses) {
        this.clauses = clauses;
    }

    @Override
    public BarleyValue execute(BarleyValue... args) {
        boolean br = false;
        AST toExecute = null;
        ArrayList<String> toDelete = new ArrayList<>();
        for (int i = 0; i < clauses.size(); i++) {
            Clause clause = clauses.get(i);
            ArrayList<Pattern> patterns = patterns(clause.getArgs());
            if (patterns.size() != args.length) continue;
            for (int k = 0; k < patterns.size(); k++) {
                Pattern pattern = patterns.get(k);
                BarleyValue arg = args[k];
                if (pattern instanceof VariablePattern) {
                    VariablePattern p = (VariablePattern) pattern;
                    Table.set(p.getVariable(), arg);
                    toDelete.add(p.getVariable());
                } else if (pattern instanceof ConstantPattern) {
                    ConstantPattern p = (ConstantPattern) pattern;
                    boolean isEquals = p.getConstant().equals(arg);
                    if (isEquals);
                    else br = true;
                } else if (pattern instanceof ListPattern) {
                    ListPattern p = (ListPattern) pattern;
                    br = !(processList(p, arg, toDelete));
                }
            }
            if (clause.getGuard() != null) {
                if ((clause.getGuard().execute()).toString() == "true");
                else continue;
            }

            if (br) {
                br = false;
                continue;
            }
            toExecute = clause.getResult();
            break;
        }
        BarleyValue result = toExecute.execute();
        if (result == null) throw new BarleyException("FunctionClause", "can't find function clause for args " + Arrays.asList(args));
        for (String var : toDelete) {
            Table.remove(var);
        }
        return result;
    }
    private boolean processList(ListPattern pattern, BarleyValue val, ArrayList<String> toDelete) {
        if (!((val instanceof BarleyList))) throw new BarleyException("BadArg", "expected list in list pattern");
        BarleyList list = (BarleyList) val;
        if (list.getList().size() != pattern.getArr().size())
            return false;
        for (int i = 0; i < pattern.getArr().size(); i++) {
            Pattern p = pattern(pattern.getArr().get(i));
            BarleyValue obj = list.getList().get(i);
            if (p instanceof VariablePattern) {
                VariablePattern c = (VariablePattern) p;
                Table.set(c.getVariable(), obj);
                toDelete.add(c.getVariable());
            } else if (p instanceof ConstantPattern) {
                ConstantPattern c = (ConstantPattern) p;
                if (!(c.getConstant().equals(obj))) return false;
            } else if (p instanceof ListPattern) {
                ListPattern c = (ListPattern) p;
                processList(c, obj, toDelete);
            }
        }
        return true;
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

    private int addAtom(String atom) {
        return AtomTable.put(atom);
    }

    @Override
    public String toString() {
        return "UserFunction{" +
                "clauses=" + clauses +
                '}';
    }
}
