package com.barley.editor.lsp.barley;

import com.barley.editor.lsp.LanguageMode;
import com.barley.editor.lsp.LanguageModeProvider;
import com.barley.editor.text.AttributedString;
import com.barley.editor.text.BufferContext;
import com.barley.editor.utils.LogFactory;
import com.googlecode.lanterna.TextColor;
import org.eclipse.lsp4j.*;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class BarleyLSPClient implements LanguageMode {

    private static final Logger _log = LogFactory.createLog();
    private static Pattern attributes = Pattern.compile("(\\bmodule\\b)|(\\bopt\\b)|(\\bdoc\\b)", Pattern.MULTILINE);
    private static Pattern keyword = Pattern.compile("(\\bwhen\\b)|(\\breceive\\b)|(\\bcase\\b)" +
            "(\\bcase\\b)|(\\bof\\b)|(\\bend\\b)" +
            "(\\band\\b)|(\\bor\\b)|(\\bglobal\\b)" +
            "(\\bnot\\b)|(\\bdef\\b)|(\\bdefguard\\b)");
    private static Pattern comment = Pattern.compile("(//.*)");
    private static Pattern string = Pattern.compile("\\\"([^\\\\\\\"]|(\\\\.))*\\\"", Pattern.MULTILINE);
    private static Pattern operators = Pattern.compile("[+\\-*/()=;!<>:|\\[\\]]");
    private static Pattern digit = Pattern.compile("^\\d*(\\.\\d+)?$");

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
        // TODO: Exclude range intersections so that strings with comments in them dont both match.
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
        int indentation = 0;
        return indentation;
    }

    @Override
    public boolean isIndentationEnd(BufferContext bufferContext, String chracter) {
        return chracter.equals(".");
    }

    @Override
    public TextDocumentItem getTextDocument(BufferContext bufferContext) {
        return new TextDocumentItem(bufferContext.getBuffer().getPath().toFile().toURI().toString(), "barley", 11, bufferContext.getBuffer().getString());
    }

    @Override
    public void applyColouring(BufferContext bufferContext, AttributedString str) {
        var stri = str.toString();
        formatToken(str, stri, comment, TextColor.ANSI.GREEN);
        formatToken(str, stri, digit, TextColor.ANSI.CYAN);
        formatToken(str, stri, keyword, TextColor.ANSI.CYAN);
        formatToken(str, stri, attributes, TextColor.ANSI.GREEN);
        formatToken(str, stri, string, TextColor.ANSI.YELLOW);
        formatToken(str, stri, operators, TextColor.ANSI.YELLOW);
        formatToken(str, stri, digit, TextColor.ANSI.YELLOW);
    }
}
