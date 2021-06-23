package parser;

import visitors.ASTVisitor;

public class ASTAssignment extends ASTStatement {
    public ASTIdentifier identifier;
    public ASTExpression expression;

    public ASTAssignment(ASTIdentifier identifier, ASTExpression expression) {
        this.identifier = identifier;
        this.expression = expression;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
