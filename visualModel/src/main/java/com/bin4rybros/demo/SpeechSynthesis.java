package com.bin4rybros.demo;

import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;

import java.util.concurrent.ExecutionException;

public class SpeechSynthesis {
    public static void synthesizeSpeech(String text) throws InterruptedException, ExecutionException {
        String speechKey = "b36ef4c761bf4554a688e5a0c5cb95e7";
        String speechRegion = "southeastasia";

        SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);

        speechConfig.setSpeechSynthesisVoiceName("en-US-JennyNeural");

        SpeechSynthesizer speechSynthesizer = new SpeechSynthesizer(speechConfig);

        SpeechSynthesisResult speechSynthesisResult = speechSynthesizer.SpeakTextAsync(text).get();

        if (speechSynthesisResult.getReason() == ResultReason.SynthesizingAudioCompleted) {
            System.out.println("Speech synthesized to speaker for text [" + text + "]");
        }
        else if (speechSynthesisResult.getReason() == ResultReason.Canceled) {
            SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(speechSynthesisResult);
            System.out.println("CANCELED: Reason=" + cancellation.getReason());

            if (cancellation.getReason() == CancellationReason.Error) {
                System.out.println("CANCELED: ErrorCode=" + cancellation.getErrorCode());
                System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                System.out.println("CANCELED: Did you set the speech resource key and region values?");
            }
        }
    }
}