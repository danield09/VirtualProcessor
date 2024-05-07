//list of possible TokenTypes.
enum TokenType {
    MATH, ADD, SUBTRACT, MULTIPLY, AND, OR, NOT, XOR, COPY, HALT, BRANCH,
    JUMP, CALL, PUSH, LOAD, RETURN, STORE, PEEK, POP, INTERRUPT, EQUAL,
    UNEQUAL, GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL, SHIFT, LEFT,
    RIGHT, NUMBER, REGISTER, NEWLINE
}

//Token class is used to save values and data during Lexer.
public class Token {
    private TokenType tokenType;
    private String value;
    private int lineNumber;
    private int linePosition;

    public Token(TokenType tType, int lineN, int lineP){
        tokenType = tType;
        lineNumber = lineN;
        linePosition = lineP;
        value = "";
    }

    public Token(TokenType tType, int lineN, int lineP, String val){
        tokenType = tType;
        lineNumber = lineN;
        linePosition = lineP;
        value = val;
    }

    public String getTokenValue(){
        return value;
    }
    public TokenType getTokenType(){
        return tokenType;
    }

    public String toString(){
        if(value.isBlank()){
            return tokenType + "";
        }
        return tokenType + "(" + value + ")";
    }
}
