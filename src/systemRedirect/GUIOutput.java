package systemRedirect;

import java.awt.BorderLayout;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;


public class GUIOutput {
	
	
	/**
	 * The default System output.
	 */
	private static final PrintStream out = System.out;
	
	/**
	 * The default System error output.
	 */
	private static final PrintStream err = System.err;
	
	/**
	 * Creates a window named "Output" and redirects <code>System.out</code> and 
	 * <code>System.err</code> to the window.
	 * <p>
	 * The window has two <code>JTextAreas</code> arranged vertically.
	 * The top one contains the text written to <code>System.out</code>
	 * and the bottom one contains text written to <code>System.err</code>
	 * 
	 */
	
	public static void redirectAllSystemOutputToNewFrame(){
		
		JFrame frame = new JFrame("Out/Err");
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JTextArea outArea = new JTextArea(13, 40);
		JScrollPane out = new JScrollPane(outArea);
		
		outArea.setLineWrap(true);
		out.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		out.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		outArea.setEditable(false);
		
		JTextArea errArea = new JTextArea(13, 40);
		JScrollPane err = new JScrollPane(errArea);
		
		errArea.setLineWrap(true);
		err.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		err.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		errArea.setEditable(false);
		
		redirectSystemOutTo(outArea);
		redirectSystemErrTo(errArea);
		
		frame.add(err, BorderLayout.SOUTH);
		frame.add(out, BorderLayout.CENTER);
		
		frame.pack();
		frame.setVisible(true);
	}

	public static void redirectSystemOutToNewFrame(){

		JFrame frame = new JFrame("Out");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JTextArea outArea = new JTextArea(13, 40);
		JScrollPane out = new JScrollPane(outArea);

		outArea.setLineWrap(true);
		out.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		out.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		outArea.setEditable(false);

		redirectSystemOutTo(outArea);

		frame.add(out);

		frame.pack();
		frame.setVisible(true);
	}

	public static void redirectSystemErrToNewFrame(){

		JFrame frame = new JFrame("Err");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JTextArea errArea = new JTextArea(13, 40);
		JScrollPane err = new JScrollPane(errArea);

		errArea.setLineWrap(true);
		err.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		err.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		errArea.setEditable(false);

		redirectSystemErrTo(errArea);

		frame.add(err);

		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * Redirects <code>System.out</code> and 
	 * <code>System.err</code> to <code>area</code>.
	 * 
	 * @param area the <code>JTextArea</code> to redirect the consol to.
	 */
	
	public static void redirectAllSystemOutputTo(JTextArea area){
		
		redirectSystemOutTo(area);
		redirectSystemErrTo(area);
	}
	
	/**
	 * Redirects <code>System.out</code> to <code>area</code>.
	 * 
	 * @param area the <code>JTextArea</code> to redirect the <code>System.out</code> to.
	 */
	
	public static void redirectSystemOutTo(JTextArea area){
		
		GUIWriter outWriter = new GUIWriter(area);
		
		System.setOut(new PrintStream(outWriter));
	}
	
	/**
	 * Redirects <code>System.err</code> to <code>area</code>.
	 * 
	 * @param area the <code>JTextArea</code> to redirect the <code>System.err</code> to.
	 */
	
	public static void redirectSystemErrTo(JTextArea area){
		
		GUIWriter errWriter = new GUIWriter(area);
		
		System.setErr(new PrintStream(errWriter));
	}
	
	/**
	 * Sets <code>System.out</code> and <code>System.err</code> to the default PrintStreams
	 */
	
	public static void resetSystemOutput(){
		
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(err));
	}
}
