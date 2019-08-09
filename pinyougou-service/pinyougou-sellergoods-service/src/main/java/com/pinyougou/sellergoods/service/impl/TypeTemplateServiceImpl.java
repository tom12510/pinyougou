package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.mapper.TypeTemplateMapper;
import com.pinyougou.pojo.SpecificationOption;
import com.pinyougou.pojo.TypeTemplate;
import com.pinyougou.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 类型模板控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-14<p>
 */
@Service
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TypeTemplateMapper typeTemplateMapper;
    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public void save(TypeTemplate typeTemplate) {
        try{
            typeTemplateMapper.insertSelective(typeTemplate);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(TypeTemplate typeTemplate) {
        try{
            typeTemplateMapper.updateByPrimaryKeySelective(typeTemplate);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {
        try{
            // 创建示范对象
            Example example = new Example(TypeTemplate.class);
            // 创建条件对象
            Example.Criteria criteria = example.createCriteria();
            // in条件
            criteria.andIn("id", Arrays.asList(ids));

            // 条件删除
            typeTemplateMapper.deleteByExample(example);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public TypeTemplate findOne(Serializable id) {
        return typeTemplateMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<TypeTemplate> findAll() {
        return null;
    }

    @Override
    public PageResult findByPage(TypeTemplate typeTemplate, int page, int rows) {
        try{
           // 开启分页
           PageInfo<Object> pageInfo = PageHelper.startPage(page, rows)
                   .doSelectPageInfo(new ISelect() {
               @Override
               public void doSelect() {
                   typeTemplateMapper.findAll(typeTemplate);
               }
           });
           return new PageResult(pageInfo.getPages(), pageInfo.getList());
        }catch (Exception ex){
           throw new RuntimeException(ex);
        }
    }

    /** 查询类型模板 */
    public List<Map<String,Object>> findTypeTemplateList(){
        try{
            return typeTemplateMapper.findTypeTemplateList();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 根据主键id查询规格选项数据 */
    public List<Map> findSpecOptionByTemplateId(Long id){
        try{
            /**
             * [{"id":27,"text":"网络", "options" : [{},{}]},
                {"id":32,"text":"机身内存", "options" : [{},{}]}]
             */
            // 获取spec_ids列数据:
            // [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
            String specIds = findOne(id).getSpecIds();
            // 把specIds字符串转化成List<Map> json处理框架
            // JSON.parseArray(); [{},{}]
            // JSON.parseObject(); {}
            List<Map> specList = JSON.parseArray(specIds, Map.class);

            for (Map map : specList) {

                // 获取id
                Long specId = Long.valueOf(map.get("id").toString());
                // SELECT * FROM `tb_specification_option` WHERE spec_id = 27
                SpecificationOption so = new SpecificationOption();
                so.setSpecId(specId);
                List<SpecificationOption> options = specificationOptionMapper.select(so);
                map.put("options", options);
            }

            return specList;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
