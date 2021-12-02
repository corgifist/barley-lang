package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;

import java.io.Serializable;
import java.util.ArrayList;

public class BlockAST implements AST, Serializable {

    public ArrayList<AST> block;

    public BlockAST(ArrayList<AST> block) {
        this.block = block;
    }

    @Override
    public BarleyValue execute() {
        int size = block.size();
        BarleyValue last = null;
        for (int i = 0; i < size; i++) {
            last = block.get(i).execute();
        }
        return last;
    }

    @Override
    public void visit(Optimization optimization) {
        ArrayList<AST> result = new ArrayList<>();
        for (AST node : block) {
            result.add(optimization.optimize(node));
        }
        block = result;
    }

    @Override
    public String toString() {
        String result = "";
        for (AST node : block) {
            result += node + "\n";
        }
        return result;
    }
}
