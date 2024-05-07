
public class InstructionCache {
    private static Word[] cacheList = new Word[8];
    private static Word startAddress;
    private static int clockCycleCost;
    public static Word read(Word address) throws Exception {
        clockCycleCost = 0;
        if(startAddress == null){
            //if the start address is null, this is startup, we immediately
            //consider this a cache hit.
            clockCycleCost += 20;//accessing L2Cache costs 20.
            Word[] newMem = L2Cache.fillCache(address);
            //newMem[0-7] is the next 8 addresses for the list.
            //newMem[8] is the starting address.
            for(int i = 0; i < 8; i++){
                //initialize each word in cacheList
                cacheList[i] = new Word();
                //copy the address from the array into cacheList.
                cacheList[i].copy(newMem[i]);
            }
            //initialise the startAddress
            startAddress = new Word();
            //copy the starting address into startAddress
            startAddress.copy(newMem[8]);
            //add the correct amount of clock cost
            //50 to fill up InstructionCache.
            //A value from L2Cache operation.
            clockCycleCost += (50 + L2Cache.getClockCycleCost());
            return cacheList[0];//the address is stored at the beginning of the cacheList.
        }

        Word addressCounter = new Word();//used for incrementing.
        addressCounter.copy(startAddress);//copy the startAddress
        for(int i = 0; i < 8;i++){
            Word result = new Word();
            //xor between the addressCounter and address to see if we have a match.
            result.copy(addressCounter.xor(address));
            boolean flag = true;
            for(int j = 0; j < 32; j++){
                //if any of the bits are true, then they are not equal.
                if(result.getBit(j).getValue()){
                    flag = false;
                }
            }

            //if the result says the address and addressCounter are equal, then we have a cache hit.
            if(flag){
                //this only costs 10 cycles.
                clockCycleCost = 10;
                //send the correct address in cacheList.
                return cacheList[i];
            }
            //increment the addressCounter.
            addressCounter.increment();
        }

        //if this doesn't find the correct address in the cacheList, this is a cache miss
        //we need to re-populate.
        clockCycleCost = 20;//accessing L2Cache costs 20 cycles.
        Word[] newMem = L2Cache.fillCache(address);
        //newMem[0-7] is the next 8 addresses for the list.
        //newMem[8] is the starting address.
        for(int i = 0; i < 8; i++){
            cacheList[i].copy(newMem[i]);
        }
        startAddress.copy(newMem[8]);
        //filling up InstructionCache costs 50 and whatever L2Cache did.
        clockCycleCost += (50 + L2Cache.getClockCycleCost());

        //now we need to get the correct address to send back.
        addressCounter = new Word();//used for incrementing
        addressCounter.copy(startAddress);//copy the startAddress
        for(int i = 0; i < 8;i++){
            Word result = new Word();
            result.copy(addressCounter.xor(address));
            //xor between the addressCounter and address to see if we have a match.
            boolean flag = true;
            for(int j = 0; j < 32; j++){
                //if any of the bits are true, then they are not equal.
                if(result.getBit(j).getValue()){
                    flag = false;
                }
            }

            //if the result says the address and addressCounter are equal, then we have a cache hit.
            if(flag){
                //return the correct in cacheList
                return cacheList[i];
            }
            //increment the addressCounter.
            addressCounter.increment();
        }
        //if at any point, this method returns null, we have a huge problem.
        return null;
    }
    public static int getClockCycleCost(){
        return clockCycleCost;//return the clock cycles done in InstructionCache.
    }
    //This method is used for Testing!
    //To clear out the values.
    public static void emptyCache(){
        cacheList = new Word[8];//re-initialize the list of words.
        startAddress = null;//set the startAddress to null.
        clockCycleCost = 0;//clock cycle is set to 0.
        L2Cache.emptyCache();//L2Cache also needs to be emptied out.
    }
}

