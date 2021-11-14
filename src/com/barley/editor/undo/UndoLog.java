package com.barley.editor.undo;

import java.util.LinkedList;

import com.barley.editor.text.BufferContext;
import com.barley.editor.utils.LogFactory;
import org.slf4j.Logger;

public class UndoLog {
    private static final Logger _log = LogFactory.createLog();

    private static abstract class ChangeRecord {
        ChangeRecord _prev;
        abstract void apply(BufferContext bufferContext);
        abstract ChangeRecord inverse();
        abstract int position();
    }

    private static class InsertRecord extends ChangeRecord {
        int _position;
        String _string;

        InsertRecord(int position, String str) {
            _position = position;
            _string = str;
        }

        void apply(BufferContext bufferContext) {
            _log.info("apply InsertRecord " + _position + ", " + _string);
            bufferContext.getBuffer().rawInsert(_position, _string);
        }

        ChangeRecord inverse() {
            return new RemoveRecord(_position, _string);
        }

        int position() {
            return _position;
        }
    }

    private static class RemoveRecord extends ChangeRecord {
        int _position;
        String _string;

        RemoveRecord(int position, String str) {
            _position = position;
            _string = str;
        }

        void apply(BufferContext bufferContext) {
            _log.info("apply RemoveRecord " + _position + ", " + _string);
            bufferContext.getBuffer().rawRemove(_position, _position + _string.length());
        }

        ChangeRecord inverse() {
            return new InsertRecord(_position, _string);
        }

        int position() {
            return _position;
        }
    }

    private static class BatchRecord extends ChangeRecord {
        LinkedList<ChangeRecord> _changes = new LinkedList<>();

        BatchRecord(ChangeRecord head) {
            var record = head;
            while (record != null) {
                _changes.addFirst(record);
                record = record._prev;
            }
        }

        BatchRecord(LinkedList<ChangeRecord> changes) {
            _changes = changes;
        }

        void apply(BufferContext bufferContext) {
            _log.info("apply BatchRecord");
            for (var record: _changes) {
                record.apply(bufferContext);
            }
            _log.info("end apply BatchRecord");
        }

        ChangeRecord inverse() {
            var changes = new LinkedList<ChangeRecord>();
            for (var record: _changes) {
                changes.addFirst(record.inverse());
            }
            _log.info("Inverted BatchRecord, this size " + _changes.size() + " inverted size " + _changes.size());
            return new BatchRecord(changes);
        }

        int position() {
            return _changes.peekLast().position();
        }
    }

    private ChangeRecord _head;
    private ChangeRecord _undoHead;
    private ChangeRecord _redoHead;
    private BufferContext _bufferContext;

    public UndoLog(BufferContext bufferContext) {
        _bufferContext = bufferContext;
    }

    private void log(ChangeRecord record) {
        record._prev = _head;
        _head = record;
    }

    public void recordInsert(int position, String str) {
        log(new InsertRecord(position, str));
    }

    public void recordRemove(int startPosition, int endPosition) {
        var str = _bufferContext.getBuffer().getSubstring(startPosition, endPosition);
        log(new RemoveRecord(startPosition, str));
    }

    public void commit() {
        if (_head == null) {
            return;
        }
        _redoHead = null;
        var record = new BatchRecord(_head);
        _head = null;
        record._prev = _undoHead;
        _undoHead = record;
    }

    public int undo() {
        if (_undoHead == null) {
            return -1;
        }
        var inv = _undoHead.inverse();
        _undoHead = _undoHead._prev;
        inv.apply(_bufferContext);
        inv._prev = _redoHead;
        _redoHead = inv;
        return inv.position();
    }

    public int redo() {
        if (_redoHead == null) {
            return -1;
        }
        var inv = _redoHead.inverse();
        _redoHead = _redoHead._prev;
        inv.apply(_bufferContext);
        inv._prev = _undoHead;
        _undoHead = inv;
        return inv.position();
    }
}
