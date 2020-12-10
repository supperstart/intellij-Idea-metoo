package com.metoo.app.test;

import com.metoo.core.tools.CommUtil;
import com.metoo.foundation.domain.CGoods;
import com.metoo.foundation.domain.Evaluate;
import com.metoo.foundation.domain.Goods;
import com.metoo.foundation.service.IEvaluateService;
import com.metoo.foundation.service.IGoodsService;
import com.metoo.foundation.service.IStoreService;
import org.nutz.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;


@Controller
public class GoodsTest {


    @Autowired
    private IStoreService storeService;
    @Autowired
    private IEvaluateService evaluateService;
    @Autowired
    private IGoodsService goodsService;

    /**
     * 商品价格and折扣率修改
     *
     * @param request
     * @param response
     * @param name
     */
    @RequestMapping("price.json")
    public void test(HttpServletRequest request,
                     HttpServletResponse response, String name) {
        Map params = new HashMap();
        params.put("goods_status", 0);
        params.put("store_name", name);
        params.put("store_status", 15);
        List<Goods> goodsList = this.goodsService.query("select obj from Goods obj where obj.goods_status=:goods_status"
                + " and obj.goods_store.store_name=:store_name and obj.goods_store.store_status=:store_status", params, -1, -1);
		/*params.put("goods_status", 0);
		List<Goods> goodsList = this.goodsService.query("select obj from Goods obj where obj.goods_status=:goods_status", params, -1, -1);*/
        if (goodsList != null) {
            for (Goods obj : goodsList) {
                if (obj.getCgoods().size() > 0) {
                    List<CGoods> cgoodsList = obj.getCgoods();
                    List goods_price_list = new ArrayList();
                    List goods_current_price_list = new ArrayList();
                    for (CGoods cobj : cgoodsList) {
                        goods_price_list.add(cobj.getGoods_price());
                        goods_current_price_list.add(cobj.getDiscount_price());
                    }
                    BigDecimal goods_price = CommUtil.null2BigDecimal(Collections.min(goods_price_list));
                    BigDecimal store_price = CommUtil.null2BigDecimal(Collections.min(goods_current_price_list));
                    double subtract = CommUtil.subtract(goods_price, store_price);
                    double div = CommUtil.div(subtract, goods_price);
                    double mul = CommUtil.mul(div, 100);
                    BigDecimal e = new BigDecimal(mul);
                    if (obj.getGoods_price() == null) {
                        obj.setGoods_price(goods_price);
                    }
                    if (obj.getGoods_current_price() == null || obj.getStore_price() == null) {
                        obj.setGoods_current_price(store_price);
                        obj.setStore_price(store_price);
                    }
                    obj.setGoods_discount_rate(e);
                    this.goodsService.update(obj);
                } else {
                    BigDecimal goods_price = obj.getGoods_price();
                    BigDecimal store_price = obj.getGoods_current_price();
                    double subtract = CommUtil.subtract(goods_price, store_price);
                    double div = CommUtil.div(subtract, goods_price);
                    double mul = CommUtil.mul(div, 100);
                    BigDecimal e = new BigDecimal(mul);
                    obj.setGoods_discount_rate(e);
                    this.goodsService.update(obj);
                }
            }
        }
    }

    @RequestMapping("/weightiness.json")
    public void timeGoods() {
        double weight = 0.0;
        Goods goods = this.goodsService.getObjById(Long.parseLong("4204"));
        System.out.println(goods.getId());
        //商品权重计算
        double well_weight = CommUtil.mul(goods.getWell_evaluate(), "0.08"); // 星级率
        weight += well_weight;
        double discount = CommUtil.mul(goods.getGoods_discount_rate(), 0.005); // 折扣率
        weight += discount;
		/*double inventory = CommUtil.mul(goods.getGoods_inventory(), 0.01);//库存
		weight += inventory;
		System.out.println(inventory);*/

        double transport = 0.0;//运费类型
        switch (goods.getGoods_store().getGrade().getGradeName()) {

            case "Local":
                transport = CommUtil.mul(1, 0.05);
                weight += transport;
                break;
            case "FBS":
                transport = CommUtil.mul(2, 0.05);
                weight += transport;
                break;

            default:
                transport = CommUtil.mul(0, 0.05);
                weight += transport;
                break;
        }
        double transportfee = 0.0;//承担运费方式
        if (goods.getGoods_transfee() == 0) {
            transportfee = CommUtil.mul(1, 0.05);
        } else {
            transportfee = CommUtil.mul(2, 0.05);
        }
        weight += transportfee;

        double div = CommUtil.div(goods.getGoods_collect(), goods.getGoods_click());
        double collect_click = CommUtil.mul(div, 0.05);//收藏/浏览
        weight += collect_click;
        Map params = new HashMap();
        params.clear();
        params.put("evaluate_goods_id", goods.getId());
        List<Evaluate> eva_list = this.evaluateService
                .query("select obj from Evaluate obj where obj.evaluate_goods.id=:evaluate_goods_id",
                        params, -1, -1);
        double div1 = CommUtil.div(eva_list.size(), goods.getGoods_salenum());//评论率
        double evaluate_salenum = CommUtil.mul(div1, 0.05);
        weight += evaluate_salenum;

        double goods_type = 0.0;//商品类型
        if (goods.getGoods_type() == 0) {
            goods_type = CommUtil.mul(2, 0.08);
        } else {
            goods_type = CommUtil.mul(1, 0.08);
        }
        weight += goods_type;

        double goods_enough = 0.0;//是否参加满减
        if (goods.getEnough_reduce() == 0) {
            goods_enough = CommUtil.mul(0, 0.03);
        } else {
            goods_enough = CommUtil.mul(1, 0.03);
        }
        weight += goods_enough;

        double enough_free = 0.0;// 是否参满包邮
        if (goods.getEnough_free() == 0) {
            enough_free = CommUtil.mul(0, 0.03);
        } else {
            enough_free = CommUtil.mul(1, 0.03);
        }
        weight += enough_free;

        double order_enough_give_status = 0.0;// 是否参满就送
        if (goods.getOrder_enough_give_status() == 0) {
            order_enough_give_status = CommUtil.mul(0, 0.03);
        } else {
            order_enough_give_status = CommUtil.mul(1, 0.03);
        }
        weight += order_enough_give_status;

        if (goods.getCarts().size() > 0) {// 加购数/浏览数
            double div2 = CommUtil.div(goods.getCarts().size(), goods.getGoods_click());
            double cart_click = CommUtil.mul(div2, 0.03);
            weight += cart_click;
        }
        if (goods.getCarts().size() > 0) {// 售出数量/浏览数
            double div3 = CommUtil.div(goods.getGoods_salenum(), goods.getGoods_click());
            double salenum_click = CommUtil.mul(div3, 0.03);
            weight += salenum_click;
        }
		
		/*if(goods.getGoods_brand() == null ){ // brand
			 CommUtil.mul(, 0.05);
		}else{
			
		}*/

        //CommUtil.mul(goods.getGoods_brand() != null ? , 0.01);
        goods.setWeightiness(CommUtil.null2BigDecimal(weight));
        this.goodsService.update(goods);
    }

    @RequestMapping("/getGoods.json")
    @ResponseBody
    public String getGoods(HttpServletRequest request, HttpServletResponse respon) throws InterruptedException {
        Goods obj = this.goodsService.getObjById(CommUtil.null2Long(6630));
        Thread.sleep(7000);
        return Json.toJson(obj.getId());
    }
}

