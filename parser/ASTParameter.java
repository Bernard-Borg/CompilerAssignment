package parser;

import lexer.Type;

public class ASTParameter {
    public ASTIdentifier identifier;
    public Type type;

    public ASTParameter(ASTIdentifier identifier, Type type) {
        this.identifier = identifier;
        this.type = type;
    }
}
