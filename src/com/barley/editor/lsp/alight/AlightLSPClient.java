package com.barley.editor.lsp.alight;

import com.barley.editor.lsp.LanguageMode;
import com.barley.editor.text.AttributedString;
import com.barley.editor.text.BufferContext;
import com.googlecode.lanterna.TextColor;
import org.eclipse.lsp4j.TextDocumentItem;

import java.util.regex.Pattern;

public class AlightLSPClient implements LanguageMode {

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

    private static Pattern _operators = Pattern.compile("(\\blet\\b)|(\\bdef\\b)|(\\bfor\\b)|(\\bif\\b)|(\\belse\\b)", Pattern.MULTILINE);
    private static Pattern _bracketKeywordPattern = Pattern.compile("\\{|\\}|\\&", Pattern.MULTILINE);
    private static Pattern _alightKeyword = Pattern.compile("\\%(\\w+|_)*");

    private void formatToken(AttributedString str, String string, Pattern pattern, TextColor colour) {
        var matcher = pattern.matcher(string);
        while (matcher.find()) {
            str.format(matcher.start(), matcher.end(), colour, TextColor.ANSI.DEFAULT);
        }
    }

    @Override
    public void applyColouring(BufferContext bufferContext, AttributedString str) {
        var string = str.toString();
        formatToken(str, string, _alightKeyword, TextColor.ANSI.GREEN);
        formatToken(str, string, _bracketKeywordPattern, TextColor.ANSI.GREEN);
        formatToken(str, string, _operators, TextColor.ANSI.CYAN);
    }

}
