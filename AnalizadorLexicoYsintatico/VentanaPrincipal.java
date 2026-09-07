import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * VentanaPrincipal
 * Ventana donde el usuario escribe la cadena (bloque de codigo 8086)
 * y presiona el boton "Analizar" para lanzar el analizador lexico.
 */
public class VentanaPrincipal extends JFrame {

    private JTextArea campoEntrada;
    private JButton botonAnalizar;

    public VentanaPrincipal() {
        super("Analizador Lexico - Procesador 8086");
        construirInterfaz();
    }

    private void construirInterfaz() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel titulo = new JLabel("Analizador Lexico - Procesador 8086", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(titulo, BorderLayout.NORTH);

        campoEntrada = new JTextArea();
        campoEntrada.setFont(new Font("Consolas", Font.PLAIN, 14));
        campoEntrada.setLineWrap(false);
        campoEntrada.setToolTipText("Escriba aqui el bloque de codigo assembler 8086 a analizar");
        JScrollPane scroll = new JScrollPane(campoEntrada);
        scroll.setBorder(BorderFactory.createTitledBorder("Cadena de entrada"));
        add(scroll, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel();
        botonAnalizar = new JButton("Analizar");
        botonAnalizar.setFont(new Font("Arial", Font.BOLD, 14));
        botonAnalizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analizarCadena();
            }
        });
        panelInferior.add(botonAnalizar);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void analizarCadena() {
        String cadena = getCampoEntrada().getText();

        if (cadena == null || cadena.length() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese una cadena antes de analizar.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Se envia EXACTAMENTE el bloque de entrada asignado, sin modificarlo
        List<Token> resultado = AnalizadorLexico.analizar(cadena);

        VentanaResultados ventanaResultados = new VentanaResultados(resultado);
        ventanaResultados.setVisible(true);
    }

    // ---------- GETTERS Y SETTERS ----------

    public JTextArea getCampoEntrada() {
        return campoEntrada;
    }

    public void setCampoEntrada(JTextArea campoEntrada) {
        this.campoEntrada = campoEntrada;
    }

    public JButton getBotonAnalizar() {
        return botonAnalizar;
    }

    public void setBotonAnalizar(JButton botonAnalizar) {
        this.botonAnalizar = botonAnalizar;
    }
}
