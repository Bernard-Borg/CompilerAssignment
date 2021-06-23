package parser;

import visitors.ASTVisitor;

public class ASTPrint extends ASTStatement {
    public ASTExpression expression;

    public ASTPrint(ASTExpression expression) {
        this.expression = expression;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
