import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.lekan.graphics.GraphicsProgram;
import org.lekan.graphics.SGText;


/**
 * 
 * @author Jordan Timmerman
 *
 */
public class EPGYAuthor extends GraphicsProgram {
	public static void main(String[] args) {
		new EPGYAuthor();
	}
	
	private static final int FRAME_WIDTH = 0;
	private static final int FRAME_HEIGHT = 0;
	private static final String GENERATE_TEXT = "Generate Text!";
	private static final String CREATE_MODEL = "Create Model";
	private static final int NUM_COLUMNS = 50;
	
	private static final SGText INPUT_LOW_WARNING = new SGText("Input must be positive.", 10, 10);
	private static final SGText INPUT_HIGH_WARNING = new SGText("Input must not be greater than 15.", 10, 10);
	private static final SGText LACK_OF_INPUT_WARNING = new SGText("Input field cannot be empty.", FRAME_WIDTH-100, FRAME_HEIGHT-10);
	private static JTextField inputField = new JTextField(NUM_COLUMNS);
	private static final int MAX_ORDER = 15;
	private static JTextArea outputField = new JTextArea("Input a number between 1 and "+MAX_ORDER+" and press create model. Then press generate text!", 3, 100);
	private static JButton createModel = new JButton(CREATE_MODEL);
	private static JButton generateText = new JButton(GENERATE_TEXT);
	private static final String FILE_NAME = "all-texts";
	private static boolean warningShown = false;
	private static boolean inputLowWarning = false;
	private static boolean inputHighWarning = false;
	
	HashMap<String, List<Character>> map = new HashMap<String, List<Character>>();
	
	
	/**
	 * Comments! 
	 */
	@Override
	public void setup() {
		INPUT_LOW_WARNING.setColor(Color.red);
		INPUT_HIGH_WARNING.setColor(Color.red);
		this.getFrame().setDimensions(FRAME_WIDTH, FRAME_HEIGHT);
		this.addTextField(inputField, SOUTH);
		this.addJComponent(createModel, SOUTH);
		this.addJComponent(generateText, SOUTH);
		this.addJComponent(outputField, NORTH);
		outputField.setEditable(false);
		outputField.setLineWrap(true);

		generateText.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals(GENERATE_TEXT)){
					if(inputField.getText().length()<1) {
						if(!warningShown){
							getFrame().addObject(LACK_OF_INPUT_WARNING);
							warningShown = true;
						}
					}
					else{
						if(warningShown==true) {
							getFrame().removeObject(LACK_OF_INPUT_WARNING);
							warningShown=false;
						}
						int input = Integer.parseInt(inputField.getText());
						if(input<0){
							System.out.println("Order must be positive.");
							if(!inputLowWarning){
								getFrame().addObject(LACK_OF_INPUT_WARNING);
								warningShown = true;
								getFrame().removeObject(INPUT_HIGH_WARNING);
								getFrame().addObject(INPUT_LOW_WARNING);
								inputLowWarning=true;
							}
						}
						else if(input>MAX_ORDER){
							if(inputLowWarning) {
								getFrame().removeObject(INPUT_LOW_WARNING);
								inputLowWarning=false;
							}
							System.out.println("Order must be less than "+MAX_ORDER+".");
							if(!inputHighWarning){
								getFrame().removeObject(INPUT_LOW_WARNING);
								getFrame().addObject(INPUT_HIGH_WARNING);
								inputHighWarning=true;
							}
						}
						else{
							if (inputHighWarning) {
								inputHighWarning=false;
								getFrame().removeObject(INPUT_HIGH_WARNING);
							}
							try {
								map = createModel(FILE_NAME, input, map);
							} catch (FileNotFoundException e1) {
								e1.printStackTrace();
							}
							generateText(map);
						}
					}
				}

			}
		});
	}
	/**
	 * Handle the button presses.
	 */
	public void actionPerformed(ActionEvent e) {
		
		if(e.getActionCommand().equals(CREATE_MODEL)){
			int input = Integer.parseInt(inputField.getText());
			long startTime = 0;
			if(input<0){
				System.out.println("Order must be positive.");
				this.getFrame().removeObject(INPUT_HIGH_WARNING);
				this.getFrame().addObject(INPUT_LOW_WARNING);
				startTime = System.currentTimeMillis();
			}
			else if(input>MAX_ORDER){
				System.out.println("Order must be less than "+MAX_ORDER+".");
				this.getFrame().removeObject(INPUT_LOW_WARNING);
				this.getFrame().addObject(INPUT_HIGH_WARNING);
				startTime = System.currentTimeMillis();
			}
			else{
				try {
					map = createModel(FILE_NAME, input, map);
					outputField.setText("Model created.");
				
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
			while(System.currentTimeMillis() - startTime < 1500){
				createModel.setEnabled(false);
			}
			createModel.setEnabled(true);
		}
		else if(e.getActionCommand().equals(GENERATE_TEXT)){
			if(map!=null) generateText(map);
		}
	}
	
	public HashMap<String, List<Character>> createModel(String file, int order, HashMap<String, List<Character>> map) throws FileNotFoundException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		char[] buffer = new char[order+1];
		
		try {
			while(bufferedReader.ready()){
				bufferedReader.read(buffer, 0, 1);
				bufferedReader.mark(order+1);
				bufferedReader.read(buffer, 1, order);
				
				String nextOrderSequence = String.valueOf(Arrays.copyOf(buffer, order));
				
				if(map.containsKey(nextOrderSequence)){
					map.get(nextOrderSequence).add(buffer[order]);
				}
				else {
					List<Character> l = new ArrayList<Character>();
					l.add(buffer[order]);
					map.put(nextOrderSequence, l);
				}
				bufferedReader.reset();
			}
			bufferedReader.close();
			return map; 
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void generateText(HashMap<String, List<Character>> map){
		Random rnd = new Random();
		int randomKey = rnd.nextInt(map.size());
		String key = map.keySet().toArray(new String[map.size()])[randomKey];
		String result = key;
		
		int randomValue = 0;
		int keys = 1;
		
		while(!result.endsWith(".")){
			randomValue = rnd.nextInt(map.get(key).size());
			char value = (Character) map.get(key).toArray()[randomValue];
			result+=value;
			key = result.substring(keys);
			keys++;
		}
		
		StringBuilder sb = new StringBuilder();
		for(char c : result.toCharArray()){
			if(c=='\n') sb.append(" ");
			else if(c=='\r') sb.append(" ");
			else if(c!='\'' && c!='\"') sb.append(c);
		}
		
		outputField.setText(sb.toString());
		
	}
}
