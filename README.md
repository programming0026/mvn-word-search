# Sopa de letras

Juego de sopa de letras de 15 x 15 hecho con Java Swing y tema oscuro FlatLaf. Las palabras pueden estar en cualquier dirección, incluso al revés.

## Características

- Palabras colocadas al azar en las **ocho direcciones** (horizontal, vertical y diagonales, en ambos sentidos).
- Selección **arrastrando el ratón** en línea recta; las palabras halladas se marcan en verde.
- Puntuación: 10 puntos por letra.
- **Palabras personalizables:** el archivo `src/main/resources/palabras.txt` (una por línea; `#` para comentarios), que va dentro del jar, o el menú *Load Words* para cargar otro archivo.
- Si el recurso falta o no tiene palabras válidas, se usan diez palabras de ejemplo.
- Atajos de teclado: Ctrl+N (nuevo juego), Ctrl+O (cargar palabras), F1 (ayuda) y Ctrl+Q (salir).

## Requisitos

- JDK 21 y Maven 3.9 o superior.

## Compilar y ejecutar

```bash
mvn compile exec:java
```

## Cómo jugar

1. Las palabras a buscar aparecen en el panel de la derecha.
2. Pulsa la primera letra de una palabra, mantén pulsado y arrastra hasta la última.
3. Suelta el botón del ratón para comprobar la selección.
4. Gana quien encuentre todas las palabras.

## Notas

- `palabras.txt` se lee del classpath (carpeta `resources`), así que el juego funciona igual desde cualquier carpeta y desde el fat jar. Para cambiar la lista por defecto edita ese archivo y vuelve a compilar; para una lista puntual usa el menú *Load Words*.
- Las palabras más largas que la cuadrícula (15 letras) o que no consigan colocarse se descartan.

## Estructura

```text
src/main/java/com/juniverse/word/
├── Main.java
├── model/
│   ├── WordSearchGrid.java  # genera la sopa: coloca las palabras y rellena con letras
│   ├── LineSelection.java   # selección en línea recta y búsqueda de la palabra elegida
│   └── WordListParser.java  # interpreta el archivo de palabras
├── dto/WordDto.java
├── gui/                     # WordsFrame (ventana) y AboutDialog
└── util/                    # fechas, iconos y mensajes
```

## Pruebas

```bash
mvn test
```

## Licencia

Código de ejemplo de uso libre con fines educativos.
