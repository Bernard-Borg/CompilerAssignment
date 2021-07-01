package parser;

import visitors.ASTVisitor;

public class ASTStructFunctionSelector extends ASTIdentifier {
    public ASTFunctionCall functionCall;

    public ASTStructFunctionSelector(ASTIdentifier structIdentifier, ASTFunctionCall functionCall) {
        super(structIdentifier);
        this.functionCall = functionCall;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
