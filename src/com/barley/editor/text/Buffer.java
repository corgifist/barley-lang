package com.barley.editor.text;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import com.barley.editor.copy.Copy;
import com.barley.editor.lsp.LanguageMode;
import com.barley.editor.lsp.LanguageModeProvider;
import com.barley.editor.ui.Cursor;
import com.barley.editor.ui.Window;
import org.eclipse.lsp4j.SemanticHighlightingInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.util.SemanticHighlightingTokens;
import com.barley.editor.EventThread;
import com.barley.editor.event.RunnableEvent;
import com.barley.editor.lsp.java.JavaLSPClient;
import com.barley.editor.undo.UndoLog;
import com.barley.editor.utils.LogFactory;
import org.slf4j.Logger;

import com.googlecode.lanterna.TextColor;

public class Buffer {
    private StringBuilder _string = new StringBuilder();
    private Path _path;
    private List<Cursor> _cursors = new ArrayList<>();
    private BufferContext _bufferContext;
    private UndoLog _undoLog;
    private int _version = 1;
    private static Logger _log = LogFactory.createLog();

    public Cursor getCursor() {
        return _cursors.get(0);
    }
    
    public void addCursor(Cursor cursor) {
        _cursors.add(cursor);
    }
    
    public List<Cursor> getCursors() {
        return _cursors;
    }
    
    public void clearCursors() {
        var cursor = _cursors.get(0);
        _cursors.clear();
        _cursors.add(cursor);
    }
    
    public List<Cursor> getCursorsOrdered() {
        var result = new ArrayList<Cursor>();
        result.addAll(_cursors);
        result.sort((Cursor c1, Cursor c2) -> {
            return c1.getPosition() - c2.getPosition();
        });
        return result;
    }
    
    private LanguageMode _languageMode;

    public Buffer(Path path, BufferContext bufferContext) {
        path = path.toAbsolutePath();
        _path = path;
        _bufferContext = bufferContext;
        _cursors.add(new Cursor(bufferContext));
        _undoLog = new UndoLog(bufferContext);
        try {
            _string.append(Files.readString(path));
            var decoration = new Decoration();
            decoration._str = AttributedString.create(_string.toString(), TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
            decoration._version = _version;
            _decorations.add(decoration);
        } catch (IOException e) {
        }
        _languageMode = LanguageModeProvider.getInstance().getLanguageMode(path);
    }

    public String getCharacter(int position) {
        if (position < 0 || _string.length() == 0 || position >= _string.length()) {
            return "";
        }
        return _string.substring(position, position + 1);
    }

    public void undo() {
        int position = _undoLog.undo();
        if (position == -1) {
            return;
        }
        getCursor().setPosition(position);
        _bufferContext.getTextLayout().calculate();
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public void redo() {
        int position = _undoLog.redo();
        if (position == -1) {
            return;
        }
        getCursor().setPosition(position);
        _bufferContext.getTextLayout().calculate();
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public UndoLog getUndoLog() {
        return _undoLog;
    }

    public void rawInsert(int position, String str) {
        _string.insert(position, str);
        _version++;
        var decoration = new Decoration();
        decoration._str = AttributedString.create(_string.toString(), TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
        decoration._version = _version;
        decoration._didInsert = true;
        decoration._insertPosition = position;
        decoration._insertString = str;
        _decorations.add(decoration);
        _languageMode.didInsert(_bufferContext, position, str);
    }

    public void rawRemove(int startPosition, int endPosition) {
        _string.delete(startPosition, endPosition);
        _version++;
        var decoration = new Decoration();
        decoration._str = AttributedString.create(_string.toString(), TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
        decoration._version = _version;
        decoration._didRemove = true;
        decoration._removeStart = startPosition;
        decoration._removeEnd = endPosition;
        _decorations.add(decoration);
        _languageMode.didRemove(_bufferContext, startPosition, endPosition);
    }

    public void remove(int startPosition, int endPosition) {
        if (endPosition - startPosition <= 0) {
            return;
        }
        _undoLog.recordRemove(startPosition, endPosition);
        rawRemove(startPosition, endPosition);
        getCursor().setPosition(startPosition);
        _bufferContext.getTextLayout().calculate();
        _bufferContext.getBufferView().adaptViewToCursor();
    }
    
    public void reindentLine() {
        var textLayout = _bufferContext.getTextLayout();
        var position = getCursor().getPosition();
        var line = textLayout.getPhysicalLineAt(position);
        int lineStartPosition = line.getStartPosition();
        if (lineStartPosition >= position) {
            return;
        }
        var substring = getString().substring(lineStartPosition, position - 1);
        if (substring.trim().equals("")) {
            remove(lineStartPosition, position - 1);
            var str = "";
            for (int i = 0; i < getIndentationLevel() - 1; ++i) {
                str += Settings.getIndentationString();
            }
            insert(str);
        }
    }


    public void insert(String str) {
        if (str.equals("\n")) {
            for (int i = 0; i < getIndentationLevel(); ++i) {
                str += Settings.getIndentationString();
            }
        }
        int inserted = 0;
        for (var cursor: getCursorsOrdered()) {
            int position = cursor.getPosition() + inserted;
            _undoLog.recordInsert(position, str);
            rawInsert(position, str);
            _bufferContext.getTextLayout().calculate();
            cursor.setPosition(position + str.length());
            _bufferContext.getBufferView().adaptViewToCursor();
            inserted += str.length();
        }
        if (isIndentationEnd(str)) {
            reindentLine();
        }
    }

    public void insert(int position, String str) {
        _undoLog.recordInsert(position, str);
        rawInsert(position, str);
        _bufferContext.getTextLayout().calculate();
        getCursor().setPosition(position + str.length());
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public void removeBefore() {
        int removed = 0;
        for (var cursor: getCursorsOrdered()) {
            int position = cursor.getPosition() - removed - 1;
            if (position < 0 || _string.length() == 0) {
                continue;
            }
            _undoLog.recordRemove(position, position + 1);
            rawRemove(position, position + 1);
            cursor.setPosition(position);
            removed++;
        }
        _bufferContext.getTextLayout().calculate();
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public void removeAt() {
        if (_string.length() == 0) {
            return;
        }
        int position = getCursor().getPosition();
        if (position >= _string.length()) {
            return;
        }
        Copy.getInstance().setText(getSubstring(position, position + 1), false /* isLine */);
        _undoLog.recordRemove(position, position + 1);
        rawRemove(getCursor().getPosition(), getCursor().getPosition() + 1);
        _bufferContext.getTextLayout().calculate();
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public void deleteInnerWord() {
        int start = findStartOfWord();
        int end = findEndOfWord();
        if (start == -1 || end == -1) {
            return;
        }
        Copy.getInstance().setText(getSubstring(start, end), false /* isLine */);
        _undoLog.recordRemove(start, end);
        rawRemove(start, end);
        getCursor().setPosition(start);
        _bufferContext.getTextLayout().calculate();
        _bufferContext.getBufferView().adaptViewToCursor();
    }
    
    public String getInnerWord() {
        int start = findStartOfWord();
        int end = findEndOfWord();
        if (start == -1 || end == -1) {
            return "";
        }
        return getSubstring(start, end);
    }

    public void deleteWord() {
        int start = getCursor().getPosition();
        if (!_wordPattern.matcher(getCharacter(start)).matches()) {
            return;
        }
        int end = findEndOfWord();
        if (end == -1) {
            return;
        }
        Copy.getInstance().setText(getSubstring(start, end), false /* isLine */);
        _undoLog.recordRemove(start, end);
        rawRemove(start, end);
        _bufferContext.getTextLayout().calculate();
        _bufferContext.getBufferView().adaptViewToCursor();
    }

    public void deleteLine() {
        var textLayout = _bufferContext.getTextLayout();
        var line = textLayout.getPhysicalLineAt(getCursor().getPosition());
        int start = line.getStartPosition();
        var glyph = line.getLastGlyph(true);
        int end;
        if (glyph != null) {
            end = glyph.getPosition() + 1;
        } else {
            end = line.getStartPosition();
        }
        if (line.getNext() == null) {
            // Last line is special
            start = Math.max(0, start - 1);
        }
        Copy.getInstance().setText(getSubstring(start, end), true /* isLine */);
        _undoLog.recordRemove(start, end);
        rawRemove(start, end);
        _bufferContext.getTextLayout().calculate();
        getCursor().setPosition(start);
        _bufferContext.getBufferView().adaptViewToCursor();
    }
    
    public String getCurrentLineText() {
        var textLayout = _bufferContext.getTextLayout();
        var line = textLayout.getPhysicalLineAt(getCursor().getPosition());
        int start = line.getStartPosition();
        var glyph = line.getLastGlyph(true);
        int end;
        if (glyph != null) {
            end = glyph.getPosition() + 1;
        } else {
            end = line.getStartPosition();
        }
        if (line.getNext() == null) {
            // Last line is special
            start = Math.max(0, start - 1);
        }
        return getSubstring(start, end);
    }

    static Pattern _wordPattern = Pattern.compile("\\w");

    private int findStartOfWord() {
        int position = getCursor().getPosition();
        if (!_wordPattern.matcher(getCharacter(position)).matches()) {
            return -1;
        }
        for (int i = position; i >= 0; --i) {
            if (!_wordPattern.matcher(getCharacter(i)).matches()) {
                return i + 1;
            }
        }
        return 0;
    }

    private int findEndOfWord() {
        int position = getCursor().getPosition();
        if (!_wordPattern.matcher(getCharacter(position)).matches()) {
            return -1;
        }
        for (int i = position; i < getLength(); ++i) {
            if (!_wordPattern.matcher(getCharacter(i)).matches()) {
                return i;
            }
        }
        return getLength();
    }

    public void write() {
        _languageMode.willSave(_bufferContext);
        try {
            Files.writeString(_path, _string.toString());
            Window.getInstance().getCommandView().setMessage("Saved file");
        } catch (IOException e) {
        }
        _languageMode.didSave(_bufferContext);
    }
    
    public void close() {
        _languageMode.didClose(_bufferContext);
    }
    
    public void open() {
        _languageMode.didOpen(_bufferContext);
    }

    public int getLength() {
        return _string.length();
    }

    public String getString() {
        return _string.toString();
    }

    public String getSubstring(int start, int end) {
        return _string.substring(start, end);
    }

    public URI getURI() {
        return _path.toFile().toURI();
    }

    public int getIndentationLevel() {
        return _languageMode.getIndentationLevel(_bufferContext);
    }
    
    public boolean isIndentationEnd(String character) {
        return _languageMode.isIndentationEnd(_bufferContext, character);
    }
    
    private static class Decoration {
        private volatile AttributedString _str;
        private int _version;
        
        private boolean _didInsert;
        private int _insertPosition;
        private String _insertString;
        
        private boolean _didRemove;
        private int _removeStart;
        private int _removeEnd;
        
        private volatile boolean _isDecorated;
    }
    
    private CopyOnWriteArrayList<Decoration> _decorations = new CopyOnWriteArrayList<>();

    public void applyDecorations(int version, List<SemanticHighlightingInformation> info) {
        _log.info("Applying decorations for version " + version);
        AttributedString str = null;
        for (var decoration: _decorations) {
            if (decoration._isDecorated) {
                _log.info("Found decorated string for version " + decoration._version);
                str = AttributedString.create(decoration._str);
            } else if (str != null) {
                if (decoration._didInsert) {
                    _log.info("Inserting string for version " + decoration._version);
                    str.insert(decoration._insertString, decoration._insertPosition, TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
                }
                if (decoration._didRemove) {
                    _log.info("Removing string for version " + decoration._version);
                    str.remove(decoration._removeStart, decoration._removeEnd);
                }
            }
            if (decoration._version != version) {
                _log.info("Skipping version " + decoration._version);
            } else {
                _log.info("Found version " + version);
                if (str != null) {
                    if (!decoration._str.toString().equals(str.toString())) {
                        throw new RuntimeException("Strings do not match: 1) " + decoration._str.toString() + "\n2) " + str.toString());
                    }
                    decoration._str = AttributedString.create(str);
                }
                _log.info("String length: " + decoration._str.length());
                for (var line: info) {
                    var decodedTokens = SemanticHighlightingTokens.decode(line.getTokens());
                    var lineNum = line.getLine();
                    for (var token: decodedTokens) {
                        var charNum = token.character;
                        int index = _bufferContext.getTextLayout().getIndexForPhysicalLineCharacter(lineNum, charNum);
                        _log.info("Format range [" + index + ", " + (index + token.length) + ")");
                        decoration._str.format(index, index + token.length, 
                                JavaLSPClient.getInstance().foregroundColourForScope(token.scope), 
                                TextColor.ANSI.DEFAULT);
                    }
                }
                _languageMode.applyColouring(_bufferContext, decoration._str);
                decoration._isDecorated = true;
                EventThread.getInstance().enqueue(new RunnableEvent(() -> {
                    _log.info("Redrawing version " + version);
                    _bufferContext.getBufferView().setNeedsRedraw();
                }));
                break;
            }
        }
    }
    
    public AttributedString getAttributedString() {
        Decoration lastAttributedDecoration = null;
        for (var decoration: _decorations) {
            if (decoration._isDecorated) {
                lastAttributedDecoration = decoration;
            }
        }
        if (lastAttributedDecoration != null) {
            for (var decoration: _decorations) {
                if (decoration == lastAttributedDecoration) {
                    break;
                } else {
                    _decorations.remove(decoration);
                }
            }
            AttributedString str = null;
            for (var decoration: _decorations) {
                if (decoration._isDecorated) {
                    _log.info("Found decorated string for version " + decoration._version);
                    str = AttributedString.create(decoration._str);
                } else if (str != null) {
                    if (decoration._didInsert) {
                        _log.info("Inserting string for version " + decoration._version);
                        str.insert(decoration._insertString, decoration._insertPosition, TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
                    }
                    if (decoration._didRemove) {
                        _log.info("Removing string for version " + decoration._version);
                        str.remove(decoration._removeStart, decoration._removeEnd);
                    }
                }
            }
            return str;
        } else {
            var str = AttributedString.create(_string.toString(), TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
            _languageMode.applyColouring(_bufferContext, str);
            return str;
        }
    }

    public TextDocumentItem getTextDocument() {
        return _languageMode.getTextDocument(_bufferContext);
    }

    public TextDocumentIdentifier getTextDocumentID() {
        return new TextDocumentIdentifier(_path.toFile().toURI().toString());
    }
    
    public VersionedTextDocumentIdentifier getVersionedTextDocumentID() {
        return new VersionedTextDocumentIdentifier(_path.toFile().toURI().toString(), _version);
    }

    public Path getPath() {
        return _path;
    }
}
