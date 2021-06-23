package visitors;

import parser.*;

public interface ASTVisitor {
    void visit(ASTProgram astProgram) throws Exception;
    void visit(ASTStatement statement) throws Exception;
    void visit(ASTAssignment astAssignment) throws Exception;
    void visit(ASTBlock astBlock) throws Exception;
    void visit(ASTFor astFor) throws Exception;
    void visit(ASTFunctionDeclaration astFunctionDeclaration) throws Exception;
    void visit(ASTIf astIf) throws Exception;
    void visit(ASTPrint astPrint) throws Exception;
    void visit(ASTReturn astReturn) throws Exception;
    void visit(ASTVariableDeclaration astVariableDeclaration) throws Exception;
    void visit(ASTWhile astWhile) throws Exception;
    void visit(ASTExpression astExpression) throws Exception;
    void visit(ASTBinaryOperator operator) throws Exception;
    void visit(ASTFunctionCall astFunctionCall) throws Exception;
    void visit(ASTIdentifier astIdentifier) throws Exception;
    void visit(ASTLiteral astLiteral) throws Exception;
    void visit(ASTUnary astUnary) throws Exception;
}
