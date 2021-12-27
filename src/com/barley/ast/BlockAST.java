package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.Table;
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
        Table.push();
        for (AST ast : block) {
            last = ast.execute();
        }
        Table.pop();
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

    public boolean add(AST ast) {
        return block.add(ast);
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
