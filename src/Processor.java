public class Processor {
    private Word PC;
    private Word SP;
    private Bit haltedStatus;
    private Word currInstruction;
    protected Word[] registers;
    private ALU alu;
    private Word regDestinationValue;
    private Word regDestinationLocation;
    private Word reg1Value;
    private Word reg2Value;
    private Word immediateValue;
    private Word opCode;
    private Word functionCode;
    private Word resultOfALU;
    private Word result;
    private int currentClockCycle;
    

    public Processor() throws Exception {
        //set up members.
        PC = new Word();
        SP = new Word();
        currInstruction = new Word();
        SP.set(1023);//SP starts at 1023.
        haltedStatus = new Bit();
        alu = new ALU();

        //sets all registers to 0 on startup.
        registers = new Word[32];
        for(int i = 0; i < 32;i++){
            registers[i] = new Word();
        }

        //these members are used mainly in
        regDestinationValue = new Word();
        regDestinationLocation = new Word();
        reg1Value = new Word();
        reg2Value = new Word();
        immediateValue = new Word();
        opCode = new Word();
        functionCode = new Word();
        resultOfALU = new Word();
        result = new Word();
        currentClockCycle = 0;
    }

    public void run() throws Exception {
        //The simple loop mentioned in the document.
        while(!haltedStatus.getValue()){//false = not halted
            fetch();
            decode();
            execute();
            store();
        }
    }

    public void decode(){
        //set up mask for opCode
        Word mask = new Word();
        for(int i = 0; i < 5;i++){
            mask.setBit(i, new Bit(true));
        }
        //since the opCode is at the beginning of the instruction, no need to shift
        //mask using and with the current instruction.
        Word opCodeWord = mask.and(currInstruction);
        //copy the content from the mask result.
        opCode.copy(opCodeWord);
        if(!opCode.getBit(0).getValue() && !opCode.getBit(1).getValue()){//...00
            //if the first 2 bits are 00, then we only need to worry about the immediate value
            //set up a mask with 27 true bits.
            Word immediateMask = new Word();
            for(int i = 0; i < 27;i++){
                immediateMask.setBit(i, new Bit(true));
            }
            //left shift by 5 since we need to get everything after opCode.
            immediateMask = immediateMask.leftShift(5);
            //apply the mask to the currentInstruction
            Word immediateWord = immediateMask.and(currInstruction);
            //undo the shift using rightShift and copy the content to immediateValue
            immediateWord = immediateWord.rightShift(5);
            immediateValue.copy(immediateWord);
        }else{
            //if the opCode is 01, 10, 11, then decode the Rd.
            //since the mask is 5 bits again, we can reuse the mask from opCode but shift it to the correct position
            mask = mask.leftShift(5);
            //use the mask and undo the shift for the correct value.
            Word registerDestination = mask.and(currInstruction);
            registerDestination = registerDestination.rightShift(5);
            //copy the value to regDestinationLocation for store()
            regDestinationLocation.copy(registerDestination);
            //copy the value stored in that register for execute()
            regDestinationValue.copy(registers[getRegisterIndex(registerDestination)]);

            //then we need to decode the function
            //create a new mask since it is 4 bits.
            Word funcMask = new Word();
            for(int i = 0; i < 4;i++){
                funcMask.setBit(i, new Bit(true));
            }
            //apply shift to get into correct position.
            funcMask = funcMask.leftShift(10);
            //apply the mask and undo the shift
            Word functionWord = funcMask.and(currInstruction);
            functionWord = functionWord.rightShift(10);
            //copy the content to functionCode
            functionCode.copy(functionWord);

            if(opCode.getBit(1).getValue()){//...1X
                //opCode with ...10 or ...11, they have a Rs2.
                //since this is a mask of 5 bits, apply shift to get into correct position.
                mask = mask.leftShift(9);
                //apply the mask and undo the shift
                Word secondRegister = mask.and(currInstruction);
                secondRegister = secondRegister.rightShift(14);
                //store the value from the register (from the result of the mask)
                reg2Value.copy(registers[getRegisterIndex(secondRegister)]);

                if(!opCode.getBit(0).getValue()){//...11
                    //if the opCode is ...11, then there is a Rs1
                    //since this is a mask of 5 bits, apply shift to get into correct position.
                    mask = mask.leftShift(5);
                    //apply the mask and undo the shift
                    Word firstRegister = mask.and(currInstruction);
                    firstRegister = firstRegister.rightShift(19);
                    //stre the value from the register (from the result of the mask)
                    reg1Value.copy(registers[getRegisterIndex(firstRegister)]);

                    //the immediate value is 8 bits long
                    //create a mask that is 8 bits long
                    Word immediateMask = new Word();
                    for(int i = 0; i < 8;i++){
                        immediateMask.setBit(i, new Bit(true));
                    }
                    //apply the correct shift to get into correct position.
                    immediateMask = immediateMask.leftShift(24);
                    //apply the mask and undo the shift.
                    Word immediateWord = immediateMask.and(currInstruction);
                    immediateWord = immediateWord.rightShift(24);
                    //copy the result into immediateValue
                    immediateValue.copy(immediateWord);
                }else{//...10
                    //create a mask of 13 bits for immediate value
                    Word immediateMask = new Word();
                    for(int i = 0; i < 13;i++){
                        immediateMask.setBit(i, new Bit(true));
                    }
                    //apply shift to get into correct position.
                    immediateMask = immediateMask.leftShift(19);
                    //apply the mask and undo the shift.
                    Word immediateWord = immediateMask.and(currInstruction);
                    immediateWord = immediateWord.rightShift(19);
                    //copy the result into immediateValue
                    immediateValue.copy(immediateWord);
                }
            }else{//...01
                //create a mask that is 18 bits long.
                Word immediateMask = new Word();
                for(int i = 0; i < 18;i++){
                    immediateMask.setBit(i, new Bit(true));
                }
                //apply shift to get into correct position.
                immediateMask = immediateMask.leftShift(14);
                //apply mask and undo the shift.
                Word immediateWord = immediateMask.and(currInstruction);
                immediateWord = immediateWord.rightShift(14);
                //copy the result into immediateValue
                immediateValue.copy(immediateWord);
            }
        }
    }
    public void execute() throws Exception {
        Bit[] addOperation = new Bit[4];
        addOperation[0] = new Bit(true);
        addOperation[1] = new Bit(true);
        addOperation[2] = new Bit(true);
        addOperation[3] = new Bit(false);
        Bit[] mathOperation = new Bit[4];
        for(int i = 0; i < 4;i++){
            mathOperation[3-i] = new Bit(functionCode.getBit(i).getValue());
        }
        if(opCode.getBit(2).getValue()){//XX1??
            if(opCode.getBit(3).getValue()){//X11??
                if(!opCode.getBit(4).getValue()){//)011?? - Push
                    if(opCode.getBit(1).getValue()){//011 1?
                        if(opCode.getBit(0).getValue()){//011 11
                            alu.op1.copy(regDestinationValue);//Rd when its 2R
                        }else{//011 10
                            alu.op1.copy(reg1Value);//Rs1 when its 3R
                        }
                        alu.op2.copy(reg2Value);//Both 2R and 3R uses Rs/Rs2
                        try{
                            addCorrectCycles(mathOperation);
                            alu.doOperation(mathOperation);
                        }catch (Exception ignored){ }
                        result.copy(alu.result);//copy the result from ALU
                    }else{//011 0?
                        //011 00 is unused.
                        //has to be 011 01
                        //addition between Rd and imm
                        alu.op1.copy(regDestinationValue);
                        alu.op2.copy(immediateValue);
                        try{
                            addCorrectCycles(mathOperation);
                            alu.doOperation(mathOperation);
                        }catch (Exception ignored){ }
                        result.copy(alu.result);//copy the result from ALU.
                    }
                }
            }else{//X01??
                if(opCode.getBit(4).getValue()){//101?? - Store
                    if(opCode.getBit(1).getValue()){//101 1?
                        //101-11, 101-10 both uses Rs/Rs2 as its value.
                        result.copy(reg2Value);
                    }else{//101 0?
                        //has to be 101 01
                        //101 00 is unused.
                        //101 01 uses imm as its value.
                        result.copy(immediateValue);
                    }
                }else{//001?? - Branch
                    if(opCode.getBit(1).getValue()){//001 1?
                        if(opCode.getBit(0).getValue()){//001 11
                            //determine if it is true or false between Rs and Rd.
                            if(determineBooleanOp(reg2Value, regDestinationValue)){
                                //if true, copy the current PC.
                                result.copy(PC);
                            }else{
                                //if false, add PC and imm
                                alu.op1.copy(PC);
                                alu.op2.copy(immediateValue);
                                try{
                                    addCorrectCycles(addOperation);
                                    alu.doOperation(addOperation);
                                }catch (Exception ignored){ }
                                result.copy(alu.result);//save the result.
                            }
                        }else{//001 10
                            //determine if it is true or false between Rs1 and Rs2
                            if(determineBooleanOp(reg1Value, reg2Value)){
                                //if true, copy the current PC.
                                result.copy(PC);
                            }else{
                                //if false, add PC and imm
                                alu.op1.copy(PC);
                                alu.op2.copy(immediateValue);
                                try{
                                    addCorrectCycles(addOperation);
                                    alu.doOperation(addOperation);
                                }catch (Exception ignored){ }
                                result.copy(alu.result);//save the result.
                            }
                        }
                    }else{//001 0?
                        if(opCode.getBit(0).getValue()){//001 01
                            //add PC and imm.
                            alu.op1.copy(PC);
                            alu.op2.copy(immediateValue);
                            try{
                                addCorrectCycles(addOperation);
                                alu.doOperation(addOperation);
                            }catch (Exception ignored){ }
                            result.copy(alu.result);//save the result.
                        }else{//001 00
                            result.copy(immediateValue);//save the immediate value.
                        }
                    }
                }
            }
        }else{//XX0??
            if(opCode.getBit(3).getValue()){//X10??
                if(opCode.getBit(4).getValue()){//110?? - Pop/Interrupt
                    if(opCode.getBit(1).getValue()){//110 1?
                        Bit[] subOperation = new Bit[4];
                        subOperation[0] = new Bit(true);
                        subOperation[1] = new Bit(true);
                        subOperation[2] = new Bit(true);
                        subOperation[3] = new Bit(true);
                        if(opCode.getBit(0).getValue()){//110 11
                            //110-11 does an add operation between Rs and imm
                            //so do the operation.
                            alu.op1.copy(reg2Value);
                            alu.op2.copy(immediateValue);
                            try{
                                alu.doOperation(addOperation);
                            }catch (Exception ignored){ }
                        }else{//110 10
                            //110-10 does an add operation between Rs1 and Rs2
                            //so do the operation.
                            alu.op1.copy(reg1Value);
                            alu.op2.copy(reg2Value);
                            try{
                                addCorrectCycles(addOperation);
                                alu.doOperation(addOperation);
                            }catch (Exception ignored){ }
                        }
                        //do the subtract from SP and the current result from ALU
                        alu.op1.copy(SP);
                        alu.op2.copy(alu.result);
                        try{
                            addCorrectCycles(subOperation);
                            alu.doOperation(subOperation);
                        }catch (Exception ignored){ }
                        //copy the address from MainMemory into result.
                        result.copy(L2Cache.read(alu.result));//read address from L2Cache
                        currentClockCycle += (50 + L2Cache.getClockCycleCost());//add the correct clock cycles from this operation.
                        //result.copy(MainMemory.read(alu.result));
                        //currentClockCycle += 300;

                    }else{//110 0?
                        if(opCode.getBit(0).getValue()){//110 01
                            //increment the SP and read the address at SP and save it to result.
                            SP.increment();
                            result.copy(L2Cache.read(SP));//read address from L2 Cache
                            currentClockCycle += (50 + L2Cache.getClockCycleCost());//add correct cycles.
                            //result.copy(MainMemory.read(SP));
                            //currentClockCycle += 300;
                        }else{//110 00
                            //No need to implement Interrupt for Assignment 5!
                        }
                    }
                }else{//010?? - Call
                    if(opCode.getBit(1).getValue()){//010 1?
                        if(opCode.getBit(0).getValue()){//010 11
                            //determine if it is true or false between Rs and Rd
                            if(determineBooleanOp(reg2Value, regDestinationValue)){
                                //copy the current PC if true.
                                result.copy(PC);
                            }else{
                                //if false, push the current PC.
                                L2Cache.write(SP, PC);//write into L2Cache.
                                currentClockCycle += (50 + L2Cache.getClockCycleCost());//get clock cycles from operation.
                                //MainMemory.write(SP, PC);
                                //currentClockCycle += 300;
                                SP.decrement();
                                //do an add operation between PC and imm.
                                alu.op1.copy(PC);
                                alu.op2.copy(immediateValue);
                                try{
                                    addCorrectCycles(addOperation);
                                    alu.doOperation(addOperation);
                                }catch (Exception ignored){ }
                                result.copy(alu.result);//save the result
                            }
                        }else{//010 10
                            //determine if it is true or false between Rs1 and Rs2
                            if(determineBooleanOp(reg1Value, reg2Value)){
                                //save the current PC if true.
                                result.copy(PC);
                            }else{
                                //if false, push the current PC.
                                L2Cache.write(SP, PC);//write into L2Cache
                                currentClockCycle += (50 + L2Cache.getClockCycleCost());//get correct clock cycles.
                                //MainMemory.write(SP, PC);
                                //currentClockCycle += 300;
                                SP.decrement();
                                //do an add operation between Rd value and imm
                                alu.op1.copy(regDestinationValue);
                                alu.op2.copy(immediateValue);
                                try{
                                    addCorrectCycles(addOperation);
                                    alu.doOperation(addOperation);
                                }catch (Exception ignored){ }
                                result.copy(alu.result);//save the result.
                            }
                        }
                    }else{//010 0?
                        //push the pc when its both 010-01 and 010-00
                        L2Cache.write(SP, PC);//write into L2Cache
                        currentClockCycle += (50 + L2Cache.getClockCycleCost());//get correct clock cycles
                        //MainMemory.write(SP, PC);
                        //currentClockCycle += 300;
                        SP.decrement();
                        if(opCode.getBit(0).getValue()){//010 01
                            //do an add operation between Rd and imm
                            alu.op1.copy(regDestinationValue);
                            alu.op2.copy(immediateValue);
                            try{
                                addCorrectCycles(addOperation);
                                alu.doOperation(addOperation);
                            }catch (Exception ignored){ }
                            result.copy(alu.result);//save the result
                        }else{//010 00
                            result.copy(immediateValue);//save the immediate value
                        }
                    }
                }
            }else{//X00??
                if(opCode.getBit(4).getValue()){//100?? - Load
                    if(opCode.getBit(1).getValue()){//100 1?
                        if(opCode.getBit(0).getValue()){//100 11
                            //do an add operation between Rd and imm
                            alu.op1.copy(reg2Value);
                            alu.op2.copy(immediateValue);
                            try{
                                addCorrectCycles(addOperation);
                                alu.doOperation(addOperation);
                            }catch (Exception ignored){ }
                            //save the address at the result of the ALU
                            result.copy(L2Cache.read(alu.result));//read address from L2Cache
                            currentClockCycle += (50 + L2Cache.getClockCycleCost());//get correct clock cycles.
                            //currentClockCycle += 300;
                            //result.copy(MainMemory.read(alu.result));
                        }else{//100 10
                            //do an add operation between Rs1 and Rs2
                            alu.op1.copy(reg1Value);
                            alu.op2.copy(reg2Value);
                            try{
                                addCorrectCycles(addOperation);
                                alu.doOperation(addOperation);
                            }catch (Exception ignored){ }
                            //save the address at the result of the ALU.
                            result.copy(L2Cache.read(alu.result));//read address from L2Cache
                            currentClockCycle += (50 + L2Cache.getClockCycleCost());//get correct clock cycles.
                            //result.copy(MainMemory.read(alu.result));
                            //currentClockCycle += 300;
                        }
                    }else{//100 0?
                        if(opCode.getBit(0).getValue()){//100 01
                            //do an add operation between Rd and imm.
                            alu.op1.copy(regDestinationValue);
                            alu.op2.copy(immediateValue);
                            try{
                                addCorrectCycles(addOperation);
                                alu.doOperation(addOperation);
                            }catch (Exception ignored){ }
                            //save the address stored at the result of the ALU.
                            result.copy(L2Cache.read(alu.result));//read address from L2Cache.
                            currentClockCycle += (50 + L2Cache.getClockCycleCost());//get correct clock cycles.
                            //result.copy(MainMemory.read(alu.result));
                            //currentClockCycle += 300;
                        }else{//100 00
                            //this is return, pop the stack.
                            SP.increment();
                            //save the value stored at the SP.
                            result.copy(L2Cache.read(SP));//read address from L2Cache.
                            currentClockCycle += (50 + L2Cache.getClockCycleCost());//get correct clock cycles.
                            //currentClockCycle += 300;
                            //result.copy(MainMemory.read(SP));
                            result.decrement();//this is used to offset to the right position for the PC.
                        }
                    }
                }else{//000?? - Math
                    if(opCode.getBit(1).getValue()){//0001?
                        //if the opCode is 0001X, then it uses the ALU doOperation
                        //transfer the bits from functionCode into an array of bits.
                        Bit[] operation = new Bit[4];
                        for(int i = 0; i < 4;i++){
                            operation[3-i] = functionCode.getBit(i);
                        }
                        if(!opCode.getBit(0).getValue()){//00011 - 3R
                            alu.op1.copy(reg1Value);//if the opCode is 00011, then we use the value from Rs1
                        }else{//00010 - 2R
                            alu.op1.copy(regDestinationValue);//if the opCode is 00010, then we use the value from immediateValue
                        }
                        alu.op2.copy(reg2Value);//both 00011 and 00010 uses Rs2 as its 2nd op.
                        addCorrectCycles(operation);
                        alu.doOperation(operation);//call ALU to do the math operation
                        resultOfALU.copy(alu.result);//copy the result to resultOfALU.
                    }else{//0000?
                        if(opCode.getBit(0).getValue()){//00001 - Dest Only
                            //just copy immediateValue into resultOfALU and store() will do the rest.
                            resultOfALU.copy(immediateValue);
                        }else{//00000 - No R (HALT)
                            //if the opCode is 00000, then set the haltedStatus to true, indicating a halt.
                            haltedStatus.set(true);
                        }
                    }
                }
            }
        }
    }
    public void store() throws Exception {
        Bit[] addOperation = new Bit[4];
        addOperation[0] = new Bit(true);
        addOperation[1] = new Bit(true);
        addOperation[2] = new Bit(true);
        addOperation[3] = new Bit(false);
        if(opCode.getBit(2).getValue()){//XX1??
            if(opCode.getBit(3).getValue()){//X11??
                if(!opCode.getBit(4).getValue()){//)011?? - Push
                    //all 011-11, 011-10, 011-01 store into mem[--sp]
                    L2Cache.write(SP, result);//write into L2Cache.
                    currentClockCycle += (50 + L2Cache.getClockCycleCost());//clock cycles from performing op.
                    //MainMemory.write(SP, result);
                    //currentClockCycle += 300;
                    SP.decrement();
                }
            }else{//X01??
                if(opCode.getBit(4).getValue()){//101?? - Store
                    if(opCode.getBit(1).getValue()){//101 1?
                        //both 101-11 and 101-10 uses Rs2/Rs
                        alu.op1.copy(regDestinationValue);
                        if(opCode.getBit(0).getValue()){//101 11
                            alu.op2.copy(immediateValue);//101-11 uses imm
                        }else{//101 10
                            alu.op2.copy(reg1Value);//101-10 uses Rs1
                        }
                        //do an add operation
                        try{
                            addCorrectCycles(addOperation);
                            alu.doOperation(addOperation);
                        }catch (Exception ignored){ }
                        //write into MainMemory at the current result
                        //with the value of result (from execute)
                        currentClockCycle += (50 + L2Cache.getClockCycleCost());//clock cycle cost from operation.
                        L2Cache.write(alu.result, result);//write into L2Cache.
                        //currentClockCycle += 300;
                        //MainMemory.write(alu.result, result);
                    }else{//101 0?
                        //101 01 is only used.
                        //write into MainMemory at the value of Rd
                        //with the value of result (from execute)
                        currentClockCycle += (50 + L2Cache.getClockCycleCost());//clock cycle cost from operation.
                        L2Cache.write(regDestinationValue, result);//write into L2Cache
                        //currentClockCycle += 300;
                        //MainMemory.write(regDestinationValue, result);
                    }
                }else{//001?? - Branch
                    //all 001-00, 001-01, 001-10, 001-11 all store into the PC
                    //whatever result is (done in execute)
                    PC.copy(result);
                }
            }
        }else{//XX0??
            if(opCode.getBit(3).getValue()){//X10??
                if(opCode.getBit(4).getValue()){//110?? - Pop/Interrupt
                    if(!opCode.getBit(0).getValue() && !opCode.getBit(1).getValue()){
                        //this is used for Interrupt, implement later.
                    }else{
                        //all 110-11, 110-10, 110-01 stored into register Rd
                        if(getRegisterIndex(regDestinationLocation) != 0)
                            registers[getRegisterIndex(regDestinationLocation)].copy(result);
                    }
                }else{//010?? - Call
                    //all 010-00, 010-01, 010-10, 010-11 all store into the PC
                    //whatever result is (done in execute)
                    PC.copy(result);
                }
            }else{//X00??
                if(opCode.getBit(4).getValue()){//100?? - Load
                    if(!opCode.getBit(1).getValue() && !opCode.getBit(0).getValue()){//100 00
                        //100-00 is return, load the PC with the result (done in execute)
                        PC.copy(result);
                    }else{//100 01, 100 10, 100 11
                        //all 100 01, 100 10, 100 11, store into Rd.
                        //make sure to not store into R0.
                        if(getRegisterIndex(regDestinationLocation) != 0)
                            registers[getRegisterIndex(regDestinationLocation)].copy(result);
                    }
                }else{//000?? - Math
                    //00011, 00010, 00001 all store in the same location

                    //this is used to ensure when the opCode is 00000, it doesn't write anything.
                    if(!opCode.getBit(0).getValue() && !opCode.getBit(1).getValue()){
                        return;
                    }
                    //the only check is if the regDestination is R0
                    //if so, don't write to it.
                    if(getRegisterIndex(regDestinationLocation) != 0)
                        registers[getRegisterIndex(regDestinationLocation)].copy(resultOfALU);
                }
            }
        }
    }
    public void fetch() throws Exception {
        /*
        //gets the instruction from memory and copy the values from memory to currInstruction.
        currInstruction.copy(MainMemory.read(PC));
        currentClockCycle += 300;
        PC.increment();//PC increments by 1.
         */
        currInstruction.copy(InstructionCache.read(PC));//read next instruction from InstructionCache
        currentClockCycle += InstructionCache.getClockCycleCost();//get correct clock cycles and add
        PC.increment();//PC increments by 1.

    }
    public boolean determineBooleanOp(Word value1, Word value2){
        Bit[] subtractOp = new Bit[4];
        for(int i = 0; i < subtractOp.length;i++){
            subtractOp[i] = new Bit(true);
        }

        //in order to determine boolean operations, do a subtract operation
        //between value1 and value2.
        alu.op1.copy(value1);
        alu.op2.copy(value2);
        try{
            currentClockCycle += 2;
            alu.doOperation(subtractOp);
        }catch (Exception ignored){ }

        if(functionCode.getBit(0).getValue()){//0??1
            if(functionCode.getBit(1).getValue()){//0?11
                //0011 is the only value, 0111 is in ALU
                //0011 - Greater than or equal to
                //check the last bit to see if true.

                //if it is a negative value, it is false.
                if(!alu.result.getBit(31).getValue()) {
                    //if it is a positive value, check if it is a zero.
                    for (int i = 0; i < 31; i++) {
                        //if any other true bits, it is not zero, return true.
                        if (alu.result.getBit(i).getValue()) {
                            return true;
                        }
                    }
                }
                return false;


                //return !alu.result.getBit(31).getValue();
            }else{//0?01
                if(functionCode.getBit(2).getValue()){//0101
                    //0101 - Less than or equal to
                    //if it is last bit is true.
                    if(!alu.result.getBit(31).getValue()) {
                        //if not, check if it is a 0.
                        for (int i = 0; i < 31; i++) {
                            //if there is any other true bits, return false since it not 0.
                            if(alu.result.getBit(i).getValue()) {
                                return false;
                            }
                        }
                    }
                    //return true if it is 0 or a negative value.
                    return true;
                }else{//0001
                    //0001 - Not equal
                    //checks last bit to see if it is true.
                    if(!alu.result.getBit(31).getValue()){
                        //if not, check if it is zero.
                        for(int i = 0; i < 31;i++){
                            //if there is any other true bits, return true since it not 0.
                            if(alu.result.getBit(i).getValue()){
                                return true;
                            }
                        }
                        //return false since it is 0.
                        return false;
                    }
                    //return true if it is a negative value.
                    return true;
                }
            }
        }else{//0??0
            if(functionCode.getBit(1).getValue()){//0?10
                //has to be 0010, no 0110 exists
                //0010 - Less than
                //if the last bit is true, then it is true.
                return alu.result.getBit(31).getValue();
            }else{//0?00
                if(functionCode.getBit(2).getValue()){//0100
                    //0100 - Greater than
                    //if it is a negative value, it is true.
                    return !alu.result.getBit(31).getValue();
                }else{//0000
                    //0000 - Equals
                    //if it is negative value, it is false.
                    if(!alu.result.getBit(31).getValue()){
                        //if it is positive value, check if it is zero.
                        for(int i = 0; i < 31;i++){
                            //any other true bits means it is not zero.
                            if(alu.result.getBit(31).getValue()){
                                return false;
                            }
                        }
                        //if 0, return true.
                        return true;
                    }
                    return false;
                }
            }
        }
    }
    //Used for testing.
    public Word[] getRegisters(){
        return registers;
    }
    //getRegisterIndex is used to determine the register index.
    public int getRegisterIndex(Word reg){
        int value = 0;
        //if the first bit is true, add 1
        if(reg.getBit(0).getValue()){
            value = value + 1;
        }
        //if the second bit is true, add 2
        if(reg.getBit(1).getValue()){
            value = value + 2;
        }
        //if the third bit is true, add 4
        if(reg.getBit(2).getValue()){
            value = value + 4;
        }
        //if the fourth bit is true, add 8
        if(reg.getBit(3).getValue()){
            value = value + 8;
        }
        //if the fifth bit is true, add 16
        if(reg.getBit(4).getValue()){
            value = value + 16;
        }
        return value;
    }
    //used to determine how many clock cycles an ALU operation does.
    public void addCorrectCycles(Bit[] op){
        if(!op[0].getValue() && op[1].getValue() && op[2].getValue() && op[3].getValue()){
            currentClockCycle += 10;//multiply is 10
        }else{
            currentClockCycle += 2;//everything else is 2.
        }
    }
}

