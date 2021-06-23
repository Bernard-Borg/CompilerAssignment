package parser;

import visitors.ASTVisitor;

public class ASTExpression implements ASTNode {
    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
