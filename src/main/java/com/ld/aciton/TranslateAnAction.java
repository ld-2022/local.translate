package com.ld.aciton;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ActiveIcon;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.psi.PsiElement;
import com.intellij.util.ReflectionUtil;
import com.ld.RendererInvocationHandler;
import com.ld.SqliteFactory;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TranslateAnAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
        if (project == null) {
            return;
        }
        JFrame frame = WindowManager.getInstance().getFrame(LangDataKeys.PROJECT.getData(e.getDataContext()));
        if (frame == null) {
            return;
        }
        TranslateDialog dialog = new TranslateDialog(project,true);
        dialog.showAndGet();
    }


    public static String translate(List<String> text){
        StringBuilder words = new StringBuilder();
        for (String s : text) {
            List<Map<String, Object>> mapList = SqliteFactory.getInstance().getBaseDb().queryForList("select * from fanyi where word_zh = ? limit 1", s);
            if (mapList.isEmpty()) {
                continue;
            }
            String word = mapList.get(0).get("word").toString();
            if (words.length() == 0) {
                words.append(word);
            } else {
                words.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1).toLowerCase(Locale.ROOT));
            }
        }
       return words.toString();
    }

    public static List<String> split(String text){
        ArrayList<String> objects = new ArrayList<>();
        while (text.length() > 0){
            int length = text.length();
            int k = 0;
            while (length >= 0){
                if (RendererInvocationHandler.ZH_EN_DIC.containsKey(text.substring(0,length))){
                    k = length;
                    break;
                }else {
                    length --;
                }
            }
            if (k == 0){
                objects.add(text);
                break;
            }
            objects.add(text.substring(0,k));
            text = text.substring(k);
        }
        return objects;
    }

    public static Icon findIcon(String path){
        Class<?> callerClass = ReflectionUtil.getGrandCallerClass();
        return callerClass == null ? null : IconLoader.findIcon(path, callerClass.getClassLoader());
    }
}
