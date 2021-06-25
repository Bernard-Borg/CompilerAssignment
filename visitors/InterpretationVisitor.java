package visitors;

import lexer.*;
import parser.*;
import semantics.FunctionSymbolTable;
import semantics.TypeValuePair;
import semantics.VariableSymbolTable;

import java.util.ArrayList;
import java.util.List;

public class InterpretationVisitor implements ASTVisitor {
    private final VariableSymbolTable variableSymbolTable;
    private final FunctionSymbolTable functionSymbolTable;
    private final ASTProgram program;
    private Type expressionType = null;
    private Object expressionValue = null;
    private boolean hasReturn = false;
    private Type returnTypeOfCurrentFunction = null;

    public InterpretationVisitor(ASTProgram program) {
        variableSymbolTable = new VariableSymbolTable();
        functionSymbolTable = new FunctionSymbolTable();
        this.program = program;
    }

    public void interpret() throws Exception {
        visit(program);
    }

    @Override
    public void visit(ASTProgram astProgram) throws Exception {
        //Global scopes
        variableSymbolTable.push();
        functionSymbolTable.push();

        for (ASTStatement statement : astProgram.statements) {
            visit(statement);
        }
    }

    @Override
    public void visit(ASTStatement statement) throws Exception {
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
            visit((ASTWhile) statement);
        }
    }

    @Override
    public void visit(ASTAssignment astAssignment) throws Exception {
        visit(astAssignment.expression);

        Type type = variableSymbolTable.lookupType(astAssignment.identifier.identifier);

        if ("int".equals(expressionType.lexeme) && "float".equals(type.lexeme)) {
            expressionValue = ((Integer) expressionValue).floatValue();
            expressionType = Type.FLOAT;
        }

        variableSymbolTable.changeValue(astAssignment.identifier.identifier, expressionValue);
    }

    @Override
    public void visit(ASTBlock astBlock) throws Exception {
        variableSymbolTable.push();
        functionSymbolTable.push();

        hasReturn = false;

        for (ASTStatement statement : astBlock.statements) {
            if (statement instanceof ASTReturn) {
                hasReturn = true;
            }

            visit(statement);
        }

        variableSymbolTable.pop();
        functionSymbolTable.pop();
    }

    @Override
    public void visit(ASTFor astFor) throws Exception {
        variableSymbolTable.push();
        //No need to do functionSymbolTable.push() since you can't declare a function in for

        if (astFor.variableDeclaration != null) {
            visit(astFor.variableDeclaration);
        }

        while (true) {
            visit(astFor.conditionExpression);

            if (!((Boolean) expressionValue)) {
                break;
            }

            visit(astFor.loopedBlock);

            if (astFor.assignment != null) {
                visit(astFor.assignment);
            }
        }

        variableSymbolTable.pop();
    }

    @Override
    public void visit(ASTFunctionDeclaration astFunctionDeclaration) {
        functionSymbolTable.registerFunction(astFunctionDeclaration);
    }

    @Override
    public void visit(ASTIf astIf) throws Exception {
        visit(astIf.conditionExpression);

        if ((Boolean) expressionValue) {
            visit(astIf.trueBlock);
        } else {
            boolean tempHasReturn = hasReturn;

            if (astIf.falseBlock != null) {
                visit(astIf.falseBlock);

                hasReturn = hasReturn && tempHasReturn;
            } else {
                hasReturn = false;
            }
        }
    }

    @Override
    public void visit(ASTPrint astPrint) throws Exception {
        visit(astPrint.expression);
        System.out.println(expressionValue.toString());
    }

    @Override
    public void visit(ASTReturn astReturn) throws Exception {
        visit(astReturn.expression);

        if ("float".equals(returnTypeOfCurrentFunction.lexeme) && "int".equals(expressionType.lexeme)) {
            expressionValue = ((Integer) expressionValue).floatValue();
        }
    }

    @Override
    public void visit(ASTVariableDeclaration astVariableDeclaration) throws Exception {
        Type variableType = astVariableDeclaration.type;

        if (astVariableDeclaration.expression != null) {
            visit(astVariableDeclaration.expression);

            if ("int".equals(expressionType.lexeme) && "float".equals(astVariableDeclaration.type.lexeme)) {
                expressionValue = ((Integer) expressionValue).floatValue();
                expressionType = Type.FLOAT;
                variableType = expressionType;
            }
        } else {
            expressionValue = null;
        }

        variableSymbolTable.insert(astVariableDeclaration.identifier.identifier, variableType, expressionValue);
    }

    @Override
    public void visit(ASTWhile astWhile) throws Exception {
        while (true) {
            visit(astWhile.conditionExpression);

            if (!(Boolean) expressionValue) {
                break;
            }

            visit(astWhile.loopedBlock);
        }

        visit(astWhile.loopedBlock);
    }

    @Override
    public void visit(ASTExpression astExpression) throws Exception {
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
        } else if (astExpression instanceof ASTArrayLiteral) {
            visit ((ASTArrayLiteral) astExpression);
        }
    }

    @Override
    public void visit(ASTBinaryOperator operator) throws Exception {
        visit(operator.expression1);
        String type1 = expressionType.lexeme;
        Object value1 = expressionValue;

        visit(operator.expression2);
        String type2 = expressionType.lexeme;
        Object value2 = expressionValue;

        switch (operator.operator.tokenType) {
            case ADD:
                expressionType = add(type1, type2, value1, value2);
                break;
            case SUB:
                expressionType = subtract(type1, type2, value1, value2);
                break;
            case MUL:
                expressionType = multiply(type1, type2, value1, value2);
                break;
            case DIV:
                expressionType = divide(type1, type2, value1, value2);
                break;
            case CMP:
                expressionType = compare(type1, type2, value1, value2);
                break;
            case NE:
                expressionType = compare(type1, type2, value1, value2);
                expressionValue = !(Boolean) expressionValue;
                break;
            case GT:
                expressionType = greaterThan(type1, type2, value1, value2);
                break;
            case LT:
                expressionType = lessThan(type1, type2, value1, value2);
                break;
            case GTE:
                expressionType = lessThan(type1, type2, value1, value2);
                expressionValue = !(Boolean) expressionValue;
                break;
            case LTE:
                expressionType = greaterThan(type1, type2, value1, value2);
                expressionValue = !(Boolean) expressionValue;
                break;
            case OR:
                expressionType = or(value1, value2);
                break;
            case AND:
                expressionType = and(value1, value2);
                break;
        }
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

    private Type add(String type1, String type2, Object value1, Object value2) {
        Type typeToReturn = null;

        if ("int".equals(type1) && "float".equals(type2)) {
            typeToReturn = Type.FLOAT;
            expressionValue = ((Integer) value1).floatValue() + (Float) value2;
        } else if ("float".equals(type1) && "int".equals(type2)) {
            typeToReturn = Type.FLOAT;
            expressionValue = (Float) value1 + ((Integer) value2).floatValue();
        } else if (checkSymmetrical("int", "string", type1, type2)) {
            typeToReturn = Type.STRING;
            expressionValue = value1.toString() + value2.toString();
        } else if (checkSymmetrical("float", "string", type1, type2)) {
            typeToReturn = Type.STRING;
            expressionValue = value1.toString() + value2.toString();
        } else if (checkBoth("int", type1, type2)) {
            typeToReturn = Type.INTEGER;
            expressionValue = (Integer) value1 + (Integer) value2;
        } else if (checkBoth("float", type1, type2)) {
            typeToReturn = Type.FLOAT;
            expressionValue = (Float) value1 + (Float) value2;
        } else if (checkBoth("string", type1, type2)) {
            typeToReturn = Type.STRING;
            expressionValue = value1.toString() + value2.toString();
        }

        return typeToReturn;
    }

    private Type subtract(String type1, String type2, Object value1, Object value2) {
        Type typeToReturn = null;

        if ("int".equals(type1) && "float".equals(type2)) {
            typeToReturn = Type.FLOAT;
            expressionValue = ((Integer) value1).floatValue() - (Float) value2;
        } else if ("float".equals(type1) && "int".equals(type2)) {
            typeToReturn = Type.FLOAT;
            expressionValue = (Float) value1 - ((Integer) value2).floatValue();
        } else if (checkBoth("int", type1, type2)) {
            typeToReturn = Type.INTEGER;
            expressionValue = (Integer) value1 - (Integer) value2;
        } else if (checkBoth("float", type1, type2)) {
            typeToReturn = Type.FLOAT;
            expressionValue = (Float) value1 - (Float) value2;
        }

        return typeToReturn;
    }

    private Type multiply(String type1, String type2, Object value1, Object value2) {
        Type typeToReturn = null;

        if ("int".equals(type1) && "float".equals(type2)) {
            typeToReturn = Type.FLOAT;
            expressionValue = ((Integer) value1).floatValue() * (Float) value2;
        } else if ("float".equals(type1) && "int".equals(type2)) {
            typeToReturn = Type.FLOAT;
            expressionValue = (Float) value1 * ((Integer) value2).floatValue();
        } else if (checkBoth("int", type1, type2)) {
            typeToReturn = Type.INTEGER;
            expressionValue = (Integer) value1 * (Integer) value2;
        } else if (checkBoth("float", type1, type2)) {
            typeToReturn = Type.FLOAT;
            expressionValue = (Float) value1 * (Float) value2;
        }

        return typeToReturn;
    }

    private Type divide(String type1, String type2, Object value1, Object value2) {
        Type typeToReturn = null;

        if ("int".equals(type1) && "float".equals(type2)) {
            typeToReturn = Type.FLOAT;
            expressionValue = ((Integer) value1).floatValue() / (Float) value2;
        } else if ("float".equals(type1) && "int".equals(type2)) {
            typeToReturn = Type.FLOAT;
            expressionValue = (Float) value1 / ((Integer) value2).floatValue();
        } else if (checkBoth("int", type1, type2)) {
            typeToReturn = Type.INTEGER;
            expressionValue = (Integer) value1 / (Integer) value2;
        } else if (checkBoth("float", type1, type2)) {
            typeToReturn = Type.FLOAT;
            expressionValue = (Float) value1 / (Float) value2;
        }

        return typeToReturn;
    }

    private Type compare(String type1, String type2, Object value1, Object value2) {
        if ("int".equals(type1) && "float".equals(type2)){
            expressionValue = value2.equals(((Integer) value1).floatValue());
        } else if ("float".equals(type1) && "int".equals(type2)) {
            expressionValue = value1.equals(((Integer) value2).floatValue());
        } else if (type1.equals(type2)) {
            expressionValue = value1.equals(value2);
        }

        return Type.BOOL;
    }

    private Type greaterThan(String type1, String type2, Object value1, Object value2) {
        if ("int".equals(type1) && "float".equals(type2)){
            expressionValue = ((Float) value2 < ((Integer) value1).floatValue());
        } else if ("float".equals(type1) && "int".equals(type2)) {
            expressionValue = ((Float) value1 > ((Integer) value2).floatValue());
        } else if (checkBoth("int", type1, type2)) {
            expressionValue = (Integer) value1 > (Integer) value2;
        } else if (checkBoth("float", type1, type2)) {
            expressionValue = (Float) value1 > (Float) value2;
        } else if (checkAny("string", type1, type2)) {
            expressionValue = value1.toString().compareTo(value2.toString()) > 0;
        } else if (checkBoth("char", type1, type2)) {
            expressionValue = value1.toString().charAt(0) > value2.toString().charAt(0);
        }

        return Type.BOOL;
    }

    private Type lessThan(String type1, String type2, Object value1, Object value2) {
        if ("int".equals(type1) && "float".equals(type2)){
            expressionValue = (Float) value2 > ((Integer) value1).floatValue();
        } else if ("float".equals(type1) && "int".equals(type2)) {
            expressionValue = (Float) value1 < ((Integer) value2).floatValue();
        } else if (checkBoth("int", type1, type2)) {
            expressionValue = (Integer) value1 < (Integer) value2;
        } else if (checkBoth("float", type1, type2)) {
            expressionValue = (Float) value1 < (Float) value2;
        } else if (checkAny("string", type1, type2)) {
            expressionValue = value1.toString().compareTo(value2.toString()) < 0;
        } else if (checkBoth("char", type1, type2)) {
            expressionValue = value1.toString().charAt(0) < value2.toString().charAt(0);
        }

        return Type.BOOL;
    }

    private Type and(Object value1, Object value2) {
        //Is it guaranteed to be boolean so no if statements needed
        expressionValue = (Boolean) value1 && (Boolean) value2;
        return Type.BOOL;
    }

    private Type or(Object value1, Object value2) {
        //Is it guaranteed to be boolean so no if statements needed
        expressionValue = (Boolean) value1 || (Boolean) value2;
        return Type.BOOL;
    }

    @Override
    public void visit(ASTFunctionCall astFunctionCall) throws Exception {
        ASTFunctionDeclaration declaredFunction = functionSymbolTable.lookup(astFunctionCall.identifier.identifier);
        variableSymbolTable.push();

        for (int i = 0; i < declaredFunction.parameterList.size(); i++) {
            visit(astFunctionCall.parameters.get(i));

            if ("int".equals(expressionType.lexeme) && "float".equals(declaredFunction.parameterList.get(i).type.lexeme)) {
                expressionValue = ((Integer) expressionValue).floatValue();
                expressionType = Type.FLOAT;
            }

            variableSymbolTable.insert(declaredFunction.parameterList.get(i).identifier.identifier, expressionType, expressionValue);
        }

        /*This is used since the function return type needs to always show the current return type,
        if another function with a different return type is called, the return type of the current function
        would switch to that return type - therefore this allows functions to be called inside other functions*/
        Type previousReturnType = returnTypeOfCurrentFunction;

        returnTypeOfCurrentFunction = declaredFunction.returnType;

        visit(declaredFunction.functionBlock);

        returnTypeOfCurrentFunction = previousReturnType;
        expressionType = declaredFunction.returnType;
        variableSymbolTable.pop();
    }

    @Override
    public void visit(ASTIdentifier astIdentifier) throws Exception {
        TypeValuePair variable = variableSymbolTable.lookup(astIdentifier.identifier);

        expressionType = variable.type;

        if (variable.value != null) {
            expressionValue = variable.value;
        } else {
            throw new Exception ("Variable " + astIdentifier.identifier + " might have not been initialised");
        }
    }

    @Override
    public void visit(ASTLiteral astLiteral) {
        if ("bool".equals(astLiteral.type)) {
            expressionType = Type.BOOL;

            if (astLiteral.token.tokenType == TokenType.TRUE) {
                expressionValue = true;
            } else {
                expressionValue = false;
            }
        } else if ("string".equals(astLiteral.type)) {
            expressionType = Type.STRING;
            expressionValue = ((Word) astLiteral.token).lexeme;
        } else if ("int".equals(astLiteral.type)) {
            expressionType = Type.INTEGER;
            expressionValue = ((WholeNumber) astLiteral.token).value;
        } else if ("float".equals(astLiteral.type)) {
            expressionType = Type.FLOAT;
            expressionValue = ((Real) astLiteral.token).value;
        } else if ("char".equals(astLiteral.type)) {
            expressionType = Type.CHAR;
            expressionValue = ((Word) astLiteral.token).lexeme;
        }
    }

    @Override
    public void visit(ASTArrayLiteral astArrayLiteral) throws Exception {
        int size = astArrayLiteral.arrayMembers.size();
        visit(astArrayLiteral.arrayMembers.get(0));
        Array arrayType = new Array(size, expressionType);

        List<Object> arrayValues = new ArrayList<>();

        for(ASTExpression expression : astArrayLiteral.arrayMembers) {
            visit(expression);

            //Converts integers to floats in a float array literal
            if ("float".equals(arrayType.arrayType.lexeme) && "int".equals(expressionType.lexeme)) {
                expressionValue = ((Integer) expressionValue).floatValue();
            }

            arrayValues.add(expressionValue);
        }

        expressionType = arrayType;
        expressionValue = arrayValues;
    }

    @Override
    public void visit(ASTUnary astUnary) throws Exception {
        visit(astUnary.expression);

        if (astUnary.unaryType == TokenType.SUB) {
            if ("float".equals(expressionType.lexeme)) {
                expressionValue = -Float.parseFloat(expressionValue.toString());
            } else {
                expressionValue = -Integer.parseInt(expressionValue.toString());
            }
        } else {
            expressionValue = !(Boolean) expressionValue;
        }
    }
}
