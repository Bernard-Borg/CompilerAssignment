import lexer.Lexer;
import parser.ASTProgram;
import parser.Parser;
import semantics.SemanticVisitor;
import visitors.InterpretationVisitor;
import visitors.XMLVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

public class Main {
    //Counter for the programs (allows multiple programs to be compiled at once)
    private static int counter = 0;

    public static void main (String[] args) {
        try {
            /*if (args.length <= 0) {
                throw new Exception("You need to pass a file path to a file in the same directory as the jar file");
            }

            String sourcePath = getPathToCurrentDirectory() + args[0];
            File inputProgram = new File(sourcePath);

            if (!inputProgram.exists()) {
                throw new Exception ("You need to pass a file path to a file in the same directory as the jar file");
            }*/

            compile("./testfile3.tlang");
        } catch (FileNotFoundException e) {
            System.out.println("No file found");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void compile(String sourcePath) throws Exception {
        counter++;

        Lexer lexer = new Lexer(sourcePath);

        Parser parser = new Parser(lexer);

        ASTProgram abstractSyntaxTree = parser.parseProgram();

        XMLVisitor xmlVisitor = new XMLVisitor(abstractSyntaxTree);

        LocalDateTime now = LocalDateTime.now();
        String fileName = String.format("/xml/output%02d-%02d-%02d %02d-%02d-%02d-%d.xml",
                now.getYear(), now.getMonth().getValue(), now.getDayOfMonth(),
                now.getHour(), now.getMinute(), now.getSecond(), counter);

        File file = new File(getPathToCurrentDirectory() + fileName);

        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new Exception("Folder for XML files could not be created");
            }
        }

        if (!file.createNewFile()) {
            throw new Exception("XML file could not be created");
        }

        xmlVisitor.printDocument(new FileOutputStream(file));

        SemanticVisitor semanticVisitor = new SemanticVisitor(abstractSyntaxTree);
        semanticVisitor.doSemanticAnalysis();

        InterpretationVisitor interpretationVisitor = new InterpretationVisitor(abstractSyntaxTree);
        interpretationVisitor.interpret();
    }

    public static String getPathToCurrentDirectory() throws URISyntaxException {
        return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .getParentFile()
                .getPath();
    }
}