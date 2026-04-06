# Setting Up GLWrapper

## Prerequisites

- Java 21 or newer
- A Gradle-based project
- An existing LWJGL application with a GLFW window and an OpenGL context

GLWrapper is distributed via [JitPack](https://jitpack.io) and requires LWJGL as a peer dependency. You are responsible for providing the LWJGL natives appropriate for your target platform(s).

---

## Adding the Repository

Add JitPack to your `repositories` block in `build.gradle`:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

---

## Adding the Dependency

Add GLWrapper to your `dependencies` block:

```groovy
dependencies {
    implementation 'com.github.tolek-tpr:GLWrapper:<version>'
}
```

Replace `<version>` with the version tag you want to use. Available versions are listed on the [JitPack page](https://jitpack.io/#tolek-tpr/GLWrapper).

---

## Adding LWJGL Natives

GLWrapper declares its LWJGL dependencies as `api`, so the core LWJGL jars are included transitively. However, platform-specific native libraries must be added to your own project. Without them, the application will fail at runtime with an `UnsatisfiedLinkError`.

Copy the natives block from the library's own `build.gradle` into your project's `dependencies` block and keep only the platforms you intend to target. For example, to target all three major desktop platforms:

```groovy
ext {
    lwjglVersion = "3.3.6"
}

dependencies {
    // ... your other dependencies ...

    runtimeOnly "org.lwjgl:lwjgl:$lwjglVersion:natives-windows"
    runtimeOnly "org.lwjgl:lwjgl:$lwjglVersion:natives-linux"
    runtimeOnly "org.lwjgl:lwjgl:$lwjglVersion:natives-macos"

    runtimeOnly "org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-windows"
    runtimeOnly "org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-linux"
    runtimeOnly "org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-macos"

    runtimeOnly "org.lwjgl:lwjgl-opengl:$lwjglVersion:natives-windows"
    runtimeOnly "org.lwjgl:lwjgl-opengl:$lwjglVersion:natives-linux"
    runtimeOnly "org.lwjgl:lwjgl-opengl:$lwjglVersion:natives-macos"

    // Add additional modules (assimp, openal, stb, nfd) as needed
}
```

Match the `lwjglVersion` value to the version declared in GLWrapper's `build.gradle` to avoid binary incompatibilities.

---

## Verifying the Setup

Once dependencies are in place, confirm the setup is working by calling the following two lines after your OpenGL context is active:

```java
Renderer.updateProjMatrix(800, 600); // or your actual window size
```

If the application starts without errors, the setup is complete. Refer to [Documentation.md](Documentation.md) for the full API walkthrough.
