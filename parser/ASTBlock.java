package parser;

import visitors.ASTVisitor;

import java.util.ArrayList;
import java.util.List;

//ASTNode for blocks ({ ... })
public class ASTBlock extends ASTStatement {
    public List<ASTStatement> statements;

    /**
     * @param statements contains the statements inside the block
     */
    public ASTBlock(List<ASTStatement> statements) {
        this.statements = new ArrayList<>(statements);
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
