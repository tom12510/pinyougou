package com.pinyougou.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinyougou.es.dao.EsItemDao;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 把SKU商品数据导入到ES索引库
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-24<p>
 */
@Component
public class ItemImport {

    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private EsItemDao esItemDao;

    /** 数据导入 */
    public void importData() throws Exception{
        // 1. 从tb_item查询数据
        Item item = new Item();
        item.setStatus("1");
        List<Item> itemList = itemMapper.select(item);

        System.out.println("=====华丽分割线=====");
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
            Map<String, String> specMap = new ObjectMapper().readValue(spec, Map.class);
            esItem.setSpec(specMap);

            esItems.add(esItem);
        }
        // 保存到索引库
        esItemDao.saveAll(esItems);
        System.out.println("=====华丽分割线=====");
    }

    public static void main(String[] args) throws Exception{
        // 创建Spring容器
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        ItemImport itemImport = ac.getBean(ItemImport.class);
        itemImport.importData();
    }
}
