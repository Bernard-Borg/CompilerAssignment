package parser;

import visitors.ASTVisitor;

public class ASTReturn extends ASTStatement {
    public ASTExpression expression;

    public ASTReturn(ASTExpression expression) {
        this.expression = expression;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
