package com.pinyougou.service;

import com.pinyougou.es.EsItem;

import java.util.List;
import java.util.Map; /**
 * ItemSearchService接口
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-24<p>
 */
public interface ItemSearchService {

    /** 搜索方法 */
    Map<String,Object> search(Map<String, Object> params);

    /** 添加或修改商品的索引数据 */
    void saveOrUpdate(List<EsItem> esItems);

    /** 删除索引 */
    void delete(List<Long> goodsIds);
}
