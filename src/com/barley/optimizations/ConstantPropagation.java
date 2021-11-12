package com.barley.optimizations;

import com.barley.ast.*;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.Table;
import com.barley.utils.AST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ConstantPropagation implements Optimization {

    private TableEmulator emulator;
    private int count;

    public ConstantPropagation(ArrayList<AST> nodes, VariableGrabber grabber) {
        emulator = new TableEmulator();
        emulator = grabber.emulate(nodes);
        this.count = 0;
    }

    @Override
    public String summary() {
        return "Performed " + count + " constant propagations";
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public AST optimize(BinaryAST ast) {
        AST left = optimize(ast.expr1);
        AST right = optimize(ast.expr2);
        count++;
        if ((left instanceof ConstantAST) && (right instanceof ConstantAST)) {
            return new ConstantAST(ast.execute());
        } else return ast;
    }

    @Override
    public AST optimize(BindAST ast) {
        optimize(ast.right);
        ast.visit(this);
        count++;
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
        return ast;
    }

    @Override
    public AST optimize(CompileAST ast) {
        return ast;
    }

    @Override
    public AST optimize(ConsAST ast) {
        ast.visit(this);
        count++;
        return ast;
    }

    @Override
    public AST optimize(ConstantAST ast) {
        return ast;
    }

    @Override
    public AST optimize(ExtractBindAST ast) {
        count++;
        HashMap<String, VariableInfo> info = new HashMap<>(emulator.variables());
        if (info.containsKey(ast.toString())) {
            count++;
            if (info.get(ast.toString()).modifications != 0) {
                return ast;
            }
            count++;
            return new ConstantAST(info.get(ast.toString()).value);
        }
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
        count++;
        LinkedList<AST> result = new LinkedList<>();
        for (AST node : ast.getArray()) {
            result.add(optimize(node));
        }
        return new ListAST(result);
    }

    @Override
    public AST optimize(MethodAST ast) {
        ast.visit(this);
        emulator.pop();
        return ast;
    }

    @Override
    public AST optimize(ProcessCallAST ast) {
        optimize(ast.expr);
        return ast;
    }

    @Override
    public AST optimize(RemoteAST ast) {
        return ast;
    }

    @Override
    public AST optimize(TernaryAST ast) {
        optimize(ast.term);
        optimize(ast.right);
        optimize(ast.left);
        return ast;
    }

    @Override
    public AST optimize(RecieveAST ast) {
        return ast;
    }

    @Override
    public AST optimize(UnaryAST ast) {
        optimize(ast.expr1);
        return ast;
    }

    @Override
    public AST optimize(AST ast) {
        Map<String, VariableInfo> vars = emulator.variables();
        Map<String, BarleyValue> candidates = new HashMap<>();
        for (Map.Entry<String, VariableInfo> e : vars.entrySet()) {
            final VariableInfo info = e.getValue();
            if (info.modifications != 0) continue;
            if (info.value == null) continue;
            candidates.put(e.getKey(), info.value);
        }
        for (Map.Entry<String, BarleyValue> e : candidates.entrySet()) {
            Table.set(e.getKey(), e.getValue());
        }

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
