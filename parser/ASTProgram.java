package parser;

import visitors.ASTVisitor;

import java.util.ArrayList;
import java.util.List;

//ASTNode for the entire program
public class ASTProgram implements ASTNode {
    public List<ASTStatement> statements;

    /**
     * @param statements statements of the whole program
     */
    public ASTProgram(List<ASTStatement> statements) {
        this.statements = new ArrayList<>(statements);
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
