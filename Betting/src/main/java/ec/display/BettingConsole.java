package ec.display;

import java.awt.FileDialog;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.xml.transform.Source;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BettingConsole extends Console {

	private Map<String,Source> sources;
	
	public BettingConsole(String[] clArgs) throws HeadlessException {
		super(clArgs);
		/*
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
				new String[] { "./META-INF/spring/applicationContext.xml" });
		// of course, an ApplicationContext is just a BeanFactory
		BeanFactory factory = (BeanFactory) appContext;
		EntityManagerFactory emf = (EntityManagerFactory) factory
				.getBean("entityManagerFactory");
		//priceService = (PriceService) factory.getBean("priceService");
		//TODO: sources = (Map<String,Source>) factory.getBean("sources");
		*/
		try {
			System.out.println("Current dir: "
					+ new File(".").getCanonicalPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		File f = new File(
				"./src/main/java/ec/app/betting/betting.params");
		loadParameters(f);
		playButton.setEnabled(true);
		stepButton.setEnabled(true);
		conPanel.enableControls();

		this.jJMenuBar.add(getSharesMenu());

	}

	public JMenu getSharesMenu() {
		JMenu m = new JMenu("Shares");
		m.add(getImportPricesMenuItem("FTSE"));
		m.add(getImportPricesMenuItem("DOW"));
		m.add(getImportPricesMenuItem("NIKKEI"));
		m.add(getImportPricesMenuItem("HSI"));
		return m;

	}

	public JMenuItem getImportPricesMenuItem(final String symbol) {
		JMenuItem i = new JMenuItem("Import " + symbol);
		i.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				FileDialog fileDialog = new FileDialog(BettingConsole.this,
						"Open...", FileDialog.LOAD);
				fileDialog.setDirectory(System.getProperty("user.dir"));
				fileDialog.setFile("*.csv");
				fileDialog.setVisible(true);
				String fileName = fileDialog.getFile();
				while (fileName != null && !fileName.endsWith(".csv")) {
					JOptionPane optPane = new JOptionPane(fileDialog.getFile()
							+ " is not a legal parameters file",
							JOptionPane.ERROR_MESSAGE);
					JDialog optDialog = optPane.createDialog(
							BettingConsole.this, "Error!");
					optDialog.setVisible(true);
					fileDialog.setFile("*.csv");
					fileDialog.setVisible(true);
					fileName = fileDialog.getFile();
				}

				if (fileName != null) {
					File f = new File(fileDialog.getDirectory(), fileName);
					System.out.println(f);
					
					BettingConsole.this.getStatusField().setText("Importing...");
					// int c = priceService.importCsv(f, symbol);
					// SharesConsole.this.getStatusField().setText("Imported "+c+" "+symbol+" prices" );
				}
			}

		});
		return i;

	}	
	
	public static void main(String[] args) {

		BettingConsole application = new BettingConsole(args);
		application.setVisible(true);
	}

}
