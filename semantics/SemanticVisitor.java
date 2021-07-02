package semantics;

import lexer.Array;
import lexer.TokenType;
import lexer.Type;
import parser.*;
import visitors.ASTVisitor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemanticVisitor implements ASTVisitor {
    private VariableSymbolTable variableSymbolTable;
    private FunctionSymbolTable functionSymbolTable;
    private final ASTProgram program;
    private Type expressionType = null;
    private Type returnTypeOfCurrentFunction = null;
    private String identifierOfCurrentFunction = "";
    private boolean hasReturn = false;

    //Stores the existing structs
    private Map<String, ASTStruct> registeredStructs;
    private boolean isStruct = false;

    public SemanticVisitor(ASTProgram program) {
        variableSymbolTable = new VariableSymbolTable();
        functionSymbolTable = new FunctionSymbolTable();
        registeredStructs = new HashMap<>();

        this.program = program;
    }

    public void doSemanticAnalysis() throws SemanticException {
        visit(program);
    }

    private void throwException(String message) throws SemanticException {
        throw new SemanticException(message);
    }

    @Override
    public void visit(ASTProgram astProgram) throws SemanticException {
        //Global scopes
        variableSymbolTable.push();
        functionSymbolTable.push();

        for (ASTStatement statement : astProgram.statements) {
            visit(statement);
        }

        variableSymbolTable.pop();
        functionSymbolTable.pop();
    }

    @Override
    public void visit(ASTStatement statement) throws SemanticException {
        if (statement instanceof ASTAssignment) {
            visit((ASTAssignment) statement);
        } else if (statement instanceof ASTBlock) {
            visit((ASTBlock) statement);
        } else if (statement instanceof ASTFor) {
            visit((ASTFor) statement);
        } else if (statement instanceof ASTFunctionDeclaration) {
            visit((ASTFunctionDeclaration) statement);
        } else if (statement instanceof ASTIf) {
            visit((ASTIf) statement);
        } else if (statement instanceof ASTPrint) {
            visit((ASTPrint) statement);
        } else if (statement instanceof ASTReturn) {
            visit((ASTReturn) statement);
        } else if (statement instanceof ASTVariableDeclaration) {
            visit((ASTVariableDeclaration) statement);
        } else if (statement instanceof ASTWhile) {
            visit((ASTWhile) statement);
        } else if (statement instanceof ASTStruct) {
            visit((ASTStruct) statement);
        } else {
            throwException("Unknown statement node");
        }
    }

    @Override
    public void visit(ASTAssignment astAssignment) throws SemanticException {
        Type type;

        if (astAssignment.identifier instanceof ASTArrayIndexIdentifier) {
            visit((ASTArrayIndexIdentifier) astAssignment.identifier);
            type = expressionType;
        } else {
            type = variableSymbolTable.lookupType(astAssignment.identifier.identifier);
        }

        if (type != null) {
            visit(astAssignment.expression);

            if (type instanceof Array) {
                //Case when assigning array to undefined auto array
                if ("auto".equals(((Array) type).arrayType.lexeme)) {
                    variableSymbolTable.changeType(astAssignment.identifier.identifier, new Array(-1, expressionType));
                } else {
                    if (!expressionType.lexeme.equals(type.lexeme)) {
                        throwException("Cannot assign expression of type " + expressionType.lexeme + " to a variable of type " + type.lexeme);
                    }
                }
            } else {
                //If type specifier is auto (declared as auto but not initialised), set it to the type being assigned
                if ("auto".equals(type.lexeme)) {
                    //Case when accessing undefined array with auto type (example a[0] where let a[size]:auto;)
                    if (astAssignment.identifier instanceof ASTArrayIndexIdentifier) {
                        variableSymbolTable.changeType(astAssignment.identifier.identifier, new Array(-1, expressionType));
                    } else {
                        variableSymbolTable.changeType(astAssignment.identifier.identifier, expressionType);
                    }

                    return;
                }

                if (!type.lexeme.equals(expressionType.lexeme)) {
                    if (!("int".equals(expressionType.lexeme) && "float".equals(type.lexeme))) {
                        throwException("Cannot assign an expression with type " + expressionType.lexeme + " to a variable of type " + type.lexeme);
                    }
                }
            }
        } else {
            throwException("Cannot resolve " + astAssignment.identifier.identifier);
        }
    }

    @Override
    public void visit(ASTBlock astBlock) throws SemanticException {
        variableSymbolTable.push();
        functionSymbolTable.push();

        hasReturn = false;

        for (ASTStatement statement : astBlock.statements) {
            if (hasReturn) {
                throwException("Unreachable statement(s)");
            }

            if (statement instanceof ASTReturn) {
                hasReturn = true;
            }

            visit(statement);
        }

        variableSymbolTable.pop();
        functionSymbolTable.pop();
    }

    @Override
    public void visit(ASTFor astFor) throws SemanticException {
        variableSymbolTable.push();
        //No need to do functionSymbolTable.push() since you can't declare a function in for

        if (astFor.variableDeclaration != null) {
            visit(astFor.variableDeclaration);
        }

        visit(astFor.conditionExpression);

        if (!"bool".equals(expressionType.lexeme)) {
            throwException("Condition expression requires bool type");
        }

        if (astFor.assignment != null) {
            visit(astFor.assignment);
        }

        visit(astFor.loopedBlock);

        variableSymbolTable.pop();
    }

    @Override
    public void visit(ASTFunctionDeclaration astFunctionDeclaration) throws SemanticException {
        if (returnTypeOfCurrentFunction != null) {
            throwException("Cannot nest functions");
        }

        if (functionSymbolTable.lookup(functionSymbolTable.generateIdentifier(astFunctionDeclaration)) != null) {
            throwException("Function " + astFunctionDeclaration.functionName.identifier + " has already been defined");
        }

        hasReturn = false;
        variableSymbolTable.push();

        for (ASTParameter parameter : astFunctionDeclaration.parameterList) {
            if ("auto".equals(parameter.type.lexeme)) {
                throwException("Parameter cannot be of type auto");
            }

            if (variableSymbolTable.lookupType(parameter.identifier.identifier) != null) {
                throwException("Variable " + parameter.identifier.identifier + " has already been defined");
            }

            variableSymbolTable.insert(parameter.identifier.identifier, parameter.type);
        }

        identifierOfCurrentFunction = functionSymbolTable.generateIdentifier(astFunctionDeclaration);
        returnTypeOfCurrentFunction = astFunctionDeclaration.returnType;
        functionSymbolTable.registerFunction(astFunctionDeclaration);

        for (ASTStatement statement : astFunctionDeclaration.functionBlock.statements) {
            if (hasReturn) {
                throwException("Unreachable statement(s)");
            }

            visit(statement);

            if (statement instanceof ASTReturn) {
                hasReturn = true;
            }
        }

        if (!hasReturn) {
            throwException("Function must return a value");
        }

        identifierOfCurrentFunction = "";
        returnTypeOfCurrentFunction = null;
        variableSymbolTable.pop();
    }

    @Override
    public void visit(ASTIf astIf) throws SemanticException {
        visit(astIf.conditionExpression);

        if (!"bool".equals(expressionType.lexeme)) {
            throwException("Condition expression requires bool type");
        }

        visit(astIf.trueBlock);

        boolean tempHasReturn = hasReturn;

        if (astIf.falseBlock != null) {
            visit(astIf.falseBlock);

            hasReturn = hasReturn && tempHasReturn;
        } else {
            hasReturn = false;
        }
    }

    @Override
    public void visit(ASTPrint astPrint) throws SemanticException {
        visit(astPrint.expression);
    }

    @Override
    public void visit(ASTReturn astReturn) throws SemanticException {
        if (returnTypeOfCurrentFunction == null) {
            throwException("You cannot return a value in global scope");
        } else {
            visit(astReturn.expression);

            if ("auto".equals(returnTypeOfCurrentFunction.lexeme)) {
                returnTypeOfCurrentFunction = expressionType;
                functionSymbolTable.lookup(identifierOfCurrentFunction).returnType = expressionType;
            }

            if (!returnTypeOfCurrentFunction.lexeme.equals(expressionType.lexeme)) {
                if (!("float".equals(returnTypeOfCurrentFunction.lexeme) && "int".equals(expressionType.lexeme))) {
                    throwException("Returning type " + expressionType.lexeme + ", required: " + returnTypeOfCurrentFunction.lexeme);
                }
            }
        }
    }

    @Override
    public void visit(ASTVariableDeclaration astVariableDeclaration) throws SemanticException {
        if (isStruct && ("auto".equals(astVariableDeclaration.type.lexeme)
                || "auto[]".equals(astVariableDeclaration.type.lexeme))) {
            throwException("Can not use auto type for variable declarations in structs");
        }

        //If declaring an array, check whether it's size is of type integer
        if (astVariableDeclaration.identifier instanceof ASTArrayIndexIdentifier) {
            visit(((ASTArrayIndexIdentifier) astVariableDeclaration.identifier).index);

            if (!"int".equals(expressionType.lexeme)) {
                throwException("Array size can only be of type int");
            }
        }

        Type declaredType = astVariableDeclaration.type;

        if (variableSymbolTable.lookupType(astVariableDeclaration.identifier.identifier) != null) {
            throwException("Variable " + astVariableDeclaration.identifier.identifier + " has already been declared");
        } else {
            if (astVariableDeclaration.expression != null) {
                visit(astVariableDeclaration.expression);

                //If an array is being declared of type auto (auto[]), change type
                if (astVariableDeclaration.type instanceof Array && expressionType instanceof Array) {
                    if ("auto".equals(((Array) astVariableDeclaration.type).arrayType.lexeme)) {
                        declaredType = new Array(-1, ((Array) expressionType).arrayType);
                    } else {
                        if (!expressionType.lexeme.equals(astVariableDeclaration.type.lexeme)) {
                            throwException("Cannot assign expression of type " + expressionType.lexeme + " to a variable of type " + astVariableDeclaration.type.lexeme);
                        }
                    }
                } else {
                    if ("auto".equals(astVariableDeclaration.type.lexeme)) {
                        declaredType = expressionType;
                    } else {
                        if (!expressionType.lexeme.equals(astVariableDeclaration.type.lexeme)) {
                            if (!("int".equals(expressionType.lexeme) && "float".equals(astVariableDeclaration.type.lexeme))) {
                                throwException("Cannot assign expression of type " + expressionType.lexeme + " to a variable of type " + astVariableDeclaration.type.lexeme);
                            }
                        }
                    }
                }
            }

            variableSymbolTable.insert(astVariableDeclaration.identifier.identifier, declaredType);
        }
    }

    @Override
    public void visit(ASTWhile astWhile) throws SemanticException {
        visit(astWhile.conditionExpression);

        if (!"bool".equals(expressionType.lexeme)) {
            throwException("Condition expression requires bool type");
        }

        visit(astWhile.loopedBlock);
    }

    @Override
    public void visit(ASTExpression astExpression) throws SemanticException {
        if (astExpression instanceof ASTBinaryOperator) {
            visit((ASTBinaryOperator) astExpression);
        } else if (astExpression instanceof ASTFunctionCall) {
            visit((ASTFunctionCall) astExpression);
        } else if (astExpression instanceof ASTStructVariableSelector) {
            visit((ASTStructVariableSelector) astExpression);
        } else if (astExpression instanceof ASTStructFunctionSelector) {
            visit((ASTStructFunctionSelector) astExpression);
        } else if (astExpression instanceof ASTArrayIndexIdentifier) {
            visit((ASTArrayIndexIdentifier) astExpression);
        } else if (astExpression instanceof ASTIdentifier) {
            visit((ASTIdentifier) astExpression);
        } else if (astExpression instanceof ASTLiteral) {
            visit((ASTLiteral) astExpression);
        } else if (astExpression instanceof ASTUnary) {
            visit((ASTUnary) astExpression);
        } else if (astExpression instanceof ASTArrayLiteral) {
            visit((ASTArrayLiteral) astExpression);
        } else {
            throwException("Unknown node while visiting expression");
        }
    }

    @Override
    public void visit(ASTBinaryOperator operator) throws SemanticException {
        String type1;
        String type2;

        visit(operator.expression1);
        type1 = expressionType.lexeme;

        visit(operator.expression2);
        type2 = expressionType.lexeme;

        switch (operator.operator.tokenType) {
            case ADD:
                expressionType = checkTypesAdd(type1, type2);
                break;
            case SUB:
            case MUL:
            case DIV:
                expressionType = checkTypesMath(operator.operator.tokenType, type1, type2);
                break;
            case CMP:
            case NE:
                expressionType = checkTypesEquality(type1, type2);
                break;
            case GT:
            case LT:
            case GTE:
            case LTE:
                expressionType = checkTypesComp(type1, type2);
                break;
            case OR:
            case AND:
                expressionType = checkTypesLogic(type1, type2);
                break;
        }
    }

    private boolean isPrimitive(String type) {
        List<String> primitiveTypes = Arrays.asList("int", "float", "string", "bool", "char");
        return primitiveTypes.contains(type);
    }

    private boolean checkSymmetrical(String typeLiteral, String typeLiteral2, String type1, String type2) {
        return (typeLiteral.equals(type1) && typeLiteral2.equals(type2)) || (typeLiteral2.equals(type1) && typeLiteral.equals(type2));
    }

    private boolean checkAny(String typeLiteral, String type1, String type2) {
        return typeLiteral.equals(type1) || typeLiteral.equals(type2);
    }

    private boolean checkBoth(String typeLiteral, String type1, String type2) {
        return typeLiteral.equals(type1) && typeLiteral.equals(type2);
    }

    //Addition (ADD)
    private Type checkTypesAdd(String type1, String type2) throws SemanticException {
        Type typeToReturn = null;

        if (checkSymmetrical("int", "float", type1, type2)) {
            typeToReturn = Type.FLOAT;
        } else if (checkSymmetrical("int", "string", type1, type2)) {
            typeToReturn = Type.STRING;
        } else if (checkSymmetrical("string", "float", type1, type2)) {
            typeToReturn = Type.STRING;
        } else if (checkSymmetrical("char", "string", type1, type2)) {
            typeToReturn = Type.STRING;
        } else if (checkBoth("int", type1, type2)) {
            typeToReturn = Type.INTEGER;
        } else if (checkBoth("float", type1, type2)) {
            typeToReturn = Type.FLOAT;
        } else if (checkBoth("string", type1, type2)) {
            typeToReturn = Type.STRING;
        } else {
            throwException("Operator '+' cannot be applied to " + type1 + " and " + type2);
        }

        return typeToReturn;
    }

    //Other mathematical operators (MUL, SUB, DIV)
    private Type checkTypesMath(TokenType operatorSymbol, String type1, String type2) throws SemanticException {
        Type typeToReturn = null;

        if (checkSymmetrical("int", "float", type1, type2)) {
            typeToReturn = Type.FLOAT;
        } else if (checkBoth("int", type1, type2)) {
            typeToReturn = Type.INTEGER;
        } else if (checkBoth("float", type1, type2)) {
            typeToReturn = Type.FLOAT;
        } else {
            throwException("Operator " + operatorSymbol.toString() + " cannot be applied to " + type1 + " and " + type2);
        }

        return typeToReturn;
    }

    //Equality operators (NE/CMP)
    private Type checkTypesEquality(String type1, String type2) throws SemanticException {
        if (isPrimitive(type1) && isPrimitive(type2)) {
            if (checkSymmetrical("int", "float", type1, type2) || type1.equals(type2)) {
                return Type.BOOL;
            }
        }

        throwException("Incomparable types " + type1 + " and " + type2);
        return Type.BOOL;
    }

    //Comparison operators (GT/LT/GTE/LTE)
    private Type checkTypesComp(String type1, String type2) throws SemanticException {
        if (isPrimitive(type1) && isPrimitive(type2)) {
            //Allow comparisons between two strings
            if (checkBoth("string", type1, type2)) {
                return Type.BOOL;
            }

            //Allow comparison between two characters
            if (checkBoth("char", type1, type2)) {
                return Type.BOOL;
            }

            //Disallow comparisons including booleans or one character and another type
            if (!checkAny("bool", type1, type2) && !checkAny("char", type1, type2)
                    && !checkAny("string", type1, type2)) {
                return Type.BOOL;
            }
        }

        throwException("Incomparable types " + type1 + " and " + type2);
        return Type.BOOL;
    }

    //Logic operators (AND/OR)
    private Type checkTypesLogic(String type1, String type2) throws SemanticException {
        if (!checkBoth("bool", type1, type2)) {
            throwException("Bad operand types " + type1 + " and " + type2);
        }

        return Type.BOOL;
    }

    @Override
    public void visit(ASTFunctionCall astFunctionCall) throws SemanticException {
        StringBuilder stringBuilder = new StringBuilder(astFunctionCall.identifier.identifier);

        //Need to loop again to get the types
        for (ASTExpression expression : astFunctionCall.parameters) {
            visit(expression);
            stringBuilder.append(expressionType.lexeme);
        }

        ASTFunctionDeclaration declaredFunction = functionSymbolTable.lookup(stringBuilder.toString());

        if (declaredFunction != null) {
            if (declaredFunction.parameterList.size() != astFunctionCall.parameters.size()) {
                throwException("Number of parameters not equal");
            }

            String declaredFunctionParameterType;

            for (int i = 0; i < declaredFunction.parameterList.size(); i++) {
                visit(astFunctionCall.parameters.get(i));

                declaredFunctionParameterType = declaredFunction.parameterList.get(i).type.lexeme;

                if (!expressionType.lexeme.equals(declaredFunctionParameterType)) {
                    if (!("int".equals(expressionType.lexeme) && "float".equals(declaredFunctionParameterType))) {
                        throwException("Incorrect parameter type, required " + declaredFunctionParameterType + ", got " + expressionType.lexeme);
                    }
                }
            }

            expressionType = declaredFunction.returnType;
        } else {
            throwException("Cannot resolve function " + astFunctionCall.identifier.identifier);
        }
    }

    @Override
    public void visit(ASTIdentifier astIdentifier) throws SemanticException {
        Type identifierType = variableSymbolTable.lookupType(astIdentifier.identifier);

        if (identifierType == null) {
            throwException("Cannot resolve identifier " + astIdentifier.identifier);
        } else {
            expressionType = identifierType;
        }
    }

    @Override
    public void visit(ASTArrayIndexIdentifier astArrayIndexIdentifier) throws SemanticException {
        visit(astArrayIndexIdentifier.index);

        if (!"int".equals(expressionType.lexeme)) {
            throwException("Array index must be of type int");
        }

        Type identifierType = variableSymbolTable.lookupType(astArrayIndexIdentifier.identifier);

        //Checks whether array is in symbol table
        if (identifierType == null) {
            throwException("Could not resolve identifier " + astArrayIndexIdentifier.identifier);
        }

        //Checks whether the variable is indeed an array
        if (!(identifierType instanceof Array)) {
            throwException("Array type expected, got " + identifierType.lexeme);
        } else {
            expressionType = ((Array) identifierType).arrayType;
        }
    }

    @Override
    public void visit(ASTLiteral astLiteral) {
        if ("bool".equals(astLiteral.type)) {
            expressionType = Type.BOOL;
        } else if ("string".equals(astLiteral.type)) {
            expressionType = Type.STRING;
        } else if ("int".equals(astLiteral.type)) {
            expressionType = Type.INTEGER;
        } else if ("float".equals(astLiteral.type)) {
            expressionType = Type.FLOAT;
        } else if ("char".equals(astLiteral.type)) {
            expressionType = Type.CHAR;
        }
    }

    @Override
    public void visit(ASTArrayLiteral astArrayLiteral) throws SemanticException {
        Array arrayType = null;
        int size = astArrayLiteral.arrayMembers.size();

        for(ASTExpression expression : astArrayLiteral.arrayMembers) {
            visit(expression);

            if (arrayType != null) {
                //Checks whether each value in the array has the same type as the first element
                if (!arrayType.arrayType.lexeme.equals(expressionType.lexeme)) {
                    //Integers inserted in a float array (first element is float) are accepted
                    if (!("float".equals(arrayType.arrayType.lexeme) && "int".equals(expressionType.lexeme))) {
                        throwException("Array values must all be of the same type");
                    }
                }
            } else {
                if (expressionType instanceof Array) {
                    throwException ("Multidimensional arrays are not supported");
                }

                arrayType = new Array (size, expressionType);
            }
        }

        expressionType = arrayType;
    }

    @Override
    public void visit(ASTUnary astUnary) throws SemanticException {
        visit(astUnary.expression);

        if (astUnary.unaryType == TokenType.SUB) {
            if (!("float".equals(expressionType.lexeme) || "int".equals(expressionType.lexeme))) {
                throwException("Unary '-' can only be used with float and integer types");
            }
        } else {
            if (!"bool".equals(expressionType.lexeme)) {
                throwException("Not can only be used with boolean types");
            }
        }
    }

    @Override
    public void visit(ASTStruct astStruct) throws SemanticException {
        isStruct = true;

        if (registeredStructs.containsKey(astStruct.structName.identifier)) {
            throwException("Duplicate struct: " + astStruct.structName.identifier);
        }

        //Set current scope to only the struct scope
        VariableSymbolTable oldVariableSymbolTable = variableSymbolTable;
        FunctionSymbolTable oldFunctionSymbolTable = functionSymbolTable;

        variableSymbolTable = astStruct.variableSymbolTable;
        functionSymbolTable = astStruct.functionSymbolTable;

        for (ASTStatement statement : astStruct.statementsList) {
            visit(statement);
        }

        astStruct.variableSymbolTable = variableSymbolTable;
        astStruct.functionSymbolTable = functionSymbolTable;

        variableSymbolTable = oldVariableSymbolTable;
        functionSymbolTable = oldFunctionSymbolTable;

        registeredStructs.put(astStruct.structName.identifier, astStruct);
        isStruct = false;
    }

    @Override
    public void visit(ASTStructVariableSelector astStructVariableSelector) throws SemanticException {
        //Get struct type
        Type type = variableSymbolTable.lookupType(astStructVariableSelector.identifier);

        if (type == null) {
            throwException ("Could not resolve variable " + astStructVariableSelector.identifier);
        }

        //Get registered struct
        ASTStruct struct = registeredStructs.get(type.lexeme);

        if (struct == null) {
            throwException("Could not resolve struct type " + type.lexeme);
        }

        //Set current scope to only the struct scope
        VariableSymbolTable oldVariableSymbolTable = variableSymbolTable;
        FunctionSymbolTable oldFunctionSymbolTable = functionSymbolTable;

        variableSymbolTable = struct.variableSymbolTable;
        functionSymbolTable = struct.functionSymbolTable;

        visit(astStructVariableSelector.elementIdentifier);

        struct.variableSymbolTable = variableSymbolTable;
        struct.functionSymbolTable = functionSymbolTable;

        variableSymbolTable = oldVariableSymbolTable;
        functionSymbolTable = oldFunctionSymbolTable;
    }

    @Override
    public void visit(ASTStructFunctionSelector astStructFunctionSelector) throws SemanticException {
        //Get struct type
        Type type = variableSymbolTable.lookupType(astStructFunctionSelector.identifier);

        if (type == null) {
            throwException ("Could not resolve variable " + astStructFunctionSelector.identifier);
        }

        //Get registered struct
        ASTStruct struct = registeredStructs.get(type.lexeme);

        if (struct == null) {
            throwException("Could not resolve struct type " + type.lexeme);
        }

        //Set current scope to only the struct scope
        VariableSymbolTable oldVariableSymbolTable = variableSymbolTable;
        FunctionSymbolTable oldFunctionSymbolTable = functionSymbolTable;

        variableSymbolTable = struct.variableSymbolTable;
        functionSymbolTable = struct.functionSymbolTable;

        visit(astStructFunctionSelector.functionCall);

        struct.variableSymbolTable = variableSymbolTable;
        struct.functionSymbolTable = functionSymbolTable;

        variableSymbolTable = oldVariableSymbolTable;
        functionSymbolTable = oldFunctionSymbolTable;
    }
}