package lexer;

import java.util.Arrays;
import java.util.List;

//Class containing all the DFA states
class States{
    public final static int BAD = -2;
    public final static int ERROR = -1;
    public final static int START = 0;
    public final static int STRING1 = 1;
    public final static int STRING2 = 2;
    public final static int IDENTIFIER = 3;
    public final static int SYMBOL = 4;
    public final static int ADD = 5;
    public final static int SUB = 6;
    public final static int MUL = 7;
    public final static int DIV = 8;
    public final static int INTEGER = 9;
    public final static int FLOAT1 = 10;
    public final static int FLOAT2 = 11;
    public final static int LT = 12;
    public final static int LTE = 13;
    public final static int NOT = 14;
    public final static int NE = 15;
    public final static int GT = 16;
    public final static int GTE = 17;
    public final static int EQ1 = 18;
    public final static int EQ2 = 19;
    public final static int COMMENT1 = 20;
    public final static int COMMENT2 = 21;
    public final static int COMMENTM1 = 22;
    public final static int COMMENTM2 = 23;
    public final static int COMMENTM3 = 24;
    public final static int CHAR1 = 25;
    public final static int CHAR2 = 26;
    public final static int CHAR3 = 27;

    public static List<Integer> getAcceptingStates() {
        return Arrays.asList(STRING2,IDENTIFIER,SYMBOL,ADD,SUB,MUL,DIV,INTEGER,FLOAT2,
                LT,LTE,NE,GT,GTE,EQ1,EQ2,COMMENT2,COMMENTM2,COMMENTM3, CHAR3);
    }
}
