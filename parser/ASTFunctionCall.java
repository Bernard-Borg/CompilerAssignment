package parser;

import visitors.ASTVisitor;

import java.util.ArrayList;
import java.util.List;

//ASTNode for function calls
public class ASTFunctionCall extends ASTExpression {
    public ASTIdentifier identifier;
    public List<ASTExpression> parameters;

    /**
     * @param identifier stores the function identifier
     * @param parameters stores the list of actual parameters
     */
    public ASTFunctionCall(ASTIdentifier identifier, List<ASTExpression> parameters) {
        this.identifier = identifier;

        //Avoid null pointer exceptions
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
