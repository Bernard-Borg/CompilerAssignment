package parser;

import lexer.Word;
import visitors.ASTVisitor;

public class ASTIdentifier extends ASTExpression {
    public String identifier;

    public ASTIdentifier(Word lookaheadTemp) {
        this.identifier = lookaheadTemp.lexeme;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
