package semantics;

import lexer.Type;

public class TypeValuePair {
    public Type type;
    public Object value;

    public TypeValuePair(Type type) {
        this.type = type;
        this.value = null;
    }

    public TypeValuePair(Type type, Object value) {
        this.type = type;
        this.value = value;
    }
}
