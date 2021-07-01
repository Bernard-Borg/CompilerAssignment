package parser;

import visitors.ASTVisitor;

public class ASTStructVariableSelector extends ASTIdentifier {
    public ASTIdentifier elementIdentifier;

    public ASTStructVariableSelector(ASTIdentifier structIdentifier, ASTIdentifier elementIdentifier) {
        super(structIdentifier);
        this.elementIdentifier = elementIdentifier;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
