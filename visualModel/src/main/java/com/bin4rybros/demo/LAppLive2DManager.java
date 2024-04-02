/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

package com.bin4rybros.demo;

import com.bin4rybros.sdk.cubism.framework.math.CubismMatrix44;
import com.bin4rybros.sdk.cubism.framework.motion.ACubismMotion;
import com.bin4rybros.sdk.cubism.framework.motion.IFinishedMotionCallback;

import java.util.ArrayList;
import java.util.List;

import static com.bin4rybros.demo.LAppDefine.*;

/**
 * A class that manages CubismModel in the sample application.
 * Manages model creation and destruction, tap event processing, and model switching.
 */
public class LAppLive2DManager {
    public static LAppLive2DManager getInstance() {
        if (s_instance == null) {
            s_instance = new LAppLive2DManager();
        }
        return s_instance;
    }

    public static void releaseInstance() {
        s_instance = null;
    }

    /**
     * Release all models currently held in the scene
     */
    public void releaseAllModel() {
        for (LAppModel model : models) {
            model.deleteModel();
        }
        models.clear();
    }

    // Perform model update and rendering processing
    public void onUpdate() {
        int width = LAppDelegate.getInstance().getWindowWidth();
        int height = LAppDelegate.getInstance().getWindowHeight();

        for (int i = 0; i < models.size(); i++) {
            LAppModel model = models.get(i);

            if (model.getModel() == null) {
                LAppPal.printLog("Failed to model.getModel().");
                continue;
            }

            projection.loadIdentity();

            if (model.getModel().getCanvasWidth() > 1.0f && width < height) {
                // When displaying a long model in a tall window, calculate the scale based on the model's width
                model.getModelMatrix().setWidth(2.0f);
                projection.scale(1.0f, (float) width / (float) height);
            } else {
                projection.scale((float) height / (float) width, 1.0f);
            }

            // Multiply if necessary
            if (viewMatrix != null) {
                projection.multiplyByMatrix(viewMatrix);
            }

            // Call before drawing one model
            LAppDelegate.getInstance().getView().preModelDraw(model);

            model.update();

            model.draw(projection);     // projection is passed by reference and will be changed

            // Call after drawing one model
            LAppDelegate.getInstance().getView().postModelDraw(model);
        }
    }

    /**
     * Process when dragging the screen
     *
     * @param x screen x coordinate
     * @param y screen y coordinate
     */
    public void onDrag(float x, float y) {
        for (int i = 0; i < models.size(); i++) {
            LAppModel model = getModel(i);
            model.setDragging(x, y);
        }
    }

    /**
     * Process when tapping the screen
     *
     * @param x screen x coordinate
     * @param y screen y coordinate
     */
    public void onTap(float x, float y) {
        if (DEBUG_LOG_ENABLE) {
            LAppPal.printLog("tap point: {" + x + ", y: " + y);
        }

        for (int i = 0; i < models.size(); i++) {
            LAppModel model = models.get(i);

            // If the head is tapped, play a random expression
            if (model.hitTest(HitAreaName.HEAD.getId(), x, y)) {
                if (DEBUG_LOG_ENABLE) {
                    LAppPal.printLog("hit area: " + HitAreaName.HEAD.getId());
                }
                model.setRandomExpression();
            }
            // If the body is tapped, start a random motion
            else if (model.hitTest(HitAreaName.BODY.getId(), x, y)) {
                if (DEBUG_LOG_ENABLE) {
                    LAppPal.printLog("hit area: " + HitAreaName.HEAD.getId());
                }

                model.startRandomMotion(MotionGroup.TAP_BODY.getId(), Priority.NORMAL.getPriority(), finishedMotion);
            }
        }
    }

    /**
     * Switch to the next scene
     * In the sample application, switch model sets.
     */
    public void nextScene() {
        final int number = (currentModel.getOrder() + 1) % ModelDir.values().length;

        changeScene(number);
    }

    /**
     * Switch scenes
     *
     * @param index index of the scene to switch to
     */
    public void changeScene(int index) {
        currentModel = ModelDir.values()[index];
        if (DEBUG_LOG_ENABLE) {
            LAppPal.printLog("model index: " + currentModel.getOrder());
        }

        String modelDirName = currentModel.getDirName();
        String modelPath = ResourcePath.ROOT.getPath() + modelDirName + "/";
        String modelJsonName = currentModel.getDirName() + ".model3.json";

        releaseAllModel();

        models.add(new LAppModel());
        models.get(0).loadAssets(modelPath, modelJsonName);

        /*
         * Present a sample of displaying the model semi-transparently.
         * If USE_RENDER_TARGET and USE_MODEL_RENDER_TARGET are defined here
         * Draw the model to another rendering target and paste the drawing result to another sprite as a texture.
         */
        LAppView.RenderingTarget useRenderingTarget;
        if (USE_RENDER_TARGET) {
            // Select this if drawing to the target owned by LAppView
            useRenderingTarget = LAppView.RenderingTarget.VIEW_FRAME_BUFFER;
        } else if (USE_MODEL_RENDER_TARGET) {
            // Select this if drawing to the target owned by each LAppModel
            useRenderingTarget = LAppView.RenderingTarget.MODEL_FRAME_BUFFER;
        } else {
            // Render to the default main frame buffer (normal)
            useRenderingTarget = LAppView.RenderingTarget.NONE;
        }

        if (USE_RENDER_TARGET || USE_MODEL_RENDER_TARGET) {
            // As a sample of giving alpha individually to the model, create another model and shift its position a little.
            models.add(new LAppModel());
            models.get(1).loadAssets(modelPath, modelJsonName);
            models.get(1).getModelMatrix().translateX(0.2f);
        }

        // Switch the rendering target
        LAppDelegate.getInstance().getView().switchRenderingTarget(useRenderingTarget);

        // Background clear color when selecting another rendering target
        float[] clearColor = {1.0f, 1.0f, 1.0f};
        LAppDelegate.getInstance().getView().setRenderingTargetClearColor(clearColor[0], clearColor[1], clearColor[2]);
    }

    /**
     * Return the model held in the current scene
     *
     * @param number index value of the model list
     * @return return an instance of the model. If the index value is out of range, return null
     */
    public LAppModel getModel(int number) {
        if (number < models.size()) {
            return models.get(number);
        }
        return null;
    }

    /**
     * Return the scene index
     *
     * @return scene index
     */
    public ModelDir getCurrentModel() {
        return currentModel;
    }

    /**
     * Return the number of models in this LAppLive2DManager instance has.
     *
     * @return number fo models in this LAppLive2DManager instance has. If models list is null, return 0.
     */
    public int getModelNum() {
        if (models == null) {
            return 0;
        }
        return models.size();
    }

    /**
     * Callback function executed when motion is finished
     */
    private static class FinishedMotion implements IFinishedMotionCallback {
        @Override
        public void execute(ACubismMotion motion) {
            LAppPal.printLog("Motion Finished: " + motion);
        }
    }

    private static final FinishedMotion finishedMotion = new FinishedMotion();

    /**
     * Singleton instance
     */
    private static LAppLive2DManager s_instance;

    private LAppLive2DManager() {
        currentModel = ModelDir.values()[0];
        changeScene(currentModel.getOrder());
    }

    private final List<LAppModel> models = new ArrayList<LAppModel>();

    /**
     * Index value of the scene to display
     */
    private ModelDir currentModel;

    // Cache variables used in the onUpdate method
    private final CubismMatrix44 viewMatrix = CubismMatrix44.create();
    private final CubismMatrix44 projection = CubismMatrix44.create();
}
