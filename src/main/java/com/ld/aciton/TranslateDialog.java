package com.ld.aciton;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TranslateDialog extends DialogWrapper{

    TransilateWindow transilateWindow;

    protected TranslateDialog(@Nullable Project project, boolean canBeParent) {
        super(project, canBeParent);
        transilateWindow = new TransilateWindow();
        setTitle("翻译工具【本地版】");
        init();
    }


    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[] {
                new TransilateWindow.SubmitAction(transilateWindow.getText(),transilateWindow.getResult()),
                new TransilateWindow.CopyAction(transilateWindow.getResult()),new AbstractAction("关闭") {
            @Override
            public void actionPerformed(ActionEvent e) {
                close(0);
            }
        }};
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return transilateWindow.getRootPanel();
    }
}
