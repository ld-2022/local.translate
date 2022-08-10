package com.ld;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupCellRenderer;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Optional;

/**
 * 代码贡献者 绑定渲染
 */
public class MyCompletionContributor extends CompletionContributor {

    Optional<Field> myCellRendererField = ReflexFactory.findField(LookupImpl.class, "myCellRenderer");


    public MyCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                LookupImpl activeLookup = (LookupImpl) LookupManager.getActiveLookup(completionParameters.getEditor());
                if (activeLookup == null || myCellRendererField.isEmpty()){
                    return;
                }
                setCellRenderer(activeLookup,completionParameters);
            }
        });
    }

    /**
     * 设置翻译代理
     * @param lookup
     * @param completionParameters
     */
    public void setCellRenderer(LookupImpl lookup,CompletionParameters completionParameters){
        myCellRendererField.ifPresent(field -> {
            try {
                final Object lookupCellRenderer = field.get(lookup);
                if (lookupCellRenderer instanceof Proxy) {
                    return;
                }
                ListCellRenderer<?> listCellRenderer = RendererInvocationHandler.getListCellRenderer((LookupCellRenderer) lookupCellRenderer, completionParameters);
                lookup.getList().setCellRenderer(listCellRenderer);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }
}
