package com.barley.optimizations;

import com.barley.ast.*;
import com.barley.runtime.BarleyList;
import com.barley.utils.AST;
import com.barley.utils.BarleyException;

import java.util.LinkedList;

public class DeadCodeElimination implements Optimization {

    private int count;

    @Override
    public String summary() {
        return "Performed " + count + " eliminations";
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public AST optimize(BinaryAST ast) {
        ast.expr1.visit(this);
        ast.expr2.visit(this);
        return ast;
    }

    @Override
    public AST optimize(BindAST ast) {
        ast.visit(this);
        return ast;
    }

    @Override
    public AST optimize(BlockAST ast) {
        ast.visit(this);
        return ast;
    }

    @Override
    public AST optimize(CallAST ast) {
        ast.visit(this);
        return ast;
    }

    @Override
    public AST optimize(CaseAST ast) {
        ast.expression.visit(this);
        return ast;
    }

    @Override
    public AST optimize(CompileAST ast) {
        return ast;
    }

    @Override
    public AST optimize(ConsAST ast) {
        ast.visit(this);
        return null;
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
        try {
            if (ast.iterable.execute().toString().equals("[]")) {
                count++;
                return new ConstantAST(new BarleyList());
            }
        } catch (BarleyException ex) {

        }
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
            node.visit(this);
            result.add(optimize(node));
        }
        count++;
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
        try {
            if (ast.term.execute().toString().equals("true")) {
                count++;
                return ast.left;
            }
            return ast.right;
        } catch (BarleyException ex) {

        }
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

        ast.visit(this);
        return ast;
    }
}
