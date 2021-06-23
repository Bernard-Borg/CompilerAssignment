package parser;

import lexer.Type;
import visitors.ASTVisitor;

public class ASTVariableDeclaration extends ASTStatement {
    public ASTIdentifier identifier;
    public Type type;
    public ASTExpression expression;

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
