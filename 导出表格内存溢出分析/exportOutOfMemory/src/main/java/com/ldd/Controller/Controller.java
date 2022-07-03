package com.ldd.Controller;

import com.ldd.service.Service;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ldd
 * @createTime 2022年07月02日 22:38:00
 * @Description TODO
 */
@RestController
public class Controller {
    @GetMapping("/exportExcel")
    public void exportExcel(HttpServletResponse res) throws IOException {
        res.setCharacterEncoding("UTF-8");
        // attachment是以附件的形式下载，inline是浏览器打开
        res.setHeader("Content-Disposition", "inline;filename=1.xlsx");
        res.setContentType("text/plain;UTF-8");
        // 把二进制流放入到响应体中
        ServletOutputStream os = res.getOutputStream();
        os.write(Service.export().toByteArray());
        os.flush();
        os.close();
        return ;
    }
    @GetMapping("/heap")
    public void heap(){
        List l = new ArrayList();
        while (true){
            l.add(new Object());
        }
    }

}
