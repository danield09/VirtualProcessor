public class Word {
    private Bit[] longWord;

    //Constructor makes an array of all false bits.
    public Word(){
        longWord = new Bit[32];//Initializes array
        //Sets all indexes of array to false bits.
        for(int i = 0; i < longWord.length;i++){
            longWord[i] = new Bit();
        }
    }
    //Constructor that copy bits into this new word.
    public Word(Word other){
        longWord = new Bit[32];//Initializes array
        //Sets all indexes of array to false bits.
        for(int i = 0; i < longWord.length;i++){
            longWord[i] = new Bit();
        }
        copy(other);//Calls copy method in order to copy bits over.
    }
    //Get a new assignment1.Bit that has the same value as bit i.
    public Bit getBit(int i){
        return new Bit(longWord[i].getValue());
    }
    //sets the bit with the value of "value" at "i"th position.
    public void setBit(int i, Bit value){
        longWord[i].set(value.getValue());
    }
    //and gate for two words.
    public Word and(Word other){
        //New word for return statement.
        Word addResult = new Word();
        for(int i = 0; i < longWord.length;i++){
            //Get the result between two bits with add method.
            Bit resultBit = longWord[i].and(other.getBit(i));
            //Set it to the return assignment1.Word object.
            addResult.setBit(i, resultBit);
        }
        return addResult;
    }
    //or gate for two words.
    public Word or(Word other){
        //New word for return statement.
        Word orResult = new Word();
        for(int i = 0; i < longWord.length;i++){
            //Get the result between two bits with or method.
            Bit resultBit = longWord[i].or(other.getBit(i));
            //Set it to the return assignment1.Word object.
            orResult.setBit(i, resultBit);
        }
        return orResult;
    }
    //xor gate for two words.
    public Word xor(Word other){
        //New word for return statement.
        Word xorResult = new Word();
        for(int i = 0; i < longWord.length;i++){
            //Get the result between two bits with xor method.
            Bit resultBit = longWord[i].xor(other.getBit(i));
            //Set it to the return assignment1.Word object.
            xorResult.setBit(i, resultBit);
        }
        return xorResult;
    }
    //negate the word, return new assignment1.Word.
    public Word not(){
        //new assignment1.Word object for return statement.
        Word notWord = new Word();
        for(int i = 0; i < longWord.length;i++){
            //set the bit at "i" th position with the opposite value of getBit(i).
            notWord.setBit(i, getBit(i).not());
        }
        return notWord;
    }
    public Word rightShift(int amount){
        //New word for return statement
        Word shiftWord = new Word();
        //we start at "amount"
        for(int i = amount;i < longWord.length;i++) {
            //we want to set the bit at amount to the index "i - amount"
            //this allows a right shift to occur between the words.
            shiftWord.setBit(i - amount, getBit(i));
        }
        return shiftWord;
    }
    public Word leftShift(int amount){
        //New word for return statement.
        Word shiftWord = new Word();
        //we start at "amount" but end at "longWord.length-amount"
        for(int i = 0; i < longWord.length-amount;i++){
            //we want to se the bit at "amount+i" with the bit at "i"
            //this allows a left shift to occur between the words.
            shiftWord.setBit(amount+i, getBit(i));
        }
        return shiftWord;
    }
    //toString
    //Start from the highest bit to lowest bit in order
    //to simulate a binary number.
    public String toString(){
        String retVal = "";
        for(int i = longWord.length-1; i > -1;i--){
            //adds "t" if true
            if(longWord[i].getValue()){
                retVal += "t, ";
            }else{
                //adds "f" if false.
                retVal += "f, ";
            }
        }
        return retVal;
    }
    //returns the value of the word as a long.
    public long getUnsigned(){
        long retVal = 0;
        for(int i = 0; i < longWord.length;i++){
            //If the bit is true at "i" location.
            if(getBit(i).getValue()){
                //we add the value of (2^i) where i is the location.
                retVal += Math.pow(2, i);
            }
        }
        return retVal;
    }
    //returns the value of the word as an int.
    public int getSigned(){
        //if the last bit is true, then it is a negative number.
        if(getBit(31).getValue()){
            //since we need to modify the current word in order to
            //get the original word, we create a new word that
            //contains the same bits.
            Word remainWord = new Word(this);
            Word convertWord = new Word(this);
            boolean status = true;
            int pos = 0;
            //For Two Complements in this situation, we need to subtract 1
            //from the word.
            while(status && pos < 31){
                //if the bit at pos is true, we can subtract 1.
                if(convertWord.getBit(pos).getValue()){
                    //we set that bit at pos to false.
                    convertWord.setBit(pos, new Bit(false));
                    //leave this loop.
                    status = false;
                }else{
                    //if the bit at pos is false, we need to carry from another value.
                    //set this bit at pos to true.
                    convertWord.setBit(pos, new Bit(true));
                }
                //move the position.
                pos++;
            }
            if(status){
                convertWord.copy(remainWord);
            }
            //For Two Complements, we need to negate the word.
            convertWord = convertWord.not();
            //Now, we can treat it as a normal number.
            int retVal = 0;
            for(int i = 0; i < longWord.length-1;i++){
                //For every value of true at "i" location, add to retVal.
                if(convertWord.getBit(i).getValue()){
                    //add (2^i) to retVal.
                    retVal += Math.pow(2, i);
                }
            }

            if(status){
                retVal *= -1;
                return retVal-1;
            }
            //We negate the number since this is a negative number in binary.
            return retVal*-1;

        }else{
            int retVal = 0;
            for(int i = 0; i < longWord.length-1;i++){
                if(getBit(i).getValue()){
                    retVal += Math.pow(2, i);
                }
            }
            return retVal;
        }
    }
    //copes the bits from one word to this word.
    public void copy(Word other){
        for(int i = 0; i < longWord.length;i++){
            //copy each value from other to this word.
            setBit(i, other.getBit(i));
        }
    }
    //set the value of bits in the word to the integer value.
    public void set(int value) {
        if(value == -2147483648){
            setBit(31, new Bit(true));
            return;
        }
        int currValue = value;//Save the current value.
        //If it is a negative word, we want to treat it as a positive number
        //before applying Two Complements.
        if(currValue < 0) currValue *= -1;
        //we start at the highest bit location.
        int position = 31;
        while(position != -1){
            //We want to see what bits can exist in this number at
            //specific location.
            double temp = currValue - (Math.pow(2, position));
            if(temp >= 0){
                //If it can exist, then we can subtract that from currValue
                //and set that position to true in the assignment1.Word.
                currValue -= Math.pow(2, position);
                setBit(position, new Bit(true));
                //if there are no more numbers to insert in the array, we can break
                //out of the loop.
                if(currValue == 0){
                    break;
                }
            }
            //Move the position.
            position--;
        }
        //If it is a positive number, we are done.
        //If it is a negative number, we need to apply Two Complements.
        if(value < 0){
            //Negate the assignment1.Word first.
            copy(not());
            boolean status = true;
            int pos = 0;
            //Now we want to add 1 to the assignment1.Word.
            while(status){
                //If the current bit is false, we can add 1 to 0.
                if(!getBit(pos).getValue()){
                    //Set that bit to true
                    setBit(pos, new Bit(true));
                    //Leaves the loop.
                    status = false;
                }else{
                    //If the current bit is true, we have a carry-out.
                    //We need to find a position to store this
                    //Since 1+1 = 0 in binary, we set this bit to false.
                    setBit(pos, new Bit(false));
                }
                //Move the position.
                pos++;
            }
        }
    }

    public void increment(){
        //on first iteration of incrementing, the secondBit is always true and carryIn is always false.
        //so just focuses on the two bits.
        Bit secondBit = new Bit(true);
        Bit solution = getBit(0).xor(secondBit);//S is solved by bit1.xor(bit2)
        Bit carryIn = getBit(0).and(secondBit);//carryIn is solved by bit1.and(bit2)
        setBit(0, solution);//set the solution in this Word.
        //if there is no carryIn, we don't need to do any more work.
        if(carryIn.getValue()){
            //if so, we need to check if any other column is affected.
            for(int i = 1;i < 32; i++){
                //the second bit is always true, so we just focus on the current bit and carryIn.
                solution = getBit(i).xor(carryIn);
                carryIn = getBit(i).and(carryIn);
                setBit(i, solution);//set the solution in this Word.
            }
        }
    }

    public void decrement(){
        Bit secondBit = new Bit(true);
        Bit solution = getBit(0).xor(secondBit);
        Bit carryIn = getBit(0).xor(secondBit);
        setBit(0, solution);
        if(carryIn.getValue()){
            for(int i = 1; i < 32;i++){
                solution = getBit(i).xor(carryIn);
                carryIn = getBit(i).xor(carryIn);
                setBit(i, solution);
                if(!carryIn.getValue()){
                    break;
                }
            }
        }
    }
}
