package com.barley.ast;

import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;

import java.io.Serializable;
import java.util.ArrayList;

public class BlockAST implements AST, Serializable {

    private ArrayList<AST> block;

    public BlockAST(ArrayList<AST> block) {
        this.block = block;
    }

    @Override
    public BarleyValue execute() {
        int size = block.size();
        BarleyValue last = null;
        for (int i = 0; i < size; i++) {
            if (i + 1 == size) last = block.get(i).execute();
            else block.get(i).execute();
        }
        return last;
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
