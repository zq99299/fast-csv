package cn.mrcode.mycat.fastcsv;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * <pte>
 * MappedByteBuffer 的数据源输入解析；通过对比，和 普通 io 差不多，
 * 通过与 inputSteam 的对比测试中，发现所有的耗时都在 buf.get() 上，
 * 这里从对外 copy 到 对内，那么把读取到堆内的操作使用批量读取。
 * 本类的写法就改成 和 inptStream 类似的了
 * </pte>
 * @author : zhuqiang
 * @date : 2018/10/21 23:04
 */
public class MappedCsvReader extends CsvReader {
    private boolean inited;
    /** 记录当前映射的位置 */
    private long fileChannelPosition;
    /** 记录当前 映射的大小，每次读取数据（映射）都必须重新计算 */
    private long fileChannelCurrentSize;
    /** 该文件一共需要映射几次，映射次数结束，则标识文件已经读取完毕 */
    private long mappedCount;
    private long currentMappedCount;
    private long totalFileSize;
    private FileChannel input;
    private Path filePath;
    private ByteBuffer sourceBuffer;
    /**
     * 记录当前已经提供给父类 buffer 多少数据了;
     * fileChannelCurrentSize 作为参考边界，
     * mapped（sourceBuffer-堆外）和 父类 buffer (堆内) 进行转换
     */
    private long currentBufferReadedSize;

    public MappedCsvReader(Path filePath) throws IOException {
        this(filePath, DefaultCsvReaderConfig.CONFIG);
    }

    public MappedCsvReader(Path filePath, CsvReaderConfig config) throws IOException {
        this.filePath = filePath;
        this.config = config;
        init();
    }

    @Override
    protected void init() throws IOException {
        if (inited) {
            return;
        }
        // bufferSize 最大不能超过 Integer.MAX_VALUE
        int mappedBufferSize = config.getMappedSize();
        RandomAccessFile source = new RandomAccessFile(filePath.toFile(), "r");
        input = source.getChannel();
        // 要计算文件需要映射几次
        totalFileSize = Files.size(filePath);
        fileChannelCurrentSize = totalFileSize;
        long eachCount = 1;
        if (totalFileSize > mappedBufferSize) {
            eachCount = totalFileSize / mappedBufferSize;
            if (totalFileSize % mappedBufferSize != 0) {
                eachCount += 1;
            }
            fileChannelCurrentSize = mappedBufferSize;
        }
        mappedCount = eachCount;
        //---------------------------------------------------------
        buffer = ByteBuffer.allocate(config.getReadeBufferSize());
        columnBuffer = ByteBuffer.allocate(config.getColumnBufferSize());

        // 解决第一行数据获取的时候无 源数据的问题
        hasMoreData = getMoreData();

    }

    @Override
    protected boolean getMoreData() throws IOException {
        if (inited && sourceTobuffer()) {
            return true;
        }
        if (!inited) {
            inited = true;
        }

        if (mappedCount == currentMappedCount) {
            // 清空buffer，让gc 回收释放资源
            // buffer.clear();  待测试是否不会影响物理文件
            return false;
        }

        // 当是最后一次的时候 （currentMappedCount 从 0 开始）
        // 读取剩余的数据
        if (mappedCount == currentMappedCount + 1) {
            fileChannelCurrentSize = totalFileSize - fileChannelPosition;
        }
        //log.debug("totalFileSize={},mappedCount={},currentMappedCount={},fileChannelPosition={},fileChannelCurrentSize={}",
        //      totalFileSize, mappedCount, currentMappedCount, fileChannelPosition, fileChannelCurrentSize);
        sourceBuffer = input.map(FileChannel.MapMode.READ_ONLY, fileChannelPosition, fileChannelCurrentSize);
        currentMappedCount++;
        fileChannelPosition += fileChannelCurrentSize;

        // 每次重新映射的之后，重置
        currentBufferReadedSize = 0;

        // 每次映射数据后都需要读取一次
        sourceTobuffer();
        return true;
    }

    public boolean sourceTobuffer() {
        if (currentBufferReadedSize < fileChannelCurrentSize) {
            buffer.clear();
            int readeBufferSize = config.getReadeBufferSize();
            int cout = (int) Long.min(readeBufferSize, fileChannelCurrentSize - currentBufferReadedSize);
            sourceBuffer.get(buffer.array(), 0, cout);
            currentBufferReadedSize += cout;
            buffer.position(cout);
            buffer.flip();
            return true;
        }
        return false;
    }
}
