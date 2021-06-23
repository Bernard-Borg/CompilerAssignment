package parser;

import visitors.ASTVisitor;

public class ASTWhile extends ASTStatement {
    public ASTExpression conditionExpression;
    public ASTBlock loopedBlock;

    public ASTWhile(ASTExpression conditionExpression, ASTBlock loopedBlock) {
        this.conditionExpression = conditionExpression;
        this.loopedBlock = loopedBlock;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
