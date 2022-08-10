package com.ld.search;

import com.intellij.ide.actions.searcheverywhere.*;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.util.Processor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class LDSearchEverywhereContributor extends AbstractGotoSEContributor{

    AnActionEvent anActionEvent;
    public LDSearchEverywhereContributor(AnActionEvent anActionEvent) {
        super(anActionEvent);
        this.anActionEvent = anActionEvent;
    }

    @Override
    protected @NotNull FilteringGotoByModel<?> createModel(@NotNull Project project) {
        return null;
    }

    @Override
    public @NotNull
    @Nls String getGroupName() {
        return "翻译";
    }

    @Override
    public int getSortWeight() {
        return 3000;
    }

    @Override
    public void fetchElements(@NotNull String pattern, @NotNull ProgressIndicator progressIndicator, @NotNull Processor<? super Object> consumer) {
        super.fetchElements(pattern, progressIndicator, consumer);
    }

    public static class Factory implements SearchEverywhereContributorFactory<Object> {
        public Factory() {
        }

        public @NotNull SearchEverywhereContributor<Object> createContributor(@NotNull AnActionEvent initEvent) {
            return new LDSearchEverywhereContributor(initEvent);
        }
    }
}
