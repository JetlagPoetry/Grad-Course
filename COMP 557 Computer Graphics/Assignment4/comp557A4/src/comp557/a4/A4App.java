package comp557.a4;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

@SuppressWarnings("serial")
public class A4App extends JFrame implements Runnable {	
	
	/**
	 * Entry point for the application, either renders the file specified as
	 * the first argument, or opens a simple interface listing all scene files
	 * in the a4data directory.
	 * @param args The first argument should be the filename of the scene XML file.
	 */
	public static void main(String[] args) {	
		Locale.setDefault(Locale.ENGLISH); // make sure things parse properly
		A4App app = new A4App();
		if (args.length == 0) {
			app.openSceneList();
		} else {
			app.renderFile(args[0]);
		}
	}

	private JList<File> listbox;
	private JPanel topPanel;

	public A4App() {}
	
	public void openSceneList(){
		File folder = new File("a4data");
		final File[] listOfFiles = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".xml");
			}
		});
		if ( listOfFiles == null || listOfFiles.length == 0 ) {
			System.err.println("No xml files in a4data directory?");
			System.exit(-1);
		}
		
		setTitle("Scenes");
		setSize(200, 500);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		getContentPane().add(topPanel);
		listbox = new JList<File>(listOfFiles);
		topPanel.add(listbox, BorderLayout.CENTER);
		setVisible(true);
		
		listbox.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		        JList<?> list = (JList<?>)evt.getSource();
		        if (evt.getClickCount() == 2) {
		            int index = list.locationToIndex(evt.getPoint());
		            if (listOfFiles[index].getName().endsWith(".xml")){
		            	renderFile(listOfFiles[index].getAbsolutePath());
		            }
		        }
		    }
		});
	}
	
	private String currentFile;
	
	/**
	 * Renders the scene in a separate thread
	 * @param path
	 */
	public void renderFile(String path) {
		currentFile = path;
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		try {
			SceneNode.nodeMap.clear();
			InputStream inputStream = new FileInputStream( new File(currentFile) );
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse( inputStream );
			
			long pstart = System.nanoTime();

			Scene scene = Parser.createScene( document.getDocumentElement() );
			
			long rstart = System.nanoTime();
			
			scene.render(true);
			
			double ptime = (double)((rstart-pstart)/1E9);
			double rtime = (double)((System.nanoTime()-rstart)/1E9);
			
			String filename = currentFile.substring(currentFile.lastIndexOf(File.separator)+1);
			System.out.println(filename + ": Parse=" + ptime + " | Render=" + rtime + " | Total=" + (ptime+rtime));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load simulation input file.", e);
		}
	}
}