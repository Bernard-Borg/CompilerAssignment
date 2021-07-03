package parser;

import lexer.Word;
import visitors.ASTVisitor;

//ASTNode for identifiers
public class ASTIdentifier extends ASTExpression {
    public String identifier;

    /**
     * Used in the constructor of ArrayIndexIdentifier and StructVariable/FunctionIdentifiers to set the array/struct identifier
     * @param identifier another identifier
     */
    public ASTIdentifier(ASTIdentifier identifier) {
        this.identifier = identifier.identifier;
    }

    /**
     * @param lookaheadTemp parser lookahead containing the identifier as a word
     */
    public ASTIdentifier(Word lookaheadTemp) {
        this.identifier = lookaheadTemp.lexeme;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
