package cn.mrcode.mycat.fastcsv;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static cn.mrcode.mycat.fastcsv.CommonTest.print;
import static cn.mrcode.mycat.fastcsv.CommonTest.utf8;
import static cn.mrcode.mycat.fastcsv.CommonTest.workPath;

/**
 * 普通 io 和 Mapped 方式只是输入源不同，处理逻辑相同，所以这里的测试大部分也适用于 MappedCsvReader
 * @author zhuqiang
 * @version 1.0.1 2018/10/22 19:04
 * @date 2018/10/22 19:04
 * @since 1.0
 */
public class DefaultCsvReaderTest {

    /**
     * 无特殊符号的分析
     */
    @Test
    public void test1() throws IOException {
        CsvReader reader = DefaultCsvReader.fromString(
                "id,姓名,年龄\r\n" +
                        "1,朱强,20\r\n" +
                        "2,zhuqiang,19");
        print(reader, utf8);
    }

    /**
     * 混合换行符 + 无边界和有边界混合 解析
     * @throws IOException
     */
    @Test
    public void test2() throws IOException {
        CsvReader reader = DefaultCsvReader.fromString(
                "id,姓名,年龄\r\n" +
                        "1,\"朱强\",20\r\n" +
                        "2,zhuqiang,19\r");
        reader.hashNext();
        reader.hashNext();
        List<byte[]> next = reader.next();
        String value = new String(next.get(1), utf8);
        Assert.assertEquals(value, "朱强");
    }

    @Test
    public void test3() throws IOException {
        CsvReader reader = DefaultCsvReader.fromString(
                "id,姓名,年龄\r\n" +
                        "1,\"朱强\",20\r\n" +
                        "2,zhuqiang,19\r");
        print(reader, utf8);
    }

    /** 列数不一致解析 */
    @Test
    public void test4() throws IOException {
        CsvReader reader = DefaultCsvReader.fromString(
                ",id,姓名,年龄\r\n" +
                        "1,\"朱强\",20\r\n" +
                        "2,zhuqiang,19");
        reader.hashNext();
        // [][id][姓名][年龄]
        List<byte[]> next = reader.next();
        Assert.assertEquals(next.get(0).length, 0);
    }

    @Test
    public void test5() throws IOException {
        CsvReader reader = DefaultCsvReader.fromString(
                "id,姓名,年龄\r\n" +
                        "2,zhuqiang,19,");
        reader.hashNext();
        reader.hashNext();
        // [2][zhuqiang][19]
        List<byte[]> next = reader.next();
        // 因为读取完逗号之后，后面没有可读数据了
        Assert.assertEquals(next.size(), 3);
    }

    /** 当无文本边界的时候，最后一列没有值的时候，将被忽略 */
    @Test
    public void test6() throws IOException {
        CsvReader reader = DefaultCsvReader.fromString(
                "id,姓名,年龄\r\n" +
                        "2,zhuqiang,19,\r\n");
        reader.hashNext();
        reader.hashNext();
        // [2][zhuqiang][19]
        List<byte[]> next = reader.next();
        // 因为读取完逗号之后，后面没有可读数据了
        Assert.assertEquals(next.size(), 3);
    }

    /**
     * 列扩容测试
     * @throws IOException
     */
    @Test
    public void test7() throws IOException {
        CsvReaderConfig csvReaderConfig = new CsvReaderConfig();
        csvReaderConfig.setReadeBufferSize(5);
        csvReaderConfig.setColumnBufferSize(2);
        CsvReader reader = DefaultCsvReader.fromString(
                "id,姓名,年龄\r\n" +
                        "2,zhuqiang,19,",
                csvReaderConfig);
        print(reader, utf8);
    }

    /**
     * 特殊字符解析
     */
    @Test
    public void test8() throws IOException {
        // csv 协议值："""id""","姓名","年龄"
        // 实际值 "id",姓名,年龄
        CsvReader reader = DefaultCsvReader.fromString(
                "\"\"\"id\"\"\",\"姓名\",\"年龄\"\r\n" +
                        "\"2\",\"zhuqiang\",\"19\"");
        print(reader, utf8);
    }

    /**
     * 特殊字符解析
     */
    @Test
    public void test9() throws IOException {
        // csv 协议值："""id""","姓名","年龄"
        // 实际值 "id",姓名,年龄
        CsvReader reader = DefaultCsvReader.fromString(
                "\"\"\"id\r\n\"\"\",\"姓名\",\"年龄\"\r\n" +
                        "\"2\",\"zhuqiang\",\"19\"\r\n");
        print(reader, utf8);
    }

    @Test
    public void test10() throws IOException {
        // 超级复杂的一列中是一个 html 网页，测试通过
        String path = workPath + "product_info.csv";
        CsvReader from = DefaultCsvReader.from(Paths.get(path));
        print(from, utf8);
    }

    @Test
    public void test11() throws IOException {
        // 文本边界和无文本边界混合模式："2",zhuqiang,19
        CsvReader reader = DefaultCsvReader.fromString(
                "id,姓名,年龄\r\n" +
                        "\"2\",zhuqiang,19");
        print(reader, utf8);
    }

    @Test
    public void test12() throws IOException {
        // csv 协议值："2"",",zhuqiang,19
        // 实际值 [2",] [zhuqiang] [19]
        CsvReader reader = DefaultCsvReader.fromString(
                "id,姓名,年龄\r\n" +
                        "\"2\"\",\", zhuqiang, 19");
        print(reader, utf8);
    }

    /**
     * 大文件
     * @throws IOException
     */
    @Test
    public void test13() throws IOException {
        String path = workPath + "simpleRandomBigData.csv";
//        String path = workPath + "product_info.csv";
        Instant start = Instant.now();
        CsvReaderConfig config = new CsvReaderConfig();
        config.setReadeBufferSize(1024 * 1024 * 1);
        CsvReader reader = DefaultCsvReader.from(Paths.get(path), config);
        for (List<byte[]> bytes : reader) {
//            bytes.stream().map(i -> new String(i, utf8)).forEach(i -> {
//                System.out.print("[" + i + "]");
//            });
//            System.out.println("");
        }
        Duration between = Duration.between(start, Instant.now());
        System.out.println(between.toMillis());
        System.out.println(between.getSeconds());
    }
}