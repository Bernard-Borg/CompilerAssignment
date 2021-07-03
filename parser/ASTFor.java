package parser;

import visitors.ASTVisitor;

//ASTNode for for loops
public class ASTFor extends ASTStatement {
    public ASTVariableDeclaration variableDeclaration;
    public ASTExpression conditionExpression;
    public ASTAssignment assignment;
    public ASTBlock loopedBlock;

    /**
     * @param variableDeclaration stores the variable declaration part of the for loop
     * @param conditionExpression stores the condition part of the for loop
     * @param assignment stores the assignment part of the for loop
     * @param loopedBlock stores the main for loop block
     */
    public ASTFor(ASTVariableDeclaration variableDeclaration, ASTExpression conditionExpression, ASTAssignment assignment, ASTBlock loopedBlock) {
        this.variableDeclaration = variableDeclaration;
        this.conditionExpression = conditionExpression;
        this.assignment = assignment;
        this.loopedBlock = loopedBlock;
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
