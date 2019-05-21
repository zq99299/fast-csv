# fast-csv

load csv  file for Mycat

使用 JDK NIO API 实现的 csv 解析工具。

主要功能：

- 无特殊字符列解析，如 xx,23,ddx
- 带特殊字符列解析，如 \"\"\"id\r\n\"\"\",\"姓名\",\"年龄\"\r\n"

解析过程按 byte 解析，不涉及到文件编码，一行的结果是 List<byte[]>

本工具适合用在读取解析 程序生成的 csv 文件的场景
## 使用示例
mavn 仓库连接：https://mvnrepository.com/search?q=mrcode

```xml
<dependency>
  <groupId>cn.mrcode.mycat</groupId>
  <artifactId>fast-csv</artifactId>
  <version>0.1.0</version>
</dependency>

或则

compile 'cn.mrcode.mycat:fast-csv:0.1.0'
```

InputStream File
```java
CsvReader reader = DefaultCsvReader.from(Paths.get(path));
for (List<byte[]> bytes : reader) {
    bytes.stream().map(i -> new String(i, utf8)).forEach(i -> {
        System.out.print("[" + i + "]");
    });
    System.out.println("");
}
```
FileChannel.map 方式
```java
CsvReader reader = new MappedCsvReader(Paths.get(path));
while (reader.hashNext()) {
    reader.next();
}
```


## 致敬
阅读 javacsv2.1 源码，参考实现

## 更新记录

### 2018/11/27 v0.1.0
简单版本的 写出 支持

### 2018/11/02 v0.1.0-SNAPSHOT
从 mycat2 项目中独立出来首版， csv 的解析功能
