package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.SpecificationMapper;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.pojo.Specification;
import com.pinyougou.pojo.SpecificationOption;
import com.pinyougou.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 规格服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-14<p>
 */
@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecificationMapper specificationMapper;
    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public void save(Specification specification) {
        try{
            // 1. 往tb_specification表插入数据
            specificationMapper.insertSelective(specification);
            System.out.println("id = " + specification.getId());

            // 2. 往tb_specification_option表插入数据
            specificationOptionMapper.save(specification);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(Specification specification) {
        try{

            // 1. 修改tb_specification中数据
            specificationMapper.updateByPrimaryKeySelective(specification);

            // 2. 修改tb_specification_option中数据
            // 2.1 删除
            // DELETE FROM `tb_specification_option` WHERE spec_id = ?
            SpecificationOption so = new SpecificationOption();
            so.setSpecId(specification.getId());
            specificationOptionMapper.delete(so);

            // 2.2 添加
            specificationOptionMapper.save(specification);
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
            // 1. 删除tb_specification_option中数据
            // DELETE FROM `tb_specification_option` WHERE spec_id in (?,?,?)
            Example example = new Example(SpecificationOption.class);
            // 创建条件对象
            Example.Criteria criteria = example.createCriteria();
            //  spec_id in (?,?,?)
            criteria.andIn("specId", Arrays.asList(ids));
            // 条件删除
            specificationOptionMapper.deleteByExample(example);


            // 2. 删除tb_specification中数据
            // DELETE FROM `tb_specification` WHERE id in (?,?,?)
            example = new Example(Specification.class);
            // 创建条件对象
            criteria = example.createCriteria();
            //  id in (?,?,?)
            criteria.andIn("id", Arrays.asList(ids));
            // 条件删除
            specificationMapper.deleteByExample(example);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Specification findOne(Serializable id) {
        return null;
    }

    @Override
    public List<Specification> findAll() {
        return null;
    }

    @Override
    public PageResult findByPage(Specification specification, int page, int rows) {
        try{
           // 开启分页
           PageInfo<Object> pageInfo = PageHelper.startPage(page, rows)
                   .doSelectPageInfo(new ISelect() {
               @Override
               public void doSelect() {
                    specificationMapper.findAll(specification);
               }
           });
           return new PageResult(pageInfo.getPages(), pageInfo.getList());
        }catch (Exception ex){
           throw new RuntimeException(ex);
        }
    }

    /** 根据规格的id查询规格选项 */
    public List<SpecificationOption> findSpecOption(Long id){
        try{
            // SELECT id,... `tb_specification_option` WHERE spec_id = ?
            // 创建对象封装条件
            SpecificationOption so = new SpecificationOption();
            so.setSpecId(id);
            return specificationOptionMapper.select(so);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 查询规格 */
    public List<Map<String,Object>> findAllByIdAndSpecName(){
        try{
            return specificationMapper.findAllByIdAndSpecName();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
