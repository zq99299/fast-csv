package cn.mrcode.mycat.fastcsv.wite;

import java.io.IOException;
import java.io.OutputStream;

import cn.mrcode.mycat.fastcsv.DefaultCsvReaderConfig;
import cn.mrcode.mycat.fastcsv.Letters;

/**
 * csv 写支持
 *
 * @author : zhuqiang
 * @date : 2018/11/26 23:03
 */
public class CsvWriter {
    // 先实现 普通 io 的写出
    private OutputStream os;
    // 列分隔符，换行符默认，不允许执行
    private byte delimiter = DefaultCsvReaderConfig.DELIMITER;
    // 是否安装边界（字段双引号引用起来，特殊字符进行 RFC 4180标准 标准转译）
    private boolean textQualifier = true;

    public CsvWriter(OutputStream os) {
        this.os = os;
    }

    /**
     * @param payload     列中的一个字段
     * @param isColumnEnd 是否是这一列的结尾
     */
    public void write(byte[] payload, boolean isColumnEnd) throws IOException {
        if (textQualifier) {
            textQualifierWrite(payload);
        } else {
            os.write(payload);
        }
        if (!isColumnEnd) {
            os.write(delimiter);
        }
    }

    // 新的一行
    public void newLine() throws IOException {
        os.write(Letters.CR);
        os.write(Letters.LF);
    }

    private void textQualifierWrite(byte[] payload) throws IOException {
        os.write(Letters.TEXT_QUALIFIER);
        for (byte currentLetter : payload) {
            os.write(currentLetter);
            if (currentLetter == Letters.TEXT_QUALIFIER) {
                os.write(Letters.TEXT_QUALIFIER);
            }
        }
        os.write(Letters.TEXT_QUALIFIER);
    }
}
