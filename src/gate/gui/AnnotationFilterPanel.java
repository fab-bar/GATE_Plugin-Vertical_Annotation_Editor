/**************************************
 * Copyright 2015 - Universität Hamburg, SIGS.
 * 
 * This file is part of the Vertical Annotation Editor, a plugin for GATE.
 *
 * Vertical Annotation Editor is free software: 
 * you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Vertical Annotation Editor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Der Vertical Annotation Editor ist Freie Software: 
 * Sie können es unter den Bedingungen der GNU General Public License, 
 * wie von der Free Software Foundation, Version 3 der Lizenz oder 
 * (nach Ihrer Wahl) jeder neueren veröffentlichten Version,
 * weiterverbreiten und/oder modifizieren.
 * Der Vertical Annotation Editor wird in der Hoffnung, 
 * dass es nützlich sein wird, aber OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; 
 * sogar ohne die implizite Gewährleistung der MARKTFÄHIGKEIT
 * oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
 * Siehe die GNU General Public License für weitere Details.
 * 
 * Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 * Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 * 
 */

package gate.gui;

import gate.FeatureMap;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * 
 * A panel that allows filtering of annotations.
 * It consists of a checkbox to activate or deactivate the filters
 * and the possibility to add and remove Feature-Value-pairs.
 * Listeners can 
 * 
 * @author Fabian Barteld
 *
 */
public class AnnotationFilterPanel extends JPanel implements ItemListener, DocumentListener {
	
	protected HashMap<Integer, JPanel> featureRestrictionList = new HashMap<Integer, JPanel>();
	protected JCheckBox filter_cb;
	protected ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
	
	// TODO: change layout
	public AnnotationFilterPanel() {
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		filter_cb = new JCheckBox("Activate filters", false);
		filter_cb.addItemListener(this);
		this.add(filter_cb);
		
		JButton addFeatureRestrictionButton = new JButton("Add Feature/Value");
		this.add(addFeatureRestrictionButton);
		
		addFeatureRestrictionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {            	

				JPanel FeaturePanel = new JPanel();
				FeaturePanel.setLayout(new BoxLayout(FeaturePanel, BoxLayout.X_AXIS));
				FeaturePanel.setBorder(BorderFactory.createEmptyBorder(5, 3, 5, 3));
				
				JTextField featureField = new JTextField();
				JTextField valueField = new JTextField();
				
				featureField.getDocument().addDocumentListener(AnnotationFilterPanel.this);
				valueField.getDocument().addDocumentListener(AnnotationFilterPanel.this);				
				
				FeaturePanel.add(featureField);
				FeaturePanel.add(valueField);
				JButton removeButton = new JButton("-");
				FeaturePanel.add(removeButton);
				
				featureRestrictionList.put(FeaturePanel.hashCode(), FeaturePanel);
				removeButton.setActionCommand(Integer.toString(FeaturePanel.hashCode()));
				
				AnnotationFilterPanel.this.add(FeaturePanel);
				AnnotationFilterPanel.this.revalidate();
				
				removeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {       
						
						int index = Integer.parseInt(arg0.getActionCommand());
						
						AnnotationFilterPanel.this.remove(featureRestrictionList.get(index));
						featureRestrictionList.remove(index);
						
						AnnotationFilterPanel.this.revalidate();
						AnnotationFilterPanel.this.notifyChangeListeners();
					}
				});
				
			}
		});
	}
	
	/**
	 * Adds a ChangeListener.
	 * 
	 * @param listener	the ChangeListener to be added
	 */
	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Removes a ChangeListener.
	 * 
	 * @param listener	the ChangeListener to be removed
	 */
	public void removeChangeListener(ChangeListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * 
	 * Returns whether the filter should be active or not.
	 * 
	 * @return state of the activation checkbox
	 */
	public boolean isActivated() {
		return filter_cb.isSelected();
	}
	
	/**
	 * 
	 * Getter for the filter texts.
	 * The filters are returned as FeatureValue-Pairs in a GATE FeatureMap.
	 * 
	 * @return a FeatureMap with all restrictions
	 */
	public FeatureMap getFeatureValueFilter() {

		FeatureMap featureValueRestrictions = gate.Utils.featureMap();
		for(JPanel panel:featureRestrictionList.values()) {
			featureValueRestrictions.put(
					((JTextField)panel.getComponent(0)).getText(),
					((JTextField)panel.getComponent(1)).getText());
		}
		return featureValueRestrictions;
	}
	
	protected void notifyChangeListeners() {
		for(ChangeListener listener:listeners) {
			listener.stateChanged(new ChangeEvent(this));
		}
	}
	
	@Override
	public void itemStateChanged(ItemEvent arg0) {
		this.notifyChangeListeners();
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		this.notifyChangeListeners();
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		this.notifyChangeListeners();
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		this.notifyChangeListeners();
	}
}
