package parser;

import visitors.ASTVisitor;

import java.util.List;

//ASTNode for array literals (in the form { 1, 2, 3, 4 })
//Note that empty literals ({}) are not allowed
public class ASTArrayLiteral extends ASTExpression {
    public List<ASTExpression> arrayMembers;

    /**
     * @param arrayMembers array literal contents
     */
    public ASTArrayLiteral (List<ASTExpression> arrayMembers) {
        this.arrayMembers = arrayMembers;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
