package facturas;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;

public class Facturas extends JFrame {
    private JTextField facturaNoField, clienteField, productoField, descripcionField, cantidadField, valorUnitarioField;
    private JLabel fechaLabel;
    private JComboBox<String> ivaComboBox, formaPagoComboBox;
    private JLabel sumaLabel, ivaLabel, totalLabel;
    private JTable productosTable;
    private Connection connection;

    public Facturas() {
        conectarBaseDatos();
        initComponents();
    }

    private void initComponents() {
        setTitle("Factura - Productos (INTERFAZ CRUD)");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Cambiar fondo a color azul
        getContentPane().setBackground(Color.BLUE);

        // Panel superior para la información de la factura
        JPanel topPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        topPanel.add(new JLabel("Factura No.:"));
        facturaNoField = new JTextField("FAC-006");
        facturaNoField.setEditable(false);
        topPanel.add(facturaNoField);

        topPanel.add(new JLabel("Cliente:"));
        clienteField = new JTextField();
        topPanel.add(clienteField);

        topPanel.add(new JLabel("Fecha:"));
        fechaLabel = new JLabel("2024-06-11");
        topPanel.add(fechaLabel);

        topPanel.add(new JLabel("Forma de Pago:"));
        formaPagoComboBox = new JComboBox<>(new String[]{"EFECT", "TARCR", "TARDB", "TRANS", "CHEQ"});
        topPanel.add(formaPagoComboBox);

        add(topPanel, BorderLayout.NORTH);

        // Panel central para los productos y la tabla
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.BLUE);

        JPanel productPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        productPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        productPanel.setBackground(Color.BLUE);

        productPanel.add(new JLabel("Producto:"));
        productoField = new JTextField();
        productoField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarProductoDesdeCodigo(productoField.getText());
            }
        });
        productPanel.add(productoField);

        productPanel.add(new JLabel("Descripción:"));
        descripcionField = new JTextField();
        descripcionField.setEditable(false);
        productPanel.add(descripcionField);

        productPanel.add(new JLabel("Cantidad:"));
        cantidadField = new JTextField();
        productPanel.add(cantidadField);

        productPanel.add(new JLabel("Val Uni:"));
        valorUnitarioField = new JTextField();
        valorUnitarioField.setEditable(false);
        productPanel.add(valorUnitarioField);

        productPanel.add(new JLabel("IVA:"));
        ivaComboBox = new JComboBox<>(new String[]{"0%", "8%", "12%", "15%"});
        productPanel.add(ivaComboBox);

        centerPanel.add(productPanel, BorderLayout.NORTH);

        // Tabla de productos
        productosTable = new JTable(new DefaultTableModel(new Object[][]{}, new String[]{"Producto", "Descripción", "Cantidad", "Val Uni", "Subtotal"}));
        centerPanel.add(new JScrollPane(productosTable), BorderLayout.CENTER);

        // Agregar el panel central al frame
        add(centerPanel, BorderLayout.CENTER);

        // Panel inferior para los botones y totales
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.setBackground(Color.BLUE);

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 6, 10, 10));
        buttonsPanel.setBackground(Color.BLUE);

        JButton buscarProductoButton = new JButton("Buscar Producto");
        buttonsPanel.add(buscarProductoButton);

        JButton agregarProductoButton = new JButton("Agregar Producto");
        buttonsPanel.add(agregarProductoButton);

        JButton actualizarProductoButton = new JButton("Actualizar Producto");
        buttonsPanel.add(actualizarProductoButton);

        JButton eliminarProductoButton = new JButton("Eliminar Producto");
        buttonsPanel.add(eliminarProductoButton);

        JButton generarFacturaButton = new JButton("Generar Factura Final");
        buttonsPanel.add(generarFacturaButton);

        JButton verFacturasButton = new JButton("Ver Facturas");
        buttonsPanel.add(verFacturasButton);

        bottomPanel.add(buttonsPanel, BorderLayout.NORTH);

        JPanel totalsPanel = new JPanel(new GridLayout(1, 6, 10, 10));
        totalsPanel.setBackground(Color.BLUE);

        totalsPanel.add(new JLabel("SUMA"));
        sumaLabel = new JLabel("$0.00");
        totalsPanel.add(sumaLabel);

        totalsPanel.add(new JLabel("IVA"));
        ivaLabel = new JLabel("$0.00");
        totalsPanel.add(ivaLabel);

        totalsPanel.add(new JLabel("TOTAL"));
        totalLabel = new JLabel("$0.00");
        totalsPanel.add(totalLabel);

        bottomPanel.add(totalsPanel, BorderLayout.SOUTH);

        // Agregar el panel inferior al frame
        add(bottomPanel, BorderLayout.SOUTH);

        // Configurar los listeners de los botones
        agregarProductoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarProducto();
            }
        });

        actualizarProductoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarProducto();
            }
        });

        eliminarProductoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminarProducto();
            }
        });

        generarFacturaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generarFacturaFinal();
            }
        });

        verFacturasButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verFacturas();
            }
        });

        buscarProductoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buscarProducto();
            }
        });

        // Mostrar la ventana
        setVisible(true);
    }

    private void conectarBaseDatos() {
        String url = "jdbc:postgresql://localhost:5432/facturas";
        String usuario = "postgres";
        String contraseña = "root234";

        try {
            connection = DriverManager.getConnection(url, usuario, contraseña);
            System.out.println("¡Conexión exitosa!");
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
        }
    }

    private void agregarProducto() {
        String producto = productoField.getText();
        if (!productoExiste(producto)) {
            JOptionPane.showMessageDialog(this, "Producto no existe");
            return;
        }

        String descripcion = descripcionField.getText();
        int cantidad = Integer.parseInt(cantidadField.getText());
        double valorUnitario = Double.parseDouble(valorUnitarioField.getText());
        String iva = (String) ivaComboBox.getSelectedItem();

        double subtotal = cantidad * valorUnitario;
        DefaultTableModel model = (DefaultTableModel) productosTable.getModel();
        model.addRow(new Object[]{producto, descripcion, cantidad, valorUnitario, subtotal});

        calcularTotales();
    }

    private void actualizarProducto() {
        int selectedRow = productosTable.getSelectedRow();
        if (selectedRow != -1) {
            DefaultTableModel model = (DefaultTableModel) productosTable.getModel();
            model.setValueAt(productoField.getText(), selectedRow, 0);
            model.setValueAt(descripcionField.getText(), selectedRow, 1);
            model.setValueAt(cantidadField.getText(), selectedRow, 2);
            model.setValueAt(valorUnitarioField.getText(), selectedRow, 3);
            model.setValueAt(Integer.parseInt(cantidadField.getText()) * Double.parseDouble(valorUnitarioField.getText()), selectedRow, 4);

            calcularTotales();
        }
    }

    private void eliminarProducto() {
        int selectedRow = productosTable.getSelectedRow();
        if (selectedRow != -1) {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar este producto?", "Confirmar eliminación", JOptionPane.OK_CANCEL_OPTION);
            if (confirm == JOptionPane.OK_OPTION) {
                DefaultTableModel model = (DefaultTableModel) productosTable.getModel();
                model.removeRow(selectedRow);
                calcularTotales();
            }
        }
    }

    private void calcularTotales() {
        DefaultTableModel model = (DefaultTableModel) productosTable.getModel();
        double suma = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            suma += (double) model.getValueAt(i, 4);
        }

        double iva = 0;
        switch ((String) ivaComboBox.getSelectedItem()) {
            case "8%":
                iva = suma * 0.08;
                break;
            case "12%":
                iva = suma * 0.12;
                break;
            case "15%":
                iva = suma * 0.15;
                break;
        }
        double total = suma + iva;

        sumaLabel.setText(String.format("$%.2f", suma));
        ivaLabel.setText(String.format("$%.2f", iva));
        totalLabel.setText(String.format("$%.2f", total));
    }
    
private void generarFacturaFinal() {
    String facturaNo = facturaNoField.getText();
    String cliente = clienteField.getText();
    String fechaString = fechaLabel.getText();
    double suma = Double.parseDouble(sumaLabel.getText().replace("$", ""));
    double iva = 15; // IVA is always 15
    double total = Double.parseDouble(totalLabel.getText().replace("$", ""));
    String formaPago = (String) formaPagoComboBox.getSelectedItem();
    double descuento = 0; // FACDESCUENTO is always 0
    double ice = 0; // FACICE is always 0
    String status = "ACT"; // FACSTATUS is always ACT

    try {
        // Convert the fecha string to a java.sql.Date object
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date parsedDate = dateFormat.parse(fechaString);
        java.sql.Date fecha = new java.sql.Date(parsedDate.getTime());

        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO facturas (FACNUMERO, CLICODIGO, FACFECHA, FACSUBTOTAL, FACDESCUENTO, FACIVA, FACICE, FACFORMAPAGO, FACSTATUS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        ps.setString(1, facturaNo);
        ps.setString(2, cliente);
        ps.setDate(3, fecha); // Set the date object
        ps.setDouble(4, suma);
        ps.setDouble(5, descuento);
        ps.setDouble(6, iva);
        ps.setDouble(7, ice);
        ps.setString(8, formaPago);
        ps.setString(9, status);
        ps.executeUpdate();

        JOptionPane.showMessageDialog(this, "Factura generada correctamente");
    } catch (SQLException | java.text.ParseException e) {
        e.printStackTrace();
    }
}

    private void verFacturas() {
        JFrame verFacturasFrame = new JFrame("Ver Facturas");
        verFacturasFrame.setSize(800, 600);
        verFacturasFrame.setLayout(new BorderLayout(10, 10));

        JTable facturasTable = new JTable(new DefaultTableModel(new Object[][]{}, new String[]{
                "Número", "Cliente", "Fecha", "Subtotal", "Descuento", "IVA", "ICE", "Forma de Pago", "Estado"}));
        JScrollPane scrollPane = new JScrollPane(facturasTable);
        verFacturasFrame.add(scrollPane, BorderLayout.CENTER);

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT FACNUMERO, CLICODIGO, FACFECHA, FACSUBTOTAL, FACDESCUENTO, FACIVA, FACICE, FACFORMAPAGO, FACSTATUS FROM FACTURAS");

            DefaultTableModel model = (DefaultTableModel) facturasTable.getModel();
            while (rs.next()) {
                String numero = rs.getString("FACNUMERO");
                String cliente = rs.getString("CLICODIGO");
                Date fecha = rs.getDate("FACFECHA");
                double subtotal = rs.getDouble("FACSUBTOTAL");
                double descuento = rs.getDouble("FACDESCUENTO");
                double iva = rs.getDouble("FACIVA");
                double ice = rs.getDouble("FACICE");
                String formaPago = rs.getString("FACFORMAPAGO");
                String estado = rs.getString("FACSTATUS");

                model.addRow(new Object[]{numero, cliente, fecha, subtotal, descuento, iva, ice, formaPago, estado});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        verFacturasFrame.setVisible(true);
    }

    private void cargarProductoDesdeCodigo(String codigo) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT PRODESCRIPCION, PROPRECIOUM FROM productos WHERE PROCODIGO = ?");
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                descripcionField.setText(rs.getString("PRODESCRIPCION"));
                valorUnitarioField.setText(String.valueOf(rs.getDouble("PROPRECIOUM")));
            } else {
                JOptionPane.showMessageDialog(this, "Producto no encontrado");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void buscarProducto() {
        JFrame buscarFrame = new JFrame("Buscar Producto");
        buscarFrame.setSize(600, 400);
        buscarFrame.setLayout(new BorderLayout(10, 10));

        JTable buscarTable = new JTable(new DefaultTableModel(new Object[][]{}, new String[]{"PROCODIGO", "PRODESCRIPCION", "PROPRECIOUM", "PROSTATUS"}));
        JScrollPane scrollPane = new JScrollPane(buscarTable);
        buscarFrame.add(scrollPane, BorderLayout.CENTER);

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT PROCODIGO, PRODESCRIPCION, PROPRECIOUM, PROSTATUS FROM productos");

            DefaultTableModel model = (DefaultTableModel) buscarTable.getModel();
            while (rs.next()) {
                String codigo = rs.getString("PROCODIGO");
                String descripcion = rs.getString("PRODESCRIPCION");
                double precio = rs.getDouble("PROPRECIOUM");
                String status = rs.getString("PROSTATUS");

                model.addRow(new Object[]{codigo, descripcion, precio, status});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        buscarTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && buscarTable.getSelectedRow() != -1) {
                    DefaultTableModel model = (DefaultTableModel) buscarTable.getModel();
                    productoField.setText((String) model.getValueAt(buscarTable.getSelectedRow(), 0));
                    cargarProductoDesdeCodigo(productoField.getText());
                    buscarFrame.dispose();
                }
            }
        });

        buscarFrame.setVisible(true);
    }

    private boolean productoExiste(String codigo) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM productos WHERE PROCODIGO = ?");
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        new Facturas();
    }
}
