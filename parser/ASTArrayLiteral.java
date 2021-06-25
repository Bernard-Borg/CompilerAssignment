package parser;

import visitors.ASTVisitor;

import java.util.List;

public class ASTArrayLiteral extends ASTExpression {
    public List<ASTExpression> arrayMembers;

    public ASTArrayLiteral (List<ASTExpression> arrayMembers) {
        this.arrayMembers = arrayMembers;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
