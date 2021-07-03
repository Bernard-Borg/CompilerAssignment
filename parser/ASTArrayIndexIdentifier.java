package parser;

import visitors.ASTVisitor;

//ASTNode for array identifiers with an index/size (for example, let arr[1]: = { 1 } or arr[0] = 3)
public class ASTArrayIndexIdentifier extends ASTIdentifier {
    public ASTExpression index;

    /**
     * @param identifier array identifier
     * @param index array index (or in the case of variable declaration, size)
     */
    public ASTArrayIndexIdentifier(ASTIdentifier identifier, ASTExpression index) {
        super(identifier);
        this.index = index;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
