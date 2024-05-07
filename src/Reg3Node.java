import java.util.Optional;
//This node represents 3R.
//This class is used to save 3 registers followed by a immediate value.
public class Reg3Node extends Node{
    private Token regRd;
    private Token regR2;
    private Token regR1;
    private Optional<Token> immValue;

    public Reg3Node(Token rd, Token r2, Token r1, Optional<Token> imm){
        regRd = rd;
        regR2 = r2;
        regR1 = r1;
        immValue = imm;
    }

    public Token getRegRd(){
        return regRd;
    }
    public Token getRegR2(){
        return regR2;
    }
    public Token getRegR1(){
        return regR1;
    }
    public Optional<Token> getImmValue(){
        return immValue;
    }

    public String toString(){
        String retVal = "Rd: " + regRd.toString() + " R2: " + regR2.toString() + " R1: " + regR1.toString();
        if(immValue.isPresent()){
            retVal += " Immediate Value: " + immValue.get().toString();
        }
        return retVal;
    }
}
