# GLWrapper Docs

## Spis Treści
1. [Wprowadzenie](#Wprowadzenie)
2. [Podstawy GLWrapper](#Podstawy-GLWrapper)
3. [Pierwsze Kroki](#Pierwsze-kroki)
4. [Renderowanie za pomocą BufferBuilders](#Renderowanie-z-BufferBuilder)
5. [Renderowanie Natychmiastowe](#Renderowanie-natychmiastowe)
6. [DrawContext](#DrawContext)
7. [Identyfikatory Zasobów](#Identyfikatory-zasobów)
8. [Zaawansowane Użycie](#Zaawansowane-użycie)
   - [Własny Fragment Shader](#Własny-Fragment-Shader)
   - [Własny Shader Program](#Własny-Shader-Program)
   - [Własne Vertex Attributes](#Własne-Vertex-Attributes)
   - [Własne Uniforms](#Własne-Uniforms)

---

## Wprowadzenie
GLWrapper to biblioteka renderująca napisana w Java, zbudowana na bazie LWJGL, która abstraktuje niskopoziomowe mechanizmy OpenGL. Jest przeznaczona dla programistów, którzy chcą renderować prostą grafikę 2D bez konieczności ręcznego zarządzania buforami, kompilacją shaderów czy wskaźnikami atrybutów wierzchołków.

Z GLWrapper typowy proces renderowania sprowadza się do opisania tego, co chcesz narysować - pozycje, kolory, tekstury - oraz wywołania jednej metody, aby przekazać dane do renderowania, a biblioteka zajmie się resztą.

> **Uwaga:** Do korzystania z zaawansowanych funkcji, takich jak pisanie własnych shaderów, wymagana jest pewna znajomość OpenGL. Podstawowe API renderowania jej jednak nie wymaga.

---

## Podstawy GLWrapper

### Model Renderowania
W trakcie każdej klatki dodajesz jeden lub więcej obiektów BufferBuilder do kolejki renderowania. Na końcu klatki pojedyncze wywołanie Renderer.render() opróżnia kolejkę — rysując wszystko, co zostało dodane, i czyszcząc ją na następną klatkę.

Oznacza to, że kolejność wywołań metod rysujących w obrębie jednej klatki determinuje kolejność renderowania, a każda klatka zaczyna się od czystego stanu.

### Układ współrzędnych
GLWrapper domyślnie używa współrzędnych ekranu. Punkt (0, 0) znajduje się w lewym dolnym rogu okna, oś x rośnie w prawo, a oś y do góry. Współrzędne odpowiadają bezpośrednio pozycjom pikseli, więc wierzchołek (400, 300, 0) w oknie 800×600 znajduje się na środku ekranu.

Współrzędna z kontroluje kolejność głębi. Obiekty o wyższej wartości z są rysowane przed obiektami o niższej wartości. W większości zastosowań 2D wystarczy z = 0.

Ten układ współrzędnych jest dostarczany automatycznie przez wbudowaną ortograficzną projekcję. Aby ją aktywować, należy wywołać `Renderer.updateProjMatrix(width, height)` z wymiarami okna, zazwyczaj w callbacku zmiany rozmiaru okna.

### Formaty Wierzchołków
Format wierzchołka określa, jakie dane zawiera każdy wierzchołek. GLWrapper udostępnia cztery wbudowane formaty jako enum `DrawMode.VertexFormat`:

| Format               | Dane dla wierzchołka                                 |
|----------------------|------------------------------------------------------|
|POSITION              | Współrzędne x, y, z                                  |
|POSITION_COLOR        | Współrzędne x, y, z + kolor ARGB                     |
|POSITION_TEXTURE      | Współrzędne x, y, z + współrzędne UV                 |
|POSITION_COLOR_TEXTURE| Współrzędne x, y, z  + kolor ARGB + współrzędne UV   |

Format wierzchołka wybierasz podczas uzyskiwania BufferBuilder. GLWrapper automatycznie używa odpowiedniego wbudowanego shadera dla każdego formatu.

### Tryby Rysowania

Tryb rysowania określa, w jaki sposób OpenGL interpretuje dostarczone wierzchołki. GLWrapper udostępnia następujące tryby poprzez enum `DrawMode`:

| Tryb             | Opis                                                |
| ---------------- | --------------------------------------------------- |
| `LINES`          | Każde dwa wierzchołki tworzą niezależny odcinek     |
| `LINE_STRIP`     | Wierzchołki są połączone w ciągłą linię             |
| `TRIANGLES`      | Każde trzy wierzchołki tworzą niezależny trójkąt    |
| `TRIANGLE_STRIP` | Wierzchołki tworzą pas połączonych trójkątów        |
| `TRIANGLE_FAN`   | Wszystkie trójkąty mają wspólny pierwszy wierzchołek|

Do rysowania pełnych (wypełnionych) kształtów najczęściej używa się `TRIANGLES` oraz `TRIANGLE_STRIP`.

BufferBuilder vs Immediate

GLWrapper udostępnia dwa tryby renderowania:

- **`BufferBuilder`** — bufor "kolejkowany". Wierzchołki są gromadzone i przekazywane do kolejki renderowania po wywołaniu `addToQueue()`. Faktyczne rysowanie następuje później, przy wywołaniu `Renderer.render()`.
- **`Immediate`** — bufor natychmiastowy. Rysowanie następuje w momencie wywołania `end()`, z pominięciem kolejki. Używaj tego trybu, gdy potrzebujesz precyzyjnej kontroli nad kolejnością rysowania względem innych elementów w kolejce.

---

## Pierwsze kroki
### Wymagania wstępne
- Działający projekt LWJGL z oknem GLFW i aktywnym kontekstem OpenGL.
- GLWrapper dodany jako biblioteka.

### Inicjalizacja GLWrapper

Po utworzeniu kontekstu OpenGL wywołaj `Renderer.updateProjMatrix(width, height)`, aby ustawić projekcję ortograficzną. Funkcję tę należy również wywoływać przy każdej zmianie rozmiaru okna.

Następnie, na końcu każdej klatki w pętli renderowania, wywołaj `Renderer.render()`, aby opróżnić kolejkę i narysować wszystkie przekazane dane.

```java
public class App {

    long window;

    public void run() {
        // ... Inicjalizacja GLFW i OpenGL ...

        // Ustawienie projekcji ortograficznej
        Renderer.updateProjMatrix(800, 600);

        loop();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // --- Kod Rysujący ---

            Renderer.render(); // Rysowania wszystkich BufferBuilders

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

}
```

---

## Renderowanie z BufferBuilder 
### Uzyskiwanie BufferBuilder
Użyj `Buffers.getBuffer(DrawMode, VertexFormat)`, aby pobrać zarządzany `BufferBuilder` dla wybranego trybu rysowania i formatu wierzchołków. GLWrapper utrzymuje wewnętrzną pulę tych obiektów, więc wywołanie `getBuffer` z tymi samymi argumentami zawsze zwraca ten sam obiekt, gotowy do ponownego użycia.

Dodawanie wierzchołków

`BufferBuilder` udostępnia API do dodawania wierzchołków. Każdy wierzchołek zaczyna się od wywołania `.vertex(x, y, z)`, po którym następują dodatkowe dane wymagane przez wybrany format wierzchołka.

**Tylko pozycja (`POSITION`):**
Podajesz wyłącznie współrzędne wierzchołka.
```java
builder.vertex(100, 100, 0);
```

**Pozycja i kolor (`POSITION_COLOR`):**
Kolory są określane jako cztery liczby zmiennoprzecinkowe w kolejności `(alpha, red, green, blue)`, każda w zakresie `0.0` do `1.0`.
```java
builder.vertex(100, 100, 0).color(1f, 1f, 0f, 0f); // czerwony
```

**Pozycja i tekstura (`POSITION_TEXTURE`):**
Współrzędne UV `(u, v)` określają, który fragment tekstury odpowiada danemu wierzchołkowi. Tekstura jest identyfikowana przez obiekt `Identifier` (zobacz sekcję „Identyfikatory Zasobów”).
```java
builder.vertex(100, 100, 0).texture(0f, 0f, new Identifier("myapp", "textures/sprite.png"));
```

**Pozycja, kolor i tekstura (`POSITION_COLOR_TEXTURE`):**
Łączy wszystkie powyższe — współrzędne, kolor oraz mapowanie tekstury. 
```java
builder.vertex(100, 100, 0)
       .color(1f, 1f, 1f, 1f)
       .texture(0f, 0f, new Identifier("myapp", "textures/sprite.png"));
```

### Renderowanie
W momencie, w którym wszystkie wierzchołki są już dodane, należy wywołać `builder.addToQueue()` aby zarejerstrować `BufferBuilder` do rysowania. Należy wykonać to tylko raz, a dodatkowe wywołania są ignorowane

### Pełny przykład
```java
while (!glfwWindowShouldClose(window)) {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    BufferBuilder builder = Buffers.getBuffer(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR);
    builder.vertex(0,   0,   0).color(1f, 1f, 0f, 0f);
    builder.vertex(200, 0,   0).color(1f, 0f, 1f, 0f);
    builder.vertex(100, 200, 0).color(1f, 0f, 0f, 1f);
    builder.addToQueue();

    Renderer.render();

    glfwSwapBuffers(window);
    glfwPollEvents();
}
```

## Renderowanie natychmiastowe
Obiekty Immediate rysuje swoją zawartość na ekranie w momencie wywołania `end()`, jeszcze przed opróżnieniem kolejki renderowania. Jest to przydatne, gdy trzeba zagwarantować, że coś zostanie narysowane przed zawartością znajdującą się w kolejce.

Aby uzyskać Immediate należy wykonać `Buffers.getImmediate(DrawMode, VertexFormat)`:
```java
Immediate immediate = Buffers.getImmediate(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION);
```

Dodawanie wierzchołków wygląda tak samo jak w BufferBuilder, a następnie wywołaj `end()`, aby natychmiast wykonać rysowanie:
```java
immediate.vertex(50, 50, 0);
immediate.vertex(150, 50, 0);
immediate.vertex(100, 150, 0);
immediate.end(); // Narysowane PRZED Renderer.render
```

### Przykład układania w kolejności
W poniższym przykładzie, mimo że` builder.addToQueue()` jest wywołane przed `immediate.end()`, zawartość `Immediate` pojawi się za trójkątem z kolejki, ponieważ `Immediate` rysuje w momencie wywołania, a zawartość kolejki dopiero później w `Renderer.render()`.

```java
BufferBuilder builder = Buffers.getBuffer(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR);
builder.vertex(0, 0, 0).color(1f, 1f, 0f, 0f);
builder.vertex(200, 0, 0).color(1f, 0f, 1f, 0f);
builder.vertex(100, 200, 0).color(1f, 0f, 0f, 1f);
builder.addToQueue(); // Dodany do kolejki, rysowany podczas Renderer.render()

Immediate immediate = Buffers.getImmediate(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION);
immediate.vertex(0, 0, 0);
immediate.vertex(300, 0, 0);
immediate.vertex(150, 300, 0);
immediate.end(); // Rysowany natychmiast

Renderer.render(); // BufferBuilder w kolejce rysowany dopiero tutaj
```

---

## DrawContext
`DrawContext` udostępnia statyczne metody pomocnicze do rysowania typowych kształtów bez ręcznego dodawania wierzchołków. Każda metoda ma dwie wersje: jedną, która automatycznie pobiera odpowiedni `BufferBuilder` z puli, oraz drugą, która przyjmuje przekazany `BufferBuilder` (lub `Immediate`) do użycia z własnymi shaderami.

Wszystkie metody `DrawContext` przyjmują parametr `boolean queue`. Gdy ma wartość true, `addToQueue()` jest wywoływane automatycznie. Gdy `false`, należy samodzielnie wywołać `addToQueue()`

Kolory w `DrawContext` są określane jako jedna liczba ARGB (np. `0xFF_FF0000` dla w pełni nieprzezroczystej czerwieni).

### drawRect
Rysuje wypełniony prostokąt zdefiniowany przez dwa przeciwległe rogi.
```java
DrawContext.drawRect(int x1, int y1, int x2, int y2, int z, boolean queue);
DrawContext.drawRect(int x1, int y1, int x2, int y2, int z, int color, boolean queue);
DrawContext.drawRect(BufferBuilder builder, int x1, int y1, int x2, int y2, int z, boolean queue);
DrawContext.drawRect(BufferBuilder builder, int x1, int y1, int x2, int y2, int z, int color, boolean queue);
```

- `(x1, y1)` — jeden róg prostokąta w pikselach przestrzeni ekranu.
- `(x2, y2)` — przeciwległy róg.
- `z` — głębia.
- `color` — spakowany kolor ARGB. Domyślnie `0xFFFFFFFF` (w pełni nieprzezroczysta biel), jeśli pominięty.
- `queue` — czy wywołać `addToQueue()` automatycznie.

```java
// Narysuj czerwony prostokąt od (50, 50) do (250, 150)
DrawContext.drawRect(50, 50, 250, 150, 0, 0xFF_FF0000, true);
```

### dradient

Rysuje wypełniony prostokąt z liniowym gradientem między dwoma kolorami wzdłuż określonej osi.

```java
// Signature
DrawContext.drawGradient(int x1, int y1, int x2, int y2, int z,
                         GradientDirection direction, int startColor, int endColor, boolean queue);
DrawContext.drawGradient(BufferBuilder builder, int x1, int y1, int x2, int y2, int z,
                         GradientDirection direction, int startColor, int endColor, boolean queue);
```

- `direction` — wartość enum `GradientDirection` określająca kierunek gradientu:
- `TOP_TO_BOTTOM` (domyślny przy null)
- `BOTTOM_TO_TOP`
- `LEFT_TO_RIGHT`
- `RIGHT_TO_LEFT`
- `startColor` / `endColor` — spakowane kolory ARGB na początku i końcu gradientu.

```java
// Narysuj prostokąt z gradientem od niebieskiego na górze do przezroczystego na dole
DrawContext.drawGradient(
    100, 100, 400, 400, 0,
    GradientDirection.TOP_TO_BOTTOM,
    0xFF_0000FF, // niebieski
    0x00_0000FF, // przezroczysty niebieski
    true
);
```

### drawTexture
Rysuje prostokąt z teksturą. Tekstura jest identyfikowana przez `Identifier` wskazujący na plik obrazu w folderze zasobów.

GLWrapper udostępnia kilka definicji o rosnącym poziomie kontroli:
```java
DrawContext.drawTexture(Identifier texture, int x1, int y1, int x2, int y2, int z, boolean queue);
DrawContext.drawTexture(Identifier texture, int x1, int y1, int x2, int y2, int z,
                        float u, float v, boolean queue);
DrawContext.drawTexture(Identifier texture, int x1, int y1, int x2, int y2, int z,
                        float u, float v, int texWidth, int texHeight, boolean queue);
DrawContext.drawTexture(Identifier texture, int x1, int y1, int x2, int y2, int z,
                        float u, float v, int texWidth, int texHeight, int color, boolean queue);
```

Każda wersja ma również wariant przyjmujący BufferBuilder jako pierwszy argument.

```java
// Narysuj teksturę sprite.png wypełniającą rejon od (0, 0) do (128, 128)
DrawContext.drawTexture(
    new Identifier("myapp", "textures/sprite.png"),
    0, 0, 128, 128, 0,
    true
);
```

---

## Identyfikatory zasobów
GLWrapper używa obiektów `Identifier` do odwoływania się do plików w classpath — najczęściej shaderów i tekstur. `Identifier` składa się z namespace oraz ścieżki:

```java
new Identifier("myapp", "textures/sprite.png")
```

Odwzorowuje się to na zasób w classpath:
```
resources/myapp/assets/textures/sprite.png
```

Namespace odpowiada katalogowi najwyższego poziomu w resources/. Segment /assets/ jest dodawany automatycznie. Ścieżka jest względna względem katalogu assets.

Dla wbudowanych zasobów GLWrapper namespace to glw:
```java
new Identifier("glw", "shaders/core/position_color_program.fsh")
// odwołuje się do: resources/glw/assets/shaders/core/position_color_program.fsh
```

---

## Zaawansowane użycie
### Własny Fragment Shader
Aby zastosować własny efekt wizualny przy zachowaniu wbudowanego przetwarzania wierzchołków, należy dostarczyć własny shader fragmentów za pomocą `withFragmentShader`. GLWrapper połączy go ze standardowym shaderem wierzchołków dla wybranego VertexFormat.
```java
BufferBuilder builder = new BufferBuilder(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR);
builder.withFragmentShader(new Identifier("myapp", "shaders/my_effect.fsh"));

builder.vertex(0, 0, 0).color(1f, 1f, 1f, 1f);
builder.vertex(200, 0, 0).color(1f, 1f, 1f, 1f);
builder.vertex(100, 200, 0).color(1f, 1f, 1f, 1f);
builder.addToQueue();

Renderer.render();
```

Można również przekazać BufferBuilder z własnym shaderem bezpośrednio do `DrawContext`:
```java
BufferBuilder builder = new BufferBuilder(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR);
builder.withFragmentShader(new Identifier("myapp", "shaders/my_effect.fsh"));

DrawContext.drawRect(builder, 0, 0, 300, 300, 0, true);

Renderer.render();
```

Aby przywrócić domyślny shader, użyj `withShader(null)`:
```java
builder.withShader(null);
```

> **Uwaga:** Modyfikowanie domyślnych buforów z `Buffers.getBuffer()` poprzez ustawienie własnego shadera jest możliwe, ale niezalecane — zmiana pozostaje aktywna do momentu resetu.

### Własny Shader Program
Aby uzyskać pełną kontrolę nad etapami vertex i fragment, należy stworzyć `ShaderProgram` i przypisać go przez `withShader`:
```java
BufferBuilder builder = new BufferBuilder(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR);
builder.withShader(new ShaderProgram(
    new Identifier("myapp", "shaders/my_shader.vsh"),
    new Identifier("myapp", "shaders/my_shader.fsh")
));

DrawContext.drawRect(builder, 0, 0, 300, 300, 0, true);

Renderer.render();
```

### Własne Vertex Attributes
Możliwe jest dodanie dodatkowych danych na wierzchołek poza wbudowanymi formatami. Wymaga to własnego shadera, `AttributeType` oraz `AttributeContainer` opisującego układ danych.

**Krok 1 — Rejestracja AttributeType:**
```java
BufferBuilder.AttributeType myType = BufferBuilder.AttributeType.register("MY_ATTRIBUTE");
```

**Krok 2 — Utworzenie AttributeContainer:**
```java
// AttributeContainer(type, size, GlNumberType, shaderLocation)
BufferBuilder.AttributeContainer myContainer =
    new BufferBuilder.AttributeContainer(myType, 1, GlNumberType.FLOAT, 3);
```

- `size` — liczba wartości tego typu na wierzchołek (np. `1` dla pojedynczego float, `3` dla vec3).
- `GlNumberType` — typ danych: `FLOAT`, `INT`, `BYTE`, `SHORT` lub `DOUBLE`.
- `shaderLocation` — indeks `layout(location = X)` w vertex shader.

Odpowiadająca deklaracja GLSL (shadera):
```glsl
layout(location = 3) in float aMyAttribute;
```

Krok 3 — Rejestracja kontenera i zapis danych:
```java
BufferBuilder builder = new BufferBuilder(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR);
builder.withShader(new ShaderProgram(
    new Identifier("myapp", "shaders/custom.vsh"),
    new Identifier("myapp", "shaders/custom.fsh")
));
builder.withVertexAttribute(myContainer);

builder.vertex(0,   0,   0).color(1f, 1f, 1f, 1f).attrib(myType, 0f);
builder.vertex(200, 0,   0).color(1f, 1f, 1f, 1f).attrib(myType, 0f);
builder.vertex(100, 200, 0).color(1f, 1f, 1f, 1f).attrib(myType, 1f);
builder.addToQueue();

Renderer.render();
```

### Własne Uniforms
Uniformy pozwalają przekazywać dowolne dane do shadera w każdej klatce. Należy zaimplementować interfejs `UniformProvider` i zarejestrować go w builderze za pomocą `withUniform`.
```java
UniformProvider timeProvider = new UniformProvider() {
    @Override
    public void apply(ShaderProgram program) {
        program.uniformFloat("uTime", (float) glfwGetTime());
    }
};

builder.withUniform(timeProvider);
```

Dla wielokrotnego użytku dobrym wzorcem jest implementowanie providerów jako rekordów:
```java
public record ViewMatrixUniformProvider(Matrix4f viewMatrix) implements UniformProvider {
    @Override
    public void apply(ShaderProgram program) {
        program.uniformMat4f("uViewMatrix", viewMatrix);
    }
}
```

Następnie można je zarejestrować w dowolnym builderze:
```java
builder.withUniform(new ViewMatrixUniformProvider(myViewMatrix));
```

Dostępne metody uniformów w ShaderProgram:
| Metoda                                       | Typ GLSL                  |
| -------------------------------------------- | ------------------------- |
| `uniformFloat(String name, float value)`     | `float` / `uniform float` |
| `uniformMat4f(String name, Matrix4f matrix)` | `mat4`                    |
| `uniformTexture(String name, int slot)`      | `sampler2D`               |
| `uniformIntArray(String name, int[] array)`  | `int[]`                   |
