package semantics;

import lexer.TokenType;
import lexer.Type;
import parser.*;
import visitors.ASTVisitor;

public class SemanticVisitor implements ASTVisitor {
    private final VariableSymbolTable variableSymbolTable;
    private final FunctionSymbolTable functionSymbolTable;
    private final ASTProgram program;
    private Type expressionType = null;
    private Type returnTypeOfCurrentFunction = null;
    private boolean hasReturn = false;

    public SemanticVisitor(ASTProgram program) {
        variableSymbolTable = new VariableSymbolTable();
        functionSymbolTable = new FunctionSymbolTable();
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
            visit ((ASTAssignment) statement);
        } else if (statement instanceof ASTBlock) {
            visit ((ASTBlock) statement);
        } else if (statement instanceof ASTFor) {
            visit ((ASTFor) statement);
        } else if (statement instanceof ASTFunctionDeclaration) {
            visit ((ASTFunctionDeclaration) statement);
        } else if (statement instanceof ASTIf) {
            visit ((ASTIf) statement);
        } else if (statement instanceof ASTPrint) {
            visit ((ASTPrint) statement);
        } else if (statement instanceof ASTReturn) {
            visit ((ASTReturn) statement);
        } else if (statement instanceof ASTVariableDeclaration) {
            visit ((ASTVariableDeclaration) statement);
        } else if (statement instanceof ASTWhile) {
            visit ((ASTWhile) statement);
        } else {
            throwException("Unknown statement node");
        }
    }

    @Override
    public void visit(ASTAssignment astAssignment) throws SemanticException {
        Type type = variableSymbolTable.lookupType(astAssignment.identifier.identifier);

        if (type != null) {
            visit(astAssignment.expression);

            if (!type.lexeme.equals(expressionType.lexeme)) {
                if (!("int".equals(expressionType.lexeme) && "float".equals(type.lexeme))) {
                    throwException("Cannot assign an expression with type " + expressionType.lexeme + " to a variable of type " + type.lexeme);
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

        if (functionSymbolTable.lookup(astFunctionDeclaration.functionName.identifier) != null) {
            throwException("Function " + astFunctionDeclaration.functionName.identifier + " has already been defined");
        }

        hasReturn = false;
        variableSymbolTable.push();

        for (ASTParameter parameter : astFunctionDeclaration.parameterList) {
            if (variableSymbolTable.lookupType(parameter.identifier.identifier) != null) {
                throwException("Variable " + parameter.identifier.identifier + " has already been defined");
            }

            variableSymbolTable.insert(parameter.identifier.identifier, parameter.type);
        }

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

            if (!returnTypeOfCurrentFunction.lexeme.equals(expressionType.lexeme)) {
                if (!("float".equals(returnTypeOfCurrentFunction.lexeme) && "int".equals(expressionType.lexeme))) {
                    throwException("Returning type " + expressionType.lexeme + ", required: " + returnTypeOfCurrentFunction.lexeme);
                }
            }
        }
    }

    @Override
    public void visit(ASTVariableDeclaration astVariableDeclaration) throws SemanticException {
        Type declaredType = astVariableDeclaration.type;

        if (variableSymbolTable.lookupType(astVariableDeclaration.identifier.identifier) != null) {
            throwException("Variable " + astVariableDeclaration.identifier.identifier + " has already been declared");
        } else {
            if (astVariableDeclaration.expression != null) {
                visit(astVariableDeclaration.expression);

                if (!expressionType.lexeme.equals(astVariableDeclaration.type.lexeme)) {
                    if (!("int".equals(expressionType.lexeme) && "float".equals(astVariableDeclaration.type.lexeme))) {
                        throwException("Cannot assign expression of type " + expressionType.lexeme + " to a variable of type " + astVariableDeclaration.type.lexeme);
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
        } else if (astExpression instanceof ASTIdentifier) {
            visit((ASTIdentifier) astExpression);
        } else if (astExpression instanceof ASTLiteral) {
            visit((ASTLiteral) astExpression);
        } else if (astExpression instanceof ASTUnary) {
            visit((ASTUnary) astExpression);
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
                expressionType = checkTypes1(operator.operator.tokenType, type1, type2);
                break;
            case CMP:
            case NE:
                expressionType = checkTypes2(type1, type2);
                break;
            case GT:
            case LT:
            case GTE:
            case LTE:
                expressionType = checkTypes3(type1, type2);
                break;
            case OR:
            case AND:
                expressionType = checkTypes4(type1, type2);
                break;
        }
    }

    private Type checkTypesAdd(String type1, String type2) throws SemanticException {
        if ("bool".equals(type1) || "bool".equals(type2)) {
            throwException("Operator '+' cannot be applied to " + type1 + " and " + type2);
        }

        Type typeToReturn = null;

        if (("int".equals(type1) && "float".equals(type2)) || ("float".equals(type1) && "int".equals(type2))) {
            typeToReturn = Type.FLOAT;
        } else if (("string".equals(type1) && "int".equals(type2)) || ("int".equals(type1) && "string".equals(type2))) {
            typeToReturn = Type.STRING;
        } else if (("string".equals(type1) && "float".equals(type2)) || ("float".equals(type1) && "string".equals(type2))) {
            typeToReturn = Type.STRING;
        } else if (type1.equals(type2)) {
            switch (type1) {
                case "int":
                    typeToReturn = Type.INTEGER;
                    break;
                case "float":
                    typeToReturn = Type.FLOAT;
                    break;
                case "string":
                    typeToReturn = Type.STRING;
                    break;
                default:
                    throwException("Error has occurred - type not recognised");
            }
        } else {
            throwException("Error has occurred - type not recognised");
        }

        return typeToReturn;
    }

    private Type checkTypes1(TokenType operatorSymbol, String type1, String type2) throws SemanticException {
        if ("bool".equals(type1) || "bool".equals(type2) || "string".equals(type1) || "string".equals(type2)) {
            throwException("Operator " + operatorSymbol.toString() + " cannot be applied to " + type1 + " and " + type2);
        }

        Type typeToReturn = null;

        if ("int".equals(type1) && "float".equals(type2) || "float".equals(type1) && "int".equals(type2)) {
            typeToReturn = Type.FLOAT;
        } else if (type1.equals(type2)) {
            if ("int".equals(type1)) {
                typeToReturn = Type.INTEGER;
            } else if ("float".equals(type1)) {
                typeToReturn = Type.FLOAT;
            } else {
                throwException("Error has occurred: type not recognised");
            }
        } else {
            throwException("Error has occurred: type not recognised");
        }

        return typeToReturn;
    }

    private Type checkTypes2(String type1, String type2) throws SemanticException {
        if ("int".equals(type1) && "float".equals(type2) || "float".equals(type1) && "int".equals(type2)) {
            return Type.BOOL;
        } else if (!type1.equals(type2)) {
            throwException("Incomparable types " + type1 + " and " + type2);
        }

        return Type.BOOL;
    }

    private Type checkTypes3(String type1, String type2) throws SemanticException {
        if ("bool".equals(type1) || "bool".equals(type2) || "string".equals(type1) || "string".equals(type2)) {
            throwException("Incomparable types " + type1 + " and " + type2);
        }

        return Type.BOOL;
    }

    private Type checkTypes4(String type1, String type2) throws SemanticException {
        if (!("bool".equals(type1) && "bool".equals(type2))) {
            throwException("Bad operand types " + type1 + " and " + type2);
        }

        return Type.BOOL;
    }

    @Override
    public void visit(ASTFunctionCall astFunctionCall) throws SemanticException {
        ASTFunctionDeclaration declaredFunction = functionSymbolTable.lookup(astFunctionCall.identifier.identifier);

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
}