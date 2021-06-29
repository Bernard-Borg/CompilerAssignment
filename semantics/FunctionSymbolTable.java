package semantics;

import parser.ASTFunctionDeclaration;

import java.util.*;

public class FunctionSymbolTable {
    private final LinkedList<Map<String, ASTFunctionDeclaration>> table;

    public FunctionSymbolTable() {
        table = new LinkedList<>();
    }

    public void push() {
        table.push (new HashMap<>());
    }

    public String generateIdentifier(ASTFunctionDeclaration function) {
        StringBuilder generatedIdentifier = new StringBuilder(function.functionName.identifier);

        function.parameterList.forEach(astParameter -> {
            generatedIdentifier.append(astParameter.type.lexeme);
        });

        return generatedIdentifier.toString();
    }

    public void registerFunction(ASTFunctionDeclaration function) {
        if (!table.isEmpty()) {
            table.peek().put(generateIdentifier(function), function);
        }
    }

    public ASTFunctionDeclaration lookup(String identifier) {
        Iterator<Map<String, ASTFunctionDeclaration>> iterator = table.iterator();

        if (!table.isEmpty()) {
            do {
                Map<String, ASTFunctionDeclaration> map = iterator.next();

                if (map.containsKey(identifier)) {
                    return map.get(identifier);
                }
            } while (iterator.hasNext());
        }

        return null;
    }

    public void pop() {
        table.pop();
    }
}
