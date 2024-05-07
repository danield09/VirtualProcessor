public class L2Cache{
    private static Word[][] cacheList = new Word[4][8];
    private static Word[] cacheAddresses = new Word[4];
    private static int clockCycleCost;
    private static int nextAddress = 0;
    public static Word[] fillCache(Word address) throws Exception {
        clockCycleCost = 0;//initialize clock costs back to 0.
        //this list is mainly used for InstructionCache.
        Word[] returnArray = new Word[9];//set up return value.
        //iterate through all 4 possible addresses.
        for(int i = 0; i < cacheAddresses.length; i++){
            //if cacheAddresses is null, we consider this a cache miss.
            if(cacheAddresses[i] == null){
                cacheAddresses[i] = new Word();//initialize the cache addresses.
                cacheAddresses[i].copy(address);//copy the address into cach address.
                Word addressCounter = new Word();//used to increment.
                addressCounter.copy(cacheAddresses[i]);//copy the cacheAddress to adressCounter.
                for(int j = 0; j < 8; j++){
                    cacheList[i][j] = new Word();//initialize the word in cacheList.
                    cacheList[i][j].copy(MainMemory.read(addressCounter));//copy the address value from MainMemory
                    //into the cacheList.
                    addressCounter.increment();//increment the addressCounter.

                    returnArray[j] = new Word();//initialize word in return array.
                    returnArray[j].copy(cacheList[i][j]);//copy into the word in return array.
                }
                returnArray[8] = new Word();//initialize the starting address in return array.
                returnArray[8].copy(cacheAddresses[i]);//copy the address into the starting address.
                clockCycleCost = 350;//this operation costs 350 clock cycles.
                return returnArray;
            }else{
                //if the cacheAddresses is not null, then there is an address to address+8
                Word addressCounter = new Word();//initialize addressCounter.
                addressCounter.copy(cacheAddresses[i]);//copy the cacheAddress at i into addressCounter.
                for(int j = 0; j < 8; j++){
                    Word compareAddresses = new Word();
                    //xor between addressCounter and address will give a full false word if equal.
                    compareAddresses.copy(addressCounter.xor(address));
                    boolean flag = true;
                    for(int k = 0; k < 32; k++){
                        //if there is any true bits, then the addresses are not equal.
                        if(compareAddresses.getBit(k).getValue()){
                            flag = false;
                        }
                    }

                    if(flag){
                        //if the addresses are equal, then we can copy value into return array.
                        for(int k = 0; k < 8; k++){
                            returnArray[k] = new Word();//initialize each address word.
                            returnArray[k].copy(cacheList[i][k]);//copy the correct cacheList into the array.
                        }

                        returnArray[8] = new Word();//initialize the starting address.
                        returnArray[8].copy(cacheAddresses[i]);//copy the starting address.
                        clockCycleCost = 0;//this operation does not have a cost.
                        return returnArray;//return the list.
                    }
                    addressCounter.increment();//increment the addressCounter
                }
            }
        }

        //if the cache is full but does not find the address, we need to replace one of the lists.
        //nextAddress is a integer between 0-3.
        cacheAddresses[nextAddress].copy(address);//this copy the new address.
        Word addressCounter = new Word();
        addressCounter.copy(cacheAddresses[nextAddress]);
        for(int i = 0; i < 8; i++){
            //read and put the correct address into the cacheList[nextAddress];
            cacheList[nextAddress][i].copy(MainMemory.read(addressCounter));
            returnArray[i] = new Word();//initialize each word in the array.
            returnArray[i].copy(cacheList[nextAddress][i]);//copy each address into the array.
            addressCounter.increment();//increment the addressCounter.
        }

        returnArray[8] = new Word();//initialize the starting address.
        returnArray[8].copy(cacheAddresses[nextAddress]);//copy the starting address.

        nextAddress++;//increments the nextAddress by 1.
        nextAddress = nextAddress % 4;//module 4 so the integer is always between 0-3
        clockCycleCost = 350;//this operations costs 350 clock cycles.
        return returnArray;
    }
    public static Word read(Word address) throws Exception {
        //iterate through the cacheAddresses
        for(int i = 0; i < cacheAddresses.length;i++){
            if(cacheAddresses[i] == null){
                //if cacheAddresses is null, this is a cache miss, we need to fill this cache.
                //in this case, we do not use the list returned fillCache.
                fillCache(address);
            }
            //addressCounter to increment from cacheAddresses[i] to cacheAddresses[i] + 8;
            Word addressCounter = new Word();
            addressCounter.copy(cacheAddresses[i]);
            for(int j = 0; j < 8; j++){
                Word result = new Word();
                //xor checks if the addressCounter and address are equal or not.
                result.copy(addressCounter.xor(address));
                boolean flag = true;
                for(int k = 0; k < 32; k++){
                    //if there are any true bits, then the two addresses do not match.
                    if(result.getBit(k).getValue()){
                        flag = false;
                    }
                }

                if(flag){
                    //if they do match, return the correct address
                    //this operation does not cost anything.
                    return cacheList[i][j];
                }
                addressCounter.increment();//increment the addressCounter.
            }
        }
        //if this reaches here, it means this cache is full but does not contain the address.
        fillCache(address);//re-populate this cache with this address
        return read(address);//call this method again and return.
    }
    public static void write(Word address, Word value) throws Exception {
        //iterate through all 4 cache addresses.
        for(int i = 0; i < cacheAddresses.length; i++){
            if(cacheAddresses[i] == null){
                //if cacheAddress is null, we have a cache miss.
                fillCache(address);//re-populate this cache with this address.
            }
            //addressCounter to increment from cacheAddresses[i] to cacheAddresses[i] + 8;
            Word addressCounter = new Word();
            addressCounter.copy(cacheAddresses[i]);
            for(int j = 0; j < 8; j++){
                Word result = new Word();
                //xor checks if the two addresses match or not.
                result.copy(addressCounter.xor(address));
                boolean flag = true;
                for(int k = 0; k < 32; k++){
                    //if there are any true bits, then the addresses do not match.
                    if(result.getBit(k).getValue()){
                        flag = false;
                    }
                }
                if(flag){
                    //if it does match, write the new value into the cacheList.
                    cacheList[i][j].copy(value);
                    //and write the new value into MainMemory.
                    MainMemory.write(address, value);
                }
            }
        }
    }
    public static int getClockCycleCost(){
        return clockCycleCost;//return the clock cycle this cache has.
    }
    //This method is used for testing!.
    //empty the whole cache.
    public static void emptyCache(){
        cacheList = new Word[4][8];//initialize the list again.
        cacheAddresses = new Word[4];//initialize the list again.
        clockCycleCost = 0;//set the value to 0.
        nextAddress = 0;//set the value to 0.
    }
}