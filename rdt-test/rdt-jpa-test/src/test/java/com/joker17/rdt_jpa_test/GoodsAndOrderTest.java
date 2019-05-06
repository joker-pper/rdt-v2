package com.joker17.rdt_jpa_test;

import com.alibaba.fastjson.JSON;
import com.joker17.rdt_jpa_test.domain.Goods;
import com.joker17.rdt_jpa_test.domain.Order;
import com.joker17.rdt_jpa_test.domain.User;
import com.joker17.rdt_jpa_test.service.IGoodsService;
import com.joker17.rdt_jpa_test.service.IOrderService;
import com.joker17.rdt_jpa_test.support.JsonUtils;
import com.joker17.redundant.core.RdtConfiguration;
import com.joker17.redundant.fill.FillType;
import com.joker17.redundant.model.ComplexAnalysis;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

public class GoodsAndOrderTest extends ApplicationTests {

    @Resource
    private IGoodsService goodsService;

    @Resource
    private IOrderService orderService;

    @Resource
    private RdtConfiguration rdtConfiguration;

    /**
     * 初始化数据
     */
    @Test
    public void initData() {
        goodsService.deleteAll();
        orderService.deleteAll();
        Goods goods = new Goods("1", "商品1", 2333);
        goodsService.save(goods);

        List<Order> orderList = new ArrayList<>();

        for (int i = 0; i < 6; i ++) {
            Order order = new Order();
            order.setId(i + 1 + "");
            order.setGoodsId("1");
            //设置商品名称,由于设置为@Transient不会被保存
            order.setGoodsName(goods.getName());
            order.setPrice(goods.getPrice());
            order.setType(new Random().nextInt(2) + 1);
            orderList.add(order);
        }

        orderService.saveAll(orderList);
    }

    /**
     * 更新goods同时更新状态为未付款的订单金额
     */
    @Test
    @Transactional
    @Rollback(false)
    public void updateGoods() {
        Goods goods = goodsService.getOne("1");
        Goods before = JSON.parseObject(JsonUtils.toJson(goods), Goods.class);
        goods.setName("新商品1");
        goods.setPrice(6666);
        goodsService.save(goods);
        //更新相关数据,将会只更新order表中price相关的数据
        rdtOperation.updateMulti(goods, before);
    }



    /**
     * 显示所有订单信息
     */
    @Test
    public void findAllOrder() {
        logger.info("result: {}", JsonUtils.toJson(orderService.findAll()));
    }

    @Test
    public void newOrderWithFill() {
        Order order = new Order();
        order.setId("222");
        order.setGoodsId("1");
        order.setType(2);
        //save填充当前数据中要持久化的price字段
        rdtOperation.fillForSave(Arrays.asList(order));
        logger.info("result: {}", JsonUtils.toJson(order));

        //show填充当前数据中未持久化的goodsName字段
        rdtOperation.fillForShow(Arrays.asList(order));
        logger.info("result: {}", JsonUtils.toJson(order));

    }


    @Test
    public void findAllOrderWithFill() {
        List<Order> orderList = orderService.findAll();
        //默认只会填充列为transient的字段值(即更新+填充的方式可以同时使用,默认不会填充持久化的数据,可配置)
        rdtOperation.fillForShow(orderList);
        logger.info("result: {}", JsonUtils.toJson(orderList));
        logger.info("----------------------------------------------------");
        //会填充所有字段
        rdtOperation.fillForShow(orderList, false, FillType.ALL);
        logger.info("result: {}", JsonUtils.toJson(orderList));
    }


    @Test
    public void newOrderWithFills() {
        Order order = new Order();
        order.setId("222");
        order.setGoodsId("1");
        order.setGoodsName("sadsada");
        order.setType(3);
        //save填充当前数据中要持久化的price字段
        rdtOperation.fillForSave(Arrays.asList(order));
        logger.info("result: {}", JsonUtils.toJson(order));

        //show填充当前数据中未持久化的goodsName字段
        rdtOperation.fillForShow(Arrays.asList(order), true);
        logger.info("result: {}", JsonUtils.toJson(order));

    }
}
