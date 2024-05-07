enum Operations{
    AND, OR, XOR, NOT, LEFT_SHIFT, RIGHT_SHIFT, ADD, SUBTRACT, MULTIPLY
}
public class ALU {
    public Word op1 = new Word();
    public Word op2 = new Word();
    public Word result = new Word();
    public void doOperation(Bit[] operation) throws Exception {
        //calls the method to determine what operation to execute.
        Operations currOperation = determineOperation(operation);
        //This should never occur on a normal execution. but just in case...
        if(currOperation == null){
            throw new Exception("Invalid Operation.");
        }

        //determine what operation.
        switch(currOperation){
            case AND:
                //calls the and method in Word and sets to result
                Word andWord = op1.and(op2);
                result.copy(andWord);
                break;
            case OR:
                //calls the or method in Word and sets to result
                Word orWord = op1.or(op2);
                result.copy(orWord);
                break;
            case XOR:
                //calls the xor method in Word and sets to result
                Word xorWord = op1.xor(op2);
                result.copy(xorWord);
                break;
            case NOT:
                //calls the not method in Word and sets to result
                Word notWord = op1.not();
                result.copy(notWord);
                break;
            case LEFT_SHIFT:
                //calls determine shift to get the integer value
                //then calls the left shift method in Word and sets to result.
                Word leftShiftWord = op1.leftShift(determineShift());
                result.copy(leftShiftWord);
                break;
            case RIGHT_SHIFT:
                //calls determine shift to get the integer value
                //then calls the right shift method in Word and sets to result.
                Word rightShiftWord = op1.rightShift(determineShift());
                result.copy(rightShiftWord);
                break;
            case ADD:
                //calls add2 since it is addition between two words.
                //stores the result to result.
                Word addResult = add2(op1, op2);
                result.copy(addResult);
                break;
            case SUBTRACT:
                //a-b is the same as a + (-b) which means a + ( not(b) + 1)
                //Save the current op2 since we need to modify with Two Complements.
                Word modifiedOp2 = new Word();
                modifiedOp2.copy(op2);
                //we negate the whole word.
                modifiedOp2 = modifiedOp2.not();
                //then we add 1 to the word.
                boolean status = true;
                int pos = 0;
                while(status){
                    if(!modifiedOp2.getBit(pos).getValue()){
                        modifiedOp2.setBit(pos, new Bit(true));
                        status = false;
                    }else{
                        modifiedOp2.setBit(pos, new Bit(false));
                    }
                    pos++;
                }

                //now we can call add2 with op1 and the new op2 word.
                //store to result.
                Word subtractResult = add2(op1, modifiedOp2);
                result.copy(subtractResult);
                break;
            case MULTIPLY:
                //we need to determine all the multiplication words, 32 possible Words.
                Word[] multiplicationArray = new Word[32];
                for(int i = 0; i < 32; i++){
                    multiplicationArray[i] = new Word();
                    //if the value at that position is 0, no need to execute any instructions since it will be all 0.
                    //if the value is 1, we need to do some math.
                    if(op2.getBit(i).getValue()){
                        for(int j = 0; j < 32; j++){
                            //we use and to determine if it is still a 1 or 0 at that poosition.
                            Bit result = op2.getBit(i).and(op1.getBit(j));
                            //set it to the current word in the array.
                            multiplicationArray[i].setBit(j, result);
                        }
                    }
                    //for every column we move, we need to add the same amount of 0 to the current word
                    //we achieve this by left shifting the current word by i
                    multiplicationArray[i] = multiplicationArray[i].leftShift(i);
                }

                //first round, we use 8 add4 calls to get 8 words.
                Word[] firstRoundArray = new Word[8];//store those 8 words in this array.
                for(int i = 0; i < 8;i++){
                    //we take from the multiplication array in group of 4.
                    //and set the result to the current word in the new array.
                    firstRoundArray[i] = add4(multiplicationArray[(4*i)],
                            multiplicationArray[(4*i)+1],
                            multiplicationArray[(4*i)+2],
                            multiplicationArray[(4*i)+3]);
                }
                //second round, we use 2 add4 calls to get 2 words.
                Word[] secondRoundArray = new Word[2];//store those 2 words in this array.
                for(int i = 0; i < 2;i++){
                    //we take from the firstRound array in group of 4.
                    //and set the result to the current word in the new array.
                    secondRoundArray[i]= add4(firstRoundArray[(4*i)],
                            firstRoundArray[(4*i)+1],
                            firstRoundArray[(4*i)+2],
                            firstRoundArray[(4*i)+3]);
                }

                //last round, we use a add2 and get our result.
                Word multiResult = add2(secondRoundArray[0], secondRoundArray[1]);
                result.copy(multiResult);//set that result to our result word.

                //do whatever phipps stated.
                break;
        }
    }
    public Operations determineOperation(Bit[] operation){
        //determines what operation we are performing.
        if(operation[0].getValue()){//1...
            if(operation[1].getValue()){//11..
                if(operation[2].getValue()){//111.
                    if(operation[3].getValue()){//1111
                        return Operations.SUBTRACT;
                    }else{//1110
                        return Operations.ADD;
                    }
                }else{//1101
                    if(operation[3].getValue()){
                        return Operations.RIGHT_SHIFT;
                    }else{//1100
                        return Operations.LEFT_SHIFT;
                    }
                }
            }else{//10..
                if(operation[2].getValue()){//101.
                    if(operation[3].getValue()){//1011
                        return Operations.NOT;
                    }else{//1010
                        return Operations.XOR;
                    }
                }else{//100.
                    if(operation[3].getValue()){//1001
                        return Operations.OR;
                    }else{//1000
                        return Operations.AND;
                    }
                }
            }
        }else{//0111
            if(operation[1].getValue() && operation[2].getValue() && operation[3].getValue()){
                return Operations.MULTIPLY;
            }
        }
        //it should NOT reach this point, unless a bad array was sent.
        return null;
    }
    public int determineShift(){
        //this is used to determine the shift but we need to ignore all but the 5 lowest bits.
        int retVal = 0;
        //we only care about those 5 bits.
        for(int i = 0; i < 5;i++){
            //if it is true at that position, we calculate its value and add it to retVal.
            if(op2.getBit(i).getValue()){
                retVal += Math.pow(2, i);
            }
        }
        return retVal;
    }

    public Word add2(Word x, Word y){
        //set up variables.
        Bit carryIn = new Bit(false);
        Word retVal = new Word();
        for(int i = 0; i < 32;i++){
            //to get the column result, use x XOR y XOR carryIn
            Bit resultCol = x.getBit(i).xor(y.getBit(i)).xor(carryIn);
            //to get the carryOut, use (x AND y) OR ( (x XOR y) AND carryIn)
            carryIn = (x.getBit(i).and(y.getBit(i)).or((x.getBit(i).xor(y.getBit(i))).and(carryIn)));
            //set the value to retVal.
            retVal.setBit(i, resultCol);
        }
        return retVal;
    }

    public Word add4(Word a, Word b, Word c, Word d){
        //this array will help keep track of the carryIn throughout the whole addition.
        Bit[] carryInArray = new Bit[32];
        for(int i = 0; i < carryInArray.length;i++){
            carryInArray[i] = new Bit();
        }
        Word retVal = new Word();

        for(int i = 0; i < 32;i++){
            //we take the current carryIn from the array.
            Bit carryIn = carryInArray[i];
            //to get the result in that column, we use (a XOR b XOR c XOR d XOR carryIn)
            Bit resultCol = (a.getBit(i).xor((b.getBit(i)).xor(c.getBit(i)).xor((d.getBit(i)).xor(carryIn))));
            //to get a carryIn in the next column, we use:
            //( (a AND b) OR ( (a XOR b) AND carryIn) ) XOR ( (a AND c) OR ( (a XOR c) AND carryIn) ) XOR
            //( (a AND d) OR ( (a XOR d) AND carryIn) ) XOR ( (b AND c) OR ( (b XOR c) AND carryIn) ) XOR
            //( (b AND d) OR ( (b XOR d) AND carryIn) ) XOR ( (c AND d) OR ( (c XOR d) AND carryIn) )
            Bit carryInCol1 = ((a.getBit(i).and(b.getBit(i))).or(((a.getBit(i).xor(b.getBit(i))).and(carryIn)))).xor(
                    ((a.getBit(i).and(c.getBit(i))).or(((a.getBit(i).xor(c.getBit(i))).and(carryIn)))).xor(
                    ((a.getBit(i).and(d.getBit(i))).or(((a.getBit(i).xor(d.getBit(i))).and(carryIn)))).xor(
                    ((b.getBit(i).and(c.getBit(i))).or(((b.getBit(i).xor(c.getBit(i))).and(carryIn)))).xor(
                    ((b.getBit(i).and(d.getBit(i))).or(((b.getBit(i).xor(d.getBit(i))).and(carryIn)))).xor(
                    ((c.getBit(i).and(d.getBit(i))).or(((c.getBit(i).xor(d.getBit(i))).and(carryIn)))))))));
            //to determine if there is a carryIn in the next-NEXT column (a carryIn of 10 in binary), we use:
            //(a AND b AND c AND d) OR (a AND c AND d AND carryIn) OR (a AND b AND d AND carryIn) OR
            //(a AND b AND c AND carryIn) OR (b AND c AND d AND carryIn)
            Bit carryInCol2 = (a.getBit(i).and(b.getBit(i).and(c.getBit(i).and(d.getBit(i))))).or(
                    (a.getBit(i).and(b.getBit(i).and(c.getBit(i).and(carryIn)))).or(
                    (a.getBit(i).and(b.getBit(i).and(d.getBit(i).and(carryIn)))).or(
                    (a.getBit(i).and(c.getBit(i).and(d.getBit(i).and(carryIn)))).or(
                    (a.getBit(i).and(b.getBit(i).and(c.getBit(i).and(carryIn)))).or(
                    (b.getBit(i).and(c.getBit(i).and(d.getBit(i).and(carryIn)))))))));
            //set the column result to retVal.
            retVal.setBit(i, resultCol);
            //there COULD be a carry In the carryIn array, check for that.
            //if there already exists a value, we need to move it.
            if(carryInCol1.getValue()){
                boolean status = true;
                //we move at the NEXT column in the array
                int position = i + 1;
                //since we are ignoring overflow, we can stop keep track of values over 32 position.
                while(status && position < 31){
                    //if there is a empty value, we can put a 1 there.
                    if(!carryInArray[position].getValue()){
                        carryInArray[position].set(true);
                        //break out of the loop
                        status = false;
                    }else{
                        //if there is a 1 there, we set it to 0 and loop.
                        carryInArray[position].set(false);
                    }
                    position++;
                }
            }


            //similar logic to carryInCol1, we need to check if there is a possible carry in the array
            if(carryInCol2.getValue()){
                boolean status = true;
                //we look at the NEXT NEXT column in the array
                int position = i + 2;
                //we dont need to keep track of values over 32 positions.
                while(status && position < 31){
                    //if there is a 0 at that position, we can put a 1 there.
                    if(!carryInArray[position].getValue()){
                        carryInArray[position].set(true);
                        //break out of the loop
                        status = false;
                    }else{
                        //if there is a 1, there is a carryIn. set to 0 and loop.
                        carryInArray[position].set(false);
                    }
                    position++;
                }
            }
        }
        return retVal;
    }
}
