package parser;

import visitors.ASTVisitor;

//ASTNode for struct function calls (like x.hello())
//Extends from identifier not expression to allow use of parseStructSelector() method
public class ASTStructFunctionSelector extends ASTIdentifier {
    public ASTFunctionCall functionCall;

    /**
     * @param structIdentifier name of struct
     * @param functionCall identifier of struct function
     */
    public ASTStructFunctionSelector(ASTIdentifier structIdentifier, ASTFunctionCall functionCall) {
        super(structIdentifier);
        this.functionCall = functionCall;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}