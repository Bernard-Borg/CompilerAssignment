package parser;

import visitors.ASTVisitor;

//ASTNode for assignment statements
public class ASTAssignment extends ASTStatement {
    public ASTIdentifier identifier;
    public ASTExpression expression;

    /**
     * @param identifier identifier of the variable being assigned to
     * @param expression expression of the value being assigned
     */
    public ASTAssignment(ASTIdentifier identifier, ASTExpression expression) {
        this.identifier = identifier;
        this.expression = expression;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
