package semantics;

import lexer.Type;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VariableSymbolTable {
    //Stack of scopes
    private LinkedList<Map<String, TypeValuePair>> table;

    /**
     * Creates a copy of another variable symbol table (needed for structs,
     * since otherwise, changing the value of one "instance" changes the
     * value of another instance
     * @param variableSymbolTable the symbol table to copy
     */
    public VariableSymbolTable(VariableSymbolTable variableSymbolTable) {
        table = new LinkedList<>();

        //Iterates over all the scopes in the other symbol table and clones each entry
        for (Map<String, TypeValuePair> map : variableSymbolTable.table) {
            Map<String, TypeValuePair> mapToCopy = new HashMap<>();

            Set<Map.Entry<String, TypeValuePair>> entries = map.entrySet();

            for (Map.Entry<String, TypeValuePair> entry : entries) {
                mapToCopy.put(entry.getKey(), new TypeValuePair(entry.getValue().type, entry.getValue().value));
            }

            table.add(mapToCopy);
        }
    }

    /**
     * Creates a new empty variable symbol table
     */
    public VariableSymbolTable() {
        table = new LinkedList<>();
    }

    /**
     * Adds a new scope
     */
    public void push() {
        table.push (new HashMap<>());
    }

    /**
     * Inserts a variable with a specific type
     * @param identifier identifier of variable to add
     * @param type type of variable to add
     */
    public void insert(String identifier, Type type) {
        if (!table.isEmpty()) {
            table.peek().put(identifier, new TypeValuePair(type));
        }
    }

    /**
     * Inserts a variable with a specific type and value
     * @param identifier identifier of variable to add
     * @param type type of variable to add
     * @param value value of variable to add
     */
    public void insert(String identifier, Type type, Object value) {
        if (!table.isEmpty()) {
            table.peek().put(identifier, new TypeValuePair(type, value));
        }
    }

    /**
     * Changes the value of a variable in the symbol table
     * @param identifier identifier of variable to update
     * @param newValue new value of variable
     */
    public void changeValue(String identifier, Object newValue) {
        TypeValuePair temp = lookup(identifier);

        if (temp != null) {
            temp.value = newValue;
        }
    }

    /**
     * Changes the type of a variable in the symbol table (created for use with the auto specifier)
     * @param identifier identifier of variable to update
     * @param type new type of variable
     */
    public void changeType(String identifier, Type type) {
        TypeValuePair temp = lookup(identifier);

        if (temp != null) {
            temp.type = type;
        }
    }

    /**
     * Gets the type of a specific variable
     * @param identifier identifier of variable to lookup
     * @return type of variable, null if not found
     */
    public Type lookupType(String identifier) {
        TypeValuePair temp = lookup(identifier);
        return temp != null ? temp.type : null;
    }

    /**
     * Gets the type and value of a specific variable
     * @param identifier identifier of variable to lookup
     * @return type and value (TypeValuePair) of variable, null if not found
     */
    public TypeValuePair lookup(String identifier) {
        Iterator<Map<String, TypeValuePair>> iterator = table.iterator();

        if (!table.isEmpty()) {
            do {
                Map<String, TypeValuePair> map = iterator.next();

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
