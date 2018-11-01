package cn.mrcode.mycat.fastcsv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;

import static cn.mrcode.mycat.fastcsv.CommonTest.*;

/**
 * ${desc}
 * @author zhuqiang
 * @version 1.0.1 2018/10/22 19:04
 * @date 2018/10/22 19:04
 * @since 1.0
 */
public class MappedCsvReaderTest {
    @Test
    public void test1() throws IOException {
        // 超级复杂的一列中是一个 html 网页，测试通过
        String path = workPath + "product_info.csv";
        CsvReader from = new MappedCsvReader(Paths.get(path));
        print(from, utf8);
    }

    @Test
    public void test2() {
        String random = RandomStringUtils.random(RandomUtils.nextInt(10, 20), true, true);
        System.out.println(random);
        System.out.println(RandomStringUtils.randomAscii(10));
        System.out.println(RandomStringUtils.randomAlphabetic(10));
        System.out.println(RandomStringUtils.randomAlphanumeric(10));
        System.out.println(RandomStringUtils.randomNumeric(10));
    }

    // 模拟超大文件，无特殊符号
    @Test
    public void test3() {
        String path = workPath + "simpleRandomBigData.csv";
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path))) {
            for (int i = 0; i < 2000_0000; i++) {
                for (int j = 0; j < 10; j++) {
                    String random = RandomStringUtils.random(RandomUtils.nextInt(10, 50), true, true);
                    writer.append(random);
                    if (j != 9) {
                        writer.append(",");
                    }
                }
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析4.76G查看用时，Integer.MAX_VALUE 的时候 耗时52秒
     * @throws IOException
     */
    @Test
    public void test4() throws IOException {
        String path = workPath + "simpleRandomBigData.csv";
        Instant start = Instant.now();
        CsvReaderConfig config = new CsvReaderConfig();
        config.setReadeBufferSize(1024 * 1024 * 1);
//        config.setMappedSize(1024 * 1024 * 400);
        CsvReader reader = new MappedCsvReader(Paths.get(path), config);
        while (reader.hashNext()) {
            reader.next();
        }
        Duration between = Duration.between(start, Instant.now());
        System.out.println(between.toMillis());
        System.out.println(between.getSeconds());
    }

    /**
     * inputStream 解析4.76G查看用时
     * 这里的读缓存意义不是很大了，大体上的压力在 inputStream 本身读取耗时上
     * 耗时58秒左右
     * @throws IOException
     */
    @Test
    public void test5() throws IOException {
        String path = workPath + "simpleRandomBigData.csv";
        Instant start = Instant.now();
        CsvReaderConfig config = new CsvReaderConfig();
        config.setReadeBufferSize(1024 * 1024 * 1);
        CsvReader reader = DefaultCsvReader.from(Paths.get(path), config);
        while (reader.hashNext()) {
            reader.next();
        }
        Duration between = Duration.between(start, Instant.now());
        System.out.println(between.toMillis());
        System.out.println(between.getSeconds());
    }

    /**
     * 使用
     * org.apache.commons:commons-csv:1.6
     * 测试同一个大文件的解析速度
     */
    @Test
    public void test6() throws IOException {
        String path = workPath + "simpleRandomBigData.csv";
        Instant start = Instant.now();
        Reader in = new FileReader(path);
        Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
        Iterator<CSVRecord> iterator = records.iterator();
        while (iterator.hasNext()) {
            iterator.next();
        }
        Duration between = Duration.between(start, Instant.now());
        System.out.println(between.toMillis());
        System.out.println(between.getSeconds());
    }
}