package com.cxsplay.printerdemo.sedprinter

import com.cxsplay.printer.PrinterWriter
import com.cxsplay.printer.PrinterWriter58mm
import com.cxsplay.printer.PrinterWriter80mm

/**
 * Created by chuxiaoshan on 2021/10/29 13:48
 *
 * Description:
 */
object MyPrintDataMaker {


    /**
     * 测试数据格式
     */
    fun getTestData58(): ByteArray {
        try {
            val printer: PrinterWriter = PrinterWriter58mm()
            printer.dataAndReset
            printer.setAlignCenter()
            printer.print("------------------------------------------------")
            printer.setEmphasizedOn()
            printer.printLineFeed()
            printer.setFontSize(0)
            printer.print("test  test test")
            printer.setEmphasizedOff()
            printer.printLineFeed()
            printer.print("test  test test")
            printer.printLineFeed()
            printer.print("test  test test")
            printer.printLineFeed()
            printer.print("test  test test")
            printer.print("testtesttesttesttesttesttesttesttesttesttesttesttest\n")
            printer.print("拂拙招坡披拨择抬拇拗其取茉苦昔苛若茂苹苗英苟苑苞范待备餐待备\n")
            printer.printLineFeed()
            printer.printInOneLine("商品", "价格", 0)
            printer.printLineFeed()
            printer.printInOneLine("商品", "价格", 1)
            printer.printLineFeed()
            printer.print("------------------------------------------------")
            printer.printLineFeed()
            printer.printLineFeed()
            printer.printLineFeed()

            printer.setAlignCenter()
            printer.feedPaperCutPartial()
            return printer.dataAndClose
        } catch (e: Exception) {
            return byteArrayOf()
        }
    }

    /**
     * 测试数据格式
     */
    fun getTestData80(): ByteArray {
        try {
            val printer: PrinterWriter = PrinterWriter80mm()
            printer.dataAndReset
            printer.setAlignCenter()
            printer.print("------------------------------------------------")
            printer.setEmphasizedOn()
            printer.printLineFeed()
            printer.setFontSize(0)
            printer.print("test  test test")
            printer.setEmphasizedOff()
            printer.printLineFeed()
            printer.print("test  test test")
            printer.printLineFeed()
            printer.print("test  test test")
            printer.printLineFeed()
            printer.print("test  test test")
            printer.printLineFeed()
            printer.print("testtesttesttesttesttesttesttesttesttesttesttesttest\n")
            printer.print("拂拙招坡披拨择抬拇拗其取茉苦昔苛若茂苹苗英苟苑苞范待备餐待备\n")
            printer.printLineFeed()
            printer.printInOneLine("商品", "价格", 0)
            printer.printLineFeed()
            printer.printInOneLine("商品", "价格", 1)
            printer.printLineFeed()
            printer.print("------------------------------------------------")
            printer.printLineFeed()
            printer.printLineFeed()
            printer.printLineFeed()

            printer.setAlignCenter()
            printer.feedPaperCutPartial()
            return printer.dataAndClose
        } catch (e: Exception) {
            return byteArrayOf()
        }
    }
}