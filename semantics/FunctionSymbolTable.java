package semantics;

import parser.ASTFunctionDeclaration;

import java.util.*;

public class FunctionSymbolTable {
    //Stack of scopes
    private final LinkedList<Map<String, ASTFunctionDeclaration>> table;

    public FunctionSymbolTable() {
        table = new LinkedList<>();
    }

    /**
     * Adds a new scope
     */
    public void push() {
        table.push (new HashMap<>());
    }

    /**
     * Generates a unique identifier for a function including parameter types (to allow for function overloading)
     * @param function function to create identifier for
     * @return identifier concatenated with the function parameter's types
     */
    public String generateIdentifier(ASTFunctionDeclaration function) {
        StringBuilder generatedIdentifier = new StringBuilder(function.functionName.identifier);

        function.parameterList.forEach(astParameter -> {
            generatedIdentifier.append(astParameter.type.lexeme);
        });

        return generatedIdentifier.toString();
    }

    /**
     * Adds function to scope
     * @param function
     */
    public void registerFunction(ASTFunctionDeclaration function) {
        if (!table.isEmpty()) {
            table.peek().put(generateIdentifier(function), function);
        }
    }

    /**
     * Looks for function with its identifier
     * @param identifier name of function to lookup
     * @return ASTFunctionDeclaration node
     */
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

    /**
     * Removes last added scope
     */
    public void pop() {
        table.pop();
    }
}
