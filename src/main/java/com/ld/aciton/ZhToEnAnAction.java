package com.ld.aciton;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.ld.RendererInvocationHandler;
import com.ld.SqliteFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ZhToEnAnAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
        if (editor == null) return;
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
//        if (element instanceof PsiField){
//            PsiField psiField = (PsiField)element;
//            String fieldName = psiField.getName();
//            System.out.println(translate(split(fieldName)));
//        }
        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        if (selectedText == null) return;
        String translate = translate(split(selectedText));
        if (translate.isEmpty()) return;

        WriteCommandAction.runWriteCommandAction(project,()-> editor.getDocument().replaceString(selectionModel.getSelectionStart(),selectionModel.getSelectionEnd(),translate));
        System.out.println("11"+translate);
    }


    public String translate(List<String> text){
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

    public List<String> split(String text){
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
}
