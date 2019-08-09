package com.pinyougou.search.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.es.EsItem;
import com.pinyougou.pojo.Item;
import com.pinyougou.service.GoodsService;
import com.pinyougou.service.ItemSearchService;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 商品消息监听器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-29<p>
 */
public class ItemMessageListener implements MessageListenerConcurrently{

    @Reference(timeout = 10000)
    private GoodsService goodsService;
    @Reference(timeout = 10000)
    private ItemSearchService itemSearchService;

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
            if ("UPDATE".equals(tags)){ // 同步商品的索引
                // 查询上架通过的SKU商品
                List<Item> itemList = goodsService.findItemByGoodsId(goodsIds);
                // 定义ES对应的商品集合
                List<EsItem> esItems = new ArrayList<>();
                for (Item item1 : itemList) {
                    EsItem esItem = new EsItem();
                    esItem.setId(item1.getId());
                    esItem.setTitle(item1.getTitle());
                    esItem.setPrice(item1.getPrice().doubleValue());
                    esItem.setImage(item1.getImage());
                    esItem.setGoodsId(item1.getGoodsId());
                    esItem.setCategory(item1.getCategory());
                    esItem.setBrand(item1.getBrand());
                    esItem.setSeller(item1.getSeller());
                    esItem.setUpdateTime(item1.getUpdateTime());
                    // {"网络":"联通4G","机身内存":"64G"}
                    String spec = item1.getSpec();
                    Map<String, String> specMap = JSON.parseObject(spec, Map.class);
                    esItem.setSpec(specMap);
                    esItems.add(esItem);
                }
                // 同步到索引库
                itemSearchService.saveOrUpdate(esItems);
            }

            if ("DELETE".equals(tags)){
                // 删除索引
                itemSearchService.delete(goodsIds);
            }

        }catch (Exception ex){
            // 消费失败
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        // 消费成功
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
