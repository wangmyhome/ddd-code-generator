package com.zto.ddd.code.generator.service;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.zto.ddd.code.generator.config.GeneratorSettings;
import com.zto.ddd.code.generator.template.SimpleTemplateEngine;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CodeGeneratorService {
    private final Project project;
    private final GeneratorSettings settings;

    public CodeGeneratorService(Project project) {
        this.project = project;
        this.settings = GeneratorSettings.getInstance();
    }

    public static CodeGeneratorService getInstance(Project project) {
        return project.getService(CodeGeneratorService.class);
    }

    public void generateCode(PsiFile mapperFile) {
        PsiClass mapperClass = getMapperClass(mapperFile);
        if (mapperClass == null) {
            throw new IllegalStateException("Selected file is not a valid Mapper class");
        }

        String baseName = getBaseName(mapperClass.getName());
        generateEntity(baseName, mapperFile);
        generateDTO(baseName, mapperFile);
        generateGateway(baseName, mapperFile);
        generateGatewayImpl(baseName, mapperFile);
        generateService(baseName, mapperFile);
        generateServiceImpl(baseName, mapperFile);
    }
    
    public String getBaseName(PsiFile mapperFile) {
        PsiClass mapperClass = getMapperClass(mapperFile);
        if (mapperClass == null) {
            throw new IllegalStateException("Selected file is not a valid Mapper class");
        }
        return getBaseName(mapperClass.getName());
    }

    private PsiClass getMapperClass(PsiFile file) {
        if (!(file instanceof PsiJavaFile)) {
            return null;
        }
        PsiJavaFile javaFile = (PsiJavaFile) file;
        PsiClass[] classes = javaFile.getClasses();
        return classes.length > 0 ? classes[0] : null;
    }

    public String getBaseName(String mapperName) {
        return mapperName.substring(0, mapperName.length() - 6); // Remove "Mapper" suffix
    }

    public void generateEntity(String baseName, PsiFile mapperFile) {
        PsiClass mapperClass = getMapperClass(mapperFile);
        if (mapperClass == null) {
            throw new IllegalStateException("Selected file is not a valid Mapper class");
        }
        Map<String, Object> context = createContext(baseName, mapperClass);
        generateFile("templates/Entity.java.vm", getEntityPath(baseName), context);
    }

    public void generateDTO(String baseName, PsiFile mapperFile) {
        PsiClass mapperClass = getMapperClass(mapperFile);
        if (mapperClass == null) {
            throw new IllegalStateException("Selected file is not a valid Mapper class");
        }
        Map<String, Object> context = createContext(baseName, mapperClass);
        generateFile("templates/DTO.java.vm", getDTOPath(baseName), context);
    }

    public void generateGateway(String baseName, PsiFile mapperFile) {
        PsiClass mapperClass = getMapperClass(mapperFile);
        if (mapperClass == null) {
            throw new IllegalStateException("Selected file is not a valid Mapper class");
        }
        Map<String, Object> context = createContext(baseName, mapperClass);
        generateFile("templates/Gateway.java.vm", getGatewayPath(baseName), context);
    }

    public void generateGatewayImpl(String baseName, PsiFile mapperFile) {
        PsiClass mapperClass = getMapperClass(mapperFile);
        if (mapperClass == null) {
            throw new IllegalStateException("Selected file is not a valid Mapper class");
        }
        Map<String, Object> context = createContext(baseName, mapperClass);
        generateFile("templates/GatewayImpl.java.vm", getGatewayImplPath(baseName), context);
    }

    public void generateService(String baseName, PsiFile mapperFile) {
        PsiClass mapperClass = getMapperClass(mapperFile);
        if (mapperClass == null) {
            throw new IllegalStateException("Selected file is not a valid Mapper class");
        }
        Map<String, Object> context = createContext(baseName, mapperClass);
        generateFile("templates/Service.java.vm", getServicePath(baseName), context);
    }

    public void generateServiceImpl(String baseName, PsiFile mapperFile) {
        PsiClass mapperClass = getMapperClass(mapperFile);
        if (mapperClass == null) {
            throw new IllegalStateException("Selected file is not a valid Mapper class");
        }
        Map<String, Object> context = createContext(baseName, mapperClass);
        generateFile("templates/ServiceImpl.java.vm", getServiceImplPath(baseName), context);
    }

    private Map<String, Object> createContext(String baseName, PsiClass mapperClass) {
        Map<String, Object> context = new HashMap<>();
        context.put("baseName", baseName);
        context.put("packageName", getPackageName());
        context.put("ormPackage", settings.getOrmPackage());
        context.put("domainPackage", settings.getDomainPackage());
        context.put("infrastructurePackage", settings.getInfrastructurePackage());
        context.put("applicationPackage", settings.getApplicationPackage());
        context.put("clientPackage", settings.getClientPackage());
        context.put("methods", mapperClass.getMethods());
        
        // 添加首字母小写的变量
        String baseNameCamelCase = baseName;
        if (baseNameCamelCase.length() > 0) {
            baseNameCamelCase = Character.toLowerCase(baseNameCamelCase.charAt(0)) + baseNameCamelCase.substring(1);
        }
        context.put("baseNameCamelCase", baseNameCamelCase);
        
        return context;
    }

    private String getPackageName() {
        return settings.getBasePackage();
    }

    private void generateFile(String templatePath, String targetPath, Map<String, Object> context) {
        try {
            // Load and process template
            String template = SimpleTemplateEngine.loadTemplate(templatePath, getClass().getClassLoader());
            String content = SimpleTemplateEngine.process(template, context);
            
            // Create target directory
            String basePath = project.getBasePath();
            if (basePath == null) {
                throw new IllegalStateException("Cannot get project base path");
            }
            
            File targetDir = new File(basePath + "/src/main/java/" + getPackageName().replace('.', '/') + 
                              "/" + targetPath.substring(0, targetPath.lastIndexOf('/')));
            if (!targetDir.exists()) {
                if (!targetDir.mkdirs()) {
                    throw new IOException("Failed to create directory: " + targetDir.getAbsolutePath());
                }
            }
            
            // 写入文件
            File targetFile = new File(basePath + "/src/main/java/" + getPackageName().replace('.', '/') + 
                               "/" + targetPath);
            try (FileWriter fileWriter = new FileWriter(targetFile)) {
                fileWriter.write(content);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate file: " + e.getMessage(), e);
        }
    }

    private String getEntityPath(String baseName) {
        return String.format("%s/%s/%s.java", 
            settings.getOrmPackage(), 
            baseName.toLowerCase(), 
            baseName);
    }

    private String getDTOPath(String baseName) {
        return String.format("%s/%s/%sDTO.java", 
            settings.getDomainPackage(), 
            baseName.toLowerCase(), 
            baseName);
    }

    private String getGatewayPath(String baseName) {
        return String.format("%s/%s/%sGateway.java", 
            settings.getDomainPackage(), 
            baseName.toLowerCase(), 
            baseName);
    }

    private String getGatewayImplPath(String baseName) {
        return String.format("%s/%s/%sGatewayImpl.java", 
            settings.getInfrastructurePackage(), 
            baseName.toLowerCase(), 
            baseName);
    }

    private String getServicePath(String baseName) {
        return String.format("%s/%s/%sI.java", 
            settings.getApplicationPackage(), 
            baseName.toLowerCase(), 
            baseName);
    }

    private String getServiceImplPath(String baseName) {
        return String.format("%s/%s/%sImpl.java", 
            settings.getApplicationPackage(), 
            baseName.toLowerCase(), 
            baseName);
    }
} 