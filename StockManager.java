package finalExam;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.sql.*;

class myTextArea extends JTextArea{
	myTextArea(){
		Font font = new Font("Verdana", Font.BOLD, 15);
		setFont(font);
		setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		setColumns(20);
	}
	public void clear() {
		setText("");
	}
	 public String get() {
		 return getText().trim();
	 }
}

class myLabel extends JLabel{
	myLabel(String name){
		super(name);
		Font font = new Font("Verdana", Font.BOLD, 15);
		setFont(font);
	}
}

class availabilityPanel extends JOptionPane{
	availabilityPanel(String name, int n){
		if(n <= 0) n = 0;
		JFrame f = new JFrame();
		String message;
		if(name.equals("")) {
			message = "Enter product name";
		}
		else {
			message = name + " has " + n + " units in stock";
		}
		showMessageDialog(f,message);
	}
}

public class StockManager extends JFrame implements ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	StockManager(){
		super("Stock Manager");
		try(Connection con = DriverManager.getConnection("jdbc:derby:stocks;create=true")){
			Statement st = con.createStatement();
			st.executeUpdate("create table stocks (name varchar(50),  quantity int)");
		}
		catch(Exception ex1) {
			System.out.println(ex1);
			System.out.println("Table exists");
		}
		
		JPanel availabilityPanel = new JPanel();
		JButton availabilityButton = new JButton("Check");
		JLabel availabityLabel = new myLabel("Check Availability");
		JLabel availabityLabelName = new JLabel("Enter product name");
		myTextArea availabilityProduct = new myTextArea();
		
		availabilityProduct.setColumns(20);
		availabilityPanel.add(availabityLabel);
		availabilityPanel.add(new JLabel("     "));
		availabilityPanel.add(availabityLabelName);
		availabilityPanel.add(availabilityProduct);
		availabilityPanel.add(new JLabel("     "));
		availabilityPanel.add(availabilityButton);
		availabilityPanel.setLayout(new GridLayout(6,1));
		
		availabilityButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String stock = availabilityProduct.getText().trim();
				new availabilityPanel(stock, isAvailable(stock));
				availabilityProduct.clear();
			}
		});
		
		JPanel updatePanel = new JPanel();
		JLabel updateLabel = new myLabel("Update Stocks   ");
		JLabel updateLabelName = new JLabel("Enter stock name   ");
		JLabel updateLabelQuantity = new JLabel("Enter quantity   ");
		myTextArea updateProduct = new myTextArea();
		myTextArea updateQuantity = new myTextArea();
		JButton updateButton = new JButton("Update Stocks   ");
		
		updatePanel.add(updateLabel);
		updatePanel.add(updateLabelName);
		updatePanel.add(updateProduct);
		updatePanel.add(updateLabelQuantity);
		updatePanel.add(updateQuantity);
		updatePanel.add(updateButton);
		updatePanel.setLayout(new GridLayout(6,1));
		
		JFrame frame = this;
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae){
				String productName = updateProduct.get();
				String quantity = updateQuantity.get();
				System.out.println(quantity);
				int q = Integer.parseInt(quantity);
				insertProduct(productName, q);
				updateProduct.clear();
				updateQuantity.clear();
				new StockManager();
				frame.setVisible(false);
			}
		});
		
		JPanel headerPanel = new JPanel();
		headerPanel.add(availabilityPanel);
		headerPanel.add(updatePanel);
		headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 20));
		add(headerPanel);
		
		JPanel bodyPanel = new JPanel();
		JButton orderButton = new JButton("Order");
		myTextArea orderProduct = new myTextArea();
		JLabel orderProductLabel = new myLabel("Order Product");
		myTextArea orderQuantity = new myTextArea();
		JPanel tempPanel = new JPanel();
		tempPanel.add(orderProductLabel);
		tempPanel.add(new JLabel("Enter name of product"));
		tempPanel.add(orderProduct);
		tempPanel.add(new JLabel("Enter quantity of product"));
		tempPanel.add(orderQuantity);
		tempPanel.add(orderButton);
		tempPanel.setLayout(new GridLayout(6,1));
		bodyPanel.add(tempPanel);
		headerPanel.add(bodyPanel);
		
		orderButton.addActionListener(new ActionListener()  {
			public void actionPerformed(ActionEvent ae) {
				String product = orderProduct.get();
				orderProduct.clear();
				String quantity = orderQuantity.get();
				orderQuantity.clear();
				orderProduct(product, Integer.parseInt(quantity));
				new StockManager();
				frame.setVisible(false);
			}
		});
		
		JTable table;
		ResultSet rs = null;
		try {
			rs = getProducts();
			ResultSetMetaData metadata = rs.getMetaData(); 
			int numberOfColumns = metadata.getColumnCount(); 
			int numberOfRows = 30;
			Object[][] resultSet = new Object[numberOfRows][numberOfColumns];
	        int row = 0;
	        while (rs.next()) {
	            for (int i = 0; i < numberOfColumns; i++) {
	                resultSet[row][i] = rs.getObject(i+1);
	            }
	            row++;
	        }
	        String[] heading  = new String[] {"Product", "Quantity in stock"};
	        table = new JTable(resultSet, heading);
	        JScrollPane jsp = new JScrollPane(table);
	        add(jsp);
		}
		catch(Exception ex) {
			System.out.println(ex);
		}
		
		setLayout(new GridLayout(2,2));
		setSize(1100, 650);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public static int isAvailable(String stockName) {
		
		try {
			int n = 0;
			Connection con = DriverManager.getConnection("jdbc:derby:stocks;create=true");
			PreparedStatement ps = con.prepareStatement("Select quantity from stocks where name = ?");
			ps.setString(1,  stockName);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				n = rs.getInt(1);
			}
			con.close();
			return n;
		}
		catch(Exception ex) {
			System.out.println("error in available fun");
			return -1;
		}
	}
	
	public static void orderProduct(String product, int n) {
		try {
			int stock = isAvailable(product);
			if(stock == -1 || stock == 0) {
				new availabilityPanel(product, 0);
			}
			else {
				Connection con = DriverManager.getConnection("jdbc:derby:stocks;create=true");
				PreparedStatement ps = con.prepareStatement("update stocks set quantity = ? where name = ?");
				ps.setString(2, product);
				ps.setInt(1, stock - n);
				ps.executeUpdate();
				con.close();
			}
		}
		catch(Exception ex) {
			System.out.println(ex);
		}
	}
	
	public static void insertProduct(String product, int stock){
		try {
			int n = isAvailable(product);
			if(n == -1 || n == 0) {
				Connection con = DriverManager.getConnection("jdbc:derby:stocks;create=true");
				PreparedStatement ps = con.prepareStatement("insert into stocks values (?, ?)");
				ps.setString(1, product);
				ps.setInt(2, stock);
				ps.executeUpdate();
				con.close();
			}
			else {
				Connection con = DriverManager.getConnection("jdbc:derby:stocks;create=true");
				PreparedStatement ps = con.prepareStatement("update stocks set quantity = ? where name = ?");
				ps.setString(2, product);
				ps.setInt(1, stock + n);
				ps.executeUpdate();
				con.close();
			}
		}
		catch(Exception ex) {
			System.out.println(ex);
		}

	}
	
	public static ResultSet getProducts() throws SQLException{
		Connection con = DriverManager.getConnection("jdbc:derby:stocks;create=true");
		//PreparedStatement ps = con.prepareStatement("select * from stocks where quantity <= 500"); // <= denotes 'lesser than'
		PreparedStatement ps = con.prepareStatement("select * from stocks order by name");
		ResultSet rs = ps.executeQuery();
		return rs;
	}
	
	public static void removeEmptyProduct() throws SQLException{
		Connection con = DriverManager.getConnection("jdbc:derby:stocks;create=true");
		Statement ps = con.createStatement();
		ps.executeQuery("delete from stocks where name = '' or name is null");
	}
	
	public static void main(String args[]) {
		new StockManager();
	}
}
