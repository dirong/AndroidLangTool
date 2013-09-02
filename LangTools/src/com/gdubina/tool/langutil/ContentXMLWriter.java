package com.gdubina.tool.langutil;

import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

class ContentXMLWriter extends XMLWriter {

    public ContentXMLWriter(Writer out) {
        super(out, new OutputFormat());
    }

    public static String getContent(Element e) {
        Writer out = new StringWriter();
        ContentXMLWriter writer = new ContentXMLWriter(out);
        try {
            writer.writeElementContent(e);
            writer.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return out.toString();
    }
}