/*
 *  Copyright (C) 2020 - 2021 Tecnio
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package me.tecnio.antihaxerman.util;

import me.tecnio.antihaxerman.AntiHaxerman;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class LogUtil {
    public static boolean logToFile(final TextFile file, final String text) {
        try {
            file.addLine(text);
            file.write();
            return true;
        } catch (final Exception ex) {
            return false;
        }
    }

    public static class TextFile {

        private File file;
        private String name;
        private final List<String> lines = new ArrayList<>();

        public TextFile(final String name, final String path) {
            AntiHaxerman.INSTANCE.getExecutorService().execute(() -> {
                this.file = new File(AntiHaxerman.INSTANCE.getPlugin().getDataFolder() + path);
                this.file.mkdirs();
                this.file = new File(AntiHaxerman.INSTANCE.getPlugin().getDataFolder() + path, name + ".txt");
                try {
                    if (!this.file.exists()) this.file.createNewFile();
                } catch (final Exception ignored) {
                }

                this.name = name;
                this.readTextFile();
            });
        }

        public void clear() {
            this.lines.clear();
        }

        public void addLine(final String line) {
            this.lines.add(line);
        }

        public void write() {
            AntiHaxerman.INSTANCE.getExecutorService().execute(() -> {
                try {
                    final FileWriter fw = new FileWriter(this.file, false);
                    final BufferedWriter bw = new BufferedWriter(fw);

                    for (final String line : this.lines) {
                        bw.write(line);
                        bw.newLine();
                    }

                    bw.close();
                    fw.close();
                } catch (final Exception ignored) {
                }
            });
        }

        public void readTextFile() {
            this.lines.clear();

            AntiHaxerman.INSTANCE.getExecutorService().execute(() -> {
                try {
                    String line;

                    final FileReader fr = new FileReader(this.file);
                    final BufferedReader br = new BufferedReader(fr);

                    while ((line = br.readLine()) != null) {
                        this.lines.add(line);
                    }

                    br.close();
                    fr.close();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            });
        }

        public String getText() {
            final StringBuilder text = new StringBuilder();

            int i = 0;

            while (i < this.lines.size()) {
                final String line = this.lines.get(i);
                text.append(line).append(this.lines.size() - 1 == i ? "" : "\n");
                ++i;
            }

            return text.toString();
        }
        public List<String> getLines() {
            return lines;
        }
    }

}