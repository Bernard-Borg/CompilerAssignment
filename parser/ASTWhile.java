package parser;

import visitors.ASTVisitor;

//ASTNode for while statements
public class ASTWhile extends ASTStatement {
    public ASTExpression conditionExpression;
    public ASTBlock loopedBlock;

    /**
     * @param conditionExpression while condition expression
     * @param loopedBlock main while block to loop
     */
    public ASTWhile(ASTExpression conditionExpression, ASTBlock loopedBlock) {
        this.conditionExpression = conditionExpression;
        this.loopedBlock = loopedBlock;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
