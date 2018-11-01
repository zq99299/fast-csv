package cn.mrcode.mycat.fastcsv;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author : zhuqiang
 * @version : V1.0
 * @date : 2018/11/1 22:28
 */
public class CommonTest {
    public static Charset gbk = Charset.forName("GBK");
    public static Charset utf8 = Charset.forName("utf8");
    public static String workPath = "E:/mnt/mycatcsvtest/";

    public static void print(CsvReader reader, Charset charset) throws IOException {
        while (reader.hashNext()) {
            List<byte[]> next = reader.next();
            next.stream().map(i -> new String(i, charset)).forEach(i -> {
                System.out.print("[" + i + "]");
            });
            System.out.println("");
        }
    }
}
