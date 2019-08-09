package com.pinyougou.shop.controller;

import org.apache.commons.io.FilenameUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 上传控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-19<p>
 */
@RestController
public class UploadController {

    /** 文件服务器的访问地址 */
    @Value("${fileServerUrl}")
    private String fileServerUrl;

    /** 文件上传的方法 */
    @PostMapping("/upload")
    public Map<String,Object> upload(@RequestParam(value = "file")MultipartFile multipartFile){
        //  {status : 200, url : ''}
        Map<String,Object> data = new HashMap<>();
        data.put("status", 500);
        try {
            // 1. 获取上传文件的名称
            String filename = multipartFile.getOriginalFilename();
            // 2. 获取上传文件的字节数组
            byte[] bytes = multipartFile.getBytes();


            /** ############ 上传文件到FastDFS服务器 ############# */
            // 3. 获取fastdfs-client.conf
            String path = this.getClass().getResource("/fastdfs-client.conf").getPath();
            // 4. 初始化客户端全局对象
            ClientGlobal.init(path);
            // 5. 创建存储客户端对象
            StorageClient storageClient  = new StorageClient();
            // 6. 上传文件到服务器
            String[] arr = storageClient.upload_file(bytes, FilenameUtils.getExtension(filename), null);

            // 7. 拼接图片访问的地址
            // http://192.168.12.131 / group1 / xx/xx/xx.jpg
            StringBuilder url = new StringBuilder(fileServerUrl);
            for (String str : arr) {
                url.append("/" + str);
            }
            data.put("status", 200);
            data.put("url", url.toString());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return data;
    }

}
