package parser;

import visitors.ASTVisitor;

public interface ASTNode {
    void accept(ASTVisitor visitor) throws Exception;
}
