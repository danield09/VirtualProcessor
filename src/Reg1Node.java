import java.util.Optional;
//This node represents 1R.
//This class is used to store one register followed by an immediate value.
public class Reg1Node extends Node{
    private Token register;
    private Optional<Token> immediateValue;
    public Reg1Node(Token reg, Optional<Token> imm){
        register = reg;
        immediateValue = imm;
    }

    public Token getRegister(){
        return register;
    }

    public Optional<Token> getImmediateValue(){
        return immediateValue;
    }

    public String toString(){
        String retVal = "Rd: " + register.toString();
        if(immediateValue.isPresent()){
            retVal += " Immediate Value: " + immediateValue.get().toString();
        }
        return retVal;
    }
}
