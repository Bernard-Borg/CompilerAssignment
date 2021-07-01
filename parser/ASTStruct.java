package parser;

import semantics.FunctionSymbolTable;
import semantics.VariableSymbolTable;
import visitors.ASTVisitor;

import java.util.List;

public class ASTStruct extends ASTStatement {
    public ASTIdentifier structName;
    public List<ASTStatement> statementsList;
    public VariableSymbolTable variableSymbolTable;
    public FunctionSymbolTable functionSymbolTable;

    public ASTStruct (ASTIdentifier structName, List<ASTStatement> statements) {
        this.structName = structName;

        if (statements.isEmpty()) {
            statements = null;
        }

        statementsList = statements;

        variableSymbolTable = new VariableSymbolTable();
        functionSymbolTable = new FunctionSymbolTable();

        variableSymbolTable.push();
        functionSymbolTable.push();
    }

    @Override
    public void accept(ASTVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
