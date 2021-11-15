package com.barley.editor.lsp.java;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.barley.editor.fileindex.ProjectPaths;
import com.barley.editor.lsp.LanguageMode;
import com.barley.editor.text.AttributedString;
import com.barley.editor.text.BufferContext;
import com.barley.editor.ui.Window;
import com.barley.editor.utils.LogFactory;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLensCapabilities;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.ColorInformation;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentColorParams;
import org.eclipse.lsp4j.ExecuteCommandCapabilities;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferencesCapabilities;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.SemanticHighlightingCapabilities;
import org.eclipse.lsp4j.SemanticHighlightingParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentSaveReason;
import org.eclipse.lsp4j.UnregistrationParams;
import org.eclipse.lsp4j.WillSaveTextDocumentParams;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.googlecode.lanterna.TextColor;

public class JavaLSPClient extends Thread implements LanguageMode {

    private static final Logger _log = LogFactory.createLog();
    private static Pattern attributes = Pattern.compile("(\\bmodule\\b)|(\\bopt\\b)|(\\bdoc\\b)", Pattern.MULTILINE);
    private static Pattern keyword = Pattern.compile(
            "(\\bprivate\\b)|(\\bprotected\\b)|(\\bpublic\\b)|(\\bstatic\\b)|(\\babstract\\b)|" +
                    "(\\bvoid\\b)|(\\bbyte\\b)|(\\bchar\\b)|(\\bboolean\\b)|(\\bshort\\b)|(\\bint\\b)|(\\blong\\b)|(\\bfloat\\b)|" +
                    "(\\bdouble\\b)|(\\bimplements\\b)|(\\bextends\\b)|(\\bclass\\b)|(\\benum\\b)|(\\bfinal\\b)|" +
                    "(\\btry\\b)|(\\bcatch\\b)|(\\bthrows\\b)|(\\bthrow\\b)|(\\brecord\\b)|(\\bnew\\b)|(\\breturn\\b)|" +
                    "(\\bif\\b)|(\\belse\\b)|(\\bfor\\b)|(\\bwhile\\b)|(\\bdo\\b)|(\\bimport\\b)|(\\bpackage\\b)|" +
                    "(\\bcase\\b)|(\\bbreak\\b)|(\\bthis\\b)|(\\bsynchronized\\b)|(\\bvar\\b)|(\\bdefault\\b)",
            Pattern.MULTILINE);;
    private static Pattern comment = Pattern.compile("(/\\*([^*]|[\\n]|(\\*+([^*/]|[\\n])))*\\*+/)|(//.*)", Pattern.MULTILINE);
    private static Pattern string = Pattern.compile("\\\"([^\\\\\\\"]|(\\\\.))*\\\"", Pattern.MULTILINE);
    private static Pattern operators = Pattern.compile("[\\+\\-*/()=;!<>:|\\[\\]]");
    private static Pattern digit = Pattern.compile("^\\d*(\\.\\d+)?$");
    private static Pattern var = Pattern.compile("([a-z]|[A-Z]|[0-9]|_)*");


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
        formatToken(str, stri, var, TextColor.ANSI.MAGENTA);
        formatToken(str, stri, comment, TextColor.ANSI.GREEN);
        formatToken(str, stri, digit, TextColor.ANSI.CYAN);
        formatToken(str, stri, keyword, TextColor.ANSI.CYAN);
        formatToken(str, stri, attributes, TextColor.ANSI.GREEN);
        formatToken(str, stri, string, TextColor.ANSI.YELLOW);
        formatToken(str, stri, operators, TextColor.ANSI.YELLOW);
        formatToken(str, stri, digit, TextColor.ANSI.YELLOW);
    }
}
