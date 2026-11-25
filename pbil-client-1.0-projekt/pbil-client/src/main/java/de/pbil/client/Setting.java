package de.pbil.client;

/** Eine Einstellung eines Moduls: Schalter (AN/AUS) oder Zahl mit Schrittweite. */
public final class Setting {
    public final String name;
    public final boolean isBool;
    public boolean b;
    public double v;
    public final double min;
    public final double max;
    public final double step;

    private Setting(String name, boolean isBool, boolean b, double v, double min, double max, double step) {
        this.name = name;
        this.isBool = isBool;
        this.b = b;
        this.v = v;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public static Setting bool(String name, boolean def) {
        return new Setting(name, true, def, 0, 0, 0, 0);
    }

    public static Setting num(String name, double def, double min, double max, double step) {
        return new Setting(name, false, false, def, min, max, step);
    }

    public boolean on() {
        return b;
    }

    public double get() {
        return v;
    }

    public int asInt() {
        return (int) Math.round(v);
    }

    /** dir = +1 oder -1; bei Schaltern wird einfach umgeschaltet. */
    public void change(int dir) {
        if (isBool) {
            b = !b;
        } else {
            set(v + dir * step);
        }
    }

    public void set(double value) {
        double r = Math.round(value / step) * step;
        v = Math.max(min, Math.min(max, Math.round(r * 1000.0) / 1000.0));
    }

    public String display() {
        if (isBool) {
            return b ? "AN" : "AUS";
        }
        if (step >= 1 || v == Math.rint(v)) {
            return String.valueOf((long) Math.rint(v));
        }
        return String.format(java.util.Locale.ROOT, "%.2f", v).replaceAll("0+$", "");
    }
}
