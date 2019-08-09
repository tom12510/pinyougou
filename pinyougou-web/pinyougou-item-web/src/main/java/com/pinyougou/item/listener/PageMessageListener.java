package com.pinyougou.item.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.service.GoodsService;
import freemarker.template.Template;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

/**
 * 静态页面监听器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-29<p>
 */
public class PageMessageListener implements MessageListenerConcurrently {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Reference(timeout = 10000)
    private GoodsService goodsService;
    @Value("${pageDir}")
    private String pageDir;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messageExts,
                                                    ConsumeConcurrentlyContext context) {
        try {
            // 获取消息对象
            MessageExt messageExt = messageExts.get(0);
            // 获取标签
            String tags = messageExt.getTags();
            // 获取消息内容
            String body = new String(messageExt.getBody(), "UTF-8");
            // 转化成List集合
            List<Long> goodsIds = JSON.parseArray(body, Long.class);
            System.out.println("goodsIds = " + goodsIds);
            // 判断操作
            if ("CREATE".equals(tags)){ // 生成商品的静态页面
                // 1. 获取模板对象
                Template template = freeMarkerConfigurer
                        .getConfiguration().getTemplate("/item.ftl");

                // 2. 循环生成静态页面
                for (Long goodsId : goodsIds) {
                    // 2.1 定义数据模型
                    Map<String, Object> dataModel = goodsService.getGoods(goodsId);

                    // 定义输出流对象
                    OutputStreamWriter writer = new OutputStreamWriter(new
                            FileOutputStream(pageDir + goodsId + ".html"), "UTF-8");
                    // 2.2 生成静态页面
                    template.process(dataModel, writer);
                    // 关闭
                    writer.close();
                }

            }
            if ("DELETE".equals(tags)){
                // 删除商品的静态页面
                for (Long goodsId : goodsIds) {
                    File file = new File(pageDir + goodsId + ".html");
                    if (file.exists() && file.isFile()){
                        file.delete();
                    }
                }
            }

        }catch (Exception ex){
            // 消费失败
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        // 消费成功
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
