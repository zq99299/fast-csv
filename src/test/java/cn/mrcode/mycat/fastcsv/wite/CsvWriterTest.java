package cn.mrcode.mycat.fastcsv.wite;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cn.mrcode.mycat.fastcsv.CommonTest;
import cn.mrcode.mycat.fastcsv.CsvReader;
import cn.mrcode.mycat.fastcsv.DefaultCsvReader;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2018/11/26 23:22
 */
public class CsvWriterTest {
    /**
     * 有特殊字符的写出测试
     */
    @Test
    public void write() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(os);
        writer.write("id".getBytes(), false);
        writer.write("姓名".getBytes(), false);
        writer.write("年龄".getBytes(), true);
        writer.newLine();

        writer.write("001".getBytes(), false);
        // 该字段被完美还原了。但是在控制台会看到有换行的迹象
        // 如果使用这段 "朱\r\n强\r\n\"" 显示就没有问题了
        // 这个是因为换行符不正确的情况，要么 windows 的 \r\n 要么是 unix 中的 \n
        // 而这里是 \r 所以本身就是有问题的
        byte[] bytes = "朱\r\n强\r\"".getBytes();
        writer.write(bytes, false);
        writer.write("27".getBytes(), true);
        System.out.println("===== 写出的内容 =====");
        System.out.println(os);
        System.out.println("===== 该字段本来打印到控制台看到的就有问题 =====");
        System.out.println("朱\r\n强\r\"");

        CsvReader reader = DefaultCsvReader.from(new ByteArrayInputStream(os.toByteArray()));
        System.out.println("===== 读取之前写的内容：还原 =====");
        CommonTest.print(reader, CommonTest.utf8);
//        reader.hashNext();
//        reader.next();
//
//        reader.hashNext();
//        List<byte[]> next = reader.next();
//        Assert.assertArrayEquals(next.get(1), bytes);
    }
}