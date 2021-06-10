package com.yzm.solr;

import com.yzm.DemoApplication;
import com.yzm.pojo.Product;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Auther: yzm
 * @Date: 2021/6/10 - 06 - 10 - 15:30
 */

@SpringBootTest(classes = DemoApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SolrTest {

    @Autowired
    private SolrTemplate solrTemplate;

    @Test
    public void test(){
        SolrInputDocument solrInputDocument = new SolrInputDocument();
        solrInputDocument.addField("id","11");
        solrInputDocument.addField("name","热卖国产手机");
        solrInputDocument.addField("price","1999");

        solrTemplate.saveDocument("testcore",solrInputDocument);
        solrTemplate.commit("testcore");
    }

    @Test
    public void test2(){
        Product product = new Product("12","美国手机",2798.1);
        solrTemplate.saveBean("testcore",product);
        solrTemplate.commit("testcore");
    }

    @Test
    public void test3(){
        Product product = new Product("13","非洲手机",2798.1);
        Product product2 = new Product("14","欧洲手机",2798.1);
        List<Product> list = new ArrayList<>();
        Collections.addAll(list,product,product2);
        solrTemplate.saveBeans("testcore",list);
        solrTemplate.commit("testcore");
    }

    @Test
    public void testUpdate(){
        SolrInputDocument solrInputDocument = new SolrInputDocument();
        solrInputDocument.addField("id","11");
        solrInputDocument.addField("name","热卖国产汽车");
        solrInputDocument.addField("price","199999");

        solrTemplate.saveDocument("testcore",solrInputDocument);
        solrTemplate.commit("testcore");
    }

    @Test
    public void testDelete(){

        solrTemplate.deleteByIds("testcore","14");
        solrTemplate.commit("testcore");
    }


    @Test
    public void testQuery(){
        SimpleQuery simpleQuery = new SimpleQuery();
        Criteria criteria = new Criteria("name");
        criteria.is("手机");

        simpleQuery.addCriteria(criteria);

        ScoredPage<Product> testcore = solrTemplate.queryForPage("testcore", simpleQuery, Product.class);
        List<Product> list = testcore.getContent();
        for (Product product : list) {
            System.out.println(product);
        }
    }

    @Test
    public void testQuery2(){

        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //设置查询条件
        Criteria criteria = new Criteria("name");
        criteria.is("手机");
        query.addCriteria(criteria);
        //分页
        query.setOffset(0L);
        query.setRows(10);
        //排序
        Sort sort = Sort.by(Sort.Direction.DESC,"price");
        query.addSort(sort);
        //设置高亮
        HighlightOptions hlo = new HighlightOptions();
        hlo.addField("name");
        hlo.setSimplePrefix("<span>");
        hlo.setSimplePostfix("</span>");
        query.setHighlightOptions(hlo);

        HighlightPage<Product> hl = solrTemplate.queryForHighlightPage("testcore", query, Product.class);

        List<HighlightEntry<Product>> highlighted = hl.getHighlighted();
        for(HighlightEntry<Product> hle : highlighted){
            List<HighlightEntry.Highlight> list = hle.getHighlights();
            Product product = hle.getEntity();

            for (HighlightEntry.Highlight h : list){//一个对象里面可能多个属性是高亮属性
                if(h.getField().getName().equals("name")){
                    product.setName( h.getSnipplets().get(0));

                    System.out.println(product);
                }
            }
        }
    }
}
