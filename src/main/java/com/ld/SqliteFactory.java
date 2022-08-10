package com.ld;

import org.springframework.jdbc.core.JdbcTemplate;
import org.sqlite.SQLiteDataSource;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class SqliteFactory {

    static SqliteFactory sqliteFactory = new SqliteFactory();

    JdbcTemplate baseDb;
    JdbcTemplate noDb;

    static Map<String,String> JAVA_KEYWORD = new HashMap<>(){{
        put("private","私有的");
        put("protected","受保护的");
        put("public","公共的");
        put("default","默认");
        put("abstract","声明抽象");
        put("class","类");
        put("extends","继承");
        put("final","最终值");
        put("implements","实现");
        put("interface","接口");
        put("native","本地");
        put("new","新建");
        put("static","静态");
        put("strictfp","严格");
        put("synchronized","线程同步");
        put("transient","短暂");
        put("volatile","易失");
        put("break","跳出循环");
        put("case","条件");
        put("continue","跳过");
        put("do","运行");
        put("else","否则");
        put("for","循环");
        put("if","如果");
        put("instanceof","实例");
        put("return","返回");
        put("switch","选择");
        put("while","循环");
        put("assert","断言");
        put("catch","捕捉异常");
        put("finally","最后执行");
        put("throw","抛出异常");
        put("throws","声明异常");
        put("try","捕获异常");
        put("import","引入");
        put("package","包");
        put("boolean","布尔");
        put("byte","字节");
        put("char","字符");
        put("double","双精度浮点");
        put("float","单精度浮点");
        put("int","整型");
        put("long","长整型");
        put("short","短整型");
        put("super","父类");
        put("this","本类");
        put("void","空");
        put("goto","跳转");
        put("const","常量");
    }};

    public SqliteFactory() {
        SQLiteDataSource baseDataSource = new SQLiteDataSource();
        baseDataSource.setUrl("jdbc:sqlite:" + Paths.get(Paths.get(System.getProperty("user.home"), "rookie", "dic").toString(), "base_dic.db"));
        baseDb = new JdbcTemplate(baseDataSource);
        SQLiteDataSource noDataSource = new SQLiteDataSource();
        noDataSource.setUrl("jdbc:sqlite:" + Paths.get(Paths.get(System.getProperty("user.home"), "rookie", "dic").toString(), "no_dic.db"));
        noDb = new JdbcTemplate(noDataSource);
    }

    public static SqliteFactory getInstance() {
        return sqliteFactory;
    }

    public JdbcTemplate getBaseDb() {
        return baseDb;
    }

    public JdbcTemplate getNoDb() {
        return noDb;
    }

    public Map<String,String> languageMap(){
        return JAVA_KEYWORD;
    }
}
