public class StringHandler {
    private String document;
    private int fingerIndex;

    public StringHandler(String doc){
        document = doc;
        fingerIndex = -1;//Starts at -1 so it counts the first character properly.
    }

    public char peek(int i){
        return document.charAt(fingerIndex + i);
    }

    public char getChar(){
        //Since its -1, no need to worry about the index starting at -1.
        fingerIndex++;
        return document.charAt(fingerIndex);
    }


    //used as an offset, to directly move the fingerIndex.
    public void swallow(int i){
        fingerIndex += i;
    }

    //check if there are more characters in the string.
    public boolean isDone(){
        return !(fingerIndex+1 < document.length());
    }
}
