public class Bit {
    private boolean value;
    //Constructor for assignment1.Bit
    //Create bit and sets to false.
    public Bit(){
        value = false;
    }
    //Create a bit and sets to v.
    public Bit(boolean v){
        value = v;
    }
    //sets the value of the bit.
    public void set(Boolean value){
        this.value = value;
    }
    //sets the value to the opposite value of the bit.
    public void toggle(){
        if(value){
            value = false;
        }else{
            value = true;
        }
    }
    //sets the value of the bit to true.
    public void set(){
        value = true;
    }
    //sets the value of the bit to false.
    public void clear(){
        value = false;
    }
    //return the value of the bit.
    public boolean getValue(){
        return value;
    }
    //and gate for two bits.
    public Bit and(Bit other){
        if(value){
            if(other.getValue()){
                //If the first bit is true and the second bit is true,
                //then return a new assignment1.Bit as true.
                return new Bit(true);
            }
        }
        //If the first bit is true but the second bit is false, vice-visa,
        //or both bits are false, then return a new assignment1.Bit as false.
        return new Bit(false);
    }
    //or gate for two bits
    public Bit or(Bit other){
        //If the first bit is true, return a new assignment1.Bit as true.
        if(value){
            return new Bit(true);
        }
        //If the second bit is true, return a new assignment1.Bit as true.
        if(other.getValue()){
            return new Bit(true);
        }
        //If both bits are false, return a new assignment1.Bit as false.
        return new Bit(false);
    }
    //xor gate for two bits.
    public Bit xor(Bit other){
        if(value){
            if(other.getValue()){
                //If the first bit is true and the second bit is true,
                //then return a new assignment1.Bit as false.
                return new Bit(false);
            }
            //If the first bit is true and the second bit is false,
            //return a new assignment1.Bit as false.
            return new Bit(true);
        }
        //If the first bit is false, and the second bit is true,
        //return a new assignment1.Bit as true.
        if(other.getValue()){
            return new Bit(true);
        }
        //If both bits are false, return a new assignment1.Bit as false.
        return new Bit(false);
    }
    //not gate for one bit.
    public Bit not(){
        //if true, return false
        if(value){
            return new Bit(false);
        }
        //if false, return true
        return new Bit(true);
    }
    //toString, return "t" for true, "f" for false.
    public String toString(){
        if(value) return "t";
        return "f";
    }
}
