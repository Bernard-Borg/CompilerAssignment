package parser;

import visitors.ASTVisitor;

//The parent of all nodes
public interface ASTNode {
    void accept(ASTVisitor visitor) throws Exception;
}
