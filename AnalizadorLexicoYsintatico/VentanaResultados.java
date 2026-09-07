
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VentanaResultados extends JFrame {

    // ==========================================
    // DATOS DEL ANALISIS
    // ==========================================

    private List<Token> tokens;

    // Analisis sintactico
    private List<ErrorSintactico> erroresSintacticos;
    private boolean sintaxisCorrecta;

    // Analisis semantico
    private List<Errorsemantico> erroresSemanticos;
    private boolean semanticaCorrecta;

    // ==========================================
    // COMPONENTES TABLA DE TOKENS
    // ==========================================

    private JTable tablaTokens;
    private DefaultTableModel modeloTokens;

    // ==========================================
    // COMPONENTES ERRORES SINTACTICOS
    // ==========================================

    private JTable tablaErrores;
    private DefaultTableModel modeloErrores;

    // ==========================================
    // COMPONENTES ERRORES SEMANTICOS
    // ==========================================

    private JTable tablaErroresSemanticos;
    private DefaultTableModel modeloErroresSemanticos;

    // ==========================================
    // ETIQUETAS DE RESUMEN
    // ==========================================

    private JLabel etiquetaResumenLexico;
    private JLabel etiquetaResumenSintactico;
    private JLabel etiquetaResumenSemantico;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public VentanaResultados(List<Token> tokens) {

        super("Resultado del Analisis Lexico, Sintactico y Semantico");

        this.tokens = tokens;

        // ==========================================
        // 1. ANALISIS SINTACTICO
        // ==========================================

        this.sintaxisCorrecta =
                AnalizadorSintactico.analizar(tokens);

        this.erroresSintacticos =
                AnalizadorSintactico.getErrores();

        // ==========================================
        // 2. ANALISIS SEMANTICO
        // ==========================================
        // El analisis semantico solamente se ejecuta
        // si el programa es sintacticamente correcto.

        if (this.sintaxisCorrecta) {

            this.semanticaCorrecta =
                    AnalizadorSemantico.analizar(tokens);

            this.erroresSemanticos =
                    AnalizadorSemantico.getErrores();

        } else {

            // Si hay errores sintacticos,
            // el analisis semantico no se ejecuta.

            this.semanticaCorrecta = false;

            this.erroresSemanticos =
                    new ArrayList<Errorsemantico>();
        }

        // ==========================================
        // CONSTRUIR INTERFAZ
        // ==========================================

        construirInterfaz();

        // ==========================================
        // CARGAR RESULTADOS
        // ==========================================

        cargarDatosLexico();

        cargarDatosSintactico();

        cargarDatosSemantico();
    }

    // ==========================================
    // CONSTRUIR INTERFAZ
    // ==========================================

    private void construirInterfaz() {

        setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        setSize(800, 550);

        setLocationRelativeTo(null);

        setLayout(
                new BorderLayout(10, 10)
        );

        // ==========================================
        // TITULO
        // ==========================================

        JLabel titulo =
                new JLabel(
                        "Resultado del Analisis",
                        SwingConstants.CENTER
                );

        titulo.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );

        titulo.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        0,
                        10
                )
        );

        add(
                titulo,
                BorderLayout.NORTH
        );

        // ==========================================
        // PESTANAS
        // ==========================================

        JTabbedPane pestanas =
                new JTabbedPane();

        // Pestaña del analizador lexico
        pestanas.addTab(
                "Tokens (Lexico)",
                construirPanelTokens()
        );

        // Pestaña del analizador sintactico
        pestanas.addTab(
                "Analisis Sintactico",
                construirPanelSintactico()
        );

        // Pestaña del analizador semantico
        pestanas.addTab(
                "Analisis Semantico",
                construirPanelSemantico()
        );

        add(
                pestanas,
                BorderLayout.CENTER
        );
    }

    // ==========================================
    // PANEL ANALISIS LEXICO
    // ==========================================

    private JPanel construirPanelTokens() {

        JPanel panel =
                new JPanel(
                        new BorderLayout(5, 5)
                );

        // ==========================================
        // TABLA DE TOKENS
        // ==========================================

        String[] columnas = {
            "Linea",
            "Token",
            "Lexema"
        };

        modeloTokens =
                new DefaultTableModel(
                        columnas,
                        0
                ) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };

        tablaTokens =
                new JTable(
                        modeloTokens
                );

        tablaTokens.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        13
                )
        );

        tablaTokens.setRowHeight(22);

        tablaTokens
                .getTableHeader()
                .setFont(
                        new Font(
                                "Arial",
                                Font.BOLD,
                                13
                        )
                );

        panel.add(
                new JScrollPane(
                        tablaTokens
                ),
                BorderLayout.CENTER
        );

        // ==========================================
        // RESUMEN LEXICO
        // ==========================================

        etiquetaResumenLexico =
                new JLabel();

        etiquetaResumenLexico.setBorder(
                BorderFactory.createEmptyBorder(
                        5,
                        10,
                        10,
                        10
                )
        );

        panel.add(
                etiquetaResumenLexico,
                BorderLayout.SOUTH
        );

        return panel;
    }

    // ==========================================
    // PANEL ANALISIS SINTACTICO
    // ==========================================

    private JPanel construirPanelSintactico() {

        JPanel panel =
                new JPanel(
                        new BorderLayout(5, 5)
                );

        // ==========================================
        // RESUMEN SINTACTICO
        // ==========================================

        etiquetaResumenSintactico =
                new JLabel();

        etiquetaResumenSintactico.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        15
                )
        );

        etiquetaResumenSintactico.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        panel.add(
                etiquetaResumenSintactico,
                BorderLayout.NORTH
        );

        // ==========================================
        // TABLA DE ERRORES
        // ==========================================

        String[] columnas = {
            "Linea",
            "Descripcion del error"
        };

        modeloErrores =
                new DefaultTableModel(
                        columnas,
                        0
                ) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };

        tablaErrores =
                new JTable(
                        modeloErrores
                );

        tablaErrores.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        13
                )
        );

        tablaErrores.setRowHeight(22);

        tablaErrores
                .getTableHeader()
                .setFont(
                        new Font(
                                "Arial",
                                Font.BOLD,
                                13
                        )
                );

        tablaErrores
                .getColumnModel()
                .getColumn(0)
                .setPreferredWidth(60);

        tablaErrores
                .getColumnModel()
                .getColumn(1)
                .setPreferredWidth(500);

        panel.add(
                new JScrollPane(
                        tablaErrores
                ),
                BorderLayout.CENTER
        );

        return panel;
    }

    // ==========================================
    // PANEL ANALISIS SEMANTICO
    // ==========================================

    private JPanel construirPanelSemantico() {

        JPanel panel =
                new JPanel(
                        new BorderLayout(5, 5)
                );

        // ==========================================
        // RESUMEN SEMANTICO
        // ==========================================

        etiquetaResumenSemantico =
                new JLabel();

        etiquetaResumenSemantico.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        15
                )
        );

        etiquetaResumenSemantico.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        panel.add(
                etiquetaResumenSemantico,
                BorderLayout.NORTH
        );

        // ==========================================
        // TABLA DE ERRORES SEMANTICOS
        // ==========================================

        String[] columnas = {
            "Linea",
            "Descripcion del error"
        };

        modeloErroresSemanticos =
                new DefaultTableModel(
                        columnas,
                        0
                ) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };

        tablaErroresSemanticos =
                new JTable(
                        modeloErroresSemanticos
                );

        tablaErroresSemanticos.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        13
                )
        );

        tablaErroresSemanticos.setRowHeight(22);

        tablaErroresSemanticos
                .getTableHeader()
                .setFont(
                        new Font(
                                "Arial",
                                Font.BOLD,
                                13
                        )
                );

        tablaErroresSemanticos
                .getColumnModel()
                .getColumn(0)
                .setPreferredWidth(60);

        tablaErroresSemanticos
                .getColumnModel()
                .getColumn(1)
                .setPreferredWidth(500);

        panel.add(
                new JScrollPane(
                        tablaErroresSemanticos
                ),
                BorderLayout.CENTER
        );

        return panel;
    }

    // ==========================================
    // CARGAR DATOS DEL ANALISIS LEXICO
    // ==========================================

    private void cargarDatosLexico() {

        List<Token> lista =
                getTokens();

        for (int i = 0;
                i < lista.size();
                i++) {

            Token t =
                    lista.get(i);

            Object[] fila = {
                t.getLinea(),
                t.getToken(),
                t.getLexema()
            };

            modeloTokens.addRow(
                    fila
            );
        }

        etiquetaResumenLexico.setText(
                "Total de tokens reconocidos: "
                + lista.size()
        );
    }

    // ==========================================
    // CARGAR DATOS DEL ANALISIS SINTACTICO
    // ==========================================

    private void cargarDatosSintactico() {

        List<ErrorSintactico> lista =
                getErroresSintacticos();

        if (isSintaxisCorrecta()) {

            etiquetaResumenSintactico.setText(
                    "El programa es sintacticamente CORRECTO."
            );

            etiquetaResumenSintactico.setForeground(
                    new Color(
                            0,
                            128,
                            0
                    )
            );

        } else {

            etiquetaResumenSintactico.setText(
                    "El programa NO es correcto. "
                    + "Se encontraron "
                    + lista.size()
                    + " error(es) de sintaxis:"
            );

            etiquetaResumenSintactico.setForeground(
                    new Color(
                            178,
                            34,
                            34
                    )
            );

            for (int i = 0;
                    i < lista.size();
                    i++) {

                ErrorSintactico e =
                        lista.get(i);

                Object[] fila = {
                    e.getLinea(),
                    e.getMensaje()
                };

                modeloErrores.addRow(
                        fila
                );
            }
        }
    }

    // ==========================================
    // CARGAR DATOS DEL ANALISIS SEMANTICO
    // ==========================================

    private void cargarDatosSemantico() {

        List<Errorsemantico> lista =
                getErroresSemanticos();

        // ==========================================
        // SI EXISTEN ERRORES SINTACTICOS
        // ==========================================

        if (!isSintaxisCorrecta()) {

            etiquetaResumenSemantico.setText(
                    "El analisis semantico no se ejecuto "
                    + "porque existen errores sintacticos."
            );

            etiquetaResumenSemantico.setForeground(
                    new Color(
                            178,
                            34,
                            34
                    )
            );

            return;
        }

        // ==========================================
        // SI NO HAY ERRORES SEMANTICOS
        // ==========================================

        if (isSemanticaCorrecta()) {

            etiquetaResumenSemantico.setText(
                    "El programa es semanticamente CORRECTO."
            );

            etiquetaResumenSemantico.setForeground(
                    new Color(
                            0,
                            128,
                            0
                    )
            );

            return;
        }

        // ==========================================
        // SI HAY ERRORES SEMANTICOS
        // ==========================================

        etiquetaResumenSemantico.setText(
                "El programa NO es semanticamente correcto. "
                + "Se encontraron "
                + lista.size()
                + " error(es) semantico(s):"
        );

        etiquetaResumenSemantico.setForeground(
                new Color(
                        178,
                        34,
                        34
                )
        );

        for (int i = 0;
                i < lista.size();
                i++) {

            Errorsemantico e =
                    lista.get(i);

            Object[] fila = {
                e.getLinea(),
                e.getMensaje()
            };

            modeloErroresSemanticos.addRow(
                    fila
            );
        }
    }

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(
            List<Token> tokens) {

        this.tokens = tokens;
    }

    public List<ErrorSintactico>
            getErroresSintacticos() {

        return erroresSintacticos;
    }

    public void setErroresSintacticos(
            List<ErrorSintactico>
                    erroresSintacticos) {

        this.erroresSintacticos =
                erroresSintacticos;
    }

    public boolean isSintaxisCorrecta() {
        return sintaxisCorrecta;
    }

    public void setSintaxisCorrecta(
            boolean sintaxisCorrecta) {

        this.sintaxisCorrecta =
                sintaxisCorrecta;
    }

    public List<Errorsemantico>
            getErroresSemanticos() {

        return erroresSemanticos;
    }

    public void setErroresSemanticos(
            List<Errorsemantico>
                    erroresSemanticos) {

        this.erroresSemanticos =
                erroresSemanticos;
    }

    public boolean isSemanticaCorrecta() {
        return semanticaCorrecta;
    }

    public void setSemanticaCorrecta(
            boolean semanticaCorrecta) {

        this.semanticaCorrecta =
                semanticaCorrecta;
    }

    public JTable getTablaTokens() {
        return tablaTokens;
    }

    public void setTablaTokens(
            JTable tablaTokens) {

        this.tablaTokens =
                tablaTokens;
    }

    public JTable getTablaErrores() {
        return tablaErrores;
    }

    public void setTablaErrores(
            JTable tablaErrores) {

        this.tablaErrores =
                tablaErrores;
    }

    public JTable getTablaErroresSemanticos() {
        return tablaErroresSemanticos;
    }

    public void setTablaErroresSemanticos(
            JTable tablaErroresSemanticos) {

        this.tablaErroresSemanticos =
                tablaErroresSemanticos;
    }
}