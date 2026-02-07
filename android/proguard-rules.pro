# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   https://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-verbose

-dontwarn android.support.**
-dontwarn com.badlogic.gdx.backends.android.AndroidFragmentApplication

# Needed by the gdx-controllers official extension.
-keep class com.badlogic.gdx.controllers.android.AndroidControllers

# Needed by the Box2D official extension.
-keepclassmembers class com.badlogic.gdx.physics.box2d.World {
   boolean contactFilter(long, long);
   boolean getUseDefaultContactFilter();
   void    beginContact(long);
   void    endContact(long);
   void    preSolve(long, long);
   void    postSolve(long, long);
   boolean reportFixture(long);
   float   reportRayFixture(long, float, float, float, float, float);
}

# You will need the next three lines if you use scene2d for UI or gameplay.
# If you don't use scene2d at all, you can remove or comment out the next line:
-keep public class com.badlogic.gdx.scenes.scene2d.** { *; }
# You will need the next two lines if you use BitmapFont or any scene2d.ui text:
-keep public class com.badlogic.gdx.graphics.g2d.BitmapFont { *; }
# You will probably need this line in most cases:
-keep public class com.badlogic.gdx.graphics.Color { *; }

# These two lines are used with mapping files; see https://developer.android.com/build/shrink-code#retracing
-keepattributes LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile

# ========== Kryo Serialization Rules ==========
# Keep all Kryo classes and their methods
-keep class com.esotericsoftware.** { *; }
-dontwarn com.esotericsoftware.**

# Keep all your game classes that get serialized over the network
-keep class com.noiprocs.** { *; }
-keepclassmembers class com.noiprocs.** { *; }

# Keep Netty classes
-keep class io.netty.** { *; }
-dontwarn io.netty.**

# Keep all fields and methods for reflection (Kryo needs this)
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Kryo needs these
-dontwarn sun.misc.**
-dontwarn java.beans.**
-dontwarn javax.naming.**

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ========== Apache Commons Pool 2 - Disable JMX ==========
# Android doesn't have java.lang.management.ManagementFactory
-dontwarn java.lang.management.**
-dontwarn javax.management.**

# Stub out JMX registration methods in Commons Pool
-assumenosideeffects class org.apache.commons.pool2.impl.BaseGenericObjectPool {
    void jmxRegister();
    void jmxUnregister();
}

# ========== Desktop-Only Terminal Libraries ==========
# These libraries are for desktop terminal/console and don't work on Android
-dontwarn org.jline.**
-dontwarn com.sun.jna.**
-dontwarn org.fusesource.jansi.**
-dontwarn org.graalvm.nativeimage.**

# ========== LZ4 Compression Library ==========
# Keep all LZ4 classes - needed for network compression
-keep class net.jpountz.** { *; }
-dontwarn net.jpountz.**

# Keep LZ4 native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# LZ4 uses JNI and native libraries
-keep class net.jpountz.lz4.LZ4Factory { *; }
-keep class net.jpountz.lz4.LZ4Compressor { *; }
-keep class net.jpountz.lz4.LZ4FastDecompressor { *; }
-keep class net.jpountz.lz4.LZ4SafeDecompressor { *; }
-keep class net.jpountz.lz4.LZ4UnknownSizeDecompressor { *; }
