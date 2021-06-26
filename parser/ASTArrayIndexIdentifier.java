package parser;

import visitors.ASTVisitor;

public class ASTArrayIndexIdentifier extends ASTIdentifier {
    public ASTExpression index;

    public ASTArrayIndexIdentifier(ASTIdentifier identifier, ASTExpression index) {
        super(identifier);
        this.index = index;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
