package com.bin4rybros.demo;

import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class SynthesisWithViseme {

    private static final String voiceName = "en-US-SaraNeural";

    public static void synthesizeSpeech(String text) throws InterruptedException, ExecutionException {
        // TODO : protect keys later
        String speechKey = "b36ef4c761bf4554a688e5a0c5cb95e7";
        String speechRegion = "southeastasia";

        SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);

        speechConfig.setSpeechSynthesisVoiceName("en-US-SaraNeural");

        // Create an AudioConfig object to handle audio output.
        AudioConfig audioConfig = AudioConfig.fromDefaultSpeakerOutput();

        // Create a SpeechSynthesizer object with both SpeechConfig and AudioConfig.
        SpeechSynthesizer speechSynthesizer = new SpeechSynthesizer(speechConfig, audioConfig);

        speechSynthesizer.VisemeReceived.addEventListener((o, e) -> {
            // Update lip sync values based on the received viseme ID and audio offset
            LAppModel.updateLipSync(e.getVisemeId(), e.getAudioOffset());
        });

        // Synthesize speech asynchronously
        SpeechSynthesisResult speechSynthesisResult = speechSynthesizer.SpeakTextAsync(text).get();

        if (speechSynthesisResult.getReason() == ResultReason.SynthesizingAudioCompleted) {
            System.out.println("Speech synthesized to speaker for text [" + text + "]");
        } else if (speechSynthesisResult.getReason() == ResultReason.Canceled) {
            SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(speechSynthesisResult);
            System.out.println("CANCELED: Reason=" + cancellation.getReason());

            if (cancellation.getReason() == CancellationReason.Error) {
                System.out.println("CANCELED: ErrorCode=" + cancellation.getErrorCode());
                System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                System.out.println("CANCELED: Did you set the speech resource key and region values?");
            }
        }
    }

//    private static String loadSsmlTemplate() {
//        InputStream inputStream = SpeechSynthesis.class.getResourceAsStream("/speech_template.xml");
//        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
//        return scanner.hasNext() ? scanner.next() : "";
//    }
}
