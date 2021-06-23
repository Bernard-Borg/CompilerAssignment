package parser;

import visitors.ASTVisitor;

public class ASTIf extends ASTStatement {
    public ASTExpression conditionExpression;
    public ASTBlock trueBlock, falseBlock;

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
