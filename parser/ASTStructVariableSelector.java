package parser;

import visitors.ASTVisitor;

//ASTNode for struct variable identifiers (like vector.x)
public class ASTStructVariableSelector extends ASTIdentifier {
    public ASTIdentifier elementIdentifier;

    /**
     * @param structIdentifier name of struct
     * @param elementIdentifier identifier of struct variable
     */
    public ASTStructVariableSelector(ASTIdentifier structIdentifier, ASTIdentifier elementIdentifier) {
        super(structIdentifier);
        this.elementIdentifier = elementIdentifier;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
