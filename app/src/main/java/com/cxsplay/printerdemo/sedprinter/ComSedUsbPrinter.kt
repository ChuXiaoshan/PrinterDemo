package com.cxsplay.printerdemo.sedprinter

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Parcelable
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.cxsplay.printerdemo.bean.OrderAttachGoods
import com.cxsplay.printerdemo.bean.OrderDetail
import com.cxsplay.printerdemo.bean.OrderGiveGoods
import com.cxsplay.printerdemo.bean.OrderGoods

/**
 * Created by chuxiaoshan on 2021/10/27 13:41
 *
 * Description: 桑达 Usb 打印机相关操作集合
 */
object ComSedUsbPrinter {

    const val USB_PERMISSION = "com.android.usb.USB_PERMISSION"

    private val manager by lazy {
        Utils.getApp().getSystemService(Context.USB_SERVICE) as UsbManager
    }

    private val mUsbDriver by lazy { UsbDriver() }

    private var mDevice: UsbDevice? = null

    private var mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val action = intent?.action
            LogUtils.d("receiver action: $action")
            if (USB_PERMISSION == action && mDevice != null) {
                synchronized(this) {
                    val device =
                        intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    val isGranted =
                        intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    if (isGranted && mDevice == device) {
                        if (mUsbDriver.usbAttached(mDevice)) mUsbDriver.openUsbDevice(mDevice)
                    } else {
                        LogUtils.d("permission denied for device $device")
                    }
                }
            }
        }
    }


    fun connectUSB() {
        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(USB_PERMISSION)
        Utils.getApp().registerReceiver(mUsbReceiver, filter)
        LogUtils.d("---list--->${manager.deviceList}")
        manager.deviceList?.forEach {
            val device = it.value
            LogUtils.d(
                "---name--->${device?.deviceName}" +
                        "\n---vid--->${device?.vendorId}" +
                        "\n---pid--->${device?.productId}" +
                        "\n---pName--->${device?.productName}"
            )
            if (isUsbPrinter(device)) {
                mDevice = device
            }
        }
        if (mDevice != null) {
            LogUtils.d(
                "---name--->${mDevice?.deviceName}" +
                        "\n---vid--->${mDevice?.vendorId}" +
                        "\n---pid--->${mDevice?.productId}" +
                        "\n---pName--->${mDevice?.productName}"
            )
            if (manager.hasPermission(mDevice)) {
                if (mUsbDriver.usbAttached(mDevice)) mUsbDriver.openUsbDevice(mDevice)
            } else {
                // 没有权限询问用户是否授予权限
                val pendingIntent =
                    PendingIntent.getBroadcast(Utils.getApp(), 0, Intent(USB_PERMISSION), 0)
                manager.requestPermission(mDevice, pendingIntent)
            }
        } else {
            ToastUtils.showShort("未发现USB打印机")
        }
    }

    private fun isUsbPrinter(device: UsbDevice): Boolean {
        return (device.productId == 8211 && device.vendorId == 1305 || device.productId == 8213 && device.vendorId == 1305)
    }

    fun closeConnection() {
        //mUsbDriver.closeUsbDevice()
        try {
            Utils.getApp().unregisterReceiver(mUsbReceiver)
        } catch (e: Exception) {
        }
    }

    fun printOrder(bean: OrderDetail) {
        mUsbDriver.write(PrintCmd.PrintFeedDot(6))
        mUsbDriver.write(PrintCmd.SetAlignment(1))
        mUsbDriver.write(PrintCmd.SetSizetext(1, 1))
        mUsbDriver.write(PrintCmd.PrintString("${bean.multiStoreName}", 0))
        if (!bean.storeOrderNo.isNullOrBlank()) {
            mUsbDriver.write(PrintCmd.PrintString(bean.storeOrderNo, 0))
        }
        mUsbDriver.write(PrintCmd.SetSizetext(0, 0))
        mUsbDriver.write(PrintCmd.PrintString("-------------------------------", 0))

        if (!bean.tableCardNum.isNullOrBlank()) {
            mUsbDriver.write(PrintCmd.SetSizetext(1, 1))
            mUsbDriver.write(PrintCmd.PrintString("取餐牌号#${bean.tableCardNum}", 0))
        }
        mUsbDriver.write(PrintCmd.SetSizetext(0, 0))
        mUsbDriver.write(PrintCmd.PrintString("\n-- ${bean.statusTxt} --", 0))
        mUsbDriver.write(PrintCmd.PrintString("-------------------------------", 0))
        mUsbDriver.write(PrintCmd.SetAlignment(0))
        val typeCateText = when (bean.typeCate) {
            "1" -> "堂食"
            "2" -> "打包"
            else -> bean.typeCateTxt
        }
        mUsbDriver.write(PrintCmd.PrintString("【订单类型】${typeCateText}", 0))
        mUsbDriver.write(PrintCmd.PrintString("【订单来源】${bean.sourceTxt}", 0))
        mUsbDriver.write(PrintCmd.PrintString("【订单编号】${bean.orderNo}", 0))
        mUsbDriver.write(PrintCmd.PrintString("【下单时间】${bean.createdAt}", 0))
        mUsbDriver.write(PrintCmd.PrintString("-------------------------------", 0))

        val goodsList = bean.orderGoods
        if (!goodsList.isNullOrEmpty()) {
            val bByte = ByteArray(2)
            bByte[0] = 18
            bByte[1] = 24
            mUsbDriver.write(PrintCmd.SetHTseat(bByte, 2))
            mUsbDriver.write(PrintCmd.PrintString("商品", 1))
            mUsbDriver.write(PrintCmd.PrintNextHT())
            mUsbDriver.write(PrintCmd.PrintString("数量", 1))
            mUsbDriver.write(PrintCmd.PrintNextHT())
            mUsbDriver.write(PrintCmd.PrintString("单价", 0))
            goodsList.forEach {
                mUsbDriver.write(PrintCmd.PrintString("${it.name}", 1))
                mUsbDriver.write(PrintCmd.PrintNextHT())
                mUsbDriver.write(PrintCmd.PrintString("${it.num}", 1))
                mUsbDriver.write(PrintCmd.PrintNextHT())
                mUsbDriver.write(PrintCmd.PrintString("${it.price}", 0))
                //规格/做法/加料
                val spec = if (!it.spec.isNullOrBlank()) "${it.spec} " else ""
                val property = if (!it.property.isNullOrBlank()) "${it.property} " else ""
                val specStr = it.orderAttachGoods?.fold("$spec$property") { total, next ->
                    total + "${next.name}x${next.num} "
                }?.trim()
                if (!specStr.isNullOrBlank()) {
                    mUsbDriver.write(PrintCmd.PrintString("$specStr", 0))
                }
                //套餐随心配
                it.orderGiveGoods?.forEach { combined ->
                    mUsbDriver.write(
                        PrintCmd.PrintString(
                            " ·${combined.name} x${combined.num} ￥${combined.price}",
                            0
                        )
                    )
                }
            }
            mUsbDriver.write(PrintCmd.PrintString("-------------------------------", 0))
        }
        mUsbDriver.write(PrintCmd.PrintString("商品合计", 1))
        mUsbDriver.write(PrintCmd.PrintNextHT())
        mUsbDriver.write(PrintCmd.PrintNextHT())
        mUsbDriver.write(PrintCmd.PrintString("￥${bean.goodsAmount}", 0))
        if (!bean.packCost.isNullOrBlank() && bean.packCost != "0") {
            mUsbDriver.write(PrintCmd.PrintString("打包费", 1))
            mUsbDriver.write(PrintCmd.PrintNextHT())
            mUsbDriver.write(PrintCmd.PrintNextHT())
            mUsbDriver.write(PrintCmd.PrintString("￥${bean.packCost}", 0))
        }
        if (!bean.minusAmount.isNullOrBlank() && bean.minusAmount != "0") {
            mUsbDriver.write(PrintCmd.PrintString("优惠合计", 1))
            mUsbDriver.write(PrintCmd.PrintNextHT())
            mUsbDriver.write(PrintCmd.PrintNextHT())
            mUsbDriver.write(PrintCmd.PrintString("￥${bean.minusAmount}", 0))
        }
        mUsbDriver.write(PrintCmd.PrintString("支付方式", 1))
        mUsbDriver.write(PrintCmd.PrintNextHT())
        mUsbDriver.write(PrintCmd.PrintNextHT())
        mUsbDriver.write(PrintCmd.PrintString("${bean.payTypeTxt}", 0))
        mUsbDriver.write(PrintCmd.PrintString("已付", 1))
        mUsbDriver.write(PrintCmd.PrintNextHT())
        mUsbDriver.write(PrintCmd.PrintNextHT())
        mUsbDriver.write(PrintCmd.PrintString("￥${bean.sumPaidAmount}", 0))
        mUsbDriver.write(PrintCmd.PrintString("-------------------------------", 0))
        mUsbDriver.write(PrintCmd.SetAlignment(1))
        mUsbDriver.write(PrintCmd.PrintString("谢谢惠顾，欢迎下次光临！", 0))
        mUsbDriver.write(PrintCmd.PrintFeedline(6))
        mUsbDriver.write(PrintCmd.PrintCutpaper(1))
    }

    fun printTest() {
        val testData1 = "testtesttesttesttesttesttesttesttesttesttesttest\n"
        val testData2 = "111111111111111111111111111111111111111111111111\n"
        val testData3 = "拂拙招坡披拨择抬拇拗其取茉苦昔苛若茂苹苗英苟苑苞\n"
//        mUsbDriver.write(PrintCmd.PrintString(testData1, 1))
//        mUsbDriver.write(PrintCmd.PrintString(testData2, 1))
//        mUsbDriver.write(PrintCmd.PrintString(testData3, 1))
//        mUsbDriver.write(PrintCmd.PrintFeedline(6))
//        mUsbDriver.write(PrintCmd.PrintCutpaper(1))

        mUsbDriver.write(MyPrintDataMaker.getTestData58())
        mUsbDriver.write(MyPrintDataMaker.getTestData80())

        val bean = OrderDetail().apply {
            typeCateTxt = "待备餐"
            amount = "100.00"
            sumPaidAmount = "200.00"
            goodsAmount = "300.00"
            couponAmount = "400.00"
            cardMinus = "500.00"
            minusAmount = "600.00"
            sourceTxt = "点餐屏"
            multiStoreName = "企迈小店002企迈"
            statusTxt = "待备餐"
            orderNo = "D53100265565651589535"
            createdAt = "2021-10-26 19:37:17"
            payTypeTxt = "已支付已支付"
            packCost = "56.78"
            tableCardNum = "002"
            storeOrderNo = "004"
            val goodsList = mutableListOf<OrderGoods>()
            goodsList.add(OrderGoods().apply {
                name = "百事可乐"
                num = "12"
                price = "123.45"
                spec = "呵呵，和我，我发，诶诶温哥华"
                property = "去冰，多冰，少冰"
                val giveGoodsList = mutableListOf<OrderGiveGoods>()
                giveGoodsList.add(OrderGiveGoods().apply {
                    name = "随心配"
                    num = "12"
                    price = "12.34"
                    spec = "呵呵，和我，我发"
                })
                orderGiveGoods = giveGoodsList
                val orderAttachList = mutableListOf<OrderAttachGoods>()
                orderAttachList.add(OrderAttachGoods().apply {
                    num = "11"
                    name = "小菜"
                    price = "56.78"
                })
                orderAttachGoods = orderAttachList
            })
            goodsList.add(OrderGoods().apply {
                name = "百事可乐百事可乐百\n事可可"
                num = "12"
                price = "123.45"
            })
            orderGoods = goodsList
        }
//        printOrder(bean)
    }
}