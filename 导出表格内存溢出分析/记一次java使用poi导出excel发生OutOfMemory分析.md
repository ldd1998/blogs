# 记一次java使用poi导出excel发生OutOfMemory分析

## 前言

在工作中使用poi导出一份大概几十M的excel数据居然发生了内存溢出，当时就百思不得其解，这导出的数据量也不大呀，而且服务器内存有好多G肯定是够用的，那这里是哪里出了问题，后来在学到JVM内存模型的时候我就又想到了这个问题，我就想能不能在JVM内存模型中找到发生OutOfMemory的地方和原因

## 复现场景

那么如果要分析其产生的原因，就要对当时的场景进行复现，于是乎我就找到了一份poi导出excel的代码加以修改复现当时的场景，当我将行数设置为100w行时，并没有发生内存溢出，导出的文件也只有 10M，但是导出时间却花了好久，大概有几十秒吧，难道导出这些数据要这么慢的吗。

```java
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author ldd
 * @createTime 2022年07月02日 21:09:00
 * @Description 导出文件内存溢出测试
 */
public class ExportExcel {
    public static void main(String[] args) {
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


        //设置单元列名
        XSSFRow row1 = userTable.createRow(1);
        row1.createCell(0).setCellValue("姓名");
        row1.createCell(1).setCellValue("年龄");
        row1.createCell(2).setCellValue("住址");


        //  循环内容
        for (int i = 0; i < 1000000; i++) {
            XSSFRow row = userTable.createRow(i + 2);
            row.createCell(0).setCellValue("姓名");
            row.createCell(1).setCellValue("年龄");
            row.createCell(2).setCellValue("地址");
        }

        try {
            // 文件名称
            String fileName = System.currentTimeMillis() + ".xlsx";
            FileOutputStream fileOutputStream = new FileOutputStream("C:\\MyFile\\learn\\blogs\\导出表格内存溢出分析\\" + fileName);
            wb.write(fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```

于是我们使用jconsole工具查看内存使用了多少，居然达到了惊人的2.6G，不知道为什么会产生这样的情况

![image-20220702214220677](https://picgo-1256570725.cos.ap-shanghai.myqcloud.com/img/image-20220702214220677.png)

至此还是没有出现内存溢出的现象，按道理应该是出现了，于是我把导出条数改成了200W条，但是却报错了，由于excel只能导出1048576行，所以我打算增加每行数据量来达到内存溢出的现象

```java\
Exception in thread "main" java.lang.IllegalArgumentException: Invalid row number (1048576) outside allowable range (0..1048575)
	at org.apache.poi.xssf.usermodel.XSSFRow.setRowNum(XSSFRow.java:369)
	at org.apache.poi.xssf.usermodel.XSSFSheet.createRow(XSSFSheet.java:712)
	at ExportExcel.main(ExportExcel.java:47)
```

我把列数改为了10列，等了大概几分钟也没有等来内存溢出，却等来了这个超出了GC开销限制异常（后来查资料发现应该是内存溢出的一种类型），

经过百度大致了解：这个是JDK6新添的错误类型。是发生在GC占用大量时间为释放很小空间的时候发生的，是一种保护机制。一般是因为堆太小，导致异常的原因：没有足够的内存，Sun 官方对此的定义：超过98%的时间用来做GC并且回收了不到2%的堆内存时会抛出此异常

```java
Exception in thread "main" java.lang.OutOfMemoryError: GC overhead limit exceeded
```

经查看JVM内存发现

eden和old gen都已经满了

![image-20220702220619102](https://picgo-1256570725.cos.ap-shanghai.myqcloud.com/img/image-20220702220619102.png)

后来还发现当old gen满了之后，程序运行速率明显变慢，一开始一秒有1w行数据，后来只有1000条左右，应该和堆内存满了有关系。

后来我想会不会时springboot自带的tomcat发生的内存溢出

经过尝试，发现并不是那样子的，最终是没有复现内存溢出的场景，应该上面那个错误就是outOfMemory的一类。

因为现在虚拟机好像最大只有4g内存可以用，我还没有设置-Xmx参数，不知道为什么定为4g

当我设置-Xmx8000m之后，再去导出100w条数据，原本在80w的时候就会出现程序运行缓慢最后出现异常，现在已经可以正常走到999999，但是就是输出不出表格到前端进行下载，但是可以正常保存到本地文件，不知道又出现了什么问题。



