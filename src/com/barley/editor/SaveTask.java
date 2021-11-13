package com.barley.editor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.TimerTask;

import static com.barley.editor.Editor.ANSI_RED;
import static com.barley.editor.Editor.ANSI_RESET;

public class SaveTask extends TimerTask {

    private String file, content;

    public SaveTask(String file, String content) {
        this.file = file;
        this.content = content;
    }

    @Override
    public void run() {
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(content);
            System.out.println(ANSI_RED + "SAVED SUCCESSFULLY" + ANSI_RESET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
