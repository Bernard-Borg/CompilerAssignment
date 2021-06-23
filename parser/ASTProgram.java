package parser;

import visitors.ASTVisitor;

import java.util.ArrayList;
import java.util.List;

public class ASTProgram implements ASTNode {
    public List<ASTStatement> statements;

    public ASTProgram(List<ASTStatement> statements) {
        this.statements = new ArrayList<>(statements);
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
