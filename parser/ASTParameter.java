package parser;

import lexer.Type;

//ASTNode for formal parameters
public class ASTParameter {
    public ASTIdentifier identifier;
    public Type type;

    /**
     * @param identifier identifier of the parameter
     * @param type declared parameter type
     */
    public ASTParameter(ASTIdentifier identifier, Type type) {
        this.identifier = identifier;
        this.type = type;
    }
}
