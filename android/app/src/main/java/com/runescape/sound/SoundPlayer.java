package com.runescape.sound;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Android replacement for the desktop {@code SoundPlayer}.
 *
 * <p>The desktop class parses the WAV byte stream produced by
 * {@link Track#data(int, int)} with {@code javax.sound.sampled} and plays it
 * through a {@code Clip}. Android has neither API, so this variant parses the
 * same canonical 8-bit mono 22050&nbsp;Hz WAV envelope and feeds converted
 * 16-bit samples to an {@link AudioTrack}. Volume, delayed start and the
 * fire-and-forget lifecycle match the desktop class.
 */
public class SoundPlayer implements Runnable {

    private static final int SAMPLE_RATE = 22050;

    public static int volume;

    private final InputStream soundStream;
    private final int delay;
    private final int soundLevel;

    public SoundPlayer(InputStream stream, int level, int delay) {
        this.soundStream = stream;
        this.soundLevel = level;
        this.delay = delay;
        if (level == 0 || volume == 4 || level - volume <= 0) {
            return;
        }
        Thread player = new Thread(this, "SoundPlayer");
        player.setDaemon(true);
        player.start();
    }

    public static int getVolume() {
        return volume;
    }

    public static void setVolume(int level) {
        volume = level;
    }

    @Override
    public void run() {
        AudioTrack track = null;
        try {
            byte[] wav = readAll(soundStream);
            if (wav == null || wav.length <= 12) {
                return;
            }

            int dataOffset = -1;
            int dataLength = 0;
            int offset = 12; // skip "RIFF", size and "WAVE"
            while (offset + 8 <= wav.length) {
                int id = littleEndianInt(wav, offset);
                int size = littleEndianInt(wav, offset + 4);
                if (id == 0x64617461) { // "data"
                    dataOffset = offset + 8;
                    dataLength = Math.min(size, wav.length - dataOffset);
                    break;
                }
                if (size < 0) {
                    break;
                }
                offset += 8 + size + (size & 1);
            }
            if (dataOffset < 0 || dataLength <= 0) {
                return;
            }

            // WAV from Track is unsigned 8-bit PCM; AudioTrack wants signed 16-bit.
            short[] samples = new short[dataLength];
            for (int i = 0; i < dataLength; i++) {
                samples[i] = (short) (((wav[dataOffset + i] & 0xFF) - 128) << 8);
            }

            float gain = decibelsToGain(soundLevel - getVolume());
            if (gain <= 0.0001f) {
                return;
            }

            int minBuffer = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            int bufferSize = Math.max(minBuffer > 0 ? minBuffer : 4096, 4096);

            track = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build())
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build();

            track.setVolume(Math.min(1f, gain));
            if (delay > 0) {
                Thread.sleep(delay);
            }
            track.play();

            int written = 0;
            while (written < samples.length && !Thread.currentThread().isInterrupted()) {
                int count = Math.min(2048, samples.length - written);
                int n = track.write(samples, written, count);
                if (n <= 0) {
                    break;
                }
                written += n;
            }

            // Streaming writes return as soon as the buffer accepts them, so wait
            // for the playback head to reach the end before releasing the track.
            while (track.getPlaybackHeadPosition() < samples.length
                    && !Thread.currentThread().isInterrupted()) {
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (track != null) {
                try {
                    track.stop();
                } catch (Exception ignored) {
                }
                track.release();
            }
        }
    }

    /** The desktop class uses the same decibel curve; 0 dB is full scale. */
    private static float decibelsToGain(int level) {
        float decibels;
        switch (level) {
            case 1:
                decibels = -80f;
                break;
            case 2:
                decibels = -70f;
                break;
            case 3:
                decibels = -60f;
                break;
            case 4:
                decibels = -50f;
                break;
            case 5:
                decibels = -40f;
                break;
            case 6:
                decibels = -30f;
                break;
            case 7:
                decibels = -20f;
                break;
            case 8:
                decibels = -10f;
                break;
            case 9:
                decibels = 0f;
                break;
            case 10:
                decibels = 6f;
                break;
            default:
                decibels = 0f;
                break;
        }
        return (float) Math.pow(10d, decibels / 20d);
    }

    private static int littleEndianInt(byte[] data, int offset) {
        return (data[offset] & 0xFF)
                | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }

    private static byte[] readAll(InputStream stream) throws Exception {
        if (stream == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
        byte[] buffer = new byte[8192];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
}
