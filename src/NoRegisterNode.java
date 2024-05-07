import java.util.Optional;
//This node represents 0R.
//This class only contains a possible immediate value.
public class NoRegisterNode extends Node{
    private Optional<Token> value;
    public NoRegisterNode(Optional<Token> v){
        value = v;
    }

    public Optional<Token> getImmediateValue(){
        return value;
    }
    public String toString(){
        if(value.isPresent())
            return "Immediate Value: " + value.toString();
        return "No Immediate Value";
    }
}
