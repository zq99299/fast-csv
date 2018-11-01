# fast-csv

load csv  file for Mycat

使用 JDK NIO API 实现的 csv 解析工具。

主要功能：

- 无特殊字符列解析，如 xx,23,ddx
- 带特殊字符列解析，如 \"\"\"id\r\n\"\"\",\"姓名\",\"年龄\"\r\n"

解析过程按 byte 解析，不涉及到文件编码，一行的结果是 List<byte[]>

## 使用示例

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

