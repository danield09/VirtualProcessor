public class MainMemory {
    private static Word[] memory = new Word[1024];
    public static Word read(Word address) throws Exception {
        //convert the address to a value.
        int addressValue = (int)address.getUnsigned();
        //throw an exception when it is outside of memory.
        if(addressValue > 1023){
            throw new Exception("Invalid Memory Address");
        }
        //get the word in that memory address
        Word currWord = memory[addressValue];
        if(currWord == null){
            //if it is null, it would be empty so just return a new, empty Word
            return new Word();
        }
        //if it is not null, return a new Word with a copy constructor.
        return new Word(currWord);
    }

    public static void write(Word address, Word value) throws Exception {
        //convert the address to a value.
        int addressValue = (int) address.getUnsigned();
        //throw an exception when it is outside of memory.
        if (addressValue > 1023){
            throw new Exception("Invalid Memory Address");
        }
        if(value == null){
            value = new Word();
        }
        if(memory[addressValue] == null){
            //if the memory address is null, initialize it.
            memory[addressValue] = new Word();
        }
        //if it is not null, we can just overwrite the data.
        for(int i = 0; i < 32;i++){
            //transfer the data from Word value to memory address bit by bit.
            memory[addressValue].setBit(i, value.getBit(i));
        }
    }

    public static void load(String[] data){
        //loops through the data array.
        for(int j = 0; j < data.length;j++){
            //if the memory address is null, initialize it.
            if(memory[j] == null){
                memory[j] = new Word();
            }
            //loops through the string.
            for(int i = 0; i < 32;i++){
                //try/catch is here to avoid crashing the program when processing strings
                //that aren't 32 characters in length.
                try {
                    //get the character at "i".
                    char currChar = data[j].charAt(i);
                    //if the character is 0, that is a false bit.
                    if (currChar == '0') {
                        //I use "31-i" since we read binary from right to left. just "i" will have it in the order
                        //from left to right.
                        memory[j].setBit(31-i, new Bit());
                    } else {
                        //if it is not a 0, we can assume it is a 1.
                        //"31-i" since we read binary from right to left. just "i" will have it in the order
                        //from left to right.
                        memory[j].setBit(31-i, new Bit(true));
                    }
                    //if there is no more characters in the strings, we can fill rest of the memory as false bits.
                }catch (StringIndexOutOfBoundsException e){
                    //loops through the rest of the memory address, setting the bits to false.
                    for(int a = i; a < 32;a++) {
                        memory[j].setBit(31 - a, new Bit());
                    }
                }
            }
        }
    }

    //This method is used only for testing!
    public static Word[] getMemory(){
        return memory;
    }
}
