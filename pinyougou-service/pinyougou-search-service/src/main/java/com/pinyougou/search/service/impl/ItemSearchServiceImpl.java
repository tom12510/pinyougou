package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.es.EsItem;
import com.pinyougou.search.dao.EsItemDao;
import com.pinyougou.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品搜索服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-24<p>
 */
@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private ElasticsearchTemplate esTemplate;
    @Autowired
    private EsItemDao esItemDao;

    /** 添加或修改商品的索引数据 */
    public void saveOrUpdate(List<EsItem> esItems){
        try{
            esItemDao.saveAll(esItems);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }


    /** 删除索引 */
    public void delete(List<Long> goodsIds){
        try{
            // 创建删除条件对象
            DeleteQuery deleteQuery = new DeleteQuery();
            deleteQuery.setIndex("pinyougou");
            deleteQuery.setType("item");
            deleteQuery.setQuery(QueryBuilders.termsQuery("goodsId", goodsIds));
            esTemplate.delete(deleteQuery);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Map<String, Object> search(Map<String, Object> params) {
        try{

            // 创建原生的搜索查询构建对象
            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
            // 添加搜索条件(默认查询全部)
            builder.withQuery(QueryBuilders.matchAllQuery());

            // 获取搜索关键字
            String keywords = (String)params.get("keywords");
            // 判断关键字是否为空
            if (StringUtils.isNoneBlank(keywords)){
                // 添加搜索条件(分词) 复制的Field不能用于高亮
                //builder.withQuery(QueryBuilders.matchQuery("keywords", keywords));
                /** ############### 1. 搜索关键字 ################# */
                // 添加多条件匹配查询
                builder.withQuery(QueryBuilders.multiMatchQuery(keywords,
                        "title", "category", "brand", "seller"));


                /** ############### 2. 搜索高亮 ################# */
                // 2.1 创建标题高亮字段
                HighlightBuilder.Field titleField = new HighlightBuilder.Field("title")
                        .preTags("<font color='red'>") // 高亮格式器前缀
                        .postTags("</font>") // 高亮格式器后缀
                        .fragmentSize(50); // 文本截断

                // 2.2 添加标题高亮字段(title)
                builder.withHighlightFields(titleField);
            }

            /** ############### 3. 搜索过滤 ################# */
            // 3.1 创建组合查询构建对象，用来封装多个过滤条件
            BoolQueryBuilder boolQuery = new BoolQueryBuilder();

            // 3.2 封装多个过滤条件
            // { "keywords": "小米", "category": "手机", "brand": "苹果",
            //    "spec": { "网络": "联通3G", "机身内存": "128G" }, "price": "1000-1500" }
            // 3.2.1 根据商品分类过滤
            String category = (String)params.get("category");
            if (StringUtils.isNoneBlank(category)){
                // 词条查询(不分词)
                boolQuery.must(QueryBuilders.termQuery("category", category));
            }
            // 3.2.2 根据商品品牌过滤
            String brand = (String)params.get("brand");
            if (StringUtils.isNoneBlank(brand)){
                // 词条查询(不分词)
                boolQuery.must(QueryBuilders.termQuery("brand", brand));
            }

            // 3.2.3 根据商品规格选项过滤(嵌套)
            // {"网络": "联通3G", "机身内存": "128G" }
            Map<String,String> specMap = (Map<String,String>)params.get("spec");
            if (specMap != null && specMap.size() > 0){
                for (String key : specMap.keySet()) {
                    // 嵌套field的名称
                    String field = "spec." + key + ".keyword" ;
                    // 添加过滤条件
                    boolQuery.must(QueryBuilders.nestedQuery("spec",
                            QueryBuilders.termQuery(field, specMap.get(key)), ScoreMode.None));
                }
            }

            // 3.2.4 根据商品价格区间过滤(范围)
            // "price": 0-500, 1000-1500, 3000-*
            String price = (String)params.get("price");
            if (StringUtils.isNoneBlank(price)){
                // 得到价格数组
                String[] priceArr = price.split("-");
                // 创建范围查询构建对象
                RangeQueryBuilder rangeQuery = new RangeQueryBuilder("price");
                if ("*".equals(priceArr[1])){
                    // 大于等于
                    rangeQuery.gte(priceArr[0]);
                }else {
                    // 起点  到  结束
                    rangeQuery.from(priceArr[0], true).to(priceArr[1], false);
                }
                // 添加BoolQuery中
                boolQuery.must(rangeQuery);
            }

            // 3.3 原生的搜索查询构建对象添加过滤条件
            builder.withFilter(boolQuery);


            // 创建搜索查询对象
            SearchQuery query = builder.build();

            /** ############### 5. 搜索分页 ################# */
            // 5.1 获取当前页码
            Integer curPage = (Integer) params.get("page");
            if (curPage == null){
                curPage = 1;
            }
            // 5.2 设置分页对象
            // 第一个参数：当前页码 - 1
            query.setPageable(PageRequest.of((curPage - 1), 10));

            /** ############### 6. 搜索排序 ################# */
            // 6.1 获取排序参数
            String sortField = (String)params.get("sortField");
            String sortValue = (String)params.get("sortValue");
            if (StringUtils.isNoneBlank(sortField)
                    && StringUtils.isNoneBlank(sortValue)) {
                // 6.2 创建排序对象
                Sort sort = new Sort("ASC".equals(sortValue)
                        ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
                // 6.3 添加排序
                query.addSort(sort);
            }

            /** 分页搜索，得到合计分页对象 */
            AggregatedPage<EsItem> page = esTemplate.queryForPage(query, EsItem.class,
                    new SearchResultMapper(){ // 搜索结果映射
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse sr,
                                                        Class<T> aClass, Pageable pageable) {
                    // 定义List集合封装搜索到得的文档
                    List<T> content = new ArrayList<>();
                    // 迭代搜索到得文档
                    for (SearchHit hit : sr.getHits()) {
                        // hit 封装了一篇文档
                        // 获取文档的json字符串 {}
                        String sourceAsString = hit.getSourceAsString();
                        // 把文档json字符串转化成EsItem对象
                        EsItem esItem = JSON.parseObject(sourceAsString, EsItem.class);

                        // 获取标题高亮字段
                        HighlightField titleHighlightField = hit.getHighlightFields().get("title");
                        if (titleHighlightField != null){
                            // 获取标题高亮内容
                            String title = titleHighlightField.getFragments()[0].toString();
                            System.out.println("title = " + title);
                            // 设置到EsItem对象
                            esItem.setTitle(title);
                        }

                        // 添加到集合中
                        content.add((T)esItem);
                    }
                    return new AggregatedPageImpl(content, pageable, sr.getHits().getTotalHits());
                }
            });


            /** ############### 4. 搜索分组 ################# */
            // 商品分类分组搜索
            List<String> categoryList = categoryGroup(keywords);

            // 定义Map集合封装响应数据 {rows : [], total : 1000}
            Map<String, Object> data = new HashMap<>();
            // 分页数据
            data.put("rows", page.getContent());
            // 总记录数
            data.put("total", page.getTotalElements());
            // 商品分类
            data.put("categoryList", categoryList);
            // 总页数
            data.put("totalPages", page.getTotalPages());

            return data;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 商品分类分组搜索 */
    private List<String> categoryGroup(String keywords){
        // 定义List集合封装分组的商品分类
        List<String> categoryList = new ArrayList<>();
        // 判断搜索关键是否为空
        if (StringUtils.isNoneBlank(keywords)){
            // 1. 创建原生的搜索查询构建对象
            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
            // 2. 添加多条件匹配查询
            builder.withQuery(QueryBuilders.multiMatchQuery(keywords,
                    "category","brand","title","seller"));

            // 3. 创建词条分组构建对象
            // categoryGroup: 分组名称，用于存储分组后的数据
            // category: 根据商品分类分组(分组的Field)
            TermsAggregationBuilder aggregationBuilder = AggregationBuilders
                    .terms("categoryGroup").field("category");
            // 4. 添加分组构建对象
            builder.addAggregation(aggregationBuilder);

            // 5. 搜索，得到多分组封装对象
            Aggregations aggregations = esTemplate.query(builder.build(),
                    new ResultsExtractor<Aggregations>() {
                        @Override
                        public Aggregations extract(SearchResponse sr) {
                            return sr.getAggregations();
                        }
                    });

            // 6. 获取指定的分组结果
            StringTerms terms = aggregations.get("categoryGroup");
            // 6.1 迭代分组结果集合
            for (StringTerms.Bucket bucket : terms.getBuckets()) {
                categoryList.add(bucket.getKeyAsString());
            }
        }
        return categoryList;
    }
}