package parser;

import lexer.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("rawtypes")
public class Parser {
    private final Lexer lexer;
    private Token lookahead;
    private boolean lookaheadUsed = true;

    //Used to prevent variable declarations with complex types in structs
    private boolean isStruct = false;

    //Stores the structs which exist (to change identifier to type)
    private final Set<String> definedStructs;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        definedStructs = new HashSet<>();
    }

    private void updateLookahead() throws Exception {
        if (lookaheadUsed) {
            lookahead = lexer.getNextToken();
            lookaheadUsed = false;
        }
    }

    private void throwException(String message) throws ParseException {
        throw new ParseException(message + " at line " + lexer.lineNumber);
    }

    //Checks whether the current lookahead is of the required token type
    private boolean isLookahead(TokenType tokenType) {
        if (lookahead == null)
            return false;

        return tokenType == lookahead.tokenType;
    }

    //Matches next token to the required token, throws exception if they don't match
    private void assertToken(TokenType tokenType) throws Exception {
        updateLookahead();

        if (lookahead == null) {
            throwException("Unexpected end of file, was expecting " + tokenType);
        } else if (lookahead.tokenType != tokenType) {
            throwException("Expected " + tokenType + ", got " + lookahead.tokenType);
        } else {
            lookaheadUsed = true;
        }
    }

    public ASTProgram parseProgram() throws Exception {
        List<ASTStatement> statementList = new ArrayList<>();
        ASTStatement statement;

        while (true) {
            statement = parseStatement();

            if (statement == null) {
                break;
            }

            statementList.add(statement);
        }

        return new ASTProgram(statementList);
    }

    private ASTStatement parseStatement() throws Exception {
        updateLookahead();

        if (lookahead == null) {
            return null;
        }

        switch (lookahead.tokenType) {
            case LET:
                ASTVariableDeclaration variableDeclaration = parseVariableDeclaration();
                assertToken(TokenType.SEMICOLON);
                return variableDeclaration;
            case IDENTIFIER:
                if (definedStructs.contains(((Word) lookahead).lexeme)) {
                    return parseFunctionDeclaration();
                }

                ASTAssignment assignment = parseAssignment();
                assertToken(TokenType.SEMICOLON);
                return assignment;
            case PRINT:
                ASTPrint printStatement = parsePrintStatement();
                assertToken(TokenType.SEMICOLON);
                return printStatement;
            case IF:
                return parseIfStatement();
            case FOR:
                return parseForStatement();
            case WHILE:
                return parseWhileStatement();
            case RETURN:
                ASTReturn returnStatement = parseReturnStatement();
                assertToken(TokenType.SEMICOLON);
                return returnStatement;
            case TYPE:
                return parseFunctionDeclaration();
            case OPENCURLYBRACKET:
                return parseBlock();
            case STRUCT:
                return parseStruct();
            default:
                throwException("Unexpected " + lookahead.tokenType + ", expecting statement");
                return null;
        }
    }

    private ASTPrint parsePrintStatement() throws Exception {
        assertToken(TokenType.PRINT);
        ASTExpression expression = parseExpression();
        return new ASTPrint(expression);
    }

    private ASTExpression parseExpression() throws Exception {
        ASTExpression simpleExpression1 = parseSimpleExpression();
        List list = new ArrayList();
        TokenType relationalOperator;
        ASTExpression simpleExpression2;

        while (true) {
            updateLookahead();

            if (lookahead == null) {
                throwException("Unexpected end of file while parsing expression");
            }

            if (isRelationalOperator(lookahead)) {
                lookaheadUsed = true;
                relationalOperator = lookahead.tokenType;
                simpleExpression2 = parseSimpleExpression();
                list.add(relationalOperator);
                list.add(simpleExpression2);
            } else {
                break;
            }
        }

        return resolveList(simpleExpression1, list);
    }

    private ASTExpression resolveList(ASTExpression firstExpression, List list) {
        if (list.isEmpty()) {
            return firstExpression;
        } else {
            ASTBinaryOperator operator = null;
            ASTBinaryOperator previousOperator = null;

            for (int i = 0; i < list.size(); i += 2) {
                if (previousOperator == null) {
                    operator = new ASTBinaryOperator(firstExpression, (ASTExpression) list.get(i + 1),
                            new Token((TokenType) list.get(i)));
                } else {
                    operator = new ASTBinaryOperator(previousOperator, (ASTExpression) list.get(i + 1),
                            new Token((TokenType) list.get(i)));
                }

                previousOperator = operator;
            }

            return operator;
        }
    }

    private boolean isRelationalOperator(Token token) {
        return token.tokenType == TokenType.GTE || token.tokenType == TokenType.GT ||
                token.tokenType == TokenType.LTE || token.tokenType == TokenType.LT ||
                token.tokenType == TokenType.CMP || token.tokenType == TokenType.NE;
    }

    private ASTExpression parseSimpleExpression() throws Exception {
        ASTExpression term1 = parseTerm();
        List list = new ArrayList();
        TokenType additiveOperator;
        ASTExpression term2;

        while (true) {
            updateLookahead();

            if (lookahead == null) {
                throwException("Unexpected end of file while parsing expression");
            }

            if (isAdditiveOperator(lookahead)) {
                lookaheadUsed = true;
                additiveOperator = lookahead.tokenType;
                term2 = parseTerm();
                list.add(additiveOperator);
                list.add(term2);
            } else {
                break;
            }
        }

        return resolveList(term1, list);
    }

    private boolean isAdditiveOperator(Token token) {
        return token.tokenType == TokenType.ADD || token.tokenType == TokenType.SUB || token.tokenType == TokenType.OR;
    }

    private ASTExpression parseTerm() throws Exception {
        ASTExpression factor1 = parseFactor();
        List list = new ArrayList();
        TokenType multiplicativeOperator;
        ASTExpression factor2;

        while (true) {
            updateLookahead();

            if (lookahead == null) {
                throwException("Unexpected end of file while parsing expression");
            }

            if (isMultiplicativeOperator(lookahead)) {
                lookaheadUsed = true;
                multiplicativeOperator = lookahead.tokenType;
                factor2 = parseFactor();
                list.add(multiplicativeOperator);
                list.add(factor2);
            } else {
                break;
            }
        }

        return resolveList(factor1, list);
    }

    private boolean isMultiplicativeOperator(Token token) {
        return token.tokenType == TokenType.MUL || token.tokenType == TokenType.DIV || token.tokenType == TokenType.AND;
    }

    private ASTStruct parseStruct() throws Exception {
        isStruct = true;
        List<ASTStatement> statementsList = new ArrayList<>();

        assertToken(TokenType.STRUCT);
        assertToken(TokenType.IDENTIFIER);

        ASTIdentifier structName = new ASTIdentifier((Word) lookahead);

        definedStructs.add(structName.identifier);

        assertToken(TokenType.OPENCURLYBRACKET);

        while (true) {
            updateLookahead();

            if (lookahead == null) {
                throwException("Unexpected end of file, expecting " + TokenType.LET + " or type");
            }

            if (isLookahead(TokenType.CLOSECURLYBRACKET)) {
                break;
            }

            if (isLookahead(TokenType.LET)) {
                statementsList.add(parseVariableDeclaration());
                assertToken(TokenType.SEMICOLON);
            } else if (isLookahead(TokenType.TYPE)) {
                statementsList.add(parseFunctionDeclaration());
            } else if (isLookahead(TokenType.IDENTIFIER)) {
                if (definedStructs.contains(((Word) lookahead).lexeme)) {
                    statementsList.add(parseFunctionDeclaration());
                } else {
                    throwException("Could not resolve identifier " + ((Word) lookahead).lexeme + " as a struct type");
                }
            } else {
                throwException("Got " + lookahead.tokenType + ", expecting " + TokenType.LET + " or type");
            }
        }

        assertToken(TokenType.CLOSECURLYBRACKET);
        isStruct = false;
        return new ASTStruct(structName, statementsList);
    }

    private ASTExpression parseFactor() throws Exception {
        updateLookahead();

        if (lookahead == null) {
            throwException("Unexpected end of file, was expecting an expression");
        }

        lookaheadUsed = true;

        switch (lookahead.tokenType) {
            case NUM:
            case STRING:
            case REAL:
            case TRUE:
            case FALSE:
            case CHAR:
                return new ASTLiteral(lookahead);
            case OPENCURLYBRACKET:
                lookaheadUsed = false;
                return parseArrayLiteral();
            case OPENROUNDBRACKET:
                lookaheadUsed = false;
                return parseSubExpression();
            case SUB:
            case NOT:
                return parseUnary();
            case IDENTIFIER:
                Token lookaheadTemp = lookahead;
                updateLookahead();

                if (isLookahead(TokenType.OPENROUNDBRACKET)) {
                    return parseFunctionCall(new ASTIdentifier((Word) lookaheadTemp));
                } else if (isLookahead(TokenType.OPENSQUAREBRACKET)) {
                    return parseArrayIndexIdentifier(new ASTIdentifier((Word) lookaheadTemp));
                } else if (isLookahead(TokenType.DOT)) {
                    return parseStructSelector(new ASTIdentifier((Word) lookaheadTemp));
                } else {
                    return new ASTIdentifier((Word) lookaheadTemp);
                }
            default:
                throwException("Unexpected " + lookahead.tokenType + ", expecting expression");
                return null;
        }
    }

    private ASTIdentifier parseAssignmentIdentifier() throws Exception {
        ASTIdentifier identifier = new ASTIdentifier((Word) lookahead);
        lookaheadUsed = true;

        updateLookahead();

        if (lookahead == null) {
            throwException("Unexpected end of file, was expecting " + TokenType.OPENSQUAREBRACKET + " or " + TokenType.EQ);
        }

        if (isLookahead(TokenType.OPENSQUAREBRACKET)) {
            identifier = parseArrayIndexIdentifier(identifier);
        } else if (isLookahead(TokenType.DOT)) {
            identifier = parseStructSelector(identifier);

            if (identifier instanceof ASTStructFunctionSelector) {
                throwException("Cannot assign value to struct function call");
            }
        }

        return identifier;
    }

    private ASTIdentifier parseDeclarationIdentifier() throws Exception {
        ASTIdentifier identifier = new ASTIdentifier((Word) lookahead);

        updateLookahead();

        if (lookahead == null) {
            throwException("Unexpected end of file, was expecting " + TokenType.OPENSQUAREBRACKET + " or " + TokenType.COLON);
        }

        if (isLookahead(TokenType.OPENSQUAREBRACKET)) {
            identifier = parseArrayIndexIdentifier(identifier);
        }

        return identifier;
    }

    private ASTArrayIndexIdentifier parseArrayIndexIdentifier(ASTIdentifier identifier) throws Exception {
        ASTExpression index;

        assertToken(TokenType.OPENSQUAREBRACKET);
        index = parseExpression();
        assertToken(TokenType.CLOSESQUAREBRACKET);

        return new ASTArrayIndexIdentifier(identifier, index);
    }

    private ASTIdentifier parseStructSelector(ASTIdentifier structIdentifier) throws Exception {
        ASTFunctionCall functionCall;

        assertToken(TokenType.DOT);
        assertToken(TokenType.IDENTIFIER);

        ASTIdentifier structMemberIdentifier = new ASTIdentifier((Word) lookahead);

        updateLookahead();

        if (isLookahead(TokenType.OPENROUNDBRACKET)) {
            functionCall = parseFunctionCall(structMemberIdentifier);
            return new ASTStructFunctionSelector(structIdentifier, functionCall);
        } else {
            return new ASTStructVariableSelector(structIdentifier, structMemberIdentifier);
        }
    }

    private ASTFunctionCall parseFunctionCall(ASTIdentifier functionIdentifier) throws Exception {
        assertToken(TokenType.OPENROUNDBRACKET);
        List<ASTExpression> parameters = null;

        updateLookahead();

        if (lookahead == null) {
            throwException("Unexpected end of file, was expecting a parameter or " + TokenType.CLOSEROUNDBRACKET);
        }

        if (isLookahead(TokenType.CLOSEROUNDBRACKET)) {
            lookaheadUsed = true;
        } else {
            parameters = parseExpressionList();
            assertToken(TokenType.CLOSEROUNDBRACKET);
        }

        return new ASTFunctionCall(functionIdentifier, parameters);
    }

    //Previously was parseActualParameters, changed name so that array literal can also use it
    private List<ASTExpression> parseExpressionList() throws Exception {
        List<ASTExpression> parameterList = new ArrayList<>();
        ASTExpression parameter = parseExpression();
        parameterList.add(parameter);

        while (true) {
            updateLookahead();

            if (lookahead == null) {
                throwException("Unexpected end of file, was expecting " + TokenType.COMMA);
            }

            if (isLookahead(TokenType.COMMA)) {
                lookaheadUsed = true;
                parameterList.add(parseExpression());
            } else {
                break;
            }
        }

        return parameterList;
    }

    private ASTArrayLiteral parseArrayLiteral() throws Exception {
        assertToken(TokenType.OPENCURLYBRACKET);
        updateLookahead();

        if (lookahead == null) {
            throwException("Unexpected end of file, was expecting expression or " + TokenType.CLOSECURLYBRACKET);
        }

        if (isLookahead(TokenType.CLOSECURLYBRACKET)) {
            throwException("Array literal cannot be empty");
            return null;
        } else {
            List<ASTExpression> expressions = parseExpressionList();
            assertToken(TokenType.CLOSECURLYBRACKET);
            return new ASTArrayLiteral(expressions);
        }
    }

    private ASTExpression parseSubExpression() throws Exception {
        assertToken(TokenType.OPENROUNDBRACKET);
        ASTExpression expression = parseExpression();
        assertToken(TokenType.CLOSEROUNDBRACKET);
        return expression;
    }

    private ASTUnary parseUnary() throws Exception {
        TokenType unaryType = lookahead.tokenType;
        ASTExpression expression = parseExpression();
        return new ASTUnary(unaryType, expression);
    }

    private ASTIf parseIfStatement() throws Exception {
        assertToken(TokenType.IF);
        assertToken(TokenType.OPENROUNDBRACKET);
        ASTExpression conditionExpression = parseExpression();
        assertToken(TokenType.CLOSEROUNDBRACKET);
        ASTBlock trueBlock = parseBlock();
        ASTBlock falseBlock = null;

        updateLookahead();

        if (isLookahead(TokenType.ELSE)) {
            lookaheadUsed = true;
            falseBlock = parseBlock();
        }

        return new ASTIf(conditionExpression, trueBlock, falseBlock);
    }

    private ASTFor parseForStatement() throws Exception {
        ASTVariableDeclaration variableDeclaration = null;
        ASTAssignment assignment = null;

        assertToken(TokenType.FOR);
        assertToken(TokenType.OPENROUNDBRACKET);

        updateLookahead();

        if (lookahead == null) {
            throwException("Unexpected end of file, expecting " + TokenType.LET + " or " + TokenType.SEMICOLON);
        }

        if (isLookahead(TokenType.LET)) {
            variableDeclaration = parseVariableDeclaration();
        }

        assertToken(TokenType.SEMICOLON);
        ASTExpression conditionExpression = parseExpression();
        assertToken(TokenType.SEMICOLON);

        updateLookahead();

        if (lookahead == null) {
            throwException("Unexpected end of file, expecting " + TokenType.IDENTIFIER + " or " + TokenType.CLOSEROUNDBRACKET);
        }

        if (isLookahead(TokenType.IDENTIFIER)) {
            assignment = parseAssignment();
        }

        assertToken(TokenType.CLOSEROUNDBRACKET);
        ASTBlock loopedBlock = parseBlock();

        return new ASTFor(variableDeclaration, conditionExpression, assignment, loopedBlock);
    }

    private ASTWhile parseWhileStatement() throws Exception {
        assertToken(TokenType.WHILE);
        assertToken(TokenType.OPENROUNDBRACKET);
        ASTExpression conditionExpression = parseExpression();
        assertToken(TokenType.CLOSEROUNDBRACKET);
        ASTBlock loopedBlock = parseBlock();

        return new ASTWhile(conditionExpression, loopedBlock);
    }

    private ASTBlock parseBlock() throws Exception {
        assertToken(TokenType.OPENCURLYBRACKET);

        List<ASTStatement> statementList = new ArrayList<>();
        ASTStatement statement;

        while(true) {
            updateLookahead();

            if (lookahead == null) {
                throwException("Unexpected end of file, was expecting " + TokenType.CLOSECURLYBRACKET);
            }

            if (isLookahead(TokenType.CLOSECURLYBRACKET)) {
                lookaheadUsed = true;
                break;
            } else {
                statement = parseStatement();
                statementList.add(statement);
            }
        }

        return new ASTBlock(statementList);
    }

    private ASTReturn parseReturnStatement() throws Exception {
        assertToken(TokenType.RETURN);
        ASTExpression expression = parseExpression();
        return new ASTReturn(expression);
    }

    private ASTFunctionDeclaration parseFunctionDeclaration() throws Exception {
        Type returnType = parseType(false, false);

        assertToken(TokenType.IDENTIFIER);
        ASTIdentifier functionName = new ASTIdentifier((Word) lookahead);

        assertToken(TokenType.OPENROUNDBRACKET);

        updateLookahead();

        if (lookahead == null) {
            throwException("Unexpected end of file, was expecting parameters or " + TokenType.CLOSEROUNDBRACKET);
        }

        List<ASTParameter> parameterList = null;

        if (isLookahead(TokenType.IDENTIFIER)) {
            parameterList = parseFormalParameters();
        }

        assertToken(TokenType.CLOSEROUNDBRACKET);

        ASTBlock functionBlock = parseBlock();
        return new ASTFunctionDeclaration(returnType, functionName, parameterList, functionBlock);
    }

    private List<ASTParameter> parseFormalParameters() throws Exception {
        List<ASTParameter> parameterList = new ArrayList<>();
        ASTParameter parameter = parseParameter();
        parameterList.add(parameter);

        while (true) {
            updateLookahead();

            if (lookahead == null) {
                throwException("Unexpected end of file, was expecting " + TokenType.COMMA);
            }

            if (isLookahead(TokenType.COMMA)) {
                lookaheadUsed = true;
                parameterList.add(parseParameter());
            } else {
                break;
            }
        }

        return parameterList;
    }

    private ASTParameter parseParameter() throws Exception {
        boolean isArray = false;

        assertToken(TokenType.IDENTIFIER);
        ASTIdentifier identifier = new ASTIdentifier((Word) lookahead);

        updateLookahead();

        if (lookahead == null) {
            throwException("Unexpected end of file, expecting " + TokenType.OPENSQUAREBRACKET + " or " + TokenType.COLON);
        }

        if (isLookahead(TokenType.OPENSQUAREBRACKET)) {
            lookaheadUsed = true;
            assertToken(TokenType.CLOSESQUAREBRACKET);
            isArray = true;
        }

        assertToken(TokenType.COLON);

        Type parameterType = parseType(isArray, false);

        return new ASTParameter(identifier, parameterType);
    }

    private ASTAssignment parseAssignment() throws Exception {
        ASTIdentifier identifier = parseAssignmentIdentifier();

        assertToken(TokenType.EQ);

        ASTExpression expression = parseExpression();

        return new ASTAssignment(identifier, expression);
    }

    private Type parseType(boolean isArray, boolean checkIfStruct) throws Exception {
        Type type = null;

        updateLookahead();

        if (isLookahead(TokenType.TYPE)) {
            type = (Type) lookahead;
            lookaheadUsed = true;
        } else if (isLookahead(TokenType.IDENTIFIER)) {
            if (checkIfStruct && isStruct) {
                throwException("Can not have complex type here");
            }

            if (definedStructs.contains(((Word) lookahead).lexeme)) {
                type = new Type(((Word) lookahead).lexeme, TokenType.COMPLEXTYPE);
                lookaheadUsed = true;
            } else {
                throwException("Could not resolve identifier " + ((Word) lookahead).lexeme + " as a struct type");
            }
        } else {
            throwException("Expected type, got " + lookahead.tokenType);
        }

        if (isArray) {
            return new Array(-1, type);
        } else {
            return type;
        }
    }

    private ASTVariableDeclaration parseVariableDeclaration() throws Exception {
        assertToken(TokenType.LET);
        assertToken(TokenType.IDENTIFIER);

        ASTIdentifier identifier = parseDeclarationIdentifier();

        boolean isArray = false;

        if (identifier instanceof ASTArrayIndexIdentifier) {
            isArray = true;
        }

        assertToken(TokenType.COLON);

        Type type = parseType(isArray, true);

        ASTExpression expression = null;

        updateLookahead();

        if (lookahead == null) {
            throwException("Unexpected end of file, expecting " + TokenType.EQ + " or " + TokenType.SEMICOLON);
        }

        if (isLookahead(TokenType.EQ)) {
            lookaheadUsed = true;
            expression = parseExpression();
        }

        return new ASTVariableDeclaration(identifier, type, expression);
    }

    public void close() throws IOException {
        lexer.close();
    }
}