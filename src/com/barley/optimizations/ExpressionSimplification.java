package com.barley.optimizations;

import com.barley.ast.*;
import com.barley.runtime.BarleyNumber;
import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;

import java.util.LinkedList;

public class ExpressionSimplification implements Optimization {

    private int count;

    @Override
    public String summary() {
        return "Performed " + count + " expression simplifications";
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public AST optimize(BinaryAST ast) {
        boolean expr1Zero = isIntegerValue(ast.expr1, 0);
        if (expr1Zero || isIntegerValue(ast.expr2, 0)) {
            switch(ast.op) {
                case '+':
                    count++;
                    return expr1Zero ? ast.expr2 : ast.expr1;
                case '-':
                    count++;
                    if (expr1Zero)
                        return new UnaryAST(ast.expr2, '-');
                    return ast.expr1;
                case '*':
                    count++;
                    return new ConstantAST(new BarleyNumber(0));
                case '/':
                    if (expr1Zero) {
                        count++;
                        return new ConstantAST(new BarleyNumber(0));
                    }
                    break;
            }

            boolean exprIsOne = isIntegerValue(ast.expr1, 1);
            if (exprIsOne || isIntegerValue(ast.expr2, 1)) {
                switch (ast.op) {
                    case '*':
                        count++;
                        return exprIsOne ? ast.expr2 : ast.expr1;
                    case '/':
                        if (!exprIsOne) {
                            count++;
                            return ast.expr1;
                        }
                        break;
                }
            }
        }

        return ast;
    }

    public static boolean isIntegerValue(AST node, int valueToCheck) {
        if (!(node instanceof ConstantAST)) return false;
        final BarleyValue value = ((ConstantAST) node).constant;
        if (!(value instanceof BarleyNumber)) return false;

        final int number = value.asInteger().intValue();
        return number == valueToCheck;
    }

    @Override
    public AST optimize(BindAST ast) {
        ast.visit(this);
        return ast;
    }

    @Override
    public AST optimize(BlockAST ast) {
        ast.visit(this);
        count++;
        return ast;
    }

    @Override
    public AST optimize(CallAST ast) {
        ast.visit(this);
        return ast;
    }

    @Override
    public AST optimize(CaseAST ast) {
        return ast;
    }

    @Override
    public AST optimize(CompileAST ast) {
        return ast;
    }

    @Override
    public AST optimize(ConsAST ast) {
        ast.visit(this);
        return ast;
    }

    @Override
    public AST optimize(ConstantAST ast) {
        return ast;
    }

    @Override
    public AST optimize(ExtractBindAST ast) {
        return ast;
    }

    @Override
    public AST optimize(GeneratorAST ast) {
        ast.visit(this);
        return ast;
    }

    @Override
    public AST optimize(JavaFunctionAST ast) {
        return ast;
    }

    @Override
    public AST optimize(ListAST ast) {
        LinkedList<AST> result = new LinkedList<>();
        for (AST node : ast.getArray()) {
            result.add(optimize(node));
        }
        return ast;
    }

    @Override
    public AST optimize(MethodAST ast) {
        ast.visit(this);
        return ast;
    }

    @Override
    public AST optimize(ProcessCallAST ast) {
        ast.expr.visit(this);
        return ast;
    }

    @Override
    public AST optimize(RemoteAST ast) {
        return ast;
    }

    @Override
    public AST optimize(TernaryAST ast) {
        ast.visit(this);
        return ast;
    }

    @Override
    public AST optimize(RecieveAST ast) {
        return ast;
    }

    @Override
    public AST optimize(UnaryAST ast) {
        ast.visit(this);
        return ast;
    }


    @Override
    public AST optimize(AST ast) {
        if (ast instanceof BinaryAST) {
            return optimize((BinaryAST) ast);
        } else if (ast instanceof BindAST) {
            return optimize((BindAST) ast);
        } else if (ast instanceof CallAST) {
            return optimize((CallAST) ast);
        } else if (ast instanceof CaseAST) {
            return optimize((CaseAST) ast);
        } else if (ast instanceof CompileAST) {
            return optimize((CompileAST) ast);
        } else if (ast instanceof ConsAST) {
            return optimize((ConsAST) ast);
        } else if (ast instanceof ConstantAST) {
            return optimize((ConstantAST) ast);
        } else if (ast instanceof ExtractBindAST) {
            return optimize((ExtractBindAST) ast);
        } else if (ast instanceof GeneratorAST) {
            return optimize((GeneratorAST) ast);
        } else if (ast instanceof ListAST) {
            return optimize((ListAST) ast);
        } else if (ast instanceof MethodAST) {
            return optimize((MethodAST) ast);
        } else if (ast instanceof ProcessCallAST) {
            return optimize((ProcessCallAST) ast);
        } else if (ast instanceof RecieveAST) {
            return optimize((RecieveAST) ast);
        } else if (ast instanceof RemoteAST) {
            return optimize((RemoteAST) ast);
        } else if (ast instanceof TernaryAST) {
            return optimize((TernaryAST) ast);
        } else if (ast instanceof UnaryAST) {
            return optimize((UnaryAST) ast);
        } else if (ast instanceof BlockAST) {
            return optimize((BlockAST) ast);
        }
        return ast;
    }
}
