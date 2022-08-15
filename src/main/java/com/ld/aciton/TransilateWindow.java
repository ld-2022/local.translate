package com.ld.aciton;

import com.ld.RendererInvocationHandler;
import com.ld.analyzer.CoderAnalyzer;
import com.ld.utils.TokenStreamUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransilateWindow {
    private JTextArea text;
    private JTextArea result;
    private JButton submit;
    private JButton copy;
    private JPanel rootPanel;


    public JPanel getRootPanel() {
        text.requestFocusInWindow();
//        initListener();
        return rootPanel;
    }

    public JTextArea getText() {
        return text;
    }

    public JTextArea getResult() {
        return result;
    }

    private void initListener(){
        text.addPropertyChangeListener(evt -> {
            Object newValue = evt.getNewValue();
            System.out.println("输入的内容:"+newValue);
        });
    }


    public static class CopyAction extends AbstractAction {
        private final JTextArea result;
        public CopyAction(JTextArea result) {
            super("复制结果");
            this.result = result;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            //复制内容到剪贴板
            String resultText = result.getText();
            StringSelection stringSelection = new StringSelection(resultText);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        }
    }

    public static class SubmitAction extends AbstractAction{
        private final JTextArea text;
        private final JTextArea result;
        public SubmitAction(JTextArea text, JTextArea result) {
            super("翻译一下");
            this.text = text;
            this.result = result;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String textText = text.getText();
            String resultText = null;
            if (isChinese(textText)){
                resultText = ZH_EN(textText);
            }else {
                try {
                    resultText = EN_ZH(textText);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            result.setText(resultText);
        }
    }


    //判断内容是中文还是英文
    private static boolean isChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        return m.find();
    }

    private static String ZH_EN(String str){
        return TranslateAnAction.translate(TranslateAnAction.split(str));
    }
    private static String EN_ZH(String str) throws IOException {
        List<String> stringList = TokenStreamUtils.toList(new CoderAnalyzer().tokenStream("xxx", str));
        List<String> result = new ArrayList<>();
        for (String s : stringList) {
            // 优先使用当前语言包
            if (RendererInvocationHandler.languageMap.containsKey(s)){
                result.add(RendererInvocationHandler.languageMap.get(s));
            }else if (RendererInvocationHandler.DIC.containsKey(s)) {
                result.add(RendererInvocationHandler.DIC.get(s));
            }
        }
        return StringUtils.join(result, " ");
    }
}
