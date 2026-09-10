# Analizador Final 

LENGUAJES Y AUTOMATAS II
GRUPO: 6N1

# Nombres de Equipo:
Eric Estrada Eligio
Pamela Rojas Hernandez
David Rene Castañeda Blancas

# Numero de Equipo:
3

---

# ANALIZADOR LÉXICO, SINTÁCTICO Y SEMÁNTICO PARA ENSAMBLADOR 8086

**Equipo 3**

**Integrantes:**
- Eric Estrada Eligio
- Pamela Rojas Hernandez
- David Rene Castañeda Blancas

## Descripción

El proyecto consiste en desarrollar un analizador de código ensamblador 8086 utilizando Java. El programa recibe como entrada un bloque de código y realiza tres etapas de análisis: léxico, sintáctico y semántico.

El análisis léxico identifica los elementos que forman el código y los convierte en tokens. El análisis sintáctico verifica que los tokens estén organizados de acuerdo con la estructura del lenguaje ensamblador 8086. Finalmente, el análisis semántico comprueba que el código tenga sentido, verificando aspectos como variables declaradas, compatibilidad de tamaños, segmentos y destinos de saltos.

Los resultados de los análisis se muestran mediante una interfaz gráfica, permitiendo consultar los tokens reconocidos y los errores encontrados.

## Objetivo

Desarrollar un analizador de código ensamblador 8086 en Java capaz de realizar análisis léxico, sintáctico y semántico, con el propósito de identificar tokens, detectar errores en la estructura del código y comprobar que las instrucciones sean semánticamente válidas.

## Objetivos específicos

- Identificar los diferentes elementos que forman un programa en ensamblador 8086.
- Generar tokens a partir del código ingresado.
- Detectar errores léxicos.
- Verificar la estructura de las instrucciones mediante el análisis sintáctico.
- Detectar errores sintácticos.
- Comprobar que las variables y etiquetas utilizadas estén declaradas.
- Comprobar la compatibilidad de tamaños entre operandos.
- Detectar errores semánticos.
- Mostrar los resultados mediante una interfaz gráfica.

## Tecnología utilizada

**Java:** fue la única tecnología utilizada para desarrollar el proyecto. Se utilizó para implementar la lógica de los analizadores y la interfaz gráfica.

Para la interfaz gráfica se utilizaron componentes de **Java Swing**, como `JFrame`, `JTextArea`, `JButton`, `JTable`, `JTabbedPane` y `JOptionPane`.

## Funcionamiento del proyecto

El proceso de análisis sigue el siguiente flujo:

1. Código ensamblador 8086.
2. Analizador léxico.
3. Generación de tokens.
4. Analizador sintáctico.
5. Analizador semántico.
6. Visualización de resultados.

## Ejemplos de prueba

### 1. Prueba del analizador léxico

**Código de prueba:**

```asm
MOV AX, BX
ADD AX, 10
MOV CX, AX
```

**Tokens que se deben reconocer:**

| Lexema | Tipo de token |
|--------|---------------|
| MOV    | Instrucción   |
| AX     | Registro      |
| ,      | Separador     |
| BX     | Registro      |
| ADD    | Instrucción   |
| 10     | Número        |
| CX     | Registro      |

Esta prueba permite comprobar que el analizador léxico identifica correctamente instrucciones, registros, números y separadores.

### 2. Prueba del analizador sintáctico

**Código de prueba:**

```asm
DATOS SEGMENT
VAR1 DW 10
DATOS ENDS

CODIGO SEGMENT
MOV AX, VAR1
ADD AX, 5
CODIGO ENDS
```

El analizador sintáctico debe verificar que las instrucciones tengan la estructura esperada. Por ejemplo, `MOV AX, VAR1` contiene una instrucción y dos operandos separados por una coma.

**Pruebas de error sintáctico:**

- `MOV AX` → falta un operando.
- `MOV AX, BX, CX` → cantidad incorrecta de operandos.

### 3. Prueba del analizador semántico

**Código de prueba correcto:**

```asm
DATOS SEGMENT
VAR1 DW 10
VAR2 DW 200
DATOS ENDS

CODIGO SEGMENT
ASSUME CS:CODIGO, DS:DATOS

INICIO:
MOV AX, DATOS
MOV DS, AX
MOV BX, VAR1
ADD BX, VAR2
CMP BX, 0
JZ FIN
INC BX

FIN:
HLT

CODIGO ENDS
END INICIO
```

Este código permite comprobar variables declaradas, segmentos, etiquetas, instrucciones y destinos de saltos.

**Pruebas de errores semánticos:**

| Prueba | Código | Resultado esperado |
|--------|--------|--------------------|
| Variable no declarada | `MOV BX, VAR3` | VAR3 no fue declarada. |
| Tamaños incompatibles | `VAR1 DB 10` / `MOV BX, VAR1` | VAR1 es BYTE y BX es WORD. |
| Salto a etiqueta inexistente | `JZ FINAL` | FINAL no está declarada como etiqueta. |
| Declaración duplicada | `VAR1 DW 10` / `VAR1 DW 20` | VAR1 fue declarada dos veces. |
| SEGMENT sin ENDS | `DATOS SEGMENT` / `VAR1 DW 10` | El segmento no fue cerrado correctamente. |
| ASSUME con segmento inexistente | `ASSUME CS:PROGRAMA, DS:DATOS` | PROGRAMA no fue declarado. |

## Tokens principales

Durante el análisis léxico se pueden reconocer diferentes tipos de elementos:

| Tipo | Ejemplos |
|------|----------|
| Instrucciones | MOV, ADD, SUB, CMP, AND, OR, XOR, TEST, XCHG |
| Registros | AX, BX, CX, DX, SI, DI, BP, SP |
| Registros de byte | AL, AH, BL, BH, CL, CH, DL, DH |
| Directivas | SEGMENT, ENDS, ASSUME, DB, DW, PROC, ENDP, END |
| Identificadores | VAR1, VAR2, DATOS, CODIGO, INICIO, FIN |
| Números | 10, 200, 0 |
| Separadores | `,` y `:` |

## Diferencia entre los tres analizadores

**Análisis léxico:** identifica qué es cada elemento del código y genera los tokens.

**Análisis sintáctico:** verifica cómo están organizados los tokens y si cumplen la estructura esperada.

**Análisis semántico:** verifica que el código tenga sentido, por ejemplo, que las variables existan, que los tamaños sean compatibles y que los saltos tengan destinos válidos.

## Conclusión

El proyecto permite aplicar las tres primeras etapas del proceso de análisis de un lenguaje sobre código ensamblador 8086. La combinación del análisis léxico, sintáctico y semántico permite detectar diferentes tipos de errores antes de considerar que un programa es válido.
