package parser;

import lexer.Type;
import visitors.ASTVisitor;

import java.util.ArrayList;
import java.util.List;

public class ASTFunctionDeclaration extends ASTStatement {
    public Type returnType;
    public ASTIdentifier functionName;
    public List<ASTParameter> parameterList;
    public ASTBlock functionBlock;

    public ASTFunctionDeclaration(Type returnType, ASTIdentifier functionName, List<ASTParameter> parameterList, ASTBlock functionBlock) {
        this.returnType = returnType;
        this.functionName = functionName;

        if (parameterList == null) {
            this.parameterList = new ArrayList<>();
        } else {
            this.parameterList = parameterList;
        }

        this.functionBlock = functionBlock;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
