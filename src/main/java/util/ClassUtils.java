package util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public enum ClassUtils {

    ;

    public static List<File> getClassFiles(File file) {
        if (file.isDirectory()) {
            List<File> results = new ArrayList<>();
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    results.addAll(getClassFiles(f));
                }
            }
            return results;
        } else {
            return file.getName().endsWith(".class") ? Collections.singletonList(file) : Collections.emptyList();
        }
    }

    public static List<Class<?>> getClasses(List<File> classFiles) {
        String separator = Pattern.quote(System.getProperty("file.separator", "/"));
        List<Class<?>> results = new ArrayList<>();
        for (File file : classFiles) {
            String classFileName = file.getAbsolutePath();
            classFileName = classFileName.substring(0, classFileName.lastIndexOf(".class"));
            List<String> tokens = Arrays.asList(classFileName.split(separator));
            for (int i = 0; i < tokens.size(); ++i) {
                String className = String.join(".", tokens.subList(i, tokens.size()));
                try {
                    Class<?> cls = Class.forName(className);
                    results.add(cls);
                } catch (ClassNotFoundException e) {
                    // Silently fail
                }
            }
        }
        return results;
    }

}
