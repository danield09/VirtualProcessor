import java.util.Optional;
//This node represents 2R.
//This class is used to save two registers followed by its immediate value.
public class Reg2Node extends Node{
    private Token regRd;
    private Token regR2;
    private Optional<Token> immValue;

    public Reg2Node(Token rd, Token r2, Optional<Token> imm){
        regRd = rd;
        regR2 = r2;
        immValue = imm;
    }

    public Token getRegRd(){
        return regRd;
    }
    public Token getRegR2(){
        return regR2;
    }
    public Optional<Token> getImmValue(){
        return immValue;
    }

    public String toString(){
        String retVal = "Rd: " + regRd.toString() + " R2: " + regR2.toString();
        if(immValue.isPresent()){
            retVal += " Immediate Value: " + immValue.get().toString();
        }
        return retVal;
    }
}
