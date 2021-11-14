package com.barley.editor.fileindex;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ProjectPaths {
    public static Path getSourceRootPath() {
        var path = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        var root = path.getRoot();
        while (path != null && !root.equals(path)) {
            if (path.resolve(".git").toFile().exists() || path.resolve(".fisked").toFile().exists()) {
                return path;
            } else {
                path = path.getParent();
            }
        }
        return null;
    }

    public static Path getProjectRootPath() {
        var path = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        var root = path.getRoot();
        while (path != null && !root.equals(path)) {
            if (path.resolve(".git").toFile().exists() || path.resolve("pom.xml").toFile().exists()) {
                return path;
            } else {
                path = path.getParent();
            }
        }
        return null;
    }
    
    public static boolean hasRepository() {
        var projectPath = getProjectRootPath();
        if (projectPath == null) {
            return false;
        }
        var gitPath = projectPath.resolve(".git");        
        return gitPath.toFile().exists();
    }
}
