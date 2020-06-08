package com.atxconsulting;

import jdk.incubator.foreign.MemoryAddress;
import org.videolan.vlc.Cint;
import org.videolan.vlc.Cpointer;
import org.videolan.vlc.Cstring;
import org.videolan.vlc.CScope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static jdk.incubator.foreign.MemoryAddress.NULL;
import static org.videolan.vlc.vlc_h.*;

public class Main {
    public static void main(String[] args) {
        try (var scope = new CScope();
             var vlcInstance = VLCInstance.Builder()
                     .withArgs(Arrays.asList("--intf", "dummy",
                             "--repeat",
                             //"--verbose=1",
                             "--ignore-config"))
                     .withScope(scope)
                     .build();
             var vlcMedia = vlcInstance.mediaFromLocation("rtsp://admin:admin@192.168.0.242/live");
             var vlcPlayer = vlcMedia.mediaPlayer()) {
            vlcMedia.addOption(":sout=#duplicate{dst=display,dst=std{access=http,mux=ts,dst=127.0.0.1:9080/live}}");

            var COUNTER = new AtomicInteger();

            var o_width = 1280;
            var o_height = 720;
            var o_pitches = o_width * 3;

            var videoBuffer = scope.allocate(o_width * o_height * 3, 16);

            vlcPlayer.formatCallbacks((chroma, width, height, lines, pitches) -> {
                try {
                    System.out.format("original|| chroma: %s width: %d height: %d%n", chroma.get(), width.get(), height.get());
                    chroma.set("RV24");
                    width.set(o_width);
                    height.set(o_height);
                    pitches.set(o_pitches);
                    System.out.format("changed || chroma %s width: %d height: %d%n", chroma.get(), width.get(), height.get());
                    return 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }, null);

            vlcPlayer.videoCallbacks((planes) -> {
                var pictureId = COUNTER.addAndGet(1);
                System.out.format("lock cb: %d%n", pictureId);
                Cpointer.set(planes, 0, videoBuffer);
                return pictureId;
            }, (pictureId, planes) -> {
                System.out.format("unlock cb: %d%n", pictureId);
            }, (pictureId) -> {
                System.out.format("display cb: %d%n", pictureId);
            });

            vlcPlayer.play();
            while (vlcPlayer.willPlay() || vlcPlayer.isPlaying()) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                    System.out.println("10s elapsed");
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    public interface Settable<T> {
        void set(MemoryAddress addr, T val);
    }

    public interface Gettable<T> {
        T get(MemoryAddress addr);
    }

    public static class OutVar<T> {
        MemoryAddress foreignValue;
        boolean dirty = false;
        CScope scope;
        Settable<T> settable;

        OutVar(MemoryAddress fval, CScope scope, Settable<T> settable) {
            foreignValue = fval;
            this.scope = scope;
            this.settable = settable;
        }

        public void set(T val) {
            settable.set(foreignValue, val);
        }
    }

    public static class InOutVar<T> extends OutVar<T> {
        Gettable<T> gettable;

        InOutVar(MemoryAddress fval, CScope scope, Settable<T> settable, Gettable<T> gettable) {
            super(fval, scope, settable);
            this.gettable = gettable;
        }

        public T get() {
            return gettable.get(foreignValue);
        }
    }

    public static class VLCInstance implements AutoCloseable {
        MemoryAddress pInstance;
        CScope scope;

        VLCInstance(List<String> args, CScope scope) {
            this.scope = scope;
            var ptrvlcArgs = Cpointer.allocateArray(args.size(), scope);
            for (var i = 0; i < args.size(); i++) {
                Cpointer.set(ptrvlcArgs, i, Cstring.toCString(args.get(i), scope));
            }
            pInstance = libvlc_new(args.size(), ptrvlcArgs);
            assert (NULL != pInstance);
        }

        @Override
        public void close() {
            if (!NULL.equals(pInstance)) {
                libvlc_release(pInstance);
                this.pInstance = NULL;
            }
        }

        public VLCMedia mediaFromLocation(String location) {
            return VLCMedia.fromLocation(this, location);
        }

        public static VBuilder Builder() {
            return new VBuilder();
        }

        public static final class VBuilder {
            ArrayList<String> args = new ArrayList<String>();
            CScope scope;

            public VBuilder withArg(String arg) {
                args.add(arg);
                return this;
            }

            public VBuilder withArgs(List<String> args) {
                this.args.addAll(args);
                return this;
            }

            public VBuilder withScope(CScope scope) {
                this.scope = scope;
                return this;
            }

            public VLCInstance build() {
                // XXX check scope can't be null, create a scope?
                return new VLCInstance(args, scope);
            }

        }
    }

    public static final class VLCMedia implements AutoCloseable {
        MemoryAddress pMedia;
        CScope scope;

        VLCMedia(MemoryAddress ptr, CScope scope) {
            this.pMedia = ptr;
            this.scope = scope;
        }

        public VLCMediaPlayer mediaPlayer() {
            return VLCMediaPlayer.fromMedia(this);
        }

        public void addOption(String option) {
            libvlc_media_add_option(this.pMedia, Cstring.toCString(option, this.scope));
        }

        @Override
        public void close() {
            if (!NULL.equals(this.pMedia)) {
                libvlc_media_release(this.pMedia);
                this.pMedia = NULL;
            }
        }

        public static VLCMedia fromLocation(VLCInstance instance, String location) {
            var pMedia = libvlc_media_new_location(instance.pInstance, Cstring.toCString(location, instance.scope));
            return new VLCMedia(pMedia, instance.scope);
        }
    }

    public static final class VLCMediaPlayer implements AutoCloseable {
        MemoryAddress pMediaPlayer;
        CScope scope;

        VLCMediaPlayer(MemoryAddress ptr, CScope scope) {
            this.pMediaPlayer = ptr;
            this.scope = scope;
        }

        @Override
        public void close() {
            if (!NULL.equals(this.pMediaPlayer)) {
                libvlc_media_player_release(this.pMediaPlayer);
                this.pMediaPlayer = NULL;
            }
        }

        public boolean play() {
            return libvlc_media_player_play(this.pMediaPlayer) != -1;
        }

        public boolean isPlaying() {
            return libvlc_media_player_is_playing(this.pMediaPlayer) == 1;
        }

        public boolean willPlay() {
            return libvlc_media_player_will_play(this.pMediaPlayer) == 1;
        }

        public void videoCallbacks(VLCVideoLockCB lock, VLCVideoUnlockCB unlock, VLCVideoDisplayCB display) {
            var lock_cb = libvlc_video_set_callbacks$lock.allocate((opaque_in, planes_in) -> {
                var ret = lock.apply(Cpointer.asArrayRestricted(planes_in, 3));
                return MemoryAddress.ofLong(ret);
            }, scope);

            var unlock_cb = NULL;
            if (unlock != null) {
                unlock_cb = libvlc_video_set_callbacks$unlock.allocate((opaque_in, picture_in, planes_in) -> {
                    unlock.apply(picture_in.toRawLongValue(), planes_in);
                }, scope);
            }

            var display_cb = NULL;
            if (display != null) {
                display_cb = libvlc_video_set_callbacks$display.allocate((opaque_in, picture_in) -> {
                    display.apply(picture_in.toRawLongValue());
                }, scope);
            }

            libvlc_video_set_callbacks(this.pMediaPlayer, lock_cb, unlock_cb, display_cb, NULL);
        }

        public void formatCallbacks(VLCFormatSetupCB setup, VLCFormatCleanupCB cleanup) {
            var setup_cb = libvlc_video_set_format_callbacks$setup.allocate((opaque, chroma, width, height, pitches, lines) -> {
                var c = new InOutVar<String>(Cpointer.asArrayRestricted(chroma, 1), scope, Cstring::copy, Cstring::toJavaString);
                var w = new InOutVar<Integer>(Cint.asArrayRestricted(width, 1), scope, Cint::set, Cint::get);
                var h = new InOutVar<Integer>(Cint.asArrayRestricted(height, 1), scope, Cint::set, Cint::get);
                var p = new OutVar<Integer>(Cint.asArrayRestricted(pitches, 1), scope, Cint::set);
                var l = new OutVar<Integer>(Cint.asArrayRestricted(lines, 1), scope, Cint::set);
                var ret = setup.apply(c, w, h, p, l);
                return ret;
            }, scope);

            var cleanup_cb = NULL;
            if (cleanup != null) {
                cleanup_cb = libvlc_video_set_format_callbacks$cleanup.allocate((opaque) -> {
                    cleanup.apply();
                }, scope);
            }

            libvlc_video_set_format_callbacks(this.pMediaPlayer, setup_cb, cleanup_cb);
        }

        public static VLCMediaPlayer fromMedia(VLCMedia media) {
            var pMediaPlayer = libvlc_media_player_new_from_media(media.pMedia);
            return new VLCMediaPlayer(pMediaPlayer, media.scope);
        }
    }

    public interface VLCVideoLockCB {
        long apply(MemoryAddress planes);
    }
    public interface VLCVideoUnlockCB {
        void apply(long picture, MemoryAddress planes);
    }
    public interface VLCVideoDisplayCB {
        void apply(long picture);
    }
    public interface VLCFormatSetupCB {
        int apply(InOutVar<String> chroma, InOutVar<Integer> width, InOutVar<Integer> height, OutVar<Integer> lines, OutVar<Integer> pitches);
    }
    public interface VLCFormatCleanupCB {
        void apply();
    }
}
