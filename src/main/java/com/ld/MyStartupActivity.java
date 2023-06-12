package com.ld;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyStartupActivity implements StartupActivity {

    public static String ROOKIE_PATH;
    public static String ROOKIE_DIC_PATH;
    public static final String BASE_DIC_NAME = "base_dic.db";
    public static final String NO_DIC_NAME = "no_dic.db";

    public static Boolean isRun = Boolean.FALSE;

    public MyStartupActivity() throws IOException {
        ROOKIE_PATH = Paths.get(System.getProperty("user.home"), "rookie").toString();
        ROOKIE_DIC_PATH = Paths.get(ROOKIE_PATH,"dic").toString();
        // 如果目录不存在 新建
        if (!Files.exists(Paths.get(ROOKIE_DIC_PATH))){
            Files.createDirectories(Paths.get(ROOKIE_DIC_PATH));
        }
    }

    @Override
    public void runActivity(@NotNull Project project) {
        if (MyStartupActivity.isRun) return;
        MyStartupActivity.isRun = Boolean.TRUE;
        ProgressManager.getInstance().run(new StartTask(project,"Rookie",true));
    }

    private static class StartTask extends Task.Backgroundable{

        Logger logger = Logger.getInstance(StartTask.class);

        public StartTask(@Nullable Project project, @NotNull String title) {
            super(project, title);
        }

        public StartTask(@Nullable Project project, @NotNull String title, boolean canBeCancelled) {
            super(project, title, canBeCancelled);
        }

        @Override
        public void run(@NotNull ProgressIndicator progressIndicator) {
            progressIndicator.setIndeterminate(false);
            // 基础词典
            progressIndicator.setText("检查词库是否初始化");
            initDic();
            // 加载词典到内存
            progressIndicator.setText("开始加载词典");
            JdbcTemplate baseDb = SqliteFactory.getInstance().getBaseDb();
            String num = baseDb.queryForMap("SELECT COUNT(word) AS num from fanyi where word_zh NOT NULL ").get("num").toString();
            progressIndicator.setText(String.format("词典数量:%s",num));
            int i1 = Integer.parseInt(num) / 100000;
            RendererInvocationHandler.DIC = new HashMap<>(400000);
            RendererInvocationHandler.PIN_DIC = new HashMap<>(400000);
            RendererInvocationHandler.ZH_EN_DIC = new HashMap<>(400000);
            for (int i = 0; i < i1; i++) {
                progressIndicator.setFraction((float)i/i1);
                int start = i * 100000;
                try {
                    List<Map<String, Object>> mapList = baseDb.queryForList("select * from fanyi where word_zh NOT NULL  LIMIT ?,100000", start);
                    for (Map<String, Object> stringObjectMap : mapList) {
                        String word = stringObjectMap.get("word").toString();
                        String word_zh = stringObjectMap.get("word_zh").toString();
                        //英文->中文
                        RendererInvocationHandler.DIC.put(word, word_zh);
                        //中文->英文
                        if (!RendererInvocationHandler.ZH_EN_DIC.containsKey(word_zh)){
                            RendererInvocationHandler.ZH_EN_DIC.put(word_zh,word);
                        }
                    }
                    mapList.clear();
                } catch (Exception exception) {
                    logger.error("加载词典到内存失败:",exception);
                }
            }
        }

        private void initDic() {
            if (!Files.exists(Paths.get(ROOKIE_DIC_PATH, BASE_DIC_NAME)) || !Files.exists(Paths.get(ROOKIE_DIC_PATH, NO_DIC_NAME))){
                if (!Files.exists(Paths.get(ROOKIE_DIC_PATH, BASE_DIC_NAME))) {
                    try (InputStream resourceAsStream = MyStartupActivity.class.getResourceAsStream("/dic/base_dic.db")){
                        assert resourceAsStream != null;
                        Files.write(Paths.get(ROOKIE_DIC_PATH, BASE_DIC_NAME), resourceAsStream.readAllBytes(), StandardOpenOption.CREATE);
                    }catch (IOException e) {
                        logger.error("写入基础词典失败:", e);
                    }
                }
                // 缺陷词典
                if (!Files.exists(Paths.get(ROOKIE_DIC_PATH, NO_DIC_NAME))) {
                    try (InputStream resourceAsStream = MyStartupActivity.class.getResourceAsStream("/dic/no_dic.db")){
                        assert resourceAsStream != null;
                        Files.write(Paths.get(ROOKIE_DIC_PATH, NO_DIC_NAME), resourceAsStream.readAllBytes(), StandardOpenOption.CREATE);

                    } catch (IOException e) {
                        logger.error("写入记忆词典失败:", e);
                    }
                }
            }
        }
    }
}
