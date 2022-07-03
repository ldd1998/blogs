package com.ldd.service;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author ldd
 * @createTime 2022年07月02日 22:48:00
 * @Description TODO
 */
public class Service {
    public static ByteArrayOutputStream export() {
        /**
         *创建HSSFWorkbook对象(excel的文档对象)，本实例是导出扩张名为xls（office2003）。
         * 如果需要导出扩展名xlsx（office2007以后版本），只需要把文中HSSF改为XSSF即可，
         * 如      HSSFWorkbook改为XSSFWorkbook。
         */
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet userTable = wb.createSheet("用户表");

        /**
         *  HSSFRow 代表行   0 = 表的第一行
         *  HSSFCell 代表列  0 = 第一行第一列
         *  注意 ： 单元行单元列下标从零开始
         */
        XSSFRow row0 = userTable.createRow(0);
        XSSFCell cell0 = row0.createCell(0);
        // 设置内容
        cell0.setCellValue("用户表格一览");
        // 合并单元格 起始行 截至列 起始列 截至行
        userTable.addMergedRegion(new CellRangeAddress(0,0,0,4));

        //  循环内容
        for (int i = 0; i < 1000000; i++) {
            System.out.println(i);
            XSSFRow row = userTable.createRow(i + 2);
            for(int k = 0; k < 5; k++){
                row.createCell(k).setCellValue("姓名姓名姓名姓名姓名姓名姓名姓名姓名姓名姓名姓名姓名姓名姓名姓名姓名姓名姓名");
            }
        }

        try {
            // 文件名称
            String fileName = System.currentTimeMillis() + ".xlsx";
            ByteArrayOutputStream fileOutputStream = new ByteArrayOutputStream();
            wb.write(fileOutputStream);
            fileOutputStream.close();
            return fileOutputStream;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
