package com.barley.editor.fileindex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.barley.editor.ui.ListView;
import com.barley.editor.ui.Window;
import com.barley.editor.utils.LogFactory;
import org.slf4j.Logger;

public class FileIndex {
    private static final Logger _log = LogFactory.createLog();
    
    private static class FileIndexItem extends ListView.ListItem {
        private Path _root;
        private Path _path;

        public FileIndexItem(Path root, Path path) {
            _root = root;
            _path = path;
        }

        public String displayString() {
            var path = _root.relativize(_path);
            return path.toString();
        }

        public void onClick() {
            Window.getInstance().setBufferPath(_path);
        }
    }

    public List<FileIndexItem> createFileIndex() {
        var list = new ArrayList<FileIndexItem>();
        try {
            var root = ProjectPaths.getSourceRootPath();
            if (root == null) {
                return list;
            }
            Files.find(root,
                       Integer.MAX_VALUE,
                       (filePath, fileAttr) -> fileAttr.isRegularFile())
            .forEach((path) -> {
                _log.info("Menu path: " + path);
                var relative = root.relativize(path);
                _log.info("Relative menu path: " + relative);
                for (int i = 0; i < relative.getNameCount(); ++i) {
                    var name = relative.getName(i);
                    _log.info("Menu path component: " + name);
                    if (name.toString().substring(0, 1).equals(".")) {
                        _log.info("Invisible component");
                        return;
                    }
                }
                list.add(new FileIndexItem(root, path));
            });
        } catch (IOException e) {
        }
        list.sort((FileIndexItem i1, FileIndexItem i2) -> {
            return i1.displayString().compareTo(i2.displayString());
        });
        return list;
    }

    public static List<FileIndexItem> createFileList() {
        var index = new FileIndex();
        return index.createFileIndex();
    }
}
