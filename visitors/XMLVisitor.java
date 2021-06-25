package visitors;

import lexer.Real;
import lexer.WholeNumber;
import lexer.Word;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import parser.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class XMLVisitor implements ASTVisitor {
    public Document xmlDocument;
    private Element currentElement;

    public XMLVisitor(ASTProgram abstractSyntaxTree) throws ParserConfigurationException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        xmlDocument = documentBuilder.newDocument();
        visit(abstractSyntaxTree);
    }

    @Override
    public void visit(ASTProgram astProgram) {
        currentElement = xmlDocument.createElement("Program");
        xmlDocument.appendChild(currentElement);

        for (ASTStatement statement : astProgram.statements) {
            visit(statement);
        }
    }

    @Override
    public void visit(ASTStatement statement) {
        if (statement instanceof ASTAssignment) {
            visit ((ASTAssignment) statement);
        } else if (statement instanceof ASTBlock) {
            visit ((ASTBlock) statement);
        } else if (statement instanceof ASTFor) {
            visit ((ASTFor) statement);
        } else if (statement instanceof ASTFunctionDeclaration) {
            visit ((ASTFunctionDeclaration) statement);
        } else if (statement instanceof ASTIf) {
            visit ((ASTIf) statement);
        } else if (statement instanceof ASTPrint) {
            visit ((ASTPrint) statement);
        } else if (statement instanceof ASTReturn) {
            visit ((ASTReturn) statement);
        } else if (statement instanceof ASTVariableDeclaration) {
            visit ((ASTVariableDeclaration) statement);
        } else if (statement instanceof ASTWhile) {
            visit ((ASTWhile) statement);
        } else {
            System.err.println("Unknown node while visiting statement");
        }
    }

    @Override
    public void visit(ASTAssignment astAssignment) {
        Element parentElement = currentElement;
        currentElement = xmlDocument.createElement("Assignment");
        parentElement.appendChild(currentElement);
        currentElement.setAttribute("identifier", astAssignment.identifier.identifier);

        visit(astAssignment.expression);

        currentElement = parentElement;
    }

    @Override
    public void visit(ASTBlock astBlock) {
        Element parentElement = currentElement;
        currentElement = xmlDocument.createElement("Block");
        parentElement.appendChild(currentElement);

        for (ASTStatement statement : astBlock.statements) {
            visit (statement);
        }

        currentElement = parentElement;
    }

    @Override
    public void visit(ASTFor astFor) {
        Element parentElement = currentElement;
        currentElement = xmlDocument.createElement("For");
        parentElement.appendChild(currentElement);

        if (astFor.variableDeclaration != null) {
            visit(astFor.variableDeclaration);
        }

        visit(astFor.conditionExpression);

        if (astFor.assignment != null) {
            visit(astFor.assignment);
        }

        visit(astFor.loopedBlock);
        currentElement = parentElement;
    }

    @Override
    public void visit(ASTPrint astPrint) {
        Element parentElement = currentElement;
        currentElement = xmlDocument.createElement("Print");
        parentElement.appendChild(currentElement);

        visit(astPrint.expression);

        currentElement = parentElement;
    }

    @Override
    public void visit(ASTReturn astReturn) {
        Element parentElement = currentElement;
        currentElement = xmlDocument.createElement("Return");
        parentElement.appendChild(currentElement);

        visit(astReturn.expression);

        currentElement = parentElement;
    }

    @Override
    public void visit(ASTVariableDeclaration astVariableDeclaration) {
        Element parentElement = currentElement;
        currentElement = xmlDocument.createElement("VariableDeclaration");
        parentElement.appendChild(currentElement);
        currentElement.setAttribute("identifier", astVariableDeclaration.identifier.identifier);
        currentElement.setAttribute("type", astVariableDeclaration.type.lexeme);

        if (astVariableDeclaration.expression != null) {
            visit(astVariableDeclaration.expression);
        }

        currentElement = parentElement;
    }

    @Override
    public void visit(ASTFunctionDeclaration astFunctionDeclaration) {
        Element parentElement = currentElement;
        currentElement = xmlDocument.createElement("FunctionDeclaration");
        parentElement.appendChild(currentElement);
        currentElement.setAttribute("returnType", astFunctionDeclaration.returnType.lexeme);
        currentElement.setAttribute("identifier", astFunctionDeclaration.functionName.identifier);

        int i = 0;

        for (ASTParameter parameter : astFunctionDeclaration.parameterList) {
            currentElement.setAttribute("parameter" + i,
                    parameter.identifier.identifier + " (" + parameter.type.lexeme + ")");
            i++;
        }

        visit(astFunctionDeclaration.functionBlock);

        currentElement = parentElement;
    }

    @Override
    public void visit(ASTIf astIf) {
        Element parentElement = currentElement;
        currentElement = xmlDocument.createElement("If");
        parentElement.appendChild(currentElement);

        visit(astIf.conditionExpression);

        Element ifElement = currentElement;

        currentElement = xmlDocument.createElement("True");
        ifElement.appendChild(currentElement);
        visit(astIf.trueBlock);

        if (astIf.falseBlock != null) {
            currentElement = xmlDocument.createElement("False");
            ifElement.appendChild(currentElement);
            visit(astIf.falseBlock);
        }

        currentElement = parentElement;
    }

    @Override
    public void visit(ASTWhile astWhile) {
        Element parentElement = currentElement;
        currentElement = xmlDocument.createElement("While");
        parentElement.appendChild(currentElement);

        visit(astWhile.conditionExpression);
        visit(astWhile.loopedBlock);

        currentElement = parentElement;
    }

    @Override
    public void visit(ASTExpression astExpression) {
        if (astExpression instanceof ASTBinaryOperator) {
            visit ((ASTBinaryOperator) astExpression);
        } else if (astExpression instanceof ASTIdentifier) {
            visit ((ASTIdentifier) astExpression);
        } else if (astExpression instanceof ASTLiteral) {
            visit ((ASTLiteral) astExpression);
        } else if (astExpression instanceof ASTFunctionCall) {
            visit ((ASTFunctionCall) astExpression);
        } else if (astExpression instanceof ASTUnary) {
            visit ((ASTUnary) astExpression);
        } else if (astExpression instanceof ASTArrayLiteral) {
            visit ((ASTArrayLiteral) astExpression);
        } else {
            System.err.println("Unknown node while visiting expression");
        }
    }

    @Override
    public void visit(ASTBinaryOperator operator) {
        Element parentElement = currentElement;
        currentElement = xmlDocument.createElement("BinaryOperator");
        currentElement.setAttribute("operator", operator.operator.tokenType.name());
        parentElement.appendChild(currentElement);

        visit(operator.expression1);
        visit(operator.expression2);

        currentElement = parentElement;
    }

    @Override
    public void visit(ASTFunctionCall astFunctionCall) {
        Element parentElement = currentElement;
        currentElement = xmlDocument.createElement("FunctionCall");
        currentElement.setAttribute("identifier", astFunctionCall.identifier.identifier);
        parentElement.appendChild(currentElement);

        if (astFunctionCall.parameters != null) {
            for (ASTExpression expression : astFunctionCall.parameters) {
                visit(expression);
            }
        }

        currentElement = parentElement;
    }

    @Override
    public void visit(ASTIdentifier astIdentifier) {
        Element parentElement = currentElement;
        currentElement = xmlDocument.createElement("Identifier");
        currentElement.setAttribute("name", astIdentifier.identifier);
        parentElement.appendChild(currentElement);

        currentElement = parentElement;
    }

    @Override
    public void visit(ASTLiteral astLiteral) {
        Element parentElement = currentElement;
        currentElement = xmlDocument.createElement("Literal");
        parentElement.appendChild(currentElement);

        String value = "";

        switch(astLiteral.token.tokenType) {
            case REAL:
                value = String.valueOf(((Real) astLiteral.token).value);
                break;
            case NUM:
                value = String.valueOf(((WholeNumber) astLiteral.token).value);
                break;
            case CHAR:
            case STRING:
                value = ((Word) astLiteral.token).lexeme;
                break;
            case TRUE:
                value = "true";
                break;
            case FALSE:
                value = "false";
                break;
            default:
                System.err.println("Literal with no type");
        }

        currentElement.setAttribute("type", astLiteral.type);
        currentElement.setAttribute("value", value);
        currentElement = parentElement;
    }

    @Override
    public void visit(ASTArrayLiteral astArrayLiteral) {
        Element parentElement = currentElement;
        currentElement = xmlDocument.createElement("ArrayLiteral");
        parentElement.appendChild(currentElement);

        for (ASTExpression expression : astArrayLiteral.arrayMembers) {
            visit(expression);
        }

        currentElement = parentElement;
    }

    @Override
    public void visit(ASTUnary astUnary) {
        Element parentElement = currentElement;
        currentElement = xmlDocument.createElement("Unary");
        currentElement.setAttribute("unarySymbol", astUnary.unaryType.name());
        parentElement.appendChild(currentElement);

        visit(astUnary.expression);

        currentElement = parentElement;
    }

    //https://stackoverflow.com/questions/2325388/what-is-the-shortest-way-to-pretty-print-a-org-w3c-dom-document-to-stdout
    public void printDocument(OutputStream out) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(xmlDocument),
                new StreamResult(new OutputStreamWriter(out, StandardCharsets.UTF_8)));
    }
}
