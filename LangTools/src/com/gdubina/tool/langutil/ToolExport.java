package com.gdubina.tool.langutil;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dom4j.Comment;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

import static com.gdubina.tool.langutil.Tool.*;

public class ToolExport {

    public static final String ATTR_TRANSLATABLE = "translatable";

    private File outExcelFile;
    private String project;
    private Map<String, Integer> keys;
    private PrintStream out;

    public ToolExport(PrintStream out) throws ParserConfigurationException {

        this.out = out == null ? System.out : out;
    }

    public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException,
            DocumentException {
        if (args == null || args.length == 0) {
            System.out.println("Project dir is missed");
            return;
        }
        run(null, args[0], args.length > 1 ? args[1] : null);
    }

    public static void run(String projectDir, String outputFile) throws SAXException, IOException,
            ParserConfigurationException, DocumentException {
        run(null, projectDir, outputFile);
    }

    public static void run(PrintStream out, String projectDir, String outputFile) throws SAXException, IOException,
            ParserConfigurationException, DocumentException {
        ToolExport tool = new ToolExport(out);
        if (projectDir == null || "".equals(projectDir)) {
            tool.out.println("Project dir is missed");
            return;
        }
        File project = new File(projectDir);
        tool.outExcelFile = new File(outputFile != null ? outputFile : "exported_strings_" + System.currentTimeMillis()
                + ".xls");
        tool.project = project.getName();
        tool.export(project);
    }

    private void export(File project) throws SAXException, IOException, DocumentException {
        File res = new File(project, "res");
        Map<String, File> files = new TreeMap<String, File>();

        for (File dir : res.listFiles()) {
            if (!dir.isDirectory() || !dir.getName().startsWith(DIR_VALUES)) {
                continue;
            }
            String dirName = dir.getName();
            files.put(dirName, dir);
        }

        if (!files.isEmpty()) {
            Set<String> paths = files.keySet();
            for (String dirName : paths) {
                if (dirName.equals(DIR_VALUES)) {
                    keys = exportDefLang(files.get(dirName));
                } else {
                    int index = dirName.indexOf('-');
                    if (index == -1) {
                        continue;
                    }
                    String lang = dirName.substring(index + 1);
                    exportLang(lang, files.get(dirName));
                }
            }
        }
    }

    private void exportLang(String lang, File valueDir) throws FileNotFoundException, IOException, SAXException,
            DocumentException {
        File stringFile = new File(valueDir, "strings.xml");
        if (!stringFile.exists()) {
            return;
        }
        exportLangToExcel(project, lang, getElements(stringFile), outExcelFile, keys);
    }

    private Map<String, Integer> exportDefLang(File valueDir) throws FileNotFoundException, IOException, SAXException,
            DocumentException {
        File stringFile = new File(valueDir, "strings.xml");
        if (!stringFile.exists()) {
            return null;
        }
        return exportDefLangToExcel(project, getElements(stringFile), outExcelFile);
    }

    @SuppressWarnings("unchecked")
    private Iterator<Element> getElements(File f) throws SAXException, IOException, DocumentException {
        SAXReader reader = new SAXReader();
        reader.setIgnoreComments(false);
        org.dom4j.Document doc = reader.read(f);
        Element root = doc.getRootElement();

        return root.nodeIterator();
    }

    private static HSSFCellStyle createTitleStyle(HSSFWorkbook wb) {
        HSSFFont bold = wb.createFont();
        bold.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        HSSFCellStyle style = wb.createCellStyle();
        style.setFont(bold);
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setWrapText(true);

        return style;
    }

    private static HSSFCellStyle createCommentStyle(HSSFWorkbook wb) {

        HSSFFont commentFont = wb.createFont();
        commentFont.setColor(HSSFColor.GREEN.index);
        commentFont.setItalic(true);
        commentFont.setFontHeightInPoints((short) 12);

        HSSFCellStyle commentStyle = wb.createCellStyle();
        commentStyle.setFont(commentFont);

        commentStyle.setFillForegroundColor(HSSFColor.LEMON_CHIFFON.index);
        commentStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        return commentStyle;
    }

    private static HSSFCellStyle createKeyStyle(HSSFWorkbook wb) {
        HSSFFont bold = wb.createFont();
        bold.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        bold.setFontHeightInPoints((short) 11);

        HSSFCellStyle keyStyle = wb.createCellStyle();
        keyStyle.setFont(bold);

        return keyStyle;
    }

    private static HSSFCellStyle createTextStyle(HSSFWorkbook wb) {
        HSSFFont plain = wb.createFont();
        plain.setFontHeightInPoints((short) 12);

        HSSFCellStyle textStyle = wb.createCellStyle();
        textStyle.setFont(plain);

        return textStyle;
    }

    private static HSSFCellStyle createMissedStyle(HSSFWorkbook wb) {

        HSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(HSSFColor.RED.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        return style;
    }

    private static void createTilte(HSSFWorkbook wb, HSSFSheet sheet) {
        HSSFRow titleRow = sheet.getRow(0);

        HSSFCell cell = titleRow.createCell(0);
        cell.setCellStyle(createTitleStyle(wb));
        cell.setCellValue("KEY");

        sheet.setColumnWidth(cell.getColumnIndex(), (40 * 256));
    }

    private static int addLang2Title(HSSFWorkbook wb, HSSFSheet sheet, String lang) {
        HSSFRow titleRow = sheet.getRow(0);
        int columnNumber = titleRow.getLastCellNum();
        HSSFCell cell = titleRow.createCell(columnNumber);
        cell.setCellStyle(createTitleStyle(wb));
        cell.setCellValue(lang);

        sheet.setColumnWidth(cell.getColumnIndex(), (60 * 256));

        return columnNumber;
    }

    private Map<String, Integer> exportDefLangToExcel(String project, Iterator<?> iterator, File f)
            throws IOException {
        out.println();
        out.println("Start processing DEFAULT language");

        Map<String, Integer> keys = new HashMap<String, Integer>();

        HSSFWorkbook wb = new HSSFWorkbook();

        HSSFCellStyle commentStyle = createCommentStyle(wb);
        HSSFCellStyle keyStyle = createKeyStyle(wb);
        HSSFCellStyle textStyle = createTextStyle(wb);

        HSSFSheet sheet;
        sheet = wb.createSheet(project);

        int rowIndex = 0;
        sheet.createRow(rowIndex++);
        createTilte(wb, sheet);
        addLang2Title(wb, sheet, "default");

        for (Iterator<?> i = iterator; i.hasNext(); ) {

            Node node = (Node) i.next();

            short type = node.getNodeType();

            if (type == Node.COMMENT_NODE) {
                HSSFRow row = sheet.createRow(rowIndex++);
                HSSFCell cell = row.createCell(0);
                cell.setCellValue(String.format("/** %s **/", ((Comment) node).getText()));
                cell.setCellStyle(commentStyle);

                sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, 255));
            } else if (type == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                if (STRING_ELEMENT.equals(element.getName())) {
                    if (!isTranslatable(element)) {
                        continue;
                    }

                    String name = element.attributeValue(ATTR_NAME);
                    String body = ContentXMLWriter.getContent(element);

                    if (keys.containsKey(name)) {
                        out.println("\t" + name + " - duplicate");
                    }

                    keys.put(name, rowIndex);

                    HSSFRow row = sheet.createRow(rowIndex++);

                    HSSFCell cell = row.createCell(0);
                    cell.setCellValue(name);
                    cell.setCellStyle(keyStyle);

                    cell = row.createCell(1);
                    cell.setCellStyle(textStyle);

                    cell.setCellValue(body);
                }
            }

        }
        sheet.createFreezePane(1, 1);

        FileOutputStream outFile = new FileOutputStream(f);
        wb.write(outFile);
        outFile.close();

        out.println("DEFAULT language was precessed");
        return keys;
    }

    private void exportLangToExcel(String project, String lang, Iterator<?> iterator, File f,
                                   Map<String, Integer> keysIndex)
            throws IOException {
        out.println();
        out.println(String.format("Start processing: '%s'", lang));

        Set<String> missedKeys = new HashSet<String>(keysIndex.keySet());

        HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(f));

        HSSFCellStyle textStyle = createTextStyle(wb);

        HSSFSheet sheet = wb.getSheet(project);
        final int column = addLang2Title(wb, sheet, lang);

        for (Iterator<?> i = iterator; i.hasNext(); ) {
            Node node = (Node) i.next();

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (STRING_ELEMENT.equals(element.getName())) {
                    if (!isTranslatable(element)) {
                        continue;
                    }

                    String name = element.attributeValue(ATTR_NAME);
                    String body = ContentXMLWriter.getContent(element);

                    Integer index = keysIndex.get(name);
                    if (index == null) {
                        out.println("\t" + name + " - row does not exist or duplicate");
                        continue;
                    }

                    missedKeys.remove(name);
                    HSSFRow row = sheet.getRow(index);

                    HSSFCell cell = row.createCell(column);
                    cell.setCellValue(body);
                    cell.setCellStyle(textStyle);

                }
            }

        }

        HSSFCellStyle missedStyle = createMissedStyle(wb);

        if (!missedKeys.isEmpty()) {
            out.println("  MISSED KEYS:");
        }
        for (String missedKey : missedKeys) {
            out.println("\t" + missedKey);
            Integer index = keysIndex.get(missedKey);
            HSSFRow row = sheet.getRow(index);
            HSSFCell cell = row.createCell((int) row.getLastCellNum());
            cell.setCellStyle(missedStyle);
        }

        FileOutputStream outStream = new FileOutputStream(f);
        wb.write(outStream);
        outStream.close();

        if (missedKeys.isEmpty()) {
            out.println(String.format("'%s' was processed", lang));
        } else {
            out.println(String.format("'%s' was processed with MISSED KEYS - %d", lang, missedKeys.size()));
        }
    }

    private boolean isTranslatable(Element e) {
        String translatable = e.attributeValue(ATTR_TRANSLATABLE);
        return translatable == null || "false".equals(translatable);
    }
}
