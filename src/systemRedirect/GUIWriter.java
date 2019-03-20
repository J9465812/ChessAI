package systemRedirect;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;


class GUIWriter extends OutputStream{
	
	private JTextArea area;
	
	public GUIWriter(JTextArea area){
		
		this.area = area;
	}

	@Override
	public void write(int b) throws IOException {
		
		char c = Character.toChars(b)[0];
		
		area.append(Character.toString(c));
	}
}
