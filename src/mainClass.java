import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;

public class mainClass {
    public static void main(String[] args) throws Exception {
        //if there is two arguments, treat them as files.
        if(args.length == 2){
            //first argument is the input file.
            Path documentPath = Paths.get(args[0]);
            //read the content from file.
            String documentContent = new String(Files.readAllBytes(documentPath));
            //lexer -> parser -> binaryString.
            Lexer lexer = new Lexer(documentContent);
            LinkedList<Token> lex = lexer.Lex();
            Parser parser = new Parser(lex);
            ArrayList<Node> nodes = parser.Parse();
            String binaryDocument = parser.getBinaryStatements(nodes);
            //write the binaryDocument into output and close.
            File output = new File(args[1]);
            output.setWritable(true);
            PrintWriter out = new PrintWriter(output);
            out.println(binaryDocument);
            out.close();

            Path outputPath = Paths.get(args[1]);
            String outputContent = new String(Files.readAllBytes(outputPath));
            MainMemory.load(new String[]{outputContent});
            Processor processor = new Processor();
            processor.run();

        }else{
            System.out.println("Invalid Parameters");
        }
    }
}
