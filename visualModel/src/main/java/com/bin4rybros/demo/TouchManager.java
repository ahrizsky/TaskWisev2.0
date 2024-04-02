/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

package com.bin4rybros.demo;

/**
 * Touch Manager
 */
public class TouchManager {
    /**
     * Event when touch begins
     *
     * @param deviceX X value of the touched screen
     * @param deviceY Y value of the touched screen
     */
    public void touchesBegan(float deviceX, float deviceY) {
        lastX = deviceX;
        lastY = deviceY;

        startX = deviceX;
        startY = deviceY;

        lastTouchDistance = -1.0f;

        isFlipAvailable = true;
        isTouchSingle = true;
    }

    /**
     * Event during drag
     *
     * @param deviceX X value of the touched screen
     * @param deviceY Y value of the touched screen
     */
    public void touchesMoved(float deviceX, float deviceY) {
        lastX = deviceX;
        lastY = deviceY;
        lastTouchDistance = -1.0f;
        isTouchSingle = true;
    }

    /**
     * Event during drag
     *
     * @param deviceX1 X value of the first touched screen
     * @param deviceY1 Y value of the first touched screen
     * @param deviceX2 X value of the second touched screen
     * @param deviceY2 Y value of the second touched screen
     */
    public void touchesMoved(float deviceX1, float deviceY1, float deviceX2, float deviceY2) {
        float distance = calculateDistance(deviceX1, deviceY1, deviceX2, deviceY2);
        float centerX = (deviceX1 + deviceX2) * 0.5f;
        float centerY = (deviceY1 + deviceY2) * 0.5f;

        if (lastTouchDistance > 0.0f) {
            scale = (float) Math.pow(distance / lastTouchDistance, 0.75f);
            deltaX = calculateMovingAmount(deviceX1 - lastX1, deviceX2 - lastX2);
            deltaY = calculateMovingAmount(deviceY1 - lastY1, deviceY2 - lastY2);
        } else {
            scale = 1.0f;
            deltaX = 0.0f;
            deltaY = 0.0f;
        }

        lastX = centerX;
        lastY = centerY;
        lastX1 = deviceX1;
        lastY1 = deviceY1;
        lastX2 = deviceX2;
        lastY2 = deviceY2;
        lastTouchDistance = distance;
        isTouchSingle = false;
    }

    /**
     * Measure the distance of a flick
     *
     * @return Flick distance
     */
    public float calculateGetFlickDistance() {
        return calculateDistance(startX, startY, lastX, lastY);
    }

    // ----- getter methods -----
    public float getStartX() {
        return startX;
    }

    public float getStartY() {
        return startY;
    }

    public float getLastX() {
        return lastX;
    }

    public float getLastY() {
        return lastY;
    }

    public float getLastX1() {
        return lastX1;
    }

    public float getLastY1() {
        return lastY1;
    }

    public float getLastX2() {
        return lastX2;
    }

    public float getLastY2() {
        return lastY2;
    }

    public float getLastTouchDistance() {
        return lastTouchDistance;
    }

    public float getDeltaX() {
        return deltaX;
    }

    public float getDeltaY() {
        return deltaY;
    }

    public float getScale() {
        return scale;
    }

    public boolean isTouchSingle() {
        return isTouchSingle;
    }

    public boolean isFlipAvailable() {
        return isFlipAvailable;
    }

    /**
     * Calculate the distance between point 1 and point 2
     *
     * @param x1 X value of the first touched screen
     * @param y1 Y value of the first touched screen
     * @param x2 X value of the second touched screen
     * @param y2 Y value of the second touched screen
     * @return Distance between the two points
     */
    private float calculateDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    /**
     * Calculate the amount of movement from two values
     * If in different directions, the movement amount is 0. If in the same direction, refer to the value with the smaller absolute value.
     *
     * @param v1 First movement amount
     * @param v2 Second movement amount
     * @return Smaller movement amount
     */
    private float calculateMovingAmount(float v1, float v2) {
        if ((v1 > 0.0f) != (v2 > 0.0f)) {
            return 0.0f;
        }

        float sign = v1 > 0.0f ? 1.0f : -1.0f;
        float absoluteValue1 = Math.abs(v1);
        float absoluteValue2 = Math.abs(v2);

        return sign * (Math.min(absoluteValue1, absoluteValue2));
    }

    /**
     * X value at the beginning of touch
     */
    private float startX;
    /**
     * Y value at the beginning of touch
     */
    private float startY;
    /**
     * X value during single touch
     */
    private float lastX;
    /**
     * Y value during single touch
     */
    private float lastY;
    /**
     * X value of the first touch during double touch
     */
    private float lastX1;
    /**
     * Y value of the first touch during double touch
     */
    private float lastY1;
    /**
     * X value of the second touch during double touch
     */
    private float lastX2;
    /**
     * Y value of the second touch during double touch
     */
    private float lastY2;
    /**
     * Distance between fingers when touched by two or more fingers
     */
    private float lastTouchDistance;
    /**
     * X movement distance from the last value to the current value
     */
    private float deltaX;
    /**
     * Y movement distance from the last value to the current value
     */
    private float deltaY;
    /**
     * Scale factor to be applied in this frame. 1 when not scaling.
     */
    private float scale;
    /**
     * True when single touch
     */
    private boolean isTouchSingle;
    /**
     * Whether flip is available or not
     */
    private boolean isFlipAvailable;
}
