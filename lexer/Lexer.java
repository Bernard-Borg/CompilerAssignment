package lexer;

import java.io.*;
import java.util.*;
import static lexer.States.*;

public class Lexer {
    public int lineNumber;
    public int characterInLine;
    private final Reader reader;
    private final Stack<Integer> stack;
    private final HashSet<Integer> acceptingStates;
    private final ArrayDeque<Character> characterBuffer;
    private final HashMap<String, Word> reservedWords;
    public boolean EOF = false;
    private int state;

    //Table containing DFA transitions
    private final int[][] transitionTable = {
        {INTEGER,IDENTIFIER,STRING1,ERROR,ADD,SUB,DIV,MUL,SYMBOL,IDENTIFIER,EQ1,LT,GT,NOT,SYMBOL,CHAR1,ERROR,ERROR},
        {STRING1,STRING1,STRING2,STRING1,STRING1,STRING1,STRING1,STRING1,STRING1,STRING1,STRING1,STRING1,STRING1,STRING1,ERROR,STRING1,STRING1,ERROR},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {IDENTIFIER,IDENTIFIER,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,IDENTIFIER,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,COMMENT1,COMMENTM1,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {INTEGER,ERROR,ERROR,FLOAT1,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {FLOAT2,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {FLOAT2,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,LTE,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,NE,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,GTE,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,EQ2,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {COMMENT1,COMMENT1,COMMENT1,COMMENT1,COMMENT1,COMMENT1,COMMENT1,COMMENT1,COMMENT1,COMMENT1,COMMENT1,COMMENT1,COMMENT1,COMMENT1,COMMENT2,COMMENT1,COMMENT1,COMMENT1},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM2,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1},
        {COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM3,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1,COMMENTM1},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR},
        {CHAR2,CHAR2,CHAR2,CHAR2,CHAR2,CHAR2,CHAR2,CHAR2,CHAR2,CHAR2,CHAR2,CHAR2,CHAR2,CHAR2,ERROR,CHAR2,CHAR2,ERROR},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,CHAR3,ERROR,ERROR},
        {ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR,ERROR}
    };

    //Initialises the lexer
    public Lexer(String programPath) throws FileNotFoundException {
        lineNumber = 1;
        characterInLine = 0;

        reader = new FileReader(programPath);

        stack = new Stack<>();
        characterBuffer = new ArrayDeque<>();
        acceptingStates = new HashSet<>(getAcceptingStates());
        reservedWords = new HashMap<>();

        //Reserves the keywords
        put(Word.OR);
        put(Word.AND);
        put(Word.TRUE);
        put(Word.FALSE);
        put(Word.LET);
        put(Word.PRINT);
        put(Word.RETURN);
        put(Word.IF);
        put(Word.ELSE);
        put(Word.FOR);
        put(Word.WHILE);
        put(Word.NOT);
        put(Type.BOOL);
        put(Type.FLOAT);
        put(Type.INTEGER);
        put(Type.STRING);
        put(Type.CHAR);
        put(Type.AUTO);
    }

    //Utility method to add strings to map of keywords
    private void put(Word word) {
        reservedWords.put(word.lexeme, word);
    }

    //Gets the next character of the file
    public char getNextChar() throws IOException {
        int nextCharacter;

        nextCharacter = reader.read();

        //If the next character is the end of file, set EOf flag to true
        if (nextCharacter == -1) {
            EOF = true;
            return ' ';
        }

        //Newline adds 1 to the line number and resets the character in line counter
        if ((char)nextCharacter == '\n') {
            characterInLine = 0;
            lineNumber++;
        } else {
            //Tabs are replaced with spaces
            if ((char) nextCharacter == '\t') {
                nextCharacter = ' ';
            }

            characterInLine++;
        }

        return (char)nextCharacter;
    }

    //Main table-driven lexer algorithm
    public String getNextLexeme() throws IOException {
        //Initialisation phase
        int category;
        state = START;

        StringBuilder lexeme = new StringBuilder();
        stack.clear();
        stack.push(BAD);

        char nextCharacter;
        int nextValue;

        //Scanning loop
        while(state != ERROR) {
            //If the character buffer is empty, read next character from file, otherwise take from buffer
            if (characterBuffer.size() == 0) {
                nextValue = getNextChar();

                //If the end of file is reached, returns the string in the current buffer
                if (EOF) {
                    if (acceptingStates.contains(state) || state == START || state == COMMENT1 || state == COMMENTM1 || state == COMMENTM2) {
                        return lexeme.toString();
                    } else {
                        return "$";
                    }
                }

                nextCharacter = (char) nextValue;
            } else {
                nextCharacter = characterBuffer.removeLast();
            }

            //Ignores spaces, newlines and carriage
            if (state == START && (nextCharacter == ' ' || nextCharacter == '\r' || nextCharacter == '\n')) {
                continue;
            }

            lexeme.append(nextCharacter);

            if (acceptingStates.contains(state)) {
                stack.clear();
            }

            stack.push(state);
            category = categoriseCharacter(nextCharacter);
            state = transitionTable[state][category];
        }

        //Rollback loop
        while (!acceptingStates.contains(state) && state != BAD) {
            state = stack.pop();

            if (state == BAD)
                break;

            //Removes last character and puts it in the buffer
            char removedCharacter = lexeme.charAt(lexeme.length() - 1);
            characterBuffer.addLast(removedCharacter);
            lexeme.deleteCharAt(lexeme.length() - 1);
        }

        //Reporting the result
        if (acceptingStates.contains(state)) {
            return lexeme.toString();
        } else {
            return "$";
        }
    }

    public int categoriseCharacter(char character) {
        String stringCharacter = String.valueOf(character);

        if (stringCharacter.matches("[0-9]")) {
            return 0;
        } else if (stringCharacter.matches("[a-zA-Z]")) {
            return 1;
        } else if (character == '"') {
            return 2;
        } else if (character == '.') {
            return 3;
        } else if (character == '+') {
            return 4;
        } else if (character == '-') {
            return 5;
        } else if (character == '/') {
            return 6;
        } else if (character == '*') {
            return 7;
        } else if (stringCharacter.matches("[}{)(\\]\\[:,;]")) {
            return 8;
        } else if (character == '_') {
            return 9;
        } else if (character == '=') {
            return 10;
        } else if (character == '<') {
            return 11;
        } else if (character == '>') {
            return 12;
        } else if (character == '!') {
            return 13;
        } else if (character == '\n') {
            return 14;
        } else if (character == '\'') {
            return 15;
        } else if (stringCharacter.matches("[\\x20-\\x7E]")) {
            return 16;
        } else {
            return 17;
        }
    }

    //Joins characters into tokens
    public Token getNextToken() throws Exception {
        String lexeme;

        do {
            //Stops loop if end of file is reached
            if (EOF) {
                return null;
            }

            lexeme = getNextLexeme();

            //Reports errors
            if (lexeme.equals("$")) {
                throw new Exception("Lexical error in line: " + lineNumber + ", character " + characterInLine);
            }
        } while (state == COMMENT2 || state == COMMENTM2 || state == COMMENTM3);

        //Creates token from current state and lexeme
        switch(state) {
            case STRING2:
                return new Word(lexeme, TokenType.STRING);
            case IDENTIFIER:
                if (reservedWords.containsKey(lexeme)) {
                    return reservedWords.get(lexeme);
                } else {
                    return new Word(lexeme, TokenType.IDENTIFIER);
                }
            case SYMBOL:
                switch (lexeme) {
                    case "{": return new Token(TokenType.OPENCURLYBRACKET);
                    case "(": return new Token(TokenType.OPENROUNDBRACKET);
                    case "}": return new Token(TokenType.CLOSECURLYBRACKET);
                    case ")": return new Token(TokenType.CLOSEROUNDBRACKET);
                    case "[": return new Token(TokenType.OPENSQUAREBRACKET);
                    case "]": return new Token(TokenType.CLOSESQUAREBRACKET);
                    case ":": return new Token(TokenType.COLON);
                    case ";": return new Token(TokenType.SEMICOLON);
                    case ",": return new Token(TokenType.COMMA);
                }
            case ADD:
                return new Token(TokenType.ADD);
            case SUB:
                return new Token(TokenType.SUB);
            case MUL:
                return new Token(TokenType.MUL);
            case DIV:
                return new Token(TokenType.DIV);
            case INTEGER:
                return new WholeNumber(Integer.parseInt(lexeme));
            case FLOAT2:
                return new Real(Float.parseFloat(lexeme));
            case LT:
                return new Token(TokenType.LT);
            case LTE:
                return new Token(TokenType.LTE);
            case NE:
                return new Token(TokenType.NE);
            case GT:
                return new Token(TokenType.GT);
            case GTE:
                return new Token(TokenType.GTE);
            case EQ1:
                return new Token(TokenType.EQ);
            case EQ2:
                return new Token(TokenType.CMP);
            case CHAR3:
                return new Word(lexeme, TokenType.CHAR);
            default:
                return null;
        }
    }

    public void close() throws IOException {
        reader.close();
    }
}
