package Inventario;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FacturaApp {
    private static final String FILE_NAME = "facturas.json";
    private List<Factura> facturas;
    private DefaultTableModel tableModel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FacturaApp().createAndShowGUI());
    }

    public FacturaApp() {
        facturas = loadFacturasFromJSON();
    }

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Gestión de Facturas");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        String[] columnNames = {"Código Producto", "Nombre Producto", "Cantidad", "Precio", "Impuesto", "Total"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable facturaTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(facturaTable);

        updateTable();

        JButton addButton = new JButton("Agregar Factura");
        JButton editButton = new JButton("Editar Factura");
        JButton deleteButton = new JButton("Eliminar Factura");

        addButton.addActionListener(e -> openFacturaForm(null, -1));
        editButton.addActionListener(e -> {
            int selectedRow = facturaTable.getSelectedRow();
            if (selectedRow != -1) {
                openFacturaForm(facturas.get(selectedRow), selectedRow);
            } else {
                JOptionPane.showMessageDialog(frame, "Seleccione una factura para editar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = facturaTable.getSelectedRow();
            if (selectedRow != -1) {
                facturas.remove(selectedRow);
                saveFacturasToJSON();
                updateTable();
            } else {
                JOptionPane.showMessageDialog(frame, "Seleccione una factura para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void openFacturaForm(Factura factura, int index) {
        JDialog formDialog = new JDialog();
        formDialog.setTitle(factura == null ? "Agregar Factura" : "Editar Factura");
        formDialog.setSize(400, 400);
        formDialog.setLayout(new GridLayout(7, 2));
        formDialog.setModal(true);

        JTextField codigoField = new JTextField(factura == null ? "" : factura.getCodigoProducto());
        JTextField nombreField = new JTextField(factura == null ? "" : factura.getNombreProducto());
        JTextField cantidadField = new JTextField(factura == null ? "" : String.valueOf(factura.getCantidad()));
        JTextField precioField = new JTextField(factura == null ? "" : String.valueOf(factura.getPrecio()));
        JTextField impuestoField = new JTextField(factura == null ? "" : String.valueOf(factura.getImpuesto()));

        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> {
            try {
                String codigo = codigoField.getText();
                String nombre = nombreField.getText();
                int cantidad = Integer.parseInt(cantidadField.getText());
                double precio = Double.parseDouble(precioField.getText());
                double impuesto = Double.parseDouble(impuestoField.getText());
                double total = cantidad * precio + impuesto;

                Factura nuevaFactura = new Factura(codigo, nombre, cantidad, precio, impuesto, total);

                if (index == -1) {
                    facturas.add(nuevaFactura);
                } else {
                    facturas.set(index, nuevaFactura);
                }

                saveFacturasToJSON();
                updateTable();
                formDialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(formDialog, "Datos inválidos. Verifique los campos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> formDialog.dispose());

        formDialog.add(new JLabel("Código Producto:"));
        formDialog.add(codigoField);
        formDialog.add(new JLabel("Nombre Producto:"));
        formDialog.add(nombreField);
        formDialog.add(new JLabel("Cantidad:"));
        formDialog.add(cantidadField);
        formDialog.add(new JLabel("Precio:"));
        formDialog.add(precioField);
        formDialog.add(new JLabel("Impuesto:"));
        formDialog.add(impuestoField);
        formDialog.add(saveButton);
        formDialog.add(cancelButton);

        formDialog.setVisible(true);
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        for (Factura factura : facturas) {
            tableModel.addRow(new Object[]{
                    factura.getCodigoProducto(),
                    factura.getNombreProducto(),
                    factura.getCantidad(),
                    factura.getPrecio(),
                    factura.getImpuesto(),
                    factura.getTotal()
            });
        }
    }

    private List<Factura> loadFacturasFromJSON() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Factura>>() {}.getType();
            return new Gson().fromJson(reader, listType);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar facturas desde el archivo JSON.", "Error", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }

    private void saveFacturasToJSON() {
        try (FileWriter writer = new FileWriter(FILE_NAME)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(facturas, writer);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al guardar las facturas en el archivo JSON.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
