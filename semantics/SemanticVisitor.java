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
    private final Map<String, ASTStruct> registeredStructs;

    //Flag which is true if currently visiting nodes inside a struct node
    private boolean isStruct = false;

    /**
     * Constructs the semantic visitor
     * @param program the abstract syntax tree
     */
    public SemanticVisitor(ASTProgram program) {
        variableSymbolTable = new VariableSymbolTable();
        functionSymbolTable = new FunctionSymbolTable();
        registeredStructs = new HashMap<>();

        this.program = program;
    }

    /**
     * Starts semantic analysis
     */
    public void doSemanticAnalysis() throws SemanticException {
        visit(program);
    }

    /**
     * Utility method which throws semantic exception
     * @param message custom exception message
     */
    private void throwException(String message) throws SemanticException {
        throw new SemanticException(message);
    }

    /**
     * ASTProgram node semantic visitor
     * @param astProgram node to visit
     */
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

    /**
     * ASTStatement node semantic visitor
     * @param statement node to visit
     */
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

    /**
     * ASTAssignment node semantic visitor
     * @param astAssignment node to visit
     */
    @Override
    public void visit(ASTAssignment astAssignment) throws SemanticException {
        Type type;

        //Visits the different identifier possibilities
        if (astAssignment.identifier instanceof ASTArrayIndexIdentifier) {
            visit((ASTArrayIndexIdentifier) astAssignment.identifier);
            type = expressionType;
        } else if (astAssignment.identifier instanceof ASTStructVariableSelector) {
            visit((ASTStructVariableSelector) astAssignment.identifier);
            type = expressionType;
        } else {
            type = variableSymbolTable.lookupType(astAssignment.identifier.identifier);
        }

        if (type != null) {
            visit(astAssignment.expression);

            if (type instanceof Array) {
                if ("auto".equals(((Array) type).arrayType.lexeme)) {
                    /*
                        Case when assigning array to undefined auto array

                        Example:

                        let x: auto;
                        x = { 3, 4, 5 };
                     */
                    variableSymbolTable.changeType(astAssignment.identifier.identifier, new Array(-1, expressionType));
                } else {
                    /*
                        Case when assigning array to variable

                        Example:

                        let x: int;
                        x = { 3, 4 };

                        Note: Currently does not support int[] being assigned to variable of type float[]
                        (This would be done here)
                     */
                    if (!expressionType.lexeme.equals(type.lexeme)) {
                        throwException("Cannot assign expression of type " + expressionType.lexeme + " to a variable of type " + type.lexeme);
                    }
                }
            } else {
                //If type specifier is auto (declared as auto but not initialised), set it to the type being assigned
                if ("auto".equals(type.lexeme)) {
                    /*
                        Case when assigning value to auto variable
                        (the auto type is changed to the type of the value being assigned)
                     */

                    //Case when accessing undefined array with auto type (example a[0] where let a[size]:auto;)
                    if (astAssignment.identifier instanceof ASTArrayIndexIdentifier) {
                        /*
                            Case when assigning a value to a specific array element of an auto array

                            Example:

                            let x[size]: auto;
                            x[0] = 3;

                            Note that the size must be specified, the following is not possible;

                            let x: auto;
                            x[0] = 3;
                         */

                        variableSymbolTable.changeType(astAssignment.identifier.identifier, new Array(-1, expressionType));
                    } else {
                        /*
                            Example:

                            let x: auto;
                            x = 3;
                         */

                        variableSymbolTable.changeType(astAssignment.identifier.identifier, expressionType);
                    }
                } else {
                    if (!type.lexeme.equals(expressionType.lexeme)) {
                        if (!("int".equals(expressionType.lexeme) && "float".equals(type.lexeme))) {
                            throwException("Cannot assign an expression with type " + expressionType.lexeme + " to a variable of type " + type.lexeme);
                        }
                    }
                }
            }
        } else {
            //If type is null, then variable has not been declared
            throwException("Cannot resolve " + astAssignment.identifier.identifier);
        }
    }

    /**
     * ASTBlock node semantic visitor
     * @param astBlock node to visit
     */
    @Override
    public void visit(ASTBlock astBlock) throws SemanticException {
        variableSymbolTable.push();
        functionSymbolTable.push();

        hasReturn = false;

        for (ASTStatement statement : astBlock.statements) {
            if (hasReturn) {
                throwException("Unreachable statement(s)");
            }

            //Added to disallow functions to be declared inside blocks like for loops, while loops, etc
            if (statement instanceof ASTFunctionDeclaration) {
                throwException("Cannot nest functions in a block");
            }

            if (statement instanceof ASTReturn) {
                hasReturn = true;
            }

            visit(statement);
        }

        variableSymbolTable.pop();
        functionSymbolTable.pop();
    }

    /**
     * ASTFor node semantic visitor
     * @param astFor node to visit
     */
    @Override
    public void visit(ASTFor astFor) throws SemanticException {
        variableSymbolTable.push();
        //No need to do functionSymbolTable.push() since you can't declare a function in for

        if (astFor.variableDeclaration != null) {
            visit(astFor.variableDeclaration);
        }

        visit(astFor.conditionExpression);

        //Verifies that the condition statement is indeed boolean
        if (!"bool".equals(expressionType.lexeme)) {
            throwException("Condition expression requires bool type");
        }

        if (astFor.assignment != null) {
            visit(astFor.assignment);
        }

        visit(astFor.loopedBlock);

        variableSymbolTable.pop();
    }

    /**
     * ASTFunctionDeclaration node semantic visitor
     * @param astFunctionDeclaration node to visit
     */
    @Override
    public void visit(ASTFunctionDeclaration astFunctionDeclaration) throws SemanticException {
        //Disallows nesting of functions
        if (returnTypeOfCurrentFunction != null) {
            throwException("Cannot nest functions");
        }

        //If the function has already been defined, an exception is thrown
        if (functionSymbolTable.lookup(functionSymbolTable.generateIdentifier(astFunctionDeclaration)) != null) {
            throwException("Function " + astFunctionDeclaration.functionName.identifier + " has already been defined");
        }

        hasReturn = false;
        variableSymbolTable.push();

        for (ASTParameter parameter : astFunctionDeclaration.parameterList) {
            //Disallows parameters of type auto
            if ("auto".equals(parameter.type.lexeme)) {
                throwException("Parameter cannot be of type auto");
            }

            //Disallow parameters with an identifier which has already been defined
            if (variableSymbolTable.lookupType(parameter.identifier.identifier) != null) {
                throwException("Variable " + parameter.identifier.identifier + " has already been defined");
            }

            variableSymbolTable.insert(parameter.identifier.identifier, parameter.type);
        }

        //Generates identifier of function for function overloading
        identifierOfCurrentFunction = functionSymbolTable.generateIdentifier(astFunctionDeclaration);
        returnTypeOfCurrentFunction = astFunctionDeclaration.returnType;
        functionSymbolTable.registerFunction(astFunctionDeclaration);

        //Parses function statements
        for (ASTStatement statement : astFunctionDeclaration.functionBlock.statements) {
            //If function has a return and more statements are left,
            if (hasReturn) {
                throwException("Unreachable statement(s)");
            }

            visit(statement);

            if (statement instanceof ASTReturn) {
                hasReturn = true;
            }
        }

        //Checks whether function contains return statement
        if (!hasReturn) {
            throwException("Function must return a value");
        }

        identifierOfCurrentFunction = "";
        returnTypeOfCurrentFunction = null;
        variableSymbolTable.pop();
    }

    /**
     * ASTIf node semantic visitor
     * @param astIf node to visit
     */
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

    /**
     * ASTPrint node semantic visitor
     * @param astPrint node to visit
     */
    @Override
    public void visit(ASTPrint astPrint) throws SemanticException {
        visit(astPrint.expression);
    }

    /**
     * ASTReturn node semantic visitor
     * @param astReturn node to visit
     */
    @Override
    public void visit(ASTReturn astReturn) throws SemanticException {
        if (returnTypeOfCurrentFunction == null) {
            //Prevents return statements from being declared outside a function
            throwException("You cannot return a value outside function scope");
        } else {
            visit(astReturn.expression);

            //If the return type is still auto, set the return type of the current function to the expression's type
            if ("auto".equals(returnTypeOfCurrentFunction.lexeme)) {
                returnTypeOfCurrentFunction = expressionType;
                functionSymbolTable.lookup(identifierOfCurrentFunction).returnType = expressionType;
            }

            /*
                If the return type and type of value being returned do not match
                (unless returning int in a float function), throw an exception
            */
            if (!returnTypeOfCurrentFunction.lexeme.equals(expressionType.lexeme)) {
                if (!("float".equals(returnTypeOfCurrentFunction.lexeme) && "int".equals(expressionType.lexeme))) {
                    throwException("Returning type " + expressionType.lexeme + ", required: " + returnTypeOfCurrentFunction.lexeme);
                }
            }
        }
    }

    /**
     * ASTVariableDeclaration node semantic visitor
     * @param astVariableDeclaration node to visit
     */
    @Override
    public void visit(ASTVariableDeclaration astVariableDeclaration) throws SemanticException {
        //Prevents structs declaring auto type
        if (isStruct && astVariableDeclaration.type.lexeme.contains("auto")){
            throwException("Can not use auto type for variable declarations in structs");
        }

        //If declaring an array, verifies that the type of the size expression is of type integer
        if (astVariableDeclaration.identifier instanceof ASTArrayIndexIdentifier) {
            visit(((ASTArrayIndexIdentifier) astVariableDeclaration.identifier).index);

            if (!"int".equals(expressionType.lexeme)) {
                throwException("Array size can only be of type int");
            }
        }

        Type declaredType = astVariableDeclaration.type;

        if (variableSymbolTable.lookupType(astVariableDeclaration.identifier.identifier) != null) {
            //Checks whether variable has already been declared; if yes, throw exception
            throwException("Variable " + astVariableDeclaration.identifier.identifier + " has already been declared");
        } else {
            //Variable and initialisation can be split so expression can be null
            if (astVariableDeclaration.expression != null) {
                visit(astVariableDeclaration.expression);

                //If an array is being declared of type auto (auto[]), change type
                if (astVariableDeclaration.type instanceof Array && expressionType instanceof Array) {
                    if ("auto".equals(((Array) astVariableDeclaration.type).arrayType.lexeme)) {
                        /*
                            Case when declaring array of type auto (auto[]) - changes type to type of array

                            Example:

                            let x[3]:auto = { 3, 4, 5 }
                            (not let x: auto = { 3, 4, 5 })
                         */

                        declaredType = new Array(-1, ((Array) expressionType).arrayType);
                    } else {
                        if (!expressionType.lexeme.equals(astVariableDeclaration.type.lexeme)) {
                            throwException("Cannot assign expression of type " + expressionType.lexeme + " to a variable of type " + astVariableDeclaration.type.lexeme);
                        }
                    }
                } else {
                    if ("auto".equals(astVariableDeclaration.type.lexeme)) {
                        /*
                            Resolves auto type when declaring a variable with type auto

                            Examples:

                            let x:auto = { 3, 4, 5 }
                            (not let x[3]: auto = { 3, 4, 5 })

                            or

                            let x: auto = 3;
                         */

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

    /**
     * ASTWhile node semantic visitor
     * @param astWhile node to visit
     */
    @Override
    public void visit(ASTWhile astWhile) throws SemanticException {
        visit(astWhile.conditionExpression);

        //Verifies that the while condition is boolean
        if (!"bool".equals(expressionType.lexeme)) {
            throwException("Condition expression requires bool type");
        }

        visit(astWhile.loopedBlock);
    }

    /**
     * ASTExpression node semantic visitor
     * @param astExpression node to visit
     */
    @Override
    public void visit(ASTExpression astExpression) throws SemanticException {
        /*
            Note that children of the ASTIdentifier node
            ASTStruct... and ASTArrayIndexIdentifier) MUST be placed above ASTIdentifier
         */

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

    /**
     * ASTBinaryOperator node semantic visitor
     * @param operator node to visit
     */
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

    /**
     * Checks whether type is primitive (not array and not struct)
     * @param type type to check
     * @return true if the type is primitive
     */
    private boolean isPrimitive(String type) {
        List<String> primitiveTypes = Arrays.asList("int", "float", "string", "bool", "char");
        return primitiveTypes.contains(type);
    }

    /**
     * Verifies that type1 and type2 contain either one of the two specified literals
     *
     * Example:
     *
     * Verifying that;
     *
     * variable1 is of type float and variable2 is of type int
     * OR
     * variable1 is of type int and variable2 is of type float
     *
     * @param typeLiteral first type required
     * @param typeLiteral2 second type required
     * @param type1 first variable type
     * @param type2 second variable type
     * @return true if variable types correspond to the opposite of each other (and match the type literals specified)
     */
    private boolean checkSymmetrical(String typeLiteral, String typeLiteral2, String type1, String type2) {
        return (typeLiteral.equals(type1) && typeLiteral2.equals(type2)) || (typeLiteral2.equals(type1) && typeLiteral.equals(type2));
    }

    /**
     * Verifies that either type1 or type2 contains the specified type
     * @param typeLiteral type required
     * @param type1 first variable type
     * @param type2 second variable type
     * @return true if either type1 or type2 matches the required type
     */
    private boolean checkAny(String typeLiteral, String type1, String type2) {
        return typeLiteral.equals(type1) || typeLiteral.equals(type2);
    }

    /**
     * Verifies that both type1 and type2 contain the specified type
     * @param typeLiteral type required
     * @param type1 first variable type
     * @param type2 second variable type
     * @return true if both type1 and type2 match the required type
     */
    private boolean checkBoth(String typeLiteral, String type1, String type2) {
        return typeLiteral.equals(type1) && typeLiteral.equals(type2);
    }

    /**
     * Checks types for addition
     * @param type1 first variable type
     * @param type2 second variable type
     * @return type of operation
     */
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

    /**
     * Checks types for most mathematical operators (*, -, /)
     * @param operatorSymbol operator (for exception output)
     * @param type1 first variable type
     * @param type2 second variable type
     * @return type of operation
     */
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

    /**
     * Checks type for equality operators (==, !=)
     * @param type1 first variable type
     * @param type2 second variable type
     * @return type of operation
     */
    private Type checkTypesEquality(String type1, String type2) throws SemanticException {
        if (isPrimitive(type1) && isPrimitive(type2)) {
            if (checkSymmetrical("int", "float", type1, type2) || type1.equals(type2)) {
                return Type.BOOL;
            }
        }

        throwException("Incomparable types " + type1 + " and " + type2);
        return Type.BOOL;
    }

    /**
     * Checks type of comparison operators (<, >, <=, >=)
     * @param type1 first variable type
     * @param type2 second variable type
     * @return type of operation
     */
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

    /**
     * Checks type of logic operators (and, or)
     * @param type1 first variable type
     * @param type2 second variable type
     * @return type of operation
     */
    private Type checkTypesLogic(String type1, String type2) throws SemanticException {
        if (!checkBoth("bool", type1, type2)) {
            throwException("Bad operand types " + type1 + " and " + type2);
        }

        return Type.BOOL;
    }

    /**
     * ASTFunctionCall node semantic visitor
     * @param astFunctionCall node to visit
     */
    @Override
    public void visit(ASTFunctionCall astFunctionCall) throws SemanticException {
        StringBuilder stringBuilder = new StringBuilder(astFunctionCall.identifier.identifier);

        /*
            Need to loop again to get the types
            (to create the identifier, since it needs to contain the types due to function overloading)
        */
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
                    throwException("Incorrect parameter type, required " + declaredFunctionParameterType + ", got " + expressionType.lexeme);
                }
            }

            expressionType = declaredFunction.returnType;
        } else {
            //If declaredFunction is null, then the function has not been declared
            throwException("Cannot resolve function " + astFunctionCall.identifier.identifier);
        }
    }

    /**
     * ASTIdentifier node semantic visitor
     * @param astIdentifier node to visit
     */
    @Override
    public void visit(ASTIdentifier astIdentifier) throws SemanticException {
        Type identifierType = variableSymbolTable.lookupType(astIdentifier.identifier);

        if (identifierType == null) {
            throwException("Cannot resolve identifier " + astIdentifier.identifier);
        } else {
            expressionType = identifierType;
        }
    }

    /**
     * ASTArrayIndexIdentifier node semantic visitor
     * @param astArrayIndexIdentifier node to visit
     */
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

    /**
     * ASTLiteral node semantic visitor
     * @param astLiteral node to visit
     */
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

    /**
     * ASTArrayLiteral node semantic visitor
     * @param astArrayLiteral node to visit
     */
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
                arrayType = new Array (size, expressionType);
            }
        }

        expressionType = arrayType;
    }

    /**
     * ASTUnary node semantic visitor
     * @param astUnary node to visit
     */
    @Override
    public void visit(ASTUnary astUnary) throws SemanticException {
        visit(astUnary.expression);

        if (astUnary.unaryType == TokenType.SUB) {
            //Unary minus can only be used with float and int types
            if (!("float".equals(expressionType.lexeme) || "int".equals(expressionType.lexeme))) {
                throwException("Unary '-' can only be used with float and integer types");
            }
        } else {
            //Not can only be used with boolean types
            if (!"bool".equals(expressionType.lexeme)) {
                throwException("Not can only be used with boolean types");
            }
        }
    }

    /**
     * ASTStruct node semantic visitor
     * @param astStruct node to visit
     */
    @Override
    public void visit(ASTStruct astStruct) throws SemanticException {
        isStruct = true;

        //Prevents two struct with the same name
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

    /**
     * ASTStructVariableSelector node semantic visitor
     * @param astStructVariableSelector node to visit
     */
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
            throwException(type.lexeme + " is not a struct type");
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

    /**
     * ASTStructFunctionSelector node semantic visitor
     * @param astStructFunctionSelector node to visit
     */
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
            throwException(type.lexeme + " is not a struct type");
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