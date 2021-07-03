package semantics;

import lexer.Type;

//Class containing a type paired with a value
public class TypeValuePair {
    public Type type;
    public Object value;

    /**
     * Constructs a type value pair with the type and null value
     * @param type type of TypeValuePair
     */
    public TypeValuePair(Type type) {
        this.type = type;
        this.value = null;
    }

    /**
     * Constructs a type value pair with the type and value
     * @param type type of TypeValuePair
     * @param value value of TypeValuePair
     */
    public TypeValuePair(Type type, Object value) {
        this.type = type;
        this.value = value;
    }
}
