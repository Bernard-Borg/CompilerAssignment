package parser;

import lexer.Type;
import visitors.ASTVisitor;

import java.util.ArrayList;
import java.util.List;

//ASTNode for function declarations
public class ASTFunctionDeclaration extends ASTStatement {
    public Type returnType;
    public ASTIdentifier functionName;
    public List<ASTParameter> parameterList;
    public ASTBlock functionBlock;

    /**
     * @param returnType stores the function's return type
     * @param functionName stores the function's name
     * @param parameterList stores the formal parameter list
     * @param functionBlock stores the function's main block
     */
    public ASTFunctionDeclaration(Type returnType, ASTIdentifier functionName, List<ASTParameter> parameterList, ASTBlock functionBlock) {
        this.returnType = returnType;
        this.functionName = functionName;

        //Avoid null pointer exceptions
        if (parameterList == null) {
            this.parameterList = new ArrayList<>();
        } else {
            this.parameterList = parameterList;
        }

        this.functionBlock = functionBlock;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
