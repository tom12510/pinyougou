package com.pinyougou.es.dao;

import com.pinyougou.es.EsItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * EsItemDao接口
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-24<p>
 */
public interface EsItemDao extends ElasticsearchRepository<EsItem, Long> {


}
