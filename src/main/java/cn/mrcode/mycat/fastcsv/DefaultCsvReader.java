package cn.mrcode.mycat.fastcsv;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;

/**
 * 普通 io 的输入源支持，支持更多的输入方式
 * @author : zhuqiang
 * @date : 2018/10/21 22:49
 */
public class DefaultCsvReader extends CsvReader implements AutoCloseable {
    private InputStream input;
    private boolean inited;

    private DefaultCsvReader(InputStream input) {
        this(input, DefaultCsvReaderConfig.CONFIG);
    }

    private DefaultCsvReader(InputStream input, CsvReaderConfig config) {
        this.input = input;
        this.config = config;
    }

    /**
     * 从输入流读取
     * @param input
     * @return
     * @throws IOException
     */
    public static CsvReader from(InputStream input) throws IOException {
        return from(input, DefaultCsvReaderConfig.CONFIG);
    }

    public static CsvReader from(InputStream input, CsvReaderConfig config) throws IOException {
        DefaultCsvReader defaultCsvReader = new DefaultCsvReader(input, config);
        defaultCsvReader.init();
        return defaultCsvReader;
    }

    /**
     * 从文件读取
     * @param filePath
     * @return
     * @throws IOException
     */
    public static CsvReader from(Path filePath) throws IOException {
        FileInputStream input = new FileInputStream(filePath.toString());
        return from(input, DefaultCsvReaderConfig.CONFIG);
    }

    public static CsvReader from(Path filePath, CsvReaderConfig config) throws IOException {
        FileInputStream input = new FileInputStream(filePath.toString());
        return from(input, config);
    }

    /**
     * 从字符串解析，一般多用于测试
     * @param csvStr
     * @return
     * @throws IOException
     */
    public static CsvReader fromString(String csvStr) throws IOException {
        return fromString(csvStr, DefaultCsvReaderConfig.CONFIG);
    }

    public static CsvReader fromString(String csvStr, CsvReaderConfig config) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(csvStr.getBytes(config.getCharset()));
        DefaultCsvReader defaultCsvReader = new DefaultCsvReader(input, config);
        defaultCsvReader.init();
        return defaultCsvReader;
    }

    @Override
    protected void init() throws IOException {
        if (inited) {
            return;
        }
        int bufferSize = config.getReadeBufferSize();
        buffer = ByteBuffer.allocate(bufferSize);
        columnBuffer = ByteBuffer.allocate(config.getColumnBufferSize());
        // 解决第一行数据获取的时候无 源数据的问题
        hasMoreData = getMoreData();
        inited = true;
    }

    @Override
    protected boolean getMoreData() throws IOException {
        int read = input.read(buffer.array());
        if (read == -1) {
            return false;
        } else {
            buffer.position(read);
            buffer.flip();
            return true;
        }
    }

    @Override
    public void close() throws Exception {
        if (input != null) {
            input.close();
        }
    }
}
