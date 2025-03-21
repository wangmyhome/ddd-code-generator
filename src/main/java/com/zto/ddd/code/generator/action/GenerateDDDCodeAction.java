package com.zto.ddd.code.generator.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import com.zto.ddd.code.generator.service.CodeGeneratorService;
import org.jetbrains.annotations.NotNull;

public class GenerateDDDCodeAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null || !psiFile.getName().endsWith("Mapper.java")) {
            Messages.showErrorDialog(project, "请选择一个Mapper文件", "无效的选择");
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "生成DDD代码", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    indicator.setText("正在分析Mapper文件...");
                    indicator.setFraction(0.1);
                    
                    CodeGeneratorService service = CodeGeneratorService.getInstance(project);
                    String baseName = service.getBaseName(psiFile);
                    
                    indicator.setText("正在生成实体类...");
                    indicator.setFraction(0.2);
                    service.generateEntity(baseName, psiFile);
                    
                    indicator.setText("正在生成DTO类...");
                    indicator.setFraction(0.3);
                    service.generateDTO(baseName, psiFile);
                    
                    indicator.setText("正在生成Gateway接口...");
                    indicator.setFraction(0.4);
                    service.generateGateway(baseName, psiFile);
                    
                    indicator.setText("正在生成Gateway实现类...");
                    indicator.setFraction(0.6);
                    service.generateGatewayImpl(baseName, psiFile);
                    
                    indicator.setText("正在生成服务接口...");
                    indicator.setFraction(0.8);
                    service.generateService(baseName, psiFile);
                    
                    indicator.setText("正在生成服务实现类...");
                    indicator.setFraction(0.9);
                    service.generateServiceImpl(baseName, psiFile);
                    
                    indicator.setText("完成");
                    indicator.setFraction(1.0);
                    
                    Messages.showInfoMessage(project, "DDD代码生成成功！", "成功");
                } catch (Exception ex) {
                    Messages.showErrorDialog(project, "生成代码失败: " + ex.getMessage(), "错误");
                }
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        e.getPresentation().setEnabled(psiFile != null && psiFile.getName().endsWith("Mapper.java"));
    }
} 