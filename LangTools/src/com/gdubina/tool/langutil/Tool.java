package com.gdubina.tool.langutil;

import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Tool {

    public static final String DIR_VALUES = "values";

    public static final String DIR_RES = "res";

    public static final String STRING_ELEMENT = "string";

    public static final String ATTR_NAME = "name";

    public static void main(String[] args) throws IOException, ParserConfigurationException,
            TransformerException, SAXException, DocumentException {
        if (args == null || args.length == 0) {
            printHelp();
            return;
        }

        if ("-i".equals(args[0])) {
            ToolImport.run(args[1]);
        } else if ("-e".equals(args[0])) {
            ToolExport.run(args[1], args.length > 2 ? args[2] : null);
        } else {
            printHelp();
        }
    }

    private static void printHelp() {
        System.out.println("commands format:");
        System.out.println("\texport: -e <project dir> <output file>");
        System.out.println("\timport: -i <input file>");
    }
}
