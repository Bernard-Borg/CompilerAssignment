package parser;

import visitors.ASTVisitor;

public class ASTFor extends ASTStatement {
    public ASTVariableDeclaration variableDeclaration;
    public ASTExpression conditionExpression;
    public ASTAssignment assignment;
    public ASTBlock loopedBlock;

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
