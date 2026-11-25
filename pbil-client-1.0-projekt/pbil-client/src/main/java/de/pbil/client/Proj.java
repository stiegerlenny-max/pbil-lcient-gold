package de.pbil.client;

/** Reine Mathe: Weltpunkt -> Bildschirmpunkt (GUI-Koordinaten). Keine Minecraft-Klassen, damit sie testbar bleibt. */
public final class Proj {
    private final double cx, cy, cz;
    private final double fx, fy, fz;
    private final double rx, rz;
    private final double ux, uy, uz;
    private final double tanHalf;
    private final int sw, sh;

    public Proj(double camX, double camY, double camZ, double yawDeg, double pitchDeg, double fovDeg, int sw, int sh) {
        this.cx = camX;
        this.cy = camY;
        this.cz = camZ;
        double yaw = Math.toRadians(yawDeg), pitch = Math.toRadians(pitchDeg);
        double sy = Math.sin(yaw), cyw = Math.cos(yaw), sp = Math.sin(pitch), cp = Math.cos(pitch);
        fx = -sy * cp;
        fy = -sp;
        fz = cyw * cp;
        rx = -cyw;
        rz = -sy;
        ux = -sy * sp;
        uy = cp;
        uz = cyw * sp;
        this.tanHalf = Math.tan(Math.toRadians(fovDeg) / 2.0);
        this.sw = sw;
        this.sh = sh;
    }

    /** {x, y} auf dem Bildschirm oder null, wenn der Punkt hinter der Kamera liegt. */
    public double[] project(double x, double y, double z) {
        double dx = x - cx, dy = y - cy, dz = z - cz;
        double depth = dx * fx + dy * fy + dz * fz;
        if (depth <= 0.05) {
            return null;
        }
        double right = dx * rx + dz * rz;
        double up = dx * ux + dy * uy + dz * uz;
        double aspect = (double) sw / sh;
        double sx = sw / 2.0 + right / (depth * tanHalf * aspect) * (sw / 2.0);
        double sy = sh / 2.0 - up / (depth * tanHalf) * (sh / 2.0);
        return new double[] {sx, sy};
    }

    /** Umrandendes Rechteck {minX, minY, maxX, maxY} der 8 Ecken einer Box, null wenn eine Ecke hinter der Kamera liegt. */
    public double[] box(double x0, double y0, double z0, double x1, double y1, double z1) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            double[] p = project((i & 1) == 0 ? x0 : x1, (i & 2) == 0 ? y0 : y1, (i & 4) == 0 ? z0 : z1);
            if (p == null) {
                return null;
            }
            minX = Math.min(minX, p[0]);
            maxX = Math.max(maxX, p[0]);
            minY = Math.min(minY, p[1]);
            maxY = Math.max(maxY, p[1]);
        }
        return new double[] {minX, minY, maxX, maxY};
    }
}
