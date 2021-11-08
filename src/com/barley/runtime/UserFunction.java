package com.barley.runtime;

import com.barley.ast.*;
import com.barley.optimizations.Optimization;
import com.barley.patterns.*;
import com.barley.utils.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UserFunction implements Function, Serializable {

    public ArrayList<Clause> clauses;

    public UserFunction(ArrayList<Clause> clauses) {
        this.clauses = clauses;
    }

    @Override
    public BarleyValue execute(BarleyValue... args) {
        // System.out.println(List.of(args));
        Table.push();
        try {
            boolean br = false;
            AST toExecute = null;
            ArrayList<String> toDelete = new ArrayList<>();
            for (int i = 0; i < clauses.size(); i++) {
                Clause clause = clauses.get(i);
                ArrayList<Pattern> patterns = patterns(clause.getArgs());
                if (patterns.isEmpty() && args.length == 0) {
                    if (clause.getGuard() != null) {
                        if ((clause.getGuard().execute()).toString().equals("true")) ;
                        else continue;
                    }
                    toExecute = clause.getResult();
                    break;
                }
                if (patterns.size() != args.length) continue;
                for (int k = 0; k < patterns.size(); k++) {
                    Pattern pattern = patterns.get(k);
                    BarleyValue arg = args[k];
                    if (pattern instanceof VariablePattern) {
                        VariablePattern p = (VariablePattern) pattern;
                        Table.define(p.getVariable(), arg);
                        toDelete.add(p.getVariable());
                    } else if (pattern instanceof ConstantPattern) {
                        ConstantPattern p = (ConstantPattern) pattern;
                        boolean isEquals = p.getConstant().equals(arg);
                        if (isEquals) ;
                        else {
                            br = true;
                            break;
                        }
                    } else if (pattern instanceof ListPattern) {
                        if (!(arg instanceof BarleyList)) {
                            br = true;
                            break;
                        }
                        ListPattern p = (ListPattern) pattern;
                        br = !(processList(p, arg, toDelete));
                    } else if (pattern instanceof ConsPattern) {
                        ConsPattern p = (ConsPattern) pattern;
                        if (!(arg instanceof BarleyList)) {
                            br = true;
                            break;
                        }
                        Table.set(p.getLeft(), head((BarleyList) arg));
                        Table.set(p.getRight(), tail((BarleyList) arg));
                    }
                }
                if (clause.getGuard() != null) {
                    if ((clause.getGuard().execute()).toString().equals("true")) ;
                    else br = true;
                }

                if (br) {
                    br = false;
                    continue;
                }
                toExecute = clause.getResult();
                break;
            }
            if (toExecute == null) throw new BarleyException("FunctionClause", "can't find function clause for args " + List.of(args) + " with clauses:\n   " + clauses);
            BarleyValue result = toExecute.execute();
            Table.pop();
            return result;
        } catch (BarleyException ex) {
            CallStack.exit();
            throw ex;
        }
    }

    public void optimize(Optimization opt) {
        ArrayList<Clause> res = new ArrayList<>();
        for (Clause cl : clauses) {
            res.add(cl.optimize(opt));
        }
        clauses = res;
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
                Table.define(c.getVariable(), obj);
                toDelete.add(c.getVariable());
            } else if (p instanceof ConstantPattern) {
                ConstantPattern c = (ConstantPattern) p;
                if (!(c.getConstant().equals(obj))) return false;
            } else if (p instanceof ListPattern) {
                ListPattern c = (ListPattern) p;
                if (processList(c, obj, toDelete)) continue;
                else return false;
            } else if (p instanceof ConsPattern) {
                ConsPattern c = (ConsPattern) p;
                if (!(obj instanceof BarleyList)) return false;
                Table.set(c.getLeft(), head((BarleyList) obj));
                Table.set(c.getRight(), tail((BarleyList) obj));
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
        } else if (ast instanceof ConsAST) {
            ConsAST cons = (ConsAST) ast;
            return new ConsPattern(cons.getLeft().toString(), cons.getRight().toString());
        } else throw new BarleyException("BadMatch", "invalid pattern in function clause");
    }

    private LinkedList<Pattern> pattern(ListPattern pattern) {
        LinkedList<AST> asts = pattern.getArr();
        LinkedList<Pattern> patterns = new LinkedList<>();
        for (AST ast : asts) {
            patterns.add(pattern(ast));
        }

        return patterns;
    }

    private BarleyValue head(BarleyList list) {
        return list.getList().get(0);
    }

    private BarleyValue tail(BarleyList list) {
        List<BarleyValue> arr = list.getList().subList(1, list.getList().size());
        LinkedList<BarleyValue> result = new LinkedList<>();
        for (BarleyValue val : arr) {
            result.add(val);
        }
        return new BarleyList(result);
    }

    public ArrayList<Clause> getClauses() {
        return clauses;
    }

    private int addAtom(String atom) {
        return AtomTable.put(atom);
    }

    @Override
    public String toString() {
        return "#Function" + clauses;
    }
}
