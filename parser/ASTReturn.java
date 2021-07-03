package parser;

import visitors.ASTVisitor;

//ASTNode for return statements
public class ASTReturn extends ASTStatement {
    public ASTExpression expression;

    /**
     * @param expression expression to return
     */
    public ASTReturn(ASTExpression expression) {
        this.expression = expression;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
