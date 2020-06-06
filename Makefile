VLC_BASE_PATH := /Applications/VLC.app/Contents/MacOS
run: vlc.jar
	VLC_PLUGIN_PATH=$(VLC_BASE_PATH)/plugins \
  DYLD_FALLBACK_LIBRARY_PATH=$(VLC_BASE_PATH)/lib \
	java -ea -Dforeign.restricted=permit \
			-cp ./vlc.jar \
			--add-modules jdk.incubator.foreign \
			-Djava.library.path=$(VLC_BASE_PATH)/lib \
			src/com/atxconsulting/Main.java

vlc.jar: src/org/videolan/vlc/vlc_h.class
	cd src && jar cf ../vlc.jar org/videolan/vlc/*.class

generate: org/videolan/vlc/vlc_h.class

src/org/videolan/vlc/vlc_h.class: src/org/videolan/vlc/vlc_h.java
	jextract \
		-d src \
    -I$(VLC_BASE_PATH)/include \
    -l vlc -t org.videolan.vlc \
    $(VLC_BASE_PATH)/include/vlc/vlc.h

src/org/videolan/vlc/vlc_h.java:
	jextract --source \
		-d src \
    -I$(VLC_BASE_PATH)/include \
    -l vlc -t org.videolan.vlc \
    $(VLC_BASE_PATH)/include/vlc/vlc.h

vision:
	jextract --source \
			-d src \
			-l Vision \
			-t com.apple.Vision \
			/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/System/Library/Frameworks/Vision.framework/Versions/A/Headers/Vision.h

clean:
	rm -rf src/org vlc.jar
