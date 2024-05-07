import java.util.HashMap;
import java.util.LinkedList;

public class Lexer {
    private StringHandler stringHandler;
    private int charPosition;
    private int lineCounter;
    private HashMap<String, TokenType> keyWords;

    public Lexer(String doc){
        //initialize all members.
        stringHandler = new StringHandler(doc);
        charPosition = 0;
        lineCounter = 1;
        keyWords = new HashMap<>();
        //set up all keywords
        setKeyWords();
    }

    public LinkedList<Token> Lex() throws Exception{
        LinkedList<Token> returnList = new LinkedList<Token>();
        while(!stringHandler.isDone()){
            char c = stringHandler.getChar();
            charPosition++;
            //if the current character is space, we ignore it.
            if(c == ' '){
                ;
            }else if((c == 'r') || (c == 'R')){
                //if the current character is an 'r', it could be a register, send it to ProcessDigit as a register.
                returnList.add(ProcessDigit(true));
            }else if(((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z'))){
                //if the current character is another letter, send it to ProcessDigit.
                returnList.add(ProcessWord());
            }else if(((c >= '0') && (c <= '9'))){
                //if the current character is a number, treat it like a number.
                returnList.add(ProcessDigit(false));
            }else if((c == '\n') || (c == '\r')){
                //if it is either one of these escape sequences, treat it as a newline character.
                returnList.add(new Token(TokenType.NEWLINE, lineCounter, charPosition-1));
                lineCounter++;
                charPosition = 0;
            }else{
                //any other letter/symbols is illegal, throw an exception.
                throw new Exception("ERROR: Illegal Character at line: " + lineCounter + " column: " + charPosition + " char: " + c);
            }
        }
        return returnList;
    }

    public Token ProcessDigit(boolean isRegister) throws Exception {
        String temp = "";

        //if isRegister is true, we need to check before treating it as a register.
        if(isRegister){
            char nextChar = stringHandler.peek(1);
            //if the next character is a letter, then it is most likely a word, send it to ProcessWord.
            if((nextChar >= 'a') && (nextChar <= 'z')){
                return ProcessWord();
            }
        }

        //if it is not a register, then we need to retrieve the current character for the whole number.
        if(!isRegister){
           temp += stringHandler.peek(0);
        }

        while(!stringHandler.isDone()){
            char c = stringHandler.getChar();
            //if the current character is a number, continue adding onto the string.
            if((c >= '0') && (c <= '9')){
                temp += c;
                charPosition++;
            }else{
                //when there is no more digits, we need to go back one in order to not skip the current character.
                stringHandler.swallow(-1);
                break;
            }
        }
        if(isRegister){
            //if the register is true but there is no number, throw an exception.
            if(temp.length() == 0){
                throw new Exception("Invalid Register Number.");
            }
            //if not, create a valid register token.
            return new Token(TokenType.REGISTER, lineCounter, charPosition-temp.length(), temp);
        }else{
            //if it is not a number, create a number token.
            return new Token(TokenType.NUMBER, lineCounter, charPosition-temp.length(), temp);
        }
    }

    public Token ProcessWord() throws Exception{
        //we need to add the current character to the word.
        String currentWord = stringHandler.peek(0) + "";
        while(!stringHandler.isDone()){
            char c = stringHandler.getChar();
            //if the current character is a letter, add onto the word.
            if(((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z'))){
                currentWord += c;
                charPosition++;
            }else{
                //if not, we need to go back by one in order to preserve the current character for the next loop.
                stringHandler.swallow(-1);
                break;
            }
        }

        //we check with the keyword hashmap, as a lowercase word.
        if(keyWords.containsKey(currentWord.toLowerCase())){
            //if it contains this word, create a new token with that specific keyword as its tokentype.
            return new Token(keyWords.get(currentWord.toLowerCase()), lineCounter, charPosition - currentWord.length());
        }else{
            //if the word does not exist, throw an exception.
            throw new Exception("Invalid word at line: " + lineCounter + " column: " + charPosition);
        }
    }

    //this method is used to initialize all valid keywords.
    public void setKeyWords(){
        keyWords.put("math", TokenType.MATH);
        keyWords.put("add", TokenType.ADD);
        keyWords.put("subtract", TokenType.SUBTRACT);
        keyWords.put("multiply", TokenType.MULTIPLY);
        keyWords.put("and", TokenType.AND);
        keyWords.put("or", TokenType.OR);
        keyWords.put("not", TokenType.NOT);
        keyWords.put("xor", TokenType.XOR);
        keyWords.put("copy", TokenType.COPY);
        keyWords.put("halt", TokenType.HALT);
        keyWords.put("branch", TokenType.BRANCH);
        keyWords.put("jump", TokenType.JUMP);
        keyWords.put("call", TokenType.CALL);
        keyWords.put("push", TokenType.PUSH);
        keyWords.put("load", TokenType.LOAD);
        keyWords.put("return", TokenType.RETURN);
        keyWords.put("store", TokenType.STORE);
        keyWords.put("peek", TokenType.PEEK);
        keyWords.put("pop", TokenType.POP);
        keyWords.put("interrupt", TokenType.INTERRUPT);
        keyWords.put("equal", TokenType.EQUAL);
        keyWords.put("unequal", TokenType.UNEQUAL);
        keyWords.put("greater", TokenType.GREATER);
        keyWords.put("less", TokenType.LESS);
        keyWords.put("greaterorequal", TokenType.GREATER_OR_EQUAL);
        keyWords.put("lessorequal", TokenType.LESS_OR_EQUAL);
        keyWords.put("shift", TokenType.SHIFT);
        keyWords.put("left", TokenType.LEFT);
        keyWords.put("right", TokenType.RIGHT);
        keyWords.put("r", TokenType.REGISTER);
    }
}
