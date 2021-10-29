package com.cxsplay.printerdemo.sedprinter;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class PrinterUtilsByte {
    public static final int LEFT = 0;
    public static final int MID = 1;
    public static final int RIGHT = 2;
    public static final int FONT_SIZE_0 = 0;
    public static final int FONT_SIZE_1 = 1;//纵向放大一倍
    public static final int FONT_SIZE_2 = 2;//横向放大一倍
    public static final int FONT_SIZE_3 = 3;//横向 纵向  均放大一倍
    public static final int CY = 1;
    private static final String TAG = "PrintUtils";
    private final ByteArrayOutputStream outputStream;

    private static final class Inner {
        private static final PrinterUtilsByte INSTANCE = new PrinterUtilsByte();
    }

    public static PrinterUtilsByte getInstance() {
        return Inner.INSTANCE;
    }

    private PrinterUtilsByte() {
        outputStream = new ByteArrayOutputStream();
    }

    /**
     * 将现有流置空
     */
    public void resetData() {
        try {
            outputStream.flush();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        outputStream.reset();
    }

    /**
     * 获取并返回已经写入的数据
     *
     * @return 已经写入的数据
     * @throws IOException 抛出的异常
     */
    public byte[] getDataAndReset() throws IOException {
        byte[] data;
        outputStream.flush();
        data = outputStream.toByteArray();
        outputStream.reset();
        return data;
    }

    private void printLeftNormalRightBold(String left, String right) throws IOException {
        printBold(false);
        byte[] bytes = left.getBytes("gbk");
        printAligin(0);
        outputStream.write(bytes, 0, bytes.length);
        printBold(true);
        int leftBytes = bytes.length;
        int rightBytes = right.getBytes("gbk").length;
        StringBuffer rightSpace = new StringBuffer();
        for (int i = 0; i < 32 - rightBytes - leftBytes; i++) {
            rightSpace.append(" ");
        }
        right = rightSpace.append(right).toString();
        outputStream.write(right.getBytes("gbk"), 0, right.getBytes("gbk").length);
        printNextLine();
    }

    private void printUnline() throws IOException {
        for (int i = 0; i < 32; i++) {
            outputStream.write("-".getBytes("gbk"));
        }
        printNextLine();
    }

    public void printNextText(String hello_no_hao) throws IOException {
        printText(hello_no_hao, 0);
    }

    public void printNextText(String hello_no_hao, int aligin) throws IOException {
        printAligin(aligin);
        outputStream.write("\n".getBytes("gbk"));
        String s = hello_no_hao;
        byte[] bytes = s.getBytes("gbk");
        outputStream.write(bytes);
    }


    /****
     * 这里是打印机初始化，需要在每次打印开始时候调用
     * 详细文档在如下地址
     * http://www.xmjjdz.com/downloads/manual/cn/ESC(POS)%E6%89%93%E5%8D%B0%E6%8E%A7%E5%88%B6%E5%91%BD%E4%BB%A4.pdf
     *
     * ESC @ 打印机初始化
     * 格式： ASCII： ESC @
     * 十进制： 27 64
     * 十六进制： 1B 40
     * 说明： ESC
     * @命令初始化打印机。
     *  清除打印缓冲区
     * 恢复默认值
     * 选择字符打印方式
     * 删除用户自定义字符
     *
     * */
    public void initPrint() throws IOException {
        write(0x1b);
        write(0x40);
    }

    public void printText(String text) throws IOException {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        printText(text, 0);
    }

    public void printText(String text, int aligin) throws IOException {
        printText(text, aligin, false);
    }

    public void printText(String text, int aligin, boolean bold) throws IOException {
        printText(text, aligin, bold, false);
    }

    public void printText(String text, int aligin, boolean bold, boolean underline) throws IOException {
        printText(text, aligin, bold, underline, FONT_SIZE_0);
    }

    public void printText(String text, int aligin, int size) throws IOException {
        printText(text, aligin, false, false, size);
    }

    public void printText(String text, int aligin, boolean isBold, int size) throws IOException {
        printText(text, aligin, isBold, false, size);
    }

    public void printText(String text, int aligin, boolean bold, boolean underline, int size) throws IOException {
        printNextLine();
        printAligin(aligin);
        printBold(bold);
        printUnderline(underline);
        Log.e("========", "执行打印方法");
        switch (size) {
            //标准大小
            case FONT_SIZE_0:
                outputStream.write(0x1c);
                outputStream.write(0x21);
                outputStream.write(1);
                break;
            //纵向放大一倍
            case FONT_SIZE_1:
                outputStream.write(0x1c);
                outputStream.write(0x21);
                outputStream.write(8);
                break;
            //横向放大一倍
            case FONT_SIZE_2:
                outputStream.write(0x1c);
                outputStream.write(0x21);
                outputStream.write(4);
                break;
            //横向纵向放大一倍
            case FONT_SIZE_3:
                outputStream.write(0x1c);
                outputStream.write(0x21);
                outputStream.write(12);
                break;
        }
        byte[] bytes = text.getBytes("gbk");
        outputStream.write(bytes, 0, bytes.length);
    }

    public Bitmap convertGreyImgByFloyd(Bitmap img) {
        int width = img.getWidth();
        //获取位图的宽
        int height = img.getHeight();
        //获取位图的高 \
        int[] pixels = new int[width * height];
        //通过位图的大小创建像素点数组
        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] gray = new int[height * width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                int red = ((grey & 0x00FF0000) >> 16);
                gray[width * i + j] = red;
            }
        }
        int e = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int g = gray[width * i + j];
                if (g >= 128) {
                    pixels[width * i + j] = 0xffffffff;
                    e = g - 255;
                } else {
                    pixels[width * i + j] = 0xff000000;
                    e = g - 0;
                }
                if (j < width - 1 && i < height - 1) {
                    //右边像素处理
                    gray[width * i + j + 1] += 3 * e / 8;
                    //下
                    gray[width * (i + 1) + j] += 3 * e / 8;
                    //右下
                    gray[width * (i + 1) + j + 1] += e / 4;
                } else if (j == width - 1 && i < height - 1) {
                    //靠右或靠下边的像素的情况
                    //下方像素处理
                    gray[width * (i + 1) + j] += 3 * e / 8;
                } else if (j < width - 1 && i == height - 1) {
                    //右边像素处理
                    gray[width * (i) + j + 1] += e / 4;
                }
            }
        }
        Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return mBitmap;
    }

    /**
     * ESC a n 设置输出对齐方式
     * 格式： ASCII： ESC a n
     * 十进制： 27 97 n
     * 十六进制： 1B 61 n
     * 说明： 该命令只对标准模式有效
     * 0 ≤ m ≤ 2 或 48 ≤ m ≤ 50
     * 左对齐: n=0,48
     * 居中对齐: n=1,49
     * 右对齐: n=2,50
     *
     * @param aligin 0 左对齐 1 居中 2 右对齐
     */

    public void printAligin(int aligin) throws IOException {
        write(0x1b);
        write(97);
        write(aligin);
    }

    /***
     * 换行 写入\n
     * **/
    public void printNextLine() throws IOException {
        printNextLine(1);
    }

    public void printLine() throws IOException {
        printText("- - - - - - - - - - - - - - -");
    }

    public void printNextLine(int num) throws IOException {
        for (int i = 0; i < num; i++) {
            write("\n");
        }
    }

    /***
     * @param value 写入十六进制内容
     * **/
    private void write(int value) throws IOException {
        outputStream.write(value);
    }

    /***
     * @param value 写入字符串内容
     * **/
    public void write(String value) throws IOException {
        outputStream.write(value.getBytes("gbk"));
    }

    private int getByteLength(String text) {
        return text.getBytes(Charset.forName("gb2312")).length;
    }

    public String leftRight(String left, String right) {
        if (TextUtils.isEmpty(left) || TextUtils.isEmpty(right)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int leftLength = getByteLength(left);
        int rightLength = getByteLength(right);
        int spaceSize = 32 - leftLength - rightLength;
        sb.append(left);
        for (int i = 0; i < spaceSize; i++) {
            sb.append(" ");
        }
        sb.append(right);
        return sb.toString();
    }

    public String leftRight(String left, String unit, String right) {
        if (TextUtils.isEmpty(left) || TextUtils.isEmpty(right) || right.equals("0.00")) {
            return "";
        }
        right = unit + right;
        StringBuilder sb = new StringBuilder();
        int leftLength = getByteLength(left);
        int rightLength = getByteLength(right);
        int spaceSize = 32 - leftLength - rightLength;
        sb.append(left);
        for (int i = 0; i < spaceSize; i++) {
            sb.append(" ");
        }
        sb.append(right);

        return sb.toString();
    }

    public String printThreeData(String left, String middle, String right) {
        StringBuilder sb = new StringBuilder();
        int leftLength = getByteLength(left);
        Log.d(TAG, "printThreeData: leftLength= " + leftLength);
        int middleLength = getByteLength(middle);
        int rightLength = getByteLength(right);
        if (left.length() > 5) {
            left = left.substring(0, 5) + "..";
            leftLength = getByteLength(left);
        }
        sb.append(left);
        int LeftAndMiddleSpaceLength = 16 - leftLength - middleLength / 2;
        for (int i = 0; i < LeftAndMiddleSpaceLength; i++) {
            sb.append(" ");
        }
        sb.append(middle);
        int middleAndRightSpace = 16 - rightLength - middleLength / 2;
        for (int i = 0; i < middleAndRightSpace; i++) {
            sb.append(" ");
        }
        sb.delete(sb.length() - 1, sb.length()).append(right);
        return sb.toString();
    }

    /**
     * 三列数据算法（商品，数量，价格）
     * 默认每行长度32
     *
     * @param left
     * @param mid
     * @param right
     * @return
     */
    public String formateThreeData(String left, String mid, String right) {
        return formateThreeData(left, "", mid, right);
    }

    public String formateThreeData(String left, String left_end, String mid, String right) {
        int maxlength = 32;//蓝牙打印机，默认每行最大32位
        int rightlength = 10;
        int leftMidLength = maxlength - rightlength;
        //如果left和mid长度超过
        if (strlen(left + left_end + mid) >= leftMidLength) {
            while (strlen(left + "..." + left_end + mid) >= leftMidLength) {
                left = left.substring(0, left.length() - 1);
            }
            left += "..." + left_end;
            //减得时候，故意多减，补空格，以便分开
            int addleftNum = leftMidLength - strlen(left + mid);
            for (int i = 0; i < addleftNum; i++) {
                left += " ";
            }
        } else {//left和mid长度不够，补空格
            left += left_end;
            int addleftNum = leftMidLength - strlen(left + mid);
            for (int i = 0; i < addleftNum; i++) {
                left += " ";
            }
        }
        //默认价格8位，可达99999.99
        if (strlen(right) < rightlength) {
            int addrightNum = rightlength - strlen(right);
            for (int i = 0; i < addrightNum; i++) {
                right = " " + right;
            }
        }
        return left + mid + right;
    }

    private static int strlen(String str) {
        if (str == null) {
            return 0;
        }

        return str.replaceAll("[^\\x00-\\xff]", "**").length();
    }

    public String printThreeLeftBoldOthersNormalData(String left, String middle, String right) throws IOException {

        printBold(true);
        printAligin(0);
        StringBuilder sb = new StringBuilder();
        int leftLength = getByteLength(left);
        Log.d(TAG, "printThreeData: leftLength= " + leftLength);
        int middleLength = getByteLength(middle);
        int rightLength = getByteLength(right);
        if (left.length() > 5) {
            left = left.substring(0, 5) + "..";
            leftLength = getByteLength(left);
        }
        outputStream.write(left.getBytes("gbk"), 0, left.getBytes("gbk").length);
        //sb.append(left);
        printBold(false);
        int LeftAndMiddleSpaceLength = 16 - leftLength - middleLength / 2;
        for (int i = 0; i < LeftAndMiddleSpaceLength; i++) {
            sb.append(" ");
        }
        sb.append(middle);
        int middleAndRightSpace = 16 - rightLength - middleLength / 2;
        for (int i = 0; i < middleAndRightSpace; i++) {
            sb.append(" ");
        }
        sb.delete(sb.length() - 1, sb.length()).append(right);
        write(sb.toString());
        printNextLine();
        return sb.toString();
    }

    public String printFourData(String one, String two, String three, String four) {
        StringBuilder sb = new StringBuilder();
        int oneLength = getByteLength(one);
        //第一个字长度<8
        if (oneLength < 8) {
            int oneLSpace = (8 - oneLength) / 2;
            int oneRSpace = 8 - oneLength - oneLSpace;
            for (int i = 0; i < oneLSpace; i++) {
                sb.append(" ");
            }
            sb.append(one);
            for (int i = 0; i < oneRSpace; i++) {
                sb.append(" ");
            }
        }

        int twoLength = two.length();
        if (twoLength < 8) {
            int twoLSpace = (8 - twoLength) / 2;
            int twoRSpace = 8 - twoLength - twoLSpace;
            for (int i = 0; i < twoLSpace; i++) {
                sb.append(" ");
            }
            sb.append(two);
            for (int i = 0; i < twoRSpace; i++) {
                sb.append(" ");
            }
        }

        int threeLength = three.length();
        if (threeLength < 8) {
            int threeLSpace = (8 - threeLength) / 2;
            int threeRSpace = 8 - threeLength - threeLSpace;
            for (int i = 0; i < threeLSpace; i++) {
                sb.append(" ");
            }
            sb.append(three);
            for (int i = 0; i < threeRSpace; i++) {
                sb.append(" ");
            }
        }

        int fourLength = four.length();
        if (fourLength < 8) {
            int fourLSpace = (8 - fourLength) / 2;
            int fourRSpace = 8 - fourLength - fourLSpace;
            for (int i = 0; i < fourLSpace; i++) {
                sb.append(" ");
            }
            sb.append(four);
            for (int i = 0; i < fourRSpace; i++) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    /***
     *
     * 0x1b, 0x45, 0x01  加粗
     * 0x1b, 0x45, 0x00   取消加粗
     * **/
    public void printBold(boolean isBold) throws IOException {
        write(0x1b);
        write(0x45);
        if (isBold) {
            write(1);
        } else {
            write(0);
        }
    }

    /**
     * 打印下划线
     * 0x1b, 0x2d, 0x01
     **/
    public void printUnderline(boolean isUnderline) throws IOException {
        write(0x1b);
        write(0x2d);
        if (isUnderline) {
            write(1);
        } else {
            write(0);
        }
    }
}
