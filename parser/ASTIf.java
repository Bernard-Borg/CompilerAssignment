package parser;

import visitors.ASTVisitor;

//ASTNode for if statements
public class ASTIf extends ASTStatement {
    public ASTExpression conditionExpression;
    public ASTBlock trueBlock, falseBlock;

    /**
     * @param conditionExpression stores the bool if condition
     * @param trueBlock stores the block which is run when the condition is true
     * @param falseBlock stores the block which is run when the condition is false
     */
    public ASTIf(ASTExpression conditionExpression, ASTBlock trueBlock, ASTBlock falseBlock) {
        this.conditionExpression = conditionExpression;
        this.trueBlock = trueBlock;
        this.falseBlock = falseBlock;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
