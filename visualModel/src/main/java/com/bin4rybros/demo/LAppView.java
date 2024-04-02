/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

package com.bin4rybros.demo;

import com.bin4rybros.sdk.cubism.framework.math.CubismMatrix44;
import com.bin4rybros.sdk.cubism.framework.math.CubismViewMatrix;
import com.bin4rybros.sdk.cubism.framework.rendering.android.CubismOffscreenSurfaceAndroid;

public class LAppView {
    /**
     * Rendering destination for LAppModel
     */
    public enum RenderingTarget {
        NONE,   // Render to the default frame buffer
        MODEL_FRAME_BUFFER,     // Render to the frame buffer each owned by LAppModelForSmallDemo
        VIEW_FRAME_BUFFER  // Render to the frame buffer owned by LAppViewForSmallDemo
    }

    public LAppView() {
        clearColor[0] = 1.0f;
        clearColor[1] = 1.0f;
        clearColor[2] = 1.0f;
        clearColor[3] = 0.0f;
    }

    // Initialize the shader
    public void initializeShader() {
        programId = LAppDelegate.getInstance().createShader();
    }

    // Initialize the view
    public void initialize() {
        int width = LAppDelegate.getInstance().getWindowWidth();
        int height = LAppDelegate.getInstance().getWindowHeight();

        float ratio = (float) width / (float) height;
        float left = -ratio;
        float right = ratio;
        float bottom = LAppDefine.LogicalView.LEFT.getValue();
        float top = LAppDefine.LogicalView.RIGHT.getValue();

        // The screen range corresponding to the device. Left end of X, right end of X, bottom end of Y, top end of Y
        viewMatrix.setScreenRect(left, right, bottom, top);
        viewMatrix.scale(LAppDefine.Scale.DEFAULT.getValue(), LAppDefine.Scale.DEFAULT.getValue());

        // Initialize to the identity matrix
        deviceToScreen.loadIdentity();

        if (width > height) {
            float screenW = Math.abs(right - left);
            deviceToScreen.scaleRelative(screenW / width, -screenW / width);
        } else {
            float screenH = Math.abs(top - bottom);
            deviceToScreen.scaleRelative(screenH / height, -screenH / height);
        }
        deviceToScreen.translateRelative(-width * 0.5f, -height * 0.5f);

        // Set the display range
        viewMatrix.setMaxScale(LAppDefine.Scale.MAX.getValue());   // Maximum expansion rate
        viewMatrix.setMinScale(LAppDefine.Scale.MIN.getValue());   // Minimum reduction rate

        // Maximum range that can be displayed
        viewMatrix.setMaxScreenRect(
                LAppDefine.MaxLogicalView.LEFT.getValue(),
                LAppDefine.MaxLogicalView.RIGHT.getValue(),
                LAppDefine.MaxLogicalView.BOTTOM.getValue(),
                LAppDefine.MaxLogicalView.TOP.getValue()
        );
    }

    // Initialize the image
    public void initializeSprite() {
        int windowWidth = LAppDelegate.getInstance().getWindowWidth();
        int windowHeight = LAppDelegate.getInstance().getWindowHeight();

        LAppTextureManager textureManager = LAppDelegate.getInstance().getTextureManager();

//        // Load the background image
//        LAppTextureManager.TextureInfo backgroundTexture = textureManager.createTextureFromPngFile(LAppDefine.ResourcePath.ROOT.getPath() + LAppDefine.ResourcePath.BACK_IMAGE.getPath());
//
//
//        // x, y are the center coordinates of the image
//        float x = windowWidth * 0.5f;
//        float y = windowHeight * 0.5f;
//        float fWidth = backgroundTexture.width * 2.0f;
//        float fHeight = windowHeight * 0.95f;
//
//        if (backSprite == null) {
//            backSprite = new LAppSprite(x, y, fWidth, fHeight, backgroundTexture.id, programId);
//        } else {
//            backSprite.resize(x, y, fWidth, fHeight);
//        }

        // Load the gear image
        LAppTextureManager.TextureInfo gearTexture = textureManager.createTextureFromPngFile(LAppDefine.ResourcePath.ROOT.getPath() + LAppDefine.ResourcePath.GEAR_IMAGE.getPath());


        float x = windowWidth - gearTexture.width * 0.5f - 96.f;
        float y = windowHeight - gearTexture.height * 0.5f;
        float fWidth = (float) gearTexture.width;
        float fHeight = (float) gearTexture.height;

        if (gearSprite == null) {
            gearSprite = new LAppSprite(x, y, fWidth, fHeight, gearTexture.id, programId);
        } else {
            gearSprite.resize(x, y, fWidth, fHeight);
        }

//        // Load the power image
//        LAppTextureManager.TextureInfo powerTexture = textureManager.createTextureFromPngFile(LAppDefine.ResourcePath.ROOT.getPath() + LAppDefine.ResourcePath.POWER_IMAGE.getPath());
//
//
//        x = windowWidth - powerTexture.width * 0.5f - 96.0f;
//        y = powerTexture.height * 0.5f;
//        fWidth = (float) powerTexture.width;
//        fHeight = (float) powerTexture.height;
//
//        if (powerSprite == null) {
//            powerSprite = new LAppSprite(x, y, fWidth, fHeight, powerTexture.id, programId);
//        } else {
//            powerSprite.resize(x, y, fWidth, fHeight);
//        }

        // Size covering the entire screen
        x = windowWidth * 0.5f;
        y = windowHeight * 0.5f;

        if (renderingSprite == null) {
            renderingSprite = new LAppSprite(x, y, windowWidth, windowHeight, 0, programId);
        } else {
            renderingSprite.resize(x, y, windowWidth, windowHeight);
        }
    }

    // Render
    public void render() {
        // Render UI and background
//        backSprite.render();
        gearSprite.render();
//        powerSprite.render();

        if (isChangedModel) {
            isChangedModel = false;
            LAppLive2DManager.getInstance().nextScene();
        }

        // Render the model
        LAppLive2DManager live2dManager = LAppLive2DManager.getInstance();
        live2dManager.onUpdate();

        // If each model has a rendering target as a texture
        if (renderingTarget == RenderingTarget.MODEL_FRAME_BUFFER && renderingSprite != null) {
            final float[] uvVertex = {
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f
            };

            for (int i = 0; i < live2dManager.getModelNum(); i++) {
                LAppModel model = live2dManager.getModel(i);
                float alpha = i < 1 ? 1.0f : model.getOpacity(); // Get opacity for one side only

                renderingSprite.setColor(1.0f, 1.0f, 1.0f, alpha);

                if (model != null) {
                    renderingSprite.renderImmediate(model.getRenderingBuffer().getColorBuffer()[0], uvVertex);
                }
            }
        }
    }

    /**
     * Called just before rendering one model.
     *
     * @param refModel model data
     */
    public void preModelDraw(LAppModel refModel) {
        // Framebuffer used for rendering to another rendering target
        CubismOffscreenSurfaceAndroid useTarget;

        // If rendering to another rendering target
        if (renderingTarget != RenderingTarget.NONE) {

            // Use target
            useTarget = (renderingTarget == RenderingTarget.VIEW_FRAME_BUFFER)
                    ? renderingBuffer
                    : refModel.getRenderingBuffer();

            // Create target if not created internally
            if (!useTarget.isValid()) {
                int width = LAppDelegate.getInstance().getWindowWidth();
                int height = LAppDelegate.getInstance().getWindowHeight();

                // Model drawing canvas
                useTarget.createOffscreenFrame((int) width, (int) height, null);
            }
            // Start rendering
            useTarget.beginDraw(null);
            useTarget.clear(clearColor[0], clearColor[1], clearColor[2], clearColor[3]); // Clear background color
        }
    }

    /**
     * Called just after rendering one model.
     *
     * @param refModel model data
     */
    public void postModelDraw(LAppModel refModel) {
        CubismOffscreenSurfaceAndroid useTarget = null;

        // If rendering to another rendering target
        if (renderingTarget != RenderingTarget.NONE) {
            // Use target
            useTarget = (renderingTarget == RenderingTarget.VIEW_FRAME_BUFFER)
                    ? renderingBuffer
                    : refModel.getRenderingBuffer();

            // End rendering
            useTarget.endDraw();

            // If using the framebuffer of LAppView, rendering to the sprite occurs here
            if (renderingTarget == RenderingTarget.VIEW_FRAME_BUFFER && renderingSprite != null) {
                final float[] uvVertex = {
                        1.0f, 1.0f,
                        0.0f, 1.0f,
                        0.0f, 0.0f,
                        1.0f, 0.0f
                };
                renderingSprite.setColor(1.0f, 1.0f, 1.0f, getSpriteAlpha(0));
                renderingSprite.renderImmediate(useTarget.getColorBuffer()[0], uvVertex);
            }
        }
    }

    /**
     * Switch rendering target.
     *
     * @param targetType rendering target
     */
    public void switchRenderingTarget(RenderingTarget targetType) {
        renderingTarget = targetType;
    }

    /**
     * Called when touched.
     *
     * @param pointX screen X coordinate
     * @param pointY screen Y coordinate
     */
    public void onTouchesBegan(float pointX, float pointY) {
        touchManager.touchesBegan(pointX, pointY);
    }

    /**
     * Called when pointer moves while touched.
     *
     * @param pointX screen X coordinate
     * @param pointY screen Y coordinate
     */
    public void onTouchesMoved(float pointX, float pointY) {
        float viewX = transformViewX(touchManager.getLastX());
        float viewY = transformViewY(touchManager.getLastY());

        touchManager.touchesMoved(pointX, pointY);

        LAppLive2DManager.getInstance().onDrag(viewX, viewY);
    }

    /**
     * Called when touch ends.
     *
     * @param pointX screen X coordinate
     * @param pointY screen Y coordinate
     */
    public void onTouchesEnded(float pointX, float pointY) {
        // Touch ends
        LAppLive2DManager live2DManager = LAppLive2DManager.getInstance();
        live2DManager.onDrag(0.0f, 0.0f);

        // Single tap
        // Get coordinates transformed to logical coordinates
        float x = deviceToScreen.transformX(touchManager.getLastX());
        // Get coordinates transformed to logical coordinates
        float y = deviceToScreen.transformY(touchManager.getLastY());

        if (LAppDefine.DEBUG_TOUCH_LOG_ENABLE) {
            LAppPal.printLog("Touches ended x: " + x + ", y:" + y);
        }

        live2DManager.onTap(x, y);

        // Check if touched gear button
        if (gearSprite.isHit(pointX, pointY)) {
            isChangedModel = true;
        }

//        // Check if touched power button
//        if (powerSprite.isHit(pointX, pointY)) {
//            // Terminate the application
//            LAppDelegate.getInstance().deactivateApp();
//        }

    }

    /**
     * Convert X coordinate to View coordinate.
     *
     * @param deviceX device X coordinate
     * @return View X coordinate
     */
    public float transformViewX(float deviceX) {
        // Get coordinates transformed to logical coordinates
        float screenX = deviceToScreen.transformX(deviceX);
        // Value after scaling, shrinking, and moving
        return viewMatrix.invertTransformX(screenX);
    }

    /**
     * Convert Y coordinate to View coordinate.
     *
     * @param deviceY device Y coordinate
     * @return View Y coordinate
     */
    public float transformViewY(float deviceY) {
        // Get coordinates transformed to logical coordinates
        float screenY = deviceToScreen.transformY(deviceY);
        // Value after scaling, shrinking, and moving
        return viewMatrix.invertTransformX(screenY);
    }

    /**
     * Convert X coordinate to Screen coordinate.
     *
     * @param deviceX device X coordinate
     * @return Screen X coordinate
     */
    public float transformScreenX(float deviceX) {
        return deviceToScreen.transformX(deviceX);
    }

    /**
     * Convert Y coordinate to Screen coordinate.
     *
     * @param deviceY device Y coordinate
     * @return Screen Y coordinate
     */
    public float transformScreenY(float deviceY) {
        return deviceToScreen.transformX(deviceY);
    }

    /**
     * Set background clear color when switching rendering target to non-default.
     *
     * @param r red (0.0~1.0)
     * @param g green (0.0~1.0)
     * @param b blue (0.0~1.0)
     */
    public void setRenderingTargetClearColor(float r, float g, float b) {
        clearColor[0] = r;
        clearColor[1] = g;
        clearColor[2] = b;
    }

    /**
     * Determine alpha at the time of rendering in a sample where models are drawn to another rendering target.
     *
     * @param assign
     * @return
     */
    public float getSpriteAlpha(int assign) {
        // Add an arbitrary difference depending on the value of assign
        float alpha = 0.25f + (float) assign * 0.5f;

        // Add an arbitrary difference to alpha as a sample
        if (alpha > 1.0f) {
            alpha = 1.0f;
        }
        if (alpha < 0.1f) {
            alpha = 0.1f;
        }
        return alpha;
    }

    /**
     * Return rendering target enum instance.
     *
     * @return rendering target
     */
    public RenderingTarget getRenderingTarget() {
        return renderingTarget;
    }

    private final CubismMatrix44 deviceToScreen = CubismMatrix44.create(); // Matrix for converting from device coordinates to screen coordinates
    private final CubismViewMatrix viewMatrix = new CubismViewMatrix();   // Matrix for scaling, shrinking, and moving the screen display
    private int programId;
    private int windowWidth;
    private int windowHeight;

    /**
     * Rendering target options
     */
    private RenderingTarget renderingTarget = RenderingTarget.NONE;
    /**
     * Clear color of rendering target
     */
    private final float[] clearColor = new float[4];

    private CubismOffscreenSurfaceAndroid renderingBuffer = new CubismOffscreenSurfaceAndroid();

    private LAppSprite backSprite;
    private LAppSprite gearSprite;
//    private LAppSprite powerSprite;
    private LAppSprite renderingSprite;

    /**
     * Model switching flag
     */
    private boolean isChangedModel;

    private final TouchManager touchManager = new TouchManager();
}
