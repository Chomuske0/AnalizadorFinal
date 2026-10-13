# Analizador Final 

- LENGUAJES Y AUTOMATAS II
- GRUPO: 6N1

# Nombres de Equipo:
- Estrada Eligio Eric
- Rojas Hernandez Pamela 
- Rene Castañeda Blancas David 
- Morales Guzmán Frida Ivette 
# Numero de Equipo:
3

---

# ANALIZADOR LÉXICO, SINTÁCTICO, SEMÁNTICO, ÁRBOLES DE EXPRESIONES Y CÓDIGO INTERMEDIO PARA ENSAMBLADOR 8086

## Descripción

El proyecto consiste en desarrollar un analizador de código ensamblador 8086 utilizando Java. El programa recibe como entrada un bloque de código y realiza cuatro etapas de análisis: léxico, sintáctico, semántico y generación de código intermedio.

El análisis léxico identifica los elementos que forman el código y los convierte en tokens. El análisis sintáctico verifica que los tokens estén organizados de acuerdo con la estructura del lenguaje ensamblador 8086. El análisis semántico comprueba que el código tenga sentido, verificando aspectos como variables declaradas, compatibilidad de tamaños, segmentos y destinos de saltos. Adicionalmente, se construye un Árbol de Expresiones (AST) que representa de forma jerárquica la estructura de las instrucciones. Finalmente, se genera código intermedio en forma de cuádruplos (operador, argumento 1, argumento 2, resultado) y su representación equivalente en código de tres direcciones.

Los resultados de los análisis se muestran mediante una interfaz gráfica, permitiendo consultar los tokens reconocidos, los errores encontrados, el árbol generado y los cuádruplos del código intermedio.

## Objetivo

Desarrollar un analizador de código ensamblador 8086 en Java capaz de realizar análisis léxico, sintáctico y semántico, con el propósito de identificar tokens, detectar errores en la estructura del código, comprobar que las instrucciones sean semánticamente válidas, construir su respectivo árbol de expresiones y generar código intermedio en forma de cuádruplos.

## Objetivos específicos

- Identificar los diferentes elementos que forman un programa en ensamblador 8086.
- Generar tokens a partir del código ingresado.
- Detectar errores léxicos.
- Verificar la estructura de las instrucciones mediante el análisis sintáctico.
- Detectar errores sintácticos.
- Comprobar que las variables y etiquetas utilizadas estén declaradas.
- Comprobar la compatibilidad de tamaños entre operandos.
- Detectar errores semánticos.
- Construir un Árbol de Expresiones (AST) para representar jerárquicamente las operaciones y sus operandos.
- Generar código intermedio en forma de cuádruplos y código de tres direcciones a partir del AST.
- Mostrar los resultados mediante una interfaz gráfica.

## Tecnología utilizada

**Java:** fue la única tecnología utilizada para desarrollar el proyecto. Se utilizó para implementar la lógica de los analizadores, la construcción del árbol y la interfaz gráfica.

Para la interfaz gráfica se utilizaron componentes de **Java Swing**, como `JFrame`, `JTextArea`, `JButton`, `JTable`, `JTabbedPane` y `JOptionPane`.

## Funcionamiento del proyecto

El proceso de análisis sigue el siguiente flujo:

1. Código ensamblador 8086.
2. Analizador léxico.
3. Generación de tokens.
4. Analizador sintáctico.
5. Analizador semántico.
6. Construcción del Árbol de Expresiones.
7. Generación de Código Intermedio (Cuádruplos y Código de 3 Direcciones).
8. Visualización de resultados en pestañas.

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

## Árbol de Expresiones (AST)

Como resultado final de los análisis, el programa genera un Árbol de Sintaxis Abstracta en formato de texto. Esto permite comprobar que el analizador comprende la jerarquía de las instrucciones siguiendo la regla de **operador-operando** (la instrucción o declaración actúa como nodo padre, y los registros o valores como nodos hijos).

**Ejemplo de generación:**
Código original:
```asm
MOV AX, DATOS
ADD AX, 10
```

Árbol generado:
```text
PROGRAMA
├── MOV (INSTRUCCION)
│   ├── AX (REGISTRO)
│   └── DATOS (IDENTIFICADOR)
└── ADD (INSTRUCCION)
    ├── AX (REGISTRO)
    └── 10 (NUMERO DEC)
```

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

## Diferencia entre las fases del analizador

**Análisis léxico:** identifica qué es cada elemento del código y genera los tokens.

**Análisis sintáctico:** verifica cómo están organizados los tokens y si cumplen la estructura esperada.

**Análisis semántico:** verifica que el código tenga sentido, por ejemplo, que las variables existan, que los tamaños sean compatibles y que los saltos tengan destinos válidos.

**Árbol de Expresiones:** organiza los elementos jerárquicamente para representar las acciones y los valores sobre los que operan.

**Código Intermedio:** traduce el árbol de expresiones a una representación simplificada (cuádruplos y código de tres direcciones) que sirve como paso previo a la generación de código máquina.

## Código Intermedio (Cuádruplos)

El programa genera código intermedio de tres direcciones a partir del Árbol de Expresiones (AST). Cada instrucción del ensamblador se traduce a uno o más **cuádruplos** con el formato: `(Operador, Argumento 1, Argumento 2, Resultado)`.

### Reglas de traducción

| Instrucción 8086 | Cuádruplo | Código de 3 Direcciones |
|-------------------|-----------|------------------------|
| `MOV AX, 10` | `(=, 10, _, AX)` | `AX = 10` |
| `ADD AX, BX` | `(+, AX, BX, AX)` | `AX = AX + BX` |
| `SUB AX, 5` | `(-, AX, 5, AX)` | `AX = AX - 5` |
| `INC AX` | `(+, AX, 1, AX)` | `AX = AX + 1` |
| `DEC BX` | `(-, BX, 1, BX)` | `BX = BX - 1` |
| `MUL BX` | `(*, AX, BX, AX)` | `AX = AX * BX` |
| `DIV BX` | `(/, AX, BX, AX)` | `AX = AX / BX` |
| `CMP AX, 5` | `(cmp, AX, 5, _)` | `cmp AX, 5` |
| `JZ FIN` | `(if==, AX, 5, FIN)` | `if AX == 5 goto FIN` |
| `JMP INICIO` | `(goto, _, _, INICIO)` | `goto INICIO` |
| `LOOP CICLO` | `(-, CX, 1, CX)` + `(if!=, CX, 0, CICLO)` | `CX = CX - 1` / `if CX != 0 goto CICLO` |
| `HLT` | `(halt, _, _, _)` | `halt` |

### Ejemplo de generación

Código original:
```asm
MOV AX, DATOS
MOV DS, AX
MOV BX, VAR1
ADD BX, VAR2
CMP BX, 0
JZ FIN
INC BX
FIN:
HLT
```

Cuádruplos generados:

| Línea | Operador | Arg 1 | Arg 2 | Resultado |
|:-----:|----------|-------|-------|-----------|
| 1 | `=` | DATOS | _ | AX |
| 2 | `=` | AX | _ | DS |
| 3 | `=` | VAR1 | _ | BX |
| 4 | `+` | BX | VAR2 | BX |
| 5 | `cmp` | BX | 0 | _ |
| 6 | `if==` | BX | 0 | FIN |
| 7 | `+` | BX | 1 | BX |
| 8 | `label` | _ | _ | FIN |
| 9 | `halt` | _ | _ | _ |

Código de tres direcciones equivalente:
```text
    AX = DATOS
    DS = AX
    BX = VAR1
    BX = BX + VAR2
    cmp BX, 0
    if BX == 0 goto FIN
    BX = BX + 1
FIN:
    halt
```

## Conclusión

El proyecto permite aplicar las etapas del proceso de análisis de un lenguaje sobre código ensamblador 8086. La combinación del análisis léxico, sintáctico, semántico, la construcción del árbol de expresiones y la generación de código intermedio en forma de cuádruplos permite detectar diferentes tipos de errores, estructurar el código y traducirlo a una representación intermedia antes de considerar que un programa es totalmente válido.
