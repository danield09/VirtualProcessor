import java.util.Optional;
//This class is used to represents statement.
//Each statement has its statement type, its operation type,
//and its registers.
public class StatementNode extends Node{
    private Token statement;
    private Optional<OperationType> operation;
    private Optional<Node> registers;
    public StatementNode(Token state, Optional<OperationType> op, Optional<Node> reg){
        statement = state;
        operation = op;
        registers = reg;
    }

    public Token getStatement(){
        return statement;
    }

    public Optional<OperationType> getOperation(){
        return operation;
    }

    public Optional<Node> getRegisters(){
        return registers;
    }

    public String toString(){
        String retVal = "Token: " + statement;
        if(operation.isPresent()){
            retVal += " Operation: " + operation.get().toString();
        }else{
            retVal += " No Operation";
        }

        if(registers.isPresent()){
            retVal += " Registers: " + registers.get().toString();
        }else{
            retVal += " No Registers";
        }
        return retVal;
    }
}
