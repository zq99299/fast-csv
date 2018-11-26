package cn.mrcode.mycat.fastcsv.wite;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import cn.mrcode.mycat.fastcsv.CsvReader;
import cn.mrcode.mycat.fastcsv.DefaultCsvReader;

/**
 * ${todo}
 *
 * @author : zhuqiang
 * @date : 2018/11/26 23:22
 */
public class CsvWriterTest {

    @Test
    public void write() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(os);
        writer.write("id".getBytes(), false);
        writer.write("姓名".getBytes(), false);
        writer.write("年龄".getBytes(), true);
        writer.newLine();

        writer.write("001".getBytes(), false);
        // 该字段被完美还原了。但是
        byte[] bytes = "朱\r\n强\r\"".getBytes();
        writer.write(bytes, false);
        writer.write("27".getBytes(), true);
        System.out.println(os);

        CsvReader reader = DefaultCsvReader.from(new ByteArrayInputStream(os.toByteArray()));
//        CommonTest.print(reader, CommonTest.utf8);
        reader.hashNext();
        reader.next();

        reader.hashNext();
        List<byte[]> next = reader.next();
        Assert.assertArrayEquals(next.get(1), bytes);
    }
}