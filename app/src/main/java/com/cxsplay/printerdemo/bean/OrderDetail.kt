package com.cxsplay.printerdemo.bean

import java.io.Serializable

/**
 * Created by chuxiaoshan on 2021/7/6 18:28
 *
 * Description: 订单详情综合类
 */

class OrderDetail : Serializable {

    var id: String? = null

    //类型
    var typeCate: String? = null

    //类型文本
    var typeCateTxt: String? = null

    //应付金额
    var amount: String? = null

    //总支付金额
    var sumPaidAmount: String? = null

    //商品总金额
    var goodsAmount: String? = null

    //优惠券优惠, 减免价
    var couponAmount: String? = null

    //会员折扣
    var cardMinus: String? = null

    //优惠总金额
    var minusAmount: String? = null

    //订单来源描述
    var sourceTxt: String? = null

    //门店名称
    var multiStoreName: String? = null

    //当前状态描述
    var statusTxt: String? = null

    //订单编号
    var orderNo: String? = null

    //创建时间
    var createdAt: String? = null

    //支付方式说明
    var payTypeTxt: String? = null

    //打包费
    var packCost: String? = null

    //用户输入的取餐牌号
    var tableCardNum: String? = null

    //取单号
    var storeOrderNo: String? = null

    //商品集合
    var orderGoods: MutableList<OrderGoods>? = null

    //商品数量
    val goodsCount: Int?
        get() = orderGoods?.fold(0) { total, next -> total + (next.num ?: "0").toInt() }
}

class OrderGoods : Serializable {
    //商品名称
    var name: String? = null

    //商品数量
    var num: String? = null

    //商品价格
    var price: String? = null

    // 规格
    var spec: String? = null

    //做法
    var property: String? = null

    //随心配
    var orderGiveGoods: MutableList<OrderGiveGoods>? = null

    //加料
    var orderAttachGoods: MutableList<OrderAttachGoods>? = null
}

class OrderAttachGoods : Serializable {
    //数量
    var num: String? = null

    //名称
    var name: String? = null

    //价格
    var price: String? = null
}

class OrderGiveGoods : Serializable {
    //数量
    var num: String? = null

    //名称
    var name: String? = null

    //价格
    var price: String? = null

    //规格
    var spec: String? = null
}