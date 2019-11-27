package com.inertia.phyzmo.datadisplay.models;

public class PointModel {
    public float x;
    public float y;

    public PointModel(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(PointModel p) {
        return this.x == p.x && this.y == p.y;
    }
}
