package semantics;

import lexer.Type;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class VariableSymbolTable {
    private final LinkedList<Map<String, TypeValuePair>> table;

    public VariableSymbolTable() {
        table = new LinkedList<>();
    }

    public void push() {
        table.push (new HashMap<>());
    }

    public void insert(String identifier, Type type) {
        if (!table.isEmpty()) {
            table.peek().put(identifier, new TypeValuePair(type));
        }
    }

    public void insert(String identifier, Type type, Object value) {
        if (!table.isEmpty()) {
            table.peek().put(identifier, new TypeValuePair(type, value));
        }
    }

    public void changeValue(String identifier, Object newValue) {
        TypeValuePair temp = lookup(identifier);

        if (temp != null) {
            temp.value = newValue;
        }
    }

    public Type lookupType(String identifier) {
        TypeValuePair temp = lookup(identifier);
        return temp != null ? temp.type : null;
    }

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

    public void pop() {
        table.pop();
    }
}
