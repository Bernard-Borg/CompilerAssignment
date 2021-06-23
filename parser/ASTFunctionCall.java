package parser;

import visitors.ASTVisitor;

import java.util.ArrayList;
import java.util.List;

public class ASTFunctionCall extends ASTExpression {
    public ASTIdentifier identifier;
    public List<ASTExpression> parameters;

    public ASTFunctionCall(ASTIdentifier identifier, List<ASTExpression> parameters) {
        this.identifier = identifier;

        if (parameters == null) {
            this.parameters = new ArrayList<>();
        } else {
            this.parameters = parameters;
        }
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
