package parser;

import lexer.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class Parser {
    private final Lexer lexer;
    private Token lookahead;
    private boolean lookaheadUsed = true;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
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

    private boolean isLookahead(TokenType tokenType) {
        if (lookahead == null)
            return false;

        return tokenType == lookahead.tokenType;
    }

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
            case PRIMITIVE:
                return parseFunctionDeclaration();
            case OPENCURLYBRACKET:
                return parseBlock();
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
                    return parseFunctionCall(lookaheadTemp);
                } else {
                    return new ASTIdentifier((Word) lookaheadTemp);
                }
            default:
                throwException("Unexpected " + lookahead.tokenType + ", expecting expression");
                return null;
        }
    }

    private ASTFunctionCall parseFunctionCall(Token identifierToken) throws Exception {
        ASTIdentifier identifier = new ASTIdentifier((Word) identifierToken);
        assertToken(TokenType.OPENROUNDBRACKET);
        List<ASTExpression> parameters = null;

        updateLookahead();

        if (lookahead == null) {
            throwException("Unexpected end of file, was expecting a parameter or ')'");
        }

        if (isLookahead(TokenType.CLOSEROUNDBRACKET)) {
            lookaheadUsed = true;
        } else {
            parameters = parseExpressionList();
            assertToken(TokenType.CLOSEROUNDBRACKET);
        }

        return new ASTFunctionCall(identifier, parameters);
    }

    //Previously was parseActualParameters, changed name so that array literal can also use it
    private List<ASTExpression> parseExpressionList() throws Exception {
        List<ASTExpression> parameterList = new ArrayList<>();
        ASTExpression parameter = parseExpression();
        parameterList.add(parameter);

        while (true) {
            updateLookahead();

            if (lookahead == null) {
                throwException("Unexpected end of file, was expecting ','");
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
            throwException("Unexpected end of file, was expecting expression or '}'");
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
            throwException("Unexpected end of file, expecting let or ';'");
        }

        if (isLookahead(TokenType.LET)) {
            variableDeclaration = parseVariableDeclaration();
        }

        assertToken(TokenType.SEMICOLON);
        ASTExpression conditionExpression = parseExpression();
        assertToken(TokenType.SEMICOLON);

        updateLookahead();

        if (lookahead == null) {
            throwException("Unexpected end of file, expecting identifier or ')'");
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
                throwException("Unexpected end of file, was expecting '}'");
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
        Type returnType = (Type) lookahead;
        lookaheadUsed = true;
        assertToken(TokenType.IDENTIFIER);
        ASTIdentifier functionName = new ASTIdentifier((Word) lookahead);
        assertToken(TokenType.OPENROUNDBRACKET);

        updateLookahead();

        if (lookahead == null) {
            throwException("Unexpected end of file, was expecting parameters or ')'");
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
                throwException("Unexpected end of file, was expecting another parameter or ','");
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
        assertToken(TokenType.IDENTIFIER);

        ASTIdentifier identifier = new ASTIdentifier((Word) lookahead);

        assertToken(TokenType.COLON);
        assertToken(TokenType.PRIMITIVE);

        Type type = (Type) lookahead;
        return new ASTParameter(identifier, type);
    }

    private ASTAssignment parseAssignment() throws Exception {
        ASTIdentifier identifier = new ASTIdentifier((Word) lookahead);
        lookaheadUsed = true;
        assertToken(TokenType.EQ);
        ASTExpression expression = parseExpression();

        return new ASTAssignment(identifier, expression);
    }

    private ASTVariableDeclaration parseVariableDeclaration() throws Exception {
        assertToken(TokenType.LET);
        assertToken(TokenType.IDENTIFIER);
        ASTIdentifier identifier = new ASTIdentifier((Word) lookahead);
        assertToken(TokenType.COLON);
        assertToken(TokenType.PRIMITIVE);
        Type type = (Type) lookahead;

        ASTExpression expression = null;

        updateLookahead();

        if (lookahead == null) {
            throwException("Unexpected end of file, expecting let or ';'");
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