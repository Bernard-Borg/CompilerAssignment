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
    public static void main (String[] args) {
        Parser parser;
        Lexer lexer;

        try {
            /*if (args.length <= 0) {
                throw new Exception("You need to pass a file path to a file in the same directory as the jar file");
            }

            String sourcePath = getPathToCurrentDirectory() + args[0];
            File inputProgram = new File(sourcePath);

            if (!inputProgram.exists()) {
                throw new Exception ("You need to pass a file path to a file in the same directory as the jar file");
            }*/

            String sourcePath = "./testfile2.tlang";
            lexer = new Lexer(sourcePath);

            parser = new Parser(lexer);

            ASTProgram abstractSyntaxTree = parser.parseProgram();
            System.out.println("Parsed successfully");

            XMLVisitor xmlVisitor = new XMLVisitor(abstractSyntaxTree);

            LocalDateTime now = LocalDateTime.now();
            String fileName = String.format("/xml/output%02d-%02d-%02d %02d-%02d-%02d.xml",
                    now.getYear(), now.getMonth().getValue(), now.getDayOfMonth(),
                    now.getHour(), now.getMinute(), now.getSecond());

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
        } catch (FileNotFoundException e) {
            System.out.println("No file found");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }

    public static String getPathToCurrentDirectory() throws URISyntaxException {
        return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .getParentFile()
                .getPath();
    }
}