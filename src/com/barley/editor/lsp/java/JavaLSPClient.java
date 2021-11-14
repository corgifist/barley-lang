package com.barley.editor.lsp.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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

    private Object _lock = new Object();
    private boolean _started = false;
    private boolean _enabled = true;

    private InputStream _istream;
    private OutputStream _ostream;
    private LanguageServer _server;
    private ServerCapabilities _capabilities;
    
    private String _homePath = System.getProperty("user.home");
    private String _fiskedHomePath = _homePath + "/.fisked";
    private String _eclipsePath = _fiskedHomePath + "/deps/eclipse.jdt.ls/org.eclipse.jdt.ls.product/target/repository";
    private String _projectPath = "";
    private String _workspacePath = _fiskedHomePath + "/workspace";
    
    public boolean hasStarted() {
        return _started;
    }
    
    public boolean isEnabled() {
        return _enabled;
    }
    
    public JavaLSPClient() {
        initColours();
    }
    
    private void initColours() {
        _foregroundColours.put(String.join(":", new String[] {
            "invalid.deprecated.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.RED);
        _foregroundColours.put(String.join(":", new String[] {
            "variable.other.autoboxing.java",
            "meta.method.body.java",
            "meta.method.java",
            "meta.class.body.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.BLUE);
        _foregroundColours.put(String.join(":", new String[] {
            "storage.modifier.static.java",
            "storage.modifier.final.java",
            "variable.other.definition.java",
            "meta.definition.variable.java",
            "meta.class.body.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.YELLOW);
        _foregroundColours.put(String.join(":", new String[] {
            "storage.modifier.static.java",
            "variable.other.definition.java",
            "meta.definition.variable.java",
            "meta.class.body.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.YELLOW);
        _foregroundColours.put(String.join(":", new String[] {
            "meta.function-call.java",
            "meta.method.body.java",
            "meta.method.java",
            "meta.class.body.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.BLUE);
        _foregroundColours.put(String.join(":", new String[] {
            "meta.definition.variable.java",
            "meta.class.body.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.GREEN);
        _foregroundColours.put(String.join(":", new String[] {
            "entity.name.function.java",
            "meta.method.identifier.java",
            "meta.method.java",
            "meta.class.body.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.BLUE);
        _foregroundColours.put(String.join(":", new String[] {
            "storage.modifier.static.java",
            "entity.name.function.java",
            "meta.function-call.java",
            "meta.method.body.java",
            "meta.method.java",
            "meta.class.body.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.BLUE);
        _foregroundColours.put(String.join(":", new String[] {
            "storage.modifier.abstract.java",
            "entity.name.function.java",
            "meta.function-call.java",
            "meta.method.body.java",
            "meta.method.java",
            "meta.class.body.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.BLUE);
        _foregroundColours.put(String.join(":", new String[] {
            "constant.other.key.java",
            "meta.declaration.annotation.java",
            "source.java"
        }), TextColor.ANSI.CYAN);
        _foregroundColours.put(String.join(":", new String[] {
            "entity.name.function.java",
            "meta.function-call.java",
            "meta.method.body.java",
            "meta.method.java",
            "meta.class.body.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.BLUE);
        _foregroundColours.put(String.join(":", new String[] {
            "variable.parameter.java",
            "meta.method.identifier.java",
            "meta.method.java",
            "meta.class.body.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.MAGENTA);
        _foregroundColours.put(String.join(":", new String[] {
            "variable.other.definition.java",
            "meta.definition.variable.java",
            "meta.method.body.java",
            "meta.method.java",
            "meta.class.body.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.MAGENTA);
        _foregroundColours.put(String.join(":", new String[] {
            "meta.method.body.java",
            "meta.method.java",
            "meta.class.body.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.DEFAULT);
        _foregroundColours.put(String.join(":", new String[] {
            "storage.type.generic.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.GREEN);
        _foregroundColours.put(String.join(":", new String[] {
            "entity.name.function.java",
            "meta.method.identifier.java",
            "meta.function-call.java",
            "meta.method.java",
            "meta.class.body.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.BLUE);
        _foregroundColours.put(String.join(":", new String[] {
            "storage.type.generic.java",
            "meta.definition.class.implemented.interfaces.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.MAGENTA);
        _foregroundColours.put(String.join(":", new String[] {
            "storage.modifier.abstract.java",
            "entity.name.type.class.java",
            "meta.class.identifier.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.GREEN);
        _foregroundColours.put(String.join(":", new String[] {
            "entity.name.type.class.java",
            "meta.class.identifier.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.GREEN);
        _foregroundColours.put(String.join(":", new String[] {
            "entity.name.type.enum.java",
            "meta.enum.java",
            "source.java"
        }), TextColor.ANSI.GREEN);
        _foregroundColours.put(String.join(":", new String[] {
            "storage.type.annotation.java",
            "meta.declaration.annotation.java",
            "source.java"
        }), TextColor.ANSI.GREEN);
        _foregroundColours.put(String.join(":", new String[] {
            "entity.name.type.interface.java",
            "meta.class.identifier.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.GREEN);
        _foregroundColours.put(String.join(":", new String[] {
            "constant.numeric.decimal.java",
            "meta.definition.variable.java",
            "meta.method.body.java",
            "meta.method.java",
            "meta.class.body.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.MAGENTA);
        _foregroundColours.put(String.join(":", new String[] {
            "keyword.other.var.java",
            "meta.class.body.java",
            "meta.class.java",
            "source.java"
        }), TextColor.ANSI.RED);
    }

    private void setup() throws IOException {
        _log.info("LSP eclipse path: " + _eclipsePath);
        _log.info("LSP workspace path: " + _projectPath);
        _log.info("LSP workspace folder path: " + _workspacePath);

        var java = "java";
        var javaArgs = "-Declipse.application=org.eclipse.jdt.ls.core.id1 -Dosgi.bundles.defaultStartLevel=4 -Declipse.product=org.eclipse.jdt.ls.core.product -Dlog.level=ALL";
        var jvmArgs = "-Xmx4G --add-modules=ALL-SYSTEM --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED";
        var jarPath = _eclipsePath + "/plugins/org.eclipse.equinox.launcher_1.5.700.v20200107-1357.jar";
        var appArgs = "-configuration " + _eclipsePath + "/config_linux -data " + _workspacePath;
        var command = java + " " + javaArgs +  " " + jvmArgs + " -jar " + jarPath + " " + appArgs;
        var commandArg = command.split(" ");
        var processBuilder = new ProcessBuilder(commandArg);
        var process = processBuilder.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (!_started) {
                    process.destroy();
                } else {
                    try {
                        _server.shutdown().get();
                        _server.exit();
                    } catch (Exception e) {
                        process.destroy();
                    }
                }
            }
        });

        _log.info("Proccess command: " + command);
        _log.info("Process PID: " + process.pid());

        new Thread() {
            public void run() {
                try {
                    _log.info("Starting LSP server...");
                    _istream = process.getInputStream();
                    _ostream = process.getOutputStream();
                    var client = new LanguageClient() {
                        @Override
                        public void telemetryEvent(Object object) {
                            _log.info("telemetryEvent called");
                        }
                        @Override
                        public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
                            _log.info("publishDiagnostics called");
                        }
                        @Override
                        public void showMessage(MessageParams message) {
                            _log.info("showMessage: " + message.getMessage());
                        }
                        @Override
                        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
                            _log.info("showMessageRequest called");
                            return null;
                        }
                        @Override
                        public void logMessage(MessageParams message) {
                            _log.info("logMessage: " + message.getMessage());
                        }
                        @Override
                        public void semanticHighlighting(SemanticHighlightingParams params) {
                            _log.info("Semantic info: " + params);
                            var document = params.getTextDocument();
                            var currentBuffer = Window.getInstance().getBufferContext().getBuffer();
                            if (Paths.get(document.getUri()).equals(Paths.get(currentBuffer.getTextDocumentID().getUri()))) {
                                currentBuffer.applyDecorations(document.getVersion(), params.getLines());
                            }
                        }
                        @Override
                        public CompletableFuture<List<WorkspaceFolder>> workspaceFolders() {
                            _log.info("Workspace folders?");
                            return null;
                        }
                        @Override
                        public CompletableFuture<List<Object>> configuration(ConfigurationParams configurationParams) {
                            _log.info("Configuration?");
                            throw new UnsupportedOperationException();
                        }
                        public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
                            _log.info("Workspace edit?");
                            throw new UnsupportedOperationException();
                        }
                        public CompletableFuture<Void> registerCapability(RegistrationParams params) {
                            _log.info("Register capability?");
                            throw new UnsupportedOperationException();
                        }
                        public CompletableFuture<Void> unregisterCapability(UnregistrationParams params) {
                            _log.info("Unregister capability?");
                            throw new UnsupportedOperationException();
                        }
                    };
                    var clientLauncher = LSPLauncher.createClientLauncher(client, _istream, _ostream);
                    var listeningFuture = clientLauncher.startListening();
                    _server = clientLauncher.getRemoteProxy();
                    try {
                        var initParams = new InitializeParams();
                        initParams.setRootUri(new File(_projectPath).toURI().toString());
                        initParams.setCapabilities(getClientCapabilities());
                        var initialized = _server.initialize(initParams).get();
                        _capabilities = initialized.getCapabilities();
                        _log.info("Server capabilities: " + _capabilities);
                        synchronized (_lock) {
                            _started = true;
                            _lock.notifyAll();
                        }
                    } catch (Exception e) {
                        _log.error("Exception initializing LSP server", e);
                    }
                    listeningFuture.get();
                } catch (Exception e) {
                    _log.error("Error reading process output: ", e);
                    return;
                }
            }
        }.start();
    }

    private ClientCapabilities getClientCapabilities() {
        var workspace = new WorkspaceClientCapabilities();
        workspace.setApplyEdit(true);
        workspace.setConfiguration(true);
        
        var executeCommand = new ExecuteCommandCapabilities();
        workspace.setExecuteCommand(executeCommand);
        
        var textDocument = new TextDocumentClientCapabilities();

        var semanticHighlighting = new SemanticHighlightingCapabilities(true);
        textDocument.setSemanticHighlightingCapabilities(semanticHighlighting);

        var codeAction = new CodeActionCapabilities(true);
        textDocument.setCodeAction(codeAction);
        
        var codeLens = new CodeLensCapabilities();
        textDocument.setCodeLens(codeLens);
        
        var references = new ReferencesCapabilities();
        textDocument.setReferences(references);

        var clientCapabilities = new ClientCapabilities(workspace, textDocument, null);
        return clientCapabilities;
    }

    public void run() {
        if (!_enabled) {
            return;
        }
        try {
            setup();
        } catch (Throwable e) {
            _log.error("Error setting up LSP server", e);
        }
    }

    public LanguageServer getServer() {
        return _server;
    }

    static JavaLSPClient _instance = new JavaLSPClient();

    public static JavaLSPClient getInstance() {
        return _instance;
    }

    public void ensureInit() {
        if (!_enabled) {
            return;
        }
        for (;;) {
            synchronized (_lock) {
                if (_started) {
                    break;
                } else {
                    try {
                        _lock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    public List<ColorInformation> decorateBuffer(BufferContext bufferContext) {
        if (!_enabled) {
            return new ArrayList<ColorInformation>();
        }
        try {
            _log.info("Decorate buffer");
            var colorParams = new DocumentColorParams(bufferContext.getBuffer().getTextDocumentID());
            return _server.getTextDocumentService().documentColor(colorParams).join();
        } catch (Exception e) {
            _log.error("Error getting colours: ", e);
            throw new RuntimeException("Error getting code actions: ", e);
        }
    }
    
    public void codeLens(BufferContext bufferContext) {
        try {
            var params = new CodeLensParams();
            params.setTextDocument(bufferContext.getBuffer().getTextDocumentID());
            var result = _server.getTextDocumentService().codeLens(params).get();
            for (var r: result) {
                _log.info("Code lens item: " + r);
                var resolved = _server.getTextDocumentService().resolveCodeLens(r).get();
                _log.info("Resolved code lens item: " + resolved);
                var params2 = new ExecuteCommandParams();
                params2.setCommand(resolved.getCommand().getCommand());
                params2.setArguments(resolved.getCommand().getArguments());
                var commandResult = _server.getWorkspaceService().executeCommand(params2).get();
                _log.info("Command result: " + commandResult);
            }
        } catch (InterruptedException | ExecutionException e) {
            _log.error("Code lens failed: ", e);
        }
        
    }

    private List<Either<Command, CodeAction>> getCodeActions(BufferContext bufferContext) {
        try {
            _log.info("Get code actions");
            var lineCount = bufferContext.getTextLayout().getPhysicalLineCount();
            var line = bufferContext.getTextLayout().getLastPhysicalLine();
            var range = new Range(new Position(0, 0), new Position(lineCount - 1, line.getGlyphs().size()));
            var diagnostics = new ArrayList<Diagnostic>();
            var context = new CodeActionContext(diagnostics);
            var params = new CodeActionParams(bufferContext.getBuffer().getTextDocumentID(), range, context);
            _log.info("Code action: " + params);
            return _server.getTextDocumentService().codeAction(params).join();
        } catch (Exception e) {
            _log.error("Error getting code actions: ", e);
            throw new RuntimeException("Error getting code actions: ", e);
        }
    }

    private Command getCodeActionCommand(BufferContext bufferContext, String title) {
        for (var either: getCodeActions(bufferContext)) {
            if (either.isLeft()) {
                _log.info("Left code action: " + either);
                var command = either.getLeft();
                if (command.getTitle().equals(title)) {
                    return command;
                }
            }
            if (either.isRight()) {
                _log.info("Right code action: " + either);
            }
        }
        return null;
    }

    private void applyWorkspaceEdit(BufferContext context, List<Object> args) {
        var json = args.get(0).toString();
        _log.info("applyWorkspaceEdit: " + json);
        var root = (Map<String, Object>)new Gson().fromJson(json, HashMap.class);
        var changes = (Map<String, Object>)root.get("changes");
        for (var changeEntry: changes.entrySet()) {
            URI uri = null;
            try {
                uri = new URI(changeEntry.getKey());
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid URI", e);
            }
            if (!uri.equals(context.getBuffer().getURI())) {
                throw new RuntimeException("Applying workspace edit to unexpected URI: " + uri);
            }
            var edits = (List<Map<String, Object>>) changeEntry.getValue();
            for (var edit: edits) {
                var range = (Map<String, Object>) edit.get("range");
                var startPoint = (Map<String, Double>)range.get("start");
                var startLine = (int) (double)startPoint.get("line");
                var startCharacter = (int) (double)startPoint.get("character");
                var startIndex = context.getTextLayout().getIndexForPhysicalLineCharacter(startLine, startCharacter);
                var endPoint = (Map<String, Double>) range.get("end");
                var endLine = (int) (double)endPoint.get("line");
                var endCharacter = (int) (double)endPoint.get("character");
                var endIndex = context.getTextLayout().getIndexForPhysicalLineCharacter(endLine, endCharacter);
                var buffer = context.getBuffer();
                var newText = (String)edit.get("newText");
                newText = newText.replaceAll("\t", "    ");
                _log.info("Insert " + newText + " at " + startIndex);
                _log.info("Remove [" + startIndex + ", " + endIndex + "]");
                buffer.remove(startIndex, endIndex);
                buffer.insert(startIndex, newText);
            }
        }
    }

    private void applyCommand(BufferContext bufferContext, Command command) {
        switch (command.getCommand()) {
            case "java.apply.workspaceEdit":
                applyWorkspaceEdit(bufferContext, command.getArguments());
                break;
            default:
                throw new RuntimeException("Unknown command: " + command);
        }
    }

    public void organizeImports(BufferContext bufferContext) {
        if (!_enabled) {
            return;
        }
        try {
            var command = getCodeActionCommand(bufferContext, "Organize imports");
            if (command != null) {
                applyCommand(bufferContext, command);
            }
        } catch (Exception e) {
            _log.error("Exception: ", e);
        }
    }

    public void makeFinal(BufferContext bufferContext) {
        if (!_enabled) {
            return;
        }
        try {
            var command = getCodeActionCommand(bufferContext, "Change modifiers to final where possible");
            if (command != null) {
                applyCommand(bufferContext, command);
            }
        } catch (Exception e) {
            _log.error("Exception: ", e);
        }
    }

    public void generateAccessors(BufferContext bufferContext) {
        if (!_enabled) {
            return;
        }
        try {
            var command = getCodeActionCommand(bufferContext, "Generate Getters and Setters");
            if (command != null) {
                applyCommand(bufferContext, command);
            }
        } catch (Exception e) {
            _log.error("Exception: ", e);
        }
    }

    public void generateToString(BufferContext bufferContext) {
        if (!_enabled) {
            return;
        }
        try {
            var command = getCodeActionCommand(bufferContext, "Generate toString()...");
            if (command != null) {
                applyCommand(bufferContext, command);
            }
        } catch (Exception e) {
            _log.error("Exception: ", e);
        }
    }
    
    public void willSave(BufferContext bufferContext) {
        if (!_enabled) {
            return;
        }
        _log.info("willSave");
        var params = new WillSaveTextDocumentParams();
        params.setTextDocument(bufferContext.getBuffer().getTextDocumentID());
        params.setReason(TextDocumentSaveReason.Manual);
        _server.getTextDocumentService().willSave(params);
    }
    
    public void didSave(BufferContext bufferContext) {
        if (!_enabled) {
            return;
        }
        _log.info("didSave");
        var params = new DidSaveTextDocumentParams();
        params.setTextDocument(bufferContext.getBuffer().getTextDocumentID());
        params.setText(bufferContext.getBuffer().getString());
        _server.getTextDocumentService().didSave(params);
    }

    public void didOpen(BufferContext bufferContext) {
        if (!_enabled) {
            return;
        }
        _log.info("didOpen");
        var params = new DidOpenTextDocumentParams();
        params.setTextDocument(bufferContext.getBuffer().getTextDocument());
        _server.getTextDocumentService().didOpen(params);
    }

    public void didClose(BufferContext bufferContext) {
        if (!_enabled) {
            return;
        }
        _log.info("didClose");
        var params = new DidCloseTextDocumentParams();
        params.setTextDocument(bufferContext.getBuffer().getTextDocumentID());
        _server.getTextDocumentService().didClose(params);
    }

    public void didInsert(BufferContext bufferContext, int position, String text) {
        if (!_enabled) {
            return;
        }
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
        _server.getTextDocumentService().didChange(params);
    }

    public void didRemove(BufferContext bufferContext, int startPosition, int endPosition) {
        if (!_enabled) {
            return;
        }
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
        _server.getTextDocumentService().didChange(params);
    }

    public ServerCapabilities getCapabilities() {
        return _capabilities;
    }
    
    private Map<String, TextColor> _foregroundColours = new HashMap<>();

    public TextColor foregroundColourForScope(int scope) {
        var capabilities = JavaLSPClient.getInstance().getCapabilities();
        var scopes = capabilities.getSemanticHighlighting().getScopes();
        var identifiers = String.join(":", scopes.get(scope));
        var result = _foregroundColours.get(identifiers);
        if (result == null) {
            throw new RuntimeException("Wait what is this scope " + scope);
        }
        return result;
    }
    
    private static Pattern _javaCommentPattern = Pattern.compile("(/\\*([^*]|[\\n]|(\\*+([^*/]|[\\n])))*\\*+/)|(//.*)", Pattern.MULTILINE);
    private static Pattern _javaStringPattern = Pattern.compile("\\\"([^\\\\\\\"]|(\\\\.))*\\\"", Pattern.MULTILINE);
    private static Pattern _javaCharacterPattern = Pattern.compile("'[^']*'", Pattern.MULTILINE);
    private static Pattern _javaKeywordPattern = Pattern.compile(
            "(\\bprivate\\b)|(\\bprotected\\b)|(\\bpublic\\b)|(\\bstatic\\b)|(\\babstract\\b)|" + 
            "(\\bvoid\\b)|(\\bbyte\\b)|(\\bchar\\b)|(\\bboolean\\b)|(\\bshort\\b)|(\\bint\\b)|(\\blong\\b)|(\\bfloat\\b)|" + 
            "(\\bdouble\\b)|(\\bimplements\\b)|(\\bextends\\b)|(\\bclass\\b)|(\\benum\\b)|(\\bfinal\\b)|" + 
            "(\\btry\\b)|(\\bcatch\\b)|(\\bthrows\\b)|(\\bthrow\\b)|(\\brecord\\b)|(\\bnew\\b)|(\\breturn\\b)|" +
            "(\\bif\\b)|(\\belse\\b)|(\\bfor\\b)|(\\bwhile\\b)|(\\bdo\\b)|(\\bimport\\b)|(\\bpackage\\b)|" +
            "(\\bcase\\b)|(\\bbreak\\b)|(\\bthis\\b)|(\\bsynchronized\\b)|(\\bvar\\b)|(\\bdefault\\b)",
            Pattern.MULTILINE);
    private static Pattern _javaKeywordTokenPattern = Pattern.compile("(\\bnull\\b)|(\\btrue\\b)|(\\bfalse\\b)", Pattern.MULTILINE);

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
    public void applyColouring(BufferContext bufferContext, AttributedString str) {
        var string = str.toString();
        formatToken(str, string, _javaKeywordPattern, TextColor.ANSI.RED);
        formatToken(str, string, _javaKeywordTokenPattern, TextColor.ANSI.CYAN);
        formatToken(str, string, _javaCharacterPattern, TextColor.ANSI.CYAN);
        formatToken(str, string, _javaCommentPattern, TextColor.ANSI.GREEN);
        formatToken(str, string, _javaStringPattern, TextColor.ANSI.CYAN);
    }

    private static Pattern _bracketPattern = Pattern.compile("\\{|\\}");
    
    @Override
    public int getIndentationLevel(BufferContext bufferContext) {
        int indentation = 0;
        int cursor = bufferContext.getBuffer().getCursor().getPosition();
        var matcher = _bracketPattern.matcher( bufferContext.getBuffer().getString());
        while (matcher.find()) {
            if (matcher.start() >= cursor) {
                return indentation;
            }
            if (matcher.group(0).equals("{")) {
                ++indentation;
            }
            if (matcher.group(0).equals("}")) {
                --indentation;
            }
        }
        return indentation;
    }
    
    @Override
    public boolean isIndentationEnd(BufferContext bufferContext, String character) {
        if (character.equals("}")) {
            return true;
        }
        return false;
    }

    @Override
    public TextDocumentItem getTextDocument(BufferContext bufferContext) {
        return new TextDocumentItem(bufferContext.getBuffer().getPath().toFile().toURI().toString(), "java", 11, bufferContext.getBuffer().getString());
    }
}
