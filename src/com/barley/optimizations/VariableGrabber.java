package com.barley.optimizations;

import com.barley.ast.*;
import com.barley.utils.AST;
import com.barley.utils.Clause;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VariableGrabber {

    private HashMap<String, VariableInfo> info;
    private HashMap<String, Integer> mods;

    public Map<String, VariableInfo> getInfo(ArrayList<AST> nodes) {
        info = new HashMap<>();
        mods = new HashMap<>();
        for (AST node : nodes) {
            cast(node);
        }
        return info;
    }

    private AST optimize(BindAST ast) {
        ast.emulate(info, mods);
        return ast;
    }

    private AST optimize(BlockAST ast) {
        for (AST node : ast.block) {
            cast(node);
        }
        return ast;
    }

    private AST optimize(MethodAST ast) {
        ArrayList<Clause> clauses = ast.method.clauses;
        for (Clause cl : clauses) {
            cast(cl.getResult());
        }
        return ast;
    }

    private AST optimize(AST ast) {
        return ast;
    }

    public AST cast(AST ast) {
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
