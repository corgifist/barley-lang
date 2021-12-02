package com.barley.optimizations;

import com.barley.ast.*;
import com.barley.utils.AST;

public interface Optimization {

    String summary();

    int count();

    AST optimize(BinaryAST ast);

    AST optimize(BindAST ast);

    AST optimize(BlockAST ast);

    AST optimize(CallAST ast);

    AST optimize(CaseAST ast);

    AST optimize(CompileAST ast);

    AST optimize(ConsAST ast);

    AST optimize(ConstantAST ast);

    AST optimize(ExtractBindAST ast);

    AST optimize(GeneratorAST ast);

    AST optimize(JavaFunctionAST ast);

    AST optimize(ListAST ast);

    AST optimize(MethodAST ast);

    AST optimize(ProcessCallAST ast);

    AST optimize(RemoteAST ast);

    AST optimize(TernaryAST ast);

    AST optimize(RecieveAST ast);

    AST optimize(UnaryAST ast);

    AST optimize(AST ast);
}
