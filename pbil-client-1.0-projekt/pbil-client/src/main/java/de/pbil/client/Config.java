package de.pbil.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;

public final class Config {
    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("pbil_client.properties");
    }

    public static void load() {
        Path f = file();
        if (!Files.isRegularFile(f)) {
            return;
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(f)) {
            p.load(in);
        } catch (IOException e) {
            return;
        }
        for (Module m : Modules.all()) {
            String on = p.getProperty("on." + m.name);
            if (on != null && Boolean.parseBoolean(on)) {
                m.setEnabled(true);
            }
            for (Setting s : m.settings) {
                String v = p.getProperty("set." + m.name + "." + s.name);
                if (v == null) {
                    continue;
                }
                try {
                    if (s.isBool) {
                        s.b = Boolean.parseBoolean(v);
                    } else {
                        s.set(Double.parseDouble(v));
                    }
                } catch (NumberFormatException ignored) {
                    // kaputter Wert: Standard behalten
                }
            }
        }
    }

    public static void save() {
        Properties p = new Properties();
        for (Module m : Modules.all()) {
            p.setProperty("on." + m.name, String.valueOf(m.enabled));
            for (Setting s : m.settings) {
                p.setProperty("set." + m.name + "." + s.name, s.isBool ? String.valueOf(s.b) : String.valueOf(s.v));
            }
        }
        try {
            Path f = file();
            Files.createDirectories(f.getParent());
            try (OutputStream out = Files.newOutputStream(f)) {
                p.store(out, "Pbil Client");
            }
        } catch (IOException ignored) {
            // Speichern ist nur eine Bequemlichkeit
        }
    }

    private Config() {
    }
}
