package com.barley.editor.lsp;

import com.barley.editor.text.AttributedString;
import com.barley.editor.text.BufferContext;
import com.barley.editor.utils.LogFactory;
import com.googlecode.lanterna.TextColor;
import org.eclipse.lsp4j.*;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CustomLanguageMode implements LanguageMode {

    private static final Logger _log = LogFactory.createLog();
    private String name;
    private Map<String, String> rules;

    public CustomLanguageMode(String name, Map<String, String> rules) {
        this.name = name;
        this.rules = rules;
    }

    @Override
    public void didInsert(BufferContext bufferContext, int position, String text) {
        var contentChanges = new ArrayList<TextDocumentContentChangeEvent>();
        var line = bufferContext.getTextLayout().getPhysicalLineAt(position);
        var lineIndex = line.getY();
        var charIndex = position - line.getStartPosition();
        _log.info("didInsert " + text + " at " +  position + " (" + lineIndex + ", " + charIndex + ")");
        var range = new Range(new Position(lineIndex, charIndex), new Position(lineIndex, charIndex));
        var insertEvent = new TextDocumentContentChangeEvent(range, 0, text);
        contentChanges.add(insertEvent);
        var params = new DidChangeTextDocumentParams();
        params.setTextDocument(bufferContext.getBuffer().getVersionedTextDocumentID());
        params.setContentChanges(contentChanges);
    }

    @Override
    public void didRemove(BufferContext bufferContext, int startPosition, int endPosition) {
        _log.info("didRemove at " + startPosition + ", " + endPosition);
        var contentChanges = new ArrayList<TextDocumentContentChangeEvent>();
        var startLine = bufferContext.getTextLayout().getPhysicalLineAt(startPosition);
        var startLineIndex = startLine.getY();
        var startIndex = startPosition - startLine.getStartPosition();
        var endLine = bufferContext.getTextLayout().getPhysicalLineAt(endPosition);
        var endLineIndex = endLine.getY();
        var endIndex = endPosition - endLine.getStartPosition();
        var range = new Range(new Position(startLineIndex, startIndex), new Position(endLineIndex, endIndex));
        var removeEvent = new TextDocumentContentChangeEvent(range, endPosition - startPosition, "");
        contentChanges.add(removeEvent);
        var params = new DidChangeTextDocumentParams();
        params.setTextDocument(bufferContext.getBuffer().getVersionedTextDocumentID());
        params.setContentChanges(contentChanges);
    }

    @Override
    public void willSave(BufferContext bufferContext) {
        _log.info("willSave");
        var params = new WillSaveTextDocumentParams();
        params.setTextDocument(bufferContext.getBuffer().getTextDocumentID());
        params.setReason(TextDocumentSaveReason.Manual);
    }

    @Override
    public void didSave(BufferContext bufferContext) {
        _log.info("didSave");
        var params = new DidSaveTextDocumentParams();
        params.setTextDocument(bufferContext.getBuffer().getTextDocumentID());
        params.setText(bufferContext.getBuffer().getString());
    }

    @Override
    public void didClose(BufferContext bufferContext) {
        _log.info("didOpen");
        var params = new DidOpenTextDocumentParams();
        params.setTextDocument(bufferContext.getBuffer().getTextDocument());
    }

    @Override
    public void didOpen(BufferContext bufferContext) {
        _log.info("didOpen");
        var params = new DidOpenTextDocumentParams();
        params.setTextDocument(bufferContext.getBuffer().getTextDocument());
    }

    private void formatToken(AttributedString str, String string, Pattern pattern, TextColor colour) {
        // TODO: Exclude range intersections so that strings with comments in them don't both match.
        // Note the misspelled dont above for this very reason. Spell it right to see what I mean.
        try {
            var matcher = pattern.matcher(string);
            while (matcher.find()) {
                str.format(matcher.start(), matcher.end(), colour, TextColor.ANSI.DEFAULT);
            }
        } catch (Throwable e) {}
    }

    @Override
    public int getIndentationLevel(BufferContext bufferContext) {
        return 0;
    }

    @Override
    public boolean isIndentationEnd(BufferContext bufferContext, String chracter) {
        return true;
    }

    @Override
    public TextDocumentItem getTextDocument(BufferContext bufferContext) {
        return new TextDocumentItem(bufferContext.getBuffer().getPath().toFile().toURI().toString(), "config", 11, bufferContext.getBuffer().getString());
    }

    @Override
    public void applyColouring(BufferContext bufferContext, AttributedString str) {
        String s = str.toString();
        for (Map.Entry<String, String> entry : rules.entrySet()) {
            System.out.println("coloring");
            System.out.println(Pattern.compile(entry.getKey()));
            formatToken(str, s, Pattern.compile(fix(entry.getKey())), formatColor(fix(entry.getValue())));
        }
    }

    private String fix(String key) {
        char cs[] = key.toCharArray();
        String s = "";
        for (char c : cs) {
            s += c;
        }
        return s;
    }

    private TextColor formatColor(String value) {
        switch (value.replaceAll(" ", "")) {
            case "ANSI_CYAN":
                return TextColor.ANSI.CYAN;
            case "ANSI_ORANGE":
            case "ANSI_YELLOW":
                return TextColor.ANSI.YELLOW;
            case "ANSI_MAGENTA":
                return TextColor.ANSI.MAGENTA;
            case "ANSI_BLACK":
                return TextColor.ANSI.BLACK;
            case "ANSI_WHITE":
                return TextColor.ANSI.WHITE;
            case "ANSI_RED":
                return TextColor.ANSI.RED;
            case "ANSI_GREEN":
                return TextColor.ANSI.GREEN;
            case "ANSI_BLUE":
                return TextColor.ANSI.BLUE;
            case "ANSI_DEFAULT":
            default:
                return TextColor.ANSI.DEFAULT;
        }
    }
}
