package com.ld;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.lookup.impl.LookupCellRenderer;
import com.intellij.codeInsight.template.impl.LiveTemplateLookupElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Key;
import com.ld.analyzer.CoderAnalyzer;
import com.ld.utils.TokenStreamUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.swing.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * 组件渲染类
 */
public class RendererInvocationHandler implements InvocationHandler {

    static Key<Boolean> ROOKIE_READ = Key.create("ROOKIE_READ");
    static Boolean support;
    static Map<String, String> DIC = null;
    static Map<String, String> PIN_DIC = null;
    public static Map<String, String> ZH_EN_DIC = null;
    static Map<String, List<String>> DIC_CACHE = new HashMap<>(1000);
    static Map<String,String> languageMap = SqliteFactory.getInstance().languageMap();
    LookupCellRenderer lookupCellRenderer;
    static final JdbcTemplate jdbcTemplate = SqliteFactory.getInstance().getNoDb();
    Map<String,String> noDicMap = new HashMap<>();


    public RendererInvocationHandler(LookupCellRenderer lookupCellRenderer, CompletionParameters completionParameters) {
        this.lookupCellRenderer = lookupCellRenderer;
    }

    public static ListCellRenderer<?> getListCellRenderer(LookupCellRenderer lookupCellRenderer, CompletionParameters completionParameters) {
        ClassLoader classLoader = ListCellRenderer.class.getClassLoader();
        Class<?>[] classes = {ListCellRenderer.class};
        return (ListCellRenderer<?>) Proxy.newProxyInstance(classLoader, classes, new RendererInvocationHandler(lookupCellRenderer, completionParameters));
    }

    /**
     * 是否支持渲染
     * @return
     */
    private Boolean isSupport() {
        // 已经检查过 不在检查
        if (support != null) {
            return support;
        }
        try {
            Optional<Field> last_computed_presentation = ReflexFactory.findField(Class.forName("com.intellij.codeInsight.lookup.impl.AsyncRendering"), "LAST_COMPUTED_PRESENTATION");
            support = last_computed_presentation.isPresent();
        } catch (ClassNotFoundException e) {
            support = false;
        }
        return support;
    }

    private Key<LookupElementPresentation> findKey() {
        try {
            Optional<Field> last_computed_presentation = ReflexFactory.findField(Class.forName("com.intellij.codeInsight.lookup.impl.AsyncRendering"), "LAST_COMPUTED_PRESENTATION");
            if (last_computed_presentation.isPresent()) {
                return (Key<LookupElementPresentation>) last_computed_presentation.get().get(null);
            }
        } catch (ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        LookupElement lookupElement = (LookupElement) objects[1];
        /**
         * 如果不是getListCellRendererComponent函数
         * 不被支持
         * 已经渲染过
         * 不支持翻译
         */
        List<String> wordList = new ArrayList<>(0);
        if (!"getListCellRendererComponent".equals(method.getName())
                || noDicMap.containsKey(lookupElement.getLookupString())
                || (lookupElement instanceof LiveTemplateLookupElement)
                || lookupElement.getUserData(ROOKIE_READ) != null
                || DIC == null
                || !isSupport()
                || !isTranslate(lookupElement.getLookupString(), wordList)) {
            return method.invoke(lookupCellRenderer, objects);
        }
        LookupElementPresentation lookupElementPresentation = lookupElement.getUserData(Objects.requireNonNull(findKey()));
        // 设置myFrozen 为false 否则无法修改内容
        ReflexFactory.findField(LookupElementPresentation.class, "myFrozen").ifPresent(field -> {
            try {
                field.set(lookupElementPresentation, Boolean.FALSE);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        assert lookupElementPresentation != null;
        // 修改 tailText
        String tailText = lookupElementPresentation.getTailText();
        if (tailText == null || tailText.isBlank()) {
            lookupElementPresentation.setTailText(" 「" + String.join("", wordList) + "」");
        } else {
            lookupElementPresentation.setTailText(tailText + " 「" + String.join("", wordList) + "」");
        }
        // 上面修改了 在改回来 放置有问题
        ReflexFactory.findMethod(LookupElementPresentation.class,"freeze").ifPresent(method1 -> {
            try {
                method1.invoke(lookupElementPresentation);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        // 修改 lookupElement
        lookupElement.putUserData(Objects.requireNonNull(findKey()), lookupElementPresentation);
        lookupElement.putUserData(ROOKIE_READ, true);
        return method.invoke(lookupCellRenderer, objects);
    }

    /**
     * 翻译文本 支持cache
     * @param lookupString
     * @param wordList
     * @return
     */
    private boolean isTranslate(String lookupString, List<String> wordList) {
        if (lookupString.contains("(")){
            lookupString = lookupString.substring(0,lookupString.indexOf("("));
        }
        // 有缓存直接返回
        if (DIC_CACHE.containsKey(lookupString)){
            wordList.addAll(DIC_CACHE.get(lookupString));
            return true;
        }
        // 分词后翻译
        List<String> humpParticiple = null;
        try {
            humpParticiple = TokenStreamUtils.toList(new CoderAnalyzer().tokenStream("xxx",lookupString));
        } catch (Exception e) {
            humpParticiple = List.of(lookupString);
            System.out.println("--->"+lookupString);
            e.printStackTrace();
        }
        for (String s : humpParticiple) {
            // 优先使用当前语言包
            if (languageMap.containsKey(s)){
                wordList.add(languageMap.get(s));
            }else if (DIC.containsKey(s)) {
                wordList.add(DIC.get(s));
            }
        }
        boolean flag = humpParticiple.size() == wordList.size();
        if (flag){
            DIC_CACHE.put(lookupString,wordList);
        }else {
            noDicMap.put(lookupString,"不支持");
            //不支持翻译，异步记录
            List<String> finalHumpParticiple = humpParticiple;
            ApplicationManager.getApplication().executeOnPooledThread(()->{
                synchronized (jdbcTemplate){
                    for (String word : finalHumpParticiple) {
                        try {
                            // 如果这个单词记录过 那么次数加1
                            Map<String, Object> stringObjectMap = jdbcTemplate.queryForMap("select * from no_dic where word = ?", word);
                            int num = Integer.parseInt(stringObjectMap.get("num").toString());
                            jdbcTemplate.update("update no_dic set num = ? where word = ?",num+1,word);
                        }catch (EmptyResultDataAccessException exception){
                            jdbcTemplate.update("insert into no_dic (word,num) values(?,1)",word);
                        }
                    }
                }
            });
        }
        return flag;
    }

    private List<String> participle(String str){
        List<String> result = new ArrayList<>();
        List<String> words = new ArrayList<>();
        if (str.contains(".")){
            words.addAll(List.of(str.split(".")));
        }
        for (String word : words) {
            result.addAll(humpParticiple(word));
        }
        return result;
    }

    /**
     * 驼峰分词
     * @param str
     * @return
     */
    private List<String> humpParticiple(String str) {
        List<String> result = new ArrayList<>();
        List<String> words = List.of(str);
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            int length = word.length();
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < length; j++) {
                String s = String.valueOf(word.charAt(j));
                if (j == 0) {
                    sb.append(s);
                    continue;
                }
                if (s.matches("[A-Z]") && !String.valueOf(word.charAt(j-1)).matches("[A-Z]")) {
                    sb.append(" ");
                }
                sb.append(s);
            }
            result.addAll(List.of(sb.toString().toLowerCase(Locale.ROOT).split(" ")));
        }
        return result;
    }

}
