import java.util.LinkedList;
import java.util.Optional;

public class TokenManager {
    private LinkedList<Token> tokenList;

    public TokenManager(LinkedList<Token> tList){
        tokenList = tList;
    }

    public Optional<Token> peek(int i){
        // the index is less than the current size, then return that token stored at that index.
        if(i < tokenList.size()){
            return Optional.ofNullable((tokenList.get(i)));
        }else{
            //otherwise, return empty.
            return Optional.empty();
        }
    }

    public Optional<Token> matchAndRemove(TokenType tokenType){
        //if there are no more tokens, return empty.
        if(!moreTokens()){
            return Optional.empty();
        }
        //if the first token has the same tokentype as the parameters, remove and return.
        if(tokenList.getFirst().getTokenType() == tokenType){
            return Optional.ofNullable(tokenList.pollFirst());
        }else{
            //otherwise, return empty.
            return Optional.empty();
        }
    }
    //Used to check if there are more tokens or not.
    public boolean moreTokens(){
        return !tokenList.isEmpty();
    }
}
