/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

package com.bin4rybros.demo;

import com.bin4rybros.sdk.cubism.framework.CubismDefaultParameterId;
import com.bin4rybros.sdk.cubism.framework.CubismFramework;
import com.bin4rybros.sdk.cubism.framework.CubismModelSettingJson;
import com.bin4rybros.sdk.cubism.framework.ICubismModelSetting;
import com.bin4rybros.sdk.cubism.framework.effect.CubismBreath;
import com.bin4rybros.sdk.cubism.framework.effect.CubismEyeBlink;
import com.bin4rybros.sdk.cubism.framework.id.CubismId;
import com.bin4rybros.sdk.cubism.framework.id.CubismIdManager;
import com.bin4rybros.sdk.cubism.framework.math.CubismMatrix44;
import com.bin4rybros.sdk.cubism.framework.model.CubismMoc;
import com.bin4rybros.sdk.cubism.framework.model.CubismUserModel;
import com.bin4rybros.sdk.cubism.framework.motion.ACubismMotion;
import com.bin4rybros.sdk.cubism.framework.motion.CubismExpressionMotion;
import com.bin4rybros.sdk.cubism.framework.motion.CubismMotion;
import com.bin4rybros.sdk.cubism.framework.motion.IFinishedMotionCallback;
import com.bin4rybros.sdk.cubism.framework.rendering.CubismRenderer;
import com.bin4rybros.sdk.cubism.framework.rendering.android.CubismOffscreenSurfaceAndroid;
import com.bin4rybros.sdk.cubism.framework.rendering.android.CubismRendererAndroid;
import com.bin4rybros.sdk.cubism.framework.utils.CubismDebug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class LAppModel extends CubismUserModel {

    ScheduledExecutorService schedulerRandomMotion = Executors.newScheduledThreadPool(1);
    private static final Map<Integer, Float> LIP_SYNC_VALUES = new HashMap<>();
    private static final Map<Integer, Float> LIP_FORM_VALUES = new HashMap<>();

    static {
        // Initialize lip sync values
        LIP_SYNC_VALUES.put(0, 0.0f);
        LIP_SYNC_VALUES.put(1, 0.6f);
        LIP_SYNC_VALUES.put(2, 0.7f);
        LIP_SYNC_VALUES.put(3, 1.0f);
        LIP_SYNC_VALUES.put(4, 0.4f);
        LIP_SYNC_VALUES.put(5, 0.5f);
        LIP_SYNC_VALUES.put(6, 0.2f);
        LIP_SYNC_VALUES.put(7, 1.0f);
        LIP_SYNC_VALUES.put(8, 0.9f);
        LIP_SYNC_VALUES.put(9, 0.9f);
        LIP_SYNC_VALUES.put(10, 1.0f);
        LIP_SYNC_VALUES.put(11, 0.5f);
        LIP_SYNC_VALUES.put(12, 0.5f);
        LIP_SYNC_VALUES.put(13, 0.5f);
        LIP_SYNC_VALUES.put(14, 0.5f);
        LIP_SYNC_VALUES.put(15, 0.1f);
        LIP_SYNC_VALUES.put(16, 0.3f);
        LIP_SYNC_VALUES.put(17, 0.5f);
        LIP_SYNC_VALUES.put(18, 0.1f);
        LIP_SYNC_VALUES.put(19, 0.3f);
        LIP_SYNC_VALUES.put(20, 0.6f);
        LIP_SYNC_VALUES.put(21, 0.0f);

        // Initialize lip form values
        LIP_FORM_VALUES.put(0, 0.8f);
        LIP_FORM_VALUES.put(1, 0.8f);
        LIP_FORM_VALUES.put(2, 0.5f);
        LIP_FORM_VALUES.put(3, -0.5f);
        LIP_FORM_VALUES.put(4, 1.0f);
        LIP_FORM_VALUES.put(5, 1.0f);
        LIP_FORM_VALUES.put(6, 1.0f);
        LIP_FORM_VALUES.put(7, -1.0f);
        LIP_FORM_VALUES.put(8, -0.5f);
        LIP_FORM_VALUES.put(9, -0.1f);
        LIP_FORM_VALUES.put(10, -0.3f);
        LIP_FORM_VALUES.put(11, 0.8f);
        LIP_FORM_VALUES.put(12, 1.0f);
        LIP_FORM_VALUES.put(13, -0.8f);
        LIP_FORM_VALUES.put(14, 0.1f);
        LIP_FORM_VALUES.put(15, 0.5f);
        LIP_FORM_VALUES.put(16, -0.9f);
        LIP_FORM_VALUES.put(17, 0.5f);
        LIP_FORM_VALUES.put(18, 0.4f);
        LIP_FORM_VALUES.put(19, -0.3f);
        LIP_FORM_VALUES.put(20, 0.4f);
        LIP_FORM_VALUES.put(21, 0.5f);
    }

    public LAppModel() {
        if (LAppDefine.MOC_CONSISTENCY_VALIDATION_ENABLE) {
            mocConsistency = true;
        }

        if (LAppDefine.DEBUG_LOG_ENABLE) {
            debugMode = true;
        }

        CubismIdManager idManager = CubismFramework.getIdManager();

        idParamAngleX = idManager.getId(CubismDefaultParameterId.ParameterId.ANGLE_X.getId());
        idParamAngleY = idManager.getId(CubismDefaultParameterId.ParameterId.ANGLE_Y.getId());
        idParamAngleZ = idManager.getId(CubismDefaultParameterId.ParameterId.ANGLE_Z.getId());
        idParamBodyAngleX = idManager.getId(CubismDefaultParameterId.ParameterId.BODY_ANGLE_X.getId());
        idParamBodyAngleY = idManager.getId(CubismDefaultParameterId.ParameterId.BODY_ANGLE_Y.getId());
        idParamEyeBallX = idManager.getId(CubismDefaultParameterId.ParameterId.EYE_BALL_X.getId());
        idParamEyeBallY = idManager.getId(CubismDefaultParameterId.ParameterId.EYE_BALL_Y.getId());
        idParamBreath = idManager.getId(CubismDefaultParameterId.ParameterId.BREATH.getId());
    }

    public void loadAssets(final String dir, final String fileName) {
        if (LAppDefine.DEBUG_LOG_ENABLE) {
            LAppPal.printLog("load model setting: " + fileName);
        }

        modelHomeDirectory = dir;
        String filePath = modelHomeDirectory + fileName;

        // json読み込み
        byte[] buffer = createBuffer(filePath);

        ICubismModelSetting setting = new CubismModelSettingJson(buffer);

        // Setup model
        setupModel(setting);

        if (model == null) {
            LAppPal.printLog("Failed to loadAssets().");
            return;
        }

        // Setup renderer.
        CubismRenderer renderer = CubismRendererAndroid.create();
        setupRenderer(renderer);

        setupTextures();
    }

    /**
     * Delete the model which LAppModel has.
     */
    public void deleteModel() {
        delete();
    }

    /**
     * Updating the model. Determining the rendering state from the model parameters.
     */
    public void update() {
        final float deltaTimeSeconds = LAppPal.getDeltaTime();
        userTimeSeconds += deltaTimeSeconds;

        dragManager.update(deltaTimeSeconds);
        dragX = dragManager.getX();
        dragY = dragManager.getY();

        // モーションによるパラメーター更新の有無
        boolean isMotionUpdated = false;

//         Load the previously saved state.
        model.loadParameters();

        // If there is no motion playback, play randomly from the idle motions.
        if (motionManager.isFinished()) {
            startRandomMotion(LAppDefine.MotionGroup.IDLE.getId(), LAppDefine.Priority.IDLE.getPriority());
//            schedulerRandomMotion.schedule(() -> {
//                startRandomMotion(LAppDefine.MotionGroup.IDLE.getId(), LAppDefine.Priority.IDLE.getPriority());
//            }, 50, TimeUnit.SECONDS);
        } else {
            // Update the motion.
            isMotionUpdated = motionManager.updateMotion(model, deltaTimeSeconds);
        }

        // Save the model's state.
        model.saveParameters();

        // Opacity
        opacity = model.getModelOpacity();

        // eye blink
        // Blink only when there is no update for the main motion.
//        if (!isMotionUpdated) {
            if (eyeBlink != null) {
                eyeBlink.updateParameters(model, deltaTimeSeconds);
            }
//        }

        // expression
        if (expressionManager != null) {
            // 表情でパラメータ更新（相対変化）
            expressionManager.updateMotion(model, deltaTimeSeconds);
        }

        // ドラッグ追従機能
        // ドラッグによる顔の向きの調整
        model.addParameterValue(idParamAngleX, dragX * 30); // -30から30の値を加える
        model.addParameterValue(idParamAngleY, dragY * 30);
        model.addParameterValue(idParamAngleZ, dragX * dragY * (-30));

        // ドラッグによる体の向きの調整
        model.addParameterValue(idParamBodyAngleX, dragX * 20); // -10から10の値を加える
        model.addParameterValue(idParamBodyAngleY, dragY * 20);

        // ドラッグによる目の向きの調整
        model.addParameterValue(idParamEyeBallX, dragX);  // -1から1の値を加える
        model.addParameterValue(idParamEyeBallY, dragY);

        // Breath Function
        if (breath != null) {
            breath.updateParameters(model, deltaTimeSeconds);
        }

        // Physics Setting
        if (physics != null) {
            physics.evaluate(model, deltaTimeSeconds);
        }

        // Lip Sync Setting
//        if (lipSync) {
//            // リアルタイムでリップシンクを行う場合、システムから音量を取得して0~1の範囲で値を入力します
//            float value = 0.0f;
//
//            for (int i = 0; i < lipSyncIds.size(); i++) {
//                CubismId lipSyncId = lipSyncIds.get(i);
//                model.addParameterValue(lipSyncId, value, 0.8f);
//            }
//        }

        updateLipSyncValues();
        updateLipFormValues();

        // Pose Setting
        if (pose != null) {
            pose.updateParameters(model, deltaTimeSeconds);
        }

        model.update();
    }

    public static void updateLipSync(long visemeId, long audioOffset) {
        // Calculate delay based on audio offset
        long delayMilliseconds = audioOffset / 10000; // Convert ticks to milliseconds
        int visemeIdInt = (int) visemeId;

        lipSyncValue = calculateLipSyncValue(visemeIdInt);
        lipFormValue = calculateLipFormValue(visemeIdInt);

//        Log.d("LAppModel", "Viseme ID: " + visemeIdInt + ", Audio Offset: " + delayMilliseconds);
    }

    private static void updateLipSyncValues() {
        if (lipSync) {
            for (CubismId lipSyncId : lipSyncIds) {
                model.addParameterValue(lipSyncId, lipSyncValue, 1f); // Adjust the third parameter as needed
            }
        }
    }

    private static void updateLipFormValues() {
        if (lipSync) {
            for (CubismId lipFormId : lipFormIds) {
                model.addParameterValue(lipFormId, lipFormValue, 1f); // Adjust the third parameter as needed
            }
        }
    }


    private static float calculateLipSyncValue(int visemeId) {
        if (LIP_SYNC_VALUES.containsKey(visemeId)) {
            return LIP_SYNC_VALUES.get(visemeId);
        }
        return 0.0f; // Default value if key is not found
    }

    private static float calculateLipFormValue(int visemeId) {
        if (LIP_FORM_VALUES.containsKey(visemeId)) {
            return LIP_FORM_VALUES.get(visemeId);
        }
        return 0.0f; // Default value if key is not found
    }

    public void startSpecificMotion(String motionGroup, int motionNumber) {
        startMotion(motionGroup, motionNumber, LAppDefine.Priority.FORCE.getPriority());
    }

    /**
     * 引数で指定したモーションの再生を開始する。
     * コールバック関数が渡されなかった場合にそれをnullとして同メソッドを呼び出す。
     *
     * @param group モーショングループ名
     * @param number グループ内の番号
     * @param priority 優先度
     * @return 開始したモーションの識別番号を返す。個別のモーションが終了したか否かを判別するisFinished()の引数で使用する。開始できない時は「-1」
     */
    public int startMotion(final String group, int number, int priority) {
        return startMotion(group, number, priority, null);
    }

    /**
     * 引数で指定したモーションの再生を開始する。
     *
     * @param group モーショングループ名
     * @param number グループ内の番号
     * @param priority 優先度
     * @param onFinishedMotionHandler モーション再生終了時に呼び出されるコールバック関数。nullの場合は呼び出されない。
     * @return 開始したモーションの識別番号を返す。個別のモーションが終了したか否かを判定するisFinished()の引数で使用する。開始できない時は「-1」
     */
    public int startMotion(final String group,
                           int number,
                           int priority,
                           IFinishedMotionCallback onFinishedMotionHandler
    ) {
        if (priority == LAppDefine.Priority.FORCE.getPriority()) {
            motionManager.setReservationPriority(priority);
        } else if (!motionManager.reserveMotion(priority)) {
            if (debugMode) {
                LAppPal.printLog("Cannot start motion.");
            }
            return -1;
        }

        // ex) idle_0
        String name = group + "_" + number;

        CubismMotion motion = (CubismMotion) motions.get(name);

        if (motion == null) {
            String fileName = modelSetting.getMotionFileName(group, number);
            if (!fileName.equals("")) {
                String path = modelHomeDirectory + fileName;

                byte[] buffer;
                buffer = createBuffer(path);

                motion = loadMotion(buffer, onFinishedMotionHandler);
                if (motion != null) {
                    final float fadeInTime = modelSetting.getMotionFadeInTimeValue(group, number);

                    if (fadeInTime != -1.0f) {
                        motion.setFadeInTime(fadeInTime);
                    }

                    final float fadeOutTime = modelSetting.getMotionFadeOutTimeValue(group, number);
                    if (fadeOutTime != -1.0f) {
                        motion.setFadeOutTime(fadeOutTime);
                    }

                    motion.setEffectIds(eyeBlinkIds, lipSyncIds, lipFormIds);
                }
            }
        } else {
            motion.setFinishedMotionHandler(onFinishedMotionHandler);
        }

        // load sound files
        String voice = modelSetting.getMotionSoundFileName(group, number);
        if (!voice.equals("")) {
            String path = modelHomeDirectory + voice;

            // 別スレッドで音声再生
            LAppWavFileHandler voicePlayer = new LAppWavFileHandler(path);
            voicePlayer.start();
        }

        if (debugMode) {
            LAppPal.printLog("start motion: " + group + "_" + number);
        }
        return motionManager.startMotionPriority(motion, priority);
    }

    /**
     * Starts a random motion from a specified motion group with a given priority.
     *
     * @param motionGroup The name of the motion group from which to select a random motion.
     * @param priority The priority level for the motion. Higher priority motions will interrupt lower priority ones.
     * @return The identifier of the started motion. Returns -1 if the motion cannot be started.
     */
    public int startRandomMotionFromGroup(final String motionGroup, int priority) {
        if (modelSetting.getMotionCount(motionGroup) == 0) {
            return -1;
        }

        Random random = new Random();
        int motionNumber = random.nextInt(modelSetting.getMotionCount(motionGroup));
        return startMotion(motionGroup, motionNumber, priority);
    }

    /**
     * ランダムに選ばれたモーションの再生を開始する。
     *
     * @param group モーショングループ名
     * @param priority 優先度
     * @param onFinishedMotionHandler モーション再生終了時に呼び出されるコールバック関数。nullの場合は呼び出されない。
     * @return 開始したモーションの識別番号を返す。個別のモーションが終了したか否かを判定するisFinished()の引数で使用する。開始できない時は-1
     */
    public int startRandomMotion(final String group, int priority, IFinishedMotionCallback onFinishedMotionHandler) {
        if (modelSetting.getMotionCount(group) == 0) {
            return -1;
        }

        Random random = new Random();
        int number = random.nextInt(Integer.MAX_VALUE) % modelSetting.getMotionCount(group);

        return startMotion(group, number, priority, onFinishedMotionHandler);
    }

    /**
     * ランダムに選ばれたモーションの再生を開始する。
     * コールバック関数が渡されなかった場合にそれをnullとして同メソッドを呼び出す。
     *
     * @param group モーショングループ名
     * @param priority 優先度
     * @return 開始したモーションの識別番号。個別のモーションが終了したか否かを判定するisFinished()の引数で使用する。開始できない時は「-1」
     */
    public int startRandomMotion(final String group, int priority) {
        return startRandomMotion(group, priority, null);
    }

    public void draw(CubismMatrix44 matrix) {
        if (model == null) {
            return;
        }

        matrix.multiplyByMatrix(modelMatrix);

        this.<CubismRendererAndroid>getRenderer().setMvpMatrix(matrix);
        this.<CubismRendererAndroid>getRenderer().drawModel();
    }

    /**
     * 当たり判定テスト
     * 指定IDの頂点リストから矩形を計算し、座標が矩形範囲内か判定する
     *
     * @param hitAreaName 当たり判定をテストする対象のID
     * @param x 判定を行うx座標
     * @param y 判定を行うy座標
     * @return 当たっているならtrue
     */
    public boolean hitTest(final String hitAreaName, float x, float y) {
        // 透明時は当たり判定なし
        if (opacity < 1) {
            return false;
        }

        final int count = modelSetting.getHitAreasCount();
        for (int i = 0; i < count; i++) {
            if (modelSetting.getHitAreaName(i).equals(hitAreaName)) {
                final CubismId drawID = modelSetting.getHitAreaId(i);

                return isHit(drawID, x, y);
            }
        }
        // 存在しない場合はfalse
        return false;
    }

    /**
     * 引数で指定した表情モーションを設定する
     *
     * @param expressionID 表情モーションのID
     */
    public void setExpression(final String expressionID) {
        ACubismMotion motion = expressions.get(expressionID);

        if (debugMode) {
            LAppPal.printLog("expression: " + expressionID);
        }

        if (motion != null) {
            expressionManager.startMotionPriority(motion, LAppDefine.Priority.IDLE.getPriority());
        } else {
            if (debugMode) {
                LAppPal.printLog("expression " + expressionID + "is null");
            }
        }
    }

    public void setSpecificExpression(String expressionID) {
        ACubismMotion motion = expressions.get(expressionID);

        if (motion != null) {
            expressionManager.startMotionPriority(motion, LAppDefine.Priority.FORCE.getPriority());
        } else {
            if (debugMode) {
                LAppPal.printLog("expression " + expressionID + " is null");
            }
        }
    }

    /**
     * ランダムに選ばれた表情モーションを設定する
     */
    public void setRandomExpression() {
        if (expressions.size() == 0) {
            return;
        }

        Random random = new Random();
        int number = random.nextInt(Integer.MAX_VALUE) % expressions.size();

        int i = 0;
        for (String key : expressions.keySet()) {
            if (i == number) {
                setExpression(key);
                return;
            }
            i++;
        }
    }

    public CubismOffscreenSurfaceAndroid getRenderingBuffer() {
        return renderingBuffer;
    }

    /**
     * .moc3ファイルの整合性をチェックする。
     *
     * @param mocFileName MOC3ファイル名
     * @return MOC3に整合性があるかどうか。整合性があればtrue。
     */
    public boolean hasMocConsistencyFromFile(String mocFileName) {
        assert mocFileName != null && !mocFileName.isEmpty();

        String path = mocFileName;
        path = modelHomeDirectory + path;

        byte[] buffer = createBuffer(path);
        boolean consistency = CubismMoc.hasMocConsistency(buffer);

        if (!consistency) {
            CubismDebug.cubismLogInfo("Inconsistent MOC3.");
        } else {
            CubismDebug.cubismLogInfo("Consistent MOC3.");
        }

        return consistency;
    }

    private static byte[] createBuffer(final String path) {
        if (LAppDefine.DEBUG_LOG_ENABLE) {
            LAppPal.printLog("create buffer: " + path);
        }
        return LAppPal.loadFileAsBytes(path);
    }

    // model3.jsonからモデルを生成する
    private void setupModel(ICubismModelSetting setting) {
        modelSetting = setting;

        isUpdated = true;
        isInitialized = false;

        // Load Cubism Model
        {
            String fileName = modelSetting.getModelFileName();
            if (!fileName.equals("")) {
                String path = modelHomeDirectory + fileName;

                if (LAppDefine.DEBUG_LOG_ENABLE) {
                    LAppPal.printLog("create model: " + modelSetting.getModelFileName());
                }

                byte[] buffer = createBuffer(path);
                loadModel(buffer, mocConsistency);
            }
        }

        // load expression files(.exp3.json)
        {
            if (modelSetting.getExpressionCount() > 0) {
                final int count = modelSetting.getExpressionCount();

                for (int i = 0; i < count; i++) {
                    String name = modelSetting.getExpressionName(i);
                    String path = modelSetting.getExpressionFileName(i);
                    path = modelHomeDirectory + path;

                    byte[] buffer = createBuffer(path);
                    CubismExpressionMotion motion = loadExpression(buffer);

                    expressions.put(name, motion);
                }
            }
        }

        // Physics
        {
            String path = modelSetting.getPhysicsFileName();
            if (!path.equals("")) {
                String modelPath = modelHomeDirectory + path;
                byte[] buffer = createBuffer(modelPath);

                loadPhysics(buffer);
            }
        }

        // Pose
        {
            String path = modelSetting.getPoseFileName();
            if (!path.equals("")) {
                String modelPath = modelHomeDirectory + path;
                byte[] buffer = createBuffer(modelPath);
                loadPose(buffer);
            }
        }

        // Load eye blink data
        if (modelSetting.getEyeBlinkParameterCount() > 0) {
            eyeBlink = CubismEyeBlink.create(modelSetting);
        }

        // Load Breath Data
        breath = CubismBreath.create();
        List<CubismBreath.BreathParameterData> breathParameters = new ArrayList<CubismBreath.BreathParameterData>();

        breathParameters.add(new CubismBreath.BreathParameterData(idParamAngleX, 0.0f, 15.0f, 6.5345f, 0.5f));
        breathParameters.add(new CubismBreath.BreathParameterData(idParamAngleY, 0.0f, 8.0f, 3.5345f, 0.5f));
        breathParameters.add(new CubismBreath.BreathParameterData(idParamAngleZ, 0.0f, 10.0f, 5.5345f, 0.5f));
        breathParameters.add(new CubismBreath.BreathParameterData(idParamBodyAngleX, 0.0f, 4.0f, 15.5345f, 0.5f));
        breathParameters.add(new CubismBreath.BreathParameterData(idParamBreath, 0.0f, 1.0f, 1.0f, 0.0f));
        breathParameters.add(new CubismBreath.BreathParameterData(CubismFramework.getIdManager().getId(CubismDefaultParameterId.ParameterId.BREATH.getId()), 0.5f, 0.5f, 3.2345f, 0.5f));

        breath.setParameters(breathParameters);

        // Load UserData
        {
            String path = modelSetting.getUserDataFile();
            if (!path.equals("")) {
                String modelPath = modelHomeDirectory + path;
                byte[] buffer = createBuffer(modelPath);
                loadUserData(buffer);
            }
        }


        // EyeBlinkIds
        int eyeBlinkIdCount = modelSetting.getEyeBlinkParameterCount();
        for (int i = 0; i < eyeBlinkIdCount; i++) {
            eyeBlinkIds.add(modelSetting.getEyeBlinkParameterId(i));
        }

        // LipSyncIds
        int lipSyncIdCount = modelSetting.getLipSyncParameterCount();
        for (int i = 0; i < lipSyncIdCount; i++) {
            lipSyncIds.add(modelSetting.getLipSyncParameterId(i));
        }

        // LipFormIds
        int lipFormIdCount = modelSetting.getLipFormParameterCount();
        for (int i = 0; i < lipFormIdCount; i++) {
            lipFormIds.add(modelSetting.getLipFormParameterId(i));
        }

        if (modelSetting == null || modelMatrix == null) {
            LAppPal.printLog("Failed to setupModel().");
            return;
        }

        // Set layout
        Map<String, Float> layout = new HashMap<String, Float>();

        // レイアウト情報が存在すればその情報からモデル行列をセットアップする
        if (modelSetting.getLayoutMap(layout)) {
            modelMatrix.setupFromLayout(layout);
        }

        model.saveParameters();

        // Load motions
        for (int i = 0; i < modelSetting.getMotionGroupCount(); i++) {
            String group = modelSetting.getMotionGroupName(i);
            preLoadMotionGroup(group);
        }

        motionManager.stopAllMotions();

        isUpdated = false;
        isInitialized = true;
    }

    /**
     * モーションデータをグループ名から一括でロードする。
     * モーションデータの名前はModelSettingから取得する。
     *
     * @param group モーションデータのグループ名
     **/
    private void preLoadMotionGroup(final String group) {
        final int count = modelSetting.getMotionCount(group);

        for (int i = 0; i < count; i++) {
            // ex) idle_0
            String name = group + "_" + i;

            String path = modelSetting.getMotionFileName(group, i);
            if (!path.equals("")) {
                String modelPath = modelHomeDirectory + path;

                if (debugMode) {
                    LAppPal.printLog("load motion: " + path + "==>[" + group + "_" + i + "]");
                }

                byte[] buffer;
                buffer = createBuffer(modelPath);

                // If a motion cannot be loaded, a process is skipped.
                CubismMotion tmp = loadMotion(buffer);
                if (tmp == null) {
                    continue;
                }

                final float fadeInTime = modelSetting.getMotionFadeInTimeValue(group, i);

                if (fadeInTime != -1.0f) {
                    tmp.setFadeInTime(fadeInTime);
                }

                final float fadeOutTime = modelSetting.getMotionFadeOutTimeValue(group, i);

                if (fadeOutTime != -1.0f) {
                    tmp.setFadeOutTime(fadeOutTime);
                }

                tmp.setEffectIds(eyeBlinkIds, lipSyncIds, lipFormIds);
                motions.put(name, tmp);
            }
        }
    }

    /**
     * OpenGLのテクスチャユニットにテクスチャをロードする
     */
    private void setupTextures() {
        for (int modelTextureNumber = 0; modelTextureNumber < modelSetting.getTextureCount(); modelTextureNumber++) {
            // テクスチャ名が空文字だった場合はロード・バインド処理をスキップ
            if (modelSetting.getTextureFileName(modelTextureNumber).equals("")) {
                continue;
            }

            // OpenGL ESのテクスチャユニットにテクスチャをロードする
            String texturePath = modelSetting.getTextureFileName(modelTextureNumber);
            texturePath = modelHomeDirectory + texturePath;

            LAppTextureManager.TextureInfo texture =
                LAppDelegate.getInstance()
                            .getTextureManager()
                            .createTextureFromPngFile(texturePath);
            final int glTextureNumber = texture.id;

            this.<CubismRendererAndroid>getRenderer().bindTexture(modelTextureNumber, glTextureNumber);

            if (LAppDefine.PREMULTIPLIED_ALPHA_ENABLE) {
                this.<CubismRendererAndroid>getRenderer().isPremultipliedAlpha(true);
            } else {
                this.<CubismRendererAndroid>getRenderer().isPremultipliedAlpha(false);
            }
        }
    }

    private ICubismModelSetting modelSetting;
    /**
     * モデルのホームディレクトリ
     */
    private String modelHomeDirectory;
    /**
     * デルタ時間の積算値[秒]
     */
    private float userTimeSeconds;

    /**
     * モデルに設定されたまばたき機能用パラメーターID
     */
    private final List<CubismId> eyeBlinkIds = new ArrayList<CubismId>();
    /**
     * モデルに設定されたリップシンク機能用パラメーターID
     */
    private static final List<CubismId> lipSyncIds = new ArrayList<CubismId>();
    /**
     * モデルに設定されたリップシンク機能用パラメーターID
     */
    private static final List<CubismId> lipFormIds = new ArrayList<CubismId>();
    /**
     * 読み込まれているモーションのマップ
     */
    private final Map<String, ACubismMotion> motions = new HashMap<String, ACubismMotion>();
    /**
     * 読み込まれている表情のマップ
     */
    private final Map<String, ACubismMotion> expressions = new HashMap<String, ACubismMotion>();

    /**
     * パラメーターID: ParamAngleX
     */
    private final CubismId idParamAngleX;
    /**
     * パラメーターID: ParamAngleY
     */
    private final CubismId idParamAngleY;
    /**
     * パラメーターID: ParamAngleZ
     */
    private final CubismId idParamAngleZ;
    /**
     * パラメーターID: ParamBodyAngleX
     */
    private final CubismId idParamBodyAngleX;
    /**
     * パラメーターID: ParamBodyAngleY
     */
    private final CubismId idParamBodyAngleY;
    /**
     * パラメーターID: ParamEyeBallX
     */
    private final CubismId idParamEyeBallX;
    /**
     * パラメーターID: ParamEyeBallY
     */
    private final CubismId idParamEyeBallY;
    private final CubismId idParamBreath;

    /**
     * フレームバッファ以外の描画先
     */
    private final CubismOffscreenSurfaceAndroid renderingBuffer = new CubismOffscreenSurfaceAndroid();
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static float lipSyncValue = 0.0f;
    private static float lipFormValue = 0.0f;
}

