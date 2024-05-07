import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

public class Parser {
    private TokenManager tokenManager;
    public Parser(LinkedList<Token> tokenList){
        //set up tokenManager for Parsing.
        tokenManager = new TokenManager(tokenList);
    }
    public ArrayList<Node> Parse() throws Exception{
        return ParseStatements();
    }
    public ArrayList<Node> ParseStatements() throws Exception{
        ArrayList<Node> listOfStatements = new ArrayList<>();
        //loops while there are more tokens.
        while(tokenManager.moreTokens()){
            //call ParseStatement to get a statement.
            Optional<Node> statement = ParseStatement();
            //if there is a statement and it is a instance of a StatementNode,
            //add it to the list.
            if(statement.isPresent() && statement.get() instanceof StatementNode) {
                listOfStatements.add(statement.get());
            }else{
                //if it is empty or not a StatementNode, throws an exception.
                throw new Exception("Invalid statement.");
            }
            //loops to remove any amount of newline after a statement.
            while(tokenManager.matchAndRemove(TokenType.NEWLINE).isPresent());
        }
        //return the list of statements.
        return listOfStatements;
    }
    public String getBinaryStatements(ArrayList<Node> listOfStatements) throws Exception{
        String document = "";
        //loops through all the statements.
        for(int i = 0; i < listOfStatements.size();i++) {
            String currentInstruction = new String();//set up the current instruction String.
            StatementNode currentStatement = (StatementNode) listOfStatements.get(i);
            Token currStatement = currentStatement.getStatement();//grabs the statement type.
            Optional<OperationType> currOperation = currentStatement.getOperation();//grabs the statement's operation.
            Optional<Node> currRegisters = currentStatement.getRegisters();//grabs the statement's registers.

            String opCode = "";
            //switch case covers all the different opCode.
            switch(currStatement.getTokenType()){
                case MATH, COPY, HALT -> opCode = "000";
                case BRANCH, JUMP -> opCode = "001";
                case CALL -> opCode = "010";
                case PUSH -> opCode = "011";
                case LOAD, RETURN -> opCode = "100";
                case STORE -> opCode = "101";
                case PEEK, POP -> opCode = "110";
                default -> throw new Exception("Invalid Statement Type.");
            }

            //default the functionCode to 0000.
            String functionCode = "0000";
            //if there is an operation type there, then change the function code to the correct code.
            if(currOperation.isPresent()){
                switch(currOperation.get()){
                    case AND -> functionCode = "1000";
                    case OR -> functionCode = "1001";
                    case XOR -> functionCode = "1010";
                    case NOT -> functionCode = "1011";
                    case LEFT_SHIFT -> functionCode = "1100";
                    case RIGHT_SHIFT -> functionCode = "1101";
                    case ADD -> functionCode = "1110";
                    case SUBTRACT -> functionCode = "1111";
                    case MULTIPLY -> functionCode = "0111";
                    case EQUALS -> functionCode = "0000";
                    case NOT_EQUALS -> functionCode = "0001";
                    case LESS_THAN -> functionCode = "0010";
                    case GREATER_THAN_OR_EQUAL -> functionCode = "0011";
                    case GREATER_THAN -> functionCode = "0100";
                    case LESS_THAN_OR_EQUAL -> functionCode = "0101";
                }
            }

            //if there are registers, check what register we are working with.
            if(currRegisters.isPresent()){
                if(currRegisters.get() instanceof NoRegisterNode){
                    //if it is a 0R, cast to a NoRegisterNode.
                    NoRegisterNode noRegNode = (NoRegisterNode) currRegisters.get();
                    Optional<Token> immValue = noRegNode.getImmediateValue();//grabs the immediateValue.
                    opCode = opCode + "00";//the opCode starts with 00, for 0R.
                    if(immValue.isPresent()){
                        //if there is an immediate value, translate it to a 27 bit binary string.
                        String immNum = (immValue.get()).getTokenValue();
                        String binaryNum = intStringToBinary(immNum);
                        String immBinary = String.format("%27s", binaryNum).replace(" ", "0");
                        currentInstruction = immBinary + opCode;//combine to create the instruction.
                    }else{
                        //if there is no immediate value, default to all 0.
                        String emptyBinary = String.format("%27s", "").replace(" ", "0");
                        currentInstruction = emptyBinary + opCode;//combine to create the instruction.
                    }
                }else if(currRegisters.get() instanceof Reg1Node){
                    //if the register is 1R...
                    opCode = opCode + "01";//update the opCode.
                    Reg1Node reg1Node = (Reg1Node) currRegisters.get();//cast to Reg1Node.
                    Token registerRdToken = reg1Node.getRegister();//Grabs the Register Token.
                    String regValue = registerRdToken.getTokenValue();//Grabs what Register it is.
                    //translate the register into a 5 bit binary string.
                    String regValueBin = intStringToBinary(regValue);
                    String regBin = String.format("%5s", regValueBin).replace(" ", "0");
                    Optional<Token> immValue = reg1Node.getImmediateValue();
                    if(immValue.isPresent()){
                        //if there is an immediate value, translate into an 18 bit binary string.
                        String immNum = (immValue.get()).getTokenValue();
                        String binaryNum = intStringToBinary(immNum);
                        String immBinary = String.format("%18s", binaryNum).replace(" ", "0");
                        currentInstruction = immBinary + functionCode + regBin + opCode;//combine to create the current instruction.
                    }else{
                        //if there is no immediate value, translate into an 17 bit binary string that empty.
                        String emptyBinary = String.format("%18s", "").replace(" ", "0");
                        currentInstruction = emptyBinary + functionCode + regBin + opCode;//combine to create the current instruction.
                    }
                }else if(currRegisters.get() instanceof Reg2Node){
                    //if the register is Reg2Node...
                    opCode = opCode + "11";//update the opCode.
                    Reg2Node reg2Node = (Reg2Node) currRegisters.get();//case into Reg2Node
                    String regRdValueInt = reg2Node.getRegRd().getTokenValue();//grabs the first register
                    String regRdValueBin = intStringToBinary(regRdValueInt);//grabs what is the first register value.
                    //create a 5 bit binary string for the first register.
                    String regRdBin = String.format("%5s", regRdValueBin).replace(" ", "0");

                    String regR2ValueInt = reg2Node.getRegR2().getTokenValue();//grabs the second register
                    String regR2ValueBin = intStringToBinary(regR2ValueInt);//grabs what is the second register value.
                    //create a 5 bit binary string for the second register.
                    String regR2Bin = String.format("%5s", regR2ValueBin).replace(" ", "0");

                    Optional<Token> immValue = reg2Node.getImmValue();
                    if(immValue.isPresent()){
                        //if there is a immediate value, translate into a 13 bit binary string.
                        String immNum = (immValue.get()).getTokenValue();
                        String binaryNum = intStringToBinary(immNum);
                        String immBinary = String.format("%13s", binaryNum).replace(" ", "0");
                        //combine to create the instruction.
                        currentInstruction = immBinary + regR2Bin + functionCode + regRdBin + opCode;
                    }else{
                        //if there is no immediate value, translate into a 13 bit binary string that empty.
                        String emptyBinary = String.format("%13s", "").replace(" ", "0");
                        //combine to create the instruction.
                        currentInstruction = emptyBinary + regR2Bin + functionCode + regRdBin + opCode;
                    }
                }else if(currRegisters.get() instanceof Reg3Node){
                    //if the registers is 3R...
                    opCode = opCode + "10";//update the opCode
                    Reg3Node reg3Node = (Reg3Node) currRegisters.get();//cast into Reg3Node
                    //grabs the first register and translate into a 5 bit binary string.
                    String regRdValueInt = reg3Node.getRegRd().getTokenValue();
                    String regRdValueBin = intStringToBinary(regRdValueInt);
                    String regRdBin = String.format("%5s", regRdValueBin).replace(" ", "0");
                    //grabs the second register and translate into a 5 bit binary string.
                    String regR2ValueInt = reg3Node.getRegR2().getTokenValue();
                    String regR2ValueBin = intStringToBinary(regR2ValueInt);
                    String regR2Bin = String.format("%5s", regR2ValueBin).replace(" ","0");
                    //grabs the third register and translate into a 5 bit binary string.
                    String regR1ValueInt = reg3Node.getRegR1().getTokenValue();
                    String regR1ValueBin = intStringToBinary(regR1ValueInt);
                    String regR1Bin = String.format("%5s", regR1ValueBin).replace(" ", "0");
                    Optional<Token> immValue = reg3Node.getImmValue();
                    if(immValue.isPresent()){
                        //if there is a immediate value, then translate into an 8 bit binary string.
                        String immNum = (immValue.get()).getTokenValue();
                        String binaryNum = intStringToBinary(immNum);
                        String immBinary = String.format("%8s", binaryNum).replace(" ", "0");
                        currentInstruction = immBinary + regR1Bin + regR2Bin + functionCode + regRdBin + opCode;
                    }else{
                        //if there is no immediate value, then translate into an empty 8 bit binary string.
                        String emptyBinary = String.format("%8s", "").replace(" ", "0");
                        currentInstruction = emptyBinary + regR1Bin + regR2Bin + functionCode + regRdBin + opCode;
                    }
                }
            }
            //adding newline for proper file writing.
            document += (currentInstruction + '\n');
        }
        //return the program as a string.
        return document;
    }
    public String intStringToBinary(String value){
        //converts a string value, a number, into a binary number string.
        return Integer.toString((Integer.parseInt(value)), 2);
    }
    public Optional<Node> ParseStatement() throws Exception{
        return ParseMath();
    }
    public Optional<Node> ParseMath() throws Exception{
        //see if the current token is MATH.
        Optional<Token> mathToken = tokenManager.matchAndRemove(TokenType.MATH);
        if(mathToken.isPresent()){
            //if the token is MATH, then we need to check for a math operation.
            Optional<OperationType> mathOp = ParseMathOperations();
            //if the result is empty, then there is no math operation, throw an exception.
            if(mathOp.isEmpty()){
                throw new Exception("Invalid Math Operations near MATH statement.");
            }
            //call at the top of the recursive descent to get the register.
            Optional<Node> regToken = ParseStatement();
            //if the register is either reg2Node and reg3Node,
            //then create a StatementNode with all parts.
            if(regToken.isPresent() && (regToken.get() instanceof Reg2Node ||
                    regToken.get() instanceof Reg3Node)){
                return Optional.of(new StatementNode(mathToken.get(), mathOp, regToken));
            }else{
                //if not, throw an exception as this is an invalid instruction.
                throw new Exception("Invalid Syntax near MATH statement.");
            }
        }
        return ParseBranch();
    }
    public Optional<Node> ParseBranch() throws Exception{
        //see if the current token is BRANCH.
        Optional<Token> branchToken = tokenManager.matchAndRemove(TokenType.BRANCH);
        if(branchToken.isPresent()){
            //if it is BRANCH token, then we need to see if there is a boolean operation.
            Optional<OperationType> booleanOp = ParseBooleanOperations();
            //if there is no boolean operation, throw an exception.
            if(booleanOp.isEmpty()){
                throw new Exception("Invalid Boolean Operation near BRANCH statement.");
            }
            //then we call the top for registers.
            Optional<Node> regToken = ParseStatement();
            //if the registers is either 2R or 3R, create a StatementNode.
            if(regToken.isPresent() && (regToken.get() instanceof Reg2Node ||
                    regToken.get() instanceof Reg3Node)){
                return Optional.of(new StatementNode(branchToken.get(), booleanOp, regToken));
            }else{
                //if not, then it is an invalid statement, throw an exception.
                throw new Exception("Invalid Syntax near BRANCH statement.");
            }
        }
        return ParseHalt();
    }
    public Optional<Node> ParseHalt() throws Exception {
        //see if the current token is HALT
        Optional<Token> haltToken = tokenManager.matchAndRemove(TokenType.HALT);
        if(haltToken.isPresent()){
            //if it is HALT, then create a StatementNode with HALT, nothing more.
            return Optional.of(new StatementNode(haltToken.get(), Optional.empty(), Optional.empty()));
        }
        return ParseCopy();
    }
    public Optional<Node> ParseCopy() throws Exception {
        //see if the current token is COPY.
        Optional<Token> copyToken = tokenManager.matchAndRemove(TokenType.COPY);
        if(copyToken.isPresent()){
            //if it is COPY, then we need a 1R Register.
            Optional<Node> destOnly = ParseStatement();
            //if the register from the top is a 1R Register, create a StatementNode.
            if(destOnly.isPresent() && destOnly.get() instanceof Reg1Node){
                return Optional.of(new StatementNode(copyToken.get(), Optional.empty(), destOnly));
            }else{
                //if it is not 1R Register, throw an exception as this is an invalid statement.
                throw new Exception("Invalid syntax near COPY statement.");
            }
        }
        return ParseJump();
    }
    public Optional<Node> ParseJump() throws Exception{
        //see if there is a JUMP token.
        Optional<Token> jumpToken = tokenManager.matchAndRemove(TokenType.JUMP);
        if(jumpToken.isPresent()){
            //if it is a JUMP token, then we need to remove any math/boolean operations
            //as they can exist but not necessary to preserve.
            ParseMathOperations();
            ParseBooleanOperations();
            //call the top in order to get registers.
            Optional<Node> regToken = ParseStatement();
            //if the register is either 0R or 1R, create an StatementNode.
            if(regToken.isPresent() && (regToken.get() instanceof NoRegisterNode ||
                    regToken.get() instanceof Reg1Node)){
                return Optional.of(new StatementNode(jumpToken.get(), Optional.empty(), regToken));
            }else{
                //if not, invalid statement, throw an exception.
                throw new Exception("Invalid Syntax near JUMP statement.");
            }
        }
        return ParseCall();
    }
    public Optional<Node> ParseCall() throws Exception{
        //see if there is a call token.
        Optional<Token> callToken = tokenManager.matchAndRemove(TokenType.CALL);
        if(callToken.isPresent()){
            //if there is a CALL token, we need to search for a boolean operation.
            Optional<OperationType> booleanOp = ParseBooleanOperations();
            //then we search for the registers by calling the top.
            Optional<Node> regToken = ParseStatement();
            if(regToken.isPresent()){
                //if the register is either 1R or 0R, then we do not care about the boolean operation
                //and create a StatementNode.
                if(regToken.get() instanceof Reg1Node || regToken.get() instanceof NoRegisterNode){
                    return Optional.of(new StatementNode(callToken.get(), booleanOp, regToken));
                }else if(regToken.get() instanceof Reg2Node || regToken.get() instanceof Reg3Node){
                    //if the register is either 2R or 3R, then we do care about the boolean operation
                    //check if there is a boolean operation and if not, throw an exception.
                    if(booleanOp.isEmpty()){
                        throw new Exception("Missing Boolean Operations near CALL statement.");
                    }else{
                        //if there is a boolean operation, create a StatementNode.
                        return Optional.of(new StatementNode(callToken.get(), booleanOp, regToken));
                    }
                }
            }
        }
        return ParsePush();
    }
    public Optional<Node> ParsePush() throws Exception{
        //see if there is a PUSH token.
        Optional<Token> pushToken = tokenManager.matchAndRemove(TokenType.PUSH);
        if(pushToken.isPresent()){
            //if there is a PUSH token, then we need to check for a math operation.
            Optional<OperationType> mathOp = ParseMathOperations();
            //if it returns empty, then throw an exception.
            if(mathOp.isEmpty()){
                throw new Exception("Missing Math Operation near PUSH statement.");
            }
            //now call the top for the registers.
            Optional<Node> regToken = ParseStatement();
            //if the register is 1R, 2R, or 3R, then we can create a StatementNode.
            if(regToken.isPresent() && (regToken.get() instanceof Reg1Node ||
                    regToken.get() instanceof Reg2Node ||
                    regToken.get() instanceof Reg3Node)){
                return Optional.of(new StatementNode(pushToken.get(), mathOp, regToken));
            }else{
                //if it is 0R, invalid statement, throw an exception.
                throw new Exception("Invalid Syntax near STORE statement.");
            }
        }
        return ParsePop();
    }
    public Optional<Node> ParsePop() throws Exception{
        //check if there is a POP token.
        Optional<Token> popToken = tokenManager.matchAndRemove(TokenType.POP);
        if(popToken.isPresent()){
            //if so, ignore both math and boolean operations.
            ParseMathOperations();
            ParseBooleanOperations();
            //call the top to get the registers.
            Optional<Node> regToken = ParseStatement();
            //check if the register is 1R
            //if so, create a StatementNode
            //if not, throw an exception.
            if(regToken.isPresent() && regToken.get() instanceof Reg1Node){
                return Optional.of(new StatementNode(popToken.get(), Optional.empty(), regToken));
            }else{
                throw new Exception("Invalid Syntax near POP statements.");
            }
        }
        return ParseLoad();
    }
    public Optional<Node> ParseLoad() throws Exception{
        //see if there is LOAD token.
        Optional<Token> loadToken = tokenManager.matchAndRemove(TokenType.LOAD);
        if(loadToken.isPresent()){
            //if so, ignore both math and boolean operations.
            ParseMathOperations();
            ParseBooleanOperations();
            //call the top to get the registers.
            Optional<Node> regToken = ParseStatement();
            //if the registers is 1R, 2R, 3R, create a StatementNode
            //if not, then invalid statement, throw an exception.
            if(regToken.isPresent() && (regToken.get() instanceof Reg1Node ||
                    regToken.get() instanceof Reg2Node ||
                    regToken.get() instanceof Reg3Node)){
                return Optional.of(new StatementNode(loadToken.get(), Optional.empty(), regToken));
            }else{
                throw new Exception("Invalid Syntax near LOAD statement.");
            }
        }
        return ParseStore();
    }
    public Optional<Node> ParseStore() throws Exception{
        //see if there is a STORE token.
        Optional<Token> storeToken = tokenManager.matchAndRemove(TokenType.STORE);
        if(storeToken.isPresent()){
            //if so, ignore both math and boolean operations.
            ParseMathOperations();
            ParseBooleanOperations();
            //check if the registers are 1R, 2R, 3R
            //if so, then create a StatementNode
            //if not, throw an exception.
            Optional<Node> regToken = ParseStatement();
            if(regToken.isPresent() && (regToken.get() instanceof Reg1Node ||
                    regToken.get() instanceof Reg2Node ||
                    regToken.get() instanceof Reg3Node)){
                return Optional.of(new StatementNode(storeToken.get(), Optional.empty(), regToken));
            }else{
                throw new Exception("Invalid Syntax near STORE statement.");
            }
        }
        return ParseReturn();
    }
    public Optional<Node> ParseReturn() throws Exception{
        //see if there is a RETURN token.
        Optional<Token> returnToken = tokenManager.matchAndRemove(TokenType.RETURN);
        if(returnToken.isPresent()){
            //if so, ignore both math and boolean operations.
            ParseMathOperations();
            ParseBooleanOperations();
            //call the registers by calling the top.
            Optional<Node> regToken = ParseStatement();
            //if the register is 0R, then create a StatementNode
            //if not, throw an exception.
            if(regToken.isPresent() && regToken.get() instanceof NoRegisterNode){
                return Optional.of(new StatementNode(returnToken.get(), Optional.empty(), regToken));
            }else{
                throw new Exception("Invalid Syntax near RETURN statement.");
            }
        }
        return ParsePeek();
    }
    public Optional<Node> ParsePeek() throws Exception{
        //see if there is a PEEK token.
        Optional<Token> peekToken = tokenManager.matchAndRemove(TokenType.PEEK);
        if(peekToken.isPresent()){
            //if so, ignore both the math and boolean operations
            ParseMathOperations();
            ParseBooleanOperations();
            //get the registers by calling the top.
            Optional<Node> regToken = ParseStatement();
            //if the registers are 2R or 3R, create a StatementNode
            //if not, throw an exception for invalid statement.
            if(regToken.isPresent() && (regToken.get() instanceof Reg2Node ||
                    regToken.get() instanceof Reg3Node)){
                return Optional.of(new StatementNode(peekToken.get(), Optional.empty(), regToken));
            }else{
                throw new Exception("Invalid Syntax near PEEK statement.");
            }
        }
        return ParseTwoRegs();
    }
    public Optional<Node> ParseTwoRegs(){
        Optional<Token> regRdToken = tokenManager.peek(0);//get the first token.
        Optional<Token> regR2Token = tokenManager.peek(1);//get the next token.
        Optional<Token> peekToken = tokenManager.peek(2);//get the next next token.
        //check if the first two tokens are present.
        if(regRdToken.isPresent() && regR2Token.isPresent()){
           //check if the third token is either empty or is present but not a register token.
            if((peekToken.isEmpty() && (tokenManager.peek(3).isEmpty())) || (peekToken.isPresent() && !(peekToken.get().getTokenType() == TokenType.REGISTER))){
                //check if the first two tokens are REGISTERS
                if(regRdToken.get().getTokenType() == TokenType.REGISTER && regR2Token.get().getTokenType()
                        == TokenType.REGISTER){
                    //then we get the two registers and the possible number.
                    tokenManager.matchAndRemove(TokenType.REGISTER);
                    tokenManager.matchAndRemove(TokenType.REGISTER);
                    Optional<Token> immToken = tokenManager.matchAndRemove(TokenType.NUMBER);
                    //return the register as a 2R Node.
                    return Optional.of(new Reg2Node(regRdToken.get(), regR2Token.get(), immToken));
                }
            }
        }
        return ParseThreeRegs();
    }
    public Optional<Node> ParseThreeRegs(){
        Optional<Token> regRdToken = tokenManager.peek(0);//get the first token
        Optional<Token> regR2Token = tokenManager.peek(1);//get the next token
        Optional<Token> regR1Token = tokenManager.peek(2);//get the next next token
        //check if all three tokens are present.
        if(regRdToken.isPresent() && regR2Token.isPresent() && regR1Token.isPresent()){
            //check if all three tokens are registers.
            if(regRdToken.get().getTokenType() == TokenType.REGISTER && regR2Token.get().getTokenType()
                    == TokenType.REGISTER && regR1Token.get().getTokenType() == TokenType.REGISTER){
                //if so, get all registers, its immediate value, and create a 3R Node.
                tokenManager.matchAndRemove(TokenType.REGISTER);
                tokenManager.matchAndRemove(TokenType.REGISTER);
                tokenManager.matchAndRemove(TokenType.REGISTER);
                Optional<Token> immToken = tokenManager.matchAndRemove(TokenType.NUMBER);
                return Optional.of(new Reg3Node(regRdToken.get(), regR2Token.get(), regR1Token.get(), immToken));
            }
        }
        return ParseDestOnlyReg();
    }
    public Optional<Node> ParseDestOnlyReg(){
        //see if there is a register token.
        Optional<Token> regToken = tokenManager.matchAndRemove(TokenType.REGISTER);
        if(regToken.isPresent()){
            //if so, then we call for a possible immediate value and create a 1R Node.
            Optional<Token> immToken = tokenManager.matchAndRemove(TokenType.NUMBER);
            return Optional.of(new Reg1Node(regToken.get(), immToken));
        }
        return ParseNoReg();
    }
    public Optional<Node> ParseNoReg(){
        //see if there is a number token.
        Optional<Token> immToken = tokenManager.matchAndRemove(TokenType.NUMBER);
        if(immToken.isPresent()){
            //if so, create a 0R Node with immediate value.
            return Optional.of(new NoRegisterNode(immToken));
        }
        //if not, create a empty 0R Node.
        return Optional.of(new NoRegisterNode(Optional.empty()));
    }
    public Optional<OperationType> ParseMathOperations() throws Exception{
        //return if the current token is any of the math operations
        //and return its respective OperationType.
        if(tokenManager.matchAndRemove(TokenType.AND).isPresent())
            return Optional.of(OperationType.AND);
        if(tokenManager.matchAndRemove(TokenType.OR).isPresent())
            return Optional.of(OperationType.OR);
        if(tokenManager.matchAndRemove(TokenType.XOR).isPresent())
            return Optional.of(OperationType.XOR);
        if(tokenManager.matchAndRemove(TokenType.NOT).isPresent())
            return Optional.of(OperationType.NOT);
        if(tokenManager.matchAndRemove(TokenType.SHIFT).isPresent()){
            //for shift, there HAS to be a left or right token.
            if(tokenManager.matchAndRemove(TokenType.LEFT).isPresent()){
                return Optional.of(OperationType.LEFT_SHIFT);
            }else if(tokenManager.matchAndRemove(TokenType.RIGHT).isPresent()){
                return Optional.of(OperationType.RIGHT_SHIFT);
            }else{
                //if not, throw an exception.
                throw new Exception("Missing keyword near SHIFT statement.");
            }
        }
        if(tokenManager.matchAndRemove(TokenType.ADD).isPresent())
            return Optional.of(OperationType.ADD);
        if(tokenManager.matchAndRemove(TokenType.SUBTRACT).isPresent())
            return Optional.of(OperationType.SUBTRACT);
        if(tokenManager.matchAndRemove(TokenType.MULTIPLY).isPresent())
            return Optional.of(OperationType.MULTIPLY);
        return Optional.empty();
    }
    public Optional<OperationType> ParseBooleanOperations(){
        //return if the current token is any of the boolean operations
        //and return its respective OperationType.
        if(tokenManager.matchAndRemove(TokenType.EQUAL).isPresent())
            return Optional.of(OperationType.EQUALS);
        if(tokenManager.matchAndRemove(TokenType.UNEQUAL).isPresent())
            return Optional.of(OperationType.NOT_EQUALS);
        if(tokenManager.matchAndRemove(TokenType.LESS).isPresent())
            return Optional.of(OperationType.LESS_THAN);
        if(tokenManager.matchAndRemove(TokenType.GREATER_OR_EQUAL).isPresent())
            return Optional.of(OperationType.GREATER_THAN_OR_EQUAL);
        if(tokenManager.matchAndRemove(TokenType.GREATER).isPresent())
            return Optional.of(OperationType.GREATER_THAN);
        if(tokenManager.matchAndRemove(TokenType.LESS_OR_EQUAL).isPresent())
            return Optional.of(OperationType.LESS_THAN_OR_EQUAL);
        return Optional.empty();//if not found, return empty.
    }
}