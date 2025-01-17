package com.jmal.clouddisk.lucene;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class ReadWord {
    public static void main(String[] args) {
        try {
            FileInputStream fis = new FileInputStream("/Users/jmal/Downloads/未命名文件.docx");
            XWPFDocument document = new XWPFDocument(fis);
            List<XWPFParagraph> paragraphs = document.getParagraphs();

            for (XWPFParagraph para : paragraphs) {
                System.out.println(para.getText());
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
