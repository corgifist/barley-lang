package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyReference;
import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MapAST implements AST, Serializable {

    private HashMap<AST, AST> map;

    public MapAST(HashMap<AST, AST> map) {
        this.map = map;
    }

    @Override
    public BarleyValue execute() {
        HashMap<BarleyValue, BarleyValue> result = new HashMap<>();
        for (Map.Entry<AST, AST> entry : map.entrySet()) {
            result.put(entry.getKey().execute(), entry.getValue().execute());
        }

        return new BarleyReference(result);
    }

    @Override
    public void visit(Optimization optimization) {

    }
}
