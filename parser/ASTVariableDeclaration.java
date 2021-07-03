package parser;

import lexer.Type;
import visitors.ASTVisitor;

//ASTNode for variable declarations
public class ASTVariableDeclaration extends ASTStatement {
    public ASTIdentifier identifier;
    public Type type;
    public ASTExpression expression;

    /**
     * @param identifier identifier of variable to be declared
     * @param type type of variable to be declared
     * @param expression expression for variable to be initialised (can be null in the case of no initial value)
     */
    public ASTVariableDeclaration(ASTIdentifier identifier, Type type, ASTExpression expression) {
        this.identifier = identifier;
        this.type = type;
        this.expression = expression;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
