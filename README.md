# Sopa de letras

Juego de sopa de letras de 15 x 15 hecho con Java Swing y tema oscuro FlatLaf. Las palabras pueden estar en cualquier dirección, incluso al revés.

## Características

- Palabras colocadas al azar en las **ocho direcciones** (horizontal, vertical y diagonales, en ambos sentidos).
- Selección **arrastrando el ratón** en línea recta; las palabras halladas se marcan en verde.
- Puntuación: 10 puntos por letra.
- **Palabras personalizables:** el archivo `palabras.txt` (una por línea; `#` para comentarios) o el menú *Load Words*.
- Si falta `palabras.txt`, se crea uno de ejemplo automáticamente.
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

- El archivo `palabras.txt` se lee desde la carpeta en la que se ejecuta el juego.
- Las palabras más largas que la cuadrícula (15 letras) o que no consigan colocarse se descartan.

## Licencia

Código de ejemplo de uso libre con fines educativos.
