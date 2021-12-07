package com.barley.editor.lsp;

import java.nio.file.Path;
import java.util.Map;

import com.barley.editor.Editor;
import com.barley.editor.lsp.alight.AlightLSPClient;
import com.barley.editor.lsp.barley.BarleyLSPClient;
import org.eclipse.lsp4j.TextDocumentItem;
import com.barley.editor.lsp.java.JavaLSPClient;
import com.barley.editor.lsp.latex.LatexLSPClient;
import com.barley.editor.text.AttributedString;
import com.barley.editor.text.BufferContext;

public class LanguageModeProvider {
    private static LanguageModeProvider _instance = new LanguageModeProvider();

    public static LanguageModeProvider getInstance() {
        return _instance;
    }

    private boolean endsIn(Path path, String ending) {
        String extension = "";
        String fileName = path.getFileName().toString();

        int i = fileName.lastIndexOf('.');
        if (i >= 0) {
            extension = fileName.substring(i + 1);
        }
        return extension.equals(ending);
    }

    private String ext(Path path) {
        String extension = "";
        String fileName = path.getFileName().toString();

        int i = fileName.lastIndexOf('.');
        if (i >= 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }
    
    private LanguageMode getPlainLanguageMode() {
        return new LanguageMode() {
            @Override
            public void didInsert(BufferContext bufferContext, int position, String str) {
            }

            @Override
            public void didRemove(BufferContext bufferContext, int startPosition, int endPosition) {
            }

            @Override
            public void willSave(BufferContext bufferContext) {
            }

            @Override
            public void didSave(BufferContext bufferContext) {
            }

            @Override
            public void didClose(BufferContext bufferContext) {
            }

            @Override
            public void didOpen(BufferContext bufferContext) {
            }

            @Override
            public int getIndentationLevel(BufferContext bufferContext) {
                return 0;
            }
            
            @Override
            public boolean isIndentationEnd(BufferContext bufferContext, String character) {
                return false;
            }

            @Override
            public TextDocumentItem getTextDocument(BufferContext bufferContext) {
                return null;
            }

            @Override
            public void applyColouring(BufferContext bufferContext, AttributedString str) {
            }
        };
    }
    
    public LanguageMode getLanguageMode(Path path) {
        if (endsIn(path, "java")) {
            return new JavaLSPClient();
        }
        if (endsIn(path, "tex")) {
            return new LatexLSPClient();
        }
        if (endsIn(path, "barley") || endsIn(path, "ams")) {
            return new BarleyLSPClient();
        }
        if (endsIn(path, "alight")) {
            return new AlightLSPClient();
        }
        return getPlainLanguageMode();
    }

    private String fix(String key) {
        char cs[] = key.toCharArray();
        String s = "";
        for (char c : cs) {
            s += c;
        }
        return s;
    }

    private boolean hardCheck(String s1, String s2) {
        if (s1.length() != s2.length()) return false;
        if (s1 == null) return false;
        char[] c1 = s1.toCharArray();
        char[] c2 = s2.toCharArray();
        for (int i = 0; i < c1.length; i++) {
            char current1 = c1[i];
            char current2 = c2[i];
            if (current1 != current2) return false;
        }
        return true;
    }
}
