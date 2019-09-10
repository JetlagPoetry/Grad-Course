package comp557.a1;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import com.jogamp.opengl.GLAutoDrawable;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.swing.HorizontalFlowPanel;
import mintools.swing.VerticalFlowPanel;

/**
 * @author 260860682 Jingyuan Wang
 */
public class KeyFramedScene {


    /** 120 frames at 30 frames per section for 4 seconds of animation */
    private final int NUM_FRAMES = 120;
    
    /** The root of the scene graph / character */
    private DAGNode root = null;
    
    /** Master list of degress of freedom */
    private List<DoubleParameter> dofList = new ArrayList<DoubleParameter>();

    /** key poses are null when not set */
    private ArrayList<double[]> keyPoses = new ArrayList<double[]>( NUM_FRAMES );

    /** Time line slide control for selecting the animation frame */
    private JSlider keyFrameSlider = new JSlider( 0, NUM_FRAMES - 1, 0 );

    /** Labels for the time line slider to show where keyframes are set */ 
    private Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();

    /** Diamon symbol used to denote a key frame */
    private final JLabel diamond = new JLabel("\u2666");
    
    /** Empty label to avoid problems when no keyframes are set */
    private final JLabel emptyLabel = new JLabel(" ");
    
    /** Current frame label */
    private final JLabel frameLabel = new JLabel("0");

    private BooleanParameter animate = new BooleanParameter("animate", false );

    private final JTextField keyFramesName = new JTextField("keyposes");
    
    private JPanel vfpPosePanel; 
    
    private VerticalFlowPanel vfpPose = new VerticalFlowPanel();

	public KeyFramedScene() {
    	// initialize the key frames array to have no key frames
    	for ( int i = 0; i < NUM_FRAMES; i++ ) {
    		keyPoses.add( null );
    	}
    	createCharacter();
	}
	
	public void display( GLAutoDrawable drawable ) {
        if ( animate.getValue() ) {
        	keyFrameSlider.setValue( (keyFrameSlider.getValue() + 1) % NUM_FRAMES );
        } 
        if ( root != null ) root.display(drawable);
	}
	
    public JPanel getControls() {
    	VerticalFlowPanel vfp = new VerticalFlowPanel();
    	
    	JButton recreate = new JButton("recreate character");
    	recreate.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createCharacter();
			}
		});
    	vfp.add( CharacterCreator.loadFromFile.getControls() );
    	vfp.add( new JLabel("Base filename for character and keyframes:") );
    	vfp.add( CharacterCreator.baseFileName );
    	vfp.add( new JLabel("keyframe sequenc name (for saving and loading):") );
    	vfp.add( keyFramesName );
    	vfp.add( recreate );
    	
    	HorizontalFlowPanel hfp = new HorizontalFlowPanel();
    	hfp.add( DAGNode.debugFrames.getControls() );
    	hfp.add( animate.getControls() );
    	hfp.add( new JLabel("      [30 FPS] Frame "));
    	hfp.add( frameLabel );
    	vfp.add( hfp.getPanel() );
    	
    	labels.put( 0, emptyLabel );
    	keyFrameSlider.setLabelTable(labels);
    	keyFrameSlider.setPaintLabels(true);
    	vfp.add( keyFrameSlider );
    	
    	keyFrameSlider.addChangeListener( new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updatePose();
			}
		});
    	
    	JButton prev = new JButton("Prev");
    	prev.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				prevKeyFrame();
			}
		});
    	JButton next = new JButton("Next");
    	next.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextKeyFrame();
			}
		});
    	JButton delete = new JButton("Delete");
    	delete.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteKeyFrame();
			}
		});
    	JButton set = new JButton("Set");
    	set.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setKeyFrame();
			}
		});
    	JButton copy = new JButton("Copy");
    	copy.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyPose();
			}
		});
		JButton paste = new JButton("Paste");
		paste.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pasteKeyFrame();
			}
		});
    	JButton load = new JButton("Load");
    	load.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadKeyFrames();
			}
		});
    	JButton save = new JButton("Save");
    	save.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveKeyFrames();
			}
		});
    	    	
    	JPanel p = new JPanel( new GridLayout(1,8) );
    	p.add( prev );
    	p.add( next );
    	p.add( set );
    	p.add( delete );
    	p.add( copy );
    	p.add( paste );
    	p.add( load );
    	p.add( save );
    	vfp.add(p);
    	
    	// this button is stupid! remove it!
//    	JButton rnd = new JButton("rnd");
//    	rnd.addActionListener( new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				for ( DoubleParameter d : dofList ) {
//					final Random r = new Random();
////					double v = (r.nextDouble() - 0.5) / 10;
////					double max = d.getMaximum();
////					double min = d.getMinimum();
////					d.setValue( (min+max)/2 + (max-min)*v );
//					double max = d.getMaximum();
//					double min = d.getMinimum();
//					d.setValue(r.nextInt((int)(max-min+1)) + min);
//				}
//			}
//		});
//    	vfp.add( rnd );
    		
    	vfpPose.setBorder( new TitledBorder("Pose") );
    	vfpPosePanel = vfpPose.getPanel();
    	vfp.add( vfpPosePanel );
    	return vfp.getPanel();
    }

	
    /** 
     * Creates the character.
     * This can be called multiple times (i.e., when making edits and running in debug mode,
     * or if the CharacteCreator loads from a file, or creates different characters for whatever
     * reason).  Keyframes are deleted, and dofs are cleared and recreated from the new DAG.  
     * Notice that the pose sliders interface is also rebuilt.
     */
    public void createCharacter() {
    	deleteAllKeyFrames();
        dofList.clear();
        vfpPose.removeAll();
    	root = CharacterCreator.create();
    	if ( root == null ) {
    		// nothing loaded... do nothing and hope for the best!
    		return;
    	}
    	root.getDOFs( dofList );
    	int labelWidth = DoubleParameter.DEFAULT_SLIDER_LABEL_WIDTH;
    	int textWidth = DoubleParameter.DEFAULT_SLIDER_TEXT_WIDTH;
    	DoubleParameter.DEFAULT_SLIDER_LABEL_WIDTH = 50;
    	DoubleParameter.DEFAULT_SLIDER_TEXT_WIDTH = 50;
    	vfpPose.add( root.getControls() );    	
    	if (vfpPosePanel != null ) {
    		vfpPosePanel.updateUI();
    	}
    	DoubleParameter.DEFAULT_SLIDER_LABEL_WIDTH = labelWidth;
    	DoubleParameter.DEFAULT_SLIDER_TEXT_WIDTH = textWidth;
    }
        
    public void setKeyFrame() {
    	int index = keyFrameSlider.getValue();
    	double[] key = keyPoses.get(index);
    	if ( key == null ) {
    		key = new double[dofList.size()];
    		keyPoses.set(index, key);
    		labels.put( index, diamond );
        	keyFrameSlider.setLabelTable(labels);
        	keyFrameSlider.updateUI();
    	}
        for ( int i = 0; i < dofList.size(); i++ ) {
        	key[i] = dofList.get(i).getValue();                
        }
    }
    
    private double[] copiedPose = null;
    
    public void pasteKeyFrame() {
    	if ( copiedPose == null ) return;
    	int index = keyFrameSlider.getValue();
    	double[] key = keyPoses.get(index);
    	if ( key == null ) {
    		key = new double[dofList.size()];
    		keyPoses.set(index, key);
    		labels.put( index, diamond );
        	keyFrameSlider.setLabelTable(labels);
        	keyFrameSlider.updateUI();
    	}
    	System.arraycopy( copiedPose, 0, key, 0, copiedPose.length );
    	updatePose();
    }

    public void copyPose() {
    	if ( copiedPose == null ) copiedPose = new double[dofList.size()];
    	for ( int i = 0; i < dofList.size(); i++ ) {
        	copiedPose[i] = dofList.get(i).getValue();                
        }
    }
    
    public void deleteKeyFrame() {
    	int index = keyFrameSlider.getValue();
    	keyPoses.set(index,null);
    	if ( index == 0 ) {
    		// need to always keep one label or the JSlider throws an exception
    		labels.put(index, emptyLabel);
    	} else {
    		labels.remove(index);
    	}
    	keyFrameSlider.setLabelTable(labels);
    	keyFrameSlider.updateUI();
    }
    
    public void deleteAllKeyFrames() {
        for ( int i = 0; i < keyPoses.size(); i++ ) {
        	if ( keyPoses.get(i) != null ) {
        		keyPoses.set( i, null );
            	if ( i == 0 ) {
            		// need to always keep one label or the JSlider throws an exception
            		labels.put(i, emptyLabel);
            	} else {
            		labels.remove(i);
            	}
        	}
        }
    	keyFrameSlider.setLabelTable(labels);
    	keyFrameSlider.updateUI();
    }
    
    @SuppressWarnings("unchecked") 
	public void loadKeyFrames() {
    	keyPoses.clear();
        try {
        	String fileName = CharacterCreator.baseFileName.getText() + "-" +  keyFramesName.getText() + ".javabin";
            FileInputStream fis = new FileInputStream( new File( fileName ) );
            ObjectInputStream ois = new ObjectInputStream(fis);
            keyPoses = (ArrayList<double[]>) ois.readObject();                
            fis.close();
            ois.close();  
            labels.clear();
            for ( int i = 0 ; i < keyPoses.size(); i++ ) {
            	if ( keyPoses.get(i) != null ) {
            		labels.put( i, diamond );
            	}
            }
            if ( labels.isEmpty() ) {
            	labels.put( 0, emptyLabel );
            }
        	keyFrameSlider.setLabelTable(labels);
        	keyFrameSlider.updateUI();
        	keyFrameSlider.setValue(0);
        	updatePose();
        } catch ( Exception ex ) {
        	ex.printStackTrace();
        }
    }
    
    public void saveKeyFrames() {
    	try {
        	String fileName = CharacterCreator.baseFileName.getText() + "-" +  keyFramesName.getText() + ".javabin";
            FileOutputStream fos = new FileOutputStream( fileName );
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject( keyPoses );                
            fos.close();
            oos.close();                
        } catch ( Exception ex ) {
        	ex.printStackTrace();
        }
    }
    
    public void nextKeyFrame() {
    	int index = keyFrameSlider.getValue() + 1;
    	while ( index < NUM_FRAMES && keyPoses.get(index) == null ) {
    		index++;
    	}
    	if ( index < NUM_FRAMES ) {
    		keyFrameSlider.setValue( index );
    		for ( int i = 0; i < dofList.size(); i++ ) {
            	dofList.get(i).setValue( keyPoses.get(index)[i] );                
            }
    	}    
    }
    
    public void prevKeyFrame() {
    	int index = keyFrameSlider.getValue() - 1;
    	while ( index >= 0 && keyPoses.get(index) == null ) {
    		index--;
    	}
    	if ( index >= 0 ) {
    		keyFrameSlider.setValue( index );
    		for ( int i = 0; i < dofList.size(); i++ ) {
            	dofList.get(i).setValue( keyPoses.get(index)[i] );                
            }        
    	}
    }
    
    public void updatePose() {
		int index = keyFrameSlider.getValue();
		frameLabel.setText( Integer.toString(index) );
		if ( keyPoses.get(index) != null ) {
    		for ( int i = 0; i < dofList.size(); i++ ) {
            	dofList.get(i).setValue( keyPoses.get(index)[i] );                
            }        
    		return;
		}
		int prev = index-1;
		while ( prev >= 0 && keyPoses.get(prev) == null ) {
			prev--;
    	}
    	if ( prev >= 0 ) {
			int next = index+1;
			while ( next < NUM_FRAMES && keyPoses.get(next) == null ) {
				next++;
	    	}
	    	if ( next < NUM_FRAMES ) {
	    		double T = next - prev;
	    		double v = index - prev;
	    		double alpha = v / T;
	            for ( int i = 0; i < dofList.size(); i++ ) {
	            	DoubleParameter p = dofList.get(i);
	                double v1 = keyPoses.get(prev)[i];
	                double v2 = keyPoses.get(next)[i];
	                p.setValue( alpha*v2 + (1-alpha)*v1 );
	            }
	    	}
    	}
	}
    
}
