# spring-framework-3.2.12
这是spring framework 的3.2.12的maven版本的源码  

将spring framework转为eclipse工程,步骤如下

### 检出spring的源码
`git clone git://github.com/SpringSource/spring-framework.git`

### 编译所有jar
`./gradlew build`

### 安装所有spring-\* jars
`./gradlew install`

### 打入源文件到了你的eclipse  
运行 `./import-into-eclipse.sh` 或者读 `import-into-idea.md` 以更适合的方式  

请详细的内容请看下载源码后的spring framework自动的readme文档,然后手动将eclipse工程转为maven工程  

[spring3.2源码的详解书籍](https://github.com/wulin-challenge/wulin-books-repository/tree/master/java-books/spring/spring%E6%BA%90%E7%A0%81)
